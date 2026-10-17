package com.newview.orders.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Orders implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer orderID;
	private Integer userID;
	private Integer ordTotal;
	private Integer discount;
	private Integer discountPrice;
	private Timestamp ordTime;
	private Integer ordType;
	private Integer actQuantity;
	@ManyToOne
	@JoinColumn(name = "pubID", insertable = false, updatable = false)
	private Publisher publisher;
	@OneToMany
	@JoinColumn(name = "orderID", referencedColumnName = "orderID")
	private List<Orderlist> orderlists;
	
	public Orders() {
	}

	public Orders(Integer orderID, Integer userID, Integer ordTotal, Integer discount, Integer discountPrice,
			Timestamp ordTime, Integer ordType, Integer actQuantity, Publisher publisher, List<Orderlist> orderlists) {
		super();
		this.orderID = orderID;
		this.userID = userID;
		this.ordTotal = ordTotal;
		this.discount = discount;
		this.discountPrice = discountPrice;
		this.ordTime = ordTime;
		this.ordType = ordType;
		this.actQuantity = actQuantity;
		this.publisher = publisher;
		this.orderlists = orderlists;
	}

	public Integer getOrderID() {
		return orderID;
	}

	public void setOrderID(Integer orderID) {
		this.orderID = orderID;
	}

	public Integer getUserID() {
		return userID;
	}

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public Integer getOrdTotal() {
		return ordTotal;
	}

	public void setOrdTotal(Integer ordTotal) {
		this.ordTotal = ordTotal;
	}

	public Integer getDiscount() {
		return discount;
	}

	public void setDiscount(Integer discount) {
		this.discount = discount;
	}

	public Integer getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(Integer discountPrice) {
		this.discountPrice = discountPrice;
	}

	public Timestamp getOrdTime() {
		return ordTime;
	}

	public void setOrdTime(Timestamp ordTime) {
		this.ordTime = ordTime;
	}

	public Integer getOrdType() {
		return ordType;
	}

	public void setOrdType(Integer ordType) {
		this.ordType = ordType;
	}

	public Integer getActQuantity() {
		return actQuantity;
	}

	public void setActQuantity(Integer actQuantity) {
		this.actQuantity = actQuantity;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public List<Orderlist> getOrderlists() {
		return orderlists;
	}

	public void setOrderlists(List<Orderlist> orderlists) {
		this.orderlists = orderlists;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
		
}
