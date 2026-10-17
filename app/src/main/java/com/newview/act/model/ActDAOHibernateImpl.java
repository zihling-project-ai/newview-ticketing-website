package com.newview.act.model;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newview.actcategory.model.ActCategory;
import com.newview.util.HibernateUtil;

public class ActDAOHibernateImpl implements ActDAO {

    private SessionFactory factory;

    public ActDAOHibernateImpl() {
    }

    public ActDAOHibernateImpl(SessionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void insert(ActVO act) {
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
    public void update(ActVO act) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();

          
            ActVO originalAct = session.get(ActVO.class, act.getActID());

            if (originalAct != null) {
                if (act.getActName() != null && !act.getActName().isEmpty() &&
                    !act.getActName().equals(originalAct.getActName())) {
                    originalAct.setActName(act.getActName());
                }
                
                if (act.getActPrice() != null && !act.getActPrice().equals(originalAct.getActPrice())) {
                    originalAct.setActPrice(act.getActPrice());
                }
                
                if (act.getActTime() != null && !act.getActTime().equals(originalAct.getActTime())) {
                    originalAct.setActTime(act.getActTime());
                }
                
                if (act.getActScope() != null && !act.getActScope().equals(originalAct.getActScope())) {
                    originalAct.setActScope(act.getActScope());
                }
                
                if (act.getActIntroduce() != null && !act.getActIntroduce().isEmpty() &&
                    !act.getActIntroduce().equals(originalAct.getActIntroduce())) {
                    originalAct.setActIntroduce(act.getActIntroduce());
                }
                
                if (act.getActContent() != null && !act.getActContent().isEmpty() &&
                    !act.getActContent().equals(originalAct.getActContent())) {
                    originalAct.setActContent(act.getActContent());
                }
                
                if (act.getTime() != null && !act.getTime().equals(originalAct.getTime())) {
                    originalAct.setTime(act.getTime());
                }
                
                if (act.getActDate() != null && !act.getActDate().equals(originalAct.getActDate())) {
                    originalAct.setActDate(act.getActDate());
                }
                
                if (act.getApprovalCondition() != null && !act.getApprovalCondition().equals(originalAct.getApprovalCondition())) {
                    originalAct.setApprovalCondition(act.getApprovalCondition());
                }
                
                if (act.getCityAddress() != null && !act.getCityAddress().isEmpty() &&
                	    originalAct.getCityAddress() != null && !originalAct.getCityAddress().isEmpty() &&
                	    !act.getCityAddress().equals(originalAct.getCityAddress())) {
                	    originalAct.setCityAddress(act.getCityAddress());
                	}
//                if (act.getCityAddressID() != null && !act.getCityAddressID().getCityName().isEmpty() &&
//                        !act.getCityAddressID().equals(originalAct.getCityAddressID())) {
//                        originalAct.setCityAddressID(act.getCityAddressID());
//                    }
                
                if (act.getCityAddressID() != null &&
                	    !act.getCityAddressID().equals(originalAct.getCityAddressID())) {
                	    originalAct.setCityAddressID(act.getCityAddressID());
                	}
                
                if (act.getActCategory() != null && !act.getActCategory().equals(originalAct.getActCategory())) {
                    originalAct.setActCategory(act.getActCategory());
                }

                session.update(originalAct);
            }

            session.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
    }


    @Override
    public void delete(Integer ActID) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            ActVO act = session.get(ActVO.class, ActID);
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
    public ActVO findByPrimaryKey(Integer actID) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        ActVO act = null;
        try {
            session.beginTransaction();
            act = session.get(ActVO.class, actID); //使用actID取得ActVO

            if (act != null) {
                Hibernate.initialize(act.getActpics());  
            }
            
            session.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
        return act;
    }


    @Override
    public List<ActVO> getAll() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            List<ActVO> list = session.createQuery("from ActVO", ActVO.class).list();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(list);

            session.getTransaction().commit();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
        return null;
    }
    public List<ActVO> getActsWithNameAndAssociations(String actName) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            List<ActVO> list = session.createQuery("from ActVO act " +
                    "left join fetch act.actCategory " +
                    "left join fetch act.actpics " +
                    "left join fetch act.city " +
                    "where act.actName like :actName", ActVO.class)
                    .setParameter("actName", "%" + actName + "%")
                    .list();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(list);
            list = list.stream().distinct().collect(Collectors.toList());
            session.getTransaction().commit();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
        return null;
    }


  

    @Override
    public List<ActVO> getActPics() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            
            //連結ActVO實體和其關連的圖片訊息 一次或取資訊；list() 方法將查询结果轉換為 List<ActVO> 
            List<ActVO> list = session.createQuery("select distinct a from ActVO a left join fetch a.actpics", ActVO.class).list();
            
            //創建一个Gson將資料轉換成JSON格式；排除没有使用Gson註解標記字段
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            
            String json = gson.toJson(list);
            System.out.println(json);
            session.getTransaction().commit();
            
            //返回活動相關的資訊及圖片(json格式) list
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
        return null;
    }

    @Override
    public List<ActCategory> getAllCategories() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            List<ActCategory> categories = session.createQuery("FROM ActCategory", ActCategory.class).list();
            session.getTransaction().commit();
            return categories;
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
        return null;
    }
    //透過類別尋找
    @Override
    public List<ActVO> getActsByCategoryID(Integer actCategoryID) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();            
            List<ActVO> acts = session.createQuery(
                    "SELECT act FROM ActVO act WHERE actVO.actCategory.actCategoryID = :categoryID", ActVO.class)
                    .setParameter("categoryID", actCategoryID)
                    .list();
            
            session.getTransaction().commit();
            return acts;
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
        return null;
    }

	public List<ActVO> getActByPubID(Integer pubID){
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();
			String hql = "FROM ActVO AS a WHERE a.publisherVO.pubID = :pubID";
			Query<ActVO> query = session.createQuery(hql,ActVO.class);
			query.setParameter("pubID", pubID);
			List<ActVO> result = query.getResultList();
			session.getTransaction().commit();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}



   

}
