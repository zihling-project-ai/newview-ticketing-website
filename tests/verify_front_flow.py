#!/usr/bin/env python3
"""Fresh private fixtures for selected checkout, availability and QR preview/scan."""
from pathlib import Path
import time,json,urllib.request,urllib.error
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_coupons.py').read_text().split('try:\n a=register')[0])
prefix='flow_'+str(time.time_ns());accounts=[prefix+'a',prefix+'b'];ids=[];activities=[];publishers=[];discounts=[];trigger='flow_fault_'+str(time.time_ns());act=None

def scoped(c,acts,coupon=0,expected=200):return c.call('Cart?actIDs='+','.join(map(str,acts))+'&useDisID='+str(coupon),expected=expected)
def selected_buy(c,q,expected=201,**extra):return checkout(c,q['requestID'],actIDs=q['actIDs'],useDisID=q['coupon']['useDisID'],expected=expected,**extra)
def scan(c,data,expected=200):
 boundary='NewView'+str(time.time_ns());body=(f'--{boundary}\r\nContent-Disposition: form-data; name="image"; filename="ticket.png"\r\nContent-Type: image/png\r\n\r\n').encode()+data+f'\r\n--{boundary}--\r\n'.encode();req=urllib.request.Request(BASE+'DecodeTicketQR',data=body,headers={'Content-Type':'multipart/form-data; boundary='+boundary,'X-CSRF-Token':c.csrf})
 try:
  with c.opener.open(req,timeout=20) as response:code=response.status;data=response.read()
 except urllib.error.HTTPError as e:code=e.code;data=e.read()
 assert code==expected,(code,data[:100]);return json.loads(data) if code==200 else None
