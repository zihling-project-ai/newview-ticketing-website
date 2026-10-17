package com.newview.recovery;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newview.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

/** Local checkout with quantity items and Redis-held assigned seats. No real payment. */
public class CartController extends HttpServlet {
 private final Gson gson=new Gson();
 private static class Invalid extends RuntimeException {final int status;Invalid(int s,String m){super(m);status=s;}}
 private void json(HttpServletResponse r,int code,Object body)throws IOException {r.setStatus(code);r.setContentType("application/json;charset=UTF-8");gson.toJson(body,r.getWriter());}
 private int number(String value,int max){try{int n=Integer.parseInt(value);if(n<=0||n>max)throw new Exception();return n;}catch(Exception e){throw new Invalid(400,"活動編號或數量不正確");}}
 private Integer user(HttpServletRequest q){HttpSession h=q.getSession(false);return h==null||h.getAttribute("userID")==null?null:Integer.valueOf(h.getAttribute("userID").toString());}
 private void lockUser(Session s,int user){s.createNativeQuery("SELECT userID FROM user WHERE userID=:id FOR UPDATE").setParameter("id",user).uniqueResult();}
 private List<Map<String,Object>> items(Session s,int user){
  List<?> data=s.createNativeQuery("SELECT c.actID,c.cartQuantity,a.actName,a.actPrice,a.pubID,p.pubName,CASE WHEN a.actDate<CURRENT_DATE THEN 1 ELSE 0 END,a.actDate,a.cityAddress,a.time FROM cartact c LEFT JOIN act a ON a.actID=c.actID LEFT JOIN Publisher p ON p.pubID=a.pubID WHERE c.userID=:id ORDER BY c.actID FOR UPDATE").setParameter("id",user).list();
  List<Map<String,Object>> result=new ArrayList<>();for(Object value:data){Object[] row=(Object[])value;Map<String,Object> item=new LinkedHashMap<>();item.put("actID",row[0]);item.put("quantity",row[1]);item.put("actName",row[2]);item.put("price",row[3]);item.put("pubID",row[4]);item.put("publisher",row[5]);item.put("ended",row[6]);item.put("actDate",row[7]==null?null:row[7].toString());item.put("venue",row[8]);item.put("time",row[9]==null?null:row[9].toString());item.putAll(SeatStore.cart(s,user,((Number)row[0]).intValue()));result.add(item);}return result;
 }
 private long total(List<Map<String,Object>> items){long total=0;int count=0;for(Map<String,Object> item:items){if(((Number)item.get("ended")).intValue()==1)throw new Invalid(409,"購物車含已結束活動，請取消勾選或移除後再結帳");if(Boolean.TRUE.equals(item.get("seatMode"))&&(!Boolean.TRUE.equals(item.get("seatsValid"))||((List<?>)item.get("seats")).size()!=((Number)item.get("quantity")).intValue()))throw new Invalid(409,"座位暫鎖已逾時或需要重新選位");Object p=item.get("price"),q=item.get("quantity");if(p==null||q==null||((Number)p).longValue()<0||((Number)q).intValue()<1||((Number)q).intValue()>10)throw new Invalid(409,"活動價格或購物車數量已失效，請更新購物車");int quantity=((Number)q).intValue();count+=quantity;total+=((Number)p).longValue()*quantity;if(total>Integer.MAX_VALUE||count>50)throw new Invalid(409,"每次最多 50 張，或總金額超過本機支援範圍");}return total;}
 private List<Map<String,Object>> selected(List<Map<String,Object>> list,String value){
  if(value==null)return new ArrayList<>(list);if(value.isBlank())return new ArrayList<>();if(value.length()>600)throw new Invalid(400,"勾選活動資料不正確");Set<Integer> ids=new TreeSet<>();for(String part:value.split(",",-1))if(!ids.add(number(part,Integer.MAX_VALUE)))throw new Invalid(400,"勾選活動不可重複");
  List<Map<String,Object>> out=new ArrayList<>();for(Map<String,Object> item:list)if(ids.remove(((Number)item.get("actID")).intValue()))out.add(item);if(!ids.isEmpty())throw new Invalid(409,"部分活動已不在購物車，請重新整理");return out;
 }
 private String scope(List<Map<String,Object>> list){List<String> ids=new ArrayList<>();for(Map<String,Object> item:list)ids.add(item.get("actID").toString());return String.join(",",ids);}
 private String snapshot(List<Map<String,Object>> items){List<Map<String,Object>> stable=new ArrayList<>();for(Map<String,Object> item:items){Map<String,Object> copy=new LinkedHashMap<>(item);copy.remove("holdExpiresAt");copy.remove("selected");stable.add(copy);}return gson.toJson(stable);}
 private List<Integer> receipt(Session s,int id,String key){String data=(String)s.createNativeQuery("SELECT orderIDs FROM local_checkout_receipt WHERE userID=:id AND requestID=:key").setParameter("id",id).setParameter("key",key).uniqueResult();return data==null?null:gson.fromJson(data,new TypeToken<List<Integer>>(){}.getType());}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {
  Integer id=user(q);if(id==null){r.sendError(401);return;}
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   Transaction tx=s.beginTransaction();try{
    if(q.getServletPath().equals("/LocalCheckout")){
     String key=q.getParameter("requestID");if(key==null||!key.matches("[0-9a-f-]{36}"))throw new Invalid(400,"結帳編號不正確");List<Integer> orders=receipt(s,id,key);if(orders==null)throw new Invalid(404,"找不到這筆模擬結帳");tx.commit();json(r,200,Map.of("simulation",true,"paid",false,"orderIDs",orders,"requestID",key));return;
    }
    lockUser(s,id);List<Map<String,Object>> list=items(s,id);List<Map<String,Object>> chosen=selected(list,q.getParameter("actIDs"));long sum=0;boolean valid=true;String problem="";try{sum=total(chosen);}catch(Invalid e){valid=false;problem=e.getMessage();}Map<String,Object> coupon=CouponStore.quote(s,id,CouponStore.selection(q.getParameter("useDisID")),chosen);String key=UUID.randomUUID().toString();HttpSession h=q.getSession();h.setAttribute("cartCoupon",gson.toJson(coupon));h.setAttribute("cartQuoteKey",key);h.setAttribute("cartQuote",snapshot(chosen));h.setAttribute("cartScope",scope(chosen));h.setAttribute("cartQuoteExpiry",System.currentTimeMillis()+600000L);for(Map<String,Object> item:list)item.put("selected",chosen.contains(item));tx.commit();Map<String,Object> response=new LinkedHashMap<>();response.put("actIDs",scope(chosen));response.putAll(Map.of("items",list,"total",sum,"requestID",key,"valid",valid,"problem",problem,"coupon",coupon,"discount",coupon.get("amount"),"payable",Math.max(0,sum-((Number)coupon.get("amount")).longValue())));json(r,200,response);
   }catch(CouponStore.Invalid e){if(tx.isActive())tx.rollback();json(r,e.status,Map.of("error",e.getMessage()));}catch(Invalid e){if(tx.isActive())tx.rollback();json(r,e.status,Map.of("error",e.getMessage()));}catch(redis.clients.jedis.exceptions.JedisException e){if(tx.isActive())tx.rollback();json(r,503,Map.of("error","座位暫存服務無法使用，請稍後重試"));}catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8");Integer id=user(q);if(id==null){r.sendError(401);return;}
  HttpSession h=q.getSession(false);if(h.getAttribute("csrf")==null||!Objects.equals(h.getAttribute("csrf"),q.getHeader("X-CSRF-Token"))){r.sendError(403);return;}
  if(!Set.of("127.0.0.1","::1","0:0:0:0:0:0:0:1").contains(q.getRemoteAddr())){r.sendError(403);return;}
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   Transaction tx=s.beginTransaction();try{lockUser(s,id);
    if(q.getServletPath().equals("/LocalCheckout")){checkout(q,r,s,tx,id,h);return;}
    int act=number(q.getParameter("actID"),Integer.MAX_VALUE);List<Object[]> released=SeatStore.selected(s,id,act);String action=q.getParameter("action");
    if("remove".equals(action)){s.createNativeQuery("DELETE FROM local_cart_seat WHERE userID=:user AND actID=:act").setParameter("user",id).setParameter("act",act).executeUpdate();s.createNativeQuery("DELETE FROM cartact WHERE userID=:user AND actID=:act").setParameter("user",id).setParameter("act",act).executeUpdate();}
    else if(Set.of("add","update").contains(action==null?"":action)){
     if(!SeatStore.layout(s,act).isEmpty())throw new Invalid(409,"此活動須從選位頁調整座位，不能直接修改數量");int quantity=number(q.getParameter("quantity"),10);
     Object price=s.createNativeQuery("SELECT actPrice FROM act WHERE actID=:act FOR UPDATE").setParameter("act",act).uniqueResult();
     Object ended=s.createNativeQuery("SELECT CASE WHEN actDate<CURRENT_DATE THEN 1 ELSE 0 END FROM act WHERE actID=:act").setParameter("act",act).uniqueResult();if(ended instanceof Number&&((Number)ended).intValue()==1)throw new Invalid(409,"活動已結束，無法加入購物車");
     if(price==null||((Number)price).longValue()<0)throw new Invalid(400,"活動不存在或尚未提供有效價格");
     Object current=s.createNativeQuery("SELECT cartQuantity FROM cartact WHERE userID=:user AND actID=:act").setParameter("user",id).setParameter("act",act).uniqueResult();
     if("update".equals(action)&&current==null)throw new Invalid(404,"購物車沒有此活動");
     int next="add".equals(action)?quantity+(current==null?0:((Number)current).intValue()):quantity;
     if(next>10)throw new Invalid(400,"每個活動最多 10 張");
     s.createNativeQuery("INSERT INTO cartact(userID,actID,cartQuantity) VALUES(:user,:act,:quantity) ON DUPLICATE KEY UPDATE cartQuantity=:quantity").setParameter("user",id).setParameter("act",act).setParameter("quantity",next).executeUpdate();Number cartCount=(Number)s.createNativeQuery("SELECT COALESCE(SUM(cartQuantity),0) FROM cartact WHERE userID=:user").setParameter("user",id).uniqueResult();if(cartCount.intValue()>50)throw new Invalid(409,"購物車每次最多 50 張");
    }else throw new Invalid(400,"不支援的購物車操作");
    tx.commit();if("remove".equals(action))try{SeatStore.release(id,act,released);}catch(redis.clients.jedis.exceptions.JedisException ignored){}json(r,200,Map.of("status","success"));
   }catch(CouponStore.Invalid e){if(tx.isActive())tx.rollback();json(r,e.status,Map.of("error",e.getMessage()));}catch(Invalid e){if(tx.isActive())tx.rollback();json(r,e.status,Map.of("error",e.getMessage()));}catch(redis.clients.jedis.exceptions.JedisException e){if(tx.isActive())tx.rollback();json(r,503,Map.of("error","座位暫存服務無法使用，請稍後重試"));}catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
 private void checkout(HttpServletRequest q,HttpServletResponse r,Session s,Transaction tx,int id,HttpSession h)throws IOException {
  String key=q.getParameter("requestID");if(key==null||!key.matches("[0-9a-f-]{36}"))throw new Invalid(400,"請先確認購物車再結帳");
  List<Integer> existing=receipt(s,id,key);if(existing!=null){tx.commit();json(r,200,Map.of("simulation",true,"paid",false,"requestID",key,"orderIDs",existing));return;}
  if(!"true".equals(q.getParameter("confirmSimulation")))throw new Invalid(400,"請確認這是本機模擬結帳");
  Long expires=(Long)h.getAttribute("cartQuoteExpiry");if(!key.equals(h.getAttribute("cartQuoteKey"))||expires==null||expires<=System.currentTimeMillis())throw new Invalid(409,"購物車確認已失效，請重新載入");
  List<Map<String,Object>> list=selected(items(s,id),q.getParameter("actIDs"));if(list.isEmpty())throw new Invalid(400,"購物車是空的");if(!scope(list).equals(h.getAttribute("cartScope")))throw new Invalid(409,"勾選項目已變更，請重新試算");total(list);
  if(!snapshot(list).equals(h.getAttribute("cartQuote")))throw new Invalid(409,"數量或活動價格已變更，請重新確認購物車");
  Map<String,Object> coupon=CouponStore.quote(s,id,CouponStore.selection(q.getParameter("useDisID")),list);
  // The preview binds coupon ownership, dates, face value, scope and effective reduction.
  String quoted=(String)h.getAttribute("cartCoupon");if(!gson.toJson(coupon).equals(quoted))throw new Invalid(409,"優惠券或折扣已變更，請重新試算");
  Map<Integer,List<Map<String,Object>>> groups=new LinkedHashMap<>();for(Map<String,Object> item:list){Object pub=item.get("pubID");groups.computeIfAbsent(pub==null?0:((Number)pub).intValue(),v->new ArrayList<>()).add(item);}
  s.createNativeQuery("INSERT INTO local_checkout_receipt(requestID,userID,orderIDs) VALUES(:key,:user,'[]')").setParameter("key",key).setParameter("user",id).executeUpdate();List<Integer> orders=new ArrayList<>();
  for(Map.Entry<Integer,List<Map<String,Object>>> group:groups.entrySet()){
   int count=0;for(Map<String,Object> item:group.getValue())count+=((Number)item.get("quantity")).intValue();
   boolean apply=((Number)coupon.get("useDisID")).intValue()!=0&&((Number)coupon.get("targetPubID")).intValue()==group.getKey();int reduction=apply?((Number)coupon.get("amount")).intValue():0;
   NativeQuery<?> insert=s.createNativeQuery("INSERT INTO orders(userID,pubID,ordTime,ordTotal,ordType,actQuantity,discount,discountPrice,discountNO) VALUES(:user,"+(group.getKey()==0?"NULL":":pub")+",CURRENT_TIMESTAMP(6),:total,3,:count,:reduction,:payable,"+(apply?":discountNO":"NULL")+")").setParameter("user",id).setParameter("total",(int)total(group.getValue())).setParameter("count",count).setParameter("reduction",reduction).setParameter("payable",(int)total(group.getValue())-reduction);if(apply)insert.setParameter("discountNO",coupon.get("discountNO"));if(group.getKey()!=0)insert.setParameter("pub",group.getKey());insert.executeUpdate();
   int order=((Number)s.createNativeQuery("SELECT LAST_INSERT_ID()").uniqueResult()).intValue();orders.add(order);if(apply)CouponStore.consume(s,id,order,coupon);
   s.createNativeQuery("INSERT INTO local_simulation_order(orderID,requestID) VALUES(:order,:key)").setParameter("order",order).setParameter("key",key).executeUpdate();
   for(Map<String,Object> item:group.getValue())for(int i=0;i<((Number)item.get("quantity")).intValue();i++){
    List<?> seats=(List<?>)item.get("seats");String seat=seats.isEmpty()?null:seats.get(i).toString();
    NativeQuery<?> ticket=s.createNativeQuery("INSERT INTO orderlist(orderID,actID,actTotal,usedType,OrderListTime,seatRowsColumns) VALUES(:order,:act,:price,0,CURRENT_TIMESTAMP(6),"+(seat==null?"NULL":":seat")+")").setParameter("order",order).setParameter("act",item.get("actID")).setParameter("price",item.get("price"));if(seat!=null)ticket.setParameter("seat",seat);ticket.executeUpdate();
    if(seat!=null){int ticketID=((Number)s.createNativeQuery("SELECT LAST_INSERT_ID()").uniqueResult()).intValue();String holdToken=(String)s.createNativeQuery("SELECT holdToken FROM local_cart_seat WHERE userID=:user AND actID=:act AND seatCode=:seat").setParameter("user",id).setParameter("act",item.get("actID")).setParameter("seat",seat).uniqueResult();s.createNativeQuery("INSERT INTO local_seat_booking(actID,seatCode,orderListID,holdToken) VALUES(:act,:seat,:ticket,:token)").setParameter("act",item.get("actID")).setParameter("seat",seat).setParameter("ticket",ticketID).setParameter("token",holdToken).executeUpdate();}
   }
  }
  s.createNativeQuery("UPDATE local_checkout_receipt SET orderIDs=:orders WHERE requestID=:key AND userID=:user").setParameter("orders",gson.toJson(orders)).setParameter("key",key).setParameter("user",id).executeUpdate();
  Map<Integer,List<Object[]>> releaseAfter=new LinkedHashMap<>();for(Map<String,Object> item:list){int act=((Number)item.get("actID")).intValue();releaseAfter.put(act,SeatStore.selected(s,id,act));}
  for(Map<String,Object> item:list){s.createNativeQuery("DELETE FROM local_cart_seat WHERE userID=:user AND actID=:act").setParameter("user",id).setParameter("act",item.get("actID")).executeUpdate();s.createNativeQuery("DELETE FROM cartact WHERE userID=:user AND actID=:act").setParameter("user",id).setParameter("act",item.get("actID")).executeUpdate();}tx.commit();for(Map.Entry<Integer,List<Object[]>> entry:releaseAfter.entrySet())try{SeatStore.release(id,entry.getKey(),entry.getValue());}catch(redis.clients.jedis.exceptions.JedisException ignored){}json(r,201,Map.of("simulation",true,"paid",false,"requestID",key,"orderIDs",orders));
 }
}
