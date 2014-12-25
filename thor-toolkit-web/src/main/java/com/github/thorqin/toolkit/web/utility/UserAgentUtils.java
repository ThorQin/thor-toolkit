/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.thorqin.toolkit.utility;


/**
 *
 * @author nuo.qin
 */
public class UserAgentUtils {

	public static enum OSType {

		WINDOWS,
		MAC,
		UNIX,
		ANDROID,
		IPHONE,
		OTHER
	}

	public static enum BrowserType {

		MSIE,
		FIREFOX,
		CHROME,
		SAFARI,
		OPERA,
		BrowserType, OTHER
	}

	public static class UserAgentInfo {

		public OSType os;
		public BrowserType browser;
	}

	public static UserAgentInfo parse(String userAgent) {
		UserAgentInfo uaInfo = new UserAgentInfo();
		if (userAgent.contains("windows")) {
			uaInfo.os = OSType.WINDOWS;
		} else if (userAgent.contains("mac")) {
			uaInfo.os = OSType.MAC;
		} else if (userAgent.contains("x11")) {
			uaInfo.os = OSType.UNIX;
		} else if (userAgent.contains("android")) {
			uaInfo.os = OSType.ANDROID;
		} else if (userAgent.contains("iphone")) {
			uaInfo.os = OSType.IPHONE;
		} else {
			uaInfo.os = OSType.OTHER;
		}
		if (userAgent.contains("msie")) {
			uaInfo.browser = BrowserType.MSIE;
		} else if (userAgent.contains("safari")) {
			uaInfo.browser = BrowserType.SAFARI;
		} else if (userAgent.contains("opr") || userAgent.contains("opera")) {
			uaInfo.browser = BrowserType.OPERA;
		} else if (userAgent.contains("chrome")) {
			uaInfo.browser = BrowserType.CHROME;
		} else if (userAgent.contains("firefox")) {
			uaInfo.browser = BrowserType.FIREFOX;
		} else {
			uaInfo.browser = BrowserType.OTHER;
		}
		return uaInfo;
	}
}
