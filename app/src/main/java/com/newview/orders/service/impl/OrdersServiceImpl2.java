package com.newview.orders.service.impl;

import java.util.Base64;
import java.util.List;

import javax.persistence.Tuple;

import com.newview.orders.model.ComPic;
import com.newview.orders.model.Orderlist;
import com.newview.orders.model.Orders;
import com.newview.orders.model.OrdersDao2;
import com.newview.orders.model.OrdersDaoImpl2;
import com.newview.orders.service.OrdersService2;
import com.newview.util.HibernateUtil;

public class OrdersServiceImpl2 implements OrdersService2 {
	private OrdersDao2 dao;
	
	public OrdersServiceImpl2() {
		dao = new OrdersDaoImpl2();
	}

	@Override
	public List<Orders> findByUserID(Integer userID) {
		return dao.selectByUserID(userID);
	}
	
	@Override
	public List<Tuple> findByOrderListID(Integer orderListID) {
		return dao.selectByOrderListIDForActCom(orderListID);
	}
	
	@Override
	public boolean saveCom(Orderlist orderlist) {
		if (orderlist == null || orderlist.getOrderListID() == null) {
			return false;
		}
		var session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			var tx = session.beginTransaction();
			
			if (dao.updateOrderlistForCom(orderlist) != 1) {
				throw new RuntimeException("update Orderlist fail");
			}
			
			var comPics = orderlist.getComPics();
			if (comPics != null && !comPics.isEmpty()) {
				for (ComPic comPic : comPics) {
					var base64 = comPic.getComPicBase64();
					if (base64 == null) {
						continue;
					}
					var bytes = Base64.getDecoder().decode(base64);
					comPic.setComPic(bytes);
					if (dao.updateComPic(comPic) != 1) {
						throw new RuntimeException("update ComPic fail");
					}
				}
			}
			tx.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
			return false;
		}
	}
	
	public int cancelOrders(Integer orderID) {			
		return dao.cancelOrdType(orderID);
		
	}
	
	public int cancelReview(Integer orderListID) {
		dao.deleteComPic(orderListID);
		return dao.deleteReview(orderListID);
		
	}
}
