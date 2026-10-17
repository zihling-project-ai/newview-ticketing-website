package com.newview.mylike.service;

import java.util.List;
import java.util.Map;

import com.newview.act.model.ActVO;
import com.newview.mylike.model.MyLikeDAO;
import com.newview.mylike.model.MyLikeDAOImpl;
import com.newview.mylike.model.MyLikeVO;
import com.newview.user.model.UserVO;


public class MyLikeServiceImpl implements MyLikeService{

	private MyLikeDAO myLikeDao;
	
	public MyLikeServiceImpl() {
		myLikeDao = new MyLikeDAOImpl();
	}
	
	public MyLikeVO insertMyLike(Integer userID, Integer actID) {

		MyLikeVO myLikeVO = new MyLikeVO();
		
		UserVO userVO = new UserVO();
		userVO.setUserID(userID);
		myLikeVO.setUser(userVO);
		
		ActVO actVO = new ActVO();
		actVO.setActID(actID);
		myLikeVO.setAct(actVO);

		myLikeDao.insert(myLikeVO);

		return myLikeVO;
	}
	
	public MyLikeVO updateMyLike(Integer userID, Integer actID) {

		MyLikeVO myLikeVO = new MyLikeVO();
		
		UserVO userVO = new UserVO();
		userVO.setUserID(userID);
		myLikeVO.setUser(userVO);
		
		ActVO actVO = new ActVO();
		actVO.setActID(actID);
		myLikeVO.setAct(actVO);

		myLikeDao.update(myLikeVO);

		return myLikeVO;
	}
	
	public void deleteMyLike(Integer myLikeID) {
		myLikeDao.delete(myLikeID);
	}
	
	
	public MyLikeVO getOneMyLike(Integer myLikeID) {
		return myLikeDao.findByPrimaryKey(myLikeID);
	}

	
	public List<MyLikeVO> getAll() {
		return myLikeDao.getAll();
	}
	
	public int findMyLikeIDByUserIDAndActID(Integer userID, Integer actID) {
	    Integer myLikeID = myLikeDao.findMyLikeIDByUserIDAndActID(userID, actID);
	    return (myLikeID != null) ? 1 : 0;
	}
	
	public List<Map<String, Integer>> findMyLikeIDsByUserID(Integer userID) {
	    return myLikeDao.findMyLikeIDsByUserID(userID);
	}
	
	public int findMyLikeIDByUserIDAndActIDforDe(Integer userID, Integer actID) {
	    Integer myLikeID = myLikeDao.findMyLikeIDByUserIDAndActID(userID, actID);
	    return myLikeID;
	}

}
