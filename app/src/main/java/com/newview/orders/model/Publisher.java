package com.newview.orders.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Publisher")
public class Publisher {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer pubID;
	private String pubName;
	private String pubEmail;
	public Publisher() {
		super();
	}
	public Publisher(Integer pubID, String pubName, String pubEmail) {
		super();
		this.pubID = pubID;
		this.pubName = pubName;
		this.pubEmail = pubEmail;
	}
	public Integer getPubID() {
		return pubID;
	}
	public void setPubID(Integer pubID) {
		this.pubID = pubID;
	}
	public String getPubName() {
		return pubName;
	}
	public void setPubName(String pubName) {
		this.pubName = pubName;
	}
	public String getPubEmail() {
		return pubEmail;
	}
	public void setPubEmail(String pubEmail) {
		this.pubEmail = pubEmail;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
