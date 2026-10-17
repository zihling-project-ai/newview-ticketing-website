package com.newview.usediscount.model;

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
import com.newview.discount.model.DiscountVO;
import com.newview.user.model.UserVO;

@Entity
@Table(name = "useDiscount")
//配合 HQL
//@NamedQuery(name = "")
public class UseDiscountVO{
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "useDisID")
	private Integer useDisID;
	
	@Expose
	@Column(name = "ditUsed", columnDefinition = "TINYINT")
	private Integer ditUsed;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discountNO", referencedColumnName = "discountNO")
	private DiscountVO discountVO;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;

	public UseDiscountVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UseDiscountVO(Integer useDisID, Integer ditUsed, DiscountVO discountVO, UserVO userVO) {
		super();
		this.useDisID = useDisID;
		this.ditUsed = ditUsed;
		this.discountVO = discountVO;
		this.userVO = userVO;
	}

	public Integer getUseDisID() {
		return useDisID;
	}

	public void setUseDisID(Integer useDisID) {
		this.useDisID = useDisID;
	}

	public Integer getDitUsed() {
		return ditUsed;
	}

	public void setDitUsed(Integer ditUsed) {
		this.ditUsed = ditUsed;
	}

	public DiscountVO getDiscountVO() {
		return discountVO;
	}

	public void setDiscountVO(DiscountVO discountVO) {
		this.discountVO = discountVO;
	}

	public UserVO getUserVO() {
		return userVO;
	}

	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

	@Override
	public String toString() {
		return "UseDiscountVO [useDisID=" + useDisID + ", ditUsed=" + ditUsed + "]";
	}

	


	

}
