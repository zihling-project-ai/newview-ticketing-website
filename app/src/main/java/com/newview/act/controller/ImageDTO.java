package com.newview.act.controller;

public class ImageDTO {
	 private String base64Image;
	 private Integer actPicID;
	public ImageDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ImageDTO(String base64Image, Integer actPicID) {
		super();
		this.base64Image = base64Image;
		this.actPicID = actPicID;
	}
	public String getBase64Image() {
		return base64Image;
	}
	public Integer getActPicID() {
		return actPicID;
	}
	public void setBase64Image(String base64Image) {
		this.base64Image = base64Image;
	}
	public void setActPicID(Integer actPicID) {
		this.actPicID = actPicID;
	}
	 
	 
}
