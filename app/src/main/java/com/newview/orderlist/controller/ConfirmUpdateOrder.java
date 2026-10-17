package com.newview.orderlist.controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newview.act.model.ActVO;
import com.newview.act.service.ActService;
import com.newview.act.service.ActServiceImpl;
import com.newview.orders.model.OrdersVO;
import com.newview.orders.service.OrdersService;
import com.newview.orders.service.OrdersServiceImpl;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.user.model.UserVO;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

@WebServlet("/ConfirmUpdateOrder")
public class ConfirmUpdateOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");

		
		// 1. 先將Json資料取出並轉換成個別值
		String orderData = req.getParameter("orderData");
		String discountParam = req.getParameter("discount");
		Integer discount = (discountParam == null || discountParam.isEmpty()) ? 0 : Integer.valueOf(discountParam);
		Integer discountprice = Integer.valueOf(req.getParameter("discountprice"));
		Integer recipientuserID = Integer.valueOf(req.getParameter("recipientuserID"));

		// 2. 轉回字串
		Gson gson = new Gson();
		Type listType = new TypeToken<List<OrderConfirmDTO>>() {
		}.getType();
		List<OrderConfirmDTO> orders = gson.fromJson(orderData, listType);

		// 3. 將訂單更新(Update)
		OrdersService orderSvc = new OrdersServiceImpl();
		String currentUserID = (String) req.getSession().getAttribute("userID");
		int userID = Integer.parseInt(currentUserID);

		for (OrderConfirmDTO order : orders) {
			// 4. 修改訂單狀態開始
			// 獲取訂單訊息
			int actID = order.getActID();
			int pubID = order.getPubID();
			int actPrice = order.getActPrice();
			int actCount = order.getActCount();
			int actPriceTotal = order.getActPriceTotal();
			String seatRowsColumns = order.getSeatRowsColumns();
			System.out.println(actID+","+pubID);
			
			// 取得訂單ID
			Integer OrderID = orderSvc.getOrderBy(userID, pubID);
			UserVO userVO = new UserVO();
			OrdersVO ordVO = orderSvc.getOneOrder(OrderID);
			System.out.println(ordVO);
			// 設定新的使用者ID(如果有)、折扣、折扣價格、訂單狀態(0)
			ordVO.setUserVO(userVO);
			userVO.setUserID(recipientuserID);
			ordVO.setDiscount(discount);
			Timestamp orderListTime = new Timestamp(System.currentTimeMillis());
			ordVO.setOrdTime(orderListTime);
			ordVO.setOrdType(0);
			
			// 呼叫Service修改VO
			orderSvc.updateOrders(ordVO);

			// 5. 刪除Redis資料
			String actIDRedis = String.valueOf(actID);
			System.out.println(actIDRedis);
			ActService actSvc = new ActServiceImpl();
			ActVO actVO = actSvc.findByPrimaryKey(actID);
			String scope = String.valueOf(actVO.getActScope());
			
			List<Integer> seatNumbers = getSeatNumbers(seatRowsColumns, scope);
			Jedis jedis = null;
			for (Integer seatNumber : seatNumbers) {
			    String seatDataKey = "seatData:" + actIDRedis;
			    try {
			    	jedis = JedisPoolUtil.getJedisPool().getResource();
			        jedis.select(4);
			        jedis.hdel(seatDataKey, String.valueOf(seatNumber)); // 使用 seatNumber 作為欄位名稱刪除對應欄位
			    } catch (JedisDataException jde) {
			        System.err.println("Redis删除异常: " + jde.getMessage());
			    }
			}

		}

		res.getWriter().write(orderData);
	}

	public static List<Integer> getSeatNumbers(String seatRowsColumns, String scope) {
	    List<Integer> seatNumbers = new ArrayList<>(); // 初始化用于存储座位号码的 List

	    if (seatRowsColumns != null && !seatRowsColumns.isEmpty()) {
	        // 只保留字符串中的数字
	        String cleanedString = seatRowsColumns.replaceAll("\\D", " ");

	        String[] numbers = cleanedString.trim().split("\\s+");

	        for (int i = 0; i < numbers.length; i += 2) {
	            int row = Integer.parseInt(numbers[i]);
	            int column = Integer.parseInt(numbers[i + 1]);
	            int seatNumber = calculateSeatNumber(row, column, scope);
	            System.out.println("Row" + ((i / 2) + 1) + " = " + row + " Column" + ((i / 2) + 1) + " = " + column);
	            System.out.println("SeatNumber" + ((i / 2) + 1) + " = " + seatNumber);
	            seatNumbers.add(seatNumber); // 将每个座位的编号添加到 List 中
	        }
	    }
	    return seatNumbers;
	}

	public static int calculateSeatNumber(int row, int column, String scope) {
	    int seatNumber = -1;

	    switch (scope) {
	        case "1":
	            seatNumber = (row - 1) * 20 + column;
	            break;
	        case "2":
	            seatNumber = (row - 1) * 20 + column;
	            break;
	        case "3":
	            seatNumber = (row - 1) * 30 + column;
	            break;
	        default:
	            // 处理其他情况
	            break;
	    }
	    return seatNumber;
	}


}
