package com.newview.recovery;

import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.security.SecureRandom;
import java.util.*;
import com.google.gson.Gson;
import com.newview.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

/** Local member recovery: explicit fields, session ownership, no outbound mail. */
public class MemberController extends HttpServlet {
 private void json(HttpServletResponse r, int code, Object body) throws IOException {
  r.setStatus(code); r.setContentType("application/json;charset=UTF-8"); new Gson().toJson(body,r.getWriter());
 }
 private String value(HttpServletRequest q,String key,int max) {
  String v=q.getParameter(key); if(v==null||v.trim().isEmpty()||v.length()>max||v.matches("(?s).*[\\p{Cntrl}<>].*"))throw new IllegalArgumentException("請檢查 "+Map.of("name","姓名","nickname","暱稱","email","信箱","cellphone","手機","birthdate","生日","account","帳號").getOrDefault(key,key)+" 欄位"); return v.trim();
 }
 private Map<String,String> fields(HttpServletRequest q) {
  Map<String,String> m=new HashMap<>();
  for(String k:List.of("name","nickname","email","birthdate"))m.put(k,value(q,k,k.equals("email")?254:100));
  m.put("cellphone",q.getParameter("cellphone")==null||q.getParameter("cellphone").isBlank()?"":value(q,"cellphone",10));
  if(!m.get("email").matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("信箱格式不正確");
  m.put("email",m.get("email").toLowerCase(Locale.ROOT));
  if(!m.get("cellphone").isEmpty()&&!m.get("cellphone").matches("09[0-9]{8}"))throw new IllegalArgumentException("手機需為 09 開頭的十位數字");
  try{LocalDate d=LocalDate.parse(m.get("birthdate"));if(d.isAfter(LocalDate.now())||d.isBefore(LocalDate.of(1900,1,1)))throw new Exception();}catch(Exception e){throw new IllegalArgumentException("生日格式或範圍不正確");}
  return m;
 }
 private void auth(HttpServletRequest q) {if(q.getSession(false)==null||q.getSession(false).getAttribute("userID")==null)throw new SecurityException();}
 private boolean csrf(HttpServletRequest q){HttpSession h=q.getSession(false);return h!=null&&Objects.equals(h.getAttribute("csrf"),q.getHeader("X-CSRF-Token"))&&h.getAttribute("csrf")!=null;}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {
  if(!q.getServletPath().equals("/GetUserInfo")){r.sendError(405);return;}
  try{auth(q);try(Session s=HibernateUtil.getSessionFactory().openSession()){
   Object[] row=(Object[])s.createNativeQuery("SELECT userAccount,userName,userNickname,userEmail,userBirth,userCell,buyAuthority,speakAuthority FROM user WHERE userID=:id").setParameter("id",Integer.valueOf(q.getSession().getAttribute("userID").toString())).uniqueResult();
   if(row==null){r.sendError(401);return;}Map<String,Object> m=new LinkedHashMap<>();String[] keys={"account","name","nickname","email","birthdate","cellphone","buyAuthority","speakAuthority"};for(int i=0;i<keys.length;i++)m.put(keys[i],row[i]==null?"":row[i].toString());json(r,200,m);
  }}catch(SecurityException e){r.sendError(401);}
 }
 @SuppressWarnings("unchecked")
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8"); String path=q.getServletPath();
  if(!csrf(q)){r.sendError(403);return;}
  if(path.equals("/GetUserInfo")){r.sendError(405);return;}
  if(path.equals("/UpdateUserInfo")){try{auth(q);}catch(SecurityException e){r.sendError(401);return;}}
  try {
   Map<String,String> m;
   if(path.equals("/SignUp")&&"verify".equals(q.getParameter("action"))){
    HttpSession h=q.getSession();m=(Map<String,String>)h.getAttribute("pendingMember");
    Long expires=(Long)h.getAttribute("memberExpires");Integer attempts=(Integer)h.getAttribute("memberAttempts");
    if(m==null||expires==null||System.currentTimeMillis()>expires||attempts==null||attempts>=5){h.removeAttribute("pendingMember");throw new IllegalArgumentException("驗證已失效，請重新註冊");}
    h.setAttribute("memberAttempts",attempts+1);
    if(!Objects.equals(q.getParameter("code"),h.getAttribute("memberCode")))throw new IllegalArgumentException("驗證碼不正確");
   }else {
    m=fields(q);
    if(path.equals("/SignUp")){
     String account=value(q,"account",40);if(!account.matches("[A-Za-z][A-Za-z0-9_]{2,39}"))throw new IllegalArgumentException("帳號需為 3–40 位英數或底線，並以英文開頭");
     String password=q.getParameter("password");if(password==null||password.length()<12||password.length()>128)throw new IllegalArgumentException("密碼需為 12–128 個字元");
     if(!password.equals(q.getParameter("confirmPassword")))throw new IllegalArgumentException("兩次密碼不一致");
     m.put("account",account);m.put("passwordHash",Passwords.hash(password));
    }
   }
   try(Session s=HibernateUtil.getSessionFactory().openSession()){
    Transaction tx=s.beginTransaction();
    try {
     Integer id=path.equals("/UpdateUserInfo")?Integer.valueOf(q.getSession().getAttribute("userID").toString()):-1;
     Number existing=(Number)s.createNativeQuery("SELECT COUNT(*) FROM user WHERE userEmail=:email AND userID<>:id").setParameter("email",m.get("email")).setParameter("id",id).uniqueResult();
     if(existing.intValue()>0){tx.rollback();json(r,409,Map.of("error","此信箱已註冊"));return;}
     if(path.equals("/SignUp")){
      Number accounts=(Number)s.createNativeQuery("SELECT COUNT(*) FROM user WHERE userAccount=:account").setParameter("account",m.get("account")).uniqueResult();
      if(accounts.intValue()>0){tx.rollback();json(r,409,Map.of("error","此帳號已註冊"));return;}
      if(!"verify".equals(q.getParameter("action"))){
       tx.commit();HttpSession h=q.getSession();String code=String.format("%06d",new SecureRandom().nextInt(1000000));h.setAttribute("pendingMember",m);h.setAttribute("memberCode",code);h.setAttribute("memberExpires",System.currentTimeMillis()+600000L);h.setAttribute("memberAttempts",0);
       json(r,200,Map.of("status","verificationRequired","localCode",code,"message","本機模擬驗證，不會寄信；十分鐘內有效，最多五次嘗試"));return;
      }
      s.createNativeQuery("INSERT INTO user(userAccount,userPassword,userName,userNickname,userEmail,userBirth,userCell,buyAuthority,speakAuthority) VALUES(:account,:passwordHash,:name,:nickname,:email,:birthdate,:cellphone,1,1)").setParameter("account",m.get("account")).setParameter("passwordHash",m.get("passwordHash")).setParameter("name",m.get("name")).setParameter("nickname",m.get("nickname")).setParameter("email",m.get("email")).setParameter("birthdate",m.get("birthdate")).setParameter("cellphone",m.get("cellphone")).executeUpdate();
      tx.commit();q.getSession().removeAttribute("pendingMember");q.getSession().removeAttribute("memberCode");json(r,201,Map.of("status","success"));
     }else {
      s.createNativeQuery("UPDATE user SET userName=:name,userNickname=:nickname,userEmail=:email,userBirth=:birthdate,userCell=:cellphone WHERE userID=:id").setParameter("name",m.get("name")).setParameter("nickname",m.get("nickname")).setParameter("email",m.get("email")).setParameter("birthdate",m.get("birthdate")).setParameter("cellphone",m.get("cellphone")).setParameter("id",id).executeUpdate();tx.commit();json(r,200,Map.of("status","success"));
     }
    }catch(Exception e){if(tx.isActive())tx.rollback();if(e instanceof org.hibernate.exception.ConstraintViolationException){json(r,409,Map.of("error","帳號或信箱已存在"));}else throw e;}
   }
  }catch(IllegalArgumentException e){json(r,400,Map.of("error",e.getMessage()));}
 }
}
