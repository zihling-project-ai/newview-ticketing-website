package com.newview.pubuser.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.publisher.model.PublisherVO;

@Entity
@Table(name="pubUser")
public class PubUserVO {
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="pubUserID" , updatable = false)
	private Integer pubUserID;
	
	@Expose
	@Column(name="pubNickname")
	private String pubNickname;
	
	@Expose
	@Column(name="pubAccount")
	private String pubAccount;
	
	@Expose
	@Column(name="pubPassword")
	private String pubPassword;
	
	@Expose
	@Column(name="pubAuthority",columnDefinition = "tinyint")
	private byte pubAuthority;
	
	@ManyToOne
	@JoinColumn(name="pubID" ,referencedColumnName ="pubID")
	private PublisherVO publisherVO;

	public Integer getPubUserID() {
		return pubUserID;
	}

	public void setPubUserID(Integer pubUserID) {
		this.pubUserID = pubUserID;
	}

	public String getPubNickname() {
		return pubNickname;
	}

	public void setPubNickname(String pubNickname) {
		this.pubNickname = pubNickname;
	}

	public String getPubAccount() {
		return pubAccount;
	}

	public void setPubAccount(String pubAccount) {
		this.pubAccount = pubAccount;
	}

	public String getPubPassword() {
		return pubPassword;
	}

	public void setPubPassword(String pubPassword) {
		this.pubPassword = pubPassword;
	}

	public byte getPubAuthority() {
		return pubAuthority;
	}

	public void setPubAuthority(byte pubAuthority) {
		this.pubAuthority = pubAuthority;
	}

	public PublisherVO getPublisherVO() {
		return publisherVO;
	}

	public void setPublisherVO(PublisherVO publisherVO) {
		this.publisherVO = publisherVO;
	}

	@Override
	public String toString() {
		return "PubUserVO [pubUserID=" + pubUserID + ", pubNickname=" + pubNickname + ", pubAccount=" + pubAccount
				+ ", pubPassword=" + "[REDACTED]" + ", pubAuthority=" + pubAuthority + "]";
	}

	

}
