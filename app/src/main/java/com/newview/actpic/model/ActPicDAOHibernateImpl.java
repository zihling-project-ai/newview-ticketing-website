package com.newview.actpic.model;

import java.util.List;

import org.hibernate.Session;

import com.newview.actcategory.model.ActCategory;
import com.newview.util.HibernateUtil;

public class ActPicDAOHibernateImpl implements ActPicDAO{

	@Override
	public void insert(ActPic actPic) {
		// TODO Auto-generated method stub
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			Integer id = (Integer) session.save(actPic);
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		
	}

	@Override
	public void update(ActPic act) {
		// TODO Auto-generated method stub
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.update(act);
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
	}

	@Override
	public void delete(Integer actPicID) {
		// TODO Auto-generated method stub
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			ActPic ActPicID = session.get(ActPic.class, actPicID);
			if (ActPicID != null) {
				session.delete(ActPicID);
			}
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
	}

	@Override
	public ActPic findByPrimaryKey(Integer actPicID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		ActPic act = null;
		try {
			session.beginTransaction();
			act = session.get(ActPic.class, actPicID);
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return act;
	}

	@Override
	public List<ActPic> getAll() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			List<ActPic> list = session.createQuery("from ActPic", ActPic.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

}
