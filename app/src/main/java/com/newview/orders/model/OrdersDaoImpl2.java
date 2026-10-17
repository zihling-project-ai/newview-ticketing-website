package com.newview.orders.model;

import java.util.List;

import javax.persistence.Tuple;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.util.HibernateUtil;



public class OrdersDaoImpl2 implements OrdersDao2 {
	
	@Override
	public int updateOrderlistForCom(Orderlist nOrderlist) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		var oOrderlist = session.getReference(Orderlist.class, nOrderlist.getOrderListID());
		oOrderlist.setReviewContent(nOrderlist.getReviewContent());
		oOrderlist.setFiveStarReview(nOrderlist.getFiveStarReview());
		return 1;
	}

	@Override
	public List<Orders> selectByUserID(Integer userID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			return session.createQuery("FROM Orders WHERE userID = :userID", Orders.class)
					.setParameter("userID", userID).list();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
			return null;
		}
	}
	
	@Override
	public List<Tuple> selectByOrderListIDForActCom(Integer orderListID) {
		var sql = new StringBuilder()
				.append("select ")
				.append("	o.orderID, o.userID, a.actName, ol.orderListID, ol.fiveStarReview, ol.reviewContent, c.comPicID, c.comPic ")
				.append("from ")
				.append("	orders o ")
				.append("	left join orderlist ol ")
				.append("		on o.orderID = ol.orderID ")
				.append("	left join act a ")
				.append("		on ol.actID = a.actID ")
				.append("	left join compic c ")
				.append("		on ol.orderListID = c.orderListID ")
				.append("where ")
				.append("	ol.orderListID = :orderListID");
		var session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			var tx = session.beginTransaction();
			var list = session.createNativeQuery(sql.toString(), Tuple.class)
					.setParameter("orderListID", orderListID)
					.list();
			tx.commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}
	
	@Override
	public int update(Orders Orders) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.update(Orders);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;

	}

	@Override
	public Orders findByPrimaryKey(Integer orderID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			Orders ordersHibernate = session.get(Orders.class, orderID);
			session.getTransaction().commit();
			return ordersHibernate;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}
	
	@Override
	public int cancelOrdType(Integer orderID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			Orders orders = session.get(Orders.class, orderID);
			if (orders != null) {
				orders.setOrdType(2); // 例如，将字段设置为 null
		            
		            // 保存或更新记录以保存更改
		            session.update(orders); // 如果使用 merge() 方法，请使用 session.merge(post);
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
	public int deleteReview(Integer orderListID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			Orderlist orderList = session.get(Orderlist.class, orderListID);
			if (orderList != null) {
				orderList.setReviewContent(""); // 例如，将字段设置为 null
				orderList.setFiveStarReview(null);
		            
		            // 保存或更新记录以保存更改
		            session.update(orderList); // 如果使用 merge() 方法，请使用 session.merge(post);
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
	public int updateComPic(ComPic comPic) {
		var session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.merge(comPic);
		return 1;
	}
	public static void main(String[] args) {
		OrdersDaoImpl2 dao = new OrdersDaoImpl2();
		var a = dao.deleteComPic(1);
		System.err.println(a);
	}
	public int deleteComPic(Integer orderListID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			    String hql = "DELETE FROM ComPic WHERE orderListID = :orderListID";
			    Query<ComPic> query = session.createQuery(hql, ComPic.class);
			    query.setParameter("orderListID", orderListID);
			    int deletedRecords = query.executeUpdate();
		        
			
			session.getTransaction().commit();
			return deletedRecords;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}
}
