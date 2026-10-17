package com.newview.orders.service;

import java.util.List;

import javax.persistence.Tuple;

import com.newview.orders.model.Orderlist;
import com.newview.orders.model.Orders;

public interface OrdersService2 {
	
	List<Orders> findByUserID(Integer userID);
	
	List<Tuple> findByOrderListID(Integer orderListID);
	
	boolean saveCom(Orderlist orderlist);
	
	int cancelOrders(Integer orderID);
	
	int cancelReview(Integer orderListID);
	
	
}
