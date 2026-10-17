package com.newview.act.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.google.gson.Gson;
import com.newview.act.model.*;
import com.newview.act.service.*;
import com.newview.actcategory.model.*;
import com.newview.actpic.model.*;
import com.newview.cityaddress.model.*;
import com.newview.publisher.model.*;
import com.newview.util.HibernateUtil;

@WebServlet("/act/act.do")
@MultipartConfig(maxFileSize = 1073741824)
public class TheActServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// 對應到的service 實體
		ActService actService = new ActServiceImpl();
		ActPicService actPicService = new ActPicServiceImpl();

		ActDAO actDAO = new ActDAOHibernateImpl();
		ActPicDAO actPicDAO = new ActPicDAOHibernateImpl();
		ActUpdateService actUpdateService = new ActUpdateServiceImpl(actDAO, actPicDAO);

		// 接收的參數
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");

		resp.setContentType("application/json");

		if ("search".equals(action)) {

			/********** 收到請求參數_找圖片 **********/

			String actName = req.getParameter("search-product");
			System.out.println("收到請求參數" + actName);

			/********** 輸入錯誤處理 **********/

			if (actName != null && !actName.isEmpty()) {

				/********** 查詢圖片資料 **********/
				List<ActWithPicsDTO> actWithPicsList = actService.searchActsByName(actName);// 至資料庫找資料

				if (!actWithPicsList.isEmpty()) {
					req.setAttribute("actWithPicsList", actWithPicsList); // 資料庫取出的list物件,存入request集合
					req.getRequestDispatcher("/SearchNewFile.jsp").forward(req, resp);// 執行完畢後，轉交回送出修改的來源網頁
				} else {
					req.setAttribute("actNameNotFound", "notFound");
					req.getRequestDispatcher("NewActJSPTest.jsp").forward(req, resp);
				}

			}
			return;
		}
		// 更新step1 先找到需要的資料
		if ("update".equals(action)) {

			/********** 收到請求參數 **********/
			String actId = req.getParameter("actId");
			System.out.println("servlet接收到的請求為：" + actId);

			/********** 輸入錯誤處理 **********/
			if (actId != null && !actId.isEmpty()) {

				/********** 查詢資料 **********/
				Integer actIdValue = Integer.parseInt(actId); // 參數轉為int
				System.out.println("準備帶著ID過去找資料了" + actIdValue);

				ActWithPicsDTO actWithPicsDTO = actService.getActWithPicturesById(actIdValue); // 至資料庫找資料整個活動&圖片的資料
				System.out.println("servlet拿到全部資料惹" + actWithPicsDTO);

				List<ActWithPicsDTO> actWithPicsList = Arrays.asList(actWithPicsDTO); // 將查詢结果 轉換為一個包含單個元素的的列表
				System.out.println("servlet將結果轉換成列表" + actWithPicsList);// 這裡的值會給頁面接值
//				for (ActWithPicsDTO item : actWithPicsList) {
//				    System.out.println(item.toString());
//				}

				if (actWithPicsDTO != null) {
					req.setAttribute("actWithPicsList", actWithPicsList); // 資料庫取出的list物件,存入request集合
					req.getRequestDispatcher("/Backstage/Allpage-publisher/activity/activity-update.jsp").forward(req,
							resp); // 執行完畢後，轉交回送出修改的來源網頁
				}
			} else {
				req.setAttribute("actIdError", "null");
				req.getRequestDispatcher("/Backstage/Allpage-publisher/pub-index.jsp").forward(req, resp);
			}
			return;
		}

