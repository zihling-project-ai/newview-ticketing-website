package com.newview.postpic.service;

import java.util.List;
import java.util.Map;

import com.newview.postpic.model.PostPicDAO;
import com.newview.postpic.model.PostPicDAOImpl;
import com.newview.postpic.model.PostPicVO;




public class PostPicServiceImpl implements PostPicService{
	
	private PostPicDAO postpicdao;
	
	
	public PostPicServiceImpl() {
		postpicdao = new PostPicDAOImpl();
	}


	@Override
	public int addPostPic(PostPicVO postPicVO) {
		// TODO Auto-generated method stub
		return postpicdao.insert(postPicVO);
	}


	@Override
	public int updatePostPic(PostPicVO postPicVO) {
		// TODO Auto-generated method stub
		return postpicdao.update(postPicVO);
	}

	@Override
	public int deletePostPic(int postID) {
		postpicdao.delete(postID);
		return 0;
	}


	@Override
	public int getPostPicByPK(int postPicID) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public List<PostPicVO> getAll() {
		// TODO Auto-generated method stub
		return null;
	}


	



}
