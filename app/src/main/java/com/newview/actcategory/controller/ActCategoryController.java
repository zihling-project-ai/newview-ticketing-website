package com.newview.actcategory.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.actcategory.service.ActCategoryService;
import com.newview.actcategory.service.ActCategoryServiceImpl;

@WebServlet("/ActCategoryAdd")
public class ActCategoryController extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		Gson gson = new Gson();
		String category = req.getParameter("category");
		System.out.println(category);
		
		//測試URL
		out.write("a");
		System.out.println("console get");
		
		ActCategoryService actCategorySvc = new ActCategoryService();
		actCategorySvc.addOneActCategory(category);
		
		
		out.write(category);
		
	}
	
	

}
