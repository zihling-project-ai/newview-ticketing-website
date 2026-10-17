package com.newview.discount.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newview.admin.model.AdminVO;
import com.newview.discount.model.DiscountVO;
import com.newview.discount.service.DiscountService;
import com.newview.publisher.model.PublisherVO;
import com.newview.publisher.service.PublisherService;


@WebServlet("/discount/discount.do")
public class DiscountServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		
		if ("add".equals(action)) {
			
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			try {
				
				/************************* 接收請求參數 **************************/
				Integer pubID = new Integer(req.getParameter("pubID").trim());
				Integer adminID = new Integer(req.getParameter("adminID").trim());
				String discountContent = req.getParameter("discountContent").trim();
				Integer disAmount = new Integer(req.getParameter("disAmount").trim());
				String discountCode = req.getParameter("discountCode").trim();
				Timestamp disStartDate = java.sql.Timestamp.valueOf(req.getParameter("disStartDate").trim());
				Timestamp disFinishDate = java.sql.Timestamp.valueOf(req.getParameter("disFinishDate").trim());

				// 檢查請求參數接收

				System.out.println(pubID);
				System.out.println(adminID);
				System.out.println(discountContent);
				System.out.println(disAmount);
				System.out.println(discountCode);
				System.out.println(disStartDate);
				System.out.println(disFinishDate);

				/************************* 輸入錯誤處理 **************************/

				String idReg = 	"^[0-9]*$";
				String contentReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{1,50}$";
				String codeReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{1,10}$";
				String amountReg = "^[1-9999999999]$";
				
				if (pubID != null && adminID != null) {
					errorMsgs.add("廠商編號及管理者編號，請擇一輸入");
				} else if ((!Integer.toString(pubID).matches(idReg)) || (!Integer.toString(adminID).matches(idReg))) {
					errorMsgs.add("編號請輸入數字");
				}
				
				if (discountContent == null || discountContent.trim().length() == 0) {
					errorMsgs.add("優惠名稱: 請勿空白");
				} else if (!discountContent.trim().matches(contentReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("優惠名稱: 只能是中、英文字母、數字和_ , 且長度必需在1到50之間");
				}
				
				if (!Integer.toString(disAmount).matches(amountReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("優惠折抵金額: 請輸入數字, 且長度必需在1到9999999999之間");
				}
				
				if (discountCode == null || discountCode.trim().length() == 0) {
					errorMsgs.add("折扣碼: 請勿空白");
				} else if (!discountContent.trim().matches(codeReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("折扣碼: 只能是中、英文字母、數字和_ , 且長度必需在1到10之間");
				}
											
				if (disStartDate == null) {
					errorMsgs.add("起始日請勿空白");
				}

				if (disFinishDate == null) {
					errorMsgs.add("結束日請勿空白");
				}

				/************************* Package data **************************/
				// Package data
				DiscountVO discountVO = new DiscountVO();
				discountVO.setDiscountContent(discountContent);
				discountVO.setDisAmount(disAmount); 
				discountVO.setDiscountCode(discountCode);
				discountVO.setDisStartDate(disStartDate);
				discountVO.setDisFinishDate(disFinishDate);

				// coz Association Publisher create PublisherVO object to set pubID
				PublisherVO publisherVO = new PublisherVO();
				publisherVO.setPubID(pubID);
				// to PublisherVO to set PublisherVO
				discountVO.setPublisherVO(publisherVO);
				System.out.println(discountVO);
				
				// coz Association Admin create AdminVO object to set adminID
				AdminVO adminVO = new AdminVO();
				adminVO.setAdminID(adminID);
				// to AdminVO to set AdminVO
				discountVO.setAdminVO(adminVO);
				System.out.println(discountVO);

				// Send error back to form
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("discountVO", discountVO);
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-administrator/discount/discount-add.jsp");
					failureView.forward(req, res);
					return;

				}

				/************************* 開始新增資料 **************************/
				DiscountService discountSvc = new com.newview.discount.service.DiscountServiceImpl();
				discountVO = discountSvc.addDiscount(discountContent, disAmount, discountCode, disStartDate, disFinishDate, pubID, adminID);
				System.out.println(discountVO);
				System.out.println("新增成功");

				/************************* 回傳資料路徑 **************************/

				String url = "/Backstage/Allpage-publisher/user/user-listAll.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);

			} catch (Exception e) {
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-publisher/user/user-add.jsp");
				failureView.forward(req, res);
			}
		}
		
		if ("update".equals(action)) {
			
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			// 送出修改的來源網頁路徑
			String requestURL = req.getParameter("requestURL");

			try {
				
				/************************* 接收請求參數 **************************/
				Integer discountNO = new Integer(req.getParameter("discountNO").trim());
				Integer pubID = new Integer(req.getParameter("pubID").trim());
				Integer adminID = new Integer(req.getParameter("adminID").trim());
				String discountContent = req.getParameter("discountContent").trim();
				Integer disAmount = new Integer(req.getParameter("disAmount").trim());
				String discountCode = req.getParameter("discountCode").trim();
				Timestamp disStartDate = java.sql.Timestamp.valueOf(req.getParameter("disStartDate").trim());
				Timestamp disFinishDate = java.sql.Timestamp.valueOf(req.getParameter("disFinishDate").trim());

				// 檢查請求參數接收

				System.out.println(pubID);
				System.out.println(adminID);
				System.out.println(discountContent);
				System.out.println(disAmount);
				System.out.println(discountCode);
				System.out.println(disStartDate);
				System.out.println(disFinishDate);

				/************************* 輸入錯誤處理 **************************/

				String idReg = 	"^[0-9]*$";
				String contentReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{1,50}$";
				String codeReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{1,10}$";
				String amountReg = "^[1-9999999999]$";
				
				if (pubID != null && adminID != null) {
					errorMsgs.add("廠商編號及管理者編號，請擇一輸入");
				} else if ((!Integer.toString(pubID).matches(idReg)) || (!Integer.toString(adminID).matches(idReg))) {
					errorMsgs.add("編號請輸入數字");
				}
				
				if (discountContent == null || discountContent.trim().length() == 0) {
					errorMsgs.add("優惠名稱: 請勿空白");
				} else if (!discountContent.trim().matches(contentReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("優惠名稱: 只能是中、英文字母、數字和_ , 且長度必需在1到50之間");
				}
				
				if (!Integer.toString(disAmount).matches(amountReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("優惠折抵金額: 請輸入數字, 且長度必需在1到9999999999之間");
				}
				
				if (discountCode == null || discountCode.trim().length() == 0) {
					errorMsgs.add("折扣碼: 請勿空白");
				} else if (!discountContent.trim().matches(codeReg)) { // 正則(規)表示式(regular-expression)
					errorMsgs.add("折扣碼: 只能是中、英文字母、數字和_ , 且長度必需在1到10之間");
				}
											
				if (disStartDate == null) {
					errorMsgs.add("起始日請勿空白");
				}

				if (disFinishDate == null) {
					errorMsgs.add("結束日請勿空白");
				}

				/************************* Package data **************************/
				// Package data
				DiscountVO discountVO = new DiscountVO();
				discountVO.setDiscountContent(discountContent);
				discountVO.setDisAmount(disAmount); 
				discountVO.setDiscountCode(discountCode);
				discountVO.setDisStartDate(disStartDate);
				discountVO.setDisFinishDate(disFinishDate);

				// coz Association Publisher create PublisherVO object to set pubID
				PublisherVO publisherVO = new PublisherVO();
				publisherVO.setPubID(pubID);
				// to PublisherVO to set PublisherVO
				discountVO.setPublisherVO(publisherVO);
				System.out.println(discountVO);
				
				// coz Association Admin create AdminVO object to set adminID
				AdminVO adminVO = new AdminVO();
				adminVO.setAdminID(adminID);
				// to AdminVO to set AdminVO
				discountVO.setAdminVO(adminVO);
				System.out.println(discountVO);

				// Send error back to form
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("discountVO", discountVO);
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-administrator/discount/discount-update.jsp");
					failureView.forward(req, res);
					return;

				}

				/************************* 開始修改資料 **************************/
				DiscountService discountSvc = new com.newview.discount.service.DiscountServiceImpl();
				discountVO = discountSvc.updateDiscount(discountNO, discountContent, disAmount, discountCode, disStartDate, disFinishDate, pubID, adminID);
				System.out.println(discountVO);
				System.out.println("修改成功");

				/************************* 回傳資料路徑 **************************/
				PublisherService pubSvc = new PublisherService();
				//回傳資料路徑這段不太清楚
				
				String url = "/Backstage/Allpage-publisher/user/user-listAll.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);

				/***************************其他可能的錯誤處理*************************************/
			} catch (Exception e) {
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-administrator/discount/discount-update.jsp");
				failureView.forward(req, res);
			}
			
		}
		
		if ("delete".equals(action)) {
			
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			// 送出刪除的來源網頁路徑
			String requestURL = req.getParameter("requestURL");
			
			/************************* 接收請求參數 **************************/
			
			try {

				Integer discountNO = new Integer(req.getParameter("discountNO").trim());
				// 檢查請求參數接收
				System.out.println(discountNO);

				/************************* 開始刪除資料 **************************/
				DiscountService discountSvc = new com.newview.discount.service.DiscountServiceImpl();
				DiscountVO discountVO = discountSvc.getOneDiscount(discountNO);
				discountSvc.deleteDiscount(discountNO);
				System.out.println("刪除成功");

				/************************* 回傳資料路徑 **************************/
				// 資料庫取出的list物件,存入request
				if (requestURL.equals("/Backstage/Allpage-administrator/discount/discount-list.jsp")
						|| requestURL.equals("/Backstage/Allpage-publisher/user/user-listAll.jsp"))
					req.setAttribute("discount-deleteByNO", discountSvc.getOneDiscount(discountVO.getPublisherVO().getPubID()));
				//discount判斷admin 和 廠商 這邊不太清楚
				String url = requestURL;
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 **********************************/

			} catch (Exception e) {
				errorMsgs.add("刪除資料失敗:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher(requestURL);
				failureView.forward(req, res);
			}
			
		}
		
		if ("getOneForDisplay".equals(action)) {
			
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			try {
				/************************* 接收請求參數 **************************/
				String str = req.getParameter("discountNO");
				if (str == null || (str.trim()).length() == 0) {
					System.out.println(str);
					errorMsgs.add("請輸入優惠編號");
				}

				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-administrator/discount/discount-list.jsp");
					failureView.forward(req, res);
					return;
				}

				Integer discountNO = null;
				try {
					discountNO = new Integer(str);

				} catch (Exception e) {
					errorMsgs.add("優惠編號格式不正確");
				}
				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-administrator/discount/discount-list.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}

				/************************* 開始查詢資料 **************************/

				DiscountService discountSvc = new com.newview.discount.service.DiscountServiceImpl();
				DiscountVO discountVO = discountSvc.getOneDiscount(discountNO);
				System.out.println(discountVO);

				if (discountVO == null) {
					errorMsgs.add("查無資料");
				}

				if (!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/Backstage/Allpage-administrator/discount/discount-list.jsp");
					failureView.forward(req, res);
					return;// 程式中斷
				}
				/************************* 回傳資料路徑 **************************/
				req.setAttribute("discountVO", discountVO); // 資料庫取出的discountVO物件,存入req
				String url = "/Backstage/Allpage-administrator/discount/discount-listOne.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("無法取得資料:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/Backstage/Allpage-administrator/discount/discount-list.jsp");
				failureView.forward(req, res);
			}
			
		}
		
		if ("getOneForUpdate".equals(action)) {

			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);

			String requestURL = req.getParameter("requestURL"); // 送出修改的來源網頁路徑

			/************************* 接收請求參數 **************************/
			try {
				Integer discountNO = new Integer(req.getParameter("discountNO").trim());
				// 檢查請求參數接收
				System.out.println(discountNO);

				/************************* 開始查詢資料 **************************/
				
				DiscountService discountSvc = new com.newview.discount.service.DiscountServiceImpl();
				DiscountVO discountVO = discountSvc.getOneDiscount(discountNO);
				System.out.println(discountVO);

				/************************* 回傳資料路徑 **************************/
				req.setAttribute("discountVO", discountVO); // 資料庫取出的discountVO物件,存入req
				String url = "/Backstage/Allpage-administrator/discount/discount-update.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);

				/*************************** 其他可能的錯誤處理 *************************************/
			} catch (Exception e) {
				errorMsgs.add("修改資料取出時失敗:" + e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("requestURL");
				failureView.forward(req, res);
			}
		}
		
	}
	
}
