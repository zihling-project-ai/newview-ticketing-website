package com.newview.act.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.newview.actcategory.model.ActCategory;
import com.newview.actpic.model.ActPic;
import com.newview.cartact.model.CartActVO;
import com.newview.cityaddress.model.CityAddress;
import com.newview.mylike.model.MyLikeVO;
import com.newview.orderlist.model.OrderListVO;
import com.newview.publisher.model.PublisherVO;

@Entity
@Table(name = "act")
// 配合 TestHQLWithParameter.java
@NamedQuery(name = "getAllActs", query = "from ActVO where actID > :actID order by actID desc")
public class ActVO {
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "actID", updatable = false)
	private Integer actID;
	@Expose
	@Column(name = "actName")
	private String actName;
	@Expose
	@Column(name = "actPrice")
	private Integer actPrice;

//	@Temporal 
//	1.用來指定型態為java.util.Date, java.util.Calendar的mapping.
//	2.TemporalType, 有DATE(mapping到java.sql.Date)、TIME(mapping到java.sql.Time)、TIMESTAMP(mapping到java.sql.Timestamp)(預設值)
//	3.如果型態本來就是java.sql.Date, java.sql.Time, java.sql.Timestamp, 就不用@Temporal 
//	4.這範例有 import java.util.Date; --> 是故意用 java.util.Date 做展示，所以一定要加 @Temporal
	@Expose
	@Temporal(TemporalType.DATE)
	@Column(name = "actTime")
	private Date actTime;
	@Expose
	@Column(name = "actScope")
	private Integer actScope;
	@Expose
	@Column(name = "actIntroduce")
	private String actIntroduce;
	@Expose
	@Column(columnDefinition = "text")
	private String actContent;
	@Expose
	@Temporal(TemporalType.TIME)
	@Column(name = "time")
	private Date time;
	@Expose
	@Temporal(TemporalType.DATE)
	@Column(name = "actDate")
	private Date actDate;

	// 搭配TestHQLQueryProperty.java
	@Expose
	@Column(columnDefinition = "TINYINT")
	private Integer approvalCondition;
	@Expose
	@Column(name = "cityAddress")
	private String cityAddress;
	
	
	
	/*外來鍵相關表格*/
	@ManyToOne
	@JoinColumn(name = "actCategoryID",referencedColumnName = "actCategoryID")
	@JsonIgnore
	private ActCategory actCategory;

	@ManyToOne
	@JoinColumn(name = "cityAddressID",referencedColumnName = "cityAddressID")
	@JsonIgnore
	private CityAddress city;
	
	
	@ManyToOne
	@JoinColumn(name = "pubID",referencedColumnName = "pubID")
	@JsonIgnore
	private PublisherVO publisherVO;
	
	
	/*主鍵相關表格*/
	@Expose
	@OneToMany(mappedBy = "act", cascade=CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<ActPic> actpics;
//	@Expose
	@OneToMany(mappedBy = "actVO", cascade=CascadeType.ALL)
	private Set<OrderListVO> orderListVOs;
	@Expose
	@OneToMany(mappedBy = "actVO", cascade=CascadeType.ALL)
	private Set<MyLikeVO> myLikeVOs;
	@Expose
	@OneToMany(mappedBy = "actVO", cascade=CascadeType.ALL)
	private Set<CartActVO> cartActVOs;
	
	
//	@OneToMany(mappedBy = "act")
//    private List<ActPic> actPics;
//以上為資料設定  以下為方法

	
	@Override
	public String toString() {
		return "ActVO [actID=" + actID + ", actName=" + actName + ", actPrice=" + actPrice + ", actTime=" + actTime
				+ ", actScope=" + actScope + ", actIntroduce=" + actIntroduce + ", actContent=" + actContent + ", time="
				+ time + ", actDate=" + actDate + ", approvalCondition=" + approvalCondition + ", cityAddress="
				+ cityAddress + ", actCategory=" + actCategory + ", city=" + city + ", publisherVO=" + publisherVO
				+ ", actpics=" + actpics + ", orderListVOs=" + orderListVOs + ", myLikeVOs=" + myLikeVOs
				+ ", cartActVOs=" + cartActVOs + "]";
	}
	

	public ActVO() {
	}



	public CityAddress getCity() {
		return city;
	}

	public PublisherVO getPublisherVO() {
		return publisherVO;
	}

	public Set<MyLikeVO> getMyLikeVOs() {
		return myLikeVOs;
	}

	

	public void setCity(CityAddress city) {
		this.city = city;
	}

	public void setPublisherVO(PublisherVO publisherVO) {
		this.publisherVO = publisherVO;
	}

	public void setMyLikeVOs(Set<MyLikeVO> myLikeVOs) {
		this.myLikeVOs = myLikeVOs;
	}

	public ActVO(Integer actID, String actName, Integer actPrice, Date actTime, Integer actScope, String actIntroduce,
			String actContent, Date time, Date actDate, Integer approvalCondition, String cityAddress,
			ActCategory actCategory, CityAddress city, PublisherVO publisherVO, Set<ActPic> actpics) {
		super();
		this.actID = actID;
		this.actName = actName;
		this.actPrice = actPrice;
		this.actTime = actTime;
		this.actScope = actScope;
		this.actIntroduce = actIntroduce;
		this.actContent = actContent;
		this.time = time;
		this.actDate = actDate;
		this.approvalCondition = approvalCondition;
		this.cityAddress = cityAddress;
		this.actCategory = actCategory;
		this.city = city;
		this.publisherVO = publisherVO;
		this.actpics = actpics;
	}

	public Integer getActID() {
		return actID;
	}

	public String getActName() {
		return actName;
	}

	public Integer getActPrice() {
		return actPrice;
	}

	public Date getActTime() {
		return actTime;
	}

	public Integer getActScope() {
		return actScope;
	}

	public String getActIntroduce() {
		return actIntroduce;
	}

	public String getActContent() {
		return actContent;
	}

	public Date getTime() {
		return time;
	}

	public Date getActDate() {
		return actDate;
	}

	public Integer getApprovalCondition() {
		return approvalCondition;
	}

	public String getCityAddress() {
		return cityAddress;
	}

	public ActCategory getActCategoryID() {
		return actCategory;
	}


	public CityAddress getCityAddressID() {
		return city;
	}

	public void setActID(Integer actID) {
		this.actID = actID;
	}

	public void setActName(String actName) {
		this.actName = actName;
	}

	public void setActPrice(Integer actPrice) {
		this.actPrice = actPrice;
	}

	public void setActTime(Date actTime) {
		this.actTime = actTime;
	}

	public void setActScope(Integer actScope) {
		this.actScope = actScope;
	}

	public void setActIntroduce(String actIntroduce) {
		this.actIntroduce = actIntroduce;
	}

	public void setActContent(String actContent) {
		this.actContent = actContent;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setActDate(Date actDate) {
		this.actDate = actDate;
	}

	public void setApprovalCondition(Integer approvalCondition) {
		this.approvalCondition = approvalCondition;
	}

	public void setCityAddress(String cityAddress) {
		this.cityAddress = cityAddress;
	}

	

	public ActCategory getActCategory() {
		return actCategory;
	}

	public void setActCategory(ActCategory actCategory) {
		this.actCategory = actCategory;
	}


	public PublisherVO getPublisherVOs() {
		return publisherVO;
	}

	public void setPublisherVOs(PublisherVO publisherVOs) {
		this.publisherVO = publisherVOs;
	}

	public void setCityAddressID(CityAddress cityAddressID) {
		this.city = cityAddressID;
	}

	public void setActCategory(String actCategoryName) {
		// TODO Auto-generated method stub
		
	}

	public Set<ActPic> getActpics() {
		return actpics;
	}

	public void setActpics(Set<ActPic> actpics) {
		this.actpics = actpics;
	}

	public void setPublisherVOs(String pubName) {
		// TODO Auto-generated method stub
		
	}

	public Set<OrderListVO> getOrderListVOs() {
		return orderListVOs;
	}

	public void setOrderListVOs(Set<OrderListVO> orderListVOs) {
		this.orderListVOs = orderListVOs;
	}

	public Set<CartActVO> getCartActVOs() {
		return cartActVOs;
	}

	public void setCartActVOs(Set<CartActVO> cartActVOs) {
		this.cartActVOs = cartActVOs;
	}
}
