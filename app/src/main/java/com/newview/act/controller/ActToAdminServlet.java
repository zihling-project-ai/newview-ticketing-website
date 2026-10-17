package com.newview.act.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.MultipartConfig;

import javax.servlet.http.Part;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.google.gson.Gson;
import com.newview.act.model.ActDAO;
import com.newview.act.model.ActDAOHibernateImpl;
import com.newview.act.model.ActVO;
import com.newview.act.service.*;
import com.newview.actcategory.model.ActCategory;
import com.newview.actpic.model.ActPic;
import com.newview.actpic.model.ActPicDAO;
import com.newview.actpic.model.ActPicDAOHibernateImpl;
import com.newview.cityaddress.model.CityAddress;
import com.newview.admin.model.AdminVO;
import com.newview.util.HibernateUtil;

@WebServlet("/act/act.admindo")
@MultipartConfig(maxFileSize = 1073741824)
public class ActToAdminServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		doPost(req, res);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		// 對應到的service 實體
		ActService actService = new ActServiceImpl();
		ActPicService actPicService = new ActPicServiceImpl();

		ActDAO actDAO = new ActDAOHibernateImpl();
		ActPicDAO actPicDAO = new ActPicDAOHibernateImpl();
		ActUpdateService actUpdateService = new ActUpdateServiceImpl(actDAO, actPicDAO);

		// 接收的參數
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");

		res.setContentType("application/json");
		
		
		if ("delete".equals(action)) {

			/********** 收到請求參數 **********/
			String actIDDelete = req.getParameter("actIDDelete");
			// String actIDelete = req.getParameter("actIDelete");
			System.out.println("servlet收到刪除請求：" + actIDDelete);

			/********** 輸入錯誤處理 **********/
			if (actIDDelete != null) {
				try {

					/********** 查詢資料 **********/
					actService.delete(Integer.parseInt(actIDDelete)); // 至資料庫刪除資料
					res.setContentType("application/json; charset=UTF-8");

					res.getWriter().println("Act ID " + actIDDelete + " 成功"); // 回應刪除成功訊息

					String url = "/Backstage/Allpage-administrator/activity/activity-list.jsp";
					RequestDispatcher successView = req.getRequestDispatcher(url);
					successView.forward(req, res);

				} catch (Exception e) {
					e.printStackTrace();
					res.getWriter().println("Act ID " + actIDDelete + " 失敗"); // 回應刪除失敗訊息

				}
			} else {

				res.getWriter().println("Act ID 不能為空"); // 回應其他錯誤訊息
			}
		}
		
	}
	

}