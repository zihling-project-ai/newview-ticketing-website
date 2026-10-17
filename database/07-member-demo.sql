-- Clearly synthetic local demonstration, idempotent inserts, no real checkout.
INSERT INTO act(actName,actIntroduce,actDate,actPrice,cityAddress)
SELECT '本機合成展示｜週末音樂會','本次還原新增的虛構活動，供會員收藏及訂單查詢展示。','2026-12-12',800,'虛構展示場地'
WHERE NOT EXISTS(SELECT 1 FROM act WHERE actName='本機合成展示｜週末音樂會');
INSERT INTO act(actName,actIntroduce,actDate,actPrice,cityAddress)
SELECT '本機合成展示｜劇場之夜','本次還原新增的虛構活動，未啟用購票或付款。','2026-12-20',600,'虛構展示劇場'
WHERE NOT EXISTS(SELECT 1 FROM act WHERE actName='本機合成展示｜劇場之夜');
INSERT INTO orders(userID,ordTime,ordTotal,ordType,actQuantity)
SELECT userID,'2026-10-17 10:00:00',1600,0,2 FROM user u WHERE userAccount='demo_alice'
AND NOT EXISTS(SELECT 1 FROM orders o WHERE o.userID=u.userID AND o.ordTime='2026-10-17 10:00:00');
INSERT INTO orderlist(orderID,actID,actTotal,usedType,OrderListTime)
SELECT o.orderID,a.actID,800,0,'2026-10-17 10:00:00' FROM orders o JOIN user u ON u.userID=o.userID CROSS JOIN act a
WHERE u.userAccount='demo_alice' AND o.ordTime='2026-10-17 10:00:00' AND a.actName='本機合成展示｜週末音樂會'
AND NOT EXISTS(SELECT 1 FROM orderlist l WHERE l.orderID=o.orderID);
INSERT INTO orderlist(orderID,actID,actTotal,usedType,OrderListTime)
SELECT o.orderID,a.actID,800,1,'2026-10-17 10:00:00' FROM orders o JOIN user u ON u.userID=o.userID CROSS JOIN act a
WHERE u.userAccount='demo_alice' AND o.ordTime='2026-10-17 10:00:00' AND a.actName='本機合成展示｜週末音樂會'
AND (SELECT COUNT(*) FROM orderlist l WHERE l.orderID=o.orderID)=1;
INSERT INTO myLike(userID,actID)
SELECT u.userID,a.actID FROM user u CROSS JOIN act a WHERE u.userAccount='demo_alice' AND a.actName='本機合成展示｜週末音樂會'
AND NOT EXISTS(SELECT 1 FROM myLike m WHERE m.userID=u.userID AND m.actID=a.actID);
