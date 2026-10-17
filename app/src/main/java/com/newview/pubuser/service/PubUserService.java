package com.newview.pubuser.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newview.publisher.model.PublisherVO;
import com.newview.pubuser.model.*;

public class PubUserService {

	private PubUserHibernateDAO dao;

	public PubUserService() {
		dao = new PubUserHibernateDAOImpl();
	}

	public PubUserVO addPubUser(String pubNickname, String pubAccount, String pubPassword, byte pubAuthority,
			Integer pubID) {

		PubUserVO pubuserVO = new PubUserVO();
		pubuserVO.setPubNickname(pubNickname);
		pubuserVO.setPubAccount(pubAccount);
		pubuserVO.setPubPassword(pubPassword);
		pubuserVO.setPubAuthority(pubAuthority);

		// coz Association Publisher create PublisherVO object to set pubID
		PublisherVO pubVO = new PublisherVO();
		pubVO.setPubID(pubID);
		// to PublisherVO to set PublisherVO
		pubuserVO.setPublisherVO(pubVO);

		dao.add(pubuserVO);
		return pubuserVO;
	}

	public PubUserVO updatePubUser(Integer pubUserID,String pubNickname, String pubAccount, String pubPassword, byte pubAuthority,
			Integer pubID) {

		PubUserVO pubuserVO = new PubUserVO();
		pubuserVO.setPubUserID(pubUserID);
		pubuserVO.setPubNickname(pubNickname);
		pubuserVO.setPubAccount(pubAccount);
		pubuserVO.setPubPassword(pubPassword);
		pubuserVO.setPubAuthority(pubAuthority);
		// coz Association Publisher create PublisherVO object to set pubID
		PublisherVO pubVO = new PublisherVO();
		pubVO.setPubID(pubID);
		// to PublisherVO to set PublisherVO
		pubuserVO.setPublisherVO(pubVO);

		dao.update(pubuserVO);
		return pubuserVO;
	}
	
	
	public boolean authenticate(String pubAccount, String pubPassword) {
		PubUserVO userlogin = dao.findByAccount(pubAccount);
				
		if(userlogin != null && userlogin.getPubPassword().equals(pubPassword)) {
			System.out.println("比對成功");
			return true;
		}else {
			System.out.println("比對失敗");
			return false;
		}
		
	}

	public void deletePubUser(Integer pubUserID) {
		dao.delete(pubUserID);
	}

	public PubUserVO getOnePubUser(Integer pubUserID) {
		return dao.findByPK(pubUserID);
	}

	public List<PubUserVO> getAll() {
		return dao.getAll();
	}


	
	public List<PubUserVO> getByCompositeQuery(Map<String, String[]> map) {
		
		Map<String, String> query = new HashMap<>();
		// Map.Entry即代表一組key-value
		Set<Map.Entry<String, String[]>> entry = map.entrySet();
		
		for (Map.Entry<String, String[]> row : entry) {
			String key = row.getKey();
			// 因為請求參數裡包含了action，做個去除動作
			if ("action".equals(key)) {
				continue;
			}
			// 若是value為空即代表沒有查詢條件，做個去除動作
			String value = row.getValue()[0]; //getValue拿到一個String陣列，接著[0]取得第一個元素檢查
			if ( value == null ||value.isEmpty() ) {
				continue;
			}
			query.put(key, value);
		}
		System.out.println(query);
		
		return dao.getAllByCQ(query);
	}

	public PubUserVO getByAccountInfo(String account) {
		
		return dao.findByAccount(account);
	}
	
	
	public List<PubUserVO> getPubuesrByPub(Integer pubID) {
		List<PubUserVO> pubUserList = dao.getListByPubID(pubID);
		return pubUserList;
	}
	
}
