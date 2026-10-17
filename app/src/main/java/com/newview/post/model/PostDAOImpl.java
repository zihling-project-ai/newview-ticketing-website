package com.newview.post.model;

import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.newview.util.HibernateUtil;

public class PostDAOImpl implements PostDAO {

	@Override
	public int insert(PostVO postVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		int postID = 0;
		
		try {
			session.beginTransaction();
//			session.saveOrUpdate(postVO);
			postID = (int) session.save(postVO);
			session.getTransaction().commit();
			return postID;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int update(PostVO postVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(postVO);
			session.getTransaction().commit();
            return 1;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

    @Override
    public int delete(Integer postID) {
        try(Session session=HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction tx=session.beginTransaction();
            try {
                Object found=session.createNativeQuery("SELECT postID FROM post WHERE postID=:id FOR UPDATE").setParameter("id",postID).uniqueResult();
                if(found==null){tx.rollback();return 0;}
                // Explicit child cleanup avoids historical bidirectional cascade re-saving the post.
                for(String table:java.util.List.of("likes","report","postmessage","postpic"))
                    session.createNativeQuery("DELETE FROM "+table+" WHERE postID=:id").setParameter("id",postID).executeUpdate();
                int affected=session.createNativeQuery("DELETE FROM post WHERE postID=:id").setParameter("id",postID).executeUpdate();
                tx.commit();return affected;
            }catch(RuntimeException e){if(tx.isActive())tx.rollback();throw e;}
        }
    }

	@Override
	public PostVO findByPrimaryKey(Integer postID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			PostVO post = session.get(PostVO.class, postID);
			session.getTransaction().commit();
			return post;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public List<PostVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			List<PostVO> list = session.createQuery("from PostVO", PostVO.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}



	@Override
	public List<PostVO> findByCategory(int postCategoryID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();

	    try {
	        session.beginTransaction();
	        // 使用 HQL 查詢
	        List<PostVO> catelist = session.createQuery("FROM PostVO p WHERE p.postCategoryVO.postCategoryID = :postCategoryID", PostVO.class)
	                .setParameter("postCategoryID", postCategoryID)
	                .list();
	        session.getTransaction().commit();
	        return catelist;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return null;
	}

	
	
	@Override
	public int getAuthorIDByPostID(int postID) {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();

	    try {
	        session.beginTransaction();
	        // 使用 HQL 查詢
	        String hql = "SELECT p.userVO.userID FROM PostVO p WHERE p.postID = :postID";
	        int userID = (int) session.createQuery(hql)
	                .setParameter("postID", postID)
	                .uniqueResult();
	        session.getTransaction().commit();
	        return userID;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return 0; // 或者返回其他值
	}


	
	
	
	@Override
	public List<PostVO> getByCompositeQuery(Map<String, String> query, String orderBy, int start, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

}
