package com.newview.likes.model;

import java.util.*;

public interface LikesDAO {
	public int insert(LikesVO likeVO);
	public int update(LikesVO likeVO);
	public int delete(Integer likeID);
	public LikesVO findByPrimaryKey(Integer likeID);
	public List<LikesVO> getAll();

//	 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<UseDiscountVO> getAll(Map<String, String[]> map);
}
