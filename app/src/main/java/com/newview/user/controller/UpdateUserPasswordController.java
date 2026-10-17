package com.newview.user.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

@WebServlet("/UpdateUserPassword")
public class UpdateUserPasswordController extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		HashMap<String, String> data = new HashMap<>();
		HttpSession session = req.getSession();
		Gson gson = new Gson();
		String json = null;

		// 從 session 取出 userID (已套用濾器 -> session 中有存放 userID)
		// 提供修改用戶密碼時使用
		String userID = (String) session.getAttribute("userID");
		System.out.println(userID);

		// 接收前端傳遞參數
		String oldPassword = req.getParameter("oldPassword");
		String newPassword = req.getParameter("newPassword");
		String confirmNewPassword = req.getParameter("confirmNewPassword");




		// 比較新密碼 / 確認密碼兩者是否一致
		if (!newPassword.equals(confirmNewPassword)) {
			data.put("status", "confirmFailed");
			json = gson.toJson(data);
			out.write(json);
			return;
		}

		// 加密前端收到之密碼，再與資料庫中密碼比對
		String oldHashPassword = encryptPassword(oldPassword);
		String newHashPassword = encryptPassword(newPassword);

		// 取得資料庫資料
		UserService userSvc = new UserServiceImpl();
		UserVO user = userSvc.getUserByPK(Integer.valueOf(userID));
		String userPassword = user.getUserPassword();

		// 原始密碼輸入錯誤
		if (!oldHashPassword.equals(userPassword)) {
			data.put("status", "originalFailed");
			json = gson.toJson(data);
			out.write(json);
			return;
		}

		// 排除錯誤後開始修改資料
		user.setUserID(Integer.valueOf(userID));
		// 密碼
		user.setUserPassword(newHashPassword);

		/*************************** 不修改區塊 ***************************/

		/**************************** 基本資料 ****************************/
		user.setUserName((userSvc.getUserByPK(Integer.valueOf(userID)).getUserName()));
		user.setUserAccount((userSvc.getUserByPK(Integer.valueOf(userID)).getUserAccount()));
		user.setUserBirth((userSvc.getUserByPK(Integer.valueOf(userID)).getUserBirth()));
		user.setUserCell((userSvc.getUserByPK(Integer.valueOf(userID)).getUserCell()));
		user.setUserEmail((userSvc.getUserByPK(Integer.valueOf(userID)).getUserEmail()));
		user.setUserNickname((userSvc.getUserByPK(Integer.valueOf(userID)).getUserNickname()));
		/****************************** 權限 ******************************/
		user.setBuyAuthority((userSvc.getUserByPK(Integer.valueOf(userID)).getBuyAuthority()));
		user.setSpeakAuthority((userSvc.getUserByPK(Integer.valueOf(userID)).getSpeakAuthority()));

		/*************************** 不修改區塊 ***************************/

		// 開始 update，取得回傳值 (-1為失敗)
		int updateStatus = userSvc.updateUser(user);

		// 回傳 status 參數 (-1 為更新失敗)
		String status = updateStatus == -1 ? "failed" : "success";
		data.put("status", status);
		json = gson.toJson(data);
		out.write(json);
	}

	// 密碼加密
	public static String encryptPassword(String password) {

		// 加密接收到的密碼 -> 使用 MD5 加密
		try {
			// 創建 MD5 實體
			MessageDigest md = MessageDigest.getInstance("MD5");

			// 轉換原始密碼
			byte[] bytes = md.digest(password.getBytes());

			// 將 byte[] 轉為 16 進制 String
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}

			// MD5 加密後的 Password
			password = sb.toString();
			return password;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

}
