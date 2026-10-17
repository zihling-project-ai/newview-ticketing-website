package com.newview.orders.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.newview.orders.model.Orderlist;
import com.newview.orders.service.OrdersService2;
import com.newview.orders.service.impl.OrdersServiceImpl2;
import com.newview.util.HibernateUtil;

@WebServlet("/orders")
public class OrdersController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Gson gson = new GsonBuilder()
			.setDateFormat("yyyy/MM/dd HH:mm:ss")
			.create();
	private OrdersService2 service;

	@Override
	public void init() throws ServletException {
		service = new OrdersServiceImpl2();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer userID = 0;
		String uid = (String) req.getSession().getAttribute("userID");
		if (uid != null) {
			userID = Integer.valueOf(uid);
		} else {
			userID = 1;
		}
		var list = service.findByUserID(userID);
		resp.setContentType("application/json; charset=utf-8");
		resp.getWriter().write(gson.toJson(list));
		HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
	}

//	@Override
//	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		Integer userID = 0;
//		Integer uid = (Integer) req.getSession().getAttribute("userID");
//		if (uid != null) {
//			userID = uid;
//		} else {
//			userID = 1;
//		}
//		var actID = Integer.parseInt(req.getParameter("actID"));
//		var result = service.removeOrders(orderID, userID);
//
//		JsonObject respBody = new JsonObject();
//		respBody.addProperty("result", result);
//		resp.getWriter().write(respBody.toString());
//	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		var orderListID = Integer.parseInt(req.getParameter("orderListID"));
		resp.setContentType("application/json; charset=utf-8");
		var list = service.findByOrderListID(orderListID);
		resp.getWriter().write(gson.toJson(list));
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		var orderlist = gson.fromJson(req.getReader(), Orderlist.class);
		var respBody = new JsonObject();
		respBody.addProperty("success", service.saveCom(orderlist));
		resp.getWriter().write(gson.toJson(respBody));
	}
	
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		var orderID = Integer.parseInt(req.getParameter("orderID"));
		var list = service.cancelOrders(orderID);


		JsonObject respBody = new JsonObject();
		respBody.addProperty("result", list);
		resp.getWriter().write(respBody.toString());
	}
	
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		var orderListID = Integer.parseInt(req.getParameter("orderListID"));
		var list = service.cancelReview(orderListID);


		JsonObject respBody = new JsonObject();
		respBody.addProperty("result", list);
		resp.getWriter().write(respBody.toString());
	}
}
