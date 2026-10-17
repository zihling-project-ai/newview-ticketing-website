package com.newview.websocketchat.model;

public class Message {
	private String userName;
	private String seatNumber;
	private String type;
	private String actID;



	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}

	public void setType(String type) {
		this.type = type;
	}


	public String getActID() {
		return actID;
	}

	public void setActID(String actID) {
		this.actID = actID;
	}

	public String getUserName() {
		return userName;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public String getType() {
		return type;
	}
}
