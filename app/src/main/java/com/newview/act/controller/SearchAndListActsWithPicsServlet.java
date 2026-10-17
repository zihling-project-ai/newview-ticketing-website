package com.newview.act.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.gson.Gson;
import com.newview.act.model.ActDAO;
import com.newview.act.model.ActDAOHibernateImpl;
import com.newview.act.model.ActVO;
import com.newview.act.service.ActPicService;
import com.newview.act.service.ActPicServiceImpl;
import com.newview.act.service.ActService;
import com.newview.act.service.ActServiceImpl;
import com.newview.act.service.ActUpdateService;
import com.newview.act.service.ActUpdateServiceImpl;
import com.newview.actcategory.model.ActCategory;
import com.newview.actpic.model.ActPic;
import com.newview.actpic.model.ActPicDAO;
import com.newview.actpic.model.ActPicDAOHibernateImpl;
import com.newview.cityaddress.model.CityAddress;

@WebServlet("/SearchSe")
@MultipartConfig(maxFileSize = 1073741824)
public class SearchAndListActsWithPicsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ActService actService;
	private ActPicService actPicService;
	private ActUpdateService actUpdateService;

	public SearchAndListActsWithPicsServlet() {
		super();
		ActDAO actDAO = new ActDAOHibernateImpl();
		ActPicDAO actPicDAO = new ActPicDAOHibernateImpl();
		actService = new ActServiceImpl(actDAO);
		actPicService = new ActPicServiceImpl(actPicDAO);
		actUpdateService = new ActUpdateServiceImpl(actDAO, actPicDAO);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		response.setContentType("application/json");
		String action = request.getParameter("action");
		String toDelete = request.getParameter("toDelete");
		System.out.println(toDelete);
		String actIDelete = request.getParameter("actIDelete");
		System.out.println(action);
		try {
			if ("search".equals(action)) {
				String actName = request.getParameter("search-product");
				System.out.println(actName);

				if (actName == null || actName.isEmpty()) {
					request.setAttribute("actNameError", "null");
					request.getRequestDispatcher("NewActJSPTest.jsp").forward(request, response);
					return;
					// 沒有輸入就直接結束
				}

				List<ActWithPicsDTO> actWithPicsList = actService.searchActsByName(actName);

				if (actWithPicsList.isEmpty()) {
					// 如果沒找到
					request.setAttribute("actNameNotFound", "notFound");
					request.getRequestDispatcher("NewActJSPTest.jsp").forward(request, response);
				} else {
					// 有找到
					request.setAttribute("actWithPicsList", actWithPicsList);
					request.getRequestDispatcher("/SearchNewFile.jsp").forward(request, response);
				}
			} else if ("update".equals(action)) {
				String actId = request.getParameter("actId");

				if (actId == null || actId.isEmpty()) {
					request.setAttribute("actIdError", "null");
					request.getRequestDispatcher("ActJSP.jsp").forward(request, response);
					return; // 沒有輸入就結束
				}

				Integer actIdValue = Integer.parseInt(actId);
				ActWithPicsDTO actWithPicsDTO = actService.getActWithPicturesById(actIdValue);

				if (actWithPicsDTO != null) {
					// 有找到
					List<ActWithPicsDTO> actWithPicsList = Arrays.asList(actWithPicsDTO);
					request.setAttribute("actWithPicsList", actWithPicsList);
					request.getRequestDispatcher("/update-act.jsp").forward(request, response);
				}
			} else if ("getJsonData".equals(action)) {
				handleJsonResponse(request, response);
			} else if ("UP".equals(action)) {
				System.out.println(toDelete);

				if (toDelete != null && !toDelete.isEmpty()) {
					// 先查看有無刪除動作再決定是否執行 圖片刪除
					try {
						actPicService.deleteActPic(toDelete);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// 最後執行修改資料方法
				actUpdateService.updateActAndImages(request, response);
				response.sendRedirect("ActJSP.jsp");

			} else if ("delete".equals(action)) {
				if (actIDelete != null && !actIDelete.isEmpty()) {
					try {
						System.out.println(actIDelete);
						// actService.delete(Integer.parseInt(actIDelete));
						response.getWriter().println("Act ID " + actIDelete + " 成功");
					} catch (Exception e) {
						e.printStackTrace();
						response.getWriter().println("Act ID " + actIDelete + " 失敗");
					} finally {
						response.sendRedirect("ActJSP.jsp");
					}
				} else {
					response.getWriter().println("Act ID 不能為空");
				}
			} else if ("pageChange".equals(action)) {
				System.out.println(action);
				String actIdString = request.getParameter("actID");
				System.out.println(actIdString);

				if (actIdString != null) {
					try {
						Integer actId = Integer.parseInt(actIdString);

						ActServiceImpl actService = new ActServiceImpl();
						ActWithPicsDTO actWithPics = actService.getActWithPicsDTOById(actId);

						if (actWithPics != null) {
							request.setAttribute("actData", actWithPics);
							request.getRequestDispatcher("/product-detail.jsp").forward(request, response);
						} else {
							response.getWriter().write("No data found for given actID");
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleJsonResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<ActWithPicsDTO> actWithPicsList = new ArrayList<>();
		String actIdStr = request.getParameter("actID");

		Integer actId = null;

		if (actIdStr != null) {
			actId = Integer.parseInt(actIdStr);
		}

		ActWithPicsDTO actWithPicsDTO = actService.getActWithPicsDTOById(actId);

		if (actWithPicsDTO != null) {
			actWithPicsList.add(actWithPicsDTO);

			String json = new Gson().toJson(actWithPicsList);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Activity not found");
		}
	}
}
