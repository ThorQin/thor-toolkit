package com.github.thorqin.toolkit.web.router;

import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.utility.StringUtils;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.HttpException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.DBRouter;
import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.github.thorqin.toolkit.web.utility.ServletUtils;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nuo.qin on 2/4/2015.
 */
public abstract class WebDBRouter extends WebRouterBase {
    private static final Logger logger = WebApplication.getLogger();

    public static class MappingInfo {
        public String procedure;
        public String[] parameters;
    }

    private SessionFactory sessionFactory = new SessionFactory();
    private final String refreshEntry;
    protected final DBService db;
    protected final String indexProcedure;
    private Map<String, MappingInfo> mapping = new HashMap<>();

    public WebDBRouter(WebApplication application) throws ValidateException {
        super(application);
        DBRouter dbRouter = this.getClass().getAnnotation(DBRouter.class);
        if (application == null)
            throw new InstantiationError("Parameter 'application' is null. " +
                    "Must use this router under web application environment.");
        if (dbRouter == null)
            throw new InstantiationError("Must either provide @DBRouter annotation or use 3 parameters constructor.");
        if (dbRouter.value().isEmpty())
            db = application.getDBService();
        else
            db = application.getDBService(dbRouter.value());
        indexProcedure = dbRouter.index();
        if (dbRouter.refreshEntry().isEmpty())
            refreshEntry = null;
        else
            refreshEntry = dbRouter.refreshEntry();
    }

    public WebDBRouter(DBService dbService, String indexProcedure, String refreshEntry) throws ValidateException {
        super(null);
        if (dbService == null)
            throw new InstantiationError("Parameter 'dbService' is null. ");
        db = dbService;
        this.indexProcedure = indexProcedure;
        if (refreshEntry == null || refreshEntry.isEmpty())
            this.refreshEntry = null;
        else
            this.refreshEntry = refreshEntry;
    }


    /**
     * Generate db mapping and put in 'newMapping' map.
     * Developer can override this method to customize mapping information.
     * @param newMapping
     */
    protected void makeMapping(Map<String, MappingInfo> newMapping) {
        try {
            String json = db.invoke(indexProcedure, String.class);
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            Map<String, List<String>> procedures = Serializer.fromJson(json, type);
            ANALYSE_PROCEDURE:
            for (String key : procedures.keySet()) {
                if (key == null)
                    continue;
                String[] pair = key.split(":");
                String entryName;
                String spName;
                String method;
                if (pair.length == 2) {
                    method = pair[0];
                    spName = pair[1];
                    entryName = pair[1];
                    if (entryName.toLowerCase().startsWith(indexProcedure.toLowerCase()))
                        entryName = entryName.substring(indexProcedure.length());
                    entryName = StringUtils.underlineToCamel(entryName);
                } else if (pair.length == 3) {
                    method = pair[0];
                    spName = pair[1];
                    entryName = pair[2];
                } else {
                    logger.log(Level.WARNING, "Invalid procedure web entry define: " + key);
                    continue;
                }
                if (method == null) {
                    logger.log(Level.WARNING, "Invalid procedure web entry define: "
                            + key + ": invalid method.");
                    continue;
                }
                if (spName == null || spName.isEmpty()) {
                    logger.log(Level.WARNING, "Invalid procedure web entry define: "
                            + key + ": invalid procedure name.");
                    continue;
                }
                MappingInfo info = new MappingInfo();
                info.procedure = spName;
                List<String> parameters = procedures.get(key);
                Set<String> paramSet = new HashSet<>();
                for (String parameter: parameters) {
                    if (!parameter.matches("^(?i)query_string|request_body|request_header|" +
                            "session|response_header|response_content_type|response$")) {
                        logger.log(Level.WARNING, "Invalid procedure web entry define: "
                                + key + ": invalid parameter: " + parameter);
                        continue ANALYSE_PROCEDURE;
                    }
                    if (parameter.contains(parameter.toLowerCase())) {
                        logger.log(Level.WARNING, "Invalid procedure web entry define: "
                                + key + ": duplicated parameter: " + parameter);
                        continue ANALYSE_PROCEDURE;
                    }
                    paramSet.add(parameter.toLowerCase());
                }
                if (parameters == null)
                    info.parameters = new String[]{};
                else {
                    info.parameters = parameters.toArray(new String[parameters.size()]);
                }
                String[] methods = method.split("\\|");
                if (methods.length < 1) {
                    logger.log(Level.WARNING, "Invalid procedure web entry define: "
                            + key + ": invalid method.");
                    continue;
                }
                for (String m: methods) {
                    if (m.matches("^(?i)POST|GET|PUT|DELETE$")) {
                        String k = m.toUpperCase() + ":" + entryName;
                        newMapping.put(k, info);
                        System.out.println("Add DB Mapping: " + k);
                    } else {
                        logger.log(Level.WARNING, "Invalid procedure web entry define: " +
                                key + ": invalid method: " + m + ", ignored.");
                        continue;
                    }
                }

            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Generate database mapping failed.", ex);
        }
    }

    @Override
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
        String sessionTypeName = config.getInitParameter("sessionClass");
        sessionFactory.setSessionType(sessionTypeName);
        mapping.clear();
        makeMapping(mapping);
    }

