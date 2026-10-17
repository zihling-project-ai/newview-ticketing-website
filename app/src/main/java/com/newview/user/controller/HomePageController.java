package com.newview.user.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.act.model.ActVO;
import com.newview.act.service.ActService;
import com.newview.act.service.ActServiceImpl;
import com.newview.post.model.PostVO;
import com.newview.post.service.PostService;
import com.newview.post.service.PostServiceImpl;
import com.newview.user.dto.HotActDTO;
import com.newview.user.dto.HotPostDTO;

@WebServlet("/Home")
public class HomePageController extends HttpServlet{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		Gson gson = new Gson();
		PrintWriter out = res.getWriter();
		String json = null;
		String action = req.getParameter("action");

		// 分開渲染 1. 熱門活動: Act
		if(action.equals("Act")) {
			ActService actSvc = new ActServiceImpl();
			List<ActVO> actVOs = actSvc.getAll();
			List<HotActDTO> homeList= actVOs.stream()
					.limit(4)
					.map(a -> new HotActDTO(a))
					.collect(Collectors.toList());
			
			json = gson.toJson(homeList);
			out.write(json);
			return;
		}
		
		// 分開渲染 2. 熱門討論文章: HotPosts
		if(action.equals("HotPosts")) {
			PostService postSvc = new PostServiceImpl();
			List<PostVO> postVOs =  postSvc.getAll();
			
			Collections.sort(postVOs, (o1, o2) -> {
				Integer i1 = Integer.valueOf(o1.getLikeCount() + o1.getDisLikeCount());
				Integer i2 = Integer.valueOf(o2.getLikeCount() + o2.getDisLikeCount());
				return i1.compareTo(i2) * -1;
			});
			
			List<HotPostDTO> hotPostList = postVOs.stream()
					.limit(3)
					.map(a -> new HotPostDTO(a))
					.collect(Collectors.toList());
			
			json = gson.toJson(hotPostList);
			out.write(json);
			return;
		}
	}
}
