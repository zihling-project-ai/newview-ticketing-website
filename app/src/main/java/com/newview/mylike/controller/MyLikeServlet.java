package com.newview.mylike.controller;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.newview.mylike.service.*;
import com.newview.act.model.ActVO;
import com.newview.mylike.model.*;
import com.newview.user.model.*;

public class MyLikeServlet extends HttpServlet{
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		doPost(req, res);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		
		 if ("insert".equals(action)) { // 來自       .jsp的請求  
				
				List<String> errorMsgs = new LinkedList<String>();
				// Store this set in the request scope, in case we need to
				// send the ErrorPage view.
				req.setAttribute("errorMsgs", errorMsgs);

					/***********************1. 接收請求參數 - 輸入格式的錯誤處理*************************/
					Integer userID = Integer.valueOf(req.getParameter("userID"));
					if (userID == null) {
						errorMsgs.add("會員帳號有誤，請重新登入");
					} 
					
					Integer actID = Integer.valueOf(req.getParameter("actID"));
					if (actID == null) {
						errorMsgs.add("請選擇欲加入「我的最愛」的活動編號");
					}
					

					MyLikeVO myLikeVO= new MyLikeVO();
					
					UserVO userVO = new UserVO();
					userVO.setUserID(userID);
					myLikeVO.setUser(userVO);
					
					ActVO actVO = new ActVO();
					actVO.setActID(actID);
					myLikeVO.setAct(actVO);


					// Send the use back to the form, if there were errors
					if (!errorMsgs.isEmpty()) {
						req.setAttribute("myLikeVO", myLikeVO); 
						RequestDispatcher failureView = req
								.getRequestDispatcher("  .jsp");
						failureView.forward(req, res);
						return;
					}
					
					/***************************2.***************************************/
					MyLikeServiceImpl myLikeSvc = new MyLikeServiceImpl();
					myLikeVO = myLikeSvc.insertMyLike(userID, actID);
					
					/***************************3.Send the Success view**********/
					String url = "/my-favourite.jsp";
					RequestDispatcher successView = req.getRequestDispatcher(url); 
					successView.forward(req, res);				
			}
			
		
		
		
		if ("delete".equals(action)) { // 來自my-favourite.jsp的請求

			List<String> errorMsgs = new LinkedList<String>();
			// Store this set in the request scope, in case we need to
			// send the ErrorPage view.
			req.setAttribute("errorMsgs", errorMsgs);
	
				/***************************1.***************************************/
				Integer myLikeID = Integer.valueOf(req.getParameter("myLikeID"));
				
				/***************************2.***************************************/
				MyLikeServiceImpl myLikeSvc = new MyLikeServiceImpl();
				myLikeSvc.deleteMyLike(myLikeID);
				
				/***************************3.Send the Success view***********/								
				String url = "/my-favourite.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);
		}
		
	}
	
}