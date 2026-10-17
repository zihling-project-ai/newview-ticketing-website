package com.newview.act.controller;

import java.sql.Date;

public class UserDTO {
	private Integer userID;
	private String userName;
	private String userAccount;
	private Date userBirth;
	private String userCell;
	private String userEmail;
	private String userNickname;
	private Integer buyAuthority;
	private Integer speakAuthority;
	
	
	
	
	public UserDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public Date getUserBirth() {
		return userBirth;
	}
	public void setUserBirth(Date userBirth) {
		this.userBirth = userBirth;
	}
	public String getUserCell() {
		return userCell;
	}
	public void setUserCell(String userCell) {
		this.userCell = userCell;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getUserNickname() {
		return userNickname;
	}
	public void setUserNickname(String userNickname) {
		this.userNickname = userNickname;
	}
	public Integer getBuyAuthority() {
		return buyAuthority;
	}
	public void setBuyAuthority(Integer buyAuthority) {
		this.buyAuthority = buyAuthority;
	}
	public Integer getSpeakAuthority() {
		return speakAuthority;
	}
	public void setSpeakAuthority(Integer speakAuthority) {
		this.speakAuthority = speakAuthority;
	}
	@Override
	public String toString() {
		return "UserDTO [userID=" + userID + ", userName=" + userName + ", userAccount=" + userAccount + ", userBirth="
				+ userBirth + ", userCell=" + userCell + ", userEmail=" + userEmail + ", userNickname=" + userNickname
				+ ", buyAuthority=" + buyAuthority + ", speakAuthority=" + speakAuthority + "]";
	}
	public UserDTO(Integer userID, String userName, String userAccount, Date userBirth, String userCell,
			String userEmail, String userNickname, Integer buyAuthority, Integer speakAuthority) {
		super();
		this.userID = userID;
		this.userName = userName;
		this.userAccount = userAccount;
		this.userBirth = userBirth;
		this.userCell = userCell;
		this.userEmail = userEmail;
		this.userNickname = userNickname;
		this.buyAuthority = buyAuthority;
		this.speakAuthority = speakAuthority;
	}
	
	
}
