package com.newview.post.model;

import java.util.*;

public interface PostDAO {
	
	public int insert(PostVO postVO);

	public int update(PostVO postVO);

	public int delete(Integer postID);

	PostVO findByPrimaryKey(Integer postID);
	
	public List<PostVO> findByCategory(int postCategoryID);

	public List<PostVO> getAll();

	List<PostVO> getByCompositeQuery(Map<String, String> query, String orderBy, int start, int pageSize);

	public int getAuthorIDByPostID(int postID);

	

	
}
