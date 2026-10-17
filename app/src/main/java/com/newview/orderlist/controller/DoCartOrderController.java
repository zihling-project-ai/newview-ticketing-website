package com.newview.orderlist.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.newview.act.model.ActVO;
import com.newview.act.service.ActService;
import com.newview.act.service.ActServiceImpl;
import com.newview.orderlist.model.OrderListVO;
import com.newview.orderlist.service.OrderListService;
import com.newview.orderlist.service.OrderListServiceImpl;
import com.newview.orders.model.OrdersVO;
import com.newview.orders.service.OrdersService;
import com.newview.orders.service.OrdersServiceImpl;
import com.newview.publisher.model.PublisherVO;
import com.newview.user.model.UserVO;

@WebServlet("/DoOrder")
public class DoCartOrderController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");

		// 先將Json資料取出並轉換成個別值
		String comfirmOrderList = req.getParameter("comfirmOrderList");
		System.out.println(comfirmOrderList);// 得出資料

		// 轉回字串
		Gson gson = new Gson();
		Type listType = new TypeToken<List<OrderItemDTO>>() {
		}.getType();
		List<OrderItemDTO> orders = gson.fromJson(comfirmOrderList, listType);

		// 搭配OrderItemDTO迴圈取值
		OrdersService orderSvc = new OrdersServiceImpl();
		OrderListService orderlistSvc = new OrderListServiceImpl();
		ActService actSvc = new ActServiceImpl();

		List<Map<String, Object>> ordersData = new ArrayList<>();
		Map<Integer, List<OrderItemDTO>> groupMap = orders.stream()
				.collect(Collectors.groupingBy(OrderItemDTO::getPubID));

		String currentUserID = (String) req.getSession().getAttribute("userID");
		int userID = Integer.parseInt(currentUserID);

		Map<OrdersVO, List<OrderListVO>> map = new LinkedHashMap<>();

		Set<Map.Entry<Integer, List<OrderItemDTO>>> entrySet = groupMap.entrySet();

		for (Map.Entry<Integer, List<OrderItemDTO>> entry : entrySet) {
			Integer pubID = entry.getKey();
			List<OrderItemDTO> list = entry.getValue();

			OrdersVO orderVO = new OrdersVO();
			UserVO userVO = new UserVO();
			PublisherVO pubVO = new PublisherVO();

			pubVO.setPubID(pubID);
			userVO.setUserID(userID);

			orderVO.setPublisherVO(pubVO);
			orderVO.setUserVO(userVO);

			List<OrderListVO> orderList = new ArrayList<>();

			int total = 0, actCountTotal = 0;
			for (OrderItemDTO dto : list) {
				int actID = dto.getActID();
				int actPrice = dto.getActPrice();
				int discount = 0;// 未修正
				int actCount = dto.getActCount();
				actCountTotal += actCount;
				int actPriceTotal = dto.getActPriceTotal();
				total += actPriceTotal;
				String seatRowsColumns = dto.getRowColumn();
				List<String> seats = splitSeatInfo(seatRowsColumns);

				for (String seat : seats) {

					OrderListVO oListVO = new OrderListVO();

					// OrderID
					oListVO.setOrdersVO(orderVO);
					// ActID
					ActVO actVO = new ActVO();
					actVO.setActID(actID);
					oListVO.setActVO(actVO);
					// 其他
					oListVO.setActTotal(actPrice);
					Timestamp orderListTime = new Timestamp(System.currentTimeMillis());
					oListVO.setOrderListTime(orderListTime);
					oListVO.setSeatRowsColumns(seat);
					oListVO.setType(2);
					String youtubeLink = "https://youtu.be/dQw4w9WgXcQ?si=3NVOtjDf3Lf9LSPW";
					byte[] newviwQRcode = generateQRCode(youtubeLink);
					oListVO.setQRcodeID(newviwQRcode);
					
					orderList.add(oListVO);
					
				}

				orderVO.setOrdTotal(total);
				orderVO.setDiscount(discount);
				int discountPrice = total - discount;
				orderVO.setDiscountPrice(discountPrice);
				orderVO.setOrdType(3);
				orderVO.setActQuantity(actCountTotal);

				Map<String, Object> jsonData = new HashMap<>(); // 建立單次迴圈的 JSON 資料
				jsonData.put("userID", userID);
				jsonData.put("actName", actSvc.findByPrimaryKey(actID).getActName());
				jsonData.put("cityAddress", actSvc.findByPrimaryKey(actID).getCityAddress());
				jsonData.put("actDate", actSvc.findByPrimaryKey(actID).getActDate());
				jsonData.put("actID", actID);
				jsonData.put("pubID", pubID);
				jsonData.put("actPrice", actPrice);
				jsonData.put("discount", discount);
				jsonData.put("actCount", actCount);
				jsonData.put("actPriceTotal", actPriceTotal);
				jsonData.put("discountPrice", discountPrice);
				jsonData.put("orderID", orderSvc.getOrderBy(userID, pubID));
				jsonData.put("seatRowsColumns", seatRowsColumns);

				ordersData.add(jsonData);

			}
			map.put(orderVO, orderList);

		}

		map.forEach((orderVO, list) -> {
			orderSvc.insertOrders(orderVO);
			list.forEach((orderListVO) -> orderlistSvc.insert(orderListVO));
		});

		System.out.print("Order and Order Detail Inserted Successfully!");
		String json = gson.toJson(ordersData);
		res.getWriter().write(json);
	}
	
	
	

	public static List<String> splitSeatInfo(String seatRowsColumns) {
		List<String> seats = new ArrayList<>();
		Pattern pattern = Pattern.compile("第.*?列");
		Matcher matcher = pattern.matcher(seatRowsColumns);

		while (matcher.find()) {
			String seat = matcher.group();
			seats.add(seat);
		}

		return seats;
	}
	
	protected byte[] generateQRCode(String youtubeLink) throws IOException {
		try {
			// 設置qrcode
			int width = 300;
			int height = 300;
			String format = "png";

			// 生成QR码
			BitMatrix bitMatrix = new MultiFormatWriter().encode(youtubeLink, BarcodeFormat.QR_CODE, width, height);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(bitMatrix, format, baos);

			// 返回改好的byte
			return baos.toByteArray();
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	}

}
