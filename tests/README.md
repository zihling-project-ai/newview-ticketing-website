# 測試說明

主要15組整合測試涵蓋會員、社群、圖片、密碼、收藏、活動、購物車、座位、取消、票券、評價、優惠券、前台流程、座位配置及訂單對照。先依docs/setup-and-validation.md建立獨立展示環境，再執行python3 tests/verify_<名稱>.py。

測試新增與清理自己的資料，不能用在真實資料庫。seats會停止／重啟本專案Redis；cart與coupons需容器內測試用root權限做失敗注入。test helpers保留exec前綴共用方式，並非完整重構成測試框架。

verify_qr_generation.py與QRVerifiedProbe.java是額外300個隨機QR往返檢查。GenerateImages.java產生原創純色測試圖。套件與來源位置已調整，本副本執行狀態以docs/evidence/current-validation.json為準，不把先前版本結果視為改名後全部通過。
