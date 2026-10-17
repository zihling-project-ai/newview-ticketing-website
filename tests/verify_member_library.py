#!/usr/bin/env python3
"""Scoped order reads and favorite lifecycle using owned synthetic fixtures."""
from pathlib import Path
import time,json,subprocess,concurrent.futures,tempfile,os
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
STATE=Path(__import__('os').environ.get('NEWVIEW_STATE_DIR',str(ROOT.parent/'.newview-ticketing-local')))
exec((ROOT/'tests/verify_social.py').read_text().split('anonymous=Client()')[0])
prefix='library_'+str(time.time_ns());accounts=[prefix+'a',prefix+'b'];password='Library-local-2026!';ids=[];act=None

def sql(query):
 p=subprocess.run(['docker','exec','-i','newview-ticketing-mysql','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -N -B -u"$MYSQL_USER" --default-character-set=utf8mb4 "$MYSQL_DATABASE"'],input=query,text=True,capture_output=True)
 if p.returncode:raise RuntimeError(p.stderr)
 return p.stdout.strip()
def must(value):assert value

def register(account):
 c=Client();c.csrf=c.call('RecoverySession')['csrf'];data=dict(account=account,password=password,confirmPassword=password,name='查詢測試',nickname='合成會員',birthdate='2000-01-01',cellphone='0912345678',email=account+'@example.test');code=c.call('SignUp',data)['localCode'];c.call('SignUp',dict(action='verify',code=code),expected=201);c.csrf=c.call('SignIn',dict(account=account,password=password))['csrf'];ids.append(int(c.call('RecoverySession')['userID']));return c
