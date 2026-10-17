package com.newview.pubuser.model;

import java.util.List;
import java.util.Map;

public interface PubUserHibernateDAO {
	
	public void add(PubUserVO pubUserVO);
	
	public void update(PubUserVO pubUserVO);
	
	public void delete(Integer pubUserID);
	
	public PubUserVO findByPK(Integer pubUserID);
	
	public PubUserVO findByAccount(String pubAccount);
	
	public List<PubUserVO> getAll();
	
	//萬用複合查詢(傳入參數型態Map)(回傳 List)
	public List<PubUserVO> getAllByCQ(Map<String,String>map);
	
	public List<PubUserVO> getListByPubID(Integer pubUserID);
	


	
}
