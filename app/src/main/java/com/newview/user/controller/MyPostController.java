package com.newview.user.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.newview.post.model.PostVO;
import com.newview.user.dto.MyPostDTO;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserServiceImpl;

@WebServlet("/MyPost")
public class MyPostController extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		Gson gson = new Gson();
		HashMap<String, String> data = new HashMap<>();
		HttpSession session = req.getSession();
		String json = null;

		String userID = (String) session.getAttribute("userID");

		// 透過 userID 查詢資料
		UserVO userVO = new UserServiceImpl().getUserByPK(Integer.valueOf(userID));
		
		Set<PostVO> postVOs = userVO.getPostVOs();
		
		// 判斷 postVOs 是否為空值
		if(postVOs == null) {
			return;
		}

		// 有資料，開始查詢資料
		// 將使用者的 postVOs 透過 stream 轉成 MyPostDTO 型態的 List
		List<MyPostDTO> myPostList = postVOs.stream()
				.sorted((o1, o2) -> {
					Timestamp t1 = Timestamp.valueOf(o1.getPostDateTime().toString());
					Timestamp t2 = Timestamp.valueOf(o2.getLikeCount().toString());
					return t1.compareTo(t2) * -1;
				})
				.map(MyPostDTO::new)
				.collect(Collectors.toList());

		// 打包回傳 ajax
		json = gson.toJson(myPostList);
		out.write(json);
	}
}
