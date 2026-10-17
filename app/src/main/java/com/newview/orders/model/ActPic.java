package com.newview.orders.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "actpic2")
@Table(name = "actpic")
public class ActPic implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer actPicID;
	private Integer actID;
	private byte[] actPic;
	
	public ActPic() {
	}

	public ActPic(Integer actPicID, Integer actID, byte[] actPic) {
		this.actPicID = actPicID;
		this.actID = actID;
		this.actPic = actPic;
	}

	public Integer getActPicID() {
		return actPicID;
	}

	public void setActPicID(Integer actPicID) {
		this.actPicID = actPicID;
	}

	public Integer getActID() {
		return actID;
	}

	public void setActID(Integer actID) {
		this.actID = actID;
	}

	public byte[] getActPic() {
		return actPic;
	}

	public void setActPic(byte[] actPic) {
		this.actPic = actPic;
	}
}
