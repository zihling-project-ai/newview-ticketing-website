package com.newview.discount.model;

import java.sql.Timestamp;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.admin.model.AdminVO;
import com.newview.orderlist.model.OrderListVO;
import com.newview.orders.model.OrdersVO;
import com.newview.publisher.model.PublisherVO;
import com.newview.usediscount.model.UseDiscountVO;

@Entity
@Table(name = "discount")
public class DiscountVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "discountNO")
	private Integer discountNO;

	@Expose
	@Column(name = "discountContent")
	private String discountContent;

	@Expose
	@Column(name = "disAmount")
	private Integer disAmount;

	@Expose
	@Column(name = "discountCode")
	private String discountCode;

	@Expose
	@Column(name = "disStartDate")
	private Timestamp disStartDate;

	@Expose
	@Column(name = "disFinishDate")
	private Timestamp disFinishDate;

	@Expose
	@OneToMany(mappedBy = "discountVO", fetch = FetchType.EAGER)
	private Set<UseDiscountVO> useDiscountVOs;

	@Expose
	@OneToMany(mappedBy = "discountVO", cascade = CascadeType.ALL)
	private Set<OrdersVO> ordersVOs;

	@ManyToOne
	@JoinColumn(name = "pubID", referencedColumnName = "pubID")
	private PublisherVO publisherVO;

	@ManyToOne
	@JoinColumn(name = "adminID", referencedColumnName = "adminID")
	private AdminVO adminVO;

	public DiscountVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DiscountVO(Integer discountNO, String discountContent, Integer disAmount, String discountCode,
			Timestamp disStartDate, Timestamp disFinishDate, Set<UseDiscountVO> useDiscountVOs, Set<OrdersVO> ordersVOs,
			PublisherVO publisherVO, AdminVO adminVO) {
		super();
		this.discountNO = discountNO;
		this.discountContent = discountContent;
		this.disAmount = disAmount;
		this.discountCode = discountCode;
		this.disStartDate = disStartDate;
		this.disFinishDate = disFinishDate;
		this.useDiscountVOs = useDiscountVOs;
		this.ordersVOs = ordersVOs;
		this.publisherVO = publisherVO;
		this.adminVO = adminVO;
	}

	public Integer getDiscountNO() {
		return discountNO;
	}

	public void setDiscountNO(Integer discountNO) {
		this.discountNO = discountNO;
	}

	public String getDiscountContent() {
		return discountContent;
	}

	public void setDiscountContent(String discountContent) {
		this.discountContent = discountContent;
	}

	public Integer getDisAmount() {
		return disAmount;
	}

	public void setDisAmount(Integer disAmount) {
		this.disAmount = disAmount;
	}

	public String getDiscountCode() {
		return discountCode;
	}

	public void setDiscountCode(String discountCode) {
		this.discountCode = discountCode;
	}

	public Date getDisStartDate() {
		return disStartDate;
	}

	public void setDisStartDate(Timestamp disStartDate) {
		this.disStartDate = disStartDate;
	}

	public Date getDisFinishDate() {
		return disFinishDate;
	}

	public void setDisFinishDate(Timestamp disFinishDate) {
		this.disFinishDate = disFinishDate;
	}

	public Set<UseDiscountVO> getUseDiscountVOs() {
		return useDiscountVOs;
	}

	public void setUseDiscountVOs(Set<UseDiscountVO> useDiscountVOs) {
		this.useDiscountVOs = useDiscountVOs;
	}

	public Set<OrdersVO> getOrdersVOs() {
		return ordersVOs;
	}

	public void setOrdersVOs(Set<OrdersVO> ordersVOs) {
		this.ordersVOs = ordersVOs;
	}

	public PublisherVO getPublisherVO() {
		return publisherVO;
	}

	public void setPublisherVO(PublisherVO publisherVO) {
		this.publisherVO = publisherVO;
	}

	public AdminVO getAdminVO() {
		return adminVO;
	}

	public void setAdminVO(AdminVO adminVO) {
		this.adminVO = adminVO;
	}

	@Override
	public String toString() {
		return "DiscountVO [discountNO=" + discountNO + ", discountContent=" + discountContent + ", disAmount="
				+ disAmount + ", discountCode=" + discountCode + ", disStartDate=" + disStartDate + ", disFinishDate="
				+ disFinishDate + "]";
	}

	
	

}
