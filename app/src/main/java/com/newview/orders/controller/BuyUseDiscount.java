package com.newview.orders.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.discount.model.DiscountDAO;
import com.newview.discount.model.DiscountDAOImpl;
import com.newview.discount.model.DiscountVO;
import com.newview.usediscount.model.UseDiscountDAO;
import com.newview.usediscount.model.UseDiscountDAOImpl;
import com.newview.usediscount.model.UseDiscountVO;

@WebServlet("/BuyUseDiscount")
public class BuyUseDiscount extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");

		// 1.接收請求參數
		String discountCode = req.getParameter("discountCode").trim();
		
		// 2.獲取購買人User訊息
        String currentUserID = req.getParameter("userID");
        int userID = Integer.parseInt(currentUserID);
           
        // 3.驗證該用戶有沒有該discountNO
        DiscountDAO discountdao = new DiscountDAOImpl();           
        int discountNO = discountdao.getDisAmountBy(discountCode).getDiscountNO(); 
        UseDiscountDAO usedao = new UseDiscountDAOImpl();
        UseDiscountVO useDiscount = usedao.getUseDiscountByUserID(userID);
        if (useDiscount != null && useDiscount.getDiscountVO().getDiscountNO() != discountNO) {
            System.out.println("Error: User doesn't have this Discount!");
        }
        
        // 4.由參數反推折扣金額跟discountNO，回傳disAmount給Ajax
        int disAmount = discountdao.getDisAmountBy(discountCode).getDisAmount();
        
        Gson gson = new Gson();
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("disAmount", disAmount);
        String json = gson.toJson(jsonData);
		res.getWriter().write(json); 
        
        // 4.由參數反推誰擁有，並把對方的使用權做修正，從0變成1	
		int useDisID = usedao.getUseDisIDBy(discountNO, userID);
		if (useDisID != -1 ) {
		    UseDiscountVO useVO = usedao.findeByPrimaryKey(useDisID);
		    useVO.setDitUsed(1); 
		    // 使用已修改的對象執行更新
		    usedao.update(useVO); 
		}

        
	}
}