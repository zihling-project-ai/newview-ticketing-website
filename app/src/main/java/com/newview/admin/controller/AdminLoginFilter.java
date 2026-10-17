package com.newview.admin.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(urlPatterns = { "/Backstage/Allpage-administrator/activity/*",
		"/Backstage/Allpage-administrator/discount/*",
		"/Backstage/Allpage-administrator/order/*",
		"/Backstage/Allpage-administrator/admin-index.jsp"})
public class AdminLoginFilter implements Filter {
	
	private FilterConfig config;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.config = filterConfig;
	}
	
	@Override
	// FilterChain 執行順序的物件
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String requestURI = req.getRequestURI();

		// 【取得 session】
		HttpSession session = req.getSession();
		System.out.println("Filter接收到" + session + "的session");

		// 【從 session 判斷此管理者是否登入過】
		Object adminAccount = session.getAttribute("adminAccount");

		if (adminAccount == null) {
			
			session.setAttribute("location", req.getRequestURI());
			res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-administrator/login/login_admin.jsp");
			System.out.println("請輸入管理者帳密登入");
			return;
						
		} else {

			System.out.println("已登入");
			chain.doFilter(request, response);
			
		}

	}

	@Override
	public void destroy() {
		config = null;
	}
	
}