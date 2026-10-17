package com.newview.websocketchat.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.websocketchat.model.Message;
import com.newview.websocketchat.model.PurchasedSeatInfo;
import com.newview.websocketchat.model.SeatInfo;
import com.newview.websocketchat.service.RedisListener;
import com.newview.websocketchat.service.RedisService;
import com.newview.websocketchat.service.RedisServiceImpl;

import redis.clients.jedis.JedisPool;

@ServerEndpoint("/WebSocketChatWeb/{userName}/{actID}")
public class SeatMiddle {
	private static final Set<Session> connectedSessions = Collections.synchronizedSet(new HashSet<>());
	private static final Set<SeatInfo> seatInfoList = Collections.synchronizedSet(new HashSet<>());
	private static Map<String, String> userIdToUserNameMap = new ConcurrentHashMap<>(); // 測試階段
	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	private static final Map<String, Session> actIDToSessionMap = new ConcurrentHashMap<>();
	private static Map<String, List<SeatInfo>> seatInfoMap = new ConcurrentHashMap<>();
	private static final Set<PurchasedSeatInfo> purchasedSeatInfoSet = Collections.synchronizedSet(new HashSet<>());

	// 方法區
	 private static RedisService redisService;

	    public SeatMiddle() {
	        // 初始化Jedis連線池
	        JedisPool jedisPool = JedisPoolUtil.getJedisPool();
	        this.redisService = new RedisServiceImpl(jedisPool);
	    }
	    //打印訊息  檢查用
	    private void printPurchasedSeatInfoSet() {
	        System.out.println("Printing purchasedSeatInfoSet:");
	        for (PurchasedSeatInfo seatInfoTo : purchasedSeatInfoSet) {
	            System.out.println(seatInfoTo); 
	        }
	    }

	// 該方法為用戶加入連線, onOpen 會先執行讓用戶收到 redis 最新消息
	private void sendRedisDataToClients(Session userSession) {
	    Set<SeatInfo> allSeatInfo = redisService.fetchSeatInfoFromRedis(userSession);

	    Gson gson = new Gson();
	    // 所有座位信息打包成 JSON
	    String jsonString = gson.toJson(allSeatInfo);
	    // JSON 消息送到 WebSocket
	    userSession.getAsyncRemote().sendText(jsonString);
	}
	
	// 該方法: 取消座位會將redis 裡面對應的userid actid
	// 來刪除座位 並向同actID 用戶發送取消的消息
		private void handleCancelOperation(Message message, Session userSession) {
		    String userName = message.getUserName();
		    String seatNumber = message.getSeatNumber();
		    String actID = message.getActID();
		    String seatType = message.getType();
		    
			if (redisService.isSeatSoldOut(actID, seatNumber)) {
				return; // 座位已售出則中斷
			}
		   System.out.println("執行刪除中"+actID+",  "+seatNumber +",  "+seatType);
		    
			SeatInfo seatInfo = new SeatInfo(userName, seatNumber, actID + "," + seatType, actID);
			String updatedSeatInfoInRedis = redisService.performRedisCancelOperations(actID, userName, seatNumber);
			PurchasedSeatInfo seatInfoToRemove = findPurchasedSeatInfo(userName, actID, seatNumber,"buy");
			
			if (seatInfoToRemove != null) {
		        purchasedSeatInfoSet.remove(seatInfoToRemove);
		    }
//			printPurchasedSeatInfoSet();
			storeSeatInfoInMap(actID, seatInfo, userSession,false);
			String updatedSeatInfoMessage = buildUpdatedSeatInfoMessage(actID);
			
			sendMessageToActID(actID, updatedSeatInfoMessage);
			storeSeatInfoInMap(actID, seatInfo, userSession,true);
		}
		private PurchasedSeatInfo findPurchasedSeatInfo(String userName, String actID, String seatNumber, String seatType) {
		    for (PurchasedSeatInfo seatInfo : purchasedSeatInfoSet) {
		        if (seatInfo.getUserName().equals(userName)
		                && seatInfo.getActID().equals(actID)
		                && seatInfo.getSeatNumber().equals(seatNumber)
		                && seatInfo.getSeatType().equals(seatType)) {
		            return seatInfo;
		        }
		    }
		    return null; // 如果未找到匹配的对象，则返回null
		}
		//斷線刪除使用  
		private PurchasedSeatInfo findPurchasedSeatInfo(String userName, String actID) {
		    for (PurchasedSeatInfo seatInfo : purchasedSeatInfoSet) {
		        if (seatInfo.getUserName().equals(userName) && seatInfo.getActID().equals(actID)) {
		            return seatInfo;
		        }
		    }
		    return null; // 如果未找到匹配的对象，则返回null
		}

