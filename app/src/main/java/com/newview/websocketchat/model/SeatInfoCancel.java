package com.newview.websocketchat.model;

public class SeatInfoCancel {
	private int sessionID;
	private String userName;
	private String userType;

	public SeatInfoCancel(int sessionID, String userName, String userType) {
		this.userName = userName;
		this.sessionID = sessionID;
		this.userType = userType;
	}

	public int getSessionID() {
		return sessionID;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserType() {
		return userType;
	}
}
