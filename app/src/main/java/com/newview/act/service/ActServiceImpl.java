package com.newview.act.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import com.newview.act.controller.ActWithPicsDTO;
import com.newview.act.controller.ImageDTO;
import com.newview.act.model.ActDAO;
import com.newview.act.model.ActDAOHibernateImpl;
import com.newview.act.model.ActVO;
import com.newview.actcategory.model.ActCategory;
import com.newview.actcategory.model.ActCategoryDAO;
import com.newview.actpic.model.ActPic;
import com.newview.cityaddress.model.CityAddress;
import com.newview.util.HibernateUtil;

public class ActServiceImpl implements ActService {

	private ActDAO actDAO;
	Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	
	public ActServiceImpl() {
		actDAO = new ActDAOHibernateImpl();
	}
	public ActServiceImpl(ActDAO actDAO) {
	    this.actDAO = actDAO;
	}


	@Override
	public void insert(ActVO act) {
		actDAO.insert(act);
	}

	@Override
	public void update(ActVO act) {
		actDAO.update(act);
	}

	@Override
	public void delete(Integer ActID) {
		actDAO.delete(ActID);
	}

	@Override
	public ActVO findByPrimaryKey(Integer ActID) {
		return actDAO.findByPrimaryKey(ActID);
	}

	@Override
	public List<ActVO> getAll() {
		
		return actDAO.getAll();
	}

