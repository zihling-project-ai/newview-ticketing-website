package com.newview.mylike.model;

import java.util.List;
import java.util.Map;

public interface MyLikeDAO {
	
	public int insert(MyLikeVO MyLikeVO);
    public int update(MyLikeVO MyLikeVO);
    public void delete(Integer myLikeID);
    public MyLikeVO findByPrimaryKey(Integer myLikeID);
    public List<MyLikeVO> getAll();
    Integer findMyLikeIDByUserIDAndActID(Integer userID, Integer actID);
    List<Map<String, Integer>> findMyLikeIDsByUserID(Integer userID);
//  萬用複合查詢(傳入參數型態Map)(回傳List)
//  public List<MyLikeVO> getAll(Map<String, String[]> map);
    
}