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
import com.newview.user.jedis.JedisPoolUtil;

import redis.clients.jedis.Jedis;

@WebServlet("/Verify")
public class VerifyController extends HttpServlet {

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
		
		// 從前端取得的驗證碼
		String verificationFromWeb = req.getParameter("verificationCode");
		System.out.println("verificationFromWeb: " + verificationFromWeb);
		
		// 從 redis 取得驗證碼
		Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
		jedis.select(15);
		String verificationCodeFromRedis = jedis.get("UserAccount:" + (String) session.getAttribute("account"));
		System.out.println("verificationCodeFromRedis: " + verificationCodeFromRedis);
		
		// 已驗證，redis 找不到驗證碼資料，回傳 status -> verified
		if (verificationCodeFromRedis == null) {
			System.out.println("使用者已驗證");
			data.put("status", "verified");
			json = gson.toJson(data);
			out.write(json);
			return;
		}
		
		// 驗證錯誤，回傳 status -> failed
		if(!verificationCodeFromRedis.equals(verificationFromWeb)) {
			System.out.println("驗證碼驗證錯誤");
			data.put("status", "failed");
			json = gson.toJson(data);
			out.write(json);
			return;
		}
		
		// 驗證成功，刪除 redis 中資料
		System.out.println("驗證碼驗證成功");
		// 刪除 redis 資料
		String account = (String) session.getAttribute("account");
		System.out.println("Verify session account: "+ account);
		jedis.del("UserAccount:" + session.getAttribute("account"));
		jedis.del("UserAccount:" + account);
		jedis.close();

		// 回傳 status -> success
		data.put("status", "success");
		json = gson.toJson(data);
		out.write(json);
	}
}
