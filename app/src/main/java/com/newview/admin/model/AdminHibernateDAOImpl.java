package com.newview.admin.model;

import java.util.List;

import javax.persistence.FetchType;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.pubuser.model.PubUserVO;
import com.newview.util.HibernateUtil;

public class AdminHibernateDAOImpl implements AdminHibernateDAO{
	
	Session session = HibernateUtil.getSessionFactory().getCurrentSession();

	@Override
	public int update(AdminVO admin) {
		try {
			session.beginTransaction();
			session.update(admin);
			session.getTransaction().commit();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}
	

	@Override
	public List<AdminVO> getAll() {
		try {
			session.beginTransaction();
			List<AdminVO> list = session.createQuery("from AdminVO",AdminVO.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}
	
	// fetch = FetchType.EAGER
	
	
	// for login
	public AdminVO findByAccount(String adminAccount) {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			String hql = "FROM AdminVO WHERE adminAccount = :adminAccount";

			Query<AdminVO> query = session.createQuery(hql, AdminVO.class);
			System.out.println(query);

			query.setParameter("adminAccount", adminAccount);
			System.out.println(adminAccount);

			AdminVO adminVO = query.uniqueResult();
			session.getTransaction().commit();
			System.out.println("交易成功");
			System.out.println(adminVO);
			return adminVO;

		} catch (Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();

			System.out.println("交易失敗");
			return null;
		}
		
	}
}
