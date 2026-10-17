package com.newview.recovery;
import javax.servlet.http.*;import java.io.IOException;import java.util.*;
import org.hibernate.Session;import org.hibernate.Transaction;import com.newview.util.HibernateUtil;import com.google.gson.Gson;
/** One immutable plain-text local review per genuinely redeemed, owned ticket. */
public class ReviewController extends HttpServlet {
 private static class Invalid extends RuntimeException{final int status;Invalid(int s,String m){super(m);status=s;}}
 private void json(HttpServletResponse r,int status,Object body)throws IOException{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");new Gson().toJson(body,r.getWriter());}
 private int id(String text){try{int id=Integer.parseInt(text);if(id<1)throw new Exception();return id;}catch(Exception e){throw new Invalid(400,"活動或票券編號不正確");}}
 private int user(HttpServletRequest q){HttpSession h=q.getSession(false);return h==null||h.getAttribute("userID")==null?-1:Integer.parseInt(h.getAttribute("userID").toString());}
 private Object[] ticket(Session s,int user,int ticket){return (Object[])s.createNativeQuery("SELECT l.reviewContent,l.fiveStarReview,l.localReviewedAt,l.usedType,l.localRedeemedAt,o.ordType,EXISTS(SELECT 1 FROM local_simulation_order m WHERE m.orderID=o.orderID),l.actID FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE l.orderListID=:ticket AND o.userID=:user FOR UPDATE").setParameter("ticket",ticket).setParameter("user",user).uniqueResult();}
 private boolean number(Object o,int n){return o instanceof Number&&((Number)o).intValue()==n;}
 private boolean eligible(Object[] row){return number(row[3],1)&&row[4]!=null&&(number(row[5],1)||number(row[5],3))&&number(row[6],1);}
 private boolean reviewed(Object[] row){return row[2]!=null||row[1] instanceof Number&&((Number)row[1]).intValue()!=0||row[0]!=null&&!row[0].toString().isBlank();}
 private void lock(Session s,int user){s.createNativeQuery("SELECT userID FROM user WHERE userID=:user FOR UPDATE").setParameter("user",user).uniqueResult();}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   if(q.getServletPath().equals("/ActivityReviews")){int act=id(q.getParameter("actID"));if(s.createNativeQuery("SELECT actID FROM act WHERE actID=:act").setParameter("act",act).uniqueResult()==null)throw new Invalid(404,"找不到活動");
    List<?> rows=s.createNativeQuery("SELECT l.fiveStarReview,l.reviewContent,l.localReviewedAt,u.userNickname FROM orderlist l JOIN orders o ON o.orderID=l.orderID JOIN user u ON u.userID=o.userID JOIN local_simulation_order m ON m.orderID=o.orderID WHERE l.actID=:act AND l.usedType=1 AND l.localRedeemedAt IS NOT NULL AND o.ordType IN (1,3) AND l.localReviewedAt IS NOT NULL AND l.fiveStarReview BETWEEN 1 AND 5 ORDER BY l.localReviewedAt DESC,l.orderListID DESC").setParameter("act",act).list();List<Map<String,Object>> reviews=new ArrayList<>();int sum=0;for(Object item:rows){Object[] row=(Object[])item;int rating=((Number)row[0]).intValue();sum+=rating;reviews.add(Map.of("rating",rating,"content",row[1]==null?"":row[1].toString(),"reviewedAt",row[2].toString(),"author",row[3]==null||row[3].toString().isBlank()?"本機會員":row[3].toString()));}
    Map<String,Object> body=new LinkedHashMap<>();body.put("count",reviews.size());body.put("average",reviews.isEmpty()?null:Math.round(sum*100.0/reviews.size())/100.0);body.put("reviews",reviews);json(r,200,body);return;
   }
   int user=user(q);if(user<0){r.sendError(401);return;}int ticket=id(q.getParameter("ticketID"));Transaction tx=s.beginTransaction();try{lock(s,user);Object[] row=ticket(s,user,ticket);if(row==null)throw new Invalid(404,"找不到票券");Map<String,Object> body=new LinkedHashMap<>();body.put("canReview",eligible(row)&&!reviewed(row));body.put("reviewed",reviewed(row));body.put("rating",row[1]);body.put("content",row[0]);body.put("reviewedAt",row[2]==null?null:row[2].toString());tx.commit();json(r,200,body);}catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }catch(Invalid e){json(r,e.status,Map.of("error",e.getMessage()));}
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8");if(!q.getServletPath().equals("/TicketReview")){r.sendError(405);return;}int user=user(q);if(user<0){r.sendError(401);return;}HttpSession h=q.getSession(false);if(h.getAttribute("csrf")==null||!Objects.equals(q.getHeader("X-CSRF-Token"),h.getAttribute("csrf"))){r.sendError(403);return;}
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   int ticket=id(q.getParameter("ticketID"));String rating=q.getParameter("rating"),content=q.getParameter("content");
   if(rating==null||!rating.matches("[1-5]")||content==null||content.isBlank()||content.length()>255||content.matches("(?s).*[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F].*"))throw new Invalid(400,"請選擇 1–5 星，並填寫 1–255 字的純文字評論");
   Transaction tx=s.beginTransaction();try{lock(s,user);Object[] row=ticket(s,user,ticket);if(row==null)throw new Invalid(404,"找不到票券");if(!eligible(row))throw new Invalid(409,"只有本人已完成本機核銷、且未取消的模擬票券可以評價");if(reviewed(row))throw new Invalid(409,"每張票券限評價一次，這張票已有評價");
    int changed=s.createNativeQuery("UPDATE orderlist SET fiveStarReview=:rating,reviewContent=:content,localReviewedAt=CURRENT_TIMESTAMP(6) WHERE orderListID=:ticket AND localReviewedAt IS NULL AND (fiveStarReview IS NULL OR fiveStarReview=0) AND (reviewContent IS NULL OR TRIM(reviewContent)='')").setParameter("rating",Integer.parseInt(rating)).setParameter("content",content.trim()).setParameter("ticket",ticket).executeUpdate();if(changed!=1)throw new Invalid(409,"票券已有評價，請重新整理");tx.commit();json(r,201,Map.of("status","reviewed"));
   }catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }catch(Invalid e){json(r,e.status,Map.of("error",e.getMessage()));}
 }
}
