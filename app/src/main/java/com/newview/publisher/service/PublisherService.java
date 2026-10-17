package com.newview.publisher.service;

import java.util.List;
import java.util.Set;

import com.newview.publisher.model.PublisherHibernateDAO;
import com.newview.publisher.model.PublisherHibernateDAOImpl;
import com.newview.publisher.model.PublisherVO;
import com.newview.pubuser.model.PubUserVO;

public class PublisherService {
	
	private PublisherHibernateDAO dao;
	
	public PublisherService() {
		dao = new PublisherHibernateDAOImpl();
	}
	
	
	public void deletePub(Integer pubID) {
		dao.delete(pubID);
	}
	
	public PublisherVO getOnePub(Integer pubID) {
		return dao.findByPK(pubID);
	}
	
	public List<PublisherVO> getAll(){
		return dao.getAll();
	}
	
	
	public Set<PubUserVO> getPubuserByPubID(Integer pubID){
		return dao.getPubuserByPubID(pubID);
	}
	
	
}
