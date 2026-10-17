package com.newview.postpic.service;

import java.util.List;
import com.newview.postpic.model.PostPicVO;

public interface PostPicService {

	public int addPostPic(PostPicVO postPicVO);
	public int updatePostPic(PostPicVO postPicVO);
	public int deletePostPic(int postID);
	public int getPostPicByPK(int postPicID);
	public List<PostPicVO> getAll();
}
