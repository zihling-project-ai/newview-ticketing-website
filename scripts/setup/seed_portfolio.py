#!/usr/bin/env python3
"""Explicit, backed-up replacement of the isolated local showcase data; never runs on app startup."""
from venue_layouts import ACTIVITY_VENUES, seats
from pathlib import Path
import sys,subprocess,json,time,secrets,hashlib,base64,zipfile,shutil
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
STATE=Path(__import__('os').environ.get('NEWVIEW_STATE_DIR',str(ROOT.parent/'.newview-ticketing-local'))).expanduser().resolve()
if '--replace-local-showcase' not in sys.argv:raise SystemExit('Requires --replace-local-showcase; replaces only newview_recovery after a complete private backup')
archive=STATE/'archive'/time.strftime('%Y%m%d-%H%M%S');archive.mkdir(parents=True,exist_ok=False);archive.chmod(0o700)
with (archive/'database.sql').open('wb') as f:
 subprocess.run(['docker','exec','newview-ticketing-mysql','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" exec mysqldump --single-transaction --no-tablespaces --skip-triggers --hex-blob -u"$MYSQL_USER" newview_recovery'],stdout=f,check=True)
with zipfile.ZipFile(archive/'source-at-data-replacement.zip','w',zipfile.ZIP_DEFLATED) as z:
 for folder in ['app/src','scripts','tests','database']:
  for p in (ROOT/folder).rglob('*'):
   if p.is_file() and '__pycache__' not in p.parts:z.write(p,p.relative_to(ROOT))
war=STATE/'tomcat/webapps/newview.war'
if war.exists():shutil.copy2(war,archive/'previous-deployed.war')
(archive/'manifest.json').write_text(json.dumps(dict(database='newview_recovery',databaseSha256=hashlib.sha256((archive/'database.sql').read_bytes()).hexdigest(),purpose='Private reversible snapshot before replacing generic recovery fixtures with fictional editorial content'),indent=2))
def q(value):
 if value is None:return 'NULL'
 if isinstance(value,(int,float)):return str(value)
 if isinstance(value,bytes):return '0x'+value.hex()
 return 'CONVERT(0x'+str(value).encode().hex()+' USING utf8mb4)'
statements=['START TRANSACTION;']
def insert(table,**values):statements.append('INSERT INTO '+table+'('+','.join(values)+') VALUES('+','.join(q(v) for v in values.values())+');')
for table in ['local_coupon_redemption','local_seat_booking','local_cart_seat','local_simulation_order','local_checkout_receipt','cartact','orderlist','orders','useDiscount','discount','compic','actpic','myLike','local_seat_layout','act','postpic','postmessage','likes','report','post','pubUser','Publisher','administrator','user','city','actCategory','postcategory']:statements.append('DELETE FROM '+table+';')
for n,name in enumerate(['綜合討論All(換票、揪團)','音樂表演Music','戲劇表演Drama','舞蹈表演Dance','脫口秀Talk Show','演唱會Singing Concert','講座Lecture'],1):insert('postcategory',postCategoryID=n,postCategoryName=name)
for n,name in enumerate(['脫口秀','講座','音樂會','歌舞秀','演唱會','戲劇','舞蹈'],1):insert('actCategory',actCategoryID=n,actCategoryName=name)
for n,name in enumerate(['臺北市','高雄市','臺中市','臺南市'],1):insert('city',cityAddressID=n,cityName=name)
users=[('yuchen','林予晨','予晨','yuchen'),('yian','周以安','以安','yian'),('lin_yu','沈映禾','映禾','yinghe'),('slow_days','許知夏','知夏','zhixia'),('after_rain','陳慕晴','雨後的晴','muqing'),('sound_notes','吳言舟','聲音筆記','yanzhou'),('paper_walk','江若寧','紙上散步','ruoning'),('night_window','方以辰','窗邊的人','yichen')]
for n,(account,name,nick,email) in enumerate(users,1):
 salt=secrets.token_bytes(16);digest=hashlib.pbkdf2_hmac('sha256',b'NewView-local-2026!',salt,210000);pw='pbkdf2$210000$'+base64.b64encode(salt).decode()+'$'+base64.b64encode(digest).decode()
 insert('user',userID=n,userAccount=account,userPassword=pw,userName=name,userNickname=nick,userEmail=email+'@example.invalid',userBirth='1996-04-18',buyAuthority=0,speakAuthority=0)
