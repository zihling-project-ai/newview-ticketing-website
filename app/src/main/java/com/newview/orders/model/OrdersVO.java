package com.newview.orders.model;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.discount.model.DiscountVO;
import com.newview.orderlist.model.OrderListVO;
import com.newview.publisher.model.PublisherVO;
import com.newview.user.model.UserVO;

@Entity
@Table(name = "orders")

public class OrdersVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "orderID")
	private Integer orderID;

	@Expose
	@Column(name = "ordTotal")
	private Integer ordTotal;

	@Expose
	@Column(name = "discount")
	private Integer discount;

	@Expose
	@Column(name = "discountPrice")
	private Integer discountPrice;

	@Expose
	@Column(name = "ordTime", insertable=false)
	private Timestamp ordTime;

	@Expose
	@Column(name = "ordType", columnDefinition = "tinyint")
	private Integer ordType; // 0 = not use, 1 = used, 2 = cancelled order

	@Expose
	@Column(name = "actQuantity")
	private Integer actQuantity;

	@Expose
	@OneToMany(mappedBy = "ordersVO", cascade = CascadeType.ALL)
	private Set<OrderListVO> orderListVOs;

	@ManyToOne
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;

	@ManyToOne
	@JoinColumn(name = "pubID", referencedColumnName = "pubID")
	private PublisherVO publisherVO;
	
	@ManyToOne
	@JoinColumn(name = "discountNO", referencedColumnName = "discountNO")
	private DiscountVO discountVO;
	

	public OrdersVO() {
		super();
	}


	public OrdersVO(Integer orderID, Integer ordTotal, Integer discount, Integer discountPrice, Timestamp ordTime,
			Integer ordType, Integer actQuantity, Set<OrderListVO> orderListVOs, UserVO userVO, PublisherVO publisherVO,
			DiscountVO discountVO) {
		super();
		this.orderID = orderID;
		this.ordTotal = ordTotal;
		this.discount = discount;
		this.discountPrice = discountPrice;
		this.ordTime = ordTime;
		this.ordType = ordType;
		this.actQuantity = actQuantity;
		this.orderListVOs = orderListVOs;
		this.userVO = userVO;
		this.publisherVO = publisherVO;
		this.discountVO = discountVO;
	}


	public Integer getOrderID() {
		return orderID;
	}


	public void setOrderID(Integer orderID) {
		this.orderID = orderID;
	}


	public Integer getOrdTotal() {
		return ordTotal;
	}


	public void setOrdTotal(Integer ordTotal) {
		this.ordTotal = ordTotal;
	}


	public Integer getDiscount() {
		return discount;
	}


	public void setDiscount(Integer discount) {
		this.discount = discount;
	}


	public Integer getDiscountPrice() {
		return discountPrice;
	}


	public void setDiscountPrice(Integer discountPrice) {
		this.discountPrice = discountPrice;
	}


	public Timestamp getOrdTime() {
		return ordTime;
	}


	public void setOrdTime(Timestamp ordTime) {
		this.ordTime = ordTime;
	}


	public Integer getOrdType() {
		return ordType;
	}


	public void setOrdType(Integer ordType) {
		this.ordType = ordType;
	}


	public Integer getActQuantity() {
		return actQuantity;
	}


	public void setActQuantity(Integer actQuantity) {
		this.actQuantity = actQuantity;
	}


	public Set<OrderListVO> getOrderListVOs() {
		return orderListVOs;
	}


	public void setOrderListVOs(Set<OrderListVO> orderListVOs) {
		this.orderListVOs = orderListVOs;
	}


	public UserVO getUserVO() {
		return userVO;
	}


	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}


	public PublisherVO getPublisherVO() {
		return publisherVO;
	}


	public void setPublisherVO(PublisherVO publisherVO) {
		this.publisherVO = publisherVO;
	}


	public DiscountVO getDiscountVO() {
		return discountVO;
	}


	public void setDiscountVO(DiscountVO discountVO) {
		this.discountVO = discountVO;
	}


	@Override
	public String toString() {
		return "OrdersVO [orderID=" + orderID + ", ordTotal=" + ordTotal + ", discount=" + discount + ", discountPrice="
				+ discountPrice + ", ordTime=" + ordTime + ", ordType=" + ordType + ", actQuantity=" + actQuantity
				+ "]";
	}


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OrdersVO)) return false;
        OrdersVO other = (OrdersVO) obj;
        return getOrderID() != null && getOrderID().equals(other.getOrderID());
    }

    @Override
    public int hashCode() {
        // Stable while Hibernate assigns IDs; never traverse nullable associations.
        return OrdersVO.class.hashCode();
    }
}
