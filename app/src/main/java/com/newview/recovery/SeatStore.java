package com.newview.recovery;
import java.util.*;
import org.hibernate.Session;
import redis.clients.jedis.*;
/** Dedicated ephemeral Redis holds; MySQL bookings remain authoritative after checkout. */
public final class SeatStore {
 public static final int TTL=600;
 private static JedisPool pool(){JedisPoolConfig config=new JedisPoolConfig();config.setTestOnBorrow(true);config.setMaxWaitMillis(2000);return new JedisPool(config,System.getenv().getOrDefault("NEWVIEW_REDIS_HOST","127.0.0.1"),Integer.parseInt(System.getenv().getOrDefault("NEWVIEW_REDIS_PORT","6383")),2000);}
 private static final JedisPool POOL=pool();
 public static void close(){POOL.close();}
 public static String key(int act,String seat){return "nv:recovery:seat:"+act+":"+seat;}
 public static List<String> layout(Session s,int act){return s.createNativeQuery("SELECT seatCode FROM local_seat_layout WHERE actID=:act ORDER BY seatCode").setParameter("act",act).list();}
 public static List<Object[]> selected(Session s,int user,int act){return s.createNativeQuery("SELECT seatCode,holdToken FROM local_cart_seat WHERE userID=:user AND actID=:act ORDER BY seatCode").setParameter("user",user).setParameter("act",act).list();}
 public static Map<String,Object> cart(Session s,int user,int act){
  boolean mode=!layout(s,act).isEmpty();List<Object[]> rows=selected(s,user,act);List<String> seats=new ArrayList<>();boolean valid=!rows.isEmpty();long ttl=TTL*1000L;
  if(mode)try(Jedis j=POOL.getResource()){for(Object[] row:rows){String seat=row[0].toString(),key=key(act,seat);seats.add(seat);if(!(user+"|"+row[1]).equals(j.get(key)))valid=false;ttl=Math.min(ttl,Math.max(0,j.pttl(key)));} }
  return Map.of("seatMode",mode,"seats",seats,"seatsValid",!mode||valid,"holdExpiresAt",mode&&valid?System.currentTimeMillis()+ttl:0L);
 }
 public static List<Map<String,Object>> map(Session s,int user,int act){
  List<String> seats=layout(s,act);Set<String> booked=new HashSet<>(s.createNativeQuery("SELECT seatCode FROM local_seat_booking WHERE actID=:act").setParameter("act",act).list());List<Map<String,Object>> result=new ArrayList<>();
  try(Jedis j=POOL.getResource()){for(String seat:seats){String key=key(act,seat),owner=j.get(key);String state=booked.contains(seat)?"booked":owner==null?"available":owner.startsWith(user+"|")?"mine":"held";result.add(Map.of("seat",seat,"state",state,"remainingMs",owner==null?0:Math.max(0,j.pttl(key))));}}return result;
 }
 public static void release(int user,int act,List<Object[]> rows){try(Jedis j=POOL.getResource()){for(Object[] row:rows)j.eval("if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end",List.of(key(act,row[0].toString())),List.of(user+"|"+row[1]));}}
 public static final class Claim {
  final int user,act;final String value;final List<String> seats;final List<?> previous;final long started;
  Claim(int u,int a,String v,List<String> seats,List<?> old,long time){user=u;act=a;value=v;this.seats=seats;previous=old;started=time;}
  public void rollback(){try(Jedis j=POOL.getResource()){for(int i=0;i<seats.size();i++){String old=previous.get(1+i*2).toString();long ttl=((Number)previous.get(2+i*2)).longValue()-(System.currentTimeMillis()-started);j.eval("if redis.call('get',KEYS[1])==ARGV[1] then if ARGV[2]~='' and tonumber(ARGV[3])>0 then redis.call('psetex',KEYS[1],ARGV[3],ARGV[2]) else redis.call('del',KEYS[1]) end end return 1",List.of(key(act,seats.get(i))),List.of(value,old,Long.toString(ttl)));}}}
 }
 public static Claim claim(int user,int act,List<String> seats,String token){
  List<String> keys=new ArrayList<>();for(String seat:seats)keys.add(key(act,seat));String owner=user+"|";long time=System.currentTimeMillis();
  String script="for _,k in ipairs(KEYS) do local v=redis.call('get',k); if v and string.sub(v,1,string.len(ARGV[1]))~=ARGV[1] then return {0} end end local result={1}; for _,k in ipairs(KEYS) do table.insert(result,redis.call('get',k) or ''); table.insert(result,redis.call('pttl',k)); redis.call('set',k,ARGV[2],'EX',ARGV[3]); end return result";
  try(Jedis j=POOL.getResource()){List<?> result=(List<?>)j.eval(script,keys,List.of(owner,owner+token,Integer.toString(TTL)));if(((Number)result.get(0)).intValue()!=1)return null;return new Claim(user,act,owner+token,seats,result,time);}
 }
}
