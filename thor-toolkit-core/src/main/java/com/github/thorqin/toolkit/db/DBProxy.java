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
import com.github.thorqin.toolkit.utility.StringHelper;
import java.sql.SQLException;

/**
 *
 * @author nuo.qin
 */
public class DBProxy implements InvocationHandler {
	private DBSession session;
	private boolean autoCommit;
	public DBProxy(DBSession session, boolean autoCommit) throws SQLException {
		this.session = session;
		this.session.setAutoCommit(false);
		this.autoCommit = autoCommit;
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
		if (dbMethod != null) {
			procName = dbMethod.value();
		} else {
			procName = (prefix.isEmpty() ? "" : prefix + "_") + StringHelper.camelToUnderline(method.getName());
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		return invokeCall(args, paramTypes, method, session, procName);
	}

	private Object invokeCall(Object[] args, Class<?>[] paramTypes, Method method, final DBSession session, String procName) throws SQLException, InstantiationException, IllegalAccessException {
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
		if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
			try {
				session.perform(procName, args);
				if (autoCommit)
					session.commit();
				return null;
			} catch (Exception ex) {
				if (autoCommit)
					session.rollback();
				throw ex;
			}
		} else {
			try {
				Object result = session.invoke(procName, method.getReturnType(), args);
				if (autoCommit)
					session.commit();
				return result;
			} catch (Exception ex) {
				if (autoCommit)
					session.rollback();
				throw ex;
			}
		}
	}
}
