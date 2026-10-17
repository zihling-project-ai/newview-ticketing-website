package com.newview.pubuser.controller;

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

import com.newview.pubuser.model.PubUserVO;
import com.newview.pubuser.service.PubUserService;

@WebFilter(urlPatterns = { "/Backstage/Allpage-publisher/activity/*",
		"/Backstage/Allpage-publisher/discount/*",
		"/Backstage/Allpage-publisher/order/*",
		"/Backstage/Allpage-publisher/pages/*",
		"/Backstage/Allpage-publisher/user/*" ,
		"/Backstage/Allpage-publisher/pub-index.jsp"})
public class PubuserLoginFilter implements Filter {

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

		// 【從 session 判斷此user是否登入過】
		Object pubAccount = session.getAttribute("pubAccount");

		
		if (pubAccount == null) {
			session.setAttribute("location", req.getRequestURI());
			res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/login/login.jsp");
			System.out.println("需登入帳密，登入後台");
			return;
			
			
		} else {

			System.out.println("已登入");
			chain.doFilter(request, response);
			
			/*
			//將pubuserVO的資料存入session
			PubUserService pubuserSvc = new PubUserService();
			PubUserVO pubuserVO = pubuserSvc.getByAccountInfo((String) pubAccount);
			
			session.setAttribute("pubuserVO", pubuserVO);
			System.out.println(pubuserVO);
			
			// 登入後判斷權限
			byte authority = pubuserVO.getPubAuthority();
			System.out.println(authority);

			switch (authority) {
			case 0:
				System.out.println("此用戶為最高權限");
				chain.doFilter(request, response);
				break;
			case 1:
				System.out.println("此用戶為一般權限");
				if (requestURI.contains("A.jsp") || requestURI.contains("B.jsp")) {
					chain.doFilter(request, response);
				} else {
					// 其他頁面禁止訪問；將轉向到首頁
					res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/login/login.jsp");
				}
				break;
			default:
				// 其他頁面禁止訪問；將轉向到首頁
				res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/login/login.jsp");
				break;
			}
			*/
		}

	}

	@Override
	public void destroy() {
		config = null;
	}

}
