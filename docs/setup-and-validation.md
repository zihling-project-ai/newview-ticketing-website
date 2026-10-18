# 本機執行、搬移與驗證

## 1. 先確認使用方式

下載只取得原始碼，不會自動安裝軟體或啟動Tomcat。這是以本機Tomcat執行的Java網站，MySQL與Redis由Docker提供；網址127.0.0.1只限自己的電腦，沒有公開部署。

前端直接維護app/src/main/webapp內的HTML／CSS／JS，沒有頁面產生器、Node或npm建置需求。修改頁首導覽等重複HTML時，要同步相關頁面；共用樣式與互動集中在css、js。

## 2. 工具與作業系統

| 工具 | 需求與確認方式 |
|---|---|
| Java／JDK | 17；`java -version`及`javac -version`均應可執行，避免只有JRE |
| Maven | 3.9系列；`mvn -version`確認使用Java17 |
| Python | Python3；`python3 --version`；工具與測試使用標準函式庫 |
| Docker | 安裝並啟動Docker服務；`docker info`應成功 |
| Apache Tomcat | 自行安裝9系列，含bin/catalina.sh；Tomcat10+的Jakarta套件不能直接替代 |

本機已實測macOS／Apple Silicon、Java17與Tomcat9。腳本使用POSIX shell、ps與訊號，Linux可依相同步驟操作但尚未獨立驗證；Windows原生操作尚未驗證，不能直接照抄這組shell指令。

首次下載Maven相依套件與Docker映像需要網路；已有快取才可能離線。現有完整回測重用本機Maven快取，沒有把「全新電腦空快取下載」標為已驗證。

## 3. 下載與私人設定

將完整專案資料夾放到可寫入的位置。開啟終端機，用自己的實際路徑進入專案根目錄，例如：

```sh
cd "/你存放專案的位置/newview-ticketing"
export CATALINA_HOME="/你安裝的位置/apache-tomcat-9"
```

上面路徑是示意，必須替換；執行目錄應能看到README.md、app、database、scripts及tests。CATALINA_HOME是Tomcat安裝目錄，裡面應有bin/catalina.sh；每次新的終端機須重新設定，或自行加入shell設定。

.env.example只是環境變數範例，工具**不會自動讀取.env**。若要自訂私有資料位置，可於所有指令前設定同一個絕對路徑：

```sh
export NEWVIEW_STATE_DIR="$HOME/.local/share/newview-ticketing"
```

不設定時，預設使用專案旁的隱藏資料夾.newview-ticketing-local。工具會建立尚不存在的父目錄；設定、隨機資料庫密碼、Maven快取、Tomcat工作檔、PID、日誌與種子備份均放在這個私人位置，**不在要上傳的來源內**。不要刪除現用mysql.env，也不要讓不同專案共用此設定。可用NEWVIEW_MAVEN指定Maven執行檔，NEWVIEW_MAVEN_REPO指定現有快取；一般使用者只要mvn在PATH即可。

## 4. 第一次建立環境

確認Docker已運作後，依序執行：

```sh
python3 scripts/local.py build
python3 scripts/setup/setup_database.py
python3 scripts/setup/setup_redis.py
python3 scripts/local.py start
```

- build編譯app/pom.xml，產出app/target/newview-0.0.1-SNAPSHOT.war；建置日誌在私人資料位置的maven-build.log。
- setup_database產生隨機密碼，建立專用MySQL容器與volume，只對空白庫匯入database/01–14；已有表時不覆寫。SQL不會在一般啟動時重跑。
- setup_redis建立本專案專用的短期座位儲存服務。
- start啟動既有資料容器、等待MySQL、將WAR載入獨立Tomcat。啟動命令返回不代表HTTP已就緒；等網站實際可讀再進行下一步。

網址：http://127.0.0.1:8083/newview/forum_home.html

