package com.newview.cityaddress.model;

import java.util.List;

import org.hibernate.Session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newview.act.model.ActVO;
import com.newview.util.HibernateUtil;

public class CityAddressDAOHibernatelmpl implements CityAddressDAO{

	@Override
	public void insert(CityAddress act) {
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
	public void update(CityAddress act) {
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
	public void delete(Integer ActID) {
		// TODO Auto-generated method stub
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			CityAddress act = session.get(CityAddress.class, ActID);
			if (act != null) {
				session.delete(act);
			}
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		
	}

	@Override
	public CityAddress findByPrimaryKey(Integer CityAddressID) {
		// TODO Auto-generated method stub
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		CityAddress act = null;
		try {
			session.beginTransaction();
			 act = session.get(CityAddress.class, CityAddressID);
			session.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return act;
	}

	@Override
	public List<CityAddress> getAll() {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();
	        List<CityAddress> list = session.createQuery("from CityAddress", CityAddress.class).list();
	        session.getTransaction().commit();
	        return list;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return null;
	}

	

}
