package com.newview.mylike.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.act.model.ActVO;
import com.newview.user.model.UserVO;
import com.newview.util.HibernateUtil;

public class MyLikeDAOImpl implements MyLikeDAO{	

	@Override
	public int insert(MyLikeVO myLikeVO) {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.saveOrUpdate(myLikeVO);
//			Integer id = (Integer) session.save(myLikeVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return 1;
	}



	@Override
	public int update(MyLikeVO myLikeVO) {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.update(myLikeVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
		
	}

	@Override
	public void delete(Integer myLikeID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();
	        MyLikeVO myLike = session.get(MyLikeVO.class, myLikeID);
	        if (myLike != null) {
	            // 先刪除user引用
	            UserVO userVO = myLike.getUser();
	            if (userVO != null) {
	                userVO.getMyLikeVOs().remove(myLike);
	                myLike.setUser(null);
	            }

	            // 再刪除 act引用
	            ActVO actVO = myLike.getAct();
	            if (actVO != null) {
	                actVO.getMyLikeVOs().remove(myLike);
	                myLike.setAct(null);
	            }

	            // 最後删除MyLikeVO
	            session.delete(myLike);
	        }
	        session.getTransaction().commit();
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	}

	@Override
	public MyLikeVO findByPrimaryKey(Integer myLikeID) {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			MyLikeVO myLike = session.get(MyLikeVO.class, myLikeID);
			session.getTransaction().commit();
			return myLike;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
		
	}

	@Override
	public List<MyLikeVO> getAll() {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			List<MyLikeVO> list = session.createQuery("from MyLikeVO", MyLikeVO.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
		
	}
	@Override
	public Integer findMyLikeIDByUserIDAndActID(Integer userID, Integer actID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();
	        Query<Integer> query = session.createQuery("SELECT myLikeID FROM MyLikeVO WHERE userVO.userID = :userID AND actVO.actID = :actID", Integer.class);
	        query.setParameter("userID", userID);
	        query.setParameter("actID", actID);
	        Integer myLikeID = query.uniqueResult();
	        session.getTransaction().commit();
	        return myLikeID;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return null;
	}
	@Override
	public List<Map<String, Integer>> findMyLikeIDsByUserID(Integer userID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();
	        
	       
	        Query<Object[]> query = session.createQuery("SELECT myLike.myLikeID, myLike.actVO.actID FROM MyLikeVO myLike WHERE myLike.userVO.userID = :userID", Object[].class);
	        query.setParameter("userID", userID);
	        List<Object[]> results = query.list();
	        
	        List<Map<String, Integer>> myLikeIDsWithActID = new ArrayList<>();
	        for (Object[] result : results) {
	            Integer myLikeID = (Integer) result[0];
	            Integer actID = (Integer) result[1];
	            
	            Map<String, Integer> entry = new HashMap<>();
	            entry.put("myLikeID", myLikeID);
	            entry.put("actID", actID);
	            myLikeIDsWithActID.add(entry);
	        }
	        
	        session.getTransaction().commit();
	        return myLikeIDsWithActID;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return null;
	}



	public static void main(String[] args) {
		MyLikeDAO dao = new MyLikeDAOImpl();

		//insert <test ok>
//		MyLikeVO myLike1 = new MyLikeVO();
//		UserVO user1 = new UserVO();
//		user1.setUserID(1);
//		myLike1.setUser(user1);
//		ActVO act1 = new ActVO();
//		act1.setActID(3);
//		myLike1.setAct(act1);
//		dao.insert(myLike1);

		//update <test ok>
		MyLikeVO myLike2 = new MyLikeVO();
		myLike2.setMyLikeID(3);
		UserVO user2 = new UserVO();
		user2.setUserID(1);
		myLike2.setUser(user2);
		ActVO act2 = new ActVO();
		act2.setActID(2);
		myLike2.setAct(act2);		
		dao.update(myLike2);

		// Delete <test ok>
//		dao.delete(4);

		// Find PK <test ok>
//		MyLikeVO myLike3 = dao.findByPrimaryKey(1);
//		System.out.println(myLike3);

		// FindALL <test ok>
//		List<MyLikeVO> list = dao.getAll();
//		for (MyLikeVO lists : list) {
//			System.out.println(lists);
//		}
	}
	
}