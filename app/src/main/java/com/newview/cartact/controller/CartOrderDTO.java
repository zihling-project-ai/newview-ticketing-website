package com.newview.cartact.controller;

import java.util.List;

public class CartOrderDTO {
	private int pubID;
	private String pubName;
	private int actID;
	private int scope;
	private String actName;
	private List<String> rowColumn;
	private int actPrice;
	private int actCount;
	private int actPriceTotal;
	private String seatStatus;
	
	public CartOrderDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CartOrderDTO(int pubID, String pubName, int actID, int scope, String actName, int actPrice,
			int actCount, int actPriceTotal, String seatStatus) {
		super();
		this.pubID = pubID;
		this.pubName = pubName;
		this.actID = actID;
		this.scope = scope;
		this.actName = actName;
		this.actPrice = actPrice;
		this.actCount = actCount;
		this.actPriceTotal = actPriceTotal;
		this.seatStatus = seatStatus;
	}

	public int getPubID() {
		return pubID;
	}

	public void setPubID(int pubID) {
		this.pubID = pubID;
	}

	public String getPubName() {
		return pubName;
	}

	public void setPubName(String pubName) {
		this.pubName = pubName;
	}

	public int getActID() {
		return actID;
	}

	public void setActID(int actID) {
		this.actID = actID;
	}

	public int getScope() {
		return scope;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public String getActName() {
		return actName;
	}

	public void setActName(String actName) {
		this.actName = actName;
	}

	public List<String> getRowColumn() {
		return rowColumn;
	}

	public void setRowColumn(List<String> rowColumn) {
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

	public String getSeatStatus() {
		return seatStatus;
	}

	public void setSeatStatus(String seatStatus) {
		this.seatStatus = seatStatus;
	}

	@Override
	public String toString() {
		return "CartOrderDTO [pubID=" + pubID + ", pubName=" + pubName + ", actID=" + actID + ", scope=" + scope
				+ ", actName=" + actName + ", rowColumn=" + rowColumn + ", actPrice=" + actPrice + ", actCount="
				+ actCount + ", actPriceTotal=" + actPriceTotal + ", seatStatus=" + seatStatus + "]";
	}
	
	
	
}
