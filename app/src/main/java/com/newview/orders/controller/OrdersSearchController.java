package com.newview.orders.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newview.act.controller.UserDTO;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

@WebServlet("/ordersSearch")
public class OrdersSearchController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;

    public OrdersSearchController() {
        super();
        userService = new UserServiceImpl();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        Integer userID = null;
        String userIDstr = request.getParameter("userID");

        if (userIDstr != null) {
            try {
                userID = Integer.parseInt(userIDstr);
            } catch (NumberFormatException e) {
                // 將無效的userID視為錯誤
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        if (userID != null) {
            // 使用userID來查找用戶資料
            UserVO user = userService.getUserByPK(userID);

            if (user != null) {
                // 將用戶資料轉換成JSON格式
            	UserDTO userDTO = new UserDTO();
                userDTO.setUserID(user.getUserID());
                userDTO.setUserName(user.getUserName());
                userDTO.setUserAccount(user.getUserAccount());
                userDTO.setUserBirth(user.getUserBirth());
                userDTO.setUserCell(user.getUserCell());
                userDTO.setUserEmail(user.getUserEmail());
                userDTO.setUserNickname(user.getUserNickname());
                userDTO.setBuyAuthority(user.getBuyAuthority());
                userDTO.setSpeakAuthority(user.getSpeakAuthority());

                // 將JSON返回給前端
                ObjectMapper objectMapper = new ObjectMapper();
                String userJson = objectMapper.writeValueAsString(userDTO);

                response.getWriter().write(userJson);
            } else {
                // 如果找不到用戶，返回相應的錯誤狀態碼
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            // 如果userID為null，返回相應的錯誤狀態碼
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
