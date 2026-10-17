package com.newview.cartact.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.act.service.ActService;
import com.newview.act.service.ActServiceImpl;
import com.newview.user.jedis.JedisPoolUtil;

import redis.clients.jedis.Jedis;

@WebServlet("/getcartorder")
public class GetCartOrderController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");

		// 1. 獲取用戶訊息(這裡有問題，直接購買會有user session，但如果先加購物車後登入?但我覺得只要選座位那邊就開始下filter就沒這問題)
//		HttpSession session = req.getSession();
//		String userIDString = (String) req.getSession().getAttribute("userID");
//		Integer userID = Integer.parseInt(userIDString);

		// 2. 連接 Redis
		Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
		jedis.select(4); // 選擇 Redis db(4)

		// 3.藉由獲取RedisHash，裁切資料後得到actID、userID、座位狀態，以及座位行列資訊，其中座位行列要先透過ActVO得到Scope資料，才能知道座位數量，進一步轉換為行列資訊
		// 3-1. 先得到所有Hash資料
		Set<String> keys = jedis.keys("seatData:*");
		List<CartOrderDTO> dtoList = new ArrayList<>();
		Comparator<String> rowComparator = Comparator.naturalOrder();
		for (String key : keys) {

			// 3-2. 再得到單一Hash資訊
			Map<String, String> hashData = jedis.hgetAll(key);
			List<String> rowList = new ArrayList<>();

			// 6-1. 此處先宣告區域變數，以供DTO使用
			int actID = 0, scope = 0, pubID = 0, actPrice = 0, actCount = 0, actPriceTotal = 0;
			String pubName = null, actName = null, seatStatus = null;

			for (Map.Entry<String, String> entry : hashData.entrySet()) {
				String SeatDatakey = entry.getKey();
				String SeatDatavalue = entry.getValue();

				// 3-3. 再裁切取值
				String[] Seatkey = SeatDatakey.split(",");
				int seatNumber = Integer.parseInt(Seatkey[0]); // 使用 parseInt 來轉換
				String[] Seatvalue = SeatDatavalue.split(",");
				int cartUID = Integer.parseInt(Seatvalue[0]); // 使用 parseInt 來轉換
				actID = Integer.parseInt(Seatvalue[1]); // 使用 parseInt 來轉換
				seatStatus = Seatvalue[2];

				// 4. 開始處理座位資料
				// 4-1. 先獲取Scope資料，呼叫ActService
				ActService actService = new ActServiceImpl();
				scope = actService.findByPrimaryKey(actID).getActScope();

				// 4-2. 使用TurnSeatNumberToRowColumn方法將座位編號轉換座位行列
				String rowColumn = TurnSeatNumberToRowColumn(seatNumber, scope);
				rowList.add(rowColumn);
				rowList.sort(rowComparator);

				// 5. 取得其他購物車資訊(活動名稱、活動單價、廠商ID、廠商名稱等)
				// 5-1. 基本資料處理
				pubID = actService.findByPrimaryKey(actID).getPublisherVO().getPubID();
				pubName = actService.findByPrimaryKey(actID).getPublisherVO().getPubName();
				actName = actService.findByPrimaryKey(actID).getActName();
				actPrice = actService.findByPrimaryKey(actID).getActPrice();
				actCount = rowList.size();
				actPriceTotal = actCount * actPrice;

			}

			// 6-2. 再次迴圈中使用DTO再新增RowColumn到rowList中，以防止資料重複
			CartOrderDTO dto = new CartOrderDTO(pubID, pubName, actID, scope, actName, actPrice, actCount,
					actPriceTotal, seatStatus);
			dto.setRowColumn(rowList);
			dtoList.add(dto);

		}// 6-3. 以Map+Lambda語法搭配Stream將資料處理的更俐落，以利再發送到前端之前資料量不會過大

		Map<Integer, List<CartOrderDTO>> map = dtoList.stream().collect(Collectors.groupingBy(CartOrderDTO::getPubID));
		map.forEach((k, v) -> {
			System.out.println(k);
			v.forEach(System.out::println);
		});
		
		// 7. 以Json資料型態回到前端
		Gson gson = new Gson();
		String jsonData = gson.toJson(map);
		res.getWriter().write(jsonData);

	}

	// 4-3. 同4-2.方法用於轉換座位編號為行列，需要提供 scope 參數
	public static String TurnSeatNumberToRowColumn(int seatNumber, int scope) {

		int rowIndex = 0;
		int seatIndex = 0;

		switch (scope) {
		case 1:
			rowIndex = (int) Math.ceil((double) seatNumber / 10);
			seatIndex = seatNumber % 10 == 0 ? 10 : seatNumber % 10;
			break;
		case 2:
			rowIndex = (int) Math.ceil((double) seatNumber / 20);
			seatIndex = seatNumber % 20 == 0 ? 20 : seatNumber % 20;
			break;
		case 3:
			rowIndex = (int) Math.ceil((double) seatNumber / 30);
			seatIndex = seatNumber % 30 == 0 ? 30 : seatNumber % 30;
			break;
		}

		return "第 " + rowIndex + "排" + "," + "第 " + seatIndex + "列";
	}
}
