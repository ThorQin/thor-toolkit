package com.github.thorqin.toolkit.web.router;

import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.utility.StringUtils;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.DBRouter;
import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    protected final DBRouter.DBType dbType;
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
        dbType = dbRouter.database();
        indexProcedure = dbRouter.index();
        if (dbRouter.refreshEntry().isEmpty())
            refreshEntry = null;
        else
            refreshEntry = dbRouter.refreshEntry();
    }

    public WebDBRouter(DBService dbService, DBRouter.DBType dbType, String indexProcedure, String refreshEntry) throws ValidateException {
        super(null);
        if (dbService == null)
            throw new InstantiationError("Parameter 'dbService' is null. ");
        db = dbService;
        this.dbType = dbType;
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
        String json;
        try {
            json = db.invoke(indexProcedure, String.class);
        } catch (Exception ex) {
            try {
                if (dbType == DBRouter.DBType.POSTGRES) {
                    logger.log(Level.INFO,
                            "Can not get information from index procedure, will analyse Postgres database directly.");
                    String pgSql = "select sp_name, proargnames from (\n" +
                            "select n.nspname || '.' || p.proname sp_name, p.proargnames \n" +
                            "from pg_proc p, pg_namespace n\n" +
                            "where p.pronamespace = n.oid) t where sp_name like '" + indexProcedure + "%'";
                    try (DBService.DBSession session = db.getSession()) {
                        try (DBService.DBCursor cursor = session.query(pgSql)) {
                            while (cursor.next()) {
                                //cursor.
                            }
                        }
                    }
                } else if (dbType == DBRouter.DBType.ORACLE) {
                    logger.log(Level.INFO,
                            "Can not get information from index procedure, will analyse Oracle database directly.");

                } else {
                    logger.log(Level.WARNING,
                            "Can not get information from index procedure.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Make db route table failed.", e);
            }
            return;
        }
        try {
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            Map<String, List<String>> procedures = Serializer.fromJson(json, type);
            for (String key : procedures.keySet()) {
                if (key == null)
                    continue;
                String[] pair = key.split(":");
                String entryName;
                String spName;
                if (pair.length == 1) {
                    spName = pair[0];
                    entryName = pair[0];
                    if (entryName.toLowerCase().startsWith(indexProcedure.toLowerCase()))
                        entryName = entryName.substring(indexProcedure.length());
                    entryName = StringUtils.underlineToCamel(entryName);
                } else if (pair.length == 2) {
                    spName = pair[0];
                    entryName = pair[1];
                } else {
                    logger.log(Level.WARNING, "Invalid procedure web entry: " + key);
                    continue;
                }
                MappingInfo info = new MappingInfo();
                info.procedure = spName;
                info.parameters = (String[]) procedures.get(key).toArray();
                newMapping.put(entryName, info);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Generate database mapping failed.", ex);
        }
        return;
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

    private boolean dispatch(HttpServletRequest req, HttpServletResponse resp) {
        return true;
    }

    @Override
    protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!dispatch(req, resp)) {
            super.service(req, resp);
        }
    }
}