	//  seatInfos 中删除座位信息
	private void deleteSeatInfoFromList(String actID, String seatNumber) {
	    List<SeatInfo> seatInfos = seatInfoMap.get(actID);
	    if (seatInfos != null) {
	        Iterator<SeatInfo> iterator = seatInfos.iterator();
	        while (iterator.hasNext()) {
	            SeatInfo seatInfo = iterator.next();
	            if (seatInfo.getSeatNumber().equals(seatNumber)) {
	                iterator.remove(); // 删除座位信息
	                break;
	            }
	        }
	    }
	}

	// 該方法為更新SeatInfo 用
			public void updateSeatInfoInList(String actID, String seatNumber, String updatedSeatInfo) {
				List<SeatInfo> seatInfos = seatInfoMap.get(actID);
				if (seatInfos != null) {
					for (SeatInfo seatInfo : seatInfos) {
						if (seatInfo.getSeatNumber().equals(seatNumber)) {
							// 更新座位信息中的 "buy" 为 "cancel"
							seatInfo.setSeatType(actID + ",cancel");
							break; // 找到後停止
						}
					}
				}
			}
			public void cancelSeatPurchase(String actID, String userName) {
			    List<PurchasedSeatInfo> seatsToRemove = new ArrayList<>();
			    List<SeatInfo> seatInfoToSend = new ArrayList<>(); // 用收集要發送的座位信息
			    
			    // 删除purchasedSeatInfoSet中符合的數據
			    for (PurchasedSeatInfo seatInfo : purchasedSeatInfoSet) {
			        if (seatInfo.getActID().equals(actID) && seatInfo.getUserName().equals(userName)) {
			            seatsToRemove.add(seatInfo);
			            
			            
			            String seatNumber = seatInfo.getSeatNumber();
			            String seatType = seatInfo.getSeatType();
			            SeatInfo seatInfoSend = new SeatInfo(userName, seatNumber, actID + ",cancel", actID);
			            
			          
//			            System.out.println("ActID: " + actID + ", UserName: " + userName + ", SeatNumber: " + seatNumber + ", SeatType: " + seatType);
			            
			            // 添加 seatInfoSend 列表中
			            seatInfoToSend.add(seatInfoSend);
			        }
			    }
			    
			    // 从purchasedSeatInfoSet中移除符合條件的資料
			    purchasedSeatInfoSet.removeAll(seatsToRemove);

			    // Redis取消操作
			    for (PurchasedSeatInfo seatInfo : seatsToRemove) {
			        String seatNumber = seatInfo.getSeatNumber();
			        redisService.performRedisCancelOperations(actID, userName, seatNumber);
			    }
			    
			    // 轉換 JSON
			    Gson gson = new Gson();
			    String seatInfoJson = gson.toJson(seatInfoToSend);
			    
			    // 發送消息
			    sendSeatInfoToClient(seatInfoJson);
			}

			// 座位信息到前端
			private void sendSeatInfoToClient(String seatInfoJson) {
			    
			    for (Session session : connectedSessions) {
			        if (session.isOpen()) {
			            session.getAsyncRemote().sendText(seatInfoJson);
			        }
			    }
			}





			


	// 點選改變購買狀態, 並存入redis, 再將buy 消息送到前端
			private void handleBuyOperation(Message message, Session userSession) {
			    String userName = message.getUserName();
			    String seatNumber = message.getSeatNumber();
			    String seatType = message.getType();
			    String actID = message.getActID();
			    
			    if (redisService.isSeatSoldOut(actID, seatNumber)) {
			        return; // 座位已售出
			    }
			    
			    String[] seatTypeParts = seatType.split(",");
			    SeatInfo seatInfo = new SeatInfo(userName, seatNumber, actID + "," + seatType, actID);
			    redisService.storeSeatInfoInRedis(actID, seatInfo);
			    storeSeatInfoInMap(actID, seatInfo, userSession, false);

			    // purchasedSeatInfo離線用戶紀錄
			    PurchasedSeatInfo purchasedSeatInfo = new PurchasedSeatInfo(userName, seatNumber, actID,seatType );
			    purchasedSeatInfoSet.add(purchasedSeatInfo);
//			    printPurchasedSeatInfoSet();
			    String updatedSeatInfoMessage = buildUpdatedSeatInfoMessage(actID);
			    sendMessageToActID(actID, updatedSeatInfoMessage);
			    storeSeatInfoInMap(actID, seatInfo, userSession,true);
			}

	

