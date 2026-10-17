package com.newview.postcategory.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.post.model.PostVO;

@Entity
@Table(name = "postcategory")
public class PostCategoryVO {
	@Id
	@Expose
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Integer postCategoryID;
	
	@Expose
	@Column(name = "postCategoryName")
	private String postCategoryName;
	
	@Expose
	@OneToMany(mappedBy = "postCategoryVO" , cascade = CascadeType.ALL)
	private Set<PostVO> postVOs;
	
	
	public PostCategoryVO() {
		super();
		// TODO Auto-generated constructor stub
	}


	public PostCategoryVO(Integer postCategoryID, String postCategoryName, Set<PostVO> postVOs) {
		super();
		this.postCategoryID = postCategoryID;
		this.postCategoryName = postCategoryName;
		this.postVOs = postVOs;
	}


	public Integer getPostCategoryID() {
		return postCategoryID;
	}


	public void setPostCategoryID(Integer postCategoryID) {
		this.postCategoryID = postCategoryID;
	}


	public String getPostCategoryName() {
		return postCategoryName;
	}


	public void setPostCategoryName(String postCategoryName) {
		this.postCategoryName = postCategoryName;
	}


	public Set<PostVO> getPostVOs() {
		return postVOs;
	}


	public void setPostVOs(Set<PostVO> postVOs) {
		this.postVOs = postVOs;
	}


	@Override
	public String toString() {
		return "PostCategoryVO [postCategoryID=" + postCategoryID + ", postCategoryName=" + postCategoryName + "]";
	}


	


	
	
	
}