//		if ("updateAct".equals(action)) {
//
//			/********** 收到請求參數 **********/
//			String actId = req.getParameter("actId");
//			System.out.println("收到請求參數" + actId);
//
//			/********** 輸入錯誤處理 **********/
//			if (actId != null && !actId.isEmpty()) {
//
//				/********** 查詢資料 **********/
//				Integer actIdValue = Integer.parseInt(actId); // 參數轉為int
//				ActWithPicsDTO actWithPicsDTO = actService.getActWithPicturesById(actIdValue); // 至資料庫找資料
//
//				List<ActWithPicsDTO> actWithPicsList = Arrays.asList(actWithPicsDTO);
//
//				if (actWithPicsDTO != null) {
//					req.setAttribute("actWithPicsList", actWithPicsList); // 資料庫取出的list物件,存入request集合
//					req.getRequestDispatcher("/update-act.jsp").forward(req, resp); // 執行完畢後，轉交回送出修改的來源網頁
//				}
//			} else {
//				req.setAttribute("actIdError", "null");
//				req.getRequestDispatcher("ActJSP.jsp").forward(req, resp);
//			}
//			return;
//		}

		if ("getJsonData".equals(action)) {
			List<ActWithPicsDTO> actWithPicsList = new ArrayList<>();

			/********** 收到請求參數 **********/
			String actIdStr = req.getParameter("actID");
			Integer actId = null;

			/********** 輸入錯誤處理 **********/
			if (actIdStr != null) {
				actId = Integer.parseInt(actIdStr);

				/********** 查詢資料 **********/
				ActWithPicsDTO actWithPicsDTO = actService.getActWithPicsDTOById(actId);

				if (actWithPicsDTO != null) {
					actWithPicsList.add(actWithPicsDTO); // 至資料庫新增資料
					String json = new Gson().toJson(actWithPicsList); // 將資料轉至json

					resp.setContentType("application/json"); // 回應的型態為json格式
					resp.setCharacterEncoding("UTF-8");
					resp.getWriter().write(json); // 回應json資料
				} else {
					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Activity not found");
				}
			}
			return;
		}

		if ("UP".equals(action)) {

			/********** 收到請求參數 **********/
			String toDelete = req.getParameter("toDelete");
			System.out.println(toDelete);

			/********** 輸入錯誤處理 **********/
			if (toDelete != null && !toDelete.isEmpty()) {

				/********** 先刪除圖片資料 **********/
				try {
					actPicService.deleteActPic(toDelete);

				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			/********** 在放入新的資料 **********/
			actUpdateService.updateActAndImages(req, resp);
			resp.sendRedirect("/Backstage/Allpage-publisher/activity/activity-list.jsp"); // 轉交資料
			return;
		}

		if ("delete".equals(action)) {

			/********** 收到請求參數 **********/
			String actIDelete = req.getParameter("actIDelete");
			// String actIDelete = req.getParameter("actIDelete");
			System.out.println("servlet收到刪除請求：" + actIDelete);

			/********** 輸入錯誤處理 **********/
			if (actIDelete != null) {
				try {

					/********** 查詢資料 **********/
					actService.delete(Integer.parseInt(actIDelete)); // 至資料庫刪除資料
					resp.setContentType("application/json; charset=UTF-8");

					resp.getWriter().println("Act ID " + actIDelete + " 成功"); // 回應刪除成功訊息

					String url = "/Backstage/Allpage-publisher/activity/activity-list.jsp";
					RequestDispatcher successView = req.getRequestDispatcher(url);
					successView.forward(req, resp);

				} catch (Exception e) {
					e.printStackTrace();
					resp.getWriter().println("Act ID " + actIDelete + " 失敗"); // 回應刪除失敗訊息

				}
			} else {

				resp.getWriter().println("Act ID 不能為空"); // 回應其他錯誤訊息
			}
			return;
		}

		if ("addPubAct".equals(action)) {

			/********** 收到請求參數 **********/
			String actName = req.getParameter("actName").trim();
			Integer actPriceStr = Integer.parseInt(req.getParameter("actPrice"));

			String actTimeStr = req.getParameter("actTime");
			Date actTime = null;
			if (actTimeStr != null && !actTimeStr.isEmpty()) {
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
				try {
					actTime = timeFormat.parse(actTimeStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Integer actScope = Integer.parseInt(req.getParameter("actScope"));
			String actIntroduce = req.getParameter("actIntroduce").trim();
			String actContent = req.getParameter("actContent").trim();

			String timeStr = req.getParameter("time");
			Date time = null;
			if (timeStr != null && !timeStr.isEmpty()) {
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
				try {
					time = timeFormat.parse(timeStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String actDateStr = req.getParameter("actDate");
			Date actDate = null;
			if (actDateStr != null && !actDateStr.isEmpty()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				try {
					actDate = dateFormat.parse(actDateStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String approvalConditionStr = (req.getParameter("approvalCondition") != null
					&& !req.getParameter("approvalCondition").isEmpty()) ? req.getParameter("approvalCondition") : "0";
			String actActAddress = req.getParameter("cityAddress").trim();

			String actCategoryIDStr = (req.getParameter("actCategory") != null
					&& !req.getParameter("actCategory").isEmpty()) ? req.getParameter("actCategory") : "1";
			
			String pubID = req.getParameter("pubID");
			String cityIDStr = req.getParameter("cityName");

			/********** 查看請求參數是否正確 **********/
			System.out.println(actName);
			System.out.println(actPriceStr);
			System.out.println(actTime);
			System.out.println(actScope);
			System.out.println(actIntroduce);
			System.out.println(actContent);
			System.out.println(time);
			System.out.println(actDate);
			System.out.println(approvalConditionStr);
			System.out.println(actActAddress);
			System.out.println(actCategoryIDStr);
			System.out.println(pubID);
			System.out.println(cityIDStr);

			/********** Package data **********/
			ActVO act = new ActVO();
			act.setActName(actName);
			act.setActPrice(actPriceStr);
			act.setActTime(actTime);
			act.setActScope(actScope);
			act.setActIntroduce(actIntroduce);
			act.setActContent(actContent);
			act.setTime(time);
			act.setActDate(actDate);
			act.setApprovalCondition(Integer.parseInt(approvalConditionStr));
			act.setCityAddress(actActAddress);

			ActCategory actC = new ActCategory();
			actC.setActCategoryID(Integer.parseInt(actCategoryIDStr));
			act.setActCategory(actC);

			PublisherVO pub = new PublisherVO();
			pub.setPubID(Integer.parseInt(pubID));
			act.setPublisherVOs(pub);

			CityAddress city = new CityAddress();
			city.setActAdressID(Integer.parseInt(cityIDStr));
			act.setCityAddressID(city);

			// 獲取上傳的圖片
			/*
			 * Part filePart = req.getPart("actImage"); System.out.println(filePart);
			 * 
			 * if (filePart != null) { InputStream fileContent = filePart.getInputStream();
			 * byte[] imageData = new byte[fileContent.available()];
			 * fileContent.read(imageData); ActPic actPic = new ActPic();
			 * actPic.setActID(act); actPic.setActPic(imageData);
			 * System.out.println("Received image data: " + imageData.length + " bytes");
			 * 
			 * Set<ActPic> actPics = new HashSet<>(); actPics.add(actPic);
			 * act.setActpics(actPics); } else {
			 * System.out.println("No image data received"); }
			 */

			// 測試獲取多張上傳的圖片
			Collection<Part> fileParts = req.getParts();
			Set<ActPic> actPics = new HashSet<>();

			System.out.println(fileParts);

			for (Part filePart : fileParts) {
				if (filePart.getName().startsWith("actImage")) {

					// 處理每個上傳的圖片
					InputStream fileContent = filePart.getInputStream();
					byte[] imageData = new byte[fileContent.available()];
					fileContent.read(imageData);

					ActPic actPic = new ActPic();
					actPic.setActID(act);
					actPic.setActPic(imageData);
					System.out.println("Received image data: " + imageData.length + " bytes");
					// Set<ActPic> actPics = new HashSet<>();
					actPics.add(actPic);

				} else {
					System.out.println("No image data received");
				}
			}
			act.setActpics(actPics);

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
					req.getSession().setAttribute("actNameError", "actNameSame");
					resp.sendRedirect("ActJSP.jsp");

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
			String url = "/Backstage/Allpage-publisher/activity/activity-list.jsp";
			RequestDispatcher successView = req.getRequestDispatcher(url);
			successView.forward(req, resp);
			return;
		}

		// 更新step2 更新資料
		if ("updateAct".equals(action)) {

			/********** 收到請求參數 **********/
			Integer actID = Integer.parseInt(req.getParameter("actIdStr"));
			System.out.println("更新資料中收到參數" + actID + "我在這");

			Integer actCategoryID = Integer.parseInt(req.getParameter("actCategoryStr"));
			Integer actScope = Integer.parseInt(req.getParameter("actScope"));
			Integer actPriceStr = Integer.parseInt(req.getParameter("actPrice"));

			String actTimeStr = req.getParameter("actTime");
			Date actTime = null;
			if (actTimeStr != null && !actTimeStr.isEmpty()) {
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
				try {
					actTime = timeFormat.parse(actTimeStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String timeStr = req.getParameter("time");
			Date time = null;
			if (timeStr != null && !timeStr.isEmpty()) {
				SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
				try {
					time = timeFormat.parse(timeStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String actDateStr = req.getParameter("actDate");
			Date actDate = null;
			if (actDateStr != null && !actDateStr.isEmpty()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				try {
					actDate = dateFormat.parse(actDateStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String actName = req.getParameter("actName").trim();
			Integer cityID = Integer.parseInt(req.getParameter("cityStr"));
			String actActAddress = req.getParameter("cityAddress").trim();
			String actIntroduce = req.getParameter("actIntroduce").trim();
			String actContent = req.getParameter("actContent").trim();

			HttpSession session = req.getSession();
			Integer pubID = (Integer) session.getAttribute("pubID");

			String approvalConditionStr = (req.getParameter("approvalCondition") != null
					&& !req.getParameter("approvalCondition").isEmpty()) ? req.getParameter("approvalCondition") : "0";

			/********** 檢查資訊 **********/
			System.out.println(actName);
			System.out.println(actPriceStr);
			System.out.println(actTime);
			System.out.println(actScope);
			System.out.println(actIntroduce);
			System.out.println(actContent);
			System.out.println(time);
			System.out.println(actDate);
			System.out.println(approvalConditionStr);
			System.out.println(actActAddress);
			System.out.println(actCategoryID);
			System.out.println(pubID);
			System.out.println(cityID);

			/********** Package data **********/

			ActVO updatedAct = actDAO.findByPrimaryKey(actID);

			if (updatedAct != null) {
				updatedAct.setActName(actName);
				updatedAct.setActPrice(actPriceStr);
				updatedAct.setActTime(actTime);
				updatedAct.setActScope(actScope);
				updatedAct.setActIntroduce(actIntroduce);
				updatedAct.setActContent(actContent);
				updatedAct.setTime(time);
				updatedAct.setActDate(actDate);
				updatedAct.setApprovalCondition(Integer.parseInt(approvalConditionStr));
				updatedAct.setCityAddress(actActAddress);

				ActCategory actC = new ActCategory();
				actC.setActCategoryID(actCategoryID);
				updatedAct.setActCategory(actC);

				PublisherVO pub = new PublisherVO();
				pub.setPubID(pubID);
				updatedAct.setPublisherVOs(pub);

				CityAddress city = new CityAddress();
				city.setActAdressID(cityID);
				updatedAct.setCityAddressID(city);
				
				


				List<byte[]> imageBytesList = new ArrayList<>();
				List<Integer> actPicIDList = new ArrayList<>();

				for (int i = 0;; i++) {
					String imageId = req.getParameter("imageId_" + i);
					if (imageId == null) {
						break;
					}
					actPicIDList.add(Integer.parseInt(imageId));
					String paramName = "imageNEW_" + i;
					Part filePart = req.getPart(paramName);
					System.out.println("真的有:    " + paramName+"和 "+imageId);
					if (filePart == null || filePart.getSize() <= 0) {
						continue;
					}

					InputStream inputStream = filePart.getInputStream();
					byte[] imageBytes = new byte[(int) filePart.getSize()];
					if (imageBytes != null) {
						System.out.println("有圖有真相");
					}
					inputStream.read(imageBytes);
					inputStream.close();

					if (imageBytes != null && imageId != null) {
						ActPicDAO actPicDAO2 = new ActPicDAOHibernateImpl();
						ActPic actPic = actPicDAO.findByPrimaryKey(Integer.parseInt(imageId));
						if (actPic != null) {
							actPic.setActPic(imageBytes);
							actPicDAO.update(actPic);
						}
					}
				}

				/* 新增圖片處理 */
				Part newFilePart = req.getPart("newImage");
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

					ActPicDAO actPicDAO2 = new ActPicDAOHibernateImpl();
					actPicDAO2.insert(newActPic);
				}

				actDAO.update(updatedAct);
			}

			/********** 回傳路徑 **********/
			String url = "/Backstage/Allpage-publisher/activity/activity-list.jsp";
			RequestDispatcher successView = req.getRequestDispatcher(url);
			successView.forward(req, resp);
			return;
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

}