	/* 新增功能為 向 相同actID 發送消息 */
	private void sendSeatInfoToActIDUsers(String actID, Session userSession) {
	    List<SeatInfo> seatInfos = seatInfoMap.get(actID);
	    if (seatInfos != null) {
	        Gson gson = new Gson();
	        String seatInfoJson = gson.toJson(seatInfos);

	        for (Session session : connectedSessions) {
	            // 檢查是否還在
	            if (session.isOpen()) {
	                String userActID = (String) session.getUserProperties().get("actID");
	                if (actID.equals(userActID) && !session.equals(userSession)) {
	                    session.getAsyncRemote().sendText(seatInfoJson);
	                }
	            }
	        }

	     
	        seatInfos.clear();
	    }
	}



	
	// 新增一个方法， seatInfoMap 中刪除座位
	private void deleteSeatInfoFromMap(String actID, String seatNumber) {
	    List<SeatInfo> seatInfos = seatInfoMap.get(actID);
	    if (seatInfos != null) {
	        Iterator<SeatInfo> iterator = seatInfos.iterator();
	        while (iterator.hasNext()) {
	            SeatInfo seatInfo = iterator.next();
	            if (seatInfo.getSeatNumber().equals(seatNumber)) {
	                iterator.remove(); // 删除座位信息
	                break;
	            }
	        }
	    }
	}

	// storeSeatInfoInMap 方法，刪除
	private void storeSeatInfoInMap(String actID, SeatInfo seatInfo, Session userSession, boolean isDelete) {
	    List<SeatInfo> seatInfos = seatInfoMap.computeIfAbsent(actID, k -> new ArrayList<>());

	    if (isDelete) {
	        
	        deleteSeatInfoFromMap(actID, seatInfo.getSeatNumber());
	    } else {
	        
	        seatInfos.add(seatInfo);
	    }
	   
	    sendSeatInfoToActIDUsers(actID, userSession);
	}


	private String extractUserNameFromSession(Session userSession) {
		Map<String, String> pathParameters = userSession.getPathParameters();
		return pathParameters.get("userName");
	}

	// 該方法為推消息時, 判斷用戶是否在同個actID, 並將發送消息者本身排除在外發送消息
	private void broadcastSeatInfoToAllExcept(String excludedUserName, Session excludedSession) {
		Gson gson = new Gson();

		List<SeatInfo> filteredSeatInfoList = new ArrayList<>();

		String userActID = (String) excludedSession.getUserProperties().get("actID"); // 得到自己的actID

		for (SeatInfo seatInfo : seatInfoList) {
			String seatUserActID = seatInfo.getActID();

			if (!excludedUserName.equals(seatInfo.getUserName()) && userActID.equals(seatUserActID)) {
				filteredSeatInfoList.add(seatInfo);
			}
		}

		String seatInfoJson = gson.toJson(filteredSeatInfoList);

		for (Session session : connectedSessions) {
			if (session.isOpen() && !session.equals(excludedSession)) {
				session.getAsyncRemote().sendText(seatInfoJson);
			}
		}
	}

	// 調用該方法時 會判斷所屬actID 將符合userName的用戶所擁有但不包含soldOut的座位刪除,
	// 並發送被取消的座位消息
	private void deleteSeatsByUserNameALL(String actID, String userName, Session session) {
	    // 離線刪除 所有相關
		cancelSeatPurchase(actID, userName);
	    // 推送更新後的座位信息
	    sendSeatInfoToActIDUsers(actID, session);
	}
	
	
	// 調用該方法時, 會將用戶目前 所選擇座位狀態更改為soldOut, 並向前端發送消息
	public static void markSeatsAsSoldOutByUserName(String actID, String targetUserName) {
	    Map<String, String> modifiedSeatsData = redisService.markSeatsInRedis(actID, targetUserName);
	    int modificationCount = modifiedSeatsData.size();
//	    System.out.println("Modification Count: " + modificationCount);

//	    if (modificationCount > 0) {
//	        sendMarkedDataToServlet(actID, targetUserName, modifiedSeatsData, modificationCount);
//	    }
	}
	
	
	
	// 調用該方法向servlet 傳遞購買消息
//	private static void sendMarkedDataToServlet(String actID, String targetUserName,
//			Map<String, String> modifiedSeatsData, int modificationCount) {
//		try {
//			String servletUrl = "http://localhost:8080/newview/SeatOrderList";
//			URL url = new URL(servletUrl);
//			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			conn.setRequestMethod("POST");
//			conn.setDoOutput(true);
//			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//
//			StringBuilder postDataBuilder = new StringBuilder();
//			postDataBuilder.append("actID=").append(URLEncoder.encode(actID, "UTF-8"));
//			postDataBuilder.append("&targetUserName=").append(URLEncoder.encode(targetUserName, "UTF-8"));
//
//			// 添加修改後的數據
//			for (Map.Entry<String, String> entry : modifiedSeatsData.entrySet()) {
//				String seatNumber = entry.getKey();
//				String seatData = entry.getValue();
//				postDataBuilder.append("&seatNumber_").append(URLEncoder.encode(seatNumber, "UTF-8"));
//				postDataBuilder.append("&seatData_").append(URLEncoder.encode(seatData, "UTF-8"));
//			}
//
//			postDataBuilder.append("&modificationCount=").append(modificationCount);
//
//			OutputStream os = conn.getOutputStream();
//			os.write(postDataBuilder.toString().getBytes("UTF-8"));
//			os.flush();
//			os.close();
//
//			int responseCode = conn.getResponseCode();
//			if (responseCode == HttpURLConnection.HTTP_OK) {
//
//			}
//
//			conn.disconnect();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	// 像擁有相同actID 用戶發送消息
	private void sendMessageToActID(String actID, String message) {
		List<Session> sessions = (List<Session>) actIDToSessionMap.get(actID);

		if (sessions != null) {
			Gson gson = new Gson();
			String seatInfoJson = gson.toJson(message);

			for (Session session : sessions) {

				if (session.isOpen()) {
					session.getAsyncRemote().sendText(seatInfoJson);

				}
			}
		}
	}

