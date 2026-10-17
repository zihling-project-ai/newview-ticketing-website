package com.newview.recovery;
import javax.servlet.http.*;
import org.hibernate.Session;
import com.newview.util.HibernateUtil;
/** Revoke every prior login on password change, including other browsers. */
public final class MemberSessions {
 public static void validate(HttpServletRequest request) {
  HttpSession h=request.getSession(false);
  if(h==null||h.getAttribute("userID")==null)return;
  try(Session s=HibernateUtil.getSessionFactory().openSession()) {
   Object hash=s.createNativeQuery("SELECT userPassword FROM user WHERE userID=:id").setParameter("id",Integer.valueOf(h.getAttribute("userID").toString())).uniqueResult();
   if(hash==null||!hash.equals(h.getAttribute("passwordStamp")))h.invalidate();
  }
 }
}
