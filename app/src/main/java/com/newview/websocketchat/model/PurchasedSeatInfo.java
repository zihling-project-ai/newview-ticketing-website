package com.newview.websocketchat.model;

public class PurchasedSeatInfo {
    private String userName;
    private String seatNumber;
    private String actID;
    private String seatType;
  

    @Override
	public String toString() {
		return "PurchasedSeatInfo [userName=" + userName + ", seatNumber=" + seatNumber + ", actID=" + actID
				+ ", seatType=" + seatType + "]";
	}


	public PurchasedSeatInfo(String userName, String seatNumber, String actID, String seatType) {
        this.userName = userName;
        this.seatNumber = seatNumber;
        this.actID = actID;
        this.seatType = seatType;
    }


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getSeatNumber() {
		return seatNumber;
	}


	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}


	public String getActID() {
		return actID;
	}


	public void setActID(String actID) {
		this.actID = actID;
	}


	public String getSeatType() {
		return seatType;
	}


	public void setSeatType(String seatType) {
		this.seatType = seatType;
	}

  
}
