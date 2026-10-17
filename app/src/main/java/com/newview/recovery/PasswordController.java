package com.newview.recovery;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.google.gson.Gson;
import com.newview.util.HibernateUtil;

/** Explicitly local simulation; never sends mail or returns a generated password. */
public class PasswordController extends HttpServlet {
 private void json(HttpServletResponse r,int status,Object body)throws IOException{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");new Gson().toJson(body,r.getWriter());}
 private void error(HttpServletResponse r,int status,String text)throws IOException{json(r,status,Map.of("error",text));}
 private String newPassword(HttpServletRequest q){
  String p=q.getParameter("newPassword");
  if(p==null||p.length()<12||p.length()>128)throw new IllegalArgumentException("新密碼需為 12–128 個字元");
  if(!p.equals(q.getParameter("confirmNewPassword")))throw new IllegalArgumentException("兩次新密碼不一致");return p;
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8");
  if(!Set.of("127.0.0.1","0:0:0:0:0:0:0:1","::1").contains(q.getRemoteAddr())){r.sendError(403);return;}
  HttpSession h=q.getSession(false);
  if(q.getServletPath().equals("/UpdateUserPassword")&&(h==null||h.getAttribute("userID")==null)){r.sendError(401);return;}
  if(h==null||h.getAttribute("csrf")==null||!Objects.equals(h.getAttribute("csrf"),q.getHeader("X-CSRF-Token"))){r.sendError(403);return;}
  synchronized(h) {
   try {
    if(q.getServletPath().equals("/RetrievePassword")){issue(q,r,h);return;}
    String password=newPassword(q);
    ResetChallenge challenge=null; int id;
    if(q.getServletPath().equals("/UpdateUserPassword"))id=Integer.parseInt(h.getAttribute("userID").toString());
    else {
     challenge=(ResetChallenge)h.getAttribute("passwordReset");
     if(challenge==null||!challenge.verify(q.getParameter("verificationCode"),System.currentTimeMillis())){error(r,400,"驗證碼錯誤、已失效或超過五次嘗試，請重新取得");return;}
     id=challenge.userID;
    }
    try(Session s=HibernateUtil.getSessionFactory().openSession()) {
     Transaction tx=s.beginTransaction();
     try {
      Object[] row=(Object[])s.createNativeQuery("SELECT userPassword,userEmail FROM user WHERE userID=:id FOR UPDATE").setParameter("id",id).uniqueResult();
      if(row==null){tx.rollback();error(r,400,"會員資料已變更，請重新驗證");return;}
      String stored=row[0].toString();
      if(challenge!=null&&(!stored.equals(challenge.passwordStamp)||!Objects.equals(row[1],challenge.email))){challenge.consume();tx.rollback();error(r,400,"會員資料已變更，請重新取得驗證碼");return;}
      if(challenge==null){String old=q.getParameter("oldPassword");if(old==null||old.length()>128||!Passwords.verify(old,stored)){tx.rollback();error(r,400,"原密碼不正確");return;}}
      if(Passwords.verify(password,stored)){tx.rollback();error(r,400,"新密碼不可與目前密碼相同");return;}
      s.createNativeQuery("UPDATE user SET userPassword=:password WHERE userID=:id").setParameter("password",Passwords.hash(password)).setParameter("id",id).executeUpdate();
      tx.commit();if(challenge!=null)challenge.consume();h.invalidate();json(r,200,Map.of("status","success","message","密碼已更新，請重新登入"));
     }catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
    }
   }catch(IllegalArgumentException e){error(r,400,e.getMessage());}
  }
 }
 private void issue(HttpServletRequest q,HttpServletResponse r,HttpSession h)throws IOException {
  String email=q.getParameter("email"),account=q.getParameter("account");
  if(email==null||email.length()>254||!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")||account==null||!account.matches("[A-Za-z][A-Za-z0-9_]{2,39}")){error(r,400,"請填寫有效的帳號與註冊信箱");return;}
  long now=System.currentTimeMillis();Long last=(Long)h.getAttribute("resetLastRequested");
  if(last!=null&&now-last<30000L){error(r,429,"請等待 30 秒後再取得驗證碼");return;}
  h.setAttribute("resetLastRequested",now);h.removeAttribute("passwordReset");
  Map<String,Object> result=new LinkedHashMap<>();result.put("status","verificationRequired");result.put("message","本機模擬驗證，不會寄信；若帳號與信箱相符，驗證碼會顯示於此，十分鐘內有效、最多五次嘗試。");
  try(Session s=HibernateUtil.getSessionFactory().openSession()) {
   Object[] row=(Object[])s.createNativeQuery("SELECT userID,userPassword,userEmail FROM user WHERE userAccount=:account AND userEmail=:email").setParameter("account",account).setParameter("email",email.trim().toLowerCase(Locale.ROOT)).uniqueResult();
   if(row!=null){String code=String.format(Locale.ROOT,"%06d",new SecureRandom().nextInt(1000000));h.setAttribute("passwordReset",new ResetChallenge(((Number)row[0]).intValue(),row[1].toString(),row[2].toString(),code,now));result.put("localCode",code);}
  }
  json(r,200,result);
 }
}
