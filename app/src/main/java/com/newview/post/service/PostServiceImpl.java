package com.newview.post.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.newview.post.model.PostDAO;
import com.newview.post.model.PostDAOImpl;
import com.newview.post.model.PostVO;
import com.newview.util.HibernateUtil;

public class PostServiceImpl implements PostService {

	private PostDAO postdao;
	private static final int PAGE_SIZE = 10;

	public PostServiceImpl() {
		postdao = new PostDAOImpl();
	}

	@Override
	public int addPost(PostVO postVO) {

		return postdao.insert(postVO);
	}

	@Override
	public int updatePost(PostVO postVO) {
		return postdao.update(postVO);

	}

	@Override
	public int deletePost(int postID) {
		return postdao.delete(postID);
	}

	@Override
	public PostVO getPostByPK(int postID) {
		return postdao.findByPrimaryKey(postID);
	}

	@Override
	public List<PostVO> getAll() {
		return postdao.getAll();
	}

	@Override
	public int getAuthorIDByPostID(int postID) {
		return postdao.getAuthorIDByPostID(postID);
	}
}
