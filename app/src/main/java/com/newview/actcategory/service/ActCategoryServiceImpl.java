package com.newview.actcategory.service;

import java.util.List;

import com.newview.actcategory.model.ActCategory;
import com.newview.actcategory.model.ActCategoryDAO;
import com.newview.actcategory.model.ActCategoryDAOHibernateImpl;
import com.newview.user.model.UserVO;

public class ActCategoryServiceImpl extends ActCategoryService {
	
	private ActCategoryDAO actCategoryDAO;
	
	public ActCategoryServiceImpl() {
		actCategoryDAO = new ActCategoryDAOHibernateImpl();	
	}
	
	public int addActCategory(ActCategory actCategory) {
		actCategoryDAO.insert(actCategory);
        return actCategory.getActCategoryID();
	}
	
}