package com.newview.post.service;

import java.util.List;
import java.util.Map;

import com.newview.post.model.PostVO;

public interface PostService {

	public int addPost(PostVO postVO);

	public int updatePost(PostVO postVO);

	public int deletePost(int postID);

	public PostVO getPostByPK(int postID);

	public List<PostVO> getAll();

	public int getAuthorIDByPostID(int postID);

//	public int getTotalPostCount(Map<String, String[]> map);

//	List<PostVO> getPostsByCompositeQuery(Map<String, String[]> map, int currentPage, String orderBy);

}
