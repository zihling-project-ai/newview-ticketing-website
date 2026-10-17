package com.newview.usediscount.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@WebServlet("/SignUp")
public class SignUpController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();

        
        
        String signUpItem = "";
        String name = req.getParameter("name");
        
        if(name.equals("test")) {
        	out.println("name: "+name);
        	out.println("connected!");
        }else {
        	System.out.println(name);
        	System.out.println("param value get failed, GET success!");
        }
        
        out.println("hi");
        
        
//        ================================================================
//        String name = req.getParameter("name");
        


    }

}