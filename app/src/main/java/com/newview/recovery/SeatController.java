package com.newview.recovery;
import javax.servlet.http.*;import java.io.IOException;import java.util.*;
import org.hibernate.Session;import org.hibernate.Transaction;import com.google.gson.Gson;import com.newview.util.HibernateUtil;
public class SeatController extends HttpServlet {
 private void json(HttpServletResponse r,int status,Object body)throws IOException{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");new Gson().toJson(body,r.getWriter());}
 private int act(HttpServletRequest q){int id=Integer.parseInt(q.getParameter("actID"));if(id<=0)throw new IllegalArgumentException();return id;}
 private int user(HttpServletRequest q){HttpSession h=q.getSession(false);return h==null||h.getAttribute("userID")==null?-1:Integer.parseInt(h.getAttribute("userID").toString());}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {
  try(Session s=HibernateUtil.getSessionFactory().openSession()){int id=act(q);Transaction tx=s.beginTransaction();try{
   Object found=s.createNativeQuery("SELECT actID FROM act WHERE actID=:id FOR UPDATE").setParameter("id",id).uniqueResult();if(found==null||SeatStore.layout(s,id).isEmpty()){tx.rollback();r.sendError(404);return;}
   Object result=SeatStore.map(s,user(q),id);tx.commit();json(r,200,Map.of("seats",result,"holdSeconds",SeatStore.TTL,"simulation",true));
  }catch(Exception e){if(tx.isActive())tx.rollback();throw e;}}
  catch(IllegalArgumentException e){r.sendError(400);}catch(redis.clients.jedis.exceptions.JedisException e){json(r,503,Map.of("error","座位暫存服務無法使用，請稍後重試"));}
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8");int user=user(q);if(user<0){r.sendError(401);return;}HttpSession h=q.getSession(false);if(h.getAttribute("csrf")==null||!Objects.equals(q.getHeader("X-CSRF-Token"),h.getAttribute("csrf"))){r.sendError(403);return;}
  SeatStore.Claim claim=null;boolean committed=false;
  try(Session s=HibernateUtil.getSessionFactory().openSession()){int id=act(q);Transaction tx=s.beginTransaction();try{
   s.createNativeQuery("SELECT userID FROM user WHERE userID=:id FOR UPDATE").setParameter("id",user).uniqueResult();s.createNativeQuery("SELECT actID FROM act WHERE actID=:id FOR UPDATE").setParameter("id",id).uniqueResult();
   List<String> layout=SeatStore.layout(s,id);if(layout.isEmpty()){tx.rollback();r.sendError(404);return;}List<Object[]> old=SeatStore.selected(s,user,id);
   String action=q.getParameter("action");List<String> chosen=new ArrayList<>();String token=UUID.randomUUID().toString();
   if("hold".equals(action)){
    Object ended=s.createNativeQuery("SELECT CASE WHEN actDate<CURRENT_DATE THEN 1 ELSE 0 END FROM act WHERE actID=:act").setParameter("act",id).uniqueResult();if(ended instanceof Number&&((Number)ended).intValue()==1){tx.rollback();json(r,409,Map.of("error","活動已結束，無法保留座位"));return;}
    String value=q.getParameter("seats");if(value==null||value.length()>200)throw new IllegalArgumentException();chosen=new ArrayList<>(Arrays.asList(value.split(",",-1)));Collections.sort(chosen);
    if(chosen.isEmpty()||chosen.size()>10||new HashSet<>(chosen).size()!=chosen.size()||!layout.containsAll(chosen))throw new IllegalArgumentException();
    for(String seat:chosen){Object booked=s.createNativeQuery("SELECT orderListID FROM local_seat_booking WHERE actID=:act AND seatCode=:seat").setParameter("act",id).setParameter("seat",seat).uniqueResult();if(booked!=null){tx.rollback();json(r,409,Map.of("error","部分座位已分配，請重新選位"));return;}}
    claim=SeatStore.claim(user,id,chosen,token);if(claim==null){tx.rollback();json(r,409,Map.of("error","部分座位已被暫鎖，請重新選位"));return;}
    Number others=(Number)s.createNativeQuery("SELECT COALESCE(SUM(cartQuantity),0) FROM cartact WHERE userID=:user AND actID<>:act").setParameter("user",user).setParameter("act",id).uniqueResult();if(others.intValue()+chosen.size()>50)throw new IllegalArgumentException();
   }else if(!"release".equals(action))throw new IllegalArgumentException();
   s.createNativeQuery("DELETE FROM local_cart_seat WHERE userID=:user AND actID=:act").setParameter("user",user).setParameter("act",id).executeUpdate();
   if(chosen.isEmpty())s.createNativeQuery("DELETE FROM cartact WHERE userID=:user AND actID=:act").setParameter("user",user).setParameter("act",id).executeUpdate();
   else {for(String seat:chosen)s.createNativeQuery("INSERT INTO local_cart_seat(userID,actID,seatCode,holdToken) VALUES(:user,:act,:seat,:token)").setParameter("user",user).setParameter("act",id).setParameter("seat",seat).setParameter("token",token).executeUpdate();s.createNativeQuery("INSERT INTO cartact(userID,actID,cartQuantity) VALUES(:user,:act,:qty) ON DUPLICATE KEY UPDATE cartQuantity=:qty").setParameter("user",user).setParameter("act",id).setParameter("qty",chosen.size()).executeUpdate();}
   tx.commit();committed=true;try{SeatStore.release(user,id,old);}catch(redis.clients.jedis.exceptions.JedisException ignored){}json(r,200,Map.of("status","success","seats",chosen,"holdSeconds",SeatStore.TTL));
  }catch(Exception e){if(tx.isActive())tx.rollback();throw e;}}
  catch(IllegalArgumentException e){json(r,400,Map.of("error","請選擇 1–10 個有效且不重複的座位，每次購物車最多 50 張"));}
  catch(redis.clients.jedis.exceptions.JedisException e){json(r,503,Map.of("error","座位暫存無法使用，未完成選位"));}
  finally{if(claim!=null&&!committed)try{claim.rollback();}catch(redis.clients.jedis.exceptions.JedisException ignored){}}
 }
}
