package com.newview.recovery;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.google.gson.Gson;
import com.newview.util.HibernateUtil;

/** Scoped read-only orders and explicit, idempotent activity favorites. */
public class MemberLibraryController extends HttpServlet {
 private void json(HttpServletResponse r,int status,Object body)throws IOException{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");new Gson().toJson(body,r.getWriter());}
 private Integer user(HttpServletRequest q){HttpSession h=q.getSession(false);return h==null||h.getAttribute("userID")==null?null:Integer.valueOf(h.getAttribute("userID").toString());}
 private int positive(String text){int id=Integer.parseInt(text);if(id<=0)throw new IllegalArgumentException();return id;}
 private List<Map<String,Object>> rows(List<?> data,String... keys){List<Map<String,Object>> result=new ArrayList<>();for(Object value:data){Object[] row=(Object[])value;Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<keys.length;i++){Object v=row[i];m.put(keys[i],v instanceof java.util.Date?v.toString():v);}result.add(m);}return result;}
 private List<Map<String,Object>> orders(Session s,int id){List<Map<String,Object>> result=rows(s.createNativeQuery("SELECT o.orderID,o.ordTime,o.ordTotal,o.ordType,o.actQuantity,p.pubName,o.discountPrice,o.discount,o.discountNO,EXISTS(SELECT 1 FROM local_simulation_order marker WHERE marker.orderID=o.orderID),(SELECT m.cancelledAt FROM local_simulation_order m WHERE m.orderID=o.orderID),(SELECT COUNT(*) FROM orderlist used WHERE used.orderID=o.orderID AND used.usedType=1) FROM orders o LEFT JOIN Publisher p ON p.pubID=o.pubID WHERE o.userID=:id ORDER BY o.ordTime DESC,o.orderID DESC").setParameter("id",id).list(),"orderID","ordTime","ordTotal","ordType","actQuantity","publisher","discountPrice","discount","discountNO","simulation","cancelledAt","usedCount");List<Map<String,Object>> details=rows(s.createNativeQuery("SELECT l.orderID,l.orderListID,l.actID,a.actName,l.actTotal,l.usedType,l.seatRowsColumns,a.actDate,a.cityAddress,a.time FROM orderlist l JOIN orders o ON o.orderID=l.orderID LEFT JOIN act a ON a.actID=l.actID WHERE o.userID=:id ORDER BY l.orderID,l.orderListID").setParameter("id",id).list(),"orderID","orderListID","actID","actName","actTotal","usedType","seat","actDate","venue","time");
  Map<String,List<Map<String,Object>>> grouped=new HashMap<>();for(Map<String,Object> item:details)grouped.computeIfAbsent(item.get("orderID").toString(),k->new ArrayList<>()).add(item);
  for(Map<String,Object> order:result)order.put("items",grouped.getOrDefault(order.get("orderID").toString(),Collections.emptyList()));return result;}

