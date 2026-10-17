package com.newview.usediscount.model;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.util.HibernateUtil;

public class UseDiscountDAOImpl implements UseDiscountDAO {

	@Override
	public int insert(UseDiscountVO useDiscountVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(useDiscountVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int update(UseDiscountVO useDiscountVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(useDiscountVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int delete(Integer useDisID) {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			UseDiscountVO useDiscount = session.get(UseDiscountVO.class, useDisID);
			if (useDiscount != null) {
				session.delete(useDiscount);
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
	public UseDiscountVO findeByPrimaryKey(Integer useDisID) {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		
		try {
			session.beginTransaction();
			UseDiscountVO usediscount = session.get(UseDiscountVO.class, useDisID);
			session.getTransaction().commit();
			return usediscount;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public List<UseDiscountVO> getAll() {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		
		try {
			session.beginTransaction();
			List<UseDiscountVO> useDiscount = session.createQuery("from UseDiscountVO", UseDiscountVO.class).list();
			session.getTransaction().commit();
			return useDiscount;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	

	// for Order & OrderList By Lin
	@Override
	public int getUseDisIDBy(int discountNO, int userID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    Integer useDisID = null;
	    try {
	        session.beginTransaction();
	        Query<Integer> query = session.createQuery(
	            "SELECT ud.useDisID FROM UseDiscountVO ud WHERE ud.discountVO.discountNO = :discountNO AND ud.userVO.userID = :userID", Integer.class
	        );
	        query.setParameter("discountNO", discountNO);
	        query.setParameter("userID", userID);
	        useDisID = query.uniqueResult();
	        session.getTransaction().commit();
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return useDisID;
	}

	// for Order & OrderList By Lin
	@Override
	public UseDiscountVO getUseDiscountByUserID(int userID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    UseDiscountVO useDiscount = null;
	    try {
	        session.beginTransaction();
	        Query<UseDiscountVO> query = session.createQuery(
	            "FROM UseDiscountVO ud WHERE ud.userVO.userID = :userID", UseDiscountVO.class
	        );
	        query.setParameter("userID", userID);
	        useDiscount = query.uniqueResult();
	        session.getTransaction().commit();
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return useDiscount;
	}


}
