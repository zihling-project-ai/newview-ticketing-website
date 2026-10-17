#!/usr/bin/env python3
"""Synthetic seat races, real Redis expiration, compensation and durable mock allocation."""
from pathlib import Path
import time,json,subprocess,concurrent.futures,urllib.request,urllib.parse,urllib.error
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_cart.py').read_text().split('try:\n a=register')[0])
prefix='seats_'+str(time.time_ns());accounts=[prefix+'a',prefix+'b'];ids=[];activities=[];publishers=[];trigger='seat_fault_'+str(time.time_ns());act=None

def redis(*args):
 p=subprocess.run(['docker','exec','newview-ticketing-redis','redis-cli','--raw',*map(str,args)],text=True,capture_output=True,check=True);return p.stdout.strip()
def seatkey(seat):return f'nv:recovery:seat:{act}:{seat}'
def hold(c,*seats,expected=200):return c.call('Seats',dict(action='hold',actID=act,seats=','.join(seats)),expected=expected)
def status(c,seat):return next(x for x in c.call('Seats?actID='+str(act))['seats'] if x['seat']==seat)['state']
def rawhold(c):
 req=urllib.request.Request(BASE+'Seats',data=urllib.parse.urlencode(dict(action='hold',actID=act,seats='A01')).encode(),headers={'X-CSRF-Token':c.csrf})
 try:
  with c.opener.open(req,timeout=20) as response:return response.status
 except urllib.error.HTTPError as e:return e.code
