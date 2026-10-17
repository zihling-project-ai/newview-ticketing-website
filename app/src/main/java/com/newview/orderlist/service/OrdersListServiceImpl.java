package com.newview.orderlist.service;

import java.sql.Timestamp;
import java.util.List;

import com.newview.discount.model.DiscountVO;
import com.newview.orderlist.model.OrderListDAO;
import com.newview.orderlist.model.OrderListDAOImpl;
import com.newview.orderlist.model.OrderListVO;
import com.newview.orders.model.OrdersVO;
import com.newview.publisher.model.PublisherVO;
import com.newview.user.model.UserVO;

public class OrdersListServiceImpl {

	private OrderListDAO orderListDao;

	public OrdersListServiceImpl() {
		orderListDao = new OrderListDAOImpl();
	}
	
	public void insertOrderlist(OrderListVO orderListVO){
		
		orderListDao.insert(orderListVO);
	}
	
	public void updateOrderlist(OrderListVO orderListVO) {
		
		orderListDao.update(orderListVO);
		
	}
	
	public void deleteOrderlistByOrder(Integer orderID) {
		orderListDao.delete(orderID);
	}
	
	public OrderListVO getOneOrderList(Integer orderListID) {
		return orderListDao.findeByPrimaryKey(orderListID);
	}
	
	public List<OrderListVO> getAll() {
		return orderListDao.getAll();
	}

}