| 服務 | 限本機連接埠 | 專用名稱 |
|---|---|---|
| Tomcat | 8083 | 網站路徑/newview |
| MySQL | 3309映射容器3306 | newview-ticketing-mysql；volume為newview-ticketing-mysql-data |
| Redis | 6383映射容器6379 | newview-ticketing-redis；只存暫鎖，不持久化 |

這些預設值由程式、容器與測試共同使用；僅修改.env的其中一個連接埠，不會自動調整整套環境。

## 5. 首次建立虛構展示內容

**僅在新建、可拋棄的本機展示庫執行一次。第一行會先備份，再替換全部展示資料；不要對有需保留資料的庫重跑。**

```sh
python3 scripts/setup/seed_portfolio.py --replace-local-showcase
python3 scripts/setup/seed_portfolio_orders.py
```

第二行透過實際網站介面建立不同狀態的訂單，拒絕已有訂單的庫。請先確認網站已能開啟。建立後：

| 展示帳號 | 展示密碼 |
|---|---|
| yuchen | NewView-local-2026! |
| yian | NewView-local-2026! |

帳密刻意用於虛構展示，勿使用真實密碼。註冊／忘記密碼只顯示本機驗證碼，不寄信。活動日期固定2026年；日後重建時須檢查是否已過期。日期與未來資料可攜性不等於永久有效的展示內容。

## 6. 之後啟動與停止

已有資料與WAR時，確認Docker已運作並重新設定CATALINA_HOME，再執行：

```sh
python3 scripts/local.py start
```

已啟動則不必重跑。不要再次執行seed，也不需重新匯入SQL。

先停止Tomcat，再按需要停止資料服務；保留資料volume：

```sh
python3 scripts/local.py stop
docker stop newview-ticketing-mysql newview-ticketing-redis
```

stop核對記錄PID的程序身分後才停止。若受限執行環境拒絕ps／訊號權限，應改在一般使用者終端機執行；不能把拒絕存取當成停止成功，也不要任意刪PID或終止其他程序。

## 7. 直接修改前端或Java

直接編輯app/src/main/webapp內的HTML／CSS／JS或app/src/main/java內的Java。沒有scripts/pages，也不需要產生HTML。

先停網站，再建置並啟動：

```sh
python3 scripts/local.py stop
python3 scripts/local.py build
python3 scripts/local.py start
```

刷新瀏覽器確認結果。不要以修改Tomcat解壓出的檔案作為永久來源，重新部署會覆蓋它們。這些步驟不重建資料庫。

## 8. 換電腦或別人下載

**重新建立展示環境：**複製／下載來源，安裝工具，設定新的CATALINA_HOME，依第4、5節執行即可；不用複製原電腦的帳密、PID、Tomcat工作檔或Maven快取。第一次下載不會自動啟動服務。

**保留自己新增的會員／訂單：**僅有來源與種子無法還原這些資料；必須另做私人MySQL備份，並在新機相容版本資料庫還原。資料庫備份不可放進GitHub；完整資料搬遷／災難復原尚未演練，這裡不將重新建立展示庫當成既有資料轉移。

同一台電腦重複下載到另一個位置，仍可能遇到既有容器／volume同名與連接埠衝突；此套預設只支援一組newview-ticketing服務，不能假設每個副本各自隔離。

## 9. 常見問題

| 現象 | 處理方式 |
|---|---|
| mvn/java/javac/docker找不到 | 檢查安裝及PATH，並用第2節的版本指令確認 |
| Docker無法連線 | 啟動Docker並確認docker info成功 |
| CATALINA_HOME或Tomcat版本不符 | 指向Tomcat9安裝目錄，不是bin或專案資料夾 |
| 找不到WAR | 先成功build，查看私人maven-build.log |
| 8083被占用或已有PID | 確認是否已啟動；停止自己的網站，不要強制終止不明程序 |
| MySQL無法登入／初始化卡住 | 查看mysql.env是否對應原容器；保留現有env與volume，勿生成新密碼來猜測既有庫 |
| 中途SQL匯入失敗 | 查看最後失敗檔與實際表結構；勿盲目重跑ALTER或刪volume |
| 網站剛啟動顯示無法連線 | 等待Tomcat載入，檢查私人tomcat-console.log及tomcat/logs |
| 已建立訂單，種子工具拒絕執行 | 這是防止覆寫的保護；一般啟動不需種子 |
| 選位暫存逾時或Redis重新啟動 | 重新選位；已成立座位以MySQL為準 |

