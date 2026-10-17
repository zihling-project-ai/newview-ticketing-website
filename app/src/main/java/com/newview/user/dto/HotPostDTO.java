package com.newview.user.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.newview.post.model.PostVO;
import com.newview.postmessage.model.PostMessageVO;

public class HotPostDTO {

	List<String> userNickname = new ArrayList<>();
	List<Timestamp> messageDate = new ArrayList<>();
	List<String> postHeader = new ArrayList<>();
	List<String> postMessages = new ArrayList<>();
	List<Integer> likeCounts = new ArrayList<>();
	List<Integer> dislikeCounts = new ArrayList<>();

	public HotPostDTO(PostVO postVOs) {
		
			this.likeCounts.add(postVOs.getLikeCount());
			this.dislikeCounts.add(postVOs.getDisLikeCount());
			this.userNickname.add(postVOs.getUserVO().getUserNickname());
			this.postHeader.add(postVOs.getPostHeader());

			Set<PostMessageVO> postMessageVOs = postVOs.getPostMessageVOs();
			for (PostMessageVO postMessageVO : postMessageVOs) {
				this.postMessages.add(postMessageVO.getMesContent());
				this.messageDate.add(postMessageVO.getMessageDate());
			}
	}

	public HotPostDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "HotPostDTO [userNickname=" + userNickname + ", messageDate=" + messageDate + ", postHeader="
				+ postHeader + ", postMessages=" + postMessages + ", likeCounts=" + likeCounts + ", dislikeCounts="
				+ dislikeCounts + "]";
	}
}
