package com.newview.user.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

@WebServlet("/GetUserInfo")
public class UserInfoController extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		HashMap<String, Object> data = new HashMap<>();
		HttpSession session = req.getSession();
		Gson gson = new Gson();

		// 從 session 取出 userID (已套用濾器 -> session 中有存放 userID)
		String userID = (String) session.getAttribute("userID");

		// 取得資料庫資料
		UserService userSvc = new UserServiceImpl();
		UserVO user = userSvc.getUserByPK(Integer.valueOf(userID));

		// 打包給前端做第一次畫面渲染 (秀出原先資料)
		data.put("account", user.getUserAccount());
		data.put("name", user.getUserName());
		data.put("nickname", user.getUserNickname());
		data.put("email", user.getUserEmail());
		data.put("birthdate", user.getUserBirth().toString());
		data.put("cellphone", user.getUserCell());
		/******************************************* 權限說明 *******************************************/
		/**
		 *									1 -> 可購買、可發言
		 *									2 -> 不可購買、限制發言
		 */
		/******************************************* 權限說明 *******************************************/
		data.put("buyAuthority", user.getBuyAuthority());
		data.put("speakAuthority", user.getSpeakAuthority());

		// 回傳 json 物件給前端做畫面渲染
		String json = gson.toJson(data);
		out.write(json);

	}
}
