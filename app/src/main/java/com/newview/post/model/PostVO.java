package com.newview.post.model;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.likes.model.LikesVO;
import com.newview.postcategory.model.PostCategoryVO;
import com.newview.postmessage.model.PostMessageVO;
import com.newview.postpic.model.PostPicVO;
import com.newview.report.model.ReportVO;
import com.newview.user.model.UserVO;

@Entity
@Table(name = "post")
public class PostVO {

	@Id
	@Expose
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "postID", updatable = false, insertable = false)
	private Integer postID;
	
	@Expose
	@Column(name = "postHeader")
	private String postHeader;

	@Expose
	@Column(name = "postDateTime", insertable=false)
	private Timestamp postDateTime;

	@Expose
	@Column(name = "lastEditedTime", insertable=false)
	private Timestamp lastEditedTime;

	@Expose
	@Column(name = "postContent", columnDefinition = "text")
	private String postContent;

	@Expose
	@Column(name = "disLikeCount")
	private Integer disLikeCount;

	@Expose
	@Column(name = "likeCount")
	private Integer likeCount;

	@Expose
	@Column(name = "postStatus", columnDefinition = "tinyint")
	private Integer postStatus;
	
	@Expose
	@OneToMany(mappedBy = "postVO" ,cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	private Set<ReportVO>reportVOs;
	
	@Expose
	@OneToMany(mappedBy = "postVO" ,cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	private Set<LikesVO>likesVOs;
	
	@Expose
	@OneToMany(mappedBy = "postVO" ,cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	private Set<PostPicVO> postPicVOs;
	
	@Expose
	@OneToMany(mappedBy = "postVO" , cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	private Set<PostMessageVO> postMessageVOs;

	@ManyToOne
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;

	@ManyToOne
	@JoinColumn(name = "postCategoryID", referencedColumnName = "postCategoryID")
	private PostCategoryVO postCategoryVO;

	public PostVO() {
		super();
		// TODO Auto-generated constructor stub
	}


	public PostVO(Integer postID, String postHeader, Timestamp postDateTime, Timestamp lastEditedTime,
			String postContent, Integer disLikeCount, Integer likeCount, Integer postStatus, Set<ReportVO> reportVOs,
			Set<LikesVO> likesVOs, Set<PostPicVO> postPicVOs, Set<PostMessageVO> postMessageVOs, UserVO userVO,
			PostCategoryVO postCategoryVO) {
		super();
		this.postID = postID;
		this.postHeader = postHeader;
		this.postDateTime = postDateTime;
		this.lastEditedTime = lastEditedTime;
		this.postContent = postContent;
		this.disLikeCount = disLikeCount;
		this.likeCount = likeCount;
		this.postStatus = postStatus;
		this.reportVOs = reportVOs;
		this.likesVOs = likesVOs;
		this.postPicVOs = postPicVOs;
		this.postMessageVOs = postMessageVOs;
		this.userVO = userVO;
		this.postCategoryVO = postCategoryVO;
	}


	
	public Integer getPostID() {
		return postID;
	}


	public void setPostID(Integer postID) {
		this.postID = postID;
	}


	public String getPostHeader() {
		return postHeader;
	}


	public void setPostHeader(String postHeader) {
		this.postHeader = postHeader;
	}


	public Timestamp getPostDateTime() {
		return postDateTime;
	}


	public void setPostDateTime(Timestamp postDateTime) {
		this.postDateTime = postDateTime;
	}


	public Timestamp getLastEditedTime() {
		return lastEditedTime;
	}


	public void setLastEditedTime(Timestamp lastEditedTime) {
		this.lastEditedTime = lastEditedTime;
	}


	public String getPostContent() {
		return postContent;
	}


	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}


	public Integer getDisLikeCount() {
		return disLikeCount;
	}


	public void setDisLikeCount(Integer disLikeCount) {
		this.disLikeCount = disLikeCount;
	}


	public Integer getLikeCount() {
		return likeCount;
	}


	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}


	public Integer getPostStatus() {
		return postStatus;
	}


	public void setPostStatus(Integer postStatus) {
		this.postStatus = postStatus;
	}


	public Set<ReportVO> getReportVOs() {
		return reportVOs;
	}


	public void setReportVOs(Set<ReportVO> reportVOs) {
		this.reportVOs = reportVOs;
	}


	public Set<LikesVO> getLikesVOs() {
		return likesVOs;
	}


	public void setLikesVOs(Set<LikesVO> likesVOs) {
		this.likesVOs = likesVOs;
	}


	public Set<PostPicVO> getPostPicVOs() {
		return postPicVOs;
	}


	public void setPostPicVOs(Set<PostPicVO> postPicVOs) {
		this.postPicVOs = postPicVOs;
	}


	public Set<PostMessageVO> getPostMessageVOs() {
		return postMessageVOs;
	}


	public void setPostMessageVOs(Set<PostMessageVO> postMessageVOs) {
		this.postMessageVOs = postMessageVOs;
	}


	public UserVO getUserVO() {
		return userVO;
	}


	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}


	public PostCategoryVO getPostCategoryVO() {
		return postCategoryVO;
	}


	public void setPostCategoryVO(PostCategoryVO postCategoryVO) {
		this.postCategoryVO = postCategoryVO;
	}


	@Override
	public String toString() {
		return "[postID=" + postID + ", postHeader=" + postHeader + ", postDateTime=" + postDateTime
				+ ", lastEditedTime=" + lastEditedTime + ", postContent=" + postContent + ", disLikeCount="
				+ disLikeCount + ", likeCount=" + likeCount + ", postStatus=" + postStatus + "]";
	}




	

}