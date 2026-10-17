package com.newview.orders.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.newview.act.model.ActDAO;
import com.newview.act.model.ActDAOHibernateImpl;
import com.newview.act.service.ActPicService;
import com.newview.act.service.ActPicServiceImpl;
import com.newview.act.service.ActService;
import com.newview.act.service.ActServiceImpl;
import com.newview.act.service.ActUpdateService;
import com.newview.act.service.ActUpdateServiceImpl;
import com.newview.actpic.model.ActPicDAO;
import com.newview.actpic.model.ActPicDAOHibernateImpl;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.websocketchat.service.RedisService;
import com.newview.websocketchat.service.RedisServiceImpl;

import redis.clients.jedis.JedisPool;

@WebServlet("/WantToBuy")
public class OrderReallyBuy extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RedisService redisService;
    private ActService actService;
    private ActPicService actPicService;
    private ActUpdateService actUpdateService;

    public OrderReallyBuy() {
        super();
        ActDAO actDAO = new ActDAOHibernateImpl();
        ActPicDAO actPicDAO = new ActPicDAOHibernateImpl();
        actService = new ActServiceImpl(actDAO);
        actPicService = new ActPicServiceImpl(actPicDAO);
        actUpdateService = new ActUpdateServiceImpl(actDAO, actPicDAO);
    }

    @Override
    public void init() throws ServletException {
        JedisPool jedisPool = JedisPoolUtil.getJedisPool();
        this.redisService = new RedisServiceImpl(jedisPool);
        ActDAO actDAO = new ActDAOHibernateImpl();
        this.actService = new ActServiceImpl(actDAO);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    } 

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");

        String action = req.getParameter("action");
        String userIDstr = (String) req.getSession().getAttribute("userID");
        String actIDstr =(String) req.getSession().getAttribute("actID");
        Integer userID;
        Integer actID;
        List<JsonObject> cartDataList = new ArrayList<>();
        Map<String, String> seatNumber;
        
        try {
            userID = Integer.parseInt(userIDstr);
            actID = Integer.parseInt(actIDstr);
            seatNumber = redisService.findSeatsNumberByActIDAndUserName(actIDstr, userIDstr);
            //設定要購買的資料
            System.out.println("執行購買任務");
            for (Map.Entry<String, String> entry : seatNumber.entrySet()) {
                String cartKey = "cart:" + userID + ":" + actID + ":" + entry.getKey()+":NotReallyBuy";
                String cartData = redisService.getCartDataFromRedis(cartKey);
                redisService.updateSoldOutToSoldOutReally(actIDstr, userIDstr);
                if (cartData != null ) {
                	cartData = cartData.replace("inCart", String.valueOf(userID));
                    cartData = cartData.replace(":NotReallyBuy", "buy");
                    Gson gson = new Gson();
                    JsonObject cartJsonObject = gson.fromJson(cartData, JsonObject.class);
                    
                    cartDataList.add(cartJsonObject);
                }
                
                if (cartData == null) {
                    res.getWriter().write("{\"error\":\"Cart not found.\"}");
                } else {
                    res.getWriter().write(cartData);
                }
            }
        } catch (NumberFormatException e) {
            res.getWriter().write("{\"error\":\"Invalid userID or actID.\"}");
            return;
        }

//        
//        if ("buy".equals(action)) {
//            System.out.println("購買中   "+seatNumber);
//        }
//        System.out.println("cartDataList: " + cartDataList);
        // 将 cartDataList 打包成 JSON 
        JsonArray jsonArray = new JsonArray();
        for (JsonObject cartData : cartDataList) {
            jsonArray.add(cartData);
        }   
        req.setAttribute("cartData", jsonArray.toString());      
        RequestDispatcher dispatcher = req.getRequestDispatcher("/SeatOrderList");
        dispatcher.forward(req, res);
    }
}
