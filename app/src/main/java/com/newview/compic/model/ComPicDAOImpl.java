package com.newview.compic.model;

import java.util.List;

import org.hibernate.Session;

import com.newview.util.HibernateUtil;

public class ComPicDAOImpl implements ComPicDAO {

	@Override
	public int insert(ComPicVO comPicVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(comPicVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int update(ComPicVO comPicVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(comPicVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int delete(Integer comPicID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			ComPicVO comPic = session.get(ComPicVO.class, comPicID);
			if (comPic != null) {
				session.delete(comPic);
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
	public ComPicVO findeByPrimaryKey(Integer comPicID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			ComPicVO comPic = session.get(ComPicVO.class, comPicID);
			session.getTransaction().commit();
			return comPic;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public List<ComPicVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			List<ComPicVO> comPic = session.createQuery("from ComPicVO", ComPicVO.class).list();
			session.getTransaction().commit();
			return comPic;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}
}
