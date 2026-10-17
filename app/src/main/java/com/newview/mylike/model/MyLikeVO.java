package com.newview.mylike.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.act.model.ActVO;
import com.newview.user.model.UserVO;


@Entity
@Table(name = "myLike")
public class MyLikeVO{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "myLikeID")
	private Integer myLikeID;
	
	
	@ManyToOne
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;	
	
	
	@ManyToOne
	@JoinColumn(name = "actID", referencedColumnName = "actID")
	private ActVO actVO;		
	
	public MyLikeVO() {
		super();
	}
	
	public MyLikeVO(Integer myLikeID, UserVO userVO, ActVO actVO) {
		super();
		this.myLikeID = myLikeID;
		this.userVO = userVO;
		this.actVO = actVO;
	}
	
	
	public Integer getMyLikeID() {
		return myLikeID;
	}
	public void setMyLikeID(Integer myLikeID) {
		this.myLikeID = myLikeID;
	}
	
	
	public UserVO getUser() {
		return userVO;
	}
	
	public void setUser(UserVO userVO) {
		this.userVO = userVO;
	}
	
	public ActVO getAct() {
		return actVO;
	}
	
	public void setAct(ActVO actVO) {
		this.actVO = actVO;
	}

	@Override
	public String toString() {
		return "MyLikeVO [myLikeID=" + myLikeID + "]";
	}
	
	
	
	
}