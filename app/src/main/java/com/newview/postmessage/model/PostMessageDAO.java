package com.newview.postmessage.model;

import java.util.*;

public interface PostMessageDAO {
	
	public int insert(PostMessageVO postMessageVO);
	public int update(PostMessageVO postMessageVO);
	public int delete(Integer postMessageID);
	public PostMessageVO findByPrimaryKey(Integer postMessageID);
	public List<PostMessageVO> getAll();
	// 萬用複合查詢(傳入參數型態Map)(回傳List)
	//	public List<PostMessageVO> getAll(Map<String, String[]> map);
	
}