## 10. 驗證範圍

2026-10-18此獨立版本已建庫、啟動並完成15組547項整合檢查，另經瀏覽器驗證會員、社群、收藏、選位、混合購物車、數量與優惠券、跨廠商拆單、票券核銷／評價、取消與釋位。來源指紋與結果見[整合回測](evidence/independent-validation.json)、[瀏覽器紀錄](evidence/independent-browser.json)、[執行狀態](evidence/current-validation.json)。

主測試指令為python3 tests/verify_<名稱>.py。測試會建立／清理專用資料；座位測試會短暫停Redis，交易測試會注入失敗，請勿與手動操作同時執行。

本次文件與搬移檢查見[啟動文件檢查](evidence/startup-doc-review.json)。未將文件更新冒稱重新跑過完整547項。Windows原生、新電腦空快取下載、完整資料搬遷、公開部署、真實支付／寄信、負載與全裝置驗證均未完成。

## 11. 認識scripts/setup中的工具

這些Python檔案是準備本機資料的輔助工具，不是Java網站每次處理請求都要執行的程式。

| 檔案 | 做什麼 | 何時使用 |
|---|---|---|
| setup_database.py | 產生私人DB設定、建立MySQL容器，對空白庫套用database/01–14 | 第一次建立環境；不會重設已有表 |
| setup_redis.py | 建立或啟動專用Redis，供Java暫鎖座位 | 第一次建立Redis；一般啟動由local.py啟動既有容器 |
| seed_portfolio.py | 放入虛構會員、活動、文章、圖片、優惠券及座位配置 | 首次建立展示內容；有備份但會替換全部展示資料，不要在現用資料重跑 |
| seed_portfolio_orders.py | 經由網站介面建立待使用／取消／已核銷等訂單範例 | 新建展示庫且尚無訂單時使用；不是啟動必需的定時工作 |
| venue_layouts.py | 定義小／中／大場地和座位標籤，供種子與測試共用 | 由其他工具匯入，不需手動執行 |

.py表示Python原始碼。__pycache__是Python自動建立的快取，不是手寫功能，不需編輯或上傳；本次已移出，但執行Python後可能再次產生，.gitignore會排除。

例如venue_layouts.py中的`1:('小型',10,10,None)`表示編號1的場地是10排、每排10席、沒有中央排除區。`A01`代表A排第1席；中／大場地有保留空區，不是每個矩形格子都可售。

選位頁的HTML只提供容器與控制項；JavaScript向Java的Seats介面取得座位與狀態，再畫出按鈕。MySQL記錄配置與已售座位，Redis記錄短期保留。因此不是把即時座位名單、會員或訂單寫死在HTML。

## 12. 上傳前的執行產物

app/target、__pycache__、.DS_Store不屬於來源，本次已移到專案外備份。重新建置／執行Python／使用Finder後可能再次產生，.gitignore會排除。從乾淨來源啟動前應先build；目前已在跑的Tomcat使用專案外的已部署WAR，不因清理來源資料夾的target而停止。

## 13. 相依套件與字型更新

2026-10-18 已移除未使用的框架依賴、升級必要 JSON／資料庫驅動與 Bootstrap，並以 Noto Sans TC 重新輸出展示圖片。更新後已重建 WAR、部署到本機 Tomcat，並完成 15 組整合回測及 300 張 QR PNG 解碼。套件掃描範圍與結果見 [相依套件檢查](DEPENDENCY_REVIEW.md)，字型來源見 [字型檢查](FONT_REVIEW.md)。新驗證不取代先前歷史快照。
