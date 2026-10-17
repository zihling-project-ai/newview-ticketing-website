package com.newview.websocketchat.model;

public class SeatInfo {
	private String userName;
	private String seatNumber;
	private String seatType;
	private String actID;
	public SeatInfo(String userName, String seatNumber, String seatType,String actID) {
		this.userName = userName;
		this.seatNumber = seatNumber;
		this.seatType = seatType;
		this.actID = actID;
	}

	@Override
	public String toString() {
		return "SeatInfo [userName=" + userName + ", seatNumber=" + seatNumber + ", seatType=" + seatType + ", actID="
				+ actID + "]";
	}

	public String getUserName() {
		return userName;
	}

	public String getSeatType() {
		return seatType;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}

	public void setSeatType(String seatType) {
		this.seatType = seatType;
	}

	public String getActID() {
        return actID;
    }

	public void setActID(String actID) {
		this.actID = actID;
	}

	
}
