package com.newview.cartact.model;

import java.util.List;
import org.hibernate.Session;
import com.newview.util.HibernateUtil;

public class CartActDAOImpl implements CartActDAO {

	@Override
	public int insert(CartActVO cartActVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(cartActVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int update(CartActVO cartActVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(cartActVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int delete(Integer cartActID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			CartActVO cartAct = session.get(CartActVO.class, cartActID);
			if (cartAct != null) {
				session.delete(cartAct);
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
	public CartActVO findeByPrimaryKey(Integer cartActID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			CartActVO cartAct = session.get(CartActVO.class, cartActID);
			session.getTransaction().commit();
			return cartAct;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public List<CartActVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			List<CartActVO> cartAct = session.createQuery("from CartActVO", CartActVO.class).list();
			session.getTransaction().commit();
			return cartAct;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}
}
