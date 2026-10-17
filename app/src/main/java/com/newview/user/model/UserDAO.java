package com.newview.user.model;

import java.util.List;

import com.newview.orders.model.OrdersVO;

public interface UserDAO {
	boolean checkUserAccount(String account);
	boolean checkUserAccount(String account, String password);
	boolean checkUserAccountByEmail(String email);
	public int insert(UserVO userVO);
	public int update(UserVO userVO);
	public int delete(Integer userID);
	public UserVO findByPrimaryKey(Integer userID);
	public UserVO getUserByAccount(String account);
	public UserVO getUserByEmail(String email);
	public List<UserVO> getAll();
	// 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<UserVO> getAll(Map<String, String[]> map);
	public OrdersVO getOrderByUserID(Integer userID);
//	public Optional<OrdersVO> getOrderByUserID(Integer userID);
	public List<Object[]> getPublisherNameByUserID(Integer userID);
	public List<Object[]> getActPicIDByUserID(Integer userID);
	public List<Object[]> getActNameByUserID(Integer userID);
	public List<Object[]> getMyLikeByUserID(Integer userID);
}
