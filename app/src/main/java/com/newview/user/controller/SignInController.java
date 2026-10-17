package com.newview.user.controller;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.newview.recovery.Passwords;
import com.newview.util.HibernateUtil;
import org.hibernate.Session;
/** Login reads credentials only; unrelated order graphs must not prevent authentication. */
public class SignInController extends HttpServlet {
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8");r.setContentType("application/json;charset=UTF-8");String account=q.getParameter("account"),password=q.getParameter("password");
  if(account==null||password==null||account.length()>100||password.length()>256){r.sendError(400);return;}
  Object[] row;
  try(Session db=HibernateUtil.getSessionFactory().openSession()){
   row=(Object[])db.createNativeQuery("SELECT userID,userPassword,userAccount FROM user WHERE userAccount=:account").setParameter("account",account).uniqueResult();
  }
  if(row==null||!Passwords.verify(password,(String)row[1])){r.setStatus(401);r.getWriter().write("{\"status\":\"failed\"}");return;}
  HttpSession old=q.getSession(false);if(old!=null)old.invalidate();HttpSession s=q.getSession();s.setAttribute("passwordStamp",row[1]);s.setAttribute("account",row[2]);s.setAttribute("userID",row[0].toString());s.setAttribute("csrf",UUID.randomUUID().toString());
  Map<String,Object> m=new HashMap<>();m.put("status","success");m.put("userID",row[0]);m.put("csrf",s.getAttribute("csrf"));m.put("location",q.getContextPath()+"/forum_home.html");r.getWriter().write(new Gson().toJson(m));
 }
}