for n,name in enumerate(['松間聲音社','折頁劇團','留白舞集','紙上漫遊'],1):insert('Publisher',pubID=n,pubName=name,pubEmail='hello'+str(n)+'@example.invalid')
activities=[
 ('暮色留聲｜河岸爵士夜', '讓薩克斯風與鋼琴，陪你把週末的步調慢下來。','2026-11-14',800,1,3,1,'河岸小廳','19:30:00','以一首熟悉的旋律開場，從老派搖擺到輕盈的即興，四位樂手將秋夜寫成一段流動的故事。\n\n演出長度約 90 分鐘，含中場休息。19:00 開放入場，請依票面座位入座。\n\n建議提早抵達，將手機調為靜音，讓今晚只留下音樂。'),
 ('慢一點的晚餐｜城市小劇場','三個室友、一張餐桌，和那些還沒說出口的話。','2026-11-21',600,2,6,2,'折頁黑盒子','19:00:00','下班後的廚房裡，三個人努力把生活煮成自己喜歡的樣子。一場關於陪伴、選擇與重新開始的溫柔喜劇。\n\n演出約 100 分鐘，無中場休息。18:30 開放入場，請依票面座位入座。\n\n遲到觀眾請依現場引導於適當段落入場。'),
 ('走進光裡｜當代舞蹈選粹','用身體描繪邊界，讓光替未完成的話留白。','2026-11-28',700,3,7,3,'留白實驗劇場','14:30:00','三段作品從呼吸出發，穿過記憶、城市與人群，在每一次停頓之間尋找新的相遇。\n\n演出約 80 分鐘，含演後短談。14:00 開放入場。\n\n本演出包含光影變化，請依個人感受斟酌參與。'),
 ('日常失物招領｜週五脫口秀','把日常裡的小尷尬，交給今晚的笑聲。','2026-11-06',450,2,1,1,'轉角喜劇小屋','20:00:00','有人忘了回訊息，有人忘了帶傘，有人只是想暫時忘記待辦清單。四段全新喜劇段子，陪你把這週好好收尾。\n\n演出約 90 分鐘，請依票面座位入座。19:30 開放入場。\n\n請勿錄音錄影，保留第一次聽見笑點的驚喜。'),
 ('聲音的形狀｜獨立音樂現場','兩組樂團、一個夜晚，把心裡的節奏大聲唱出來。','2026-12-05',980,1,5,2,'浪潮音樂空間','19:00:00','從輕盈的吉他聲到層層堆疊的合成器，兩組獨立樂團帶來新作與不插電特別段落。\n\n18:30 開放入場，演出約 120 分鐘。全場站席，請依現場工作人員引導。\n\n一人一票，請保管隨身物品。'),
 ('把日子寫成詩｜創作講座','從一張車票、一個午後，找到日常裡的故事。','2026-12-12',350,4,2,4,'紙間書房','14:00:00','不需要先成為作家，也可以留下自己的句子。透過三個短練習，練習觀察、刪減與命名。\n\n講座與練習約 120 分鐘，13:30 開放入場。請攜帶慣用的筆記工具。\n\n活動後保留 20 分鐘自由交流，歡迎分享，也可以只是安靜聆聽。'),
 ('雨停以前｜室內樂午後','弦樂與窗邊的微光，為午後留一點安靜。','2026-10-11',650,1,3,1,'木窗音樂沙龍','14:30:00','從古典小品到為本次演出編寫的短曲，弦樂四重奏與觀眾一起，聆聽雨聲之間的空白。\n\n演出約 75 分鐘，自由入座。\n\n每一段曲目之間，樂手將分享樂曲背後的小故事。'),
 ('寫給明天的信｜朗讀音樂會','讓一封沒有地址的信，在文字與旋律間抵達。','2026-10-08',480,4,3,3,'拾光藝文廳','19:30:00','由朗讀、鋼琴與環境聲組成的夜晚。四封不同年代的虛構書信，連起相遇、離別與期待。\n\n演出約 85 分鐘，自由入座。\n\n離場前可以帶走一張空白信紙，把想說的話留給明天。')]
