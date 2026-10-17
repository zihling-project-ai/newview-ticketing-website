-- Synthetic demo data only. Demo accounts are not historical users.
INSERT INTO user(userID,userAccount,userPassword,userName,userNickname,userEmail,buyAuthority,speakAuthority) VALUES
(1,'demo_alice','pbkdf2$210000$BDU906eJciLQSkqQCbWm2g==$DJUuNhK+2UcoARmix2OBNHRNEVehOIRqVZxA9zk9B5w=','Alice 示範','Alice','alice@example.invalid',0,0),
(2,'demo_bob','pbkdf2$210000$QNQ1VN83LuRc1mPhzHpE2Q==$jxFLmlxtORixDQRsgKnwMTFEDcHURLxGQvIaWlhzWmI=','Bob 示範','Bob','bob@example.invalid',0,0);
INSERT INTO postcategory(postCategoryID,postCategoryName) VALUES(1,'活動交流'),(2,'觀演心得');
INSERT INTO post(postID,postHeader,postContent,postCategoryID,userID,postStatus,postDateTime,lastEditedTime) VALUES
(1,'歡迎回到 NewView','這是本機還原使用的合成文章，並非當年的真實資料。',1,1,1,'2026-10-15 10:00:00','2026-10-15 10:00:00'),
(2,'分享你的觀演心得','社群功能正在逐步恢復：閱讀、發文、留言與互動。',2,2,1,'2026-10-16 10:00:00','2026-10-16 10:00:00');
INSERT INTO postmessage(postID,userID,mesContent) VALUES(1,2,'這是合成的測試留言。');