try:
 a=register(accounts[0]);b=register(accounts[1]);anon=Client()
 act=int(sql(f"INSERT INTO act(actName,actPrice) VALUES('{prefix}',500);SELECT LAST_INSERT_ID();"));activities=[act]
 sql('INSERT INTO local_seat_layout(actID,seatCode) VALUES '+','.join(f"({act},'A0{i}')" for i in range(1,7))+';')
 check('Anonymous may read synthetic seat layout',lambda:must(len(anon.call('Seats?actID='+str(act))['seats'])==6))
 check('Seat map reveals no member IDs',lambda:must('userID' not in json.dumps(anon.call('Seats?actID='+str(act)))))
 check('Anonymous cannot hold seats',lambda:hold(anon,'A01',expected=401))
 check('Hold requires CSRF',lambda:a.call('Seats',dict(action='hold',actID=act,seats='A01'),expected=403,csrf=False))
 check('Unknown seat rejected',lambda:hold(a,'Z99',expected=400))
 check('Duplicate seats rejected',lambda:hold(a,'A01','A01',expected=400))
 check('Empty selection rejected',lambda:hold(a,expected=400))
 check('Invalid activity rejected',lambda:a.call('Seats?actID=abc',expected=400))
 check('Missing layout is 404',lambda:a.call('Seats?actID=2147483647',expected=404))
 with concurrent.futures.ThreadPoolExecutor(2) as pool:races=list(pool.map(rawhold,[a,b]))
 check('Two members racing one seat yield one winner',lambda:must(sorted(races)==[200,409]))
 winner=a if races[0]==200 else b;loser=b if winner is a else a
 check('Winner sees own hold',lambda:must(status(winner,'A01')=='mine'))
 check('Loser sees another hold',lambda:must(status(loser,'A01')=='held'))
 check('Redis hold has actual ten-minute TTL',lambda:must(590000<=int(redis('PTTL',seatkey('A01')))<=600000))
 check('GET map does not renew TTL',lambda:must(int(redis('PTTL',seatkey('A01')))<600000 and status(winner,'A01')=='mine'))
 check('GET cannot mutate selected seats',lambda:must(status(loser,'A01')=='held' and loser.call(f'Seats?actID={act}&action=release')['seats']))
 check('Removing cart releases own seat',lambda:modify(winner,'remove',act))
 check('Released seat available again',lambda:must(status(loser,'A01')=='available'))
 check('Multiple seats held atomically',lambda:hold(a,'A01','A02'))
 check('Partial conflict does not lock unclaimed seat',lambda:hold(b,'A02','A03',expected=409))
 check('Uncontested seat remains free after conflict',lambda:must(status(b,'A03')=='available'))
 check('Replacing selection succeeds',lambda:hold(a,'A02','A03'))
 check('Dropped old seat released',lambda:must(status(b,'A01')=='available'))
 cart=a.call('Cart');check('Cart quantity matches selected seat count',lambda:must(cart['items'][0]['quantity']==2 and cart['items'][0]['seats']==['A02','A03'] and cart['valid']))
 check('Quantity update cannot bypass seat selection',lambda:a.call('Cart',dict(action='update',actID=act,quantity=1),expected=409))
 check('Generic add cannot bypass seat selection',lambda:a.call('Cart',dict(action='add',actID=act,quantity=1),expected=409))
 redis('PEXPIRE',seatkey('A02'),150);redis('PEXPIRE',seatkey('A03'),150);time.sleep(.25)
 check('Real Redis expiration releases seats',lambda:must(status(b,'A02')=='available'))
 check('Expired cart marked invalid',lambda:must(not a.call('Cart')['valid']))
 check('Expired seats cannot checkout',lambda:checkout(a,key(a),expected=409))
 check('Other member can acquire expired seat',lambda:hold(b,'A02'))
 check('Old owner release cannot delete newer owners hold',lambda:a.call('Seats',dict(action='release',actID=act)))
 check('New owner lock remains',lambda:must(status(b,'A02')=='mine'))
 b.call('Seats',dict(action='release',actID=act));hold(a,'A01')
 previous=redis('GET',seatkey('A01'));initial_ttl=int(redis('PTTL',seatkey('A01')))
 trigger_sql(f"DELIMITER //\nCREATE TRIGGER {trigger} BEFORE INSERT ON local_cart_seat FOR EACH ROW BEGIN IF NEW.actID={act} AND NEW.seatCode='A03' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic seat write rollback'; END IF; END//\nDELIMITER ;\n")
 check('Injected cart seat write failure rejects selection',lambda:hold(a,'A01','A03',expected=500))
 check('Failed selection restores old token without extending TTL',lambda:must(redis('GET',seatkey('A01'))==previous and int(redis('PTTL',seatkey('A01')))<=initial_ttl))
 check('Failed selection releases newly claimed seat',lambda:must(redis('EXISTS',seatkey('A03'))=='0'))
 check('Failed selection preserves database cart',lambda:must(a.call('Cart')['items'][0]['seats']==['A01']))
 trigger_sql('DROP TRIGGER '+trigger+';')
 trigger_sql(f"DELIMITER //\nCREATE TRIGGER {trigger} BEFORE INSERT ON orderlist FOR EACH ROW BEGIN IF NEW.actID={act} THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic seated checkout rollback'; END IF; END//\nDELIMITER ;\n")
 check('Failed seated checkout returns error',lambda:checkout(a,key(a),expected=500))
 check('Failed checkout retains original temporary hold',lambda:must(redis('GET',seatkey('A01'))==previous and counts()==0 and int(sql(f'SELECT COUNT(*) FROM local_seat_booking WHERE actID={act};'))==0))
 trigger_sql('DROP TRIGGER '+trigger+';');result=checkout(a,key(a));order=result['orderIDs'][0]
 check('Successful mock checkout records assigned seat',lambda:must(a.call('MemberOrders?orderID='+str(order))[0]['seat']=='A01'))
 check('Successful checkout clears cart and hold',lambda:must(a.call('Cart')['items']==[] and redis('EXISTS',seatkey('A01'))=='0'))
 check('Allocated seat stays blocked after Redis hold removed',lambda:must(status(b,'A01')=='booked'))
 check('Another member cannot hold booked seat',lambda:hold(b,'A01',expected=409))
 hold(b,'A04');pending_key=key(b)
 subprocess.run(['docker','stop','newview-ticketing-redis'],check=True,stdout=subprocess.DEVNULL)
 check('Redis outage fails closed for seat reads',lambda:a.call('Seats?actID='+str(act),expected=503))
 check('Redis outage prevents checkout without losing cart',lambda:checkout(b,pending_key,expected=503))
 subprocess.run(['docker','start','newview-ticketing-redis'],check=True,stdout=subprocess.DEVNULL)
 # Old pooled connections may fail once after forced restart; wait for fresh resources.
 for attempt in range(4):
  try:state=status(b,'A01');break
  except AssertionError:
   if attempt==3:raise
 check('Database allocations survive Redis restart',lambda:must(state=='booked'))
 check('Restart drops only ephemeral pending holds',lambda:must(status(a,'A04')=='available' and not b.call('Cart')['valid']))
 check('Expired cart can be reselected after restart',lambda:hold(b,'A04'))
 check('Reselection repairs cart validity',lambda:must(b.call('Cart')['valid']))
 check('Explicit release clears own selected cart',lambda:b.call('Seats',dict(action='release',actID=act)))
 check('Released cart is empty',lambda:must(b.call('Cart')['items']==[]))
finally:
 subprocess.run(['docker','start','newview-ticketing-redis'],stdout=subprocess.DEVNULL,check=True)
 trigger_sql('DROP TRIGGER IF EXISTS '+trigger+';')
 if act:
  for i in range(1,7):redis('DEL',seatkey('A0'+str(i)))
  sql(f'DELETE FROM local_seat_booking WHERE actID={act};DELETE FROM local_cart_seat WHERE actID={act};')
 if ids:
  users=','.join(map(str,ids));sql(f'DELETE m FROM local_simulation_order m JOIN orders o ON o.orderID=m.orderID WHERE o.userID IN ({users});DELETE l FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM orders WHERE userID IN ({users});DELETE FROM local_checkout_receipt WHERE userID IN ({users});DELETE FROM cartact WHERE userID IN ({users});DELETE FROM user WHERE userID IN ({users});')
 if act:sql(f'DELETE FROM local_seat_layout WHERE actID={act};DELETE FROM act WHERE actID={act};')
 (ROOT/'docs/seat-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Seat checks passed:',len(results))