	@Override
	public List<ActVO> getActPics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActVO> getAllWithAssociations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActCategory> getAllCategories() {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();
	        List<ActCategory> categories = session.createQuery("from ActCategory", ActCategory.class).list();
	        session.getTransaction().commit();
	        return categories;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	        return null; 
	    }
	}


	@Override
	public List<CityAddress> getAllCities() {
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    try {
	        session.beginTransaction();
	        List<CityAddress> cities = session.createQuery("from CityAddress", CityAddress.class).list();
	        session.getTransaction().commit();
	        return cities;
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.getTransaction().rollback();
	    }
	    return Collections.emptyList(); 
	}


	@Override
	public List<ActWithPicsDTO> searchActsByName(String actName) {
	    List<ActVO> searchResults = actDAO.getActsWithNameAndAssociations(actName);

	    List<ActWithPicsDTO> actWithPicsList = new ArrayList<>();

	    for (ActVO act : searchResults) {
	        ActWithPicsDTO actWithPicsDTO = new ActWithPicsDTO();
	        Set<ActPic> actPics = act.getActpics();
	        List<String> base64Images = new ArrayList<>(); 

	        for (ActPic pic : actPics) {
	            if (pic.getActPic() != null) {
	                byte[] picBytes = pic.getActPic();
	                String base64Image = Base64.getEncoder().encodeToString(picBytes);
	                base64Images.add(base64Image); 
	            } else {
	                System.out.println("NULL");
	            }
	        }

	        actWithPicsDTO.setActDate(act.getActDate());
	        actWithPicsDTO.setActTime(act.getActTime());
	        actWithPicsDTO.setTime(act.getTime());
	        actWithPicsDTO.setActID(act.getActID());
	        actWithPicsDTO.setBase64Images(base64Images); 
	        actWithPicsDTO.setActScope(act.getActScope());
	        actWithPicsDTO.setActName(act.getActName());
	        actWithPicsDTO.setActContent(act.getActContent());
	        actWithPicsDTO.setActPrice(act.getActPrice());
	        actWithPicsDTO.setActIntroduce(act.getActIntroduce());
	        actWithPicsDTO.setActCategoryName(act.getActCategoryID().getActCategoryName());
	        actWithPicsDTO.setActAddress(act.getCityAddress());
	        actWithPicsDTO.setCityAddress(act.getCityAddressID().getCityName());
	        actWithPicsDTO.setApprovalCondition(act.getApprovalCondition());
	        actWithPicsList.add(actWithPicsDTO);
	    }

	    return actWithPicsList;
	}

	public ActWithPicsDTO getActWithPicturesById(Integer actIdValue) {
	    ActWithPicsDTO actWithPicsDTO = null;

	    try {
	       
	        ActDAO actDAO = new ActDAOHibernateImpl();
	        ActVO act = actDAO.findByPrimaryKey(actIdValue); //使用actID取得 ActVO
//	        System.out.println("service找到的資料:" + act);

	        if (act != null) {
	            Set<ActPic> actPics = act.getActpics(); //DAO方法取得活動資訊及圖片(json格式)
	            List<ImageDTO> imageDataList = new ArrayList<>(); //創建ImageDTO用於儲存圖片
	            for (ActPic pic : actPics) { //遍歷活動相關的圖片集合
	                if (pic.getActPic() != null) {
	                    byte[] picBytes = pic.getActPic(); //獲取圖片的位元組
	                    //將圖片轉為Base64，以便在前端顯示
	                    String base64Image = Base64.getEncoder().encodeToString(picBytes);

	                    ImageDTO imageData = new ImageDTO();
	                    imageData.setBase64Image(base64Image);
	                    imageData.setActPicID(pic.getActPicID());
	                    
	                    //將ImageDTO添加到圖片訊息的列表中
	                    imageDataList.add(imageData);
	                }
	            }

	            actWithPicsDTO = new ActWithPicsDTO();
	            actWithPicsDTO.setActDate(act.getActDate());
	            actWithPicsDTO.setActTime(act.getActTime());
	            actWithPicsDTO.setTime(act.getTime());
	            actWithPicsDTO.setActID(act.getActID());
	            actWithPicsDTO.setActScope(act.getActScope());
	            actWithPicsDTO.setActName(act.getActName());
	            actWithPicsDTO.setActContent(act.getActContent());
	            actWithPicsDTO.setActPrice(act.getActPrice());
	            actWithPicsDTO.setActIntroduce(act.getActIntroduce());
	            actWithPicsDTO.setImages(imageDataList);
	            actWithPicsDTO.setActCategoryName(act.getActCategoryID().getActCategoryName());
	            actWithPicsDTO.setActAddress(act.getCityAddress());
	            actWithPicsDTO.setCityAddress(act.getCityAddressID().getCityName());
	            actWithPicsDTO.setPubName(act.getPublisherVOs().getPubName());
	            actWithPicsDTO.setApprovalCondition(act.getApprovalCondition());
	            actWithPicsDTO.setImages(imageDataList);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return actWithPicsDTO;
	}


	@Override
	public ActWithPicsDTO getActWithPicsDTOById(Integer actId) {
	    ActWithPicsDTO actWithPicsDTO = new ActWithPicsDTO();

	 
	    ActDAO actDAO = new ActDAOHibernateImpl(); 
	    ActVO act = actDAO.findByPrimaryKey(actId);
	   
	    if(act != null) {
	        actWithPicsDTO.setActDate(act.getActDate());	     
	        actWithPicsDTO.setTime(act.getTime());
	        actWithPicsDTO.setActID(act.getActID());
	        actWithPicsDTO.setActScope(act.getActScope());
	        actWithPicsDTO.setActName(act.getActName());
	        actWithPicsDTO.setActContent(act.getActContent());
	        actWithPicsDTO.setActPrice(act.getActPrice());
	        actWithPicsDTO.setActIntroduce(act.getActIntroduce());
	        actWithPicsDTO.setActCategoryName(act.getActCategoryID().getActCategoryName());
	        actWithPicsDTO.setActAddress(act.getCityAddress());
	        actWithPicsDTO.setCityAddress(act.getCityAddressID().getCityName());
	        actWithPicsDTO.setPubName(act.getPublisherVOs().getPubName());
	        actWithPicsDTO.setApprovalCondition(act.getApprovalCondition());
	        actWithPicsDTO.setActCategoryID(act.getActCategory().getActCategoryID());
	        
	        Set<ActPic> actPics = act.getActpics();
	        List<String> base64Images = new ArrayList<>();
	        for(ActPic pic : actPics) {
	            if(pic.getActPic() != null) {
	                byte[] picBytes = pic.getActPic();
	                String base64Image = Base64.getEncoder().encodeToString(picBytes);
	                base64Images.add(base64Image);
	            }
	        }
	        actWithPicsDTO.setBase64Images(base64Images);
	    }
	    return actWithPicsDTO;
	}
	@Override
	public List<ActWithPicsDTO> searchActsByCategory(Integer actCategoryID) {
	    List<ActVO> searchResults = actDAO.getActsByCategoryID(actCategoryID);

	    List<ActWithPicsDTO> actWithPicsList = new ArrayList<>();

	    for (ActVO act : searchResults) {
	        ActWithPicsDTO actWithPicsDTO = new ActWithPicsDTO();
	        Set<ActPic> actPics = act.getActpics();
	        List<String> base64Images = new ArrayList<>(); 

	        for (ActPic pic : actPics) {
	            if (pic.getActPic() != null) {
	                byte[] picBytes = pic.getActPic();
	                String base64Image = Base64.getEncoder().encodeToString(picBytes);
	                base64Images.add(base64Image); 
	            } else {
	                System.out.println("NULL");
	            }
	        }

	        actWithPicsDTO.setActDate(act.getActDate());
	        actWithPicsDTO.setActTime(act.getActTime());
	        actWithPicsDTO.setTime(act.getTime());
	        actWithPicsDTO.setActID(act.getActID());
	        actWithPicsDTO.setBase64Images(base64Images); 
	        actWithPicsDTO.setActScope(act.getActScope());
	        actWithPicsDTO.setActName(act.getActName());
	        actWithPicsDTO.setActContent(act.getActContent());
	        actWithPicsDTO.setActPrice(act.getActPrice());
	        actWithPicsDTO.setActIntroduce(act.getActIntroduce());
	        actWithPicsDTO.setActCategoryName(act.getActCategoryID().getActCategoryName());
	        actWithPicsDTO.setActAddress(act.getCityAddress());
	        actWithPicsDTO.setCityAddress(act.getCityAddressID().getCityName());
	        actWithPicsDTO.setApprovalCondition(act.getApprovalCondition());
	        actWithPicsList.add(actWithPicsDTO);
	    }

	    return actWithPicsList;
	    }
	
	public List<ActVO> getActByPub(Integer pubID){
		return actDAO.getActByPubID(pubID);
	}

	
}