try:
 a=register(accounts[0]);b=register(accounts[1]);anon=Client()
 for n in range(2):publishers.append(int(sql(f"INSERT INTO Publisher(pubName) VALUES('{prefix}{n}');SELECT LAST_INSERT_ID();")))
 for n,(price,pub) in enumerate([(500,0),(300,0),(600,1)]):activities.append(int(sql(f"INSERT INTO act(actName,actPrice,pubID,actDate) VALUES('{prefix}{n}',{price},{publishers[pub]},DATE_ADD(CURRENT_DATE,INTERVAL 30 DAY));SELECT LAST_INSERT_ID();")))
 act,qty,other=activities;u,d=coupon(ids[0],100,publishers[0]);sql(f"INSERT INTO local_seat_layout(actID,seatCode) VALUES({act},'S01'),({act},'S02');")
 a.call('Seats',dict(action='hold',actID=act,seats='S01,S02'));modify(a,'add',qty,1);modify(a,'add',other,1)
 check('All three activities initially selected',lambda:must(a.call('Cart')['total']==1900))
 q=scoped(a,[qty],u)
 check('Only selected activity priced and coupon capped to its group',lambda:must(q['total']==300 and q['discount']==100 and q['payable']==200 and sum(x['selected'] for x in q['items'])==1))
 check('Forged selection not in cart rejected',lambda:scoped(a,[2147483647],expected=409))
 check('Duplicate selection rejected',lambda:a.call('Cart?actIDs='+str(qty)+','+str(qty),expected=400))
 check('Malformed selection rejected',lambda:a.call('Cart?actIDs=abc',expected=400))
 check('Empty selection can preview zero',lambda:must(scoped(a,[])['total']==0))
 q=scoped(a,[])
 check('Empty selection cannot checkout',lambda:selected_buy(a,q,400))
 q=scoped(a,[qty],u)
 check('Changing selection without new preview rejected',lambda:checkout(a,q['requestID'],actIDs=str(other),useDisID=u,expected=409))
 check('Other member cannot reuse scoped quote',lambda:checkout(b,q['requestID'],actIDs=str(qty),useDisID=u,expected=409))
 q=scoped(a,[qty],u);order=selected_buy(a,q)['orderIDs'][0]
 check('Partial checkout stores only selected activity',lambda:must(len(a.call('MemberOrders?orderID='+str(order)))==1 and orderrow(order)=='300,100,200'))
 remaining=a.call('Cart')['items']
 check('Unselected seated and quantity activities remain',lambda:must({x['actID'] for x in remaining}=={act,other} and next(x for x in remaining if x['actID']==act)['seatsValid']))
 check('Partial request replay does not consume remaining cart',lambda:must(selected_buy(a,q,200)['orderIDs']==[order] and len(a.call('Cart')['items'])==2))
 cancel(a,order)
 q=scoped(a,[act],u);seat_order=selected_buy(a,q)['orderIDs'][0]
 check('Selected seat checkout keeps unrelated quantity item',lambda:must(len(a.call('Cart')['items'])==1 and a.call('Cart')['items'][0]['actID']==other and int(sql(f'SELECT COUNT(*) FROM local_seat_booking WHERE actID={act};'))==2))
 ts=[x['orderListID'] for x in a.call('MemberOrders?orderID='+str(seat_order))];code=payload(a,ts[0])
 check('Preview anonymous rejected',lambda:anon.call('PreviewTicket',dict(payload=code),expected=401))
 check('Preview requires CSRF',lambda:a.call('PreviewTicket',dict(payload=code),csrf=False,expected=403))
 check('Foreign token preview rejected',lambda:b.call('PreviewTicket',dict(payload=code),expected=404))
 check('Owner can preview ticket without consuming it',lambda:must(a.call('PreviewTicket',dict(payload=code))['canRedeem'] and get(a,ts[0])['usedType']==0))
 check('Preview does not expose token or owner data',lambda:must(not {'payload','userID','userEmail'}&set(a.call('PreviewTicket',dict(payload=code)))))
 with a.opener.open(BASE+'TicketQR?ticketID='+str(ts[0])) as response:png=response.read()
 check('Uploaded owned QR decoded with ticket preview',lambda:must(scan(a,png)['ticketID']==ts[0] and scan(a,png)['payload']==code))
 check('QR scan does not redeem',lambda:must(get(a,ts[0])['usedType']==0))
 check('Foreign QR upload rejected',lambda:scan(b,png,404))
 check('Unreadable QR upload rejected',lambda:scan(a,b'not a QR',400))
 check('Oversize QR upload rejected',lambda:scan(a,b'x'*(2*1024*1024+1),413))
 redeem(a,code)
 check('Order exposes redeemed count for cancellation UI',lambda:must(next(x for x in a.call('MemberOrders') if x['orderID']==seat_order)['usedCount']==1))
 check('Used QR scan reports used state without usable token',lambda:must(not scan(a,png)['canRedeem'] and 'payload' not in scan(a,png)))
 check('Used preview cannot lead to repeated redemption',lambda:redeem(a,code,409))
 modify(a,'add',qty,1);cq=scoped(a,[qty]);co=selected_buy(a,cq)['orderIDs'][0];ticket=a.call('MemberOrders?orderID='+str(co))[0]['orderListID'];cc=payload(a,ticket);cancel(a,co)
 check('Cancelled code preview remains nonredeemable',lambda:must(not a.call('PreviewTicket',dict(payload=cc))['canRedeem']))
 sql(f'UPDATE act SET actDate=DATE_SUB(CURRENT_DATE,INTERVAL 1 DAY) WHERE actID={other};')
 check('Past activity exposed as ended',lambda:must(a.call('ActivityDetail?actID='+str(other))['ended']==1))
 check('Past activity cannot be added',lambda:a.call('Cart',dict(action='add',actID=other,quantity=1),expected=409))
 check('Cart with ended selected activity is invalid',lambda:must(not scoped(a,[other])['valid']))
 q=scoped(a,[other]);check('Ended selected activity cannot checkout',lambda:selected_buy(a,q,409))
 sql(f'UPDATE act SET actDate=DATE_SUB(CURRENT_DATE,INTERVAL 1 DAY) WHERE actID={act};')
 check('Past seated activity cannot renew holds',lambda:a.call('Seats',dict(action='hold',actID=act,seats='S01'),expected=409))
 modify(a,'add',qty,1)
 q=scoped(a,[qty]);check('Ended unselected activity does not block other checkout',lambda:selected_buy(a,q))
 check('Ended unselected activity remains for later removal',lambda:must(a.call('Cart')['items'][0]['actID']==other))
 profile=a.call('GetUserInfo');profile['cellphone']=''
 check('Profile can save without optional phone',lambda:a.call('UpdateUserInfo',profile))
 check('Empty optional phone persists',lambda:must(a.call('GetUserInfo')['cellphone']==''))
finally:
 trigger_sql('DROP TRIGGER IF EXISTS '+trigger+';')
 if ids:
  users=','.join(map(str,ids));sql(f'DELETE r FROM local_coupon_redemption r JOIN orders o ON o.orderID=r.orderID WHERE o.userID IN ({users});DELETE b FROM local_seat_booking b JOIN orderlist l ON l.orderListID=b.orderListID JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM local_cart_seat WHERE userID IN ({users});DELETE m FROM local_simulation_order m JOIN orders o ON o.orderID=m.orderID WHERE o.userID IN ({users});DELETE l FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM orders WHERE userID IN ({users});DELETE FROM local_checkout_receipt WHERE userID IN ({users});DELETE FROM cartact WHERE userID IN ({users});DELETE FROM useDiscount WHERE userID IN ({users});DELETE FROM user WHERE userID IN ({users});')
 if discounts:sql('DELETE FROM discount WHERE discountNO IN ('+','.join(map(str,discounts))+');')
 if activities:sql('DELETE FROM local_seat_layout WHERE actID IN ('+','.join(map(str,activities))+');DELETE FROM act WHERE actID IN ('+','.join(map(str,activities))+');')
 if publishers:sql('DELETE FROM Publisher WHERE pubID IN ('+','.join(map(str,publishers))+');')
 (ROOT/'docs/front-flow-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Front flow checks passed:',len(results))
