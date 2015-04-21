/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.utility.Localization;

/**
 *
 * @author nuo.qin
 */
@SuppressWarnings("serial")
public class HttpException extends RuntimeException {
	private final int httpStatus;
	private final Object jsonObj;
	private final boolean isJson;

	public static String getCauseMessage(Throwable ex) {
		if (ex == null)
			return null;
		Throwable e = ex;
		Throwable cause;
		while ((cause = e.getCause()) != null)
			e = cause;
		return e.getMessage();
	}

	private static String makeMessage(Integer status, String message, Throwable ex, Localization loc) {
		if (loc != null) {
			if (message != null)
				return loc.get(message);
			else {
				if (ex != null) {
					return loc.get(getCauseMessage(ex));
				} else {
					if (status == null)
						status = 500;
					return String.valueOf(status);
				}
			}
		} else {
			if (message != null)
				return message;
			else {
				if (ex != null) {
					return getCauseMessage(ex);
				} else {
					if (status == null)
						status = 500;
					return String.valueOf(status);
				}
			}
		}
	}
	
	public HttpException(Integer httpStatus) {
		super(String.valueOf(httpStatus));
		this.httpStatus = httpStatus;
		jsonObj = null;
		isJson = false;
	}
	
	public HttpException(Integer httpStatus, Throwable throwable) {
		this(httpStatus, null, throwable, null);
	}

	public HttpException(Integer httpStatus, Throwable throwable, Localization loc) {
		this(httpStatus, null, throwable, loc);
	}
	
	public HttpException(Integer httpStatus, String message) {
		this(httpStatus, message, null, null);
	}

	public HttpException(Integer httpStatus, String message, Localization loc) {
		this(httpStatus, message, null, loc);
	}
	
	public HttpException(Integer httpStatus, String message, Throwable throwable) {
		this(httpStatus, message, throwable, null);
	}

	public HttpException(Integer httpStatus, String message, Throwable throwable, Localization loc) {
		super(makeMessage(httpStatus, message, throwable, loc), throwable);
		if (httpStatus == null)
			httpStatus = 500;
		this.httpStatus = httpStatus;
		jsonObj = null;
		isJson = false;
	}
	
	public HttpException(Integer httpStatus, Object jsonObj) {
		super(String.valueOf(httpStatus));
		if (httpStatus == null)
			httpStatus = 500;
		this.jsonObj = jsonObj;
		this.httpStatus = httpStatus;
		isJson = false;
	}
	
	public HttpException(Integer httpStatus, Object jsonObj, Throwable throwable) {
		super(String.valueOf(httpStatus), throwable);
		if (httpStatus == null)
			httpStatus = 500;
		this.jsonObj = jsonObj;
		this.httpStatus = httpStatus;
		isJson = false;
	}
	
	public HttpException(Integer httpStatus, String message, boolean isJsonString) {
		super(message);
		if (httpStatus == null)
			httpStatus = 500;
		this.httpStatus = httpStatus;
		this.jsonObj = null;
		isJson = isJsonString;
	}
	
	public int getHttpStatus() {
		return httpStatus;
	}
	
	public Object getJsonObject() {
		return jsonObj;
	}
	
	public boolean isJsonString() {
		return isJson;
	}
}
