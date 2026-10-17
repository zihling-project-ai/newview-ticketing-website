package com.newview.pubuser.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.util.HibernateUtil;

public class PubUserHibernateDAOImpl implements PubUserHibernateDAO {
	private static final String GET_ALL_STMT = "from PubUserVO order by pubUserID";

	@Override
	public void add(PubUserVO pubUserVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.save(pubUserVO);
			session.getTransaction().commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			throw ex;
		}
	}

	@Override
	public void update(PubUserVO pubUserVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.update(pubUserVO);
			session.getTransaction().commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			throw ex;
		}
	}

	/*
	 * @Override public void delete(Integer pubUserID) { Session session =
	 * HibernateUtil.getSessionFactory().getCurrentSession(); try {
	 * session.beginTransaction(); PubUserVO pubUser =
	 * session.get(PubUserVO.class,pubUserID); if (pubUser != null) {
	 * session.delete(pubUser); } session.getTransaction().commit(); } catch
	 * (Exception e) { e.printStackTrace(); session.getTransaction().rollback(); }
	 * 
	 * }
	 */

	public void delete(Integer pubUserID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();

//			【此時多方(宜)可採用HQL刪除】
			Query query = session.createQuery("delete PubUserVO where pubUserID=?0");
			query.setParameter(0, pubUserID);
			System.out.println("刪除的筆數=" + query.executeUpdate());

//			【或此時多方(也)可採用去除關聯關係後，再刪除的方式】
//			PubUesrVO pubUserVO = new PubUesrVO();
//			pubUserVO.setPubUesrID(pubUserID);
//			session.delete(pubUserVO);
			session.getTransaction().commit();

		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
	}

	@Override
	public PubUserVO findByPK(Integer pubUserID) {
		PubUserVO pubUserVO = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			pubUserVO = (PubUserVO) session.get(PubUserVO.class, pubUserID);
			session.getTransaction().commit();

		} catch (Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			throw ex;
		}
		return pubUserVO;
	}

	// for login
	public PubUserVO findByAccount(String pubAccount) {
		// List<PubUserVO> pubUserVO = null;
		// PubUserVO pubUserVO = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			String hql = "FROM PubUserVO WHERE pubAccount = :pubAccount";

			Query<PubUserVO> query = session.createQuery(hql, PubUserVO.class);
			System.out.println(query);

			query.setParameter("pubAccount", pubAccount);
			System.out.println(pubAccount);

			PubUserVO pubuserVO = query.uniqueResult();
			session.getTransaction().commit();
			System.out.println("交易成功");
			System.out.println(pubuserVO);
			return pubuserVO;

		} catch (Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();

			System.out.println("交易失敗");
			return null;
		}

	}

	/*
	 * @Override public List<PubUserVO> getAll() { Session session =
	 * HibernateUtil.getSessionFactory().getCurrentSession(); try {
	 * session.beginTransaction(); List<PubUserVO> list =
	 * session.createQuery("from PubUser",PubUserVO.class).list();
	 * session.getTransaction().commit(); return list; } catch (Exception e) {
	 * e.printStackTrace(); session.getTransaction().rollback(); } return null; }
	 */

	public List<PubUserVO> getAll() {
		List<PubUserVO> list = null;
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			Query<PubUserVO> query = session.createQuery(GET_ALL_STMT, PubUserVO.class);
			list = query.getResultList();
			session.getTransaction().commit();
		} catch (Exception ex) {
			session.getTransaction().rollback();
			throw ex;
		}
		return list;
	}

	@Override
	public List<PubUserVO> getAllByCQ(Map<String, String> map) {

		// 創建的 Session 不會自動參與容器管理的事務
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {
			session.beginTransaction();

			if (map.size() == 0)
				return getAll();

			// 【●創建 CriteriaBuilder】用於建立查詢條件
			CriteriaBuilder builder = session.getCriteriaBuilder();
			// 【●創建 CriteriaQuery】//指定了查詢的返回型別
			CriteriaQuery<PubUserVO> criteriaQuery = builder.createQuery(PubUserVO.class);
			// 【●創建 Root】//查詢的根實體類別
			Root<PubUserVO> root = criteriaQuery.from(PubUserVO.class);

			List<Predicate> predicates = new ArrayList<>();

			for (Map.Entry<String, String> row : map.entrySet()) {
				if ("pubUserID".equals(row.getKey())) {
					predicates.add(builder.equal(root.get("pubUserID"), row.getValue()));
				}
				if ("pubNickname".equals(row.getKey())) {
					predicates.add(builder.like(root.get("pubNickname"), "%" + row.getValue() + "%"));
				}
				if ("pubAuthority".equals(row.getKey())) {
					byte pubAuthorityValue = Byte.parseByte(row.getValue());
					predicates.add(builder.equal(root.get("pubAuthority"), pubAuthorityValue));
				}


			}

			criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
			criteriaQuery.orderBy(builder.asc(root.get("pubUserID")));
			// 【●最後完成創建 javax.persistence.Query●】
			Query<PubUserVO> query = session.createQuery(criteriaQuery); // javax.persistence.Query; //Hibernate 5 開始
																			// 取代原 org.hibernate.Query 介面

			session.getTransaction().commit();
			return query.getResultList();

		} catch (RuntimeException ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
			return null;

		}

	}

	@Override
	public List<PubUserVO> getListByPubID(Integer pubID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			String hqlString = "FROM PubUserVO AS p WHERE p.publisherVO.pubID = :pubID";
			Query<PubUserVO> query = session.createQuery(hqlString,PubUserVO.class);
			query.setParameter("pubID", pubID);
			return query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

}
