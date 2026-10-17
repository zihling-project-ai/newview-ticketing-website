package com.newview.orders.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.publisher.model.PublisherVO;
import com.newview.util.HibernateUtil;

public class OrdersDAOImpl implements OrdersDAO {

	private static Timestamp now = new Timestamp(System.currentTimeMillis());

	@Override
	public int insert(OrdersVO ordersVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.saveOrUpdate(ordersVO);
//				Integer id = (Integer) session.save(ordersVO);
			session.getTransaction().commit();

		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return 1;

	}

	@Override
	public int update(OrdersVO ordersVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.update(ordersVO);
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
			OrdersVO ordersVO = session.get(OrdersVO.class, orderID);
			if (ordersVO != null) {
				session.delete(ordersVO);
			}
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;

	}

	@Override
	public OrdersVO findByPrimaryKey(Integer orderID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			OrdersVO ordersHibernateVO = session.get(OrdersVO.class, orderID);
			session.getTransaction().commit();
			return ordersHibernateVO;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;

	}

	@Override
	public List<OrdersVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			List<OrdersVO> list = session.createQuery("from OrdersVO", OrdersVO.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;

	}
	
	
	
	//for Publisher Backstage get Order List by Mandy
	public List<OrdersVO> getOrdersByPubID(Integer pubID){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			String hql = "FROM OrdersVO AS o WHERE o.publisherVO.pubID = :pubID";
			Query<OrdersVO> query = session.createQuery(hql,OrdersVO.class);
			query.setParameter("pubID",pubID);
			return query.getResultList();
		}catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}
	

	
	//for Order & OrderList by Lin
	@Override
	public int deleteOrdersbyUserIDandPubID(Integer userID, Integer pubID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();

	        // 使用原生SQL來執行刪除操作
	        String sql = "DELETE FROM Orders WHERE pubID = :pubID AND userID = :userID";
	        Query query = session.createSQLQuery(sql);
	        query.setParameter("pubID", pubID);
	        query.setParameter("userID", userID);

	        int rowsAffected = query.executeUpdate();

	        session.getTransaction().commit();
	        return rowsAffected; // 返回受影響的行數
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	        return -1; // 返回錯誤碼
	    }
	}


	//for Order & OrderList by Lin
	//for Order & OrderList by Lin
	@Override
	public Integer getOrderBy(int userID, int pubID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();

	        Query query = session.createQuery("SELECT orderVO.orderID FROM OrdersVO orderVO WHERE orderVO.userVO.userID = :userID AND orderVO.publisherVO.pubID = :pubID", Integer.class);
	        query.setParameter("userID", userID);
	        query.setParameter("pubID", pubID);

	        List<Integer> orderIDs = query.list();

	        session.getTransaction().commit();

	        if (orderIDs.isEmpty()) {
	            return null; 
	        } else {
	    
	            return orderIDs.get(0);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return null; 
	}





}
