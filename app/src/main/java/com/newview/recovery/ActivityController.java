package com.newview.recovery;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.hibernate.Session;
import com.google.gson.Gson;
import com.newview.util.HibernateUtil;

/** Read-only catalog for the loopback recovery site; does not perform checkout. */
public class ActivityController extends HttpServlet {
 private void json(HttpServletResponse r,int code,Object body)throws IOException{r.setStatus(code);r.setContentType("application/json;charset=UTF-8");new Gson().toJson(body,r.getWriter());}
 private int positive(String input){int id=Integer.parseInt(input);if(id<=0)throw new IllegalArgumentException();return id;}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   String path=q.getServletPath();
   if(path.equals("/ActivityImage")){
    int act=positive(q.getParameter("actID"));String pic=q.getParameter("picID");
    org.hibernate.query.NativeQuery<?> query=s.createNativeQuery("SELECT actPic FROM actpic WHERE actID=:act"+(pic==null?"":" AND actPicID=:pic")+" ORDER BY actPicID LIMIT 1").setParameter("act",act);
    if(pic!=null)query.setParameter("pic",positive(pic));byte[] bytes=(byte[])query.uniqueResult();if(bytes==null){r.sendError(404);return;}
    try{bytes=ImageSupport.normalize(bytes);}catch(IllegalArgumentException e){r.sendError(404);return;}r.setContentType("image/png");r.getOutputStream().write(bytes);return;
   }
   boolean detail=path.equals("/ActivityDetail");Integer act=detail?positive(q.getParameter("actID")):null;
   HttpSession h=q.getSession(false);int user=h==null||h.getAttribute("userID")==null?-1:Integer.parseInt(h.getAttribute("userID").toString());
   String sql="SELECT a.actID,a.actName,a.actIntroduce,a.actDate,a.actPrice,a.cityAddress,a.actCategoryID,c.actCategoryName,a.cityAddressID,city.cityName,p.pubName,a.time,a.actContent,EXISTS(SELECT 1 FROM actpic pic WHERE pic.actID=a.actID),EXISTS(SELECT 1 FROM myLike m WHERE m.actID=a.actID AND m.userID=:user),EXISTS(SELECT 1 FROM local_seat_layout sl WHERE sl.actID=a.actID),CASE WHEN a.actDate<CURRENT_DATE THEN 1 ELSE 0 END FROM act a LEFT JOIN actCategory c ON c.actCategoryID=a.actCategoryID LEFT JOIN city ON city.cityAddressID=a.cityAddressID LEFT JOIN Publisher p ON p.pubID=a.pubID"+(detail?" WHERE a.actID=:act":"")+" ORDER BY a.actDate,a.actID";
   org.hibernate.query.NativeQuery<?> query=s.createNativeQuery(sql).setParameter("user",user);if(detail)query.setParameter("act",act);
   List<Map<String,Object>> result=new ArrayList<>();String[] keys={"actID","actName","actIntroduce","actDate","actPrice","cityAddress","categoryID","categoryName","cityID","cityName","publisher","time","actContent","hasImage","favorite","seatMode","ended"};
   for(Object value:query.list()){Object[] row=(Object[])value;Map<String,Object> item=new LinkedHashMap<>();for(int i=0;i<keys.length;i++){Object v=row[i];item.put(keys[i],v instanceof java.util.Date?v.toString():v);}if(!detail)item.remove("actContent");result.add(item);}
   if(detail){if(result.isEmpty()){r.sendError(404);return;}Map<String,Object> item=result.get(0);item.put("imageIDs",s.createNativeQuery("SELECT actPicID FROM actpic WHERE actID=:act ORDER BY actPicID").setParameter("act",act).list());json(r,200,item);}else json(r,200,result);
  }catch(IllegalArgumentException e){r.sendError(400);}
 }
}
