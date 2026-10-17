-- Recovered category IDs/labels from both original forum_dopost.html and forum_revisedpost.html.
-- Applies to the synthetic recovery database, not historical member data.
INSERT INTO postcategory(postCategoryID,postCategoryName) SELECT 1,'綜合討論All(換票、揪團)' WHERE NOT EXISTS(SELECT 1 FROM postcategory WHERE postCategoryID=1);
UPDATE postcategory SET postCategoryName='綜合討論All(換票、揪團)' WHERE postCategoryID=1;
INSERT INTO postcategory(postCategoryID,postCategoryName) SELECT 2,'音樂表演Music' WHERE NOT EXISTS(SELECT 1 FROM postcategory WHERE postCategoryID=2);
UPDATE postcategory SET postCategoryName='音樂表演Music' WHERE postCategoryID=2;
INSERT INTO postcategory(postCategoryID,postCategoryName) SELECT 3,'戲劇表演Drama' WHERE NOT EXISTS(SELECT 1 FROM postcategory WHERE postCategoryID=3);
UPDATE postcategory SET postCategoryName='戲劇表演Drama' WHERE postCategoryID=3;
INSERT INTO postcategory(postCategoryID,postCategoryName) SELECT 4,'舞蹈表演Dance' WHERE NOT EXISTS(SELECT 1 FROM postcategory WHERE postCategoryID=4);
UPDATE postcategory SET postCategoryName='舞蹈表演Dance' WHERE postCategoryID=4;
INSERT INTO postcategory(postCategoryID,postCategoryName) SELECT 5,'脫口秀Talk Show' WHERE NOT EXISTS(SELECT 1 FROM postcategory WHERE postCategoryID=5);
UPDATE postcategory SET postCategoryName='脫口秀Talk Show' WHERE postCategoryID=5;
INSERT INTO postcategory(postCategoryID,postCategoryName) SELECT 6,'演唱會Singing Concert' WHERE NOT EXISTS(SELECT 1 FROM postcategory WHERE postCategoryID=6);
UPDATE postcategory SET postCategoryName='演唱會Singing Concert' WHERE postCategoryID=6;
INSERT INTO postcategory(postCategoryID,postCategoryName) SELECT 7,'講座Lecture' WHERE NOT EXISTS(SELECT 1 FROM postcategory WHERE postCategoryID=7);
UPDATE postcategory SET postCategoryName='講座Lecture' WHERE postCategoryID=7;
