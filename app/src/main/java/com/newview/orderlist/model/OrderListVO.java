package com.newview.orderlist.model;

import java.sql.Timestamp;
import java.util.Arrays;
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
import com.newview.act.model.ActVO;
import com.newview.compic.model.ComPicVO;
import com.newview.orders.model.OrdersVO;

@Entity
@Table(name = "orderlist")
public class OrderListVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "orderListID")
	private Integer orderListID;

	@Expose
	@Column(name = "actTotal")
	private Integer actTotal;

	@Expose
	@Column(name = "QRcodeID", columnDefinition = "longblob")
	private byte[] QRcodeID;

	@Expose
	@Column(name = "OrderListTime", insertable = false)
	private Timestamp OrderListTime;

	@Expose
	@Column(name = "reviewContent")
	private String reviewContent;

	@Expose
	@Column(name = "fiveStarReview", columnDefinition = "tinyint")
	private Integer fiveStarReview;

	@Expose
	@Column(name = "seatRowsColumns", length = 32)
	private String seatRowsColumns;

	@Expose
	@Column(name = "usedType", columnDefinition = "tinyint")
	private Integer usedType;

	@ManyToOne
	@JoinColumn(name = "orderID", referencedColumnName = "orderID")
	private OrdersVO ordersVO;

	@Expose
	@ManyToOne
	@JoinColumn(name = "actID", referencedColumnName = "actID")
	private ActVO actVO;

//	@Expose
	@OneToMany(mappedBy = "orderListVO", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<ComPicVO> comPicVOs;

	public OrderListVO() {
		super();
	}

	public OrderListVO(Integer orderListID, Integer actTotal, byte[] qRcodeID, Timestamp orderListTime,
			String reviewContent, Integer fiveStarReview, String seatRowsColumns, Integer type,
			OrdersVO ordersVO, ActVO actVO, Set<ComPicVO> comPicVOs) {
		super();
		this.orderListID = orderListID;
		this.actTotal = actTotal;
		QRcodeID = qRcodeID;
		OrderListTime = orderListTime;
		this.reviewContent = reviewContent;
		this.fiveStarReview = fiveStarReview;
		this.seatRowsColumns = seatRowsColumns;
		this.usedType = type;
		this.ordersVO = ordersVO;
		this.actVO = actVO;
		this.comPicVOs = comPicVOs;
	}

	public Integer getOrderListID() {
		return orderListID;
	}

	public void setOrderListID(Integer orderListID) {
		this.orderListID = orderListID;
	}

	public Integer getActTotal() {
		return actTotal;
	}

	public void setActTotal(Integer actTotal) {
		this.actTotal = actTotal;
	}

	public byte[] getQRcodeID() {
		return QRcodeID;
	}

	public void setQRcodeID(byte[] qRcodeID) {
		QRcodeID = qRcodeID;
	}

	public Timestamp getOrderListTime() {
		return OrderListTime;
	}

	public void setOrderListTime(Timestamp orderListTime) {
		OrderListTime = orderListTime;
	}

	public String getReviewContent() {
		return reviewContent;
	}

	public void setReviewContent(String reviewContent) {
		this.reviewContent = reviewContent;
	}

	public Integer getFiveStarReview() {
		return fiveStarReview;
	}

	public void setFiveStarReview(Integer fiveStarReview) {
		this.fiveStarReview = fiveStarReview;
	}


	public String getSeatRowsColumns() {
		return seatRowsColumns;
	}

	public void setSeatRowsColumns(String seatRowsColumns) {
		this.seatRowsColumns = seatRowsColumns;
	}

	public Integer getType() {
		return usedType;
	}

	public void setType(Integer type) {
		this.usedType = type;
	}

	public OrdersVO getOrdersVO() {
		return ordersVO;
	}

	public void setOrdersVO(OrdersVO ordersVO) {
		this.ordersVO = ordersVO;
	}

	public ActVO getActVO() {
		return actVO;
	}

	public void setActVO(ActVO actVO) {
		this.actVO = actVO;
	}

	public Set<ComPicVO> getComPicVOs() {
		return comPicVOs;
	}

	public void setComPicVOs(Set<ComPicVO> comPicVOs) {
		this.comPicVOs = comPicVOs;
	}
	
	@Override
	public String toString() {
		return "OrderListVO [orderListID=" + orderListID + ", actTotal=" + actTotal + ", QRcodeID="
				+ Arrays.toString(QRcodeID) + ", OrderListTime=" + OrderListTime + ", reviewContent=" + reviewContent
				+ ", fiveStarReview=" + fiveStarReview + ", seatRowsColumns=" + seatRowsColumns 
				+ ", usedType=" + usedType + "]";
	}

}