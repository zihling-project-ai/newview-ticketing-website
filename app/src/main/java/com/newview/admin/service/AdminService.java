package com.newview.admin.service;


import com.newview.admin.model.*;


public class AdminService {
	
	private AdminHibernateDAO adminDao;
	
	public AdminService() {
		adminDao = new AdminHibernateDAOImpl();
	}
	
	public boolean authenticate(String adminAccount, String adminPassword) {
		AdminVO adminLogin = adminDao.findByAccount(adminAccount);
				
		if(adminLogin != null && adminLogin.getAdminPassword().equals(adminPassword)) {
			System.out.println("比對成功");
			return true;
		}else {
			System.out.println("比對失敗");
			return false;
		}
		
	}
	
	public AdminVO getByAccountInfo(String adminAccount) {
		return adminDao.findByAccount(adminAccount);
	}
}