 private List<Map<String,Object>> favorites(Session s,int id){return rows(s.createNativeQuery("SELECT a.actID,a.actName,a.actIntroduce,a.actDate,a.actPrice,a.cityAddress,EXISTS(SELECT 1 FROM actpic pic WHERE pic.actID=a.actID) FROM myLike m JOIN act a ON a.actID=m.actID WHERE m.userID=:id ORDER BY m.myLikeID DESC").setParameter("id",id).list(),"actID","actName","actIntroduce","actDate","actPrice","cityAddress","hasImage");}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {
  Integer id=user(q);if(id==null){r.sendError(401);return;}
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   switch(q.getServletPath()){
    case "/MemberPage": json(r,200,Map.of("orders",orders(s,id),"mylike",favorites(s,id)));break;
    case "/MemberOrders":
     if(q.getParameter("orderID")==null){json(r,200,orders(s,id));break;}
     int order=positive(q.getParameter("orderID"));
     Object found=s.createNativeQuery("SELECT orderID FROM orders WHERE orderID=:order AND userID=:user").setParameter("order",order).setParameter("user",id).uniqueResult();
     if(found==null){r.sendError(404);return;}
     // Join owner again so the item query cannot expose another member's order.
     List<Map<String,Object>> items=rows(s.createNativeQuery("SELECT l.orderListID,l.actID,a.actName,l.actTotal,l.usedType,l.seatRowsColumns,l.OrderListTime,a.actDate FROM orderlist l JOIN orders o ON o.orderID=l.orderID LEFT JOIN act a ON a.actID=l.actID WHERE o.orderID=:order AND o.userID=:user ORDER BY l.orderListID").setParameter("order",order).setParameter("user",id).list(),"orderListID","actID","actName","actTotal","usedType","seat","issuedAt","actDate");json(r,200,items);break;
    case "/MemberFavorites":json(r,200,favorites(s,id));break;
    case "/MemberActivities":json(r,200,rows(s.createNativeQuery("SELECT a.actID,a.actName,a.actIntroduce,a.actDate,a.actPrice,a.cityAddress,EXISTS(SELECT 1 FROM actpic pic WHERE pic.actID=a.actID),CASE WHEN m.myLikeID IS NULL THEN 0 ELSE 1 END FROM act a LEFT JOIN myLike m ON m.actID=a.actID AND m.userID=:id ORDER BY a.actDate,a.actID").setParameter("id",id).list(),"actID","actName","actIntroduce","actDate","actPrice","cityAddress","hasImage","favorite"));break;
    case "/MemberActivityImage":
     int act=positive(q.getParameter("actID"));
     byte[] image=(byte[])s.createNativeQuery("SELECT actPic FROM actpic WHERE actID=:act ORDER BY actPicID LIMIT 1").setParameter("act",act).uniqueResult();
     if(image==null){r.sendError(404);return;}try{image=ImageSupport.normalize(image);}catch(IllegalArgumentException e){r.sendError(404);return;}r.setContentType("image/png");r.getOutputStream().write(image);break;
    default:r.sendError(405);
   }
  }catch(IllegalArgumentException e){r.sendError(400);}
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  Integer id=user(q);if(id==null){r.sendError(401);return;}
  if(!Set.of("/MemberFavorites","/likeChange").contains(q.getServletPath())){r.sendError(405);return;}
  HttpSession h=q.getSession(false);if(h.getAttribute("csrf")==null||!Objects.equals(q.getHeader("X-CSRF-Token"),h.getAttribute("csrf"))){r.sendError(403);return;}
  String action=q.getParameter("action");if(!Set.of("add","remove").contains(action==null?"":action)){r.sendError(400);return;}
  try{int act=positive(q.getParameter("actID"));try(Session s=HibernateUtil.getSessionFactory().openSession()){
   Transaction tx=s.beginTransaction();try{
    // Serialize favorite changes for this member; the unique constraint is a second guard.
    s.createNativeQuery("SELECT userID FROM user WHERE userID=:id FOR UPDATE").setParameter("id",id).uniqueResult();
    Object exists=s.createNativeQuery("SELECT actID FROM act WHERE actID=:act").setParameter("act",act).uniqueResult();
    if(exists==null){tx.rollback();r.sendError(404);return;}
    if(action.equals("add")){s.createNativeQuery("INSERT INTO myLike(userID,actID) VALUES(:id,:act) ON DUPLICATE KEY UPDATE myLikeID=myLikeID").setParameter("id",id).setParameter("act",act).executeUpdate();}
    else{s.createNativeQuery("DELETE FROM myLike WHERE userID=:id AND actID=:act").setParameter("id",id).setParameter("act",act).executeUpdate();}
    tx.commit();json(r,200,Map.of("status","success","favorite",action.equals("add")));
   }catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }}catch(IllegalArgumentException e){r.sendError(400);}
 }
}
