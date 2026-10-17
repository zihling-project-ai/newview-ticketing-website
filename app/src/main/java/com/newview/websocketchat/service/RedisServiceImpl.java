package com.newview.websocketchat.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import com.google.gson.Gson;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.websocketchat.model.SeatInfo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisServiceImpl implements RedisService{
 
    private final JedisPool jedisPool;
  

    public RedisServiceImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
    
	@Override
	public Set<SeatInfo> fetchSeatInfoFromRedis(Session userSession) {
	    Set<SeatInfo> allSeatInfo = new HashSet<>();
	    try (Jedis jedis = JedisPoolUtil.getJedisPool().getResource()) {
	        Set<String> keys = jedis.keys("seatData:*");
	        String actID = (String) userSession.getUserProperties().get("actID");

	        for (String key : keys) {
	            Map<String, String> data = jedis.hgetAll(key);

	            for (Map.Entry<String, String> entry : data.entrySet()) {
	                String keyName = entry.getKey();
	                String value = entry.getValue();

	                // 解析座位信息的 actID
	                String[] parts = value.split(",");
	                String seatActID = parts[1];

	                if (seatActID.equals(actID) && !parts[0].isEmpty()) {
	                    SeatInfo seatInfo = new SeatInfo(parts[0], keyName, parts[1] + "," + parts[2], parts[1]);
	                    allSeatInfo.add(seatInfo);
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return allSeatInfo;
	}

	public String performRedisCancelOperations(String actName, String userName, String seatNumber) {
	    Jedis jedis = null;
	    String updatedSeatInfoInRedis = null;
	    try {
	        jedis = JedisPoolUtil.getJedisPool().getResource();
	        // Redis 中得到座位信息
	        String existingSeatInfo = jedis.hget("seatData:" + actName, seatNumber);
	        if (existingSeatInfo != null && existingSeatInfo.endsWith(userName + "," + actName + "," + "buy")) {
	            // 更新消息中的 "buy" 為 "cancel"
	            updatedSeatInfoInRedis = existingSeatInfo.replace("buy", "cancel");
	            jedis.hset("seatData:" + actName, seatNumber, updatedSeatInfoInRedis);
	            // Redis 刪除
	            jedis.hdel("seatData:" + actName, seatNumber);
	        }
	    } finally {
	        if (jedis != null) {
	            jedis.close();
	        }
	    }
	    return updatedSeatInfoInRedis;
	}

	// 是否已售出
		public boolean isSeatSoldOut(String actID, String seatNumber) {
			Jedis jedis = null;
			try {
				jedis = JedisPoolUtil.getJedisPool().getResource();
				String existingSeatInfo = jedis.hget("seatData:" + actID, seatNumber);
				return (existingSeatInfo != null && existingSeatInfo.endsWith(",soldOut"));
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
		// Redis儲存
		public void storeSeatInfoInRedis(String actID, SeatInfo seatInfo) {
			Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
			try {
				if (!jedis.exists("seatData:" + actID)) {
					jedis.hset("seatData:" + actID, seatInfo.getSeatNumber(),
							seatInfo.getUserName() + "," + seatInfo.getSeatType());
				} else {
					jedis.hset("seatData:" + actID, seatInfo.getSeatNumber(),
							seatInfo.getUserName() + "," + seatInfo.getSeatType());
				}
			} finally {
				jedis.close();
			}
		}
		
		

		
		public Map<String, String> markSeatsInRedis(String actID, String targetUserName) {
		    Jedis jedis = null;		   
		    Map<String, String> modifiedSeatsData = new HashMap<>(); 

		    try {
		        jedis = JedisPoolUtil.getJedisPool().getResource();
		        

		        jedis.select(0); 

		        Map<String, String> seatsData = jedis.hgetAll("seatData:" + actID);

		        for (Map.Entry<String, String> entry : seatsData.entrySet()) {
		            String seatNumber = entry.getKey();
		            String seatInfo = entry.getValue();
//		            System.out.println(seatNumber + "   " + seatInfo);
		            String[] seatInfoParts = seatInfo.split(",");
		            String userName = seatInfoParts[0];
		            String seatType = seatInfoParts[2];
		            System.out.println(seatType);
		            if (userName.equals(targetUserName) && !seatType.equals("soldOut")&& !seatType.equals("soldReally")) {
		                String newSeatInfo = userName + "," + actID + ",soldOut";
		                jedis.hset("seatData:" + actID, seatNumber, newSeatInfo);
		                modifiedSeatsData.put(seatNumber, newSeatInfo);
		            }
		        }

		        jedis.select(3); // 切換到 db3，

		        // 複製到db3
		        if (!modifiedSeatsData.isEmpty()) {
		            for (Map.Entry<String, String> entry : modifiedSeatsData.entrySet()) {
		                String seatNumber = entry.getKey();
		                String newSeatInfo = entry.getValue();
		                jedis.hset("seatData:" + actID, seatNumber, newSeatInfo); 
		                // 放到 db3
		            }
		        }
		    } finally {
		        if (jedis != null) {
		            jedis.select(0); //最後關閉前 切換回原本db0
		            jedis.close();
		        }
		    }

		    return modifiedSeatsData;
		}

		
		
		public void deleteSeatDataFromRedis(String actID, String seatNumber, String userName) {
		    Jedis jedis = null;
		    try {
		        jedis = JedisPoolUtil.getJedisPool().getResource();
		        jedis.hdel("seatData:" + actID, seatNumber);
		        jedis.del("seatData:" + actID + ":userName:" + userName);
		    } finally {
		        if (jedis != null) {
		            jedis.close();
		        }
		    }
		}
		public Map<String, String> getSeatDataFromRedis(String actID) {
		    Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
		    try {
		        Map<String, String> seatData = jedis.hgetAll("seatData:" + actID);
		        return seatData;
		    } finally {
		        if (jedis != null) {
		            jedis.close();
		        }
		    }
		}
		//購物車使用方法, 改為db1複製資料,db4  也會獲得一份
		public Map<String, String> markSeatsInRedisAndDB1(String actID, String targetUserName) {
		    Jedis jedis = null;
		    Map<String, String> modifiedSeatsData = new HashMap<>();

		    int seatExpirationTime = 60; // 設定座位的過期時間

		    try {
		        jedis = JedisPoolUtil.getJedisPool().getResource();

		        // 從主數據庫（db0）獲取座位數據
		        Map<String, String> seatsData = jedis.hgetAll("seatData:" + actID);
		        String newSeatInfo = null ;
		        for (Map.Entry<String, String> entry : seatsData.entrySet()) {
		            String seatNumber = entry.getKey();
		            String seatInfo = entry.getValue();
		            
		            String[] seatInfoParts = seatInfo.split(",");
		            String userName = seatInfoParts[0];
		            String actName = seatInfoParts[1];
		            String seatType = seatInfoParts[2];

		            if (userName.equals(targetUserName) && seatType.equals("buy") ) {
		                // 將 "buy" 標記的座位狀態改為 "inCart

		                String newSeatInfoCart = seatNumber + "," + actName + ",inCart";
		                newSeatInfo = userName + "," + actName + ",inCart";
		                jedis.hset("seatData:" + actID, seatNumber, newSeatInfo);
		                // 切換到 db1
		                jedis.select(1);

		                // 為每個座位設定一個單獨的 key，並設定 TTL
		                String seatKey = "cart:" + targetUserName + ":" + actID + ":" + seatNumber;
		                jedis.set(seatKey, newSeatInfoCart);
		                jedis.expire(seatKey, seatExpirationTime);		                
		                jedis.select(0);		                
		                modifiedSeatsData.put(seatNumber, newSeatInfoCart);
		            }
		        }

		        //複製到 db4
		        if (!modifiedSeatsData.isEmpty()) {
		            jedis.select(4); // 切换到 db4
		            
		            for (Map.Entry<String, String> entry : modifiedSeatsData.entrySet()) {
		                String seatNumber = entry.getKey();
		                
		                
		                jedis.hset("seatData:" + actID, seatNumber, newSeatInfo); // 存到 db4
		            }
		            
		        }
		    } finally {
		        if (jedis != null) {
		        	jedis.select(0);
		            jedis.close();
		        }
		    }

		    return modifiedSeatsData;
		}
		//確認購買,複製到db3
		public Map<String, String> findSeatsByActIDAndUserName(String actID, String userName) {
		    Jedis jedis = null;
		    int seatExpirationTime = 60;
		    Map<String, String> result = new HashMap<>();
		    try {
		        jedis = JedisPoolUtil.getJedisPool().getResource();
		        jedis.select(3); 
		        
		        Map<String, String> allSeats = jedis.hgetAll("seatData:" + actID);
		        for (Map.Entry<String, String> entry : allSeats.entrySet()) {
		            if (entry.getValue().startsWith(userName + "," + actID+",soldOut")) {
		                result.put(entry.getKey(), entry.getValue());
		                               
		                String[] seatInfoParts = entry.getValue().split(",");			            
			            String actName = seatInfoParts[1];
			            String seatType = seatInfoParts[2];
			            jedis.select(5);
			            String seatKey = "cart:" + userName + ":" + actID + ":" + entry.getKey()+":NotReallyBuy";
			            String newSeatInfoCart = entry.getKey() + "," + actName + ",inCart";
		                jedis.set(seatKey, newSeatInfoCart);
		                jedis.expire(seatKey, seatExpirationTime);	
			            
		            }else {
		            	System.out.println("沒找到");
		            }
		        }
		        
		        
		    } finally {
		        if (jedis != null) {
		            jedis.close();
		        }
		    }
		    return result;
		}

		
		
		public String getCartDataFromRedis(String cartKey) {
		    Jedis jedis = null;
		    Map<String, String> cartData = new HashMap<>();
		    Gson gson = new Gson();

		    try {
		        jedis = JedisPoolUtil.getJedisPool().getResource();
		        jedis.select(5);
		        String[] parts = cartKey.split(":");
		        String actID = parts[2];
		        
		        String seatInfo = jedis.get(cartKey);
		        
		        // 如果cartKey不存在，返回null
		        if (seatInfo == null) {
		            return null;
		        }
		        
		        String[] keyParts = cartKey.split(":");
		        String seatNumber =parts[3];
		        cartData.put(seatNumber, seatInfo);
		        System.out.println(seatNumber+"  號碼");
		        // 刪除找到的資料
		        jedis.del(cartKey);
		        jedis.select(3);
		        jedis.hdel("seatData:" + actID, seatNumber);
		        
		    } finally {
		        if (jedis != null) {
		            jedis.close();
		        }
		    }

		    return gson.toJson(cartData);
		}

		
		

		public String findSeatKeyByActIDAndUserName(String actID, String userName) {
		    Jedis jedis = null;
		    try {
		        jedis = JedisPoolUtil.getJedisPool().getResource();
		        jedis.select(5); 

		        Map<String, String> allSeats = jedis.hgetAll("seatData:" + actID);
		        for (Map.Entry<String, String> entry : allSeats.entrySet()) {
		            if (entry.getValue().startsWith(userName + "," + actID+",soldOut")) {
		                String seatKey = "cart:" + userName + ":" + actID + ":" + entry.getKey() + ":NotReallyBuy";
		                return seatKey;
		            }
		        }
		    } finally {
		        if (jedis != null) {
		            jedis.close();
		        }
		    }
		    return null; // 如果未找到匹配的seatKey
		}
		
		 public  Map<String, String> findSeatsNumberByActIDAndUserName(String actID, String userName) {
		        Jedis jedis = null;
		        Map<String, String> result = new HashMap<>();
		        try {
		            jedis = JedisPoolUtil.getJedisPool().getResource();
		            jedis.select(3); 

		            Map<String, String> allSeats = jedis.hgetAll("seatData:" + actID);
		            for (Map.Entry<String, String> entry : allSeats.entrySet()) {
		                if (entry.getValue().startsWith(userName + "," + actID+",soldOut")) {
		                    result.put(entry.getKey(), entry.getValue());
		                }
		            }
		        } finally {
		            if (jedis != null) {
		                jedis.close();
		            }
		        }
		        return result;
		    }
		 public void updateSoldOutToSoldOutReally(String actID, String targetUserID) {
			    Jedis jedis = null;
			    try {
			        jedis = JedisPoolUtil.getJedisPool().getResource();
			        jedis.select(0); // 切换到 db0

			        Map<String, String> seatsData = jedis.hgetAll("seatData:" + actID);

			        for (Map.Entry<String, String> entry : seatsData.entrySet()) {
			            String seatNumber = entry.getKey();
			            String seatInfo = entry.getValue();

			            String[] seatInfoParts = seatInfo.split(",");
			            String userName = seatInfoParts[0];
			            String seatType = seatInfoParts[2];
			            System.out.println(userName+" 和 "+ seatType);
			            if (seatType.equals("soldOut") && userName.equals(targetUserID)) {
			                // 如果狀態 "soldOut" 且用户ID匹配，改成 "soldOutReally"
			            	System.out.println("更改為really");
			                String newSeatInfo = userName + "," + actID + ",soldReally";
			                jedis.hset("seatData:" + actID, seatNumber, newSeatInfo);
			            }
			        }
			    } finally {
			        if (jedis != null) {
			            jedis.close();
			        }
			    }
			}

}