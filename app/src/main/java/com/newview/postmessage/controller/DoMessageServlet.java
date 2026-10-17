package com.newview.postmessage.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.post.model.PostVO;
import com.newview.postmessage.model.PostMessageDAO;
import com.newview.postmessage.model.PostMessageDAOImpl;
import com.newview.postmessage.model.PostMessageVO;
import com.newview.postmessage.service.PostMessageService;
import com.newview.postmessage.service.PostMessageServiceImpl;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

@WebServlet("/DoMessageServlet")
@MultipartConfig
public class DoMessageServlet extends HttpServlet {
	private static final long serialVersionUID = -1195497717454804472L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");
		String action = req.getParameter("action");

		// ------------------------------------新增開始-------------------------------------------//

		if ("AddMsg".equals(action)) { // 接受来自forum_postdetail.html请求(Ajax)

			// --------------------1.接受請求參數---------------------------//
			Integer postID = Integer.valueOf(req.getParameter("postID").trim());
			String mesContent = req.getParameter("MesContent");

			// --------------------2.開始新增數據---------------------------//
			PrintWriter out = res.getWriter();
			PostMessageService postmsgSvc = new PostMessageServiceImpl();

			// 新增Message相關物件
			PostMessageVO postmsgVO = new PostMessageVO();

			// PostID
			PostVO postVO = new PostVO();
			postVO.setPostID(postID);
			postmsgVO.setPostVO(postVO);

			// UserID
			UserVO userVO = new UserVO();
			// 用Session獲取用戶的userID
			String userIDString = (String) req.getSession().getAttribute("userID");
			Integer userID = Integer.parseInt(userIDString);
			userVO.setUserID(userID);
			postmsgVO.setUserVO(userVO);

			// MesContent
			postmsgVO.setMesContent(mesContent);
			postmsgVO.setMessageDate(new Timestamp(System.currentTimeMillis()));

			// 儲存物件
			if(postmsgSvc.addPostMessage(postmsgVO)<=0){res.sendError(500);return;}
			

			// ---------------------------------資料回傳--------------------------------//

			UserService userSvc = new UserServiceImpl();

			// 建構JSON數據
			Map<String, Object> jsonData = new HashMap<>();
			jsonData.put("userName", userSvc.getUserByPK(userID).getUserNickname()); // 作者訊息
			jsonData.put("userAccount", userSvc.getUserByPK(userID).getUserAccount()); // 作者訊息
			jsonData.put("MesContent", postmsgVO.getMesContent()); // 分類訊息
			jsonData.put("msgDate", postmsgVO.getMessageDate().toString()); // 分類訊息
			jsonData.put("postMessageID", postmsgVO.getPostMessageID()); // 留言ID

			

			Gson gson = new Gson();
			String json = gson.toJson(jsonData);

			// Return Json to Ajax
			res.getWriter().write(json);
		}

		// ------------------------------------FindByPostPK + ListAll開始-------------------------------------------//

		if ("GetAll".equals(action)) {

			int postID = Integer.parseInt(req.getParameter("postID").trim());
			PostMessageDAO dao = new PostMessageDAOImpl();
			List<PostMessageVO> msg = dao.getAll();

			List<Map<String, Object>> postmsgList = new ArrayList<>();
			for (PostMessageVO postmsg : msg) {
				// 檢查留言的 postID 是否與請求中的 postID 相匹配
				if (postmsg.getPostVO().getPostID() == postID) {
					Map<String, Object> postmsgData = new HashMap<>();
					postmsgData.put("userID", postmsg.getUserVO().getUserID());
                    postmsgData.put("userAccount", postmsg.getUserVO().getUserAccount());
					postmsgData.put("userName", postmsg.getUserVO().getUserNickname());
					postmsgData.put("mesContent", postmsg.getMesContent());
					postmsgData.put("messageDate", postmsg.getMessageDate().toString());
					postmsgData.put("postMessageID", postmsg.getPostMessageID());
					postmsgList.add(postmsgData);
				}
			}

			

			Gson gson = new Gson();
			String jsonData = gson.toJson(postmsgList);
			res.getWriter().write(jsonData);
		}

		// ------------------------------------Update開始-------------------------------------------//

		if ("Update".equals(action)) {

			// 1.接受請求參數//
			Integer postMessageID = Integer.valueOf(req.getParameter("postMessageID"));
			Integer userID = Integer.valueOf((String) req.getSession().getAttribute("userID"));
			String mesContent = req.getParameter("mesContent").trim();

			// 2.開始新增數據//
			PrintWriter out = res.getWriter();
			PostMessageService postmsgSvc = new PostMessageServiceImpl();
			PostMessageVO existingPostMsg = postmsgSvc.getPostMessageByPK(postMessageID);
			Timestamp messageDate = new Timestamp(System.currentTimeMillis());

			if (existingPostMsg == null) {res.sendError(404);return;}
            if (existingPostMsg != null) {

				existingPostMsg.setMesContent(mesContent);
				existingPostMsg.setMessageDate(messageDate);
			}

			// 更新實體資料
			if(postmsgSvc.updatePostMessage(existingPostMsg)<=0){res.sendError(500);return;}
			out.println("{\"status\":\"updated\"}");

		}

		// ------------------------------------Delete開始-------------------------------------------//

		if ("Delete".equals(action)) {

			int postMessageID = Integer.parseInt(req.getParameter("postMessageID").trim());
			PostMessageService postmsgSvc = new PostMessageServiceImpl();
			if(postmsgSvc.deletePostMessage(postMessageID)<=0){res.sendError(500);return;}
			res.getWriter().write("{\"status\":\"deleted\"}");
		}
	}
}