for n,(name,intro,date,price,pub,category,city,venue,clock,content) in enumerate(activities,1):
 insert('act',actID=n,actName=name,actIntroduce=intro,actDate=date,actPrice=price,pubID=pub,actCategoryID=category,cityAddressID=city,cityAddress=venue,time=clock,actContent=content,approvalCondition=1)
 insert('actpic',actID=n,actPic=(ROOT/f'app/src/main/webapp/assets/portfolio/poster-{n}.png').read_bytes())
for act,scope in ACTIVITY_VENUES.items():
 for seat in seats(scope):insert('local_seat_layout',actID=act,seatCode=seat)
posts=[
 ('散場之後，還想再聽一次那段旋律',2,1,'一直覺得小型演出最迷人的，是能聽見樂器之間很細的對話。那天離開木窗時雨剛好停了，回家路上還在想第二首曲子的開頭。有人也有一首捨不得讓它結束的曲子嗎？',7),
 ('一個人看戲，也可以是很好的週末',3,2,'以前總等朋友有空才安排看戲，最近開始試著自己買票。能照自己的步調提早到場，演後也不用急著討論，反而多了一點和故事獨處的時間。',2),
 ('第一次看當代舞，應該從哪裡開始？',4,3,'最近想把原本只看戲劇的習慣往外推一點。比起先弄懂每個動作的意思，我更想知道大家會怎麼準備、怎麼觀察一場舞作。歡迎推薦適合入門的觀看方式。',3),
 ('想找一起聽爵士的同好',1,4,'準備去聽河岸那場爵士夜，想找喜歡小編制演出的朋友一起。可以各自買票，演出前在附近喝杯咖啡，散場後交換最喜歡的段落。',1),
 ('喜劇演出最喜歡坐前排還是後排？',5,5,'前排很有參與感，後排可以好好觀察整個房間的反應，兩種都很有趣。最近開始期待週五的演出，想聽聽大家的小習慣。',4),
 ('把看演出的票根，變成一本小冊子',1,6,'今年試著每看一場就在筆記本留一頁，貼票根、寫日期，再留下三句當時最想記住的話。現在翻起來，比照片更容易想起那天的心情。',6),
 ('不會寫詩，也能參加創作講座嗎？',7,7,'看到紙間書房的活動很心動，但平常只寫零散的日記。其實最想學的是把一件很小的事看仔細一點，如果你也有類似的經驗，想聽聽你的分享。',6),
 ('那封信，讓我想起很久沒聯絡的朋友',2,8,'朗讀和鋼琴交錯的段落很安靜，卻一直留在心裡。離場時拿了一張信紙，回家寫下第一句才發現，有些話不是不想說，只是不知道怎麼開始。',8),
 ('你會為了場館，特別去一場活動嗎？',1,3,'有些空間的燈光、座椅和散場路線，本身就像演出的一部分。我很喜歡有窗的小廳，等候入場的十幾分鐘也會變成很舒服的記憶。',7),
 ('耳塞、水和舒服的鞋：我的現場小清單',6,4,'站席演出除了期待歌單，我也會先準備好耳塞和水，選一雙能站久一點的鞋。想把這篇留給第一次進音樂現場的朋友，也歡迎補充。',5),
 ('演後座談，讓故事多了一個角度',3,5,'有時候最喜歡的不是得到一個標準答案，而是發現台上的人也正在提問。聽到創作過程裡那些小小的取捨，會更想重新回味剛才的畫面。',2),
 ('最近的藝文願望清單',1,6,'想看一場沒有先讀介紹的舞作、聽一次不熟悉曲目的音樂會，再給自己一個下午逛書店。今年剩下的時間不長，但好像還能安排幾次小小的出走。',3),
 ('什麼樣的笑點，會讓你回家後還想笑？',5,7,'我最喜歡那種很日常、當下聽完卻突然換了個角度的段子。比起大聲熱鬧，有時候一句很輕的觀察，就能把整個星期的緊繃放掉。',4),
 ('週末講座後，要不要交換一段文字？',7,8,'打算參加寫作練習，想找幾個願意交換短文的人。不評論好不好，只說哪一句讓自己停了一下。也可以只帶一個還沒完成的開頭。',6),
 ('音樂會前的半小時，你通常怎麼度過？',2,2,'我會提早抵達，先在附近慢慢走一圈，再把手機收起來。有時候從日常切換到演出的那段空白，就是整個夜晚很重要的前奏。',1),
 ('一起把城市的藝文角落記下來',1,1,'不一定是知名的大場館，也可能是一個讓人願意停下來的小書房。想慢慢收集那些交通方便、氣氛舒服、值得再回去的空間。你心裡有哪個口袋名單？',8),
 ('演出結束後，我想保留一點安靜',4,4,'以前散場就會立刻打開手機記錄，最近試著先不下結論。讓幾個畫面留在腦中，隔天再寫，反而更知道自己真正記住了什麼。',3),
 ('每一次收藏，都是對下個週末的期待',1,5,'打開收藏清單的時候，常常覺得生活忽然多了幾個小小的光點。不一定每場都會去，但知道還有喜歡的事情在等，就已經很好。',1)]
