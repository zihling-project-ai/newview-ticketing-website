package com.newview.admin.model;

import java.util.List;

public interface AdminHibernateDAO {
	int update(AdminVO admin);
	List<AdminVO>getAll();
	AdminVO findByAccount(String adminAccount);
	
}


