package com.newview.postmessage.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.post.model.PostVO;
import com.newview.user.model.UserVO;

@Entity
@Table(name = "postmessage")
public class PostMessageVO {
	@Id
	@Expose
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer postMessageID;
	
	@Expose
	@Column(name = "mesContent", columnDefinition = "text")
	private String mesContent;
	
	@Expose
	@Column(name = "messageDate" , insertable=false)
	private Timestamp messageDate;
	
	@ManyToOne
	@JoinColumn(name = "postID", referencedColumnName = "postID")
	private PostVO postVO;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;

	public PostMessageVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PostMessageVO(Integer postMessageID, String mesContent, Timestamp messageDate, PostVO postVO,
			UserVO userVO) {
		super();
		this.postMessageID = postMessageID;
		this.mesContent = mesContent;
		this.messageDate = messageDate;
		this.postVO = postVO;
		this.userVO = userVO;
	}

	public Integer getPostMessageID() {
		return postMessageID;
	}

	public void setPostMessageID(Integer postMessageID) {
		this.postMessageID = postMessageID;
	}

	public String getMesContent() {
		return mesContent;
	}

	public void setMesContent(String mesContent) {
		this.mesContent = mesContent;
	}

	public Timestamp getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(Timestamp messageDate) {
		this.messageDate = messageDate;
	}

	public PostVO getPostVO() {
		return postVO;
	}

	public void setPostVO(PostVO postVO) {
		this.postVO = postVO;
	}

	public UserVO getUserVO() {
		return userVO;
	}

	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

	@Override
	public String toString() {
		return "PostMessageVO [postMessageID=" + postMessageID + ", mesContent=" + mesContent + ", messageDate="
				+ messageDate + "]";
	}

	
	

}
