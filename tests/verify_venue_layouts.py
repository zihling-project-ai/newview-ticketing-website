#!/usr/bin/env python3
"""Fresh local fixtures verify historical venue sizes end-to-end without touching member orders."""
from pathlib import Path
import json,time
import sys
sys.path.insert(0,str(next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())/'scripts/setup'))
from venue_layouts import VENUES,seats
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_tickets.py').read_text().split('try:\n a=register')[0])
prefix='venue_'+str(time.time_ns());accounts=[prefix];ids=[];activities=[];publishers=[]
try:
 c=register(prefix);pub=int(sql("INSERT INTO Publisher(pubName) VALUES('"+prefix+"');SELECT LAST_INSERT_ID();"));publishers.append(pub)
 for scope,expected,selected,gap in [(1,100,['A01','J10'],None),(2,336,['A01','T20'],'G07'),(3,768,['A01','AD30'],'K10')]:
  act=int(sql(f"INSERT INTO act(actName,actDate,actPrice,pubID) VALUES('{prefix}{scope}',DATE_ADD(CURRENT_DATE,INTERVAL 30 DAY),500,{pub});SELECT LAST_INSERT_ID();"));activities.append(act)
  sql('INSERT INTO local_seat_layout(actID,seatCode) VALUES '+','.join(f"({act},'{code}')" for code in seats(scope))+';')
  data=c.call('Seats?actID='+str(act));check(f'Venue {scope} capacity {expected}',lambda:must(len(data['seats'])==expected))
  check(f'Venue {scope} seat mode enabled',lambda:must(c.call('ActivityDetail?actID='+str(act))['seatMode']==1))
  if gap:check(f'Venue {scope} central gap not selectable',lambda:c.call('Seats',dict(action='hold',actID=act,seats=gap),expected=400))
  c.call('Seats',dict(action='hold',actID=act,seats=','.join(selected)))
  cart=c.call('Cart');row=next(x for x in cart['items'] if x['actID']==act)
  check(f'Venue {scope} cart keeps exact selected positions',lambda:must(row['seats']==selected and row['quantity']==2 and row['seatsValid']))
  check(f'Venue {scope} cart prices selected seats',lambda:must(cart['total']==1000))
  order=checkout(c,cart['requestID'])['orderIDs'][0];items=c.call('MemberOrders?orderID='+str(order))
  check(f'Venue {scope} order retains selected positions',lambda:must(sorted(x['seat'] for x in items)==selected))
  ticket=get(c,items[0]['orderListID'])
  check(f'Venue {scope} ticket retains selected position',lambda:must(ticket['seat'] in selected))
  check(f'Venue {scope} booking reserves both seats',lambda:must(sum(x['state']=='booked' for x in c.call('Seats?actID='+str(act))['seats'])==2))
  cancel(c,order)
  check(f'Venue {scope} cancellation releases only its seats',lambda:must(all(x['state']=='available' for x in c.call('Seats?actID='+str(act))['seats'])))
finally:
 if activities:
  actlist=','.join(map(str,activities));sql(f'DELETE FROM local_seat_booking WHERE actID IN ({actlist});DELETE FROM local_cart_seat WHERE actID IN ({actlist});')
 if ids:
  users=','.join(map(str,ids));sql(f'DELETE m FROM local_simulation_order m JOIN orders o ON o.orderID=m.orderID WHERE o.userID IN ({users});DELETE l FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM orders WHERE userID IN ({users});DELETE FROM local_checkout_receipt WHERE userID IN ({users});DELETE FROM cartact WHERE userID IN ({users});DELETE FROM user WHERE userID IN ({users});')
 if activities:sql(f'DELETE FROM local_seat_layout WHERE actID IN ({actlist});DELETE FROM act WHERE actID IN ({actlist});')
 for pub in publishers:sql(f'DELETE FROM Publisher WHERE pubID={pub};')
 (ROOT/'docs/venue-layout-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Venue layout checks passed:',len(results))
