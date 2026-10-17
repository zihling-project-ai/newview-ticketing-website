package com.newview.postpic.model;

import java.util.List;

import org.hibernate.Session;

import com.newview.util.HibernateUtil;
import com.newview.util.Util;

public class PostPicDAOImpl implements PostPicDAO {

	@Override
	public int insert(PostPicVO postPicVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.saveOrUpdate(postPicVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return 1;
	}

	@Override
	public int update(PostPicVO postPicVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(postPicVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int delete(Integer postID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		int i = 0;
		try {
			session.beginTransaction();
			i= session.createNativeQuery("delete from postpic where postID =" + postID).executeUpdate();
//			PostPicVO post = session.get(PostPicVO.class, postID);
//			if (post != null) {
//				session.delete(post);
//			}
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return i;
	}

	@Override
	public PostPicVO findByPrimaryKey(Integer postPicID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			PostPicVO post = session.get(PostPicVO.class, postPicID);
			session.getTransaction().commit();
			return post;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public List<PostPicVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			List<PostPicVO> list = session.createQuery("from PostPicVO", PostPicVO.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

}
