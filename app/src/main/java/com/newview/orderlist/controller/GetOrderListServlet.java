package com.newview.orderlist.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.act.model.ActVO;
import com.newview.orderlist.model.OrderListVO;
import com.newview.orderlist.service.OrderListService;
import com.newview.orderlist.service.OrderListServiceImpl;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

@WebServlet("/GetOrderListServlet")
public class GetOrderListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");

		OrderListService ordSvc = new OrderListServiceImpl();
		OrderListVO orderVO = new OrderListVO();

		ActVO actVO = new ActVO();

		// 獲取購買人User訊息from Session
		String currentUserID = (String) req.getSession().getAttribute("userID");
		int userID = Integer.parseInt(currentUserID);
		UserService userSvc = new UserServiceImpl();
		UserVO userVOs = userSvc.getUserByPK(userID);

		Map<String, Object> OrderInfo = new HashMap<>();

		// 基本資訊確認
		OrderInfo.put("userID", userID);
		OrderInfo.put("userName", userVOs.getUserName());
		OrderInfo.put("userBirth", userVOs.getUserBirth());
		OrderInfo.put("userCell", userVOs.getUserCell());
		OrderInfo.put("userEmail", userVOs.getUserEmail());
		
		Gson gson = new Gson();
		String json = gson.toJson(OrderInfo);
		res.getWriter().write(json);

	}

}