comments=['我也很喜歡這種不用急著下結論的感覺。','謝謝分享，已經放進下一次的清單了。','讀完忽然很想安排一個這樣的週末。','這個角度好有意思，原本沒特別注意過。','一個人去也很自在，最近才慢慢體會到。','可以先從自己喜歡的片段開始，不一定要全懂。']
for n,(title,category,user,content,pic) in enumerate(posts,1):
 timestamp=f'2026-10-{14+(n//5):02d} {8+(n%5):02d}:20:00';insert('post',postID=n,postHeader=title,postContent=content,postCategoryID=category,userID=user,postStatus=1,postDateTime=timestamp,lastEditedTime=timestamp,likeCount=0,disLikeCount=0)
 insert('postpic',postID=n,postpic=(ROOT/f'app/src/main/webapp/assets/portfolio/poster-{pic}.png').read_bytes())
 for k in range(1,2+n%3):insert('postmessage',postID=n,userID=(user+k)%8+1,mesContent=comments[(n+k)%len(comments)],messageDate=timestamp)
 for k in range(1,2+n%5):insert('likes',postID=n,userID=(user+k)%8+1,likeOrNot=1)
statements.append('UPDATE post p SET likeCount=(SELECT COUNT(*) FROM likes l WHERE l.postID=p.postID AND likeOrNot=1),disLikeCount=0;')
for user,act in [(1,1),(1,4),(1,6),(2,3),(2,5),(3,2)]:insert('myLike',userID=user,actID=act)
for user,pub,title,amount,start,end in [(1,1,'秋日同行｜音樂季折抵 100 元',100,'2026-10-01','2027-01-01'),(1,2,'新朋友見面禮｜劇場折抵 80 元',80,'2026-10-01','2027-01-01'),(1,None,'夏末回饋｜折抵 50 元',50,'2026-08-01','2026-10-01'),(2,3,'留白之間｜舞蹈演出折抵 100 元',100,'2026-10-01','2027-01-01')]:
 insert('discount',pubID=pub,discountContent=title,disAmount=amount,discountCode='NV-'+secrets.token_hex(4).upper(),disStartDate=start,disFinishDate=end);statements.append('SET @coupon=LAST_INSERT_ID();');statements.append(f'INSERT INTO useDiscount(userID,discountNO,ditUsed) VALUES({user},@coupon,0);')
statements.append('COMMIT;');payload='\n'.join(statements);(archive/'replacement.sql').write_text(payload)
p=subprocess.run(['docker','exec','-i','newview-ticketing-mysql','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --default-character-set=utf8mb4 -u"$MYSQL_USER" newview_recovery'],input=payload,text=True,capture_output=True)
if p.returncode:raise SystemExit('Seed failed; transaction rolled back. '+p.stderr[:500])
for file in archive.iterdir():
 if file.is_file():file.chmod(0o600)
# Delete only this site's now-obsolete temporary hold namespace, never other Redis data.
p=subprocess.run(['docker','exec','newview-ticketing-redis','redis-cli','--scan','--pattern','nv:recovery:seat:*'],capture_output=True,text=True,check=True)
for key in p.stdout.splitlines():subprocess.run(['docker','exec','newview-ticketing-redis','redis-cli','DEL',key],capture_output=True,check=True)
(STATE/'portfolio-seed.json').write_text(json.dumps(dict(backup=str(archive),users=8,activities=8,posts=18,artwork='assets/portfolio',fictional=True),ensure_ascii=False,indent=2));print('Fictional portfolio data installed; complete private backup:',archive)
