package com.newview.orderlist.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newview.orderlist.model.OrderListVO;
import com.newview.orderlist.service.OrderListService;

public class SearchOrderListServlet extends HttpServlet{
	 private static final long serialVersionUID = 1L;
	    private OrderListService orderListService; 

	    public void setOrderListService(OrderListService orderListService) {
	        this.orderListService = orderListService;
	    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String UserIDStr = request.getParameter("userID");
		Integer UserID = Integer.parseInt(UserIDStr);
		OrderListVO orderList = orderListService.findeByPrimaryKey(UserID);
		
		 if (orderList != null) {
		       //找到數據後轉發
		        request.setAttribute("orderList", orderList); 
		        request.getRequestDispatcher("/myTicket.html").forward(request, response);
		    } else {
		       System.out.println("沒有資料");
		    }
			
	}
}
