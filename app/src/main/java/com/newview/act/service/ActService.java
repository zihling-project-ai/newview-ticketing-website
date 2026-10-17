package com.newview.act.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newview.act.controller.ActWithPicsDTO;
import com.newview.act.model.ActVO;
import com.newview.actcategory.model.ActCategory;
import com.newview.cityaddress.model.CityAddress;

public interface ActService {
	void insert(ActVO act);
	void update(ActVO act);
    void delete(Integer ActID);
    ActVO findByPrimaryKey(Integer ActID);
    List<ActVO> getAll();
	List<ActVO> getActPics();
	List<ActVO> getAllWithAssociations();
	List<ActCategory> getAllCategories();
	List<CityAddress> getAllCities();
	List<ActWithPicsDTO> searchActsByName(String actName);
	ActWithPicsDTO getActWithPicturesById(Integer actIdValue);
	ActWithPicsDTO getActWithPicsDTOById(Integer actId);
    List<ActWithPicsDTO> searchActsByCategory(Integer actCategoryID);
	
	
	//use to publisher backstage by Mandy
	public List<ActVO> getActByPub(Integer pubID);
	
	
}
