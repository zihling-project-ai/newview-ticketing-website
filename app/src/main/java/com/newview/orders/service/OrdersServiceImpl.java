package com.newview.orders.service;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Session;

import com.newview.discount.model.DiscountVO;
import com.newview.orders.model.OrdersDAO;
import com.newview.orders.model.OrdersDAOImpl;
import com.newview.orders.model.OrdersVO;
import com.newview.publisher.model.PublisherVO;
import com.newview.user.model.UserVO;
import com.newview.util.HibernateUtil;

public class OrdersServiceImpl implements OrdersService {

	private OrdersDAO ordersDao;

	public OrdersServiceImpl() {
		ordersDao = new OrdersDAOImpl();
	}

	public int insertOrders(OrdersVO ordersVO) {

		return ordersDao.insert(ordersVO);
	}

	public int updateOrders(OrdersVO ordersVO) {

		return ordersDao.update(ordersVO);

	}

	public int deleteOrders(Integer orderID) {
		return ordersDao.delete(orderID);
	}

	public OrdersVO getOneOrder(Integer orderID) {
		return ordersDao.findByPrimaryKey(orderID);
	}

	public List<OrdersVO> getAll() {
		return ordersDao.getAll();
	}

	@Override
	public int deleteOrdersbyUserIDandPubID(Integer userID, Integer pubID) {
		// TODO Auto-generated method stub
		return ordersDao.deleteOrdersbyUserIDandPubID(userID, pubID);
	}

	@Override
	public Integer getOrderBy(int userID, int pubID) {
		// TODO Auto-generated method stub
		return ordersDao.getOrderBy(userID, pubID);
	}

	public OrdersVO insertOrders(Integer userID, Integer ordTotal, Integer discount, Integer discountPrice,
			Timestamp ordTime, Integer pubID, Integer ordType, Integer actQuantity, Integer discountNO) {

		OrdersVO ordersVO = new OrdersVO();

		UserVO userVO = new UserVO();
		userVO.setUserID(userID);
		ordersVO.setUserVO(userVO);

		ordersVO.setOrdTotal(ordTotal);
		ordersVO.setDiscount(discount);
		ordersVO.setDiscountPrice(discountPrice);
		ordersVO.setOrdTime(ordTime);

		PublisherVO publisherVO = new PublisherVO();
		publisherVO.setPubID(pubID);
		ordersVO.setPublisherVO(publisherVO);

		ordersVO.setOrdType(ordType);
		ordersVO.setActQuantity(actQuantity);

		DiscountVO discountVO = new DiscountVO();
		discountVO.setDiscountNO(discountNO);
		ordersVO.setDiscountVO(discountVO);

		return ordersVO;
	}

	public OrdersVO updateOrders(Integer userID, Integer ordTotal, Integer discount, Integer discountPrice,
			Timestamp ordTime, Integer pubID, Integer ordType, Integer actQuantity, Integer discountNO) {

		OrdersVO ordersVO = new OrdersVO();

		UserVO userVO = new UserVO();
		userVO.setUserID(userID);
		ordersVO.setUserVO(userVO);

		ordersVO.setOrdTotal(ordTotal);
		ordersVO.setDiscount(discount);
		ordersVO.setDiscountPrice(discountPrice);
		ordersVO.setOrdTime(ordTime);

		PublisherVO publisherVO = new PublisherVO();
		publisherVO.setPubID(pubID);
		ordersVO.setPublisherVO(publisherVO);

		ordersVO.setOrdType(ordType);
		ordersVO.setActQuantity(actQuantity);

		DiscountVO discountVO = new DiscountVO();
		discountVO.setDiscountNO(discountNO);
		ordersVO.setDiscountVO(discountVO);

		return ordersVO;

	}

	public List<UserVO> getAllUsers() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			List<UserVO> users = session.createQuery("from UserVO", UserVO.class).list();
			session.getTransaction().commit();
			return users;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
			return null;
		}
	}

	@Override // for Publisher Backstage
	public List<OrdersVO> getOrdersByPub(Integer pubID) {
		List<OrdersVO> orderList = ordersDao.getOrdersByPubID(pubID);
		return orderList;
	}

}

