/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.web.session.SessionFactory;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author nuo.qin
 */
public class WebSecurityManager extends WebFilterBase {
	private WebSecurityManager security;

	private SessionFactory sessionFactory = new SessionFactory();

	public WebSecurityManager(WebApplication application) {
		super(application);
	}

	public WebSecurityManager() {
		super(null);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		String sessionTypeName = config.getInitParameter("sessionClass");
		sessionFactory.setSessionType(sessionTypeName);
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
