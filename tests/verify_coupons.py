#!/usr/bin/env python3
"""Dedicated private synthetic coupon transactions; never touches preserved demos."""
from pathlib import Path
import time,json,concurrent.futures
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_tickets.py').read_text().split('try:\n a=register')[0])
prefix='coupon_'+str(time.time_ns());accounts=[prefix+'a',prefix+'b'];ids=[];activities=[];publishers=[];discounts=[];trigger='coupon_fault_'+str(time.time_ns());act=None

def coupon(owner,amount=30,pub=None,start='DATE_SUB(NOW(),INTERVAL 1 DAY)',end='DATE_ADD(NOW(),INTERVAL 1 DAY)'):
 d=int(sql(f"INSERT INTO discount(discountContent,disAmount,disStartDate,disFinishDate,pubID) VALUES('{prefix}',{amount},{start},{end},{pub if pub else 'NULL'});SELECT LAST_INSERT_ID();"));discounts.append(d)
 u=int(sql(f'INSERT INTO useDiscount(userID,discountNO,ditUsed) VALUES({owner},{d},0);SELECT LAST_INSERT_ID();'));return u,d

def quote(c,u=0,expected=200):return c.call('Cart?useDisID='+str(u),expected=expected)
def buy(c,u=0,expected=201,k=None,**extra):return checkout(c,k or quote(c,u)['requestID'],expected=expected,useDisID=u,**extra)
def wallet(c,u):return next(x for x in c.call('MemberCoupons') if x['useDisID']==u)
def used(u):return int(sql(f'SELECT ditUsed FROM useDiscount WHERE useDisID={u};'))
def orderrow(o):return sql(f'SELECT CONCAT(ordTotal,",",discount,",",discountPrice) FROM orders WHERE orderID={o};')
try:
 a=register(accounts[0]);b=register(accounts[1]);anon=Client()
 for n in range(2):publishers.append(int(sql(f"INSERT INTO Publisher(pubName) VALUES('{prefix}{n}');SELECT LAST_INSERT_ID();")))
 for n in range(2):activities.append(int(sql(f"INSERT INTO act(actName,actPrice,pubID) VALUES('{prefix}{n}',100,{publishers[n]});SELECT LAST_INSERT_ID();")))
 act,other=activities;u,d=coupon(ids[0]);bu,bd=coupon(ids[1]);modify(a,'add',act,1);modify(b,'add',act,1)
 check('Wallet HTML reachable',lambda:anon.call('my-coupons.html'))
 check('Anonymous wallet rejected',lambda:anon.call('MemberCoupons',expected=401))
 check('Wallet mutation prohibited',lambda:a.call('MemberCoupons',dict(useDisID=u),expected=405))
 check('Wallet scoped to authenticated owner',lambda:must([x['useDisID'] for x in b.call('MemberCoupons?userID='+str(ids[0]))]==[bu]))
 check('Wallet contains no user or secret code fields',lambda:must(not {'userID','discountCode','userEmail'}&set(wallet(a,u))))
 check('Foreign assigned coupon preview rejected',lambda:quote(b,u,404))
 check('Malformed coupon rejected',lambda:quote(a,'abc',400))
 check('Negative coupon rejected',lambda:quote(a,-1,400))
 check('Missing assigned coupon rejected',lambda:quote(a,2147483647,404))
 check('Fixed reduction quoted from database',lambda:must(quote(a,u)['discount']==30 and quote(a,u)['payable']==70))
 check('Repeated previews never consume coupon',lambda:must(used(u)==0))
 k=quote(a,u)['requestID']
 check('Coupon checkout requires CSRF',lambda:a.call('LocalCheckout',dict(requestID=k,useDisID=u,confirmSimulation='true'),csrf=False,expected=403))
 check('Omitting selected coupon invalidates quote',lambda:checkout(a,k,expected=409))
 check('No consume on rejected checkout',lambda:must(used(u)==0))
 result=buy(a,u,k=k,discount=9999,discountPrice=1,total=1,userID=ids[1]);o=result['orderIDs'][0]
 check('Authoritative gross reduction and net stored',lambda:must(orderrow(o)=='100,30,70'))
 check('Coupon consumed exactly with order',lambda:must(used(u)==1 and int(sql(f'SELECT COUNT(*) FROM local_coupon_redemption WHERE orderID={o} AND useDisID={u};'))==1))
 check('Other member coupon unaffected',lambda:must(used(bu)==0))
 check('Same request replay returns same order',lambda:must(buy(a,u,k=k,expected=200)['orderIDs']==[o]))
 modify(a,'add',act,1)
 check('Used coupon cannot preview again',lambda:quote(a,u,409))
 check('Used coupon cannot checkout with new quote',lambda:buy(a,u,k=quote(a)['requestID'],expected=409))
 check('Other member cannot cancel coupon order',lambda:cancel(b,o,404))
 check('Foreign cancel cannot restore coupon',lambda:must(used(u)==1))
 check('Successful cancellation restores coupon',lambda:must(cancel(a,o)['couponRestored'] and used(u)==0))
 second=buy(a,u)['orderIDs'][0]
 check('Restored coupon can be reused',lambda:must(used(u)==1 and orderrow(second)=='100,30,70'))
 check('Repeat old cancellation never restores new use',lambda:must(cancel(a,o)['alreadyCancelled'] and used(u)==1))
 check('Old receipt replay after cancel does not consume again',lambda:must(buy(a,u,k=k,expected=200)['orderIDs']==[o] and used(u)==1))
 cancel(a,second)
 modify(a,'add',act,1);k=quote(a,u)['requestID'];sql(f'UPDATE discount SET disAmount=40 WHERE discountNO={d};')
 check('Changed coupon amount invalidates quote',lambda:buy(a,u,k=k,expected=409))
 check('Amount change rejection preserves coupon and cart',lambda:must(used(u)==0 and len(quote(a)['items'])==1))
 sql(f'UPDATE discount SET disAmount=30 WHERE discountNO={d};')
 k=quote(a,u)['requestID'];sql(f'UPDATE discount SET disFinishDate=DATE_SUB(NOW(),INTERVAL 1 SECOND) WHERE discountNO={d};')
 check('Expiry between preview and checkout rejected',lambda:buy(a,u,k=k,expected=409))
 sql(f'UPDATE discount SET disFinishDate=DATE_ADD(NOW(),INTERVAL 1 DAY) WHERE discountNO={d};')
 for label,kw in [('Future',dict(start='DATE_ADD(NOW(),INTERVAL 1 DAY)')),('Expired',dict(end='DATE_SUB(NOW(),INTERVAL 1 DAY)')),('Unknown date',dict(end='NULL')),('Zero amount',dict(amount=0)),('Negative amount',dict(amount=-1))]:
  bad,_=coupon(ids[0],**kw);check(label+' coupon unavailable',lambda bad=bad:quote(a,bad,409))
 scoped,sd=coupon(ids[0],pub=publishers[1])
 check('Wrong publisher rejected',lambda:quote(a,scoped,409))
 modify(a,'add',other,2)
 check('Global coupon rejects ambiguous multi-publisher checkout',lambda:quote(a,u,409))
 check('Scoped coupon only reduces its publisher group',lambda:must(quote(a,scoped)['payable']==270))
 rr=buy(a,scoped)['orderIDs'];rowlist=[orderrow(x) for x in rr]
 check('Split order reduction applied once to matching publisher',lambda:must(sorted(rowlist)==['100,0,100','200,30,170']))
 for x in rr:cancel(a,x)
 huge,hd=coupon(ids[0],amount=999);modify(a,'add',act,1)
 check('Reduction capped to total and net cannot go negative',lambda:must(quote(a,huge)['discount']==100 and quote(a,huge)['payable']==0))
 ho=buy(a,huge)['orderIDs'][0]
 check('Zero net amount persisted correctly',lambda:must(orderrow(ho)=='100,100,0'))
 cancel(a,ho)
 modify(a,'add',act,1);k=quote(a,u)['requestID']
 trigger_sql(f"DELIMITER //\nCREATE TRIGGER {trigger} BEFORE INSERT ON orderlist FOR EACH ROW BEGIN IF NEW.actID={act} THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic coupon checkout rollback'; END IF; END//\nDELIMITER ;\n")
 check('Injected checkout failure rejected',lambda:buy(a,u,k=k,expected=500))
 check('Failed checkout rolls coupon and ledger back',lambda:must(used(u)==0 and int(sql(f'SELECT COUNT(*) FROM local_coupon_redemption WHERE useDisID={u};'))==0 and len(quote(a)['items'])==1))
 trigger_sql('DROP TRIGGER '+trigger+';')
 k=quote(a,u)['requestID']
 with concurrent.futures.ThreadPoolExecutor(2) as pool:race=list(pool.map(lambda _:raw(a,'LocalCheckout',dict(requestID=k,useDisID=u,confirmSimulation='true')),range(2)))
 check('Concurrent same request creates once and replays once',lambda:must(sorted(x[0] for x in race)==[200,201]))
 co=a.call('LocalCheckout?requestID='+k)['orderIDs'][0]
 check('Concurrent checkout has single coupon redemption',lambda:must(used(u)==1 and int(sql(f'SELECT COUNT(*) FROM local_coupon_redemption WHERE useDisID={u};'))==1))
 trigger_sql(f"DELIMITER //\nCREATE TRIGGER {trigger} BEFORE UPDATE ON useDiscount FOR EACH ROW BEGIN IF NEW.useDisID={u} AND NEW.ditUsed=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic coupon restore rollback'; END IF; END//\nDELIMITER ;\n")
 check('Coupon restoration failure rejects cancellation',lambda:cancel(a,co,500))
 check('Failed restore preserves order and used coupon',lambda:must(used(u)==1 and int(sql(f'SELECT ordType FROM orders WHERE orderID={co};'))==3))
 trigger_sql('DROP TRIGGER '+trigger+';')
 sql(f'UPDATE discount SET disFinishDate=DATE_SUB(NOW(),INTERVAL 1 SECOND) WHERE discountNO={d};');cancel(a,co)
 check('Cancel restores expired coupon without renewing validity',lambda:must(used(u)==0 and not wallet(a,u)['available']))
 modify(a,'add',act,1)
 du,dd=coupon(ids[0]);sql(f'INSERT INTO useDiscount(userID,discountNO,ditUsed) VALUES({ids[0]},{dd},0);')
 check('Duplicate historical assignment blocked conservatively',lambda:quote(a,du,409))
 # Distinct sessions and distinct checkout keys cannot reuse the same assigned coupon.
 race_coupon,race_discount=coupon(ids[0]);peer=Client();peer.csrf=peer.call('SignIn',dict(account=accounts[0],password=password))['csrf'];k1=quote(a,race_coupon)['requestID'];k2=quote(peer,race_coupon)['requestID']
 with concurrent.futures.ThreadPoolExecutor(2) as pool:
  tasks=[pool.submit(raw,c,'LocalCheckout',dict(requestID=k,useDisID=race_coupon,confirmSimulation='true')) for c,k in [(a,k1),(peer,k2)]];races=[task.result() for task in tasks]
 check('Different session keys cannot double consume coupon',lambda:must(sorted(x[0] for x in races)==[201,400]))
 check('Distinct key race leaves one active redemption',lambda:must(int(sql(f'SELECT COUNT(*) FROM local_coupon_redemption WHERE useDisID={race_coupon};'))==1))
 ro=int(sql(f'SELECT orderID FROM local_coupon_redemption WHERE useDisID={race_coupon};'));cancel(a,ro);modify(a,'add',act,1)
 check('Restored expired coupon still rejected at preview',lambda:quote(a,u,409))
 check('Copied assignment cannot be submitted by another member',lambda:buy(b,race_coupon,k=quote(b)['requestID'],expected=404))
 # Redeemed order cannot be cancelled to reclaim its coupon.
 fresh,fd=coupon(ids[0]);used_order=buy(a,fresh)['orderIDs'][0];t=a.call('MemberOrders?orderID='+str(used_order))[0]['orderListID'];redeem(a,payload(a,t))
 check('Used ticket order cannot reclaim coupon',lambda:cancel(a,used_order,409))
 check('Coupon remains used after prohibited cancellation',lambda:must(used(fresh)==1))
 # Same publisher, different activities; another publisher; mixed seated and quantity tickets.
 third=int(sql(f"INSERT INTO act(actName,actPrice,pubID) VALUES('{prefix}_same_publisher',150,{publishers[0]});SELECT LAST_INSERT_ID();"));activities.append(third)
 sql(f"INSERT INTO local_seat_layout(actID,seatCode) VALUES({act},'S01'),({act},'S02');")
 a.call('Seats',dict(action='hold',actID=act,seats='S01,S02'));modify(a,'add',third,1);modify(a,'add',other,3)
 group_coupon,group_discount=coupon(ids[0],pub=publishers[0]);q=quote(a,group_coupon)
 check('Mixed cart keeps three separate activity lines',lambda:must(len(q['items'])==3 and q['total']==650 and q['payable']==620))
 k=q['requestID']
 trigger_sql(f"DELIMITER //\nCREATE TRIGGER {trigger} BEFORE INSERT ON orders FOR EACH ROW BEGIN IF NEW.userID={ids[0]} AND NEW.pubID={publishers[1]} THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic second publisher rollback'; END IF; END//\nDELIMITER ;\n")
 check('Failure in second publisher aborts whole checkout',lambda:buy(a,group_coupon,k=k,expected=500))
 check('Multi-publisher failure preserves unused coupon and all cart lines',lambda:must(used(group_coupon)==0 and len(quote(a)['items'])==3))
 check('Multi-publisher rollback removes first publisher seat booking',lambda:must(int(sql(f'SELECT COUNT(*) FROM local_seat_booking WHERE actID={act};'))==0))
 check('Failed checkout keeps owned Redis holds',lambda:must(all(x['state']=='mine' for x in a.call('Seats?actID='+str(act))['seats'])))
 trigger_sql('DROP TRIGGER '+trigger+';');group_orders=buy(a,group_coupon)['orderIDs']
 grouped={int(sql(f'SELECT pubID FROM orders WHERE orderID={oid};')):oid for oid in group_orders};ao=grouped[publishers[0]];bo=grouped[publishers[1]]
 check('Same publisher activities merge, different publisher splits',lambda:must(len(group_orders)==2 and orderrow(ao)=='350,30,320' and orderrow(bo)=='300,0,300'))
 check('Merged order retains both activity identities and quantities',lambda:must(sorted((x['actID'],sum(y['actID']==x['actID'] for y in a.call('MemberOrders?orderID='+str(ao)))) for x in {x['actID']:x for x in a.call('MemberOrders?orderID='+str(ao))}.values())==sorted([(act,2),(third,1)])))
 check('Each publisher has correct ticket count',lambda:must(len(a.call('MemberOrders?orderID='+str(ao)))==3 and len(a.call('MemberOrders?orderID='+str(bo)))==3))
 check('Two selected seats persisted on merged order',lambda:must(int(sql(f'SELECT COUNT(*) FROM local_seat_booking WHERE actID={act};'))==2))
 check('Cancelling eligible publisher restores coupon and its seats',lambda:must(cancel(a,ao)['couponRestored'] and used(group_coupon)==0 and int(sql(f'SELECT COUNT(*) FROM local_seat_booking WHERE actID={act};'))==0))
 check('Cancelling one publisher leaves other order usable',lambda:must(int(sql(f'SELECT ordType FROM orders WHERE orderID={bo};'))==3 and all(x['usedType']==0 for x in a.call('MemberOrders?orderID='+str(bo)))))
 check('Other publisher cancel does not restore coupon twice',lambda:must(not cancel(a,bo)['couponRestored'] and used(group_coupon)==0))
