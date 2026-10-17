package com.newview.pubuser.controller;

import java.io.IOException;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.newview.pubuser.service.*;
import com.newview.publisher.service.*;
import com.newview.publisher.model.*;
import com.newview.pubuser.model.*;

@WebServlet("/pubuser/pubuser.do")
public class PubuserServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");

		if ("add".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				/************************* 接收請求參數 **************************/
				String pubNickname = req.getParameter("pubNickname");
				String pubAccount = req.getParameter("pubAccount").trim();
				String pubPassword = req.getParameter("pubPassword").trim();
				byte pubAuthority = Byte.parseByte(req.getParameter("pubAuthority"));
				// Integer pubID = Integer.valueOf(req.getParameter("pubID").trim());
				Integer pubID = new Integer(req.getParameter("pubID").trim());

				// 檢查請求參數接收
				System.out.println(pubNickname);
				System.out.println(pubAccount);

				System.out.println(pubAuthority);
				System.out.println(pubID);

				/************************* 輸入錯誤處理 **************************/

				String nameReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$";
				if (pubNickname == null || pubNickname.trim().length() == 0) {
					errorMsgs.add("員工姓名: 請勿空白");

				} else if (!pubNickname.trim().matches(nameReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("員工姓名: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間");

				}

				if (pubAccount == null || pubAccount.trim().length() == 0) {
					errorMsgs.add("帳號請勿空白");
				}

				if (pubPassword == null || pubPassword.trim().length() == 0) {
					errorMsgs.add("密碼請勿空白");
				}

				/************************* Package data **************************/
				// Package data
				PubUserVO pubuserVO = new PubUserVO();
				pubuserVO.setPubNickname(pubNickname);
				pubuserVO.setPubAccount(pubAccount);
				pubuserVO.setPubPassword(pubPassword);
				pubuserVO.setPubAuthority(pubAuthority);
				// coz Association Publisher create PublisherVO object to set pubID
				PublisherVO pubVO = new PublisherVO();
				pubVO.setPubID(pubID);
				// to PublisherVO to set PublisherVO
				pubuserVO.setPublisherVO(pubVO);
				System.out.println(pubuserVO);

				// Send error back to form
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("pubuserVO", pubuserVO);
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-add.jsp");
					failureView.forward(req, resp);
					return;

				}

				/************************* 開始新增資料 **************************/
				PubUserService pubUserSvc = new PubUserService();
				pubuserVO = pubUserSvc.addPubUser(pubNickname, pubAccount, pubPassword, pubAuthority, pubID);
				System.out.println(pubuserVO);
				System.out.println("新增成功");

				/************************* 回傳資料路徑 **************************/

				String url = "/Backstage/Allpage-publisher/user/user-listAll.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);

			} catch (Exception e) {
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-add.jsp");
				failureView.forward(req, resp);
			}
		}

		if ("update".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			// 送出修改的來源網頁路徑
			String requestURL = req.getParameter("requestURL");

			try {
				/************************* 接收請求參數 **************************/

				Integer pubUserID = new Integer(req.getParameter("pubUserID").trim());
				String pubNickname = req.getParameter("pubNickname");
				String pubAccount = req.getParameter("pubAccount").trim();
				String pubPassword = req.getParameter("pubPassword").trim();
				byte pubAuthority = Byte.parseByte(req.getParameter("pubAuthority"));
				Integer pubID = new Integer(req.getParameter("pubID").trim());

				// 檢查請求參數接收
				System.out.println(pubNickname);
				System.out.println(pubAccount);

				System.out.println(pubAuthority);
				System.out.println(pubID);

				/************************* 輸入錯誤處理 **************************/

				String nameReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$";
				if (pubNickname == null || pubNickname.trim().length() == 0) {
					errorMsgs.add("員工姓名: 請勿空白");

				} else if (!pubNickname.trim().matches(nameReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("員工姓名: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間");

				}

				if (pubAccount == null || pubAccount.trim().length() == 0) {
					errorMsgs.add("帳號請勿空白");
				}

				if (pubPassword == null || pubPassword.trim().length() == 0) {
					errorMsgs.add("密碼請勿空白");
				}

				/************************* Package data **************************/
				// Package data
				PubUserVO pubuserVO = new PubUserVO();
				pubuserVO.setPubNickname(pubNickname);
				pubuserVO.setPubAccount(pubAccount);
				pubuserVO.setPubPassword(pubPassword);
				pubuserVO.setPubAuthority(pubAuthority);
				// coz Association Publisher create PublisherVO object to set pubID
				PublisherVO pubVO = new PublisherVO();
				pubVO.setPubID(pubID);
				// to PublisherVO to set PublisherVO
				pubuserVO.setPublisherVO(pubVO);
				System.out.println(pubuserVO);

				// Send error back to form
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("pubUserVO", pubuserVO);
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-add.jsp");
					failureView.forward(req, resp);
					return;
				}

				/************************* 開始修改資料 **************************/
				PubUserService pubUserSvc = new PubUserService();
				pubuserVO = pubUserSvc.updatePubUser(pubUserID,pubNickname, pubAccount, pubPassword, pubAuthority, pubID);
				System.out.println(pubuserVO);
				System.out.println("修改成功");

				/************************* 回傳資料路徑 **************************/
				PublisherService pubSvc = new PublisherService();
				if (requestURL.equals("/Backstage/Allpage-publisher/user/user-listOne.jsp")
						|| requestURL.equals("/Backstage/Allpage-publisher/user/user-listAll.jsp"))
					req.setAttribute("user-listOne", pubSvc.getPubuserByPubID(pubID)); // 資料庫取出的list物件,存入request

				String url = requestURL;
				RequestDispatcher successView = req.getRequestDispatcher(url); // 修改成功後,轉交回送出修改的來源網頁
				successView.forward(req, resp);

				/*************************** 其他可能的錯誤處理 *************************************/

			} catch (Exception e) {
				errorMsgs.add("修改資料失敗:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-add.jsp");
				failureView.forward(req, resp);
			}

		}

		if ("delete".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			// 送出刪除的來源網頁路徑
			String requestURL = req.getParameter("requestURL");

			/************************* 接收請求參數 **************************/
			try {

				Integer pubUserID = new Integer(req.getParameter("pubUserID").trim());
				// 檢查請求參數接收
				System.out.println(pubUserID);

				/************************* 開始刪除資料 **************************/
				PubUserService pubUserSvc = new PubUserService();
				PubUserVO pubuserVO = pubUserSvc.getOnePubUser(pubUserID);
				pubUserSvc.deletePubUser(pubUserID);
				System.out.println("刪除成功");

				/************************* 回傳資料路徑 **************************/
				// 資料庫取出的list物件,存入request
				if (requestURL.equals("/Backstage/Allpage-publisher/user/user-listByID.jsp")
						|| requestURL.equals("/Backstage/Allpage-publisher/user/user-listAll.jsp"))
					req.setAttribute("user-listByID", pubUserSvc.getOnePubUser(pubuserVO.getPublisherVO().getPubID()));

				String url = requestURL;
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);

				/*************************** 其他可能的錯誤處理 **********************************/

			} catch (Exception e) {
				errorMsgs.add("刪除資料失敗:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher(requestURL);
				failureView.forward(req, resp);
			}
		}

		if ("getOneForDisplay".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				/************************* 接收請求參數 **************************/
				String str = req.getParameter("pubUserID");
				if (str == null || (str.trim()).length() == 0) {
					System.out.println(str);
					errorMsgs.add("請輸入使用者編號");
				}

				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-listAll.jsp");
					failureView.forward(req, resp);
					return;
				}

				Integer pubUserID = null;
				try {
					pubUserID = new Integer(str);

				} catch (Exception e) {
					errorMsgs.add("使用者編號格式不正確");
				}
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-listAll.jsp");
					failureView.forward(req, resp);
					return;// 程式中斷
				}

				/************************* 開始查詢資料 **************************/

				PubUserService pubUserSvc = new PubUserService();
				PubUserVO pubuserVO = pubUserSvc.getOnePubUser(pubUserID);
				System.out.println(pubuserVO);

				if (pubuserVO == null) {
					errorMsgs.add("查無資料");
				}

				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-listAll.jsp");
					failureView.forward(req, resp);
					return;// 程式中斷
				}
				/************************* 回傳資料路徑 **************************/
				req.setAttribute("pubuserVO", pubuserVO); // 資料庫取出的pubuserVO物件,存入req
				String url = "/Backstage/Allpage-publisher/user/user-listOne.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("無法取得資料:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-listAll.jsp");
				failureView.forward(req, resp);
			}
		}

		if ("getOneForUpdate".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			String requestURL = req.getParameter("requestURL"); // 送出修改的來源網頁路徑

			/************************* 接收請求參數 **************************/
			try {
				Integer pubUserID = new Integer(req.getParameter("pubUserID").trim());
				// 檢查請求參數接收
				System.out.println(pubUserID);

				/************************* 開始查詢資料 **************************/

				PubUserService pubUserSvc = new PubUserService();
				PubUserVO pubuserVO = pubUserSvc.getOnePubUser(pubUserID);
				System.out.println(pubuserVO);

				/************************* 回傳資料路徑 **************************/
				req.setAttribute("pubuserVO", pubuserVO); // 資料庫取出的pubuserVO物件,存入req
				String url = "/Backstage/Allpage-publisher/user/user-update.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("修改資料取出時失敗:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("requestURL");
				failureView.forward(req, resp);
			}
		}
		// 複合查詢
		if ("pubuserCompositeQuery".equals(action)) {
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			try {
				/************************* 接收請求參數 **************************/
				
				Map<String, String[]> map = req.getParameterMap();
				System.out.println("複合查詢接收到的請求為" + map);

				/************************* 開始查詢資料 **************************/
				
				// 建立PubUserService 使用其getByCompositeQuery方法
				PubUserService pubuserSvc = new PubUserService();
				List<PubUserVO> pulist = pubuserSvc.getByCompositeQuery(map);
				System.out.println("複合查詢查詢到的資料為" + pulist);
				
				/************************* 回傳資料路徑 **************************/
				req.setAttribute("pulist", pulist); // 資料庫取出的list物件,存入request
				String url = "/Backstage/Allpage-publisher/user/listusers_ByCQ.jsp";
				
				RequestDispatcher successView = req.getRequestDispatcher(url);
				System.out.println("轉交網址至listusers_ByCQ.jsp");
				System.out.println(successView.toString());
				
				successView.forward(req, resp);
				System.out.println("轉交成功...............");

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("複合查詢出現例外!!");
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-listAll.jsp");
				failureView.forward(req, resp);
			}

		}

		if ("login".equals(action)) {
			/************************* 接收請求參數 **************************/
			String pubAccount = req.getParameter("pubAccount").trim();
			String pubPassword = req.getParameter("pubPassword").trim();

			System.out.println(pubAccount);

			/************************* 開始查詢資料 **************************/
			PubUserService pubUserSvc = new PubUserService();
			System.out.println(pubUserSvc);

			boolean loginSuccessful = pubUserSvc.authenticate(pubAccount, pubPassword);
			System.err.println(loginSuccessful);

			/************************* 回傳資料路徑 **************************/
			if (!loginSuccessful) {
				System.out.println("登入失敗");
				resp.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/login/login.jsp");
				return;
			} else {
				System.out.println("登入成功");
				HttpSession session = req.getSession();
				session.setAttribute("pubAccount", pubAccount);

				//將DAO裡的找到的資訊存入pubuserVO
				PubUserService pubuserSvc = new PubUserService();
				PubUserVO pubuserVO = pubuserSvc.getByAccountInfo(pubAccount);
				
				//將pubuserVO的資料存入session
				session.setAttribute("pubuserVO", pubuserVO);
				//將pubID存入session裡
				session.setAttribute("pubID",pubuserVO.getPublisherVO().getPubID());
				byte authority = pubuserVO.getPubAuthority();
				
				/*
				//依照權限導向不同頁面
				if(authority == 0) {
					resp.sendRedirect("admin.jsp");
				}else {
					resp.sendRedirect("user.jsp");
				}
				*/
				
				
				try {
					String location = (String) session.getAttribute("location");
					if (location != null) {
						session.removeAttribute("location"); // *看看有無來源網頁 (-->如有來源網頁:則重導至來源網頁)
						resp.sendRedirect(location);
						return;
					}
				} catch (Exception ignored) {
				}
				
				
				resp.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/user/user-add.jsp");
				// *(-->如無來源網頁:則重導至後台首頁)
			}

		}
		
		
		if ("logout".equals(action)) {
			/************************* 接收請求參數 **************************/
			String logout = req.getParameter("logout");
			System.out.println("servlet已接收參數" + logout);
			
			HttpSession session = req.getSession();
			session.invalidate();

	        System.out.println("session已清除");
	        resp.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/login/login.jsp");
	        System.out.println("已導向");

		}
		
		
		
		
		
		
		

	}

}
