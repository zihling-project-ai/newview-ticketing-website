package com.newview.websocketchat.service;

import com.newview.user.jedis.JedisPoolUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisListener {
	 public static void start() {
	        new Thread(() -> subscribeToDb(1)).start();
	        new Thread(() -> subscribeToDb(5)).start();
	    }
	 //依需要來監聽不同DB
	 private static void subscribeToDb(int dbIndex) { 
	        Jedis jedis = null;
	        try {
	            jedis = JedisPoolUtil.getJedisPool().getResource();
	            KeyExpiredListener listener = new KeyExpiredListener(dbIndex);
	            jedis.psubscribe(listener, "__keyevent@" + dbIndex + "__:expired");
	        } finally {
	            if (jedis != null) {
	                jedis.close();
	            }
	        }
	    }
	}

	class KeyExpiredListener extends JedisPubSub {
		 private final int dbIndex;

		    public KeyExpiredListener(int dbIndex) {
		        this.dbIndex = dbIndex;
		    }

	    @Override
	    public void onPSubscribe(String pattern, int subscribedChannels) {
	        System.out.println("Waiting for key expiration events...");
	    }

	    @Override
	    public void onPMessage(String pattern, String channel, String message) {
	       

	        String[] parts = message.split(":");

	        if (parts.length == 5) {
	            String cartName = parts[0];
	            String userName = parts[1];
	            String actID = parts[2];
	            String seatNumber = parts[3];
	            String seatTypeStr = parts[4];
	            String value = userName + "," + actID + ",inCart";
	            
	           
	            deleteSeatData(actID, seatNumber);
	        }else if (parts.length == 4) {
	        	
		            String cartName = parts[0];
		            String userName = parts[1];
		            String actID = parts[2];
		            String seatNumber = parts[3];
		           
		            String value = userName + "," + actID + ",inCart";
		            
		           
		            deleteSeatData(actID, seatNumber);
	        } else {
	            System.out.println("Invalid message format: " + message);
	        }	       
	    }

	    // 删除方法, 依照獲得的DB 位置來刪除該位置資料
	    private void deleteSeatData(String actID, String seatNumber) {
	        Jedis jedis = null;
	        try {
	            jedis = JedisPoolUtil.getJedisPool().getResource();

	            //只刪除跟db1有關的資料
	            if (dbIndex == 1) {
	                jedis.select(4);
	                jedis.hdel("seatData:" + actID, seatNumber);
	                jedis.select(0);
	                jedis.hdel("seatData:" + actID, seatNumber);
	            }
	            
	            //只刪除跟db5有關的資料
	            if (dbIndex == 5) {
	                jedis.select(3);
	                jedis.hdel("seatData:" + actID, seatNumber);
	                jedis.select(0);
	                jedis.hdel("seatData:" + actID, seatNumber);
	            }

	        } finally {
	            if (jedis != null) {
	                jedis.close();
	            }
	        }
	    }
	}