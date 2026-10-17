-- Names follow the historical activity UI; IDs are new local records, not recovered originals.
INSERT INTO actCategory(actCategoryName)
SELECT name FROM (SELECT '脫口秀' name UNION ALL SELECT '講座' UNION ALL SELECT '音樂會' UNION ALL SELECT '歌舞秀' UNION ALL SELECT '演唱會' UNION ALL SELECT '戲劇' UNION ALL SELECT '舞蹈') names
WHERE NOT EXISTS(SELECT 1 FROM actCategory c WHERE c.actCategoryName=names.name);
INSERT INTO city(cityName) SELECT '臺北市' WHERE NOT EXISTS(SELECT 1 FROM city WHERE cityName='臺北市');
INSERT INTO city(cityName) SELECT '高雄市' WHERE NOT EXISTS(SELECT 1 FROM city WHERE cityName='高雄市');
UPDATE act SET actCategoryID=(SELECT MIN(actCategoryID) FROM actCategory WHERE actCategoryName='音樂會'),cityAddressID=(SELECT MIN(cityAddressID) FROM city WHERE cityName='臺北市'),actContent='這是本機還原使用的虛構音樂會，供活動瀏覽、詳情與收藏驗證。日期、場地、價格均為合成展示資料，未開放購票。' WHERE actName='本機合成展示｜週末音樂會';
UPDATE act SET actCategoryID=(SELECT MIN(actCategoryID) FROM actCategory WHERE actCategoryName='戲劇'),cityAddressID=(SELECT MIN(cityAddressID) FROM city WHERE cityName='高雄市'),actContent='這是本機還原使用的虛構劇場活動，供詳情與收藏展示，不代表當年已舉行或售出的活動。' WHERE actName='本機合成展示｜劇場之夜';
