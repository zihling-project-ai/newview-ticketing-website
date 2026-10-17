package com.newview.recovery;
import javax.servlet.http.*;import java.io.IOException;import java.util.*;
import com.google.gson.Gson;import org.hibernate.Session;import org.hibernate.Transaction;import com.newview.util.HibernateUtil;
/** Cancel only an owned, unused local simulation; never performs payment/refund. */
public class CancelOrderController extends HttpServlet {
 private void json(HttpServletResponse r,int status,Object value)throws IOException{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");new Gson().toJson(value,r.getWriter());}
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8");HttpSession h=q.getSession(false);if(h==null||h.getAttribute("userID")==null){r.sendError(401);return;}
  if(h.getAttribute("csrf")==null||!Objects.equals(q.getHeader("X-CSRF-Token"),h.getAttribute("csrf"))){r.sendError(403);return;}
  if(!"true".equals(q.getParameter("confirmCancellation"))){json(r,400,Map.of("error","請確認取消此本機模擬訂單"));return;}
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   int user=Integer.parseInt(h.getAttribute("userID").toString()),order=Integer.parseInt(q.getParameter("orderID"));if(order<=0)throw new IllegalArgumentException();
   Transaction tx=s.beginTransaction();try{
    s.createNativeQuery("SELECT userID FROM user WHERE userID=:user FOR UPDATE").setParameter("user",user).uniqueResult();
    Object[] row=(Object[])s.createNativeQuery("SELECT o.ordType,EXISTS(SELECT 1 FROM local_simulation_order m WHERE m.orderID=o.orderID) FROM orders o WHERE o.orderID=:order AND o.userID=:user FOR UPDATE").setParameter("order",order).setParameter("user",user).uniqueResult();
    if(row==null){tx.rollback();r.sendError(404);return;}
    if(((Number)row[1]).intValue()!=1){tx.rollback();json(r,409,Map.of("error","目前只開放取消本機模擬訂單"));return;}
    if(!(row[0] instanceof Number)){tx.rollback();json(r,409,Map.of("error","這筆訂單狀態不明，無法取消"));return;}
    int state=((Number)row[0]).intValue();
    if(state==2){tx.commit();json(r,200,Map.of("status","cancelled","alreadyCancelled",true,"releasedSeats",0,"refunded",false));return;}
    if(state!=3){tx.rollback();json(r,409,Map.of("error","這筆訂單目前狀態無法取消"));return;}
    List<?> acts=s.createNativeQuery("SELECT DISTINCT actID FROM orderlist WHERE orderID=:order AND actID IS NOT NULL ORDER BY actID").setParameter("order",order).list();
    for(Object act:acts)s.createNativeQuery("SELECT actID FROM act WHERE actID=:act FOR UPDATE").setParameter("act",act).uniqueResult();
    Number used=(Number)s.createNativeQuery("SELECT COUNT(*) FROM orderlist WHERE orderID=:order AND (usedType IS NULL OR usedType<>0)").setParameter("order",order).uniqueResult();
    if(used.intValue()>0){tx.rollback();json(r,409,Map.of("error","已有使用或狀態不明的票券，無法整筆取消"));return;}
    List<?> booked=s.createNativeQuery("SELECT b.actID,b.seatCode,b.holdToken FROM local_seat_booking b JOIN orderlist l ON l.orderListID=b.orderListID WHERE l.orderID=:order").setParameter("order",order).list();
    // Scope to original ticket IDs, never to the activity or seat alone.
    int released=s.createNativeQuery("DELETE b FROM local_seat_booking b JOIN orderlist l ON l.orderListID=b.orderListID WHERE l.orderID=:order").setParameter("order",order).executeUpdate();
    s.createNativeQuery("UPDATE orders SET ordType=2 WHERE orderID=:order AND userID=:user").setParameter("order",order).setParameter("user",user).executeUpdate();
    s.createNativeQuery("UPDATE local_simulation_order SET cancelledAt=CURRENT_TIMESTAMP(6) WHERE orderID=:order").setParameter("order",order).executeUpdate();
    boolean couponRestored=CouponStore.restore(s,user,order);
    tx.commit();
    for(Object value:booked){Object[] seat=(Object[])value;if(seat[2]!=null)try{SeatStore.release(user,((Number)seat[0]).intValue(),Collections.singletonList(new Object[]{seat[1],seat[2]}));}catch(redis.clients.jedis.exceptions.JedisException ignored){}}
    json(r,200,Map.of("status","cancelled","alreadyCancelled",false,"releasedSeats",released,"couponRestored",couponRestored,"refunded",false));
   }catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }catch(IllegalArgumentException e){r.sendError(400);}
 }
}
