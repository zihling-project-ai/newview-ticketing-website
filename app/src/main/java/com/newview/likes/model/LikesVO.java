package com.newview.likes.model;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "likes")
//配合 HQL
//@NamedQuery(name = "")
public class LikesVO {
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "likeID")
	private Integer likeID;

	@Expose
	@Column(name = "likeOrNot", columnDefinition = "TINYINT")
	private Integer likeOrNot;
	
	@Expose
	@Column(name = "visitorID")
	private Integer visitorID;

	@ManyToOne
	@JoinColumn(name = "postID", referencedColumnName = "postID")
	private PostVO postVO;

	@ManyToOne
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;

	public LikesVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LikesVO(Integer likeID, Integer likeOrNot, PostVO postVO, UserVO userVO) {
		super();
		this.likeID = likeID;
		this.likeOrNot = likeOrNot;
		this.postVO = postVO;
		this.userVO = userVO;
	}

	public Integer getLikeID() {
		return likeID;
	}

	public void setLikeID(Integer likeID) {
		this.likeID = likeID;
	}

	public Integer getLikeOrNot() {
		return likeOrNot;
	}

	public void setLikeOrNot(Integer likeOrNot) {
		this.likeOrNot = likeOrNot;
	}

	public Integer getVisitorID() {
		return visitorID;
	}

	public void setVisitorID(Integer visitorID) {
		this.visitorID = visitorID;
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
		return "LikesVO [likeID=" + likeID + ", likeOrNot=" + likeOrNot + ", visitorID=" + visitorID + "]";
	}

	

}
