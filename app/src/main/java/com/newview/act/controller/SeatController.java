package com.newview.act.controller;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/Seat")
public class SeatController extends HttpServlet {
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		String jsonData = sb.toString();

		Gson gson = new Gson();

		JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

		// 提取JsonObject的資料
//		String userid = jsonObject.get("userid").getAsString();
		String actID = jsonObject.get("actID").getAsString();       
		String actPrice = jsonObject.get("actPrice").getAsString();
		String actName = jsonObject.get("actName").getAsString();
		String actScopeStr = jsonObject.get("actScope").getAsString();
		Integer actScope = Integer.parseInt(actScopeStr);
		// 獲取 HttpSession
		HttpSession session = request.getSession();
		String userID = (String) session.getAttribute("userID");
		// 將這些資料設置為 session 屬性
		
		session.setAttribute("actID", actID);
		session.setAttribute("actPrice", actPrice);
		session.setAttribute("actName", actName);
		session.setAttribute("actScope", actScope);

		switch(actScope) {
		case 1:
			request.getRequestDispatcher("seatChooseWebsocketSmall.jsp").forward(request, response);
			break;
		case 2:
			request.getRequestDispatcher("seatChooseWebsocket.jsp").forward(request, response);
			break;
		case 3:
			request.getRequestDispatcher("seatChooseWebsocketLarge.jsp").forward(request, response);
			break;
		}
		
	}
}
