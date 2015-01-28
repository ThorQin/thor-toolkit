/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web;

/**
 *
 * @author nuo.qin
 */
@SuppressWarnings("serial")
public class HttpException extends RuntimeException {
	private final int httpStatus;
	private final Object jsonObj;
	private final boolean isJson;
	
	public HttpException(int httpStatus) {
		super("Http Code: " + httpStatus);
		this.httpStatus = httpStatus;
		jsonObj = null;
		isJson = false;
	}
	
	public HttpException(int httpStatus, Throwable throwable) {
		super("Http Code: " + httpStatus, throwable);
		this.httpStatus = httpStatus;
		jsonObj = null;
		isJson = false;
	}
	
	public HttpException(int httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
		jsonObj = null;
		isJson = false;
	}
	
	public HttpException(int httpStatus, String message, Throwable throwable) {
		super(message, throwable);
		this.httpStatus = httpStatus;
		jsonObj = null;
		isJson = false;
	}
	
	public HttpException(int httpStatus, Object jsonObj) {
		super("Http Code: " + httpStatus);
		this.jsonObj = jsonObj;
		this.httpStatus = httpStatus;
		isJson = false;
	}
	
	public HttpException(int httpStatus, Object jsonObj, Throwable throwable) {
		super("Http Code: " + httpStatus, throwable);
		this.jsonObj = jsonObj;
		this.httpStatus = httpStatus;
		isJson = false;
	}
	
	public HttpException(int httpStatus, String message, boolean isJsonString) {
		super(message);
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
