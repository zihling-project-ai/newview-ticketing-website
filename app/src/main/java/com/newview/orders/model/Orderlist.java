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
public class Orderlist implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer orderListID;
	private Integer orderID;
	private Integer actTotal;
	private byte[] qrCodeId;
	private Timestamp orderListTime;
	private String reviewContent;
	private Integer fiveStarReview;
	private Integer usedType;
	private String seatRowsColumns;
	@ManyToOne
	@JoinColumn(name = "actID", insertable = false, updatable = false)
	private Act act;
	@OneToMany
	@JoinColumn(name = "orderListID", referencedColumnName = "orderListID")
	private List<ComPic> comPics;
	
	public Orderlist() {
	}

	public Orderlist(Integer orderListID, Integer orderID, Integer actTotal, byte[] qrCodeId, Timestamp orderListTime,
			String reviewContent, Integer fiveStarReview, Integer usedType, String seatRowsColumns, Act act,
			List<ComPic> comPics) {
		super();
		this.orderListID = orderListID;
		this.orderID = orderID;
		this.actTotal = actTotal;
		this.qrCodeId = qrCodeId;
		this.orderListTime = orderListTime;
		this.reviewContent = reviewContent;
		this.fiveStarReview = fiveStarReview;
		this.usedType = usedType;
		this.seatRowsColumns = seatRowsColumns;
		this.act = act;
		this.comPics = comPics;
	}

	public Integer getOrderListID() {
		return orderListID;
	}

	public void setOrderListID(Integer orderListID) {
		this.orderListID = orderListID;
	}

	public Integer getOrderID() {
		return orderID;
	}

	public void setOrderID(Integer orderID) {
		this.orderID = orderID;
	}

	public Integer getActTotal() {
		return actTotal;
	}

	public void setActTotal(Integer actTotal) {
		this.actTotal = actTotal;
	}

	public byte[] getQrCodeId() {
		return qrCodeId;
	}

	public void setQrCodeId(byte[] qrCodeId) {
		this.qrCodeId = qrCodeId;
	}

	public Timestamp getOrderListTime() {
		return orderListTime;
	}

	public void setOrderListTime(Timestamp orderListTime) {
		this.orderListTime = orderListTime;
	}

	public String getReviewContent() {
		return reviewContent;
	}

	public void setReviewContent(String reviewContent) {
		this.reviewContent = reviewContent;
	}

	public Integer getFiveStarReview() {
		return fiveStarReview;
	}

	public void setFiveStarReview(Integer fiveStarReview) {
		this.fiveStarReview = fiveStarReview;
	}

	public Integer getUsedType() {
		return usedType;
	}

	public void setUsedType(Integer usedType) {
		this.usedType = usedType;
	}

	public String getSeatRowsColumns() {
		return seatRowsColumns;
	}

	public void setSeatRowsColumns(String seatRowsColumns) {
		this.seatRowsColumns = seatRowsColumns;
	}

	public Act getAct() {
		return act;
	}

	public void setAct(Act act) {
		this.act = act;
	}

	public List<ComPic> getComPics() {
		return comPics;
	}

	public void setComPics(List<ComPic> comPics) {
		this.comPics = comPics;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}