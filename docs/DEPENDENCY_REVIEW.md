# 公開前相依套件檢查

更新日期：2026-10-18。本次已移除未使用套件並更新所需依賴；修正後解析的 Maven compile／runtime／provided、前端及固定版本建置外掛合計 40 項，OSV 查詢未命中已知漏洞公告。這是本次範圍內的版本比對結果，不是無漏洞或正式營運安全保證。

## 修正結果

- 移除未使用的 Spring Boot parent／starters、Spring MVC／Data、Thymeleaf、Oracle JDBC、c3p0／proxool、Log4j、SnakeYAML、WebJar Bootstrap、Lombok 與未使用測試套件等依賴來源；保留實際需要的 Servlet、Hibernate、Gson、Redis、Mail 與 QR 支援。
- Jackson databind／core 升級至 2.22.4，Gson 至 2.14.0，MySQL Connector/J 至 26.7.0，編譯 Java 17 成功。
- Bootstrap 改為 5.3.8 bundle（含 Popper Core 2.11.8），移除 jQuery 及舊 Bootstrap／Popper 檔案；文章輪播改用 data-bs-*，保留既有自訂首頁輪播與分類表格。
- 修正後已成功取得 Maven dependency:tree；[修正後版本清單](evidence/dependency-review-after.json) 可重查所有 40 項。前一輪 98 項、79 筆命中結果保留作為歷史基準，不是現況。
- 已完成 15 組原有整合套件及 300 張隨機 QR 圖片解碼，結果見 [回測紀錄](evidence/dependency-update-validation.json)。另確認手機導覽、分類搜尋／排序／分頁及文章兩圖切換。
- 建置外掛只比對外掛本體，未掃描其全部傳遞相依；Tomcat、JDK、Docker 映像與 OS 不在本次 40 項範圍。沒有執行漏洞利用驗證。

## 修正前檢查快照

以下為修正前狀態，僅供追溯。

## 方法與範圍

以現用部署包的 WEB-INF/lib 取得 95 個 Maven 套件項目，另查詢 3 個隨附前端套件，合計 98 項；透過 [OSV API](https://google.github.io/osv.dev/api/) 比對套件名稱與版本。21 項命中 79 筆不重複公告，同一公告可能影響多個套件，不能把各列筆數直接相加視為獨立漏洞數。完整版本、檔案指紋、公告別名、修正範圍與來源見 [機器可讀結果](evidence/dependency-review.json)。

Maven dependency:tree 離線執行因快取沒有 dependency plugin 而失敗，因此改以已部署套件盤點。這不是全新建置的完整依賴樹：未涵蓋所有 test／provided／建置外掛、Tomcat 本體、JDK、MySQL／Redis 映像或作業系統。未命中不代表沒有漏洞。POM 的授權宣告僅作清單，部分授權由父 POM 繼承，尚未全部解析。

本次沒有執行攻擊測試，也沒有改動套件或部署。原有功能回測通過，不代表安全檢查通過。

## 版本命中結果

嚴重度沿用公告 database_specific.severity；屬公告評分，並非本網站可利用性的評分。

| 套件 | 版本 | 命中公告數 | 公告嚴重度分布 |
|---|---|---:|---|
| `org.webjars:bootstrap` | 4.2.1 | 1 | MODERATE: 1 |
| `com.mchange:c3p0` | 0.9.5.5 | 2 | HIGH: 1、MODERATE: 1 |
| `com.fasterxml.jackson.core:jackson-core` | 2.13.3 | 3 | HIGH: 3 |
| `com.fasterxml.jackson.core:jackson-databind` | 2.13.3 | 12 | MODERATE: 6、HIGH: 6 |
| `org.apache.logging.log4j:log4j-api` | 2.17.2 | 1 | MODERATE: 1 |
| `org.apache.logging.log4j:log4j-core` | 2.17.2 | 3 | MODERATE: 3 |
| `com.mchange:mchange-commons-java` | 0.2.19 | 2 | HIGH: 2 |
| `mysql:mysql-connector-java` | 8.0.29 | 1 | HIGH: 1 |
| `org.yaml:snakeyaml` | 1.30 | 7 | HIGH: 2、MODERATE: 5 |
| `org.springframework.boot:spring-boot` | 2.7.0 | 2 | HIGH: 2 |
| `org.springframework.boot:spring-boot-autoconfigure` | 2.7.0 | 2 | MODERATE: 1、HIGH: 1 |
| `org.springframework:spring-context` | 5.3.20 | 2 | MODERATE: 1、LOW: 1 |
| `org.springframework:spring-core` | 5.3.20 | 2 | LOW: 1、HIGH: 1 |
| `org.springframework.data:spring-data-commons` | 2.7.0 | 3 | MODERATE: 2、HIGH: 1 |
| `org.springframework:spring-expression` | 5.3.20 | 7 | MODERATE: 3、HIGH: 3、LOW: 1 |
| `org.springframework:spring-web` | 5.3.20 | 6 | MODERATE: 2、HIGH: 3、CRITICAL: 1 |
| `org.springframework:spring-webmvc` | 5.3.20 | 17 | HIGH: 4、MODERATE: 9、LOW: 2、CRITICAL: 2 |
| `org.thymeleaf:thymeleaf` | 3.0.15.RELEASE | 3 | CRITICAL: 3 |
| `org.thymeleaf:thymeleaf-spring5` | 3.0.15.RELEASE | 3 | CRITICAL: 3 |
| `jquery` | 3.2.1 | 3 | MODERATE: 3 |
| `bootstrap` | 4.0.0-beta | 1 | MODERATE: 1 |

## 修正前的優先處理建議與適用條件（歷史）

1. 前端 jQuery 3.2.1 與 Bootstrap 4.0.0-beta 為頁面直接載入，優先升級或移除不需要的功能，並回測輪播、分類表格、手機導覽及社群。jQuery 官方 3.5.0 說明已修正舊版 HTML 處理中的 XSS 問題；該版本是歷史修正點，不代表今天建議鎖定的最終版本。Bootstrap 另有 WAR 內 WebJar 4.2.1，須一併清理，避免只更新頁面檔案仍留下舊版。
2. Jackson databind 2.13.3 命中 CVE-2022-42003／42004 等公告，專案 OrdersSearchController 確實使用 ObjectMapper；仍須逐項確認資料輸入與設定是否符合觸發條件，不能僅因匯入類別就宣稱可利用。
3. Spring 5.3.20、Boot 2.7.0、SnakeYAML 1.30、Log4j 2.17.2、c3p0 等皆有版本命中。先排除未使用的 starter、模板、驅動與開發工具，再選擇相容的依賴版本，並重新掃描及回測。Boot 3 會涉及 javax／jakarta 與 Tomcat 相容性，不能直接換版號了事。
4. 目前 web.xml 為 Servlet 路由，沒有查到 DispatcherServlet 或 @RequestMapping；部分 Spring MVC、模板或特定日誌設定公告未確認可達。仍保留版本命中，不以「暫時未找到入口」當作已修復。
5. 正式部署前另檢查實際 Tomcat、Java 與資料庫／Redis 映像版本，及重新建立環境的可重現性。

## 來源

- [jQuery 3.5.0 安全修正](https://blog.jquery.com/2020/04/10/jquery-3-5-0-released/)
- [Jackson 2.13.4 發行紀錄](https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.13.4)
- [Spring 官方安全公告](https://spring.io/security/)
- 各項 OSV／GHSA 原文連結與 affected 範圍收錄於機器可讀結果；未把 scanner 提供的任一修正版號直接視為全站升級方案。
