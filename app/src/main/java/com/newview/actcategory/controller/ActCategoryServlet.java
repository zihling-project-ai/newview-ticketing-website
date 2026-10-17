package com.newview.actcategory.controller;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;

import com.newview.act.model.ActVO;
import com.newview.actcategory.model.*;
import com.newview.actcategory.service.*;



@WebServlet("/actcategory/actcategory.do")
public class ActCategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		
		
		if ("add".equals(action)) { // 來自activity-add.jsp的請求
			
			List<String> errorMsgs = new LinkedList<String>();
			// Store this set in the request scope, in case we need to
			// send the ErrorPage view.
			req.setAttribute("errorMsgs", errorMsgs);
			
			/***********************1.接收請求參數 - 輸入格式的錯誤處理**************/
			
			String actCategoryName = req.getParameter("actCategoryName").trim();
			if (actCategoryName == null || actCategoryName.trim().length() == 0) {
				errorMsgs.add("分類名稱，請勿留空");
			}
			
			ActCategory actCategory = new ActCategory();
			actCategory.setActCategoryName(actCategoryName);
			
			if (!errorMsgs.isEmpty()) {
				req.setAttribute("actCategory", actCategory); 
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-administrator/activity-add.jsp");
				failureView.forward(req, res);
									return;
								}
			
			/***********************2.開始新增資料**************/
			
			ActCategoryService actSvc = new ActCategoryService();
			actCategory = actSvc.addActCategory(actCategoryName);
			
			/***********************3.新增完成，準備轉交(Send the Success view)*************/
			
			req.setAttribute("actCategory", actCategory);
			String url = "/Backstage/Allpage-administrator/activity/activity-category.jsp";
			RequestDispatcher successView = req.getRequestDispatcher(url);
			successView.forward(req, res);
			
		}
		
		
		if ("update".equals(action)) {
			
		}
		
		
		if ("delete".equals(action)) {
			
//			List<String> errorMsgs = new LinkedList<String>();
//			req.setAttribute("errorMsgs", errorMsgs);
//			// 送出刪除的來源網頁路徑
//			String requestURL = req.getParameter("requestURL");
			
			Integer actCategoryID = Integer.valueOf(req.getParameter("actCategoryID"));
			
			ActCategoryService actCategorySvc = new ActCategoryService();
			ActCategory actCategory = actCategorySvc.getOneActCategory(actCategoryID);
			actCategorySvc.deleteActCategory(actCategoryID);
			
//			if (requestURL.equals("/Backstage/Allpage-administrator/activity/activity-category.jsp")
//				req.setAttribute("user-listByID", actCategorySvc.getOneActCategory(actCategory.getPublisherVO().getPubID()));

			
			
			String url = "/Backstage/Allpage-administrator/activity/activity-category.jsp";
			RequestDispatcher successView = req.getRequestDispatcher(url);
			successView.forward(req, res);
			
		}
		
		
		
		
		
		
		
		 ActCategoryDAO dao = new ActCategoryDAOHibernateImpl();
		 List<ActCategory> actCategoryList = dao.getAll();
		 
		 
		 String actCategoryName = req.getParameter("actCategoryName");
		 
		 
//		 for (ActCategory actCategory : actCategoryList) {
//			 
//		 }
		
		
	}
}