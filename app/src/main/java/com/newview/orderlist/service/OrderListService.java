package com.newview.orderlist.service;

import java.util.List;

import com.newview.orderlist.model.OrderListVO;

public interface OrderListService {
	public int insert(OrderListVO orderListVO);
	public int update(OrderListVO orderListVO);
	public int delete(Integer orderListID);
	public OrderListVO findeByPrimaryKey(Integer orderListID);
	public List<OrderListVO> getAll();
	Integer findUserIDByOrderIDAndActID(Integer orderID, Integer actID);
	List<Integer> findOrderIDsByActID(Integer actID);
}
