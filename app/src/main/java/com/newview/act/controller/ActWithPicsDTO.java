package com.newview.act.controller;

import java.util.Date;
import java.util.List;

public class ActWithPicsDTO {
	private Integer actID;
	private Integer actScope;
    private String actName;
    private String actContent;
    private double actPrice;
    private String actIntroduce;
    private List<String> base64Images;
    private String actCategoryName;
    private Integer actCategoryID;
    private Date actDate;
    private Date actTime;
    private Date time;
    private String actAddress;
    private String cityAddress;
    private String pubName;
    private Integer approvalCondition;
    private List<ImageDTO> images;
    
	
    
    
	public Integer getActCategoryID() {
		return actCategoryID;
	}
	public void setActCategoryID(Integer actCategoryID) {
		this.actCategoryID = actCategoryID;
	}
	public Integer getApprovalCondition() {
		return approvalCondition;
	}
	public List<ImageDTO> getImages() {
		return images;
	}
	public void setImages(List<ImageDTO> images) {
		this.images = images;
	}
	
	public void setApprovalCondition(Integer approvalCondition) {
		this.approvalCondition = approvalCondition;
	}
	public String getPubName() {
		return pubName;
	}
	public void setPubName(String pubName) {
		this.pubName = pubName;
	}
	public String getCityAddress() {
		return cityAddress;
	}
	public void setCityAddress(String cityAddress) {
		this.cityAddress = cityAddress;
	}
	public String getActAddress() {
		return actAddress;
	}
	public void setActAddress(String actAddress) {
		this.actAddress = actAddress;
	}
	public Integer getActID() {
		return actID;
	}
	public Integer getActScope() {
		return actScope;
	}
	public String getActName() {
		return actName;
	}
	public String getActContent() {
		return actContent;
	}
	public double getActPrice() {
		return actPrice;
	}
	public String getActIntroduce() {
		return actIntroduce;
	}
	public List<String> getBase64Images() {
		return base64Images;
	}
	public String getActCategoryName() {
		return actCategoryName;
	}
	public Date getActDate() {
		return actDate;
	}
	public Date getTime() {
		return time;
	}
	public void setActID(Integer actID) {
		this.actID = actID;
	}
	public void setActScope(Integer actScope) {
		this.actScope = actScope;
	}
	public void setActName(String actName) {
		this.actName = actName;
	}
	public void setActContent(String actContent) {
		this.actContent = actContent;
	}
	public void setActPrice(double actPrice) {
		this.actPrice = actPrice;
	}
	public void setActIntroduce(String actIntroduce) {
		this.actIntroduce = actIntroduce;
	}
	public void setBase64Images(List<String> base64Images) {
		this.base64Images = base64Images;
	}
	public void setActCategoryName(String actCategoryName) {
		this.actCategoryName = actCategoryName;
	}
	public void setActDate(Date actDate) {
		this.actDate = actDate;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public ActWithPicsDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public ActWithPicsDTO(Integer actID, Integer actScope, String actName, String actContent, double actPrice,
			String actIntroduce, List<String> base64Images, String actCategoryName, Date actDate, Date time,
			String actAddress, String cityAddress, String pubName, Integer approvalCondition, Date actTime) {
		super();
		this.actID = actID;
		this.actScope = actScope;
		this.actName = actName;
		this.actContent = actContent;
		this.actPrice = actPrice;
		this.actIntroduce = actIntroduce;
		this.base64Images = base64Images;
		this.actCategoryName = actCategoryName;
		this.actDate = actDate;
		this.actTime = actTime;
		this.time = time;
		this.actAddress = actAddress;
		this.cityAddress = cityAddress;
		this.pubName = pubName;
		this.approvalCondition = approvalCondition;
	}
	public String getPublisherVOs() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getPubID() {
		// TODO Auto-generated method stub
		return null;
	}
	public Date getActTime() {
		return actTime;
	}
	public void setActTime(Date actTime) {
		this.actTime = actTime;
	}
	

    
   
}

