package com.newview.publisher.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.act.model.ActVO;
import com.newview.discount.model.DiscountVO;
import com.newview.orders.model.OrdersVO;
import com.newview.pubuser.model.PubUserVO;

@Entity
@Table(name="Publisher")
public class PublisherVO {
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="pubID" , updatable = false)
	private Integer pubID;
	
	@Expose
	@Column(name="pubName")
	private String pubName;
	
	@Expose
	@Column(name="pubEmail")
	private String pubEmail;

	@Expose
	@OneToMany(mappedBy = "publisherVO", cascade = CascadeType.ALL)
	@OrderBy("pubUserID")
	private Set<PubUserVO> pubUserVOs;
	
	@Expose
	@OneToMany(mappedBy = "publisherVO", cascade = CascadeType.ALL)
	private Set<DiscountVO> discountVOs;
	
	@Expose
	@OneToMany(mappedBy = "publisherVO", cascade = CascadeType.ALL)
	private Set<OrdersVO> ordersVOs;
	
	@Expose
	@OneToMany(mappedBy = "publisherVO", cascade = CascadeType.ALL)
	private Set<ActVO> actVOs;

	public PublisherVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getPubID() {
		return pubID;
	}

	public void setPubID(Integer pubID) {
		this.pubID = pubID;
	}

	public String getPubName() {
		return pubName;
	}

	public void setPubName(String pubName) {
		this.pubName = pubName;
	}

	public String getPubEmail() {
		return pubEmail;
	}

	public void setPubEmail(String pubEmail) {
		this.pubEmail = pubEmail;
	}

	public Set<PubUserVO> getPubUserVOs() {
		return pubUserVOs;
	}

	public void setPubUserVOs(Set<PubUserVO> pubUserVOs) {
		this.pubUserVOs = pubUserVOs;
	}

	public Set<DiscountVO> getDiscountVOs() {
		return discountVOs;
	}

	public void setDiscountVOs(Set<DiscountVO> discountVOs) {
		this.discountVOs = discountVOs;
	}

	public Set<OrdersVO> getOrdersVOs() {
		return ordersVOs;
	}

	public void setOrdersVOs(Set<OrdersVO> ordersVOs) {
		this.ordersVOs = ordersVOs;
	}

	public Set<ActVO> getActVOs() {
		return actVOs;
	}

	public void setActVOs(Set<ActVO> actVOs) {
		this.actVOs = actVOs;
	}

	@Override
	public String toString() {
		return "PublisherVO [pubID=" + pubID + ", pubName=" + pubName + ", pubEmail=" + pubEmail + "]";
	}


	
	
}
