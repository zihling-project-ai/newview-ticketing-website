package com.newview.actcategory.service;

import java.util.List;
import java.util.Set;

import com.newview.act.model.ActVO;
import com.newview.actcategory.model.ActCategory;
import com.newview.actcategory.model.ActCategoryDAO;
import com.newview.actcategory.model.ActCategoryDAOHibernateImpl;
import com.newview.pubuser.model.PubUserVO;

public class ActCategoryService {

	private ActCategoryDAO dao;

	public ActCategoryService() {
		dao = new ActCategoryDAOHibernateImpl();
	}

	public void addOneActCategory(String actCategoryName) {
		ActCategory actCategory = new ActCategory();

		actCategory.setActCategoryName(actCategoryName);

		dao.insert(actCategory);
		
		System.out.println("add categoryh success!");

	}

	public ActCategory addActCategory(String actCategoryName) {

		ActCategory actCategory = new ActCategory();

		actCategory.setActCategoryName(actCategoryName);

		dao.insert(actCategory);

		return actCategory;
	}

	public ActCategory updateActCategory(Integer actCategoryID, String actCategoryName, Set<ActVO> acts) {

		ActCategory actCategory = new ActCategory();

		actCategory.setActCategoryID(actCategoryID);
		actCategory.setActCategoryName(actCategoryName);
		actCategory.setActs(acts);

		dao.update(actCategory);

		return actCategory;
	}

	public void deleteActCategory(Integer actCategoryID) {
		dao.delete(actCategoryID);
	}
	
	public ActCategory getOneActCategory(Integer actCategoryID) {
		return dao.findByPrimaryKey(actCategoryID);
	}

	public List<ActCategory> getAll() {
		return dao.getAll();
	}

}