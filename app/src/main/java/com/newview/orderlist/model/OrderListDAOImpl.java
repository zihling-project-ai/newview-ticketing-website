package com.newview.orderlist.model;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.util.HibernateUtil;

public class OrderListDAOImpl implements OrderListDAO {
	

	public  OrderListDAOImpl() {
	  
	}
	@Override
	public int insert(OrderListVO orderListVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.save(orderListVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int update(OrderListVO orderListVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(orderListVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int delete(Integer orderID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			OrderListVO orderList = session.get(OrderListVO.class, orderID);
			if (orderList != null) {
				session.delete(orderList);
			}
			session.getTransaction().commit();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public OrderListVO findeByPrimaryKey(Integer orderListID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			OrderListVO orderList = session.get(OrderListVO.class, orderListID);
			session.getTransaction().commit();
			return orderList;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public List<OrderListVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			List<OrderListVO> orderList = session.createQuery("from OrderListVO", OrderListVO.class).list();
			session.getTransaction().commit();
			return orderList;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	} 
	
	public Integer findUserIDByOrderIDAndActID(Integer orderID, Integer actID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();
	        String hql = "SELECT o.userVO.userID FROM OrderListVO ol " +
	                "JOIN ol.ordersVO o " +
	                "WHERE o.orderID = :orderID " +
	                "AND ol.actVO.actID = :actID";
	        		// actID, orderID 

	        Query query = session.createQuery(hql);
	        query.setParameter("orderID", orderID);
	        query.setParameter("actID", actID); 
	        
	        Integer userID = (Integer) query.uniqueResult();
	        session.getTransaction().commit();
	        
	        return userID;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return null;
	}
	public List<Integer> findOrderIDsByActID(Integer actID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    List<Integer> orderIDs = null;
	    
	    try {
	        session.beginTransaction();
	        
	        String hql = "SELECT o.orderID FROM OrderListVO ol " +
	                     "JOIN ol.ordersVO o " +
	                     "WHERE ol.actVO.actID = :actID";
	        
	        Query<Integer> query = session.createQuery(hql, Integer.class);
	        query.setParameter("actID", actID); 
	        
	        orderIDs = query.list();
	        session.getTransaction().commit();
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return orderIDs;
	}



}
