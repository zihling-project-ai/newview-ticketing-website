# 技術選型與用途

以下依目前程式與設定盤點；當年未有紀錄的選型原因標示為「未確認」。取捨是對現況的工程分析，不冒充當年的設計決策。

| 技術／版本 | 實際責任 | 程式證據與驗證界線 |
|---|---|---|
| Java 17 | 編譯與執行後端 | app/pom.xml；本次重新編譯 |
| Servlet 4、Tomcat 9 | HTTP、Session、WAR 載入 | app/src/main/webapp/WEB-INF/web.xml；已實際啟動並通過本機整合回測 |
| Maven、明確版本的必要依賴 | WAR 打包與相依版本管理 | app/pom.xml；已移除未使用的 Spring Boot／MVC、Thymeleaf、Oracle 與舊日誌套件 |
| Hibernate 5.6.15.Final、MySQL Connector/J 26.7.0 | ORM、連線與原生 SQL | app/src/main/java/com/newview/util/HibernateUtil.java；hibernate.cfg.xml |
| MySQL 8／InnoDB | 會員、活動、社群、訂單、票券與座位永久狀態 | database/01–14；重建 SQL，不是找回的原始 SQL |
| Redis 7、Jedis 2.9.0 | 十分鐘暫鎖座位 | SeatStore.java；不是會員 Session 或全站快取 |
| HTML／CSS／JavaScript、AJAX | 前台顯示與同源請求 | app/src/main/webapp；直接維護HTML/CSS/JS，無頁面產生器 |
| Bootstrap 5.3.8（含 Popper Core 2.11.8）、Font Awesome 4.7.0 | 前端版型、圖片輪播與圖示 | 已移除 jQuery／舊 Bootstrap；版本掃描與範圍見 DEPENDENCY_REVIEW.md |
| ZXing 3.5.3、ImageIO | QR 產生／辨識、PNG/JPEG 正規化 | LocalTicketQR.java、ImageSupport.java |
| Python 3、Docker | 建置輔助、建庫、種子與測試 | scripts、tests；不參與一般使用者請求執行 |

## 資料一致性與取捨

MySQL 為已成立座位與訂單的依據，Redis 僅記錄短期保留。Lua 執行座位暫鎖與相同擁有者／token 比對刪除；讀取不延長 600 秒期限。SQL 交易、列鎖與唯一鍵共同處理訂單／座位重複。

兩個儲存系統不是一個分散式交易。Redis 清理失敗可能留下短暫鎖至到期；站票沒有有限容量帳本，不能宣稱全面防超賣。改為 SQL-only 或更換資料庫是可能替代方案，但不屬本次必要整理。

Apache 在這裡指 Tomcat 應用伺服器與 Maven 建置工具，沒有 Apache HTTP Server 的部署證據。
