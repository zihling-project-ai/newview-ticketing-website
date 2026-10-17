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
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

import redis.clients.jedis.Jedis;

@WebServlet("/VerifyPassword")
public class VerifyPasswordController extends HttpServlet{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		HttpSession session = req.getSession();
		Gson gson = new Gson();
		String json = null;
		HashMap<String, String> data = new HashMap<>();
		String hashPassword = null;
		
		// 取得前端傳遞 email、驗證碼參數
		String email = req.getParameter("email");
		String verificationCodeFromWeb = req.getParameter("verificationCode");
		
		// 從 redis 取得驗證碼 (1) 、新密碼
		Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
		jedis.select(15);
		// 驗證碼 (1)
		String verificationCodeFromRedis = jedis.get("UserEmail_Verify:" + email);
		// 新密碼 (2)
		String newPasswordFromRedis = jedis.get("UserEmail_NewPassword:" + email);
		
		// 驗證碼失敗，請重新輸入
		if(!verificationCodeFromRedis.equals(verificationCodeFromWeb)) {
			data.put("status", "failed");
			json = gson.toJson(data);
			out.write(json);
			return;
		}
		
		// 驗證成功，告知使用者密碼已重設
		jedis.del("UserEmail_Verify:" + email);  // 刪除驗證碼
		jedis.del("UserEmail_NewPassword:" + email);  // 刪除新密碼
		
		// 關閉 redis 連線池
		jedis.close();
		
		// 取得 userVO，更新資料
		UserService userSvc = new UserServiceImpl();
		Integer userID = userSvc.getUserByEmail(email).getUserID();
		UserVO userVO = userSvc.getUserByPK(userID);

		/*************************** 不修改區塊 ***************************/
		// 取得 user 中資料者註冊資訊，取代原先資訊(不更新)
		userVO.setUserID(userVO.getUserID());
		userVO.setUserName(userVO.getUserName());
		userVO.setUserAccount(userVO.getUserAccount());
		userVO.setUserBirth(userVO.getUserBirth());
		userVO.setUserCell(userVO.getUserCell());
		userVO.setUserEmail(userVO.getUserEmail());
		userVO.setUserNickname(userVO.getUserNickname());
		userVO.setBuyAuthority(userVO.getBuyAuthority());
		userVO.setSpeakAuthority(userVO.getSpeakAuthority());
		/*************************** 不修改區塊 ***************************/

		// 加密 newPassword
		// 加密密碼 -> MD5
		try {
			// 創建 MD5 實體
			MessageDigest md = MessageDigest.getInstance("MD5");

			// 轉換原始密碼
			byte[] bytes = md.digest(newPasswordFromRedis.getBytes());

			// 將 byte[] 轉為 16 進制 String
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}

			// MD5 加密後的 Password
			hashPassword = sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// 更新 hashPawwrod 到資料庫中
		userVO.setUserPassword(hashPassword);

		// 更新使用者資訊
		userSvc.updateUser(userVO);
		System.out.println("密碼重設完成");

		// 比對成功，寄驗證信給前端傳來之 email + 驗證信 + 驗證碼 + 新密碼 (亂數8碼)，回傳 status -> success
		data.put("status", "success");
		json = gson.toJson(data);
		out.write(json);
	}
}
