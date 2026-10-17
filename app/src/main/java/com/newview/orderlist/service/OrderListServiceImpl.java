package com.newview.orderlist.service;

import java.util.List;

import com.newview.orderlist.model.OrderListDAO;
import com.newview.orderlist.model.OrderListDAOImpl;
import com.newview.orderlist.model.OrderListVO;

public class OrderListServiceImpl implements OrderListService{
	  private OrderListDAO orderListDAO;

	  public OrderListServiceImpl() {
		    this.orderListDAO = new OrderListDAOImpl();
		}

	@Override
	public int insert(OrderListVO orderListVO) {
		// TODO Auto-generated method stub
		return orderListDAO.insert(orderListVO);
	}

	@Override
	public int update(OrderListVO orderListVO) {
		// TODO Auto-generated method stub
		return  orderListDAO.update(orderListVO);
	}

	@Override
	public int delete(Integer orderListID) {
		// TODO Auto-generated method stub
		return orderListDAO.delete(orderListID);
	}

	@Override
	public OrderListVO findeByPrimaryKey(Integer orderListID) {
		// TODO Auto-generated method stub
		return orderListDAO.findeByPrimaryKey(orderListID);
	}

	@Override
	public List<OrderListVO> getAll() {
		// TODO Auto-generated method stub
		return orderListDAO.getAll();
	}
	@Override
	public Integer findUserIDByOrderIDAndActID(Integer orderID, Integer actID) {
		// TODO Auto-generated method stub
		return orderListDAO.findUserIDByOrderIDAndActID(orderID,actID);
	}

	@Override
	public List<Integer> findOrderIDsByActID(Integer actID) {
		// TODO Auto-generated method stub
		return orderListDAO.findOrderIDsByActID(actID);
	}
 
}
