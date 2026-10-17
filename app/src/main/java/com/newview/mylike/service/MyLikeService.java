package com.newview.mylike.service;

import java.util.List;
import java.util.Map;

import com.newview.mylike.model.MyLikeVO;

public interface MyLikeService {
	MyLikeVO insertMyLike(Integer userID, Integer actID);
	MyLikeVO updateMyLike(Integer userID, Integer actID);
	void deleteMyLike(Integer myLikeID);
	MyLikeVO getOneMyLike(Integer myLikeID);
	List<MyLikeVO> getAll();
    int findMyLikeIDByUserIDAndActID(Integer userID, Integer actID);
	List<Map<String, Integer>> findMyLikeIDsByUserID(Integer userID) ;
	int findMyLikeIDByUserIDAndActIDforDe(Integer userID, Integer actID);
}
