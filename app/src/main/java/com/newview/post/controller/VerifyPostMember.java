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
import com.newview.post.service.PostService;
import com.newview.post.service.PostServiceImpl;


@WebServlet("/VerifyPostMember")
public class VerifyPostMember extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	res.setContentType("application/json");
    	res.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("verifyPostMember".equals(action)) {
        	
            // 獲取postID和當前用戶的userID
            int postID = Integer.parseInt(req.getParameter("postID"));
            String currentUserID = (String) req.getSession().getAttribute("userID");


            // 獲取文章的作者ID
            PostService postSvc = new PostServiceImpl();
            int authorID = postSvc.getAuthorIDByPostID(postID);
            // 轉換 authorID 為字串
            String authorIDString = String.valueOf(authorID);


            // 判斷用戶是否有權限
            boolean hasPermission = (currentUserID != null && currentUserID.equals(authorIDString));


            // 建立Json資料
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("hasPermission", hasPermission);
            jsonData.put("authorID", authorID);
            jsonData.put("userID", currentUserID);

            // 發送Json資料
            Gson gson = new Gson();
            String json = gson.toJson(jsonData);
            res.getWriter().write(json);
        }
    }
}

