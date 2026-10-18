# 第三方素材與授權

更新日期：2026-10-18。原始碼已由維護者改為公開 GitHub 儲存庫；本次文件更新未變更遠端或網站部署。

## 團隊與課程

維護者已於 2026-10-18 確認團隊程式與課程內容可公開分享。本文件依其確認記錄，未另外調閱契約。這不涵蓋所有第三方套件與素材，也不自動授予整庫開源再授權權利；目前沒有全庫 LICENSE。

## 隨附素材與前端套件

| 項目 | 來源、條款與檢查結果 |
|---|---|
| Bootstrap 5.3.8 | 官方 MIT；CSS 與 bundle 原檔頭保留，全文見 licenses/bootstrap-5.3.8.txt。已移除舊 Bootstrap 與 jQuery |
| Popper Core 2.11.8 | 包含在 Bootstrap bundle，MIT 全文見 licenses/popper-core-2.11.8.txt；不再使用 Popper 1.12.5 |
| Font Awesome 4.7.0 | 官方將 fonts 指定為 SIL OFL 1.1，CSS／LESS／SCSS 為 MIT，其他文件為 CC BY 3.0；原著作權與檔頭保留。另補官方版本 README、OFL 及 MIT 條款。字型 metadata 記載 Copyright Dave Gandy 2016；不得把圖示商標當作服務背書 |
| 幾何海報與輪播圖 | 原繪圖構圖，文字已改用 OFL 1.1 的 Noto Sans TC 重新輸出共 11 張 PNG，不散布系統字型檔；見 FONT_REVIEW.md |
| 網站截圖 | 為本機網站實際畫面，使用虛構展示資料；仍包含網站使用的圖示、字型及介面素材 |
| 測試圖片 | 由 Java 工具產生的純色圖 |
| 原始網站版型 | 團隊／課程可公開分享已由維護者確認，並另確認未使用課程外模板或素材 |

Font Awesome 官方來源核對結果見 [檔案比對](evidence/fontawesome-source-check.json)。共核對 37 個檔案：7 個逐位元一致，30 個在正規化換行後一致。沒有未能對應官方內容的檔案，但不宣稱整個目錄逐位元等同官方發行包；本輪未改動這些素材。

## Java 相依套件

Maven 不提交快取 JAR，但 WAR 建置會包含執行相依套件；沒有把 JAR 放到 Git 不代表後續散布 WAR 可省略通知。修正前部署包 95 個 Maven 套件的 POM 宣告保留在歷史清單；本輪精簡後的實際依賴見 [目前套件清單](evidence/dependency-review-after.json)。部分宣告由父 POM 繼承，本次未完成全部遞迴解析；Oracle 驅動已移除。MySQL Connector/J 等仍依各自條款，不能統一宣稱為 MIT。

完整傳遞依賴、建置外掛及最終 WAR 的授權通知彙整仍未完成。本輪為素材與宣告盤點，不是完整法律審查；授權原文保留原語言，不改寫成中文條款。

## 授權原文與來源

- [歷史 jQuery MIT](licenses/jquery-3.2.1.txt)（目前已移除）
- [Bootstrap 5.3.8 MIT](licenses/bootstrap-5.3.8.txt)
- [Popper Core 2.11.8 MIT](licenses/popper-core-2.11.8.txt)
- [Font Awesome 4.7.0 官方說明](licenses/fontawesome-4.7.0-upstream-readme.txt)
- [OFL 1.1 條款](licenses/OFL-1.1.txt)
- [MIT 條款範本](licenses/MIT.txt)：一般條款原文，實際權利人以原套件檔頭為準，不取代著作權通知。
- [Font Awesome 官方授權頁](https://fontawesome.com/v4/license/)
- [下載來源與 SHA-256](licenses/source-register.json)

公開決策與未完成事項以 [公開前狀態](PUBLICATION_REVIEW.md) 為準。

網站字型與圖片字型的詳細檢查見 [字型說明](FONT_REVIEW.md)。
