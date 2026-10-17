package com.newview.mylike.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.newview.act.model.ActVO;
import com.newview.mylike.model.MyLikeVO;
import com.newview.mylike.service.MyLikeService;
import com.newview.mylike.service.MyLikeServiceImpl;
import com.newview.user.model.UserVO;

@WebServlet("/likeChange")
public class LikeChangeController extends HttpServlet {
	 private MyLikeService myLikeService;

	    @Override
	    public void init() throws ServletException {
	        super.init();
	        myLikeService = new MyLikeServiceImpl();
	    }
	 public void doGet(HttpServletRequest req, HttpServletResponse res)
	            throws ServletException, IOException {

	        doPost(req, res);
	    }
	 
	 public void doPost(HttpServletRequest req, HttpServletResponse res)
	            throws ServletException, IOException {

	        req.setCharacterEncoding("UTF-8");
	        res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            String actIDStr = req.getParameter("actID");
//            String userIDStr = req.getParameter("userID");
            HttpSession session = req.getSession();
            String userIDStr = (String) session.getAttribute("userID");
            Integer actID = null;
            Integer userID = null;
           
            if (actIDStr != null) {
                actID = Integer.parseInt(actIDStr);
            }
            if (userIDStr != null) {
                userID = Integer.parseInt(userIDStr);
            }
            if(userIDStr != null && actIDStr != null ) {
            	//找到是否有相關資料
            	int likeOrNot = myLikeService.findMyLikeIDByUserIDAndActID(userID, actID);
            	
            	switch(likeOrNot) {
            	case 1:
            		 Integer myLikeID = myLikeService.findMyLikeIDByUserIDAndActIDforDe(userID, actID);
            		    if (myLikeID != null) {
            		        myLikeService.deleteMyLike(myLikeID);
            		    }
            		 break;
            	case 0:	            	         		
            		 myLikeService.insertMyLike(userID, actID);  
            		 break;
            	}
            	
            }else {
            	System.out.println("缺少資料");
            }
            
            
	 }
}
