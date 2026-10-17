package com.newview.user.model;

import java.util.List;

import org.hibernate.Session;

import com.newview.orders.model.OrdersVO;
import com.newview.util.HibernateUtil;

public class UserDAOImpl implements UserDAO {

	@Override
	public int insert(UserVO userVO) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		int userID = 0;

		try {
			session.beginTransaction();
			userID = (int) session.save(userVO);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return userID;
	}

	@Override
	public int update(UserVO userVO) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			session.update(userVO);
			session.getTransaction().commit();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return -1;
	}

	@Override
	public int delete(Integer userID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			UserVO user = session.get(UserVO.class, userID);
			if (user != null) {
				session.delete(user);
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
	public UserVO findByPrimaryKey(Integer userID) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			UserVO user = session.get(UserVO.class, userID);
			session.getTransaction().commit();
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		session.getTransaction().commit();
		return null;
	}

	@Override
	public List<UserVO> getAll() {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();
			List<UserVO> list = session.createQuery("from UserVO", UserVO.class).list();
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return null;
	}

	@Override
	public boolean checkUserAccount(String account) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();

			String sql = "from UserVO WHERE userAccount = :userAccount ";
			UserVO user = (UserVO) session.createQuery(sql).setParameter("userAccount", account).uniqueResult();

			// 若不存在使用者帳號為 null -> 回傳 false
			if (user == null) {
				session.getTransaction().commit();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		// 若存在使用者帳號查得到值-> 回傳 true
		session.getTransaction().commit();
		return true;
	}

	@Override
	public boolean checkUserAccount(String account, String password) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		String userPassword;

		try {
			session.beginTransaction();

			String sql = "from UserVO WHERE userAccount = :userAccount ";
			UserVO user = (UserVO) session.createQuery(sql).setParameter("userAccount", account).uniqueResult();

			// 若不存在使用者帳號為 null -> 回傳 true
			if (user != null) {
				// userAccount 已確認 DB 中有此筆資料不需再用變數存放
				// 判斷 userPassword 是否一致，比對正確回傳 true
				userPassword = user.getUserPassword();

				if (!password.equals(userPassword)) {
					// 傳入密碼與 DB 中密碼不一致 -> 回傳 false
					return false;
				}

				// 比對完成 -> 回傳 true
				session.getTransaction().commit();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		// 若使用者不存在得不到值 -> 回傳 false
		session.getTransaction().commit();
		return false;
	}

	@Override
	public UserVO getUserByAccount(String account) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();

			String sql = "from UserVO WHERE userAccount = :userAccount ";
			UserVO userVO = (UserVO) session.createQuery(sql).setParameter("userAccount", account).uniqueResult();
			session.getTransaction().commit();

			return userVO;
		} catch (Exception e) {
			session.getTransaction().rollback();

		}
		session.getTransaction().commit();
		return null;
	}

	@Override
	public OrdersVO getOrderByUserID(Integer userID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();

			String sql = "from OrdersVO WHERE userID = :userID ";
			OrdersVO ordersVO = (OrdersVO) session.createQuery(sql).setParameter("userID", userID).uniqueResult();
			session.getTransaction().commit();

			return ordersVO;
		} catch (Exception e) {
			session.getTransaction().rollback();

		}
		session.getTransaction().commit();
		return null;
	}

	public List<Object[]> getActPicIDByUserID(Integer userID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();

			List<Object[]> list = session.createNativeQuery("SELECT ap.ActPicID FROM user u "
					+ "JOIN orders o ON u.userid = o.userid " + "JOIN orderlist ol ON o.orderID = ol.orderID "
					+ "JOIN act a ON ol.actID = a.actID " + "JOIN actpic ap ON a.actID = ap.actID "
					+ "WHERE a.actID = ap.actID and u.userid = :userID ").setParameter("userID", userID).list();

			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		} finally {
			// Shared factory is closed by RecoveryLifecycle only.
		}
		return null;
	}

	@Override
	public boolean checkUserAccountByEmail(String email) {
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		try {
			session.beginTransaction();

			String sql = "from UserVO WHERE userEmail = :userEmail ";
			UserVO user = (UserVO) session.createQuery(sql).setParameter("userEmail", email).uniqueResult();

			// 若不存在使用者帳號為 null -> 回傳 false
			if (user == null) {
				session.getTransaction().commit();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		// 若存在使用者帳號查得到值-> 回傳 true
		session.getTransaction().commit();
		return true;
	}

	@Override
	public UserVO getUserByEmail(String email) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();

			String sql = "from UserVO WHERE userEmail = :userEmail ";
			UserVO userVO = (UserVO) session.createQuery(sql).setParameter("userEmail", email).uniqueResult();
			session.getTransaction().commit();

			return userVO;
		} catch (Exception e) {
			session.getTransaction().rollback();

		}
		session.getTransaction().commit();
		return null;
	}

	@Override
	public List<Object[]> getPublisherNameByUserID(Integer userID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();

			List<Object[]> list = session.createNativeQuery("SELECT p.pubname FROM user u "
					+ "JOIN orders o ON u.userid = o.userid "
					+ "JOIN publisher p ON o.pubID = p.pubID "
					+ "where o.pubID = p.pubID and u.userID = :userID ")
					.setParameter("userID", userID).list();
			
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		} finally {
			// Shared factory is closed by RecoveryLifecycle only.
		}
		return null;
	}
	
	public List<Object[]> getActNameByUserID(Integer userID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();

			List<Object[]> list = session.createNativeQuery
					("SELECT a.actName FROM user u "
							+ "JOIN orders o ON u.userid = o.userid "
							+ "JOIN orderlist ol ON o.orderID = ol.orderID "
							+ "JOIN act a ON ol.actID = a.actID "
							+ "WHERE ol.actID = a.actID and u.userid = :userID ")
					.setParameter("userID", userID).list();
			
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		} finally {
			// Shared factory is closed by RecoveryLifecycle only.
		}
		return null;
	}

	@Override
	public List<Object[]> getMyLikeByUserID(Integer userID) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

		try {
			session.beginTransaction();

			List<Object[]> list = session.createNativeQuery
					("SELECT ml.userID, u.userName, a.actName, a.actIntroduce FROM myLike ml "
							+ "JOIN act a ON ml.actID = a.actID "
							+ "JOIN user u ON ml.userID = u.userID "
							+ "WHERE ml.userID = :userID ")
					.setParameter("userID", userID).list();
			
			session.getTransaction().commit();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		} finally {
			// Shared factory is closed by RecoveryLifecycle only.
		}
		return null;
	}
}
