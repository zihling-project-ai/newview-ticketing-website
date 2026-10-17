package com.newview.user.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.newview.orders.model.OrdersVO;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserServiceImpl;

public class OrderDTO {
	
	List<String> publisher = new ArrayList<>();
	
	List<Timestamp> ordTime = new ArrayList<>();
	
	List<Integer> ordTotal = new ArrayList<>();

	public OrderDTO(Integer userID) {
		UserVO userVO = new UserServiceImpl().getUserByPK(userID);
		Set<OrdersVO> ordersVOs = userVO.getOrdersVOs();
		
		for(OrdersVO ordersVO : ordersVOs) {
			this.publisher.add(ordersVO.getPublisherVO().getPubName());
			this.ordTime.add(ordersVO.getOrdTime());
			this.ordTotal.add(ordersVO.getOrdTotal());
		}
	}

	public OrderDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "OrderDTO [publisher=" + publisher + ", ordTime=" + ordTime + ", ordTotal=" + ordTotal + "]";
	}
}
