package com.newview.orders.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.newview.orders.model.OrdersVO;
import com.newview.user.model.UserVO;
import com.newview.publisher.model.PublisherVO;
import com.newview.discount.model.DiscountVO;
import com.newview.orders.service.OrdersService;
import com.newview.publisher.service.PublisherService;
import com.newview.pubuser.model.PubUserVO;
import com.newview.pubuser.service.PubUserService;


@WebServlet("/orders/orders.do")
public class OrdersServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		doPost(req, res);
	}	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");


		if ("update".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			// 送出修改的來源網頁路徑
			String requestURL = req.getParameter("requestURL");

			try {
				/************************* 接收請求參數 - 輸入格式的錯誤處理 **************************/

				Integer userID = new Integer(req.getParameter("userID").trim());
				Integer ordTotal = new Integer(req.getParameter("ordTotal").trim());
				Integer discount = new Integer(req.getParameter("discount").trim());
				Integer discountPrice = new Integer(req.getParameter("discountPrice").trim());
				//Timestamp ordTime = java.sql.Timestamp.valueOf(req.getParameter("ordTime").trim());
				Integer pubID = new Integer(req.getParameter("pubID").trim());
				Integer ordType = new Integer(req.getParameter("ordType").trim());
				Integer actQuantity = new Integer(req.getParameter("actQuantity").trim());
				Integer discountNO = new Integer(req.getParameter("discountNO").trim());


				// 檢查請求參數接收
				System.out.println(userID);
				System.out.println(ordTotal);
				System.out.println(discount);
				System.out.println(discountPrice);
				//System.out.println(ordTime);
				System.out.println(pubID);
				System.out.println(ordType);
				System.out.println(actQuantity);
				System.out.println(discountNO);

				
				
				java.sql.Timestamp ordTime = null;
				try {
					ordTime = java.sql.Timestamp.valueOf(req.getParameter("ordTime").trim());
				} catch (IllegalArgumentException e) {
					ordTime=new java.sql.Timestamp(System.currentTimeMillis());
					errorMsgs.add("請輸入訂單成立時間!");
				}


				/************************* Package data **************************/
				// Package data
				OrdersVO ordersVO = new OrdersVO();
				ordersVO.setOrdTotal(ordTotal);
				ordersVO.setDiscount(discount);
				ordersVO.setDiscountPrice(discountPrice);
				ordersVO.setOrdTime(ordTime);
				ordersVO.setOrdType(ordType);
				ordersVO.setActQuantity(actQuantity);
				
				// coz Association User create UserVO object to set userID
				UserVO userVO = new UserVO();
				userVO.setUserID(userID);
				// to UserVO to set UserVO
				ordersVO.setUserVO(userVO);
				System.out.println(ordersVO);
				
				// coz Association Publisher create PublisherVO object to set pubID
				PublisherVO pubVO = new PublisherVO();
				pubVO.setPubID(pubID);
				// to PublisherVO to set PublisherVO
				ordersVO.setPublisherVO(pubVO);
				System.out.println(ordersVO);
				
				// coz Association Discount create DiscountVO object to set discountNO
				DiscountVO discountVO = new DiscountVO();
				discountVO.setDiscountNO(discountNO);
				// to discountVO to set DiscountVO
				ordersVO.setDiscountVO(discountVO);
				System.out.println(discountVO);

				// Send error back to form
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("ordersVO", ordersVO);
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-administrator/order/order-list.jsp"); 
					//導入網址要再改, 頁面要再做
					failureView.forward(req, res);
					return;
				}

				/************************* 開始修改資料 **************************/
				OrdersService ordersSvc = new com.newview.orders.service.OrdersServiceImpl();
				ordersVO = ordersSvc.updateOrders(userID, ordTotal, discount, discountPrice, ordTime, pubID, ordType, actQuantity, discountNO);
				System.out.println(ordersVO);
				System.out.println("修改成功");

				/************************* 回傳資料路徑 **************************/
				//再研究
				PublisherService pubSvc = new PublisherService();
				if (requestURL.equals("/Backstage/Allpage-publisher/user/user-listOne.jsp")
						|| requestURL.equals("/Backstage/Allpage-publisher/user/user-listAll.jsp"))
					req.setAttribute("user-listOne", pubSvc.getPubuserByPubID(pubID)); // 資料庫取出的list物件,存入request

				String url = requestURL;
				RequestDispatcher successView = req.getRequestDispatcher(url); // 修改成功後,轉交回送出修改的來源網頁
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/

			} catch (Exception e) {
				errorMsgs.add("修改資料失敗:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-administrator/order/order-list.jsp");
				        //重導網址要再改 頁面要再做
				failureView.forward(req, res);
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
					failureView.forward(req, res);
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
					failureView.forward(req, res);
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
					failureView.forward(req, res);
					return;// 程式中斷
				}
				/************************* 回傳資料路徑 **************************/
				req.setAttribute("pubuserVO", pubuserVO); // 資料庫取出的pubuserVO物件,存入req
				String url = "/Backstage/Allpage-publisher/user/user-listOne.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("無法取得資料:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-listAll.jsp");
				failureView.forward(req, res);
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
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("修改資料取出時失敗:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("requestURL");
				failureView.forward(req, res);
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
				
				successView.forward(req, res);
				System.out.println("轉交成功...............");

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("複合查詢出現例外!!");
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-listAll.jsp");
				failureView.forward(req, res);
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
				res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/login/login.jsp");
				return;
			} else {
				System.out.println("登入成功");
				HttpSession session = req.getSession();
				String se = session.toString();
				System.out.println(se);
				session.setAttribute("pubAccount", pubAccount);

				try {
					String location = (String) session.getAttribute("location");
					if (location != null) {
						session.removeAttribute("location"); // *看看有無來源網頁 (-->如有來源網頁:則重導至來源網頁)
						res.sendRedirect(location);
						return;
					}
				} catch (Exception ignored) {
				}
				res.sendRedirect(req.getContextPath() + "/Backstage/Allpage-publisher/pub-index.jsp");
				// *(-->如無來源網頁:則重導至後台首頁)
			}

		}

	}

	
}
