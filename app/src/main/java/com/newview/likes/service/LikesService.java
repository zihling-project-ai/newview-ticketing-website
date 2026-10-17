package com.newview.likes.service;

import java.util.List;
import java.util.Map;

import com.newview.likes.model.LikesVO;
import com.newview.post.model.PostVO;

public interface LikesService {

	int addLikes(LikesVO likesVO);

	int updateLikes(LikesVO likesVO);

	int deleteLikes(int likeID);

	LikesVO getPostByPK(int likeID);

	List<LikesVO> getAll();

}
