package com.newview.publisher.model;

import java.util.List;
import java.util.Set;

import com.newview.pubuser.model.PubUserVO;

public interface PublisherHibernateDAO {
	public void add(PublisherVO publisher);
	public void update(PublisherVO publisher);
	public void delete(Integer pubID);
	public PublisherVO findByPK(Integer pubID);
	public List<PublisherVO> getAll();
	//查詢某部門的員工(一對多)(回傳 Set)
	public Set<PubUserVO> getPubuserByPubID(Integer pubID);
	
}
