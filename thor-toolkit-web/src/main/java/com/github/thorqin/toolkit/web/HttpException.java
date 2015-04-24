/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.utility.Localization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String getHttpMessage(Throwable ex, Localization loc) {
        return getHttpMessage(null, null, ex, loc);
    }

    public static String getHttpMessage(Throwable ex) {
        return getHttpMessage(null, null, ex, null);
    }

    private static int getHttpStatus(Integer httpStatus, String message, Throwable throwable) {
        if (message == null) {
            if (throwable != null) {
                message = getCauseMessage(throwable);
            } else {
                if (httpStatus == null)
                    httpStatus = 500;
                message = String.valueOf(httpStatus);
            }
        }
        Matcher matcher = errorPattern.matcher(message);
        if (matcher.find()) {
            int status = Integer.valueOf(matcher.group(1));
            if (httpStatus == null)
                httpStatus = status;
        }
        if (httpStatus == null)
            httpStatus = 500;
        return httpStatus;
    }

    private static String getHttpMessage(Integer httpStatus, String message, Throwable throwable, Localization loc) {
        if (message == null) {
            if (throwable != null) {
                message = getCauseMessage(throwable);
            } else {
                if (httpStatus == null)
                    httpStatus = 500;
                message = String.valueOf(httpStatus);
            }
        }
        Matcher matcher = errorPattern.matcher(message);
        if (matcher.find()) {
            String msg = matcher.group(2);
            if (msg != null) {
                message = msg;
            }
        }
        if (loc != null)
            return loc.get(message);
        else
            return message;
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

    private final static Pattern errorPattern = Pattern.compile("<http:(\\d{3})(?::(.+))?>");

	public HttpException(Integer httpStatus, String message, Throwable throwable, Localization loc) {
        super(getHttpMessage(httpStatus, message, throwable, loc));
		this.httpStatus = getHttpStatus(httpStatus, message, throwable);
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
