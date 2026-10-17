package com.newview.postpic.model;

import java.util.*;

public interface PostPicDAO {
	public int insert(PostPicVO postPicVO);
	public int update(PostPicVO postPicVO);
	public int delete(Integer postID);
	public  PostPicVO findByPrimaryKey(Integer postPicID);
	public List<PostPicVO> getAll();
	// 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<PostVO> getAll(Map<String, String[]> map);
}
