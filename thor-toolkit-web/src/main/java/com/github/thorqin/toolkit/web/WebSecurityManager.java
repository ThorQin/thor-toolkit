/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.web.session.ClientSession;
import com.github.thorqin.toolkit.web.session.SessionGenerator;
import com.github.thorqin.toolkit.web.session.WebSession;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author nuo.qin
 */
public class WebSecurityManager extends WebFilterBase {
	private WebSecurityManager security;

	private SessionGenerator sessionGenerator = new SessionGenerator();

	public WebSecurityManager(WebApplication application) {
		super(application);
	}

	public WebSecurityManager() {
		super(null);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		String sessionTypeName = config.getInitParameter("sessionClass");
		sessionGenerator.setSessionType(sessionTypeName);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//		long beginTime = System.currentTimeMillis();
//		if (security.checkPermission((HttpServletRequest)request, (HttpServletResponse)response))
//			chain.doFilter(request, response);
//		if (security.getSetting().trace) {
//			WebSecurityManager.LoginInfo loginInfo = security.getLoginInfo((HttpServletRequest)request,
//					(HttpServletResponse)response);
//			RequestInfo reqInfo = MonitorService.buildRequestInfo((HttpServletRequest)request,
//					(HttpServletResponse)response, loginInfo, "Security Manager", beginTime);
//			MonitorService.record(reqInfo);
//		}
	}

	@Override
	public void destroy() {
	}
	
}
