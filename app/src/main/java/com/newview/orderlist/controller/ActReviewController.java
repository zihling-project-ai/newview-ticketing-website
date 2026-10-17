package com.newview.orderlist.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.orderlist.model.OrderListVO;
import com.newview.orderlist.service.OrderListService;
import com.newview.orderlist.service.OrderListServiceImpl;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserServiceImpl; // 引入UserServiceImpl

@WebServlet("/review")
public class ActReviewController extends HttpServlet {
	private OrderListService orderListService;
	private UserServiceImpl userService; // 添加UserServiceImpl

	public ActReviewController() {
		this.orderListService = new OrderListServiceImpl();
		this.userService = new UserServiceImpl(); // 初始化UserServiceImpl
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		Integer actID = Integer.parseInt(request.getParameter("actID"));
	    List<Integer> orderIDs = orderListService.findOrderIDsByActID(actID);
	    
	    List<Map<String, Object>> results = new ArrayList<>();

	    for (Integer orderId : orderIDs) {
	        Map<String, Object> resultMap = new HashMap<>();
	        OrderListVO content = orderListService.findeByPrimaryKey(orderId);
	        String userContent = content.getReviewContent();
	        Integer star = content.getFiveStarReview();
	        
	        Integer userID = orderListService.findUserIDByOrderIDAndActID(orderId, actID);

	        if (userID != null) {
	            UserVO user = userService.getUserByPK(userID);
	            String userName = user.getUserName();

	            resultMap.put("orderID", orderId);
	            resultMap.put("actID", actID);
	            resultMap.put("userName", userName);
	            resultMap.put("StarWith", star);
	            resultMap.put("review", userContent);
	        } else {
	            resultMap.put("message", "No userID found for orderID " + orderId);
	        }
	        
	        results.add(resultMap);
	    }

	    Gson gson = new Gson();
	    String jsonResult = gson.toJson(results);

	    // 發送JSON響應
	    
	    response.getWriter().write(jsonResult);
	}

	
	public static void main(String[] args) {
	    ActReviewController controller = new ActReviewController();

	    // 設定 actID
	    Integer actID = 1; 

	    // 從指定的 actID 獲取所有相關的 orderID
	    List<Integer> orderIDs = controller.orderListService.findOrderIDsByActID(actID);
	    
	    // 遍歷每個 orderID
	    for (Integer orderId : orderIDs) {
	        OrderListVO content = controller.orderListService.findeByPrimaryKey(orderId);
	        String userContent = content.getReviewContent();
	        Integer star = content.getFiveStarReview();
	        Integer userID = controller.orderListService.findUserIDByOrderIDAndActID(orderId, actID);
	        
	        if (userID != null) {
	            UserVO user = controller.userService.getUserByPK(userID);
	            String userName = user.getUserName();
	            System.out.println("Order ID: " + orderId);
	            System.out.println("Act ID: " + actID);
	            System.out.println("User ID: " + userID);
	            System.out.println("User Name: " + userName);
	            System.out.println("Star: " + star);
	            System.out.println("Review: " + userContent);
	            System.out.println("----------");  // 為了分隔每筆資料
	        } else {
	            System.out.println("No userID found for orderID " + orderId);
	        }
	    }
	}


}
