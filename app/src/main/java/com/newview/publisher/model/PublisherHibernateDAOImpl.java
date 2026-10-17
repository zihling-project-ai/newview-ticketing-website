package com.newview.publisher.model;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.pubuser.model.PubUserVO;
import com.newview.util.HibernateUtil;

public class PublisherHibernateDAOImpl implements PublisherHibernateDAO{

	private static final String GET_ALL_STMT = "from PublisherVO order by pubID";
	
	@Override
	public void add(PublisherVO publisher) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.saveOrUpdate(publisher);
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			throw ex;
		}
	}

	@Override
	public void update(PublisherVO publisher) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.update(publisher);
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			throw ex;
		}
	}

	@Override
	public void delete(Integer pubID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			PublisherVO pub = session.get(PublisherVO.class,pubID);
			if(pub != null) {
				session.delete(pub);
			}
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			throw ex;
		}
	}

	@Override
	public PublisherVO findByPK(Integer pubID) {
		PublisherVO publisher = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			publisher = (PublisherVO) session.get(PublisherVO.class,pubID);
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			throw ex;
		}
		return publisher;
	}

	@Override
	public List<PublisherVO> getAll() {
		List<PublisherVO> list = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			Query<PublisherVO> query = session.createQuery(GET_ALL_STMT,PublisherVO.class);
			list = query.getResultList();
			session.getTransaction().rollback();
		}catch(Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return list;
	}

	@Override
	public Set<PubUserVO> getPubuserByPubID(Integer pubID) {
		Set<PubUserVO> set = findByPK(pubID).getPubUserVOs();
		return set;
	}

}