	// 該方法為 更新狀態
	private String buildUpdatedSeatInfoMessage(String actID) {

		List<SeatInfo> seatInfos = seatInfoMap.get(actID);
		Gson gson = new Gson();
		String seatInfoJson = gson.toJson(seatInfos);

		JsonObject json = new JsonObject();
		json.addProperty("type", "seatInfoUpdate");
		json.addProperty("actID", actID);
		json.addProperty("seatInfo", seatInfoJson);

		return json.toString();
	}

	// 方法區
	@OnOpen
	public void onOpen(@PathParam("userName") String userName, @PathParam("actID") String actID, Session userSession)
			throws IOException {
		synchronized (connectedSessions) {
			connectedSessions.add(userSession);
		}
		userIdToUserNameMap.put(userSession.getId(), userName);
		userSession.getUserProperties().put("actID", actID);
		executorService.submit(() -> {
			 //使用其他線呈 來執行監聽器(可能會改位置)
	            RedisListener.start();
	        });
//	      System.out.println("WebSocket connected - userName: " + userName + ", actID: " + actID);

		sendRedisDataToClients(userSession);
	}

	@OnMessage
	public void onMessage(Session userSession, String message) {
		executorService.submit(() -> {
			try {

//	              System.out.println("Received message: " + message);

				handleMessage(userSession, message, 100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void handleMessage(Session userSession, String message, long delayMillis) {
		Gson gson = new Gson();

		try {
			Message messageObject = gson.fromJson(message, Message.class);
			String senderActID = messageObject.getActID();
			String messageType = messageObject.getType();
			String userActID = (String) userSession.getUserProperties().get("actID");
//			System.err.println(userActID);

			if (senderActID.equals(userActID)) {
				if ("cancel".equals(messageType)) {
					// 取消狀態,回傳前端
					handleCancelOperation(messageObject, userSession);
				} else if ("buy".equals(messageType)) {
//					System.err.println("收到購買");
					// 購買狀態,回傳前端
					handleBuyOperation(messageObject, userSession);
				} else if ("disconnect".equals(messageType)) {
					// 用戶斷線, 將未售出座位更改為取消,發送回前端
//					System.err.println("disconnect");
					deleteSeatsByUserNameALL(senderActID, messageObject.getUserName(), userSession);
				} else if ("soldOut".equals(messageType)) {
					// 用戶決定購買, 將座位更改為已賣出,發送回前端
					markSeatsAsSoldOutByUserName(senderActID, messageObject.getUserName());
				}else if("inCart".equals(messageType)) {
					redisService.markSeatsInRedisAndDB1(senderActID,messageObject.getUserName());
				}
			}
		} catch (JsonSyntaxException e) {
			System.err.println("Failed to parse JSON message: " + message);
		}
	}

	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
	    try {
	        synchronized (connectedSessions) {
	            connectedSessions.remove(userSession);
	        }

	        String userName = extractUserNameFromSession(userSession);

	        if (!isUserSeatSoldOut(userName)) {
	            broadcastSeatInfoToAllExcept(userName, userSession);
	        }

	        String text = String.format("session ID = %s, disconnected; close code = %d; reason phrase = %s",
	                userSession.getId(), reason.getCloseCode().getCode(), reason.getReasonPhrase());
//	        System.out.println(text);
//	        System.out.println("斷線正常");
	    } catch (Exception e) {
//	    	System.out.println("斷線異常");
	        e.printStackTrace();
	    }
	}


	private boolean isUserSeatSoldOut(String userName) {
		synchronized (seatInfoList) {
			return seatInfoList.stream()
					.anyMatch(info -> userName.equals(info.getUserName()) && "soldOut".equals(info.getSeatType()));
		}
	}

	@OnError
	public void onError(Session userSession, Throwable e) {
		System.out.println("Error: " + e.toString());
	}

}
