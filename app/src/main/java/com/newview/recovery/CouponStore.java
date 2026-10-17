package com.newview.recovery;
import java.util.*;import org.hibernate.Session;
/** Assigned fixed-amount coupons; callers lock the owning user before checkout/cancel. */
final class CouponStore {
 static class Invalid extends RuntimeException {final int status;Invalid(int s,String m){super(m);status=s;}}
 static int selection(String text){if(text==null||text.isBlank()||text.equals("0"))return 0;try{int id=Integer.parseInt(text);if(id<1)throw new Exception();return id;}catch(Exception e){throw new Invalid(400,"優惠券編號不正確");}}
 static List<Map<String,Object>> list(Session s,int user){
  List<?> rows=s.createNativeQuery("SELECT u.useDisID,d.discountNO,d.discountContent,d.disAmount,d.disStartDate,d.disFinishDate,d.pubID,p.pubName,u.ditUsed,(d.disStartDate<=CURRENT_TIMESTAMP(6) AND d.disFinishDate>CURRENT_TIMESTAMP(6)),(SELECT COUNT(*) FROM useDiscount x WHERE x.userID=u.userID AND x.discountNO=u.discountNO),EXISTS(SELECT 1 FROM local_coupon_redemption r WHERE r.useDisID=u.useDisID) FROM useDiscount u JOIN discount d ON d.discountNO=u.discountNO LEFT JOIN Publisher p ON p.pubID=d.pubID WHERE u.userID=:user ORDER BY u.useDisID").setParameter("user",user).list();
  List<Map<String,Object>> result=new ArrayList<>();for(Object o:rows){Object[] a=(Object[])o;Map<String,Object> m=new LinkedHashMap<>();String[] keys={"useDisID","discountNO","content","amount","startsAt","endsAt","pubID","publisher"};for(int i=0;i<8;i++)m.put(keys[i],a[i] instanceof java.util.Date?a[i].toString():a[i]);boolean unused=a[8] instanceof Number&&((Number)a[8]).intValue()==0;boolean valid=a[9] instanceof Number&&((Number)a[9]).intValue()==1;boolean unique=((Number)a[10]).intValue()==1;boolean active=((Number)a[11]).intValue()==1;boolean amount=a[3] instanceof Number&&((Number)a[3]).intValue()>0;
   m.put("available",unused&&valid&&unique&&!active&&amount);m.put("status",!unique?"資料重複，暫不可用":!unused||active?"已使用":!valid?"未開始、已過期或期限不完整":!amount?"折扣資料無效":"可使用");result.add(m);
  }return result;
 }
 static Map<String,Object> quote(Session s,int user,int selection,List<Map<String,Object>> items){
  if(selection==0)return Map.of("useDisID",0,"amount",0,"targetPubID",0,"signature","");
  Map<String,Object> coupon=null;for(Map<String,Object> c:list(s,user))if(((Number)c.get("useDisID")).intValue()==selection)coupon=c;
  if(coupon==null)throw new Invalid(404,"找不到本人的優惠券");if(!Boolean.TRUE.equals(coupon.get("available")))throw new Invalid(409,"優惠券目前不可使用");
  Map<Integer,Long> totals=new LinkedHashMap<>();for(Map<String,Object> item:items){Object pub=item.get("pubID");int p=pub==null?0:((Number)pub).intValue();long price=item.get("price") instanceof Number?((Number)item.get("price")).longValue():0;totals.merge(p,price*((Number)item.get("quantity")).intValue(),Long::sum);}
  Object pub=coupon.get("pubID");int target;if(pub==null){if(totals.size()!=1)throw new Invalid(409,"全站券限單一廠商結帳，請先調整購物車");target=totals.keySet().iterator().next();}else target=((Number)pub).intValue();
  if(!totals.containsKey(target)||totals.get(target)<=0)throw new Invalid(409,"購物車沒有此優惠券適用的付費活動");
  int amount=(int)Math.min(((Number)coupon.get("amount")).longValue(),totals.get(target));Map<String,Object> result=new LinkedHashMap<>();result.put("useDisID",selection);result.put("discountNO",coupon.get("discountNO"));result.put("amount",amount);result.put("targetPubID",target);result.put("signature",new com.google.gson.Gson().toJson(coupon));return result;
 }
 static void consume(Session s,int user,int order,Map<String,Object> quote){int selection=((Number)quote.get("useDisID")).intValue();if(selection==0)return;
  int updated=s.createNativeQuery("UPDATE useDiscount SET ditUsed=1 WHERE useDisID=:coupon AND userID=:user AND ditUsed=0").setParameter("coupon",selection).setParameter("user",user).executeUpdate();if(updated!=1)throw new Invalid(409,"優惠券已使用，請重新確認");
  s.createNativeQuery("INSERT INTO local_coupon_redemption(orderID,useDisID) VALUES(:order,:coupon)").setParameter("order",order).setParameter("coupon",selection).executeUpdate();
 }
 static boolean restore(Session s,int user,int order){Object id=s.createNativeQuery("SELECT r.useDisID FROM local_coupon_redemption r JOIN useDiscount u ON u.useDisID=r.useDisID WHERE r.orderID=:order AND u.userID=:user FOR UPDATE").setParameter("order",order).setParameter("user",user).uniqueResult();if(id==null)return false;
  s.createNativeQuery("UPDATE useDiscount SET ditUsed=0 WHERE useDisID=:coupon AND userID=:user").setParameter("coupon",id).setParameter("user",user).executeUpdate();s.createNativeQuery("DELETE FROM local_coupon_redemption WHERE orderID=:order AND useDisID=:coupon").setParameter("order",order).setParameter("coupon",id).executeUpdate();return true;
 }
}
