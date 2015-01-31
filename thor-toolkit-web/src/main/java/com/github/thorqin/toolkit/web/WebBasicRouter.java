package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.Validator;
import com.github.thorqin.toolkit.web.annotation.WebModule;
import com.github.thorqin.toolkit.web.annotation.Entity;
import com.github.thorqin.toolkit.web.annotation.Entity.ParseEncoding;
import com.github.thorqin.toolkit.web.annotation.Entity.SourceType;
import com.github.thorqin.toolkit.web.annotation.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.github.thorqin.toolkit.web.utility.RuleMatcher;
import com.github.thorqin.toolkit.web.utility.ServletUtils;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class WebBasicRouter extends WebRouterBase {
	private static final long serialVersionUID = -4658671798328062327L;
	private static final Logger logger = Logger.getLogger(WebBasicRouter.class.getName());
	
	protected class MappingInfo {
		public Object instance;
		public Method method;
	}
	
	private RuleMatcher<MappingInfo> mapping = null;
	private List<MappingInfo> startup = new LinkedList<>();
	private List<MappingInfo> cleanups = new LinkedList<>();
	private SessionFactory sessionFactory = new SessionFactory();


	public WebBasicRouter(WebApplication application) {
		super(application);
	}

	public WebBasicRouter() {
		super(null);
	}

	private static URI getClassPath(String relativePath) throws URISyntaxException {
		return WebBasicRouter.class.getResource(relativePath).toURI();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String sessionTypeName = config.getInitParameter("sessionClass");
		sessionFactory.setSessionType(sessionTypeName);
		if (mapping == null) {
			try {
				makeApiMapping();
			} catch (Exception ex) {
				throw new ServletException("Initialize dispatcher servlet error.", ex);
			}
		}
		for (MappingInfo info : startup) {
			try {
				info.method.invoke(info.instance);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				logger.log(Level.SEVERE, "Invoke startup failed", ex);
			}
		}
		startup.clear();
	}

	@Override
	public void destroy() {
		// Servlet destroy
		for (MappingInfo info : cleanups) {
			try {
				info.method.invoke(info.instance);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				logger.log(Level.SEVERE, "Invoke cleanup failed", ex);
			}
		}
		cleanups.clear();
		super.destroy();
	}

	public void setCrossSiteHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("P3P","CP=CAO PSA OUR");
		response.setHeader("Access-Control-Allow-Methods",
				"GET,POST,PUT,DELETE,HEAD,OPTIONS");
		response.setHeader("Access-Control-Allow-Headers",
				"Content-Type,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control");
	}


	
	private Object addMapping(List<MappingInfo> collection, Class<?> clazz, Method method, Object inst) {
		try {
			if (inst == null) {
				inst = WebApplication.createInstance(clazz, application);
			}
			MappingInfo info = new MappingInfo();
			info.instance = inst;
			info.method = method;
			method.setAccessible(true);
			collection.add(info);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException | SecurityException ex) {
			logger.log(Level.SEVERE,
					"New instance failed: " + clazz.getName() + "." + method.getName(), ex);
		}
		return inst;
	}
	
	private static WebEntry checkMethodParametersAndAnnotation(Class<?> clazz, Method method) {
		if (!method.isAnnotationPresent(WebEntry.class))
			return null;
		WebEntry entry = method.getAnnotation(WebEntry.class);
		if (entry.method().length <=0 )
			return null;
		Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		PARAMETER_CHECK:
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> paramType = parameterTypes[i];
			if (paramType.equals(HttpServletRequest.class) ||
					paramType.equals(HttpServletResponse.class) ||
					paramType.equals(WebSession.class)) {
				continue;
			}
			Annotation[] annotations = parameterAnnotations[i];
			if (annotations != null) {
				for (Annotation annotation : annotations) {
					if (annotation instanceof Entity ||
							annotation instanceof Param ||
                            annotation instanceof Query) {
						continue PARAMETER_CHECK;
					}
				}
			}
			logger.log(Level.WARNING,
				"Method ''{0}.{1}'' has unknown(or unsupported) parameter at #{2}, method ignored.",
				new Object[]{clazz.getName(), method.getName(), i + 1});
			return null;
		}
		return entry;
	}

	private void analyzeClass(Class<?> clazz) throws Exception {
		if (!clazz.isAnnotationPresent(WebModule.class)) {
			return;
		}
		WebModule classAnno = clazz.getAnnotation(WebModule.class);
		if (application != null) {
            String applyAppName = classAnno.application().trim();
			if (!applyAppName.isEmpty() && !applyAppName.equals(application.getName()))
				return;
		}
		boolean crossSite = classAnno.crossSite();
		String path = clazz.getAnnotation(WebModule.class).value();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		Object inst = WebApplication.createInstance(clazz, application);

		for (Field field : clazz.getDeclaredFields()) {
			Class<?> fieldType = field.getType();
			DBInstance dbAnno = field.getAnnotation(DBInstance.class);
			if (dbAnno != null && application != null) {
				if (fieldType.equals(DBService.class)) {
					field.setAccessible(true);
					if (dbAnno.value().isEmpty())
						field.set(inst, application.getDBService());
					else
						field.set(inst, application.getDBService(dbAnno.value()));
				}
			}
		}

		if (LifeCycleListener.class.isAssignableFrom(clazz)) {
			Method onStartup = clazz.getMethod("onStartup");
			if (onStartup != null)
				addMapping(startup, clazz, onStartup, inst);
			Method onShutdown = clazz.getMethod("onShutdown");
			if (onShutdown != null)
				addMapping(cleanups, clazz, onShutdown, inst);
		}

		if (mapping == null) {
			return;
		}
		for (Method method : clazz.getDeclaredMethods()) {
			WebEntry entry = checkMethodParametersAndAnnotation(clazz, method);
			if (entry == null)
				continue;
			String name = entry.value();
			if (name.isEmpty()) {
				name = method.getName() + classAnno.suffix();
			} else if (name.equals("/")) {
				name = "";
			} else if (name.startsWith("/")) {
				name = name.substring(1);
			}
			String fullPath = path + name;
			if (crossSite || entry.crossSite()) {
				String key = WebEntry.HttpMethod.OPTIONS + ":" + fullPath;
				System.out.println("Add Mapping: " + key);
				MappingInfo info = new MappingInfo();
				info.instance = this;
				info.method = this.getClass().getMethod(
						"setCrossSiteHeaders", HttpServletResponse.class);
				info.method.setAccessible(true);
				mapping.addURLRule(key, info, entry.order(), entry.useCache());
			}
			String methodPrefix = "";
			for (WebEntry.HttpMethod httpMethod : entry.method()) {
				if (!methodPrefix.isEmpty())
					methodPrefix += "|";
				methodPrefix += httpMethod;
			}
			methodPrefix = "(" + methodPrefix + ")";
            Set<String> parameters = new HashSet<>();
            String exp = RuleMatcher.ruleToExp(fullPath, parameters);
			String key = methodPrefix + ":" + exp;
			System.out.println("Add Mapping: " + methodPrefix + ":" + fullPath);
			MappingInfo info = new MappingInfo();
			info.instance = inst;
			info.method = method;
			method.setAccessible(true);
			mapping.addRule(key, parameters, info, entry.order(), entry.useCache());
		}
	}

	private void scanClasses(File path) throws Exception {
		if (path == null) {
			return;
		}
		if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File item : files) {
                    scanClasses(item);
                }
            }
			return;
		}
		else if (!path.isFile() || !path.getName().endsWith(".class")) {
			return;
		}
		try (DataInputStream fstream = new DataInputStream(new FileInputStream(path.getPath()))){
			ClassFile cf = new ClassFile(fstream);
			String className = cf.getName();
			AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(
					AnnotationsAttribute.visibleTag);
			if (visible == null) {
				return;
			}
			for (javassist.bytecode.annotation.Annotation ann : visible.getAnnotations()) {
				if (ann.getTypeName().equals(WebModule.class.getName())) {
					Class<?> clazz = Class.forName(className);
					if (clazz == null) {
						continue;
					}
					analyzeClass(clazz);
				}
			}
		}
	}

	private synchronized void makeApiMapping() throws Exception {
		if (mapping != null) {
			return;
		}
		mapping = new RuleMatcher<>();
		File file = new File(getClassPath("/"));
		scanClasses(file);
	}

	private static String readHttpBody(HttpServletRequest request) {
		try {
			InputStream is = request.getInputStream();
			if (is != null) {
				Writer writer = new StringWriter();
				char[] buffer = new char[1024];
				try {
					String encoding = request.getCharacterEncoding();
					if (encoding == null) {
						encoding = "UTF-8";
					}
					Reader reader = new BufferedReader(
							new InputStreamReader(is, encoding));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} catch (IOException ex) {
					logger.log(Level.WARNING,
							"Read http body failed: ", ex);
				}
				return writer.toString();
			} else {
				return "";
			}
		} catch (IOException e) {
			return "";
		}
	}
	
	private static Object parseFromBody(Class<?> paramType, Entity annoEntity, MethodRuntimeInfo mInfo) {
		try {
			if ((annoEntity.encoding() == ParseEncoding.JSON ||
					annoEntity.encoding() == ParseEncoding.EITHER) &&
					mInfo.postType == RequestPostType.JSON) {
				return Serializer.fromJson(mInfo.httpBody, paramType);
			} else if ((annoEntity.encoding() == ParseEncoding.HTTP_FORM ||
					annoEntity.encoding() == ParseEncoding.EITHER) &&
					mInfo.postType == RequestPostType.HTTP_FORM) {
				return Serializer.fromUrlEncoding(mInfo.httpBody, paramType);
			} else {
				logger.log(Level.WARNING, 
					"WARNING: Cannot deserialize class ''{0}'' from HTTP body: Unsupported post encoding.", paramType.getName());
				return null;
			}
		} catch (IOException | ClassCastException | IllegalAccessException | InstantiationException ex) {
			logger.log(Level.WARNING, 
					"WARNING: Cannot deserialize class ''{0}'' from HTTP body.", paramType.getName());
			return null;
		}
	}
	
	private static Object parseFromQueryString(Class<?> paramType, MethodRuntimeInfo mInfo) {
		try {
			return Serializer.fromUrlEncoding(mInfo.request.getQueryString(), paramType);
		} catch (UnsupportedEncodingException | IllegalAccessException | InstantiationException ex) {
			logger.log(Level.WARNING, 
					"Warnning: Cannot deserialize class ''{0}'' from QueryString.", paramType.getName());
			return null;
		}
	}
	
	private static Object convertParam(String val, Class<?> paramType, String paramName) throws ValidateException {
		if (paramType.equals(String.class))
			return val;
		else if (paramType.equals(Integer.class) || paramType.equals(int.class)) {
			try {
				return Integer.valueOf(val);
			} catch (NumberFormatException err) {
				throw new ValidateException("Invalid parameter '" + paramName + "': Need an integer value");
			}
		} else if (paramType.equals(Long.class) || paramType.equals(long.class)) {
			try {
				return Long.valueOf(val);
			} catch (NumberFormatException err) {
				throw new ValidateException("Invalid parameter '" + paramName + "': Need an long integer value");
			}
		} else if (paramType.equals(Short.class) || paramType.equals(short.class)) {
			try {
				return Short.valueOf(val);
			} catch (NumberFormatException err) {
				throw new ValidateException("Invalid parameter '" + paramName + "': Need an short integer value");
			}
		} else if (paramType.equals(Byte.class) || paramType.equals(byte.class)) {
			try {
				return Byte.valueOf(val);
			} catch (NumberFormatException err) {
				throw new ValidateException("Invalid parameter '" + paramName + "': Need an byte value");
			}
		} else if (paramType.equals(Float.class) || paramType.equals(float.class)) {
			try {
				return Float.valueOf(val);
			} catch (NumberFormatException err) {
				throw new ValidateException("Invalid parameter '" + paramName + "': Need an float value");
			}
		} else if (paramType.equals(Double.class) || paramType.equals(double.class)) {
			try {
				return Double.valueOf(val);
			} catch (NumberFormatException err) {
				throw new ValidateException("Invalid parameter '" + paramName + "': Need an double value");
			}
		} else if (paramType.equals(Boolean.class) || paramType.equals(boolean.class)) {
			try {
				return Boolean.valueOf(val);
			} catch (Exception err) {
				throw new ValidateException("Invalid parameter '" + paramName + "': Need an boolean value");
			}
		} else
			throw new ValidateException("Invalid parameter '" + paramName + "': Cannot translate to specified parameter type!");
	}

	private Object makeParam(
			Class<?> paramType, 
			Annotation[] annos,
			MethodRuntimeInfo mInfo) throws ValidateException, ServletException, UnsupportedEncodingException {
		if (paramType.equals(HttpServletRequest.class)) {
			return mInfo.request;
		} else if (paramType.equals(HttpServletResponse.class)) {
			return mInfo.response;
		} else if (paramType.equals(HttpSession.class)) {
			return mInfo.request.getSession(true);
		} else if (paramType.equals(WebSession.class)) {
            // Obtain session object
            mInfo.session = sessionFactory.getSession(application, mInfo.request, mInfo.response);
            if (mInfo.session != null && !mInfo.session.isNew()) {
                mInfo.session.touch();
            }
			return mInfo.session;
		} else {
			for (Annotation ann : annos) {
				if (ann instanceof Param) {
                    Param annParam = (Param) ann;
                    String paramName = annParam.value();
                    Object obj = null;
                    if (mInfo.urlParams.containsKey(paramName)) {
                        String val = mInfo.urlParams.get(paramName);
                        obj = convertParam(val, paramType, paramName);
                    }
                    Validator validator = new Validator();
                    validator.validate(obj, paramType, annos);
                    return obj;
                } else if (ann instanceof Query) {
                    Query annParam = (Query) ann;
                    String paramName = annParam.value();
                    Map<String, String> queryMap = Serializer.fromUrlEncoding(mInfo.request.getQueryString());
                    Object obj = null;
                    if (queryMap != null && queryMap.containsKey(paramName)) {
                        String val = queryMap.get(paramName);
                        obj = convertParam(val, paramType, paramName);
                    }
                    Validator validator = new Validator();
                    validator.validate(obj, paramType, annos);
                    return obj;
				} else if (ann instanceof Entity) {
					Entity annoEntity = (Entity)ann;
					Object param = null;
					if (annoEntity.source() == SourceType.HTTP_BODY ) {
						param = parseFromBody(paramType, annoEntity, mInfo);
					} else if (annoEntity.source() == SourceType.QUERY_STRING) {
						param = parseFromQueryString(paramType, mInfo);
					} else if (annoEntity.source() == SourceType.EITHER) {
						param = parseFromBody(paramType, annoEntity, mInfo);
						if (param == null)
							param = parseFromQueryString(paramType, mInfo);
					}
					Validator validator = new Validator();
					validator.validate(param, paramType, annos);
					return param;
				}
			}
			return null;
		}
	}
	
	private static enum RequestPostType {
		JSON,
		HTTP_FORM,
		UNKNOW
	}
	
	private static class MethodRuntimeInfo {
		public RequestPostType postType;
		public String httpBody;
		public HttpServletRequest request;
		public HttpServletResponse response;
		public WebSession session = null;
		public Map<String, String> urlParams = new HashMap<>();
	}

	private boolean dispatch(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		long beginTime = System.currentTimeMillis();

		String httpMethod = request.getMethod().toUpperCase();
		String requestPath = request.getServletPath();
		if (request.getPathInfo() != null) {
			requestPath += request.getPathInfo();
		}
		String key = httpMethod + ":" + requestPath;
		RuleMatcher<MappingInfo>.Result matchResult = mapping.match(key);
		if (matchResult == null) {
			return false;
		}
		// Handler has been found then route the input request to appropriate routine to do a further processing.
		try {
			MethodRuntimeInfo mInfo = new MethodRuntimeInfo();
			mInfo.request = request;
			mInfo.response = response;
			// Extract URL's parameters which like '/{user}/{id}' form into a hash map.
			mInfo.urlParams = matchResult.parameters;

			boolean postJson = (request.getContentType() != null &&
					request.getContentType().split(";")[0].equalsIgnoreCase("application/json") ||
					request.getContentType() == null &&
					request.getMethod().equalsIgnoreCase("POST"));
			boolean postForm = (request.getContentType() != null
				&& request.getContentType().split(";")[0].equalsIgnoreCase("application/x-www-form-urlencoded"));
			if (postJson)
				mInfo.postType = RequestPostType.JSON;
			else if (postForm)
				mInfo.postType = RequestPostType.HTTP_FORM;
			else
				mInfo.postType = RequestPostType.UNKNOW;
			if (mInfo.postType != RequestPostType.UNKNOW)
				mInfo.httpBody = readHttpBody(request);

            MappingInfo info = matchResult.info;
			WebEntry entryAnno = info.method.getAnnotation(WebEntry.class);
			if (entryAnno != null && entryAnno.crossSite()) {
				setCrossSiteHeaders(response);
			}
			Object inst = info.instance;
			Class<?>[] params = info.method.getParameterTypes();
			Annotation[][] annos = info.method.getParameterAnnotations();
			List<Object> realParameters = new ArrayList<>(params.length);
			for (int i = 0; i < params.length; i++) {
				realParameters.add(makeParam(params[i], annos[i], mInfo));
			}
			Object result = info.method.invoke(inst, realParameters.toArray());
			if (mInfo.session != null && !mInfo.session.isSaved() && !mInfo.session.isNew())
				mInfo.session.save();

            if (result != null) {
                if (result.getClass().equals(WebContent.class)) {
                    WebContent content = (WebContent)result;
                    if (content.getType() == WebContent.Type.JSON) {
                        ServletUtils.sendJsonString(response, content.toString());
                    } else if (content.getType() == WebContent.Type.HTML) {
                        ServletUtils.sendHtml(response, content.toString());
                    } else if (content.getType() == WebContent.Type.PLAIN) {
                        ServletUtils.sendText(response, content.toString());
                    } else if (content.getType() == WebContent.Type.REDIRECTION) {
                        request.getRequestDispatcher(content.toString()).forward(request, response);
                    }
                } else
                    ServletUtils.sendJsonObject(response, result);
            }
			return true;
		} catch (ValidateException ex) {
			ServletUtils.sendText(response, HttpServletResponse.SC_BAD_REQUEST, "Bad request: invalid parameters!");
			logger.log(Level.WARNING, "Bad request: {0}", ex.getMessage());
			return true;
		} catch (HttpException ex) {
			if (ex.getMessage() != null) {
				if (ex.getJsonObject() != null)
					ServletUtils.sendJsonObject(response, ex.getHttpStatus(), ex.getJsonObject());
				else if (ex.isJsonString())
					ServletUtils.sendJsonString(response, ex.getHttpStatus(), ex.getMessage());
				else
					ServletUtils.sendText(response, ex.getHttpStatus(), ex.getMessage());
			} else
				ServletUtils.send(response, ex.getHttpStatus());
			if (ex.getCause() != null)
				logger.log(Level.WARNING, ex.getMessage(), ex.getCause());
			else
				logger.log(Level.WARNING, ex.getMessage());
			return true;
		} catch (InvocationTargetException ex) {
			Throwable realEx = ex.getTargetException();
			if (HttpException.class.isInstance(realEx)) {
				HttpException httpEx = (HttpException)realEx;
				if (httpEx.getMessage() != null) {
					if (httpEx.getJsonObject() != null)
						ServletUtils.sendJsonObject(response, httpEx.getHttpStatus(), httpEx.getJsonObject());
					else if (httpEx.isJsonString())
						ServletUtils.sendJsonString(response, httpEx.getHttpStatus(), httpEx.getMessage());
					else
						ServletUtils.sendText(response, httpEx.getHttpStatus(), httpEx.getMessage());
				} else
					ServletUtils.send(response, httpEx.getHttpStatus());
				if (httpEx.getCause() != null)
					logger.log(Level.WARNING, httpEx.getMessage(), httpEx.getCause());
				else
					logger.log(Level.WARNING, httpEx.getMessage());
			} else {
				ServletUtils.sendText(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error!");
				logger.log(Level.SEVERE, "Error processing", ex);
			}
			return true;
		} catch (Exception ex) {
			ServletUtils.sendText(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error!");
			logger.log(Level.SEVERE, "Error processing", ex);
			return true;
		} finally {
			if (application != null && application.getSetting().traceRouter) {
				Tracer.Info traceInfo = new Tracer.Info();
				traceInfo.catalog = "router";
				traceInfo.name = "access";
				traceInfo.put("url", requestPath);
				traceInfo.put("method", httpMethod);
				traceInfo.put("startTime", beginTime);
				traceInfo.put("runningTime", System.currentTimeMillis() - beginTime);
				application.trace(traceInfo);
			}
		}
	}

	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!dispatch(request, response)) {
			super.doHead(request, response);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!dispatch(request, response)) {
			super.doGet(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!dispatch(request, response)) {
			super.doPost(request, response);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!dispatch(request, response)) {
			super.doDelete(request, response);
		}
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!dispatch(request, response)) {
			super.doOptions(request, response);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!dispatch(request, response)) {
			super.doPut(request, response);
		}
	}

	@Override
	protected void doTrace(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!dispatch(request, response)) {
			super.doTrace(request, response);
		}
	}
}
