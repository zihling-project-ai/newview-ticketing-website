package com.newview.orders.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class ComPic implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer comPicID;
	private Integer orderListID;
	private byte[] comPic;
	@Transient
	private String comPicBase64;
	
	public ComPic() {
	}

	public ComPic(Integer comPicID, Integer orderListID, byte[] comPic) {
		this.comPicID = comPicID;
		this.orderListID = orderListID;
		this.comPic = comPic;
	}

	public Integer getComPicID() {
		return comPicID;
	}

	public void setComPicID(Integer comPicID) {
		this.comPicID = comPicID;
	}

	public Integer getOrderListID() {
		return orderListID;
	}

	public void setOrderListID(Integer orderListID) {
		this.orderListID = orderListID;
	}

	public byte[] getComPic() {
		return comPic;
	}

	public void setComPic(byte[] comPic) {
		this.comPic = comPic;
	}

	public String getComPicBase64() {
		return comPicBase64;
	}

	public void setComPicBase64(String comPicBase64) {
		this.comPicBase64 = comPicBase64;
	}
}