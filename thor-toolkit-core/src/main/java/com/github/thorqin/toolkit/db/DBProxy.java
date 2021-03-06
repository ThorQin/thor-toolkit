/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import com.github.thorqin.toolkit.db.DBService.DBOut;
import com.github.thorqin.toolkit.db.DBService.DBRef;
import com.github.thorqin.toolkit.db.DBService.DBSession;
import com.github.thorqin.toolkit.db.annotation.DBInterface;
import com.github.thorqin.toolkit.db.annotation.DBMethod;
import com.github.thorqin.toolkit.utility.StringUtils;
import java.sql.SQLException;

/**
 * DBProxy object.
 * <b>Once obtain a DBProxy object session's autoCommit will be set to false.</b>
 * @author nuo.qin
 */
public class DBProxy implements InvocationHandler {
	private final DBSession session;
	private final DBService db;

	/**
	 * Obtain a DBProxy object
	 * @param session DBSession object
	 */
	public DBProxy(DBSession session) {
		this.session = session;
		this.db = null;
	}

	public DBProxy(DBService db) {
		this.session = null;
		this.db = db;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> classInterface = proxy.getClass().getInterfaces()[0];
		DBInterface dbInterface = classInterface.getAnnotation(DBInterface.class);
		String prefix = "";
		if (dbInterface != null) {
			prefix = dbInterface.prefix();
		}
		String procName;
		DBMethod dbMethod = method.getAnnotation(DBMethod.class);
		if (dbMethod != null && !dbMethod.value().isEmpty()) {
			procName = dbMethod.value();
		} else {
			procName = prefix + StringUtils.camelToUnderline(method.getName());
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		if (session != null)
			return invokeCall(args, paramTypes, method, procName, session);
		else {
			try (DBSession newSession = db.getSession()) {
				return invokeCall(args, paramTypes, method, procName, newSession);
			}
		}
	}

	private Object invokeCall(Object[] args, Class<?>[] paramTypes, Method method, String procName, final DBSession session) throws SQLException, InstantiationException, IllegalAccessException {
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				Class<?> superClass = paramTypes[i].getSuperclass();
				if (args[i] == null && superClass != null && superClass.equals(DBOut.class)) {
					args[i] = paramTypes[i].newInstance();
				} else if ( args[i] == null && superClass != null && superClass.equals(DBRef.class)) {
					args[i] = paramTypes[i].newInstance();
				}
			}
		}
        boolean autoCommit = session.getAutoCommit();
        try {
            Object result = null;
            if (autoCommit)
                session.setAutoCommit(false);
            if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
                session.invoke(procName, args);
            } else {
                result = session.invoke(procName, method.getReturnType(), args);
            }
            if (autoCommit)
                session.commit();
            return result;
        } catch (Exception ex) {
            if (autoCommit)
                session.rollback();
            throw ex;
        } finally {
            if (autoCommit)
                session.setAutoCommit(autoCommit);
        }
	}
}
