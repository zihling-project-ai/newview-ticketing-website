# 公開狀態與檢查紀錄

更新日期：2026-10-18。維護者已將 [newview-ticketing-website](https://github.com/zihling-project-ai/newview-ticketing-website) 改為 Public。本次以未提供登入憑證的 GitHub REST API 取得 HTTP 200，確認 private=false、visibility=public、預設分支為 main；README.md、app/pom.xml 與 docs/FONT_REVIEW.md 的原始內容也均可匿名取得（HTTP 200）。這確認程式碼可由外部讀取，不代表網站已部署至網際網路；網站仍為本機展示。

套件、字型與前一輪文件修改已由維護者確認完成 commit 與 push。本次公開狀態文件更新僅修改本機，尚未 commit／push，未變更任何 GitHub 設定。

## 已完成

- 網站、重建 SQL、必要啟停與資料工具、測試、中文工程文件、圖表及截圖分開整理。
- 私人設定、備份與執行產物放在來源之外；先前已檢查待提交來源與提交紀錄的敏感資訊。這不是永久有效的保證，每次新增內容仍需檢查。
- 已完成本機建置及功能驗證；範圍與限制見 [啟動文件](setup-and-validation.md)。
- 本次已查閱前端授權、補充通知與授權原文，並完成部署包及前端共 98 項套件的 OSV 版本比對。

## 檢查結果與持續維護範圍

- 原 21 項套件命中 79 筆公告已藉移除與更新處理；修正後 40 項清單 OSV 未命中，且完成整合回測，見 [套件檢查](DEPENDENCY_REVIEW.md)。仍不宣稱零漏洞。
- Maven 應用程式依賴樹含 provided 已取得；建置外掛傳遞依賴、容器及平台檢查仍未完成。
- 第三方套件部分授權由父 POM 繼承，完整授權及通知核對仍未完成，見 [素材與授權](THIRD_PARTY_NOTICES.md)。

## 維護者已確認事項

- 2026-10-18，維護者於本次審查確認團隊程式與課程內容可公開分享；據此記錄為已確認，不再列為待取得同意。這是維護者提供的確認，沒有另外調閱團隊或課程契約。
- 2026-10-18，維護者進一步確認沒有使用課程以外的模板或素材；此項已完成確認。明確盤點的第三方函式庫及字型仍依各自授權條款保留通知。
- 海報與輪播共 11 張 PNG 已用 Noto Sans TC（OFL 1.1，字重 500）重新輸出，替換本機庫內 26 筆可確認的原展示圖片；沒有重置其他資料。Font Awesome 授權與網站系統字型使用方式見 FONT_REVIEW.md。

團隊與課程可公開分享的確認，不等於已授權將全庫以 MIT 等條款再授權；目前不新增整庫開源 LICENSE。公開可讀與授予他人開源再利用權是不同決定。

## 歷史驗證文件

evidence/current-validation.json 與 final-content-review.json 中的「未上傳」描述是當時檢查快照，保留歷史紀錄，不代表現在遠端狀態。現況以本文件為準；修正前後套件清單分別見 evidence/dependency-review.json 與 evidence/dependency-review-after.json。

維護者已決定並完成公開程式碼；上述未涵蓋項目仍保留作為檢查限制，不宣稱已完成正式部署或全面安全稽核。
