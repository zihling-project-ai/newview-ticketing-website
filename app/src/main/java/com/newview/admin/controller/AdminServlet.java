package com.newview.admin.controller;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import com.newview.admin.model.*;
import com.newview.admin.service.*;


@WebServlet("/admin/admin.do")
public class AdminServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {
		doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		
		if ("adminLogin".equals(action)) { // 來自login_admin.jsp的請求
			
			/************************* 接收請求參數 **************************/
			String adminAccount = req.getParameter("adminAccount").trim();
			String adminPassword = req.getParameter("adminPassword").trim();

			System.out.println(adminAccount);

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			

			/*********1.接收請求參數 - 輸入格式的錯誤處理*********/			

			if (adminAccount == null || (adminAccount.trim()).length() == 0) {
				errorMsgs.add("請輸入帳號");
			}
			if (adminPassword == null || (adminPassword.trim()).length() == 0) {
				errorMsgs.add("請輸入密碼");

			}
			

			if (!errorMsgs.isEmpty()) {
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-administrator/login/login_admin.jsp");
				failureView.forward(req, res);
				return; //程式中斷
			}
			
			
			/************************* 2. 開始查詢資料 **************************/
			AdminService adminSvc = new AdminService();
			System.out.println(adminSvc);
			
			boolean loginSuccess = adminSvc.authenticate(adminAccount, adminPassword);
			
			/************************* 3. 回傳路徑 **************************/
			if (!loginSuccess) {
				System.out.println("登入失敗");
				res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-administrator/login/login_admin.jsp");
				return;
			} else {
				System.out.println("登入成功");
				HttpSession session = req.getSession();
				session.setAttribute("adminAccount", adminAccount);

				//將DAO裡的找到的資訊存入adminVO
				AdminService adminsvc = new AdminService();
				AdminVO adminVO = adminsvc.getByAccountInfo(adminAccount);
				
				//將adminVO的資料存入session
				session.setAttribute("adminVO", adminVO);
				
				try {
					String location = (String) session.getAttribute("location");
					if (location != null) {
						session.removeAttribute("location"); // *看看有無來源網頁 (-->如有來源網頁:則重導至來源網頁)
						res.sendRedirect(location);
						return;
					}
				} catch (Exception ignored) {
				}
				
				
				res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-administrator/admin-index.jsp");
				// *(-->如無來源網頁:則重導至後台首頁)

				
				
			}		
		}
		
		if ("adminLogout".equals(action)) {
			/************************* 接收請求參數 **************************/
			String logout = req.getParameter("adminLogout");
			System.out.println("servlet已接收參數" + logout);
			
			HttpSession session = req.getSession();
			session.invalidate();

	        System.out.println("session已清除");
	        res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-administrator/login/login_admin.jsp");
	        System.out.println("已導向");

			}
		
		
		}

	
}