    @Override
    public final void destroy() {
        // Servlet destroy
        super.destroy();
    }

    private static enum RequestPostType {
        JSON,
        HTTP_FORM,
        UNKNOWN
    }

    private boolean dispatch(HttpServletRequest request, HttpServletResponse response) {
        long beginTime = System.currentTimeMillis();

        String httpMethod = request.getMethod().toUpperCase();
        String requestPath = request.getServletPath();
        if (request.getPathInfo() != null) {
            requestPath += request.getPathInfo();
        }
        requestPath = requestPath.substring(requestPath.lastIndexOf("/") + 1);

        if (refreshEntry != null && requestPath.equals(refreshEntry)) {
            System.out.println("NOTE: Refresh db route table!!");
            mapping.clear();
            makeMapping(mapping);
            ServletUtils.sendText(response, "Route table has been refreshed!");
            return true;
        }

        String key = httpMethod + ":" + requestPath;
        MappingInfo mappingInfo = mapping.get(key);
        if (mappingInfo == null)
            return false;
        try {
            RequestPostType postType;
            String httpBody = null;
            boolean postJson = (request.getContentType() != null &&
                    request.getContentType().split(";")[0].equalsIgnoreCase("application/json") ||
                    request.getContentType() == null &&
                            (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("PUT")));
            boolean postForm = (request.getContentType() != null
                    && request.getContentType().split(";")[0].equalsIgnoreCase("application/x-www-form-urlencoded"));
            if (postJson)
                postType = RequestPostType.JSON;
            else if (postForm)
                postType = RequestPostType.HTTP_FORM;
            else
                postType = RequestPostType.UNKNOWN;
            if (postType != RequestPostType.UNKNOWN)
                httpBody = ServletUtils.readHttpBody(request);

            Map<String, String> headers = null;
            WebSession session = null;
            DBService.DBRefString refSession = null;
            DBService.DBOutString outResponseHeader = null;
            DBService.DBOutString outResponse = null;
            DBService.DBOutString outResponseContentType = null;
            List<Object> parameters = new ArrayList<>(mappingInfo.parameters.length);
            for (String paramName : mappingInfo.parameters) {
                if (paramName.equalsIgnoreCase("request_body")) {
                    if (postType == RequestPostType.JSON) {
                        parameters.add(httpBody);
                    } else if (postType == RequestPostType.HTTP_FORM) {
                        parameters.add(Serializer.toJsonString(Serializer.fromUrlEncoding(httpBody)));
                    } else
                        parameters.add(null);
                } else if (paramName.equalsIgnoreCase("query_string")) {
                    String queryString = request.getQueryString();
                    if (queryString == null || queryString.isEmpty())
                        parameters.add(null);
                    else {
                        parameters.add(Serializer.toJsonString(Serializer.fromUrlEncoding(queryString)));
                    }
                } else if (paramName.equalsIgnoreCase("request_header")) {
                    if (headers == null) {
                        headers = new HashMap<>();
                        List<String> names = Collections.list(request.getHeaderNames());
                        for (String name : names) {
                            headers.put(name, request.getHeader(name));
                        }
                    }
                    parameters.add(Serializer.toJsonString(headers));
                } else if (paramName.equalsIgnoreCase("response_header")) {
                    outResponseHeader = new DBService.DBOutString();
                    parameters.add(outResponseHeader);
                } else if (paramName.equalsIgnoreCase("response")) {
                    outResponse = new DBService.DBOutString();
                    parameters.add(outResponse);
                } else if (paramName.equalsIgnoreCase("response_content_type")) {
                    outResponseContentType = new DBService.DBOutString();
                    parameters.add(outResponseContentType);
                } else if (paramName.equalsIgnoreCase("session")) {
                    if (session == null) {
                        session = sessionFactory.getSession(application, request, response);
                        if (session != null && !session.isNew()) {
                            session.touch();
                        }
                    }
                    refSession = new DBService.DBRefString(Serializer.toJsonString(session.getMap()));
                    parameters.add(refSession);
                } else {
                    throw new InvalidParameterException("Invalid DB entry parameter: " + paramName);
                }
            }
            // Call database stored procedure
            db.invoke(mappingInfo.procedure, parameters.toArray());

            if (refSession != null && refSession.getValue() != null && session != null) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> map = Serializer.fromJson(refSession.getValue(), type);
                for (String sessionKey : map.keySet()) {
                    session.set(sessionKey, map.get(sessionKey));
                }
                session.save();
            }
            if (outResponseHeader != null && outResponseHeader.getValue() != null) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> map = Serializer.fromJson(outResponseHeader.getValue(), type);
                for (String headerKey : map.keySet()) {
                    response.setHeader(headerKey, map.get(headerKey));
                }
            }
            String contentType;
            if (outResponseContentType != null && outResponseContentType.getValue() != null) {
                contentType = outResponseContentType.getValue();
            } else {
                contentType = "application/json";
            }
            if (outResponse != null && outResponse.getValue() != null) {
                ServletUtils.sendText(response, outResponse.getValue(), contentType);
            }
        } catch (SQLException ex) {
            if (ex.getMessage() != null) {
                Pattern pattern = Pattern.compile("<http:(\\d{3})(?::(.+))?>");
                Matcher matcher = pattern.matcher(ex.getMessage());
                if (matcher.find()) {
                    int status = Integer.valueOf(matcher.group(1));
                    String msg = matcher.group(2);
                    ServletUtils.sendText(response, status, msg);
                    logger.log(Level.WARNING, "Return HTTP error: " + ex.getMessage());
                } else {
                    ServletUtils.sendText(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error!");
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            } else {
                ServletUtils.sendText(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error!");
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            ServletUtils.sendText(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error!");
            logger.log(Level.SEVERE,
                    "Error processing!!", ex);
        } finally {
            if (application != null && application.getSetting().traceRouter) {
                Tracer.Info traceInfo = new Tracer.Info();
                traceInfo.catalog = "db_router";
                traceInfo.name = "access";
                traceInfo.put("url", ServletUtils.getURL(request));
                traceInfo.put("method", httpMethod);
                traceInfo.put("clientIP", request.getRemoteAddr());
                traceInfo.put("startTime", beginTime);
                traceInfo.put("runningTime", System.currentTimeMillis() - beginTime);
                application.trace(traceInfo);
            }
        }
        return true;
    }

    @Override
    protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!dispatch(req, resp)) {
            super.service(req, resp);
        }
    }
}
