	package com.newview.user.controller;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class LoginFilter
 */

@WebFilter(urlPatterns = { "/member.html","/seatchangeWebsocketMiddle.html","/change-personal-info.html", "/my-profile.html", "/change-password.html", "/forum_dopost.html", "/seatChooseWebsocketSmall.jsp","/seatChooseWebsocket.jsp","/seatChooseWebsocketLarge.jsp" })

public class LoginFilter extends HttpFilter implements Filter {

	/**
	 * @see HttpFilter#HttpFilter()
	 */
	public LoginFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// 強轉型 HttpServletRequest, HttpServletResponse
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		// 設定編碼格式
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");

		// 取得 PrintWriter
//				PrintWriter out = res.getWriter();

		// 成功過濾 -> 提示訊息
		System.out.println("成功啟用sign-in filter");

		// 取得 session
		HttpSession session = req.getSession();

		// 從 session 判斷此 user 是否登入過
		String account = (String) session.getAttribute("account");
		if (account == null) {
			// 紀錄先前網站位置
//			session.setAttribute("location", "http://localhost:8081/"+req.getRequestURI());
			session.setAttribute("location", req.getRequestURI());
			res.sendRedirect(req.getContextPath() + "/sign-in.html");
			System.out.println("網頁重導至 signIn");
			
		} else {
			System.out.println("filter-else -> 已登入，請從 session 取得 userID");			
			chain.doFilter(request, response);
		}
		
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
