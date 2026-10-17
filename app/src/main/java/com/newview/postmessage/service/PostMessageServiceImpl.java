package com.newview.postmessage.service;

import java.util.List;

import com.newview.postmessage.model.PostMessageDAO;
import com.newview.postmessage.model.PostMessageDAOImpl;
import com.newview.postmessage.model.PostMessageVO;



public class PostMessageServiceImpl implements PostMessageService{
	
	private PostMessageDAO postmessagedao;
	
	
	public PostMessageServiceImpl() {
		postmessagedao = new PostMessageDAOImpl();
	}


	@Override
	public int addPostMessage(PostMessageVO postVO) {
		// TODO Auto-generated method stub
		return postmessagedao.insert(postVO);
	}


	@Override
	public int updatePostMessage(PostMessageVO postVO) {
		// TODO Auto-generated method stub
		return postmessagedao.update(postVO);
	}


	@Override
	public int deletePostMessage(int postMessageID) {
		// TODO Auto-generated method stub
		return postmessagedao.delete(postMessageID);
	}


	@Override
	public PostMessageVO getPostMessageByPK(int postMessageID) {
		// TODO Auto-generated method stub
		return postmessagedao.findByPrimaryKey(postMessageID);
	}


	@Override
	public List<PostMessageVO> getAll() {
		// TODO Auto-generated method stub
		return postmessagedao.getAll();
	}



	


}