try:
 a=register(accounts[0]);b=register(accounts[1]);anon=Client()
 check('Anonymous order list rejected',lambda:anon.call('MemberOrders',expected=401))
 check('Anonymous favorites rejected',lambda:anon.call('MemberFavorites',expected=401))
 check('Anonymous activity catalog rejected',lambda:anon.call('MemberActivities',expected=401))
 check('Anonymous favorite mutation rejected',lambda:anon.call('MemberFavorites',dict(action='add',actID=1),expected=401))
 check('New member orders empty',lambda:must(a.call('MemberOrders')==[]))
 check('New member favorites empty',lambda:must(a.call('MemberFavorites')==[]))
 act=int(sql("INSERT INTO act(actName,actIntroduce,actPrice) VALUES('"+prefix+"','<script>fixture text</script>',900); SELECT LAST_INSERT_ID();"))
 image=(ROOT/'tests/fixtures/sample.png').read_bytes()
 sql(f"INSERT INTO actpic(actID,actPic) VALUES({act},X'{image.hex()}');")
 order=int(sql(f"INSERT INTO orders(userID,ordTime,ordTotal,ordType,actQuantity) VALUES({ids[0]},'2026-10-17 09:00:00',1800,0,2); SELECT LAST_INSERT_ID();"))
 other=int(sql(f"INSERT INTO orders(userID,ordTime,ordTotal,ordType,actQuantity) VALUES({ids[1]},'2026-10-17 09:00:00',900,2,1); SELECT LAST_INSERT_ID();"))
 sql(f"INSERT INTO orderlist(orderID,actID,actTotal,usedType) VALUES({order},{act},900,0),({order},{act},900,1),({other},{act},900,0);")
 orders=a.call('MemberOrders?userID='+str(ids[1]))
 check('Forged owner ignored in order list',lambda:must([o['orderID'] for o in orders]==[order]))
 check('Stored order amount retained',lambda:must(orders[0]['ordTotal']==1800))
 check('Owner reads both ticket details',lambda:must(len(a.call('MemberOrders?orderID='+str(order)))==2))
 check('Other member cannot read order detail',lambda:b.call('MemberOrders?orderID='+str(order),expected=404))
 check('Missing order detail is 404',lambda:a.call('MemberOrders?orderID=2147483647',expected=404))
 check('Malformed order rejected',lambda:a.call('MemberOrders?orderID=abc',expected=400))
 check('POST cannot mutate order',lambda:a.call('MemberOrders',dict(orderID=order,action='cancel'),expected=405))
 check('GET cannot mutate favorites',lambda:a.call('likeChange?actID='+str(act),expected=405))
 check('Favorite requires CSRF',lambda:a.call('MemberFavorites',dict(action='add',actID=act),expected=403,csrf=False))
 check('Invalid favorite action rejected',lambda:a.call('MemberFavorites',dict(action='toggle',actID=act),expected=400))
 check('Missing activity rejected',lambda:a.call('MemberFavorites',dict(action='add',actID=2147483647),expected=404))
 check('Invalid activity ID rejected',lambda:a.call('MemberFavorites',dict(action='add',actID=-1),expected=400))
 check('Add favorite ignores forged userID',lambda:a.call('MemberFavorites',dict(action='add',actID=act,userID=ids[1])))
 check('Repeated add is idempotent',lambda:a.call('MemberFavorites',dict(action='add',actID=act)))
 check('Only one favorite exists',lambda:must(len(a.call('MemberFavorites'))==1))
 check('Other member favorites untouched',lambda:must(b.call('MemberFavorites?userID='+str(ids[0]))==[]))
 check('Other member cannot remove owner favorite',lambda:b.call('MemberFavorites',dict(action='remove',actID=act,userID=ids[0])))
 check('Owner favorite remains',lambda:must(len(a.call('MemberFavorites'))==1))
 check('Catalog indicates saved activity',lambda:must(next(x for x in a.call('MemberActivities') if x['actID']==act)['favorite']==1))
 check('Activity image returns normalized PNG',lambda:must(a.opener.open(BASE+'MemberActivityImage?actID='+str(act)).read().startswith(b'\x89PNG')))
 check('Missing activity image returns 404',lambda:a.call('MemberActivityImage?actID=2147483647',expected=404))
 check('Original favorite route supports explicit remove',lambda:a.call('likeChange',dict(action='remove',actID=act)))
 check('Repeated remove is idempotent',lambda:a.call('MemberFavorites',dict(action='remove',actID=act)))
 check('Favorites empty after removal',lambda:must(a.call('MemberFavorites')==[]))
 # Two independent logins race to add the same activity.
 second=Client();second.csrf=second.call('SignIn',dict(account=accounts[0],password=password))['csrf']
 with concurrent.futures.ThreadPoolExecutor(2) as pool:
  tasks=[pool.submit(c.call,'MemberFavorites',dict(action='add',actID=act)) for c in [a,second]]
  for task in tasks:task.result()
 check('Concurrent adds still produce one favorite',lambda:must(len(a.call('MemberFavorites'))==1))
 check('Favorite persists across re-login',lambda:must(second.call('MemberFavorites')[0]['actID']==act))
 summary=a.call('MemberPage')
 check('Member summary scopes orders and favorites',lambda:must(len(summary['orders'])==1 and len(summary['mylike'])==1))
 a.call('RecoverySession',{})
 check('Logout revokes order access',lambda:a.call('MemberOrders',expected=401))
 check('Logout revokes favorites access',lambda:a.call('MemberFavorites',expected=401))
 # Entity identity regression: distinct orders must survive a Hibernate-style Set.
 with tempfile.TemporaryDirectory() as tmp:
  test=Path(tmp)/'OrderIdentityCheck.java'
  test.write_text('import com.newview.orders.model.OrdersVO;\nimport java.util.HashSet;\npublic class OrderIdentityCheck {public static void main(String[] args){\nOrdersVO a=new OrdersVO();a.setOrderID(1);OrdersVO b=new OrdersVO();b.setOrderID(2);OrdersVO same=new OrdersVO();same.setOrderID(1);\nHashSet<OrdersVO> orders=new HashSet<>();orders.add(a);orders.add(b);orders.add(same);assert orders.size()==2;\nOrdersVO transientA=new OrdersVO(),transientB=new OrdersVO();assert !transientA.equals(transientB);int hash=transientA.hashCode();transientA.setOrderID(3);assert hash==transientA.hashCode();\n}}\n')
  cp=os.pathsep.join([str(ROOT/'app/target/classes')]+[str(p) for p in Path(os.environ.get('NEWVIEW_MAVEN_REPO',str(STATE/'m2'))).rglob('*.jar')])
  subprocess.run(['javac','-cp',cp,'-d',tmp,str(test)],check=True)
  check('Order identity preserves distinct orders without publisher',lambda:subprocess.run(['java','-ea','-cp',tmp+os.pathsep+cp,'OrderIdentityCheck'],check=True))
finally:
 if ids:
  clause=','.join(map(str,ids));sql(f"DELETE l FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({clause}); DELETE FROM orders WHERE userID IN ({clause});DELETE FROM myLike WHERE userID IN ({clause}); DELETE FROM user WHERE userID IN ({clause});")
 if act:sql(f'DELETE FROM actpic WHERE actID={act}; DELETE FROM act WHERE actID={act};')
 (ROOT/'docs/member-library-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Member library checks passed:',len(results))
