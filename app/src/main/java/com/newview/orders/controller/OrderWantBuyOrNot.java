package com.newview.orders.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.newview.act.controller.ActWithPicsDTO;
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
import com.newview.orders.model.OrdersVO;
import com.newview.publisher.model.PublisherVO;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.websocketchat.service.RedisService;
import com.newview.websocketchat.service.RedisServiceImpl;

import redis.clients.jedis.JedisPool;

@WebServlet("/Makabaka")
public class OrderWantBuyOrNot extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private RedisService redisService;    
	private ActService actService;
	private ActPicService actPicService;
	private ActUpdateService actUpdateService;

	public OrderWantBuyOrNot() {
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        Integer userID = null;
        Integer actID = null;
        String userIDstr = (String) session.getAttribute("userID");
        String actIDstr = (String) session.getAttribute("actID");

        try {
            if (userIDstr != null) {
                userID = Integer.parseInt(userIDstr);
            }
            if (actIDstr != null) {
                actID = Integer.parseInt(actIDstr);
            }
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
            response.getWriter().write("{\"error\": \"Invalid number format\"}");
            return;
        }      
        // 如果 userID 和 actID 都不為 null，則進行查詢
        if (userID != null && actID != null) {
            Map<String, String> result = redisService.findSeatsByActIDAndUserName(actIDstr, userIDstr);

            // 獲得活動所有相關訊息
            ActWithPicsDTO actWithPicsDTO = actService.getActWithPicturesById(actID);
                      
            // 收集所有的座位
            List<String> allSeatNumbers = new ArrayList<>(result.keySet());
            int totalSeatsReceived = allSeatNumbers.size();
            
            // 將所有訊息一起丟給前端
            Gson gson = new Gson();
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", "success");
            responseJson.add("actWithPics", gson.toJsonTree(actWithPicsDTO));
            responseJson.add("allSeatNumbers", gson.toJsonTree(allSeatNumbers));  
            responseJson.addProperty("totalSeatsReceived", totalSeatsReceived);  
            responseJson.addProperty("userID", userID); 
            String responseString = gson.toJson(responseJson);
            response.getWriter().write(responseString);
        } else {
            System.out.println("沒找到");
            response.getWriter().write("{\"status\": \"not found\"}");
        }
    }
}