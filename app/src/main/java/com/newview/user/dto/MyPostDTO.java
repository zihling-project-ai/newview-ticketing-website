package com.newview.user.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.newview.post.model.PostVO;

public class MyPostDTO {
	
	List<Integer> postID = new ArrayList<>();
	List<String> postCategoryName = new ArrayList<>();
	List<String> postContent = new ArrayList<>();
	List<String> postHeader = new ArrayList<>();
	List<Timestamp> postDateTime = new ArrayList<>();
	List<Timestamp> lastEditedTime = new ArrayList<>();
	List<Integer> postStatus = new ArrayList<>();
	
	public MyPostDTO(PostVO postVO) {
		
		this.postID.add(postVO.getPostID());
		this.postCategoryName.add(postVO.getPostCategoryVO().getPostCategoryName());
		this.postContent.add(postVO.getPostContent());
		this.postHeader.add(postVO.getPostHeader());
		this.postDateTime.add(postVO.getPostDateTime());
		this.lastEditedTime.add(postVO.getLastEditedTime());
		this.postStatus.add(postVO.getPostStatus());
	}
	
	public MyPostDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "MyPostDTO [postID=" + postID + ", postCategoryName=" + postCategoryName + ", postContent=" + postContent
				+ ", postHeader=" + postHeader + ", postDateTime=" + postDateTime + ", lastEditedTime=" + lastEditedTime
				+ ", postStatus=" + postStatus + "]";
	}
}
