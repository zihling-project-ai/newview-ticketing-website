package com.newview.websocketchat.service;

import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import com.newview.websocketchat.model.SeatInfo;

public interface RedisService {
	Set<SeatInfo> fetchSeatInfoFromRedis(Session userSession);
	String performRedisCancelOperations(String actName, String userName, String seatNumber);
	boolean isSeatSoldOut(String actID, String seatNumber);
	void storeSeatInfoInRedis(String actID, SeatInfo seatInfo);
	void deleteSeatDataFromRedis(String actID, String seatNumber, String userName);
	Map<String, String> markSeatsInRedis(String actID, String targetUserName);
	Map<String, String> getSeatDataFromRedis(String actID);	
	Map<String, String> markSeatsInRedisAndDB1(String actID, String targetUserName);
	Map<String, String> findSeatsByActIDAndUserName(String actID, String userName);
	String getCartDataFromRedis(String cartKey);
    Map<String, String> findSeatsNumberByActIDAndUserName(String actID, String userName);
	String findSeatKeyByActIDAndUserName(String actID, String userName);
	void updateSoldOutToSoldOutReally(String actID, String targetUserID);
}