finally:
 trigger_sql('DROP TRIGGER IF EXISTS '+trigger+';')
 if ids:
  users=','.join(map(str,ids));sql(f'DELETE b FROM local_seat_booking b JOIN orderlist l ON l.orderListID=b.orderListID JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM local_cart_seat WHERE userID IN ({users});DELETE r FROM local_coupon_redemption r JOIN orders o ON o.orderID=r.orderID WHERE o.userID IN ({users});DELETE m FROM local_simulation_order m JOIN orders o ON o.orderID=m.orderID WHERE o.userID IN ({users});DELETE l FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM orders WHERE userID IN ({users});DELETE FROM local_checkout_receipt WHERE userID IN ({users});DELETE FROM cartact WHERE userID IN ({users});DELETE FROM useDiscount WHERE userID IN ({users});DELETE FROM user WHERE userID IN ({users});')
 if discounts:sql('DELETE FROM discount WHERE discountNO IN ('+','.join(map(str,discounts))+');')
 if act:
  for seat in ['S01','S02']:subprocess.run(['docker','exec','newview-ticketing-redis','redis-cli','DEL',f'nv:recovery:seat:{act}:{seat}'],capture_output=True)
 if activities:sql('DELETE FROM local_seat_layout WHERE actID IN ('+','.join(map(str,activities))+');DELETE FROM act WHERE actID IN ('+','.join(map(str,activities))+');')
 if publishers:sql('DELETE FROM Publisher WHERE pubID IN ('+','.join(map(str,publishers))+');')
 (ROOT/'docs/coupon-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Coupon checks passed:',len(results))
