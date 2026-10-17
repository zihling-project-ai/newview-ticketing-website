package com.newview.act.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.newview.act.model.ActDAO;
import com.newview.act.model.ActDAOHibernateImpl;
import com.newview.act.model.ActVO;
import com.newview.act.service.ActService;
import com.newview.act.service.ActServiceImpl;
import com.newview.actcategory.model.ActCategory;
import com.newview.actpic.model.ActPic;
import com.newview.actpic.model.ActPicDAO;
import com.newview.actpic.model.ActPicDAOHibernateImpl;
import com.newview.cityaddress.model.CityAddress;
import com.newview.publisher.model.PublisherVO;
import com.newview.util.HibernateUtil;

@WebServlet("/uploadImageServlet")
@MultipartConfig(maxFileSize = 1073741824)
public class UploadImageServlet extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		String toDelete = request.getParameter("toDelete");
		if ("addAct".equals(action)) {
			String actName = (request.getParameter("actName") != null && !request.getParameter("actName").isEmpty())
					? request.getParameter("actName")
					: "Default Act Name";
			System.out.println(actName);
			String actPriceStr = (request.getParameter("actPrice") != null
					&& !request.getParameter("actPrice").isEmpty()) ? request.getParameter("actPrice") : "1000";
			String actCategoryIDStr = (request.getParameter("actCategory") != null
					&& !request.getParameter("actCategory").isEmpty()) ? request.getParameter("actCategory") : "1";
			
			
			 System.out.println(actCategoryIDStr);
			 
			 
			String publisherName = (request.getParameter("publisher") != null
					&& !request.getParameter("publisher").isEmpty()) ? request.getParameter("publisher") : "1";
			String actTimeStr = (request.getParameter("time") != null && !request.getParameter("time").isEmpty())
					? request.getParameter("time")
					: "12:00:00";
			String cityIDStr = (request.getParameter("cityName") != null && !request.getParameter("cityName").isEmpty())
					? request.getParameter("cityName")
					: "1";
			String actIntroduce = (request.getParameter("actIntroduce") != null
					&& !request.getParameter("actIntroduce").isEmpty()) ? request.getParameter("actIntroduce") : "未輸入";
			String actDateStr = (request.getParameter("actDate") != null && !request.getParameter("actDate").isEmpty())
					? request.getParameter("actDate")
					: "1970-01-01";
			String actTimeDate = (request.getParameter("actTime") != null && !request.getParameter("actTime").isEmpty())
					? request.getParameter("actTime")
					: "1970-01-01";
			String approvalConditionStr = (request.getParameter("approvalCondition") != null
					&& !request.getParameter("approvalCondition").isEmpty()) ? request.getParameter("approvalCondition")
							: "0";
			String actContent = (request.getParameter("actContent") != null
					&& !request.getParameter("actContent").isEmpty()) ? request.getParameter("actContent")
							: "Default Act Content";
			String actScope = (request.getParameter("actScope") != null && !request.getParameter("actScope").isEmpty())
					? request.getParameter("actScope")
					: "1";
			String actActAddress = (request.getParameter("actActAddress") != null
					&& !request.getParameter("actActAddress").isEmpty()) ? request.getParameter("actActAddress")
							: "Default actActAddress";
			String pubID = (request.getParameter("pubID") != null && !request.getParameter("pubID").isEmpty())
					? request.getParameter("pubID")
					: "1";
			Date actTime = null;
			if (actTimeStr != null && !actTimeStr.isEmpty()) {
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
				try {
					actTime = timeFormat.parse(actTimeStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Date actDate = null;
			if (actDateStr != null && !actDateStr.isEmpty()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				try {
					actDate = dateFormat.parse(actDateStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ActVO act = new ActVO();
			ActCategory actC = new ActCategory();
			CityAddress city = new CityAddress();
			PublisherVO pub = new PublisherVO();
			act.setActID(act.getActID());
			city.setActAdressID(Integer.parseInt(cityIDStr));
			act.setActName(actName);
			act.setActPrice(Integer.parseInt(actPriceStr));
			actC.setActCategoryID(Integer.parseInt(actCategoryIDStr));
			act.setActCategory(actC);
			pub.setPubID(Integer.parseInt(pubID));
			act.setPublisherVOs(pub);
			act.setActTime(actTime);
			act.setTime(actTime);
			act.setActScope(Integer.parseInt(actScope));
			act.setActContent(actContent);
			act.setCityAddressID(city);
			
			act.setActIntroduce(actIntroduce);
			act.setActDate(actDate);
			act.setApprovalCondition(Integer.parseInt(approvalConditionStr));
			act.setCityAddress(actActAddress);

			// 獲取上傳的圖片
			Part filePart = request.getPart("actImage");
			System.out.println(filePart);
			;
			if (filePart != null) {
				InputStream fileContent = filePart.getInputStream();
				byte[] imageData = new byte[fileContent.available()];
				fileContent.read(imageData);
				ActPic actPic = new ActPic();
				actPic.setActID(act);
				actPic.setActPic(imageData);
				System.out.println("Received image data: " + imageData.length + " bytes");

				Set<ActPic> actPics = new HashSet<>();
				actPics.add(actPic);
				act.setActpics(actPics);
			} else {
				System.out.println("No image data received");
			}

			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			try {
				session.beginTransaction();
				String hql = "FROM ActVO WHERE actName = :actName";
			    Query query = session.createQuery(hql);
			    query.setParameter("actName", actName);
			    List results = query.list();
			    if (!results.isEmpty()) {
			    	System.out.println("處理同名活動");
			        // 活動名稱存在
			    	request.getSession().setAttribute("actNameError", "actNameSame");
			    	response.sendRedirect("ActJSP.jsp");
	               
			        return;
			    }
				session.save(act);
				session.getTransaction().commit();
			} catch (Exception e) {
				if (session.getTransaction() != null) {
					session.getTransaction().rollback();
				}
				e.printStackTrace();
			} finally {
				// 關閉 Session
				session.close();
			}

			response.sendRedirect("ActJSP.jsp");
		}
	}
	}

