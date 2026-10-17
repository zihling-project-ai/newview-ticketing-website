package com.newview.post.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

@WebServlet("/GetMemberSession")
public class GetMemberSession extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		String action = req.getParameter("action");

		if ("getmember".equals(action)) {
			
			String userIDString = (String) req.getSession().getAttribute("userID");
			Integer userID = Integer.parseInt(userIDString);
			UserService userSvc = new UserServiceImpl();
			UserVO userVOs = userSvc.getUserByPK(userID);
			
			Map<String, Object> jsonData = new HashMap<>();
			jsonData.put("userAccount", userVOs.getUserAccount());
			jsonData.put("userNickname", userVOs.getUserNickname());

			Gson gson = new Gson();
			String json = gson.toJson(jsonData);
			res.getWriter().write(json);
		}

	}
}
