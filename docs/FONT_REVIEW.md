# 網站與圖片字型檢查

更新日期：2026-10-18。

## 網站文字

網站 CSS 使用 -apple-system、BlinkMacSystemFont、Segoe UI、Noto Sans TC、Georgia、sans-serif 等本機字型名稱。只是要求瀏覽器使用訪客已安裝的字型，沒有把 macOS／Windows 系統字型檔加入來源或下載到訪客裝置。這些宣告不代表專案散布相關商用字型；未設定 Google Fonts 或其他遠端字型 CDN。

## 隨附 Font Awesome 4.7.0

隨附 Font Awesome 字型依官方 v4 條款為 SIL OFL 1.1，CSS／LESS／SCSS 為 MIT。保留原作者 Dave Gandy 與授權檔頭，補上 docs/licenses 的全文及網站內 fonts/font-awesome-4.7.0/LICENSE.txt，使部署包也包含通知。使用官方未修改的字型；SVG 與上游的差異僅為 CRLF／LF 換行，本輪同步同版本官方原檔，未改動字形。

官方來源比對紀錄見 evidence/fontawesome-source-check.json（其中 SVG 指紋是修正前的歷史值）；目前 SVG 已恢復官方位元內容。品牌圖示仍屬各品牌商標，不用於暗示背書。

## 海報與輪播圖片

原先圖片中的文字由 macOS STHeiti 光柵化。為避免其適用條款待查，本輪改用 Noto Sans TC，字重 500，重新輸出 8 張活動海報與 3 張輪播 PNG，版面與幾何繪圖構圖保留。Noto 官方 Sans/LICENSE 採 SIL OFL 1.1，可用於此類圖片創作；授權不要求產出的圖片本身採 OFL。

字型檔只存本機私人繪圖工作位置，不放進儲存庫；仍保留 Noto 授權全文、來源與字型／圖片 SHA-256，以便核對。現有資料庫只替換 SHA2 能對應原展示海報的 26 筆圖片，不覆寫使用者另上傳的圖片。見 [圖片字型與指紋](evidence/portfolio-font-review.json)。

本次對已盤點字型有明確來源與條款，沒有發現需要付費授權卻被直接打包的系統字型檔；不對未提供的第三方原始模板、未知素材或未來新增字型作保證。

## 來源與條款

- [Font Awesome v4 官方條款](https://fontawesome.com/v4/license/)
- [Font Awesome 4.7.0 官方版本說明](licenses/fontawesome-4.7.0-upstream-readme.txt)
- [Noto Sans CJK 授權全文](licenses/noto-sans-cjk-OFL.txt)
- [Noto 官方授權來源](https://github.com/notofonts/noto-cjk/blob/main/Sans/LICENSE)
- [SIL OFL 官方網站](https://openfontlicense.org/)
