package com.github.thorqin.toolkit.web.router;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.Validator;
import com.github.thorqin.toolkit.web.HttpException;
import com.github.thorqin.toolkit.web.LifeCycleListener;
import com.github.thorqin.toolkit.web.MessageConstant;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebModule;
import com.github.thorqin.toolkit.web.annotation.Entity;
import com.github.thorqin.toolkit.web.annotation.Encoding;
import com.github.thorqin.toolkit.web.annotation.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.github.thorqin.toolkit.web.utility.RuleMatcher;
import com.github.thorqin.toolkit.web.utility.ServletUtils;
import com.google.gson.JsonSyntaxException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class WebBasicRouter extends WebRouterBase {
	private static final long serialVersionUID = -4658671798328062327L;
	private final Logger logger;
	
	private static class MappingInfo {
		public Object instance;
		public Method method;
        public String localeMessage;
	}

    private static class ServiceInfo {
        public Object instance;
        public Field field;
        public String serviceName;
    }
	
	private RuleMatcher<MappingInfo> mapping = null;
	private List<MappingInfo> startup = new LinkedList<>();
	private List<MappingInfo> cleanups = new LinkedList<>();
    private List<ServiceInfo> serviceMapping = new LinkedList<>();
	private SessionFactory sessionFactory = new SessionFactory();
    private ConfigManager.ChangeListener changeListener = new ConfigManager.ChangeListener() {
        @Override
        public void onConfigChanged(ConfigManager configManager) {
            for (ServiceInfo serviceInfo: serviceMapping) {
                try {
                    serviceInfo.field.set(serviceInfo.instance, application.getService(serviceInfo.serviceName));
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Reassign service instance failed!", ex);
                }
            }
        }
    };


	public WebBasicRouter(WebApplication application) {
		super(application);
        if (application != null) {
            logger = application.getLogger();
        } else
            logger = Logger.getLogger(WebBasicRouter.class.getName());
	}

	public WebBasicRouter() {
		super(null);
        logger = Logger.getLogger(WebBasicRouter.class.getName());
	}

	private static URI getClassPath(String relativePath) throws URISyntaxException {
		return WebBasicRouter.class.getResource(relativePath).toURI();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
        try {
            String sessionTypeName = config.getInitParameter("sessionClass");
            sessionFactory.setSessionType(sessionTypeName);
            if (mapping == null) {
                try {
                    makeApiMapping();
                } catch (Exception ex) {
                    throw new ServletException("Initialize dispatcher servlet error.", ex);
                }
            }
            if (application != null) {
                application.getConfigManager().addChangeListener(changeListener);
            }
            for (MappingInfo info : startup) {
                try {
                    info.method.invoke(info.instance);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    logger.log(Level.SEVERE, "Invoke startup failed", ex);
                }
            }
            startup.clear();
        } catch (ServletException ex) {
            logger.log(Level.SEVERE, "Initialize WebBasicRouter failed!", ex);
            throw new RuntimeException(ex);
        }
	}

	@Override
	public void destroy() {
        if (application != null) {
            application.getConfigManager().removeChangeListener(changeListener);
        }
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
		ServletUtils.setCrossSiteHeaders(response);
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
	
	private WebEntry checkMethodParametersAndAnnotation(Class<?> clazz, Method method) {
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
					paramType.equals(WebSession.class) ||
                    paramType.equals(Localization.class)
                    ) {
				continue;
			}
			Annotation[] annotations = parameterAnnotations[i];
			if (annotations != null) {
				for (Annotation annotation : annotations) {
					if (annotation instanceof Entity ||
							annotation instanceof Part ||
                            annotation instanceof Param) {
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

    private static boolean shouldUseCache(WebEntry entry, String address) {
        if (entry.cache() == CacheType.ENABLED)
            return true;
        else if (entry.cache() == CacheType.DISABLED)
            return false;
        else {
            if (RuleMatcher.paramPattern.matcher(address).find())
                return false;
            else
                return true;
        }
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
        String className = clazz.getName();
		boolean crossSite = classAnno.crossSite();
		String path = clazz.getAnnotation(WebModule.class).value();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		Object inst = WebApplication.createInstance(clazz, application);
        String localeMessage = classAnno.localeMessage();

		for (Field field : clazz.getDeclaredFields()) {
			// Class<?> fieldType = field.getType();
            Service annotation = field.getAnnotation(Service.class);
            if (annotation != null && application != null) {
                field.setAccessible(true);
                field.set(inst, application.getService(annotation.value()));
                ServiceInfo info = new ServiceInfo();
                info.instance = inst;
                info.field = field;
                info.serviceName = annotation.value();
                serviceMapping.add(info);
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
				String key = HttpMethod.OPTIONS + ":" + fullPath;
				System.out.println("Add Mapping: " + key);
				MappingInfo info = new MappingInfo();
				info.instance = this;
				info.method = this.getClass().getMethod(
						"setCrossSiteHeaders", HttpServletResponse.class);
				info.method.setAccessible(true);
                mapping.addURLRule(key, info, entry.order(),
                        shouldUseCache(entry, fullPath));
			}
			String methodPrefix = "";
			for (HttpMethod httpMethod : entry.method()) {
				if (!methodPrefix.isEmpty())
					methodPrefix += "|";
				methodPrefix += httpMethod;
			}
			methodPrefix = "(" + methodPrefix + ")";
            Set<String> parameters = new HashSet<>();
            String exp = RuleMatcher.ruleToExp(fullPath, parameters);
			String key = methodPrefix + ":" + exp;
            // Print to console to show mapping information
			System.out.println("Add Mapping: " + methodPrefix + ":" +
                    fullPath + " -> " + className + "::" + method.getName());

			MappingInfo info = new MappingInfo();
			info.instance = inst;
			info.method = method;
            info.localeMessage = localeMessage;
			method.setAccessible(true);
			mapping.addRule(key, parameters, info, entry.order(),
                    shouldUseCache(entry, fullPath));
		}
	}

	private void scanClasses(File path) throws Exception {
		if (path == null) {
			return;
		}
        serviceMapping.clear();
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
	
	private Object parseFromBody(Class<?> paramType, Entity annoEntity, MethodRuntimeInfo mInfo) {
		try {
			if ((annoEntity.encoding() == Encoding.JSON ||
					annoEntity.encoding() == Encoding.EITHER) &&
					mInfo.postType == RequestPostType.JSON) {
				return Serializer.fromJson(mInfo.httpBody, paramType);
			} else if ((annoEntity.encoding() == Encoding.HTTP_FORM ||
					annoEntity.encoding() == Encoding.EITHER) &&
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
	
	private Object parseFromQueryString(Class<?> paramType, MethodRuntimeInfo mInfo) {
		try {
			return Serializer.fromUrlEncoding(mInfo.request.getQueryString(), paramType);
		} catch (UnsupportedEncodingException | IllegalAccessException | InstantiationException ex) {
			logger.log(Level.WARNING, 
					"Warning: Cannot deserialize class ''{0}'' from QueryString.", paramType.getName());
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
			return mInfo.session;
		} else if (paramType.equals(Localization.class)) {
            return mInfo.loc;
        } else {
			for (Annotation ann : annos) {
				if (ann instanceof Part) {
                    Part annPart = (Part) ann;
                    String paramName = annPart.value();
                    Object obj = null;
                    if (mInfo.urlParams.containsKey(paramName)) {
                        String val = mInfo.urlParams.get(paramName);
                        obj = convertParam(val, paramType, paramName);
                    }
                    Validator validator = new Validator(mInfo.loc);
                    validator.validate(obj, paramType, annos);
                    return obj;
                } else if (ann instanceof Param) {
                    Param annParam = (Param) ann;
                    String paramName = annParam.value();
                    Object obj = null;
                    if ((annParam.source() == SourceType.EITHER || annParam.source() == SourceType.HTTP_BODY) &&
                            mInfo.postType == RequestPostType.HTTP_FORM) {
                        if (mInfo.formParams == null)
                            mInfo.formParams = Serializer.fromUrlEncoding(mInfo.httpBody);
                        if (mInfo.formParams != null && mInfo.formParams.containsKey(paramName)) {
                            String val = mInfo.formParams.get(paramName);
                            obj = convertParam(val, paramType, paramName);
                        }
                    }
                    if (obj == null && (annParam.source() == SourceType.EITHER || annParam.source() == SourceType.QUERY_STRING)) {
                        if (mInfo.queryParams == null)
                            mInfo.queryParams = Serializer.fromUrlEncoding(mInfo.request.getQueryString());
                        if (mInfo.queryParams != null && mInfo.queryParams.containsKey(paramName)) {
                            String val = mInfo.queryParams.get(paramName);
                            obj = convertParam(val, paramType, paramName);
                        }
                    }
                    Validator validator = new Validator(mInfo.loc);
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
					Validator validator = new Validator(mInfo.loc);
					validator.validate(param, paramType, annos);
					return param;
				}
			}
			return null;
		}
	}
	
	private enum RequestPostType {
		JSON,
		HTTP_FORM,
        UNKNOWN
	}
	
	private static class MethodRuntimeInfo {
		public RequestPostType postType;
		public String httpBody;
		public HttpServletRequest request;
		public HttpServletResponse response;
		public WebSession session = null;
        //public String localeMessage;
        public Localization loc;
        public Map<String, String> queryParams = null;
        public Map<String, String> formParams = null;
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
        Localization loc = Localization.getInstance();
        try {
            MethodRuntimeInfo mInfo = new MethodRuntimeInfo();
			mInfo.request = request;
			mInfo.response = response;
            // Obtain session object
            mInfo.session = sessionFactory.getSession(application, mInfo.request, mInfo.response);
            if (mInfo.session != null && !mInfo.session.isNew()) {
                mInfo.session.touch();
            }
			// Extract URL's parameters which like '/{user}/{id}' form into a hash map.
			mInfo.urlParams = matchResult.parameters;

            // Build loc object
            MappingInfo info = matchResult.info;
            Object lang = mInfo.session.get("lang");
            String language;
            if (lang != null && lang.getClass().equals(String.class)) {
                language = (String)lang;
            } else
                language = request.getHeader("Accept-Language");
            mInfo.loc = Localization.getInstance(info.localeMessage, language);
            loc = mInfo.loc;
			boolean postJson = (request.getContentType() != null &&
					request.getContentType().split(";")[0].equalsIgnoreCase("application/json") ||
					request.getContentType() == null &&
                            (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("PUT")));
			boolean postForm = (request.getContentType() != null
				&& request.getContentType().split(";")[0].equalsIgnoreCase("application/x-www-form-urlencoded"));
			if (postJson)
				mInfo.postType = RequestPostType.JSON;
			else if (postForm)
				mInfo.postType = RequestPostType.HTTP_FORM;
			else
				mInfo.postType = RequestPostType.UNKNOWN;
			if (mInfo.postType != RequestPostType.UNKNOWN)
                mInfo.httpBody = ServletUtils.readHttpBody(request);

			WebEntry entryAnno = info.method.getAnnotation(WebEntry.class);
            if (entryAnno != null && entryAnno.crossSite()) {
                ServletUtils.setCrossSiteHeaders(response);
            }
			Object inst = info.instance;
			Class<?>[] params = info.method.getParameterTypes();
			Annotation[][] annos = info.method.getParameterAnnotations();
			List<Object> realParameters = new ArrayList<>(params.length);
			for (int i = 0; i < params.length; i++) {
				realParameters.add(makeParam(params[i], annos[i], mInfo));
			}
			Object result;
			try {
				result = info.method.invoke(inst, realParameters.toArray());
			} finally {
				if (mInfo.session != null && !mInfo.session.isSaved() && !mInfo.session.isNew())
					mInfo.session.save();
			}

            if (result != null) {
                boolean supportGzip = ServletUtils.supportGZipCompression(request);
                boolean useGzip = application != null ? application.getSetting().gzip : true;
                supportGzip = (useGzip && supportGzip);
                if (result.getClass().equals(WebContent.class)) {
                    WebContent content = (WebContent)result;
                    String strContent = content.toString();
                    if (strContent != null) {
                        if (content.getType() == WebContent.Type.JSON) {
                            ServletUtils.sendJsonString(response, strContent, supportGzip);
                        } else if (content.getType() == WebContent.Type.HTML) {
                            ServletUtils.sendHtml(response, strContent, supportGzip);
                        } else if (content.getType() == WebContent.Type.PLAIN) {
                            ServletUtils.sendText(response, strContent, supportGzip);
                        } else if (content.getType() == WebContent.Type.REDIRECTION) {
                            request.getRequestDispatcher(strContent).forward(request, response);
                        }
                    }
                } else {
                    ServletUtils.sendJsonObject(response, result, supportGzip);
                }
            }
		} catch (JsonSyntaxException ex) {
            String message = MessageConstant.INVALID_REQUEST_CONTENT.getMessage(loc);
			ServletUtils.sendText(response, HttpServletResponse.SC_BAD_REQUEST, message);
            String logMsg = MessageFormat.format("Bad request, invalid JSON content: {0}: {1}",
                    ServletUtils.getURL(request), ex.getMessage());
			logger.logp(Level.WARNING,
                    matchResult.info.method.getDeclaringClass().getName(),
                    matchResult.info.method.getName(),
                    logMsg);
		} catch (ValidateException ex) {
            ServletUtils.sendText(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            String logMsg = MessageFormat.format("Validate failed: {0}: {1}",
                    ServletUtils.getURL(request), ex.getMessage());
            logger.logp(Level.WARNING,
                    matchResult.info.method.getDeclaringClass().getName(),
                    matchResult.info.method.getName(),
                    logMsg);
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
            String logMsg = MessageFormat.format("HttpException: {0}: {1}",
                    ServletUtils.getURL(request), ex.getMessage());
            logger.logp(Level.WARNING,
                    matchResult.info.method.getDeclaringClass().getName(),
                    matchResult.info.method.getName(),
                    logMsg,
                    ex.getCause());
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
                String logMsg = MessageFormat.format("HttpException: {0}: {1}",
                        ServletUtils.getURL(request), ex.getMessage());
                logger.logp(Level.WARNING,
                        matchResult.info.method.getDeclaringClass().getName(),
                        matchResult.info.method.getName(),
                        logMsg,
                        httpEx.getCause());
			} else {
                String message = MessageConstant.UNEXPECTED_SERVER_ERROR.getMessage(loc);
				ServletUtils.sendText(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
                Throwable logThrowable;
                if (realEx != null)
                    logThrowable = realEx;
                else
                    logThrowable = ex;
                String logMsg = MessageFormat.format("Unexpected server error, process failed: {0}: {1}",
                        ServletUtils.getURL(request), logThrowable.getMessage());
				logger.logp(Level.SEVERE,
                        matchResult.info.method.getDeclaringClass().getName(),
                        matchResult.info.method.getName(),
                        logMsg, logThrowable);
			}
		} catch (Exception ex) {
            String message = MessageConstant.UNEXPECTED_SERVER_ERROR.getMessage(loc);
			ServletUtils.sendText(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            String logMsg = MessageFormat.format("Unexpected server error: {0}: {1}",
                    ServletUtils.getURL(request), ex.getMessage());
			logger.logp(Level.SEVERE,
                    matchResult.info.method.getDeclaringClass().getName(),
                    matchResult.info.method.getName(),
                    logMsg, ex);
		} finally {
			if (application != null && application.getSetting().traceRouter) {
				Tracer.Info traceInfo = new Tracer.Info();
				traceInfo.catalog = "router";
				traceInfo.name = "access";
				traceInfo.put("url", ServletUtils.getURL(request));
				traceInfo.put("method", httpMethod);
                traceInfo.put("clientIP", request.getRemoteAddr());
				traceInfo.put("startTime", beginTime);
				traceInfo.put("runningTime", System.currentTimeMillis() - beginTime);
				application.getTracer().trace(traceInfo);
			}
		}
        return true;
	}

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!dispatch(req, resp)) {
            super.service(req, resp);
        }
    }
}
