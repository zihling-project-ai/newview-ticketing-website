package com.newview.discount.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.newview.discount.model.*;
import com.newview.publisher.model.*;
import com.newview.admin.model.*;


public class DiscountServiceImpl implements DiscountService{
	
	private DiscountDAO discountDAO;
	
	public DiscountServiceImpl() {
		discountDAO = new DiscountDAOImpl();
	}

	@Override
	public DiscountVO addDiscount(String discountContent, Integer disAmount, String discountCode,
			Timestamp disStartDate, Timestamp disFinishDate, Integer pubID, Integer adminID) {

		DiscountVO discountVO = new DiscountVO();
		discountVO.setDiscountContent(discountContent);
		discountVO.setDisAmount(disAmount);
		discountVO.setDiscountCode(discountCode);
		discountVO.setDisStartDate(disStartDate);
		discountVO.setDisFinishDate(disFinishDate);

		// coz Association Publisher create PublisherVO object to set pubID
		PublisherVO publisherVO = new PublisherVO();
		publisherVO.setPubID(pubID);
		discountVO.setPublisherVO(publisherVO);

		// coz Association Admin create AdminVO object to set adminID
		AdminVO adminVO = new AdminVO();
		adminVO.setAdminID(adminID);
		discountVO.setAdminVO(adminVO);

		discountDAO.insert(discountVO);
		return discountVO;
		
	}

	@Override
	public DiscountVO updateDiscount(Integer discountNO,String discountContent, Integer disAmount, String discountCode,
			Timestamp disStartDate, Timestamp disFinishDate, Integer pubID, Integer adminID) {
		System.out.println("service已接收到請求");
		DiscountVO discountVO = new DiscountVO();
		discountVO.setDiscountNO(discountNO);
		discountVO.setDiscountContent(discountContent);
		discountVO.setDisAmount(disAmount);
		discountVO.setDiscountCode(discountCode);
		discountVO.setDisStartDate(disStartDate);
		discountVO.setDisFinishDate(disFinishDate);

		// coz Association Publisher create PublisherVO object to set pubID
		PublisherVO publisherVO = new PublisherVO();
		publisherVO.setPubID(pubID);
		discountVO.setPublisherVO(publisherVO);

		// coz Association Admin create AdminVO object to set adminID
		AdminVO adminVO = new AdminVO();
		adminVO.setAdminID(adminID);
		discountVO.setAdminVO(adminVO);
		
		System.out.println("準備請DAO更新資料庫");
		System.out.println("service接收到的資料" + discountVO);
		discountDAO.update(discountVO);
		System.out.println("service準備回傳資料");
		return discountVO;
	}

	@Override
	public void deleteDiscount(Integer discountNO) {
		discountDAO.delete(discountNO);
	}

	@Override
	public DiscountVO getOneDiscount(Integer discountNO) {
		return discountDAO.findByPrimaryKey(discountNO);
	}

	@Override
	public List<DiscountVO> getAll() {
		return discountDAO.getAll();
	}

	@Override // for Publisher Backstage get discount List
	public List<DiscountVO> getDiscountByPub(Integer pubID) {
		List<DiscountVO> discountList = discountDAO.getDiscountByPubID(pubID);
		return discountList;
	}

	@Override
	public void deleteDiscountbyPub(Integer discountNO) {
		discountDAO.deleteByPub(discountNO);	
	}
	
	
	



}