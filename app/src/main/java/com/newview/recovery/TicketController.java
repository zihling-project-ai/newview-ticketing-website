package com.newview.recovery;
import javax.servlet.http.*;import java.io.IOException;import java.security.SecureRandom;import java.util.*;
import org.hibernate.Session;import org.hibernate.Transaction;import com.newview.util.HibernateUtil;import com.google.gson.Gson;
import com.google.zxing.*;import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;import com.google.zxing.client.j2se.MatrixToImageWriter;
/** Owner-only local tickets. QR is opaque data, never a GET redemption URL. */
public class TicketController extends HttpServlet {
 public static final String PREFIX="NEWVIEW-LOCAL-TICKET:";
 private static class Invalid extends RuntimeException {final int status;Invalid(int s,String m){super(m);status=s;}}
 private void json(HttpServletResponse r,int code,Object value)throws IOException{r.setStatus(code);r.setContentType("application/json;charset=UTF-8");new Gson().toJson(value,r.getWriter());}
 private int user(HttpServletRequest q){HttpSession h=q.getSession(false);return h==null||h.getAttribute("userID")==null?-1:Integer.parseInt(h.getAttribute("userID").toString());}
 private int id(String text){try{int i=Integer.parseInt(text);if(i<1)throw new Exception();return i;}catch(Exception e){throw new Invalid(400,"票券編號不正確");}}
 private void lock(Session s,int user){s.createNativeQuery("SELECT userID FROM user WHERE userID=:user FOR UPDATE").setParameter("user",user).uniqueResult();}
 private Object[] ticket(Session s,int user,String condition,Object value){
  return (Object[])s.createNativeQuery("SELECT l.orderListID,l.orderID,l.actID,a.actName,a.actDate,l.actTotal,l.seatRowsColumns,l.usedType,o.ordType,EXISTS(SELECT 1 FROM local_simulation_order m WHERE m.orderID=o.orderID),l.localTicketToken,l.localRedeemedAt,a.cityAddress,a.time,p.pubName FROM orderlist l JOIN orders o ON o.orderID=l.orderID LEFT JOIN act a ON a.actID=l.actID LEFT JOIN Publisher p ON p.pubID=o.pubID WHERE o.userID=:user AND "+condition+" FOR UPDATE").setParameter("user",user).setParameter("value",value).uniqueResult();
 }
 private boolean active(Object[] row){return row[9] instanceof Number&&((Number)row[9]).intValue()==1&&row[8] instanceof Number&&((Number)row[8]).intValue()==3&&row[7] instanceof Number&&((Number)row[7]).intValue()==0;}
 private Map<String,Object> describe(Object[] row){String[] keys={"ticketID","orderID","actID","actName","actDate","amount","seat","usedType","orderState","simulation"};Map<String,Object> result=new LinkedHashMap<>();for(int i=0;i<keys.length;i++){Object v=row[i];result.put(keys[i],v instanceof java.util.Date?v.toString():v);}result.put("redeemedAt",row[11]==null?null:row[11].toString());result.put("canRedeem",active(row));result.put("venue",row[12]);result.put("time",row[13]==null?null:row[13].toString());result.put("publisher",row[14]);return result;}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {
  if(q.getServletPath().equals("/RedeemLocalTicket")||q.getServletPath().equals("/PreviewTicket")||q.getServletPath().equals("/DecodeTicketQR")){r.sendError(405);return;}
  int user=user(q);if(user<0){r.sendError(401);return;}
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   Transaction tx=s.beginTransaction();try{lock(s,user);
    if(q.getParameter("ticketID")==null&&q.getServletPath().equals("/Tickets")){
     List<?> rows=s.createNativeQuery("SELECT l.orderListID,l.orderID,l.actID,a.actName,a.actDate,l.actTotal,l.seatRowsColumns,l.usedType,o.ordType,EXISTS(SELECT 1 FROM local_simulation_order m WHERE m.orderID=o.orderID),l.localTicketToken,l.localRedeemedAt,a.cityAddress,a.time,p.pubName FROM orderlist l JOIN orders o ON o.orderID=l.orderID LEFT JOIN act a ON a.actID=l.actID LEFT JOIN Publisher p ON p.pubID=o.pubID WHERE o.userID=:user ORDER BY o.ordTime DESC,l.orderListID DESC").setParameter("user",user).list();List<Map<String,Object>> data=new ArrayList<>();for(Object row:rows)data.add(describe((Object[])row));tx.commit();json(r,200,data);return;
    }
    Object[] row=ticket(s,user,"l.orderListID=:value",id(q.getParameter("ticketID")));if(row==null)throw new Invalid(404,"找不到票券");
    if(!active(row)&&q.getServletPath().equals("/TicketQR"))throw new Invalid(409,"已使用、取消或非本機模擬票券，無法顯示有效模擬 QR");
    Map<String,Object> result=describe(row);String payload=null;
    if(active(row)){String token=(String)row[10];if(token==null){byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);s.createNativeQuery("UPDATE orderlist SET localTicketToken=:token WHERE orderListID=:id").setParameter("token",token).setParameter("id",row[0]).executeUpdate();}payload=PREFIX+token;result.put("payload",payload);}
    tx.commit();
    if(q.getServletPath().equals("/TicketQR")){r.setContentType("image/png");r.getOutputStream().write(LocalTicketQR.png(payload));}
    else json(r,200,result);
   }catch(Invalid e){if(tx.isActive())tx.rollback();json(r,e.status,Map.of("error",e.getMessage()));}catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
  q.setCharacterEncoding("UTF-8");boolean scan=q.getServletPath().equals("/DecodeTicketQR"),preview=q.getServletPath().equals("/PreviewTicket")||scan;if(!preview&&!q.getServletPath().equals("/RedeemLocalTicket")){r.sendError(405);return;}int user=user(q);if(user<0){r.sendError(401);return;}HttpSession h=q.getSession(false);
  if(h.getAttribute("csrf")==null||!Objects.equals(h.getAttribute("csrf"),q.getHeader("X-CSRF-Token"))){r.sendError(403);return;}
  if(!preview&&!"true".equals(q.getParameter("confirmSimulation"))){json(r,400,Map.of("error","請確認進行本機模擬核銷"));return;}
  String payload=q.getParameter("payload");
  if(scan){try{Part image=q.getPart("image");if(image==null||image.getSize()<1)throw new IllegalArgumentException();if(image.getSize()>ImageSupport.MAX_BYTES)throw new IllegalStateException();byte[] normalized;try(java.io.InputStream in=image.getInputStream()){normalized=ImageSupport.normalize(in.readNBytes(ImageSupport.MAX_BYTES+1));}
    java.awt.image.BufferedImage bitmap;javax.imageio.ImageReader reader=javax.imageio.ImageIO.getImageReadersByFormatName("png").next();try(javax.imageio.stream.ImageInputStream input=new javax.imageio.stream.MemoryCacheImageInputStream(new java.io.ByteArrayInputStream(normalized))){reader.setInput(input,true,true);bitmap=reader.read(0);}finally{reader.dispose();}payload=new MultiFormatReader().decode(new BinaryBitmap(new com.google.zxing.common.HybridBinarizer(new com.google.zxing.client.j2se.BufferedImageLuminanceSource(bitmap))),Map.of(DecodeHintType.TRY_HARDER,true)).getText();
   }catch(IllegalStateException e){json(r,413,Map.of("error","QR 圖片不得超過 2 MB"));return;}catch(Exception e){json(r,400,Map.of("error","無法辨識票券 QR，請提供清晰的 PNG 或 JPEG 圖片（最多 2 MB）"));return;}}
  if(payload==null||!payload.matches(PREFIX+"[A-Za-z0-9_-]{43}")){json(r,400,Map.of("error","請輸入完整的本機模擬票券碼"));return;}
  try(Session s=HibernateUtil.getSessionFactory().openSession()){
   Transaction tx=s.beginTransaction();try{lock(s,user);Object[] row=ticket(s,user,"l.localTicketToken=:value",payload.substring(PREFIX.length()));if(row==null)throw new Invalid(404,"找不到票券");if(preview){Map<String,Object> result=describe(row);if(scan&&active(row))result.put("payload",payload);tx.commit();json(r,200,result);return;}if(!active(row))throw new Invalid(409,"票券已使用、已取消或目前不可核銷");
    s.createNativeQuery("UPDATE orderlist SET usedType=1,localRedeemedAt=CURRENT_TIMESTAMP(6) WHERE orderListID=:id AND usedType=0").setParameter("id",row[0]).executeUpdate();
    Number unused=(Number)s.createNativeQuery("SELECT COUNT(*) FROM orderlist WHERE orderID=:order AND (usedType IS NULL OR usedType<>1)").setParameter("order",row[1]).uniqueResult();if(unused.intValue()==0)s.createNativeQuery("UPDATE orders SET ordType=1 WHERE orderID=:order AND userID=:user").setParameter("order",row[1]).setParameter("user",user).executeUpdate();
    Object time=s.createNativeQuery("SELECT localRedeemedAt FROM orderlist WHERE orderListID=:id").setParameter("id",row[0]).uniqueResult();tx.commit();json(r,200,Map.of("status","redeemed","simulation",true,"ticketID",row[0],"redeemedAt",time.toString()));
   }catch(Invalid e){if(tx.isActive())tx.rollback();json(r,e.status,Map.of("error",e.getMessage()));}catch(Exception e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
}
