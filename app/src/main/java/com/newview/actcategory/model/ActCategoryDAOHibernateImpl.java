package com.newview.actcategory.model;

import java.util.List;

import org.hibernate.Session;

import com.newview.act.model.ActVO;
import com.newview.util.HibernateUtil;

public class ActCategoryDAOHibernateImpl implements ActCategoryDAO{

	@Override
	public void insert(ActCategory act) {
		// TODO Auto-generated method stub
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			Integer id = (Integer) session.save(act);
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		
	}

	@Override
	public void update(ActCategory act) {
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
	public void delete(Integer ActCategoryID) {
		// TODO Auto-generated method stub
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			ActCategory actCategory = session.get(ActCategory.class, ActCategoryID);
			if (actCategory != null) {
				session.delete(actCategory);
			}
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		
	}

	@Override
	public ActCategory findByPrimaryKey(Integer ActCategoryID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		ActCategory act = null;
		try {
			session.beginTransaction();
			 act = session.get(ActCategory.class, ActCategoryID);
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return act;
	}

	@Override
	public List<ActCategory> getAll() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			List<ActCategory> list = session.createQuery("from ActCategory", ActCategory.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	
	}

}
