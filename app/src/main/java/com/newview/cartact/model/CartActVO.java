package com.newview.cartact.model;

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
@Table(name = "cartact")
public class CartActVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cartActID")
	private Integer cartActID;

//	@Expose
	@Column(name = "cartQuantity")
	private Integer cartQuantity;

	@ManyToOne
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;

	@ManyToOne
	@JoinColumn(name = "actID", referencedColumnName = "actID")
	private ActVO actVO;

	public CartActVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CartActVO(Integer cartActID, Integer cartQuantity, UserVO userVO, ActVO actVO) {
		super();
		this.cartActID = cartActID;
		this.cartQuantity = cartQuantity;
		this.userVO = userVO;
		this.actVO = actVO;
	}

	public Integer getCartActID() {
		return cartActID;
	}

	public void setCartActID(Integer cartActID) {
		this.cartActID = cartActID;
	}

	public Integer getCartQuantity() {
		return cartQuantity;
	}

	public void setCartQuantity(Integer cartQuantity) {
		this.cartQuantity = cartQuantity;
	}

	public UserVO getUserVO() {
		return userVO;
	}

	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

	public ActVO getActVO() {
		return actVO;
	}

	public void setActVO(ActVO actVO) {
		this.actVO = actVO;
	}

	@Override
	public String toString() {
		return "CartActVO [cartActID=" + cartActID + ", cartQuantity=" + cartQuantity + "]";
	}

	
	

}