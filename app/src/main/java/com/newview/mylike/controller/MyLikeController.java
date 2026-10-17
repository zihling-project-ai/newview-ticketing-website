package com.newview.mylike.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.newview.mylike.service.MyLikeService;
import com.newview.mylike.service.MyLikeServiceImpl;

@WebServlet("/LikuSoDesu")
public class MyLikeController extends HttpServlet {
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
        String action = req.getParameter("action");        
        String actIDstr = req.getParameter("actID");  
        Integer  actID = null;
        if(actIDstr != null && !actIDstr.isEmpty()) {
        	 actID = Integer.parseInt(actIDstr);
            }
        Integer userID = null ;
        String userIDstr = req.getParameter("userID");
        if(userIDstr != null && !userIDstr.isEmpty()) {
        userID = Integer.parseInt(userIDstr);
        }
       
        if ("desu".equals(action) ) {
        	List<Map<String, Integer>> myLikeIDsWithActID =  myLikeService.findMyLikeIDsByUserID(userID); 
        	for (Map<String, Integer> entry : myLikeIDsWithActID) {
        	    Integer myLikeID = entry.get("myLikeID");
        	    Integer actIDIn = entry.get("actID");
        	   
        	}
            JsonObject jsonResponse = new JsonObject();                    
            Gson gson = new Gson();
            JsonArray likeArray = gson.toJsonTree(myLikeIDsWithActID).getAsJsonArray();
            
            jsonResponse.add("likeIDs", likeArray);
            
           
            res.getWriter().write(new Gson().toJson(jsonResponse));
        }
    }
//    
}

