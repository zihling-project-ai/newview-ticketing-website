package com.newview.postpic.model;

import java.util.Arrays;

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

@Entity
@Table(name = "postpic")
public class PostPicVO{
	@Id
	@Expose
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Integer postPicID;
	
	@Expose
	@Column(name = "postpic", columnDefinition = "longblob")
	private byte[] postPic;
	
	@ManyToOne
	@JoinColumn(name = "postID" , referencedColumnName ="postID")
	private PostVO postVO;
	
	
	public PostPicVO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public PostPicVO(Integer postPicID, byte[] postPic) {
		super();
		this.postPicID = postPicID;
		this.postPic = postPic;
	}

	public Integer getPostPicID() {
		return postPicID;
	}
	public void setPostPicID(Integer postPicID) {
		this.postPicID = postPicID;
	}

	
	public PostVO getPost() {
		return postVO;
	}

	public void setPost(PostVO post) {
		this.postVO = post;
	}

	public byte[] getPostPic() {
		return postPic;
	}
	public void setPostPic(byte[] postPic) {
		this.postPic = postPic;
	}

	@Override
	public String toString() {
		return "PostPicVO [postPicID=" + postPicID + ", postPic=" + Arrays.toString(postPic) + "]";
	}

	
	

}
