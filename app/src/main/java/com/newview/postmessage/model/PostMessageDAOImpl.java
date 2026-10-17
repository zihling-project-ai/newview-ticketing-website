package com.newview.postmessage.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.newview.util.HibernateUtil;
import com.newview.util.Util;

public class PostMessageDAOImpl implements PostMessageDAO {

	@Override
	public int insert(PostMessageVO postMessageVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.saveOrUpdate(postMessageVO);
			session.getTransaction().commit();
            return 1;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int update(PostMessageVO postMessageVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(postMessageVO);
			session.getTransaction().commit();
            return 1;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int delete(Integer postMessageID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        int i=0;
		try {
			session.beginTransaction();
			Query query = session.createNativeQuery("delete from postmessage where postMessageID=:id").setParameter("id",postMessageID);
//			PostMessageVO postMessage = session.get(PostMessageVO.class, postMessageID);
//			if (postMessage != null) {
//				session.delete(postMessage);
//			}
			i=query.executeUpdate();
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return i;
	}

	@Override
	public PostMessageVO findByPrimaryKey(Integer postMessageID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			PostMessageVO postMessage = session.get(PostMessageVO.class, postMessageID);
			session.getTransaction().commit();
			return postMessage;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public List<PostMessageVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			List<PostMessageVO> list = session.createQuery("from PostMessageVO", PostMessageVO.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}


	
}

