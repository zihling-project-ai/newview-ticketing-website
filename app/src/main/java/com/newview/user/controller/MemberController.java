package com.newview.user.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.mylike.model.MyLikeVO;
import com.newview.orders.model.OrdersVO;
import com.newview.user.dto.MyLikeActDTO;
import com.newview.user.dto.OrderDTO;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

import redis.clients.jedis.Jedis;

@WebServlet("/MemberPage")
public class MemberController extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		HashMap<String, Object> data = new HashMap<>();
		PrintWriter out = res.getWriter();

		// 取得 session 中的 userID，載入以下資訊
		String userID = (String) req.getSession().getAttribute("userID");
		System.out.println("session中的userID: " + userID);

		// 透過 userID 查詢資料
		UserService userSvc = new UserServiceImpl();
		UserVO userVO = userSvc.getUserByPK(Integer.valueOf(userID));
		OrdersVO ordersVO = userSvc.getOrderByUserID(Integer.valueOf(userID));

		// 回傳 status -> hasNoOrders '預設'沒訂單
		data.put("status", "hasNoOrders");
		if (ordersVO != null) {
			OrderDTO orderDTO = new OrderDTO(Integer.valueOf(userID));
			data.put("orders", orderDTO);

			// 回傳 status -> hasOrders，'覆蓋'原先無訂單的 status
			data.put("status", "hasOrders");
		}

		// 取得我的最愛資料
		Set<MyLikeVO> myLikeVOs = userVO.getMyLikeVOs();
		
		List<MyLikeActDTO> myLikeActList = myLikeVOs.stream()
				.map(a -> new MyLikeActDTO(a))
				.collect(Collectors.toList());
		
		data.put("mylike", myLikeActList);

		// 檢查啟用狀態 -> 從 redis 取得驗證碼
		Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
		jedis.select(15);
		String activate = jedis.get("UserAccount:" + userVO.getUserName()) == null ? "已啟用" : "未啟用";
		jedis.close();

		// 取得會員資料
		String name = userVO.getUserName();
		String nickname = userVO.getUserNickname();
		String email = userVO.getUserEmail();
		String birthdate = userVO.getUserBirth().toString();
		String cellphone = userVO.getUserCell();

		// 加入 map 中，使用 json 傳遞物件
		data.put("userID", userID);
		data.put("name", name);
		data.put("nickname", nickname);
		data.put("email", email);
		data.put("birthdate", birthdate);
		data.put("cellphone", cellphone);
		data.put("activate", activate);

//		System.out.println(data);

		// 使用 out.write() 傳遞 json 格式資料
		String json = gson.toJson(data);
		out.write(json);
		out.flush();

		System.out.println("成功回應 ajax");
	}
}
