package com.newview.cityaddress.model;

import java.util.List;

import com.newview.act.model.ActVO;

public interface CityAddressDAO {

	void insert(CityAddress act);

	void update(CityAddress act);

	void delete(Integer ActID);

	CityAddress findByPrimaryKey(Integer ActID);

	List<CityAddress> getAll();
}
