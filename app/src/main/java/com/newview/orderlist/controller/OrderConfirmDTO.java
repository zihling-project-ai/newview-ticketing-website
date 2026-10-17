package com.newview.orderlist.controller;

public class OrderConfirmDTO {
	private int orderID;
	private String seatRowsColumns;
	private int actID;
	private int discountPrice;
	private int discount;
	private int actCount;
	private int userID;
	private String actName;
	private int pubID;
	private int actPriceTotal;
	private int actPrice;
	
	public OrderConfirmDTO() {
		super();
		// TODO Auto-generated constructor stub
	}



	public OrderConfirmDTO(int orderID, String seatRowsColumns, int actID, int discountPrice, int discount,
			int actCount, int userID, String actName, int pubID, int actPriceTotal, int actPrice) {
		super();
		this.orderID = orderID;
		this.seatRowsColumns = seatRowsColumns;
		this.actID = actID;
		this.discountPrice = discountPrice;
		this.discount = discount;
		this.actCount = actCount;
		this.userID = userID;
		this.actName = actName;
		this.pubID = pubID;
		this.actPriceTotal = actPriceTotal;
		this.actPrice = actPrice;
	}


	public int getOrderID() {
		return orderID;
	}


	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}


	public String getSeatRowsColumns() {
		return seatRowsColumns;
	}


	public void setSeatRowsColumns(String seatRowsColumns) {
		this.seatRowsColumns = seatRowsColumns;
	}


	public int getActID() {
		return actID;
	}


	public void setActID(int actID) {
		this.actID = actID;
	}


	public int getDiscountPrice() {
		return discountPrice;
	}


	public void setDiscountPrice(int discountPrice) {
		this.discountPrice = discountPrice;
	}


	public int getDiscount() {
		return discount;
	}


	public void setDiscount(int discount) {
		this.discount = discount;
	}


	public int getActCount() {
		return actCount;
	}


	public void setActCount(int actCount) {
		this.actCount = actCount;
	}


	public int getUserID() {
		return userID;
	}


	public void setUserID(int userID) {
		this.userID = userID;
	}


	public String getActName() {
		return actName;
	}


	public void setActName(String actName) {
		this.actName = actName;
	}


	public int getPubID() {
		return pubID;
	}


	public void setPubID(int pubID) {
		this.pubID = pubID;
	}


	public int getActPriceTotal() {
		return actPriceTotal;
	}


	public void setActPriceTotal(int actPriceTotal) {
		this.actPriceTotal = actPriceTotal;
	}



	public int getActPrice() {
		return actPrice;
	}



	public void setActPrice(int actPrice) {
		this.actPrice = actPrice;
	}
	
	
}
