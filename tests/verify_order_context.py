#!/usr/bin/env python3
"""Verify order-to-ticket context and ownership with fresh disposable local data."""
from pathlib import Path
import time,json
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_tickets.py').read_text().split('try:\n a=register')[0])
prefix='order_context_'+str(time.time_ns());accounts=[prefix+'a',prefix+'b'];ids=[];activities=[];publishers=[]
try:
 a=register(accounts[0]);b=register(accounts[1]);anon=Client()
 for n in range(2):publishers.append(int(sql(f"INSERT INTO Publisher(pubName) VALUES('{prefix}{n}');SELECT LAST_INSERT_ID();")))
 for n,(price,pub) in enumerate([(500,0),(300,0),(600,1)]):activities.append(int(sql(f"INSERT INTO act(actName,actDate,actPrice,pubID,cityAddress,time) VALUES('{prefix}{n}',DATE_ADD(CURRENT_DATE,INTERVAL 30 DAY),{price},{publishers[pub]},'虛構核對場地','19:30:00');SELECT LAST_INSERT_ID();")))
 act,qty,other=activities;sql(f"INSERT INTO local_seat_layout(actID,seatCode) VALUES({act},'A01'),({act},'A02');")
 a.call('Seats',dict(action='hold',actID=act,seats='A01,A02'));modify(a,'add',qty,2);modify(a,'add',other,1)
 q=a.call('Cart');receipt=checkout(a,q['requestID']);orders=a.call('MemberOrders');tickets=a.call('Tickets')
 check('Checkout yields two publisher orders',lambda:must(len(orders)==2 and len(receipt['orderIDs'])==2))
 check('Nested items all point to their own order',lambda:must(all(x['orderID']==o['orderID'] for o in orders for x in o['items'])))
 check('Nested items match original order quantities',lambda:must(all(len(o['items'])==o['actQuantity'] for o in orders)))
 check('Same publisher shows both activities together',lambda:must(any({x['actID'] for x in o['items']}=={act,qty} for o in orders)))
 check('Nested dates and venues available',lambda:must(all(x['actDate'] and x['venue']=='虛構核對場地' and x['time']=='19:30:00' for o in orders for x in o['items'])))
 check('Each ticket maps to exactly one order item',lambda:must({t['ticketID'] for t in tickets}=={x['orderListID'] for o in orders for x in o['items']} and len(tickets)==5))
 check('Ticket parent order IDs match nested items',lambda:must(all(next(o['orderID'] for o in orders if any(x['orderListID']==t['ticketID'] for x in o['items']))==t['orderID'] for t in tickets)))
 check('Ticket cards have venue time and publisher context',lambda:must(all(t['venue']=='虛構核對場地' and t['time']=='19:30:00' and t['publisher'].startswith(prefix) for t in tickets)))
 check('Assigned positions stay identical across order and ticket',lambda:must(sorted(t['seat'] for t in tickets if t['actID']==act)==['A01','A02']))
 t=tickets[0];detail=get(a,t['ticketID'])
 check('Ticket detail retains its parent and event context',lambda:must(detail['orderID']==t['orderID'] and detail['actID']==t['actID'] and detail['venue']==t['venue']))
 check('Order list never embeds redemption tokens',lambda:must(all('payload' not in x and 'localTicketToken' not in x for o in orders for x in o['items'])))
 check('Receipt points to the same visible orders',lambda:must(set(a.call('LocalCheckout?requestID='+q['requestID'])['orderIDs'])=={o['orderID'] for o in orders}))
 check('Other member has no nested order content',lambda:must(b.call('MemberOrders')==[]))
 check('Other member cannot open order deep link data',lambda:b.call('MemberOrders?orderID='+str(t['orderID']),expected=404))
 check('Other member cannot open linked ticket',lambda:b.call('Tickets?ticketID='+str(t['ticketID']),expected=404))
 check('Other member cannot read receipt',lambda:b.call('LocalCheckout?requestID='+q['requestID'],expected=404))
 check('Anonymous cannot read order context',lambda:anon.call('MemberOrders',expected=401))
 check('Anonymous cannot read ticket context',lambda:anon.call('Tickets',expected=401))
finally:
 if activities:
  acts=','.join(map(str,activities));sql(f'DELETE FROM local_seat_booking WHERE actID IN ({acts});DELETE FROM local_cart_seat WHERE actID IN ({acts});')
 if ids:
  users=','.join(map(str,ids));sql(f'DELETE m FROM local_simulation_order m JOIN orders o ON o.orderID=m.orderID WHERE o.userID IN ({users});DELETE l FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM orders WHERE userID IN ({users});DELETE FROM local_checkout_receipt WHERE userID IN ({users});DELETE FROM cartact WHERE userID IN ({users});DELETE FROM user WHERE userID IN ({users});')
 if activities:sql(f'DELETE FROM local_seat_layout WHERE actID IN ({acts});DELETE FROM act WHERE actID IN ({acts});')
 for pub in publishers:sql(f'DELETE FROM Publisher WHERE pubID={pub};')
 (ROOT/'docs/order-context-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Order context checks passed:',len(results))
