package com.newview.orders.service;

import java.sql.Timestamp;
import java.util.List;

import com.newview.orders.model.OrdersVO;
import com.newview.user.model.UserVO;

public interface OrdersService {

	public int insertOrders(OrdersVO ordersVO);

	public int updateOrders(OrdersVO ordersVO);

	public int deleteOrders(Integer orderID);

	public OrdersVO getOneOrder(Integer orderID);

	List<OrdersVO> getAll();

	int deleteOrdersbyUserIDandPubID(Integer userID, Integer pubID);

	public Integer getOrderBy(int userID, int pubID);


	public OrdersVO insertOrders(Integer userID, Integer ordTotal, Integer discount, Integer discountPrice,
			Timestamp ordTime, Integer pubID, Integer ordType, Integer actQuantity, Integer discountNO);

	public OrdersVO updateOrders(Integer userID, Integer ordTotal, Integer discount, Integer discountPrice,
			Timestamp ordTime, Integer pubID, Integer ordType, Integer actQuantity, Integer discountNO);

	public List<UserVO> getAllUsers();
	
	// for Publisher Backstage
	public List<OrdersVO> getOrdersByPub(Integer pubID);

}