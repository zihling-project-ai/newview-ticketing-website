package com.newview.postcategory.model;

import java.util.*;



public interface PostCategoryDAO {
	public int insert(PostCategoryVO postCategoryVO);
	public int update(PostCategoryVO postCategoryVO);
	public int delete(Integer postCategoryID);
	public  PostCategoryVO findByPrimaryKey(Integer postCategoryID);
	public List<PostCategoryVO> getAll();
	// 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<PostCategoryVO> getAll(Map<String, String[]> map);
}
