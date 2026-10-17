package com.newview.user.service;

import java.util.List;

import com.newview.orders.model.OrdersVO;
import com.newview.user.model.UserDAO;
import com.newview.user.model.UserDAOImpl;
import com.newview.user.model.UserVO;

public class UserServiceImpl implements UserService {

	private UserDAO userdao;

	public UserServiceImpl() {
		userdao = new UserDAOImpl();
	}

	@Override
	public boolean checkUserAccount(String account) {
		return userdao.checkUserAccount(account);
	}

	@Override
	public boolean checkUserAccount(String account, String password) {
		return userdao.checkUserAccount(account, password);
	}

	@Override
	public int addUser(UserVO userVO) {
		return userdao.insert(userVO);
	}

	@Override
	public int updateUser(UserVO userVO) {
		return userdao.update(userVO);
	}

	@Override
	public UserVO getUserByPK(int userID) {
		return userdao.findByPrimaryKey(userID);
	}

	@Override
	public List<UserVO> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserVO getUserByAccount(String account) {
		return userdao.getUserByAccount(account);
	}

	@Override
	public OrdersVO getOrderByUserID(Integer userID) {
		return userdao.getOrderByUserID(userID);
	}

//	@Override
//	public Optional<OrdersVO> getOrderByUserID(Integer userID) {
//		return userdao.getOrderByUserID(userID);
//	}
	
	@Override
	public boolean checkUserAccountByEmail(String email) {
		return userdao.checkUserAccountByEmail(email);
	}

	@Override
	public UserVO getUserByEmail(String email) {
		return userdao.getUserByEmail(email);
	}

	@Override
	public List<Object[]> getPublisherNameByUserID(Integer userID) {
		return userdao.getPublisherNameByUserID(userID);
	}

	@Override
	public List<Object[]> getActPicIDByUserID(Integer userID) {
		return userdao.getActPicIDByUserID(userID);
	}

	@Override
	public List<Object[]> getActNameByUserID(Integer userID) {
		return userdao.getActNameByUserID(userID);
	}

	@Override
	public List<Object[]> getMyLikeByUserID(Integer userID) {
		return userdao.getMyLikeByUserID(userID);
	}
}
