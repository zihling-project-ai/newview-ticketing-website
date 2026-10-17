package com.newview.orderlist.controller;

public class OrderItemDTO {
	private int actID;
	private int pubID;
	private String rowColumn;
	private int actPrice;
	private int actCount;
	private int actPriceTotal;
	
	public OrderItemDTO() {
		super();

	}

	public OrderItemDTO(int actID, int pubID, String rowColumn, int actPrice, int actCount, int actPriceTotal) {
		super();
		this.actID = actID;
		this.pubID = pubID;
		this.rowColumn = rowColumn;
		this.actPrice = actPrice;
		this.actCount = actCount;
		this.actPriceTotal = actPriceTotal;
	}

	public int getActID() {
		return actID;
	}

	public void setActID(int actID) {
		this.actID = actID;
	}

	public int getPubID() {
		return pubID;
	}

	public void setPubID(int pubID) {
		this.pubID = pubID;
	}

	public String getRowColumn() {
		return rowColumn;
	}

	public void setRowColumn(String rowColumn) {
		this.rowColumn = rowColumn;
	}

	public int getActPrice() {
		return actPrice;
	}

	public void setActPrice(int actPrice) {
		this.actPrice = actPrice;
	}

	public int getActCount() {
		return actCount;
	}

	public void setActCount(int actCount) {
		this.actCount = actCount;
	}

	public int getActPriceTotal() {
		return actPriceTotal;
	}

	public void setActPriceTotal(int actPriceTotal) {
		this.actPriceTotal = actPriceTotal;
	}
	
	
	
	
}
