package com.newview.act.service;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.newview.act.model.ActDAO;
import com.newview.act.model.ActDAOHibernateImpl;
import com.newview.act.model.ActVO;
import com.newview.actcategory.model.ActCategory;
import com.newview.actcategory.model.ActCategoryDAO;
import com.newview.actcategory.model.ActCategoryDAOHibernateImpl;
import com.newview.actpic.model.ActPic;
import com.newview.actpic.model.ActPicDAO;
import com.newview.actpic.model.ActPicDAOHibernateImpl;
import com.newview.cityaddress.model.CityAddress;
import com.newview.cityaddress.model.CityAddressDAO;
import com.newview.cityaddress.model.CityAddressDAOHibernatelmpl;

public class ActUpdateServiceImpl implements ActUpdateService {
    private ActDAO actDAO;
    private ActPicDAO actPicDAO;

    public ActUpdateServiceImpl(ActDAO actDAO, ActPicDAO actPicDAO) {
        this.actDAO = actDAO;
        this.actPicDAO = actPicDAO;
    }

    @Override
    public void updateActAndImages(HttpServletRequest request, HttpServletResponse response) {
        try {
            String actIdStr = request.getParameter("actId");
            System.out.println(actIdStr+"我在這");
            String actName = request.getParameter("actName");
            String actPriceStr = request.getParameter("actPrice");
            String actCategoryID = request.getParameter("actCategory");
            String publisherName = request.getParameter("publisher");
            String actTimeStr = request.getParameter("time");
            String cityID = request.getParameter("cityName");          
            String actIntroduce = request.getParameter("actIntroduce");
            String actDate = request.getParameter("actDate");
            String approvalConditionStr = request.getParameter("approvalCondition");

            Integer actID = Integer.parseInt(actIdStr);
            Integer actPrice = (int) Double.parseDouble(actPriceStr);
            Integer actCategoryNameI = Integer.parseInt(actCategoryID);
            Integer cityIDI = Integer.parseInt(cityID);
            Integer  approvalCondition = Integer.parseInt( approvalConditionStr);
            Date actTime = null;
            if (actTimeStr != null && !actTimeStr.isEmpty()) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                actTime = timeFormat.parse(actTimeStr);
            }

            Date actDateD = null;
            if (actDate != null && !actDate.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                actDateD = dateFormat.parse(actDate);
            }

           

            ActCategoryDAO actCategory = new ActCategoryDAOHibernateImpl();
            ActDAO actDAO = new ActDAOHibernateImpl();
            ActVO updatedAct = actDAO.findByPrimaryKey(actID);
            CityAddressDAO cityAddress = new CityAddressDAOHibernatelmpl();
            if (updatedAct != null) {
                // 更新活动信息
            	CityAddress cityAddressName = cityAddress.findByPrimaryKey(cityIDI);
            	ActCategory actCategoryName = actCategory.findByPrimaryKey(actCategoryNameI);
            	  System.out.println(cityAddressName);
                updatedAct.setActName(actName);
                updatedAct.setActPrice(actPrice);
                updatedAct.setActCategory(actCategoryName);
                updatedAct.setPublisherVOs(publisherName);
                updatedAct.setTime(actTime);
                updatedAct.setCityAddressID(cityAddressName);
                updatedAct.setActIntroduce(actIntroduce);
                updatedAct.setActDate(actDateD);
                updatedAct.setApprovalCondition(approvalCondition);
                
                System.out.println(updatedAct);
                
                List<byte[]> imageBytesList = new ArrayList<>();
		        List<Integer> actPicIDList = new ArrayList<>();

		        for (int i = 0; ; i++) {
		            String imageId = request.getParameter("imageId_" + i);
		            if (imageId == null) { 
		                
		                break;
		            }
		            actPicIDList.add(Integer.parseInt(imageId));
		            String paramName = "imageNEW_" + i;

		            Part filePart = request.getPart(paramName);
		            System.out.println("真的有:    " + paramName+"和 "+imageId);
		            if (filePart == null || filePart.getSize() <= 0) {
		            	 continue;
		            }

		            InputStream inputStream = filePart.getInputStream();
		            byte[] imageBytes = new byte[(int) filePart.getSize()];
		            if(imageBytes!=null) {
		            	System.out.println("有圖有真相");
		            }
		            inputStream.read(imageBytes);
		            inputStream.close();

		            if (imageBytes != null && imageId != null) {
		                ActPicDAO actPicDAO = new ActPicDAOHibernateImpl();
		                ActPic actPic = actPicDAO.findByPrimaryKey(Integer.parseInt(imageId));
		                if (actPic != null) {
		                    actPic.setActPic(imageBytes);
		                    actPicDAO.update(actPic);
		                }
		            }
		        }
		        /*新增圖片處理*/
		        Part newFilePart = request.getPart("newImage");
		        if (newFilePart != null && newFilePart.getSize() > 0) {
		            InputStream newImageInputStream = newFilePart.getInputStream();
		            byte[] newImageBytes = new byte[(int) newFilePart.getSize()];
		            newImageInputStream.read(newImageBytes);
		            newImageInputStream.close();
		            System.out.println("處理中請稍後");
		            
		            ActPic newActPic = new ActPic();
		            updatedAct.setActID(actID);
		            newActPic.setActID(updatedAct); 
		            newActPic.setActPic(newImageBytes); 

		            ActPicDAO actPicDAO = new ActPicDAOHibernateImpl();
		            actPicDAO.insert(newActPic); 
		        }

              
                actDAO.update(updatedAct);

               
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   

}
