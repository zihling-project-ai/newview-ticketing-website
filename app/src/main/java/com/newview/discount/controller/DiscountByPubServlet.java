package com.newview.discount.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.DateFormat;

import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.format.annotation.DateTimeFormat;

import com.newview.admin.model.AdminVO;
import com.newview.discount.model.DiscountVO;
import com.newview.discount.service.*;
import com.newview.publisher.model.PublisherVO;

@WebServlet("/pub/discount.do")
public class DiscountByPubServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");

		DiscountService discountSvc = new DiscountServiceImpl();

		if ("add".equals(action)) {
			// 收到請求
			System.out.println("收到請求了!!!");
			try {
				Integer pubID = Integer.parseInt(req.getParameter("pubIDStr"));

				String adminIDStr = (req.getParameter("adminIDStr") != null
						&& !req.getParameter("adminIDStr").isEmpty()) ? req.getParameter("adminIDStr") : "1";
				Integer adminID = Integer.parseInt(adminIDStr);

				String discountContent = req.getParameter("discountContent");
				Integer disAmount = Integer.parseInt(req.getParameter("disAmount"));
				String discountCode = req.getParameter("discountCode");

				java.sql.Timestamp disStartDate = null;
				String disStartDateStr = req.getParameter("disStartDateStr");

				if (disStartDateStr != null && !disStartDateStr.isEmpty()) {
				    try {
				        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				        LocalDateTime localDateTime = LocalDateTime.parse(disStartDateStr, formatter);
				        disStartDate = Timestamp.valueOf(localDateTime);
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
				}
				
				java.sql.Timestamp disFinishDate = null;
				String disFinishDateStr = req.getParameter("disFinishDateStr");

				if (disFinishDateStr != null && !disFinishDateStr.isEmpty()) {
				    try {
				        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				        LocalDateTime localDateTime = LocalDateTime.parse(disFinishDateStr, formatter);
				        disFinishDate = Timestamp.valueOf(localDateTime);
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
				}
				System.out.println("資料包裝中");
				/********** Package data **********/
				DiscountVO discountVO = new DiscountVO();
				discountVO.setDiscountContent(discountContent);
				discountVO.setDisAmount(disAmount);
				discountVO.setDiscountCode(discountCode);
				discountVO.setDisStartDate(disStartDate);
				discountVO.setDisFinishDate(disFinishDate);

				PublisherVO pub = new PublisherVO();
				pub.setPubID(pubID);
				discountVO.setPublisherVO(pub);

				AdminVO admin = new AdminVO();
				admin.setAdminID(adminID);
				discountVO.setAdminVO(admin);

				System.out.println("已包裝" + discountVO);
				/********** 新增資料 **********/

				discountVO = discountSvc.addDiscount(discountContent, disAmount, discountCode,
						disStartDate, disFinishDate, pubID, adminID);
				
				String url = "/Backstage/Allpage-publisher/discount/discount-list.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);

			} catch (Exception e) {
				e.printStackTrace();
				resp.sendRedirect("/Backstage/Allpage-publisher/pub-index.jsp"); // 回應路徑
			}
		}

		if ("update".equals(action)) {

			try {
				String discountNOStr = req.getParameter("discountNOStr");
				Integer discountNO = Integer.parseInt(discountNOStr);

				String discountContent = req.getParameter("discountContent");

				String disAmountStr = req.getParameter("disAmount");
				Integer disAmount = Integer.parseInt(disAmountStr);

				String discountCode = req.getParameter("discountCode");

				java.sql.Timestamp disStartDate = null;
				String disStartDateStr = req.getParameter("disFinishDate");

				if (disStartDateStr != null && !disStartDateStr.isEmpty()) {
				    try {
				        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				        LocalDateTime localDateTime = LocalDateTime.parse(disStartDateStr, formatter);
				        disStartDate = Timestamp.valueOf(localDateTime);
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
				}
				
				java.sql.Timestamp disFinishDate = null;
				String disFinishDateStr = req.getParameter("disFinishDate");

				if (disFinishDateStr != null && !disFinishDateStr.isEmpty()) {
				    try {
				        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				        LocalDateTime localDateTime = LocalDateTime.parse(disFinishDateStr, formatter);
				        disFinishDate = Timestamp.valueOf(localDateTime);
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
				}

				String pubIDStr = req.getParameter("pubIDStr");
				Integer pubID = Integer.parseInt(pubIDStr);

				String adminIDStr = (req.getParameter("adminIDStr") != null
						&& !req.getParameter("adminIDStr").isEmpty()) ? req.getParameter("adminIDStr") : "1";
				Integer adminID = Integer.parseInt(adminIDStr);

				System.out.println("接收完請求了");
				/********** Package data **********/
				DiscountVO discountVO = new DiscountVO();
				discountVO.setDiscountNO(discountNO);
				discountVO.setDiscountContent(discountContent);
				discountVO.setDisAmount(disAmount);
				discountVO.setDiscountCode(discountCode);
				discountVO.setDisStartDate(disStartDate);
				discountVO.setDisFinishDate(disFinishDate);

				PublisherVO pub = new PublisherVO();
				pub.setPubID(pubID);
				discountVO.setPublisherVO(pub);

				AdminVO admin = new AdminVO();
				admin.setAdminID(adminID);
				discountVO.setAdminVO(admin);

				/********** 新增資料 **********/
				System.out.println("準備新增資料");

				discountVO = discountSvc.updateDiscount(discountNO, discountContent, disAmount, discountCode,
						disStartDate, disFinishDate, pubID, adminID);

				String url = "/Backstage/Allpage-publisher/discount/discount-list.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);

			} catch (Exception e) {
				e.printStackTrace();
				resp.sendRedirect("/Backstage/Allpage-publisher/pub-index.jsp"); // 回應路徑
			}

		}

		if ("delete".equals(action)) {
			String discountNOStr = req.getParameter("discountNO").trim(); // 收到請求
			Integer discountNO = Integer.parseInt(discountNOStr);
			System.out.println("servlet收到刪除請求" + discountNO);
			try {
				discountSvc.deleteDiscountbyPub(discountNO); // 透過service至資料庫刪除資料
				System.out.println(discountNO + "刪除成功");
				String url = "/Backstage/Allpage-publisher/discount/discount-list.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);
			} catch (Exception e) {
				e.printStackTrace();
				resp.sendRedirect("/Backstage/Allpage-publisher/pub-index.jsp"); // 回應路徑
			}
		}

		if ("getOneForUpdate".equals(action)) {
			String discountNO = req.getParameter("discountNO").trim(); // 收到請求
			System.out.println("servlet收到顯示請求" + discountNO);
			try {

				DiscountVO discountVO = discountSvc.getOneDiscount(Integer.parseInt(discountNO)); // 透過service至資料庫找到資料
				System.out.println(discountVO);
				System.out.println("成功找到資料");

				req.setAttribute("discountVO", discountVO); // 資料庫取出的pubuserVO物件,存入req
				String url = "/Backstage/Allpage-publisher/discount/discount-update.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, resp);

			} catch (Exception e) {
				e.printStackTrace();
				resp.sendRedirect("/Backstage/Allpage-publisher/pub-index.jsp"); // 回應路徑
			}
		}

	}
}