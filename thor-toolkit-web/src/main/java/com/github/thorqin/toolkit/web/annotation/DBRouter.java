package com.github.thorqin.toolkit.web.annotation;

/**
 * Created by nuo.qin on 2/4/2015.
 */
public @interface DBRouter {
    /**
     * Database configuration name in config.json
     * @return Configuration name
     */
    public String value() default "db";

    /**
     * Firstly, framework consider this value is a stored procedure name and
     * try to call it to obtain web entries info.
     * if procedure not found but the database is a well-supported database type
     * then consider this value is a procedure prefix,
     * all procedures which name start with this value will be used as a web entry.
     * @return index stored procedure name or prefix
     */
    public String index() default "_web_";

    public static enum DBType {
        POSTGRES,
        ORACLE,
        UNKNOWN
    }

    /**
     * Which type of database will be connected to.
     * if connect to a well supported database,
     * the framework can analyse procedure parameters automatically,
     * so developer can avoid manually write an index procedure
     * to provide entries information.
     * @return Database type enumerate value
     */
    public DBType database() default DBType.UNKNOWN;

    public String refreshEntry() default "refreshRouteTable";
}
