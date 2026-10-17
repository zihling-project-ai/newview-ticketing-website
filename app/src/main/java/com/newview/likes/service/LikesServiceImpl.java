package com.newview.likes.service;

import java.util.List;

import com.newview.likes.model.LikesDAO;
import com.newview.likes.model.LikesDAOImpl;
import com.newview.likes.model.LikesVO;


public class LikesServiceImpl implements LikesService {

	private LikesDAO likesdao;

	public LikesServiceImpl() {
		likesdao = new LikesDAOImpl();
	}

	@Override
	public int addLikes(LikesVO likesVO) {

		return likesdao.insert(likesVO);
	}

	@Override
	public int updateLikes(LikesVO likesVO) {
		return likesdao.update(likesVO);

	}

	@Override
	public int deleteLikes(int likeID) {
		return likesdao.delete(likeID);
	}

	@Override
	public LikesVO getPostByPK(int likeID) {
		return likesdao.findByPrimaryKey(likeID);
	}

	@Override
	public List<LikesVO> getAll() {
		return likesdao.getAll();
	}

}
