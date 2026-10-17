package com.newview.compic.model;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.orderlist.model.OrderListVO;

@Entity
@Table(name = "compic")
public class ComPicVO implements java.io.Serializable {
	
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comPicID")
	private Integer comPicID;

	@Expose
	@Column(name = "comPic", columnDefinition = "longblob")
	private byte[] comPic;
	
	@ManyToOne
	@JoinColumn(name = "orderListID", referencedColumnName = "orderListID")
	private OrderListVO orderListVO;

	public ComPicVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComPicVO(Integer comPicID, byte[] comPic, OrderListVO orderListVO) {
		super();
		this.comPicID = comPicID;
		this.comPic = comPic;
		this.orderListVO = orderListVO;
	}

	public Integer getComPicID() {
		return comPicID;
	}

	public void setComPicID(Integer comPicID) {
		this.comPicID = comPicID;
	}

	public byte[] getComPic() {
		return comPic;
	}

	public void setComPic(byte[] comPic) {
		this.comPic = comPic;
	}

	public OrderListVO getOrderListVO() {
		return orderListVO;
	}

	public void setOrderListVO(OrderListVO orderListVO) {
		this.orderListVO = orderListVO;
	}

	@Override
	public String toString() {
		return "ComPicVO [comPicID=" + comPicID + ", comPic=" + Arrays.toString(comPic) + "]";
	}

	

	
}