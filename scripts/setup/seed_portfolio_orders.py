#!/usr/bin/env python3
"""Populate a fresh fictional dataset through the actual local application endpoints."""
from pathlib import Path
import json
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
STATE=Path(__import__('os').environ.get('NEWVIEW_STATE_DIR',str(ROOT.parent/'.newview-ticketing-local'))).expanduser().resolve()
exec((ROOT/'tests/verify_tickets.py').read_text().split('try:\n a=register')[0])
if int(sql('SELECT COUNT(*) FROM orders;'))!=0:raise SystemExit('Orders already exist; refusing to duplicate showcase history')
def buy(c,act,quantity=1,coupon=0,seats=None):
 if seats:c.call('Seats',dict(action='hold',actID=act,seats=','.join(seats)))
 else:modify(c,'add',act,quantity)
 request=c.call('Cart?useDisID='+str(coupon))['requestID'];order=checkout(c,request,useDisID=coupon)['orderIDs'][0];tickets=[x['orderListID'] for x in c.call('MemberOrders?orderID='+str(order))];return order,tickets
alice=Client();alice.login('yuchen');bob=Client();bob.login('yian')
cupons=alice.call('MemberCoupons');music=next(x['useDisID'] for x in cupons if x.get('pubID')==1);theatre=next(x['useDisID'] for x in cupons if x.get('pubID')==2)
ready,rt=buy(alice,1,2,music,['A01','A02']);cancelled,ct=buy(alice,2,1,theatre,['A01']);cancel(alice,cancelled)
# Historical showcase orders are created at a simulated event date, then restored.
historicalDate=sql('SELECT actDate FROM act WHERE actID=7;')
sql('UPDATE act SET actDate=CURRENT_DATE WHERE actID=7;')
for c,rating,text in [(alice,5,'弦樂的層次很細緻，樂手分享曲目故事的段落也很喜歡。小廳的距離讓每一次呼吸都聽得見。'),(bob,4,'很適合慢下來的午後。最喜歡中段那首小品，散場後還想再找相關作品來聽。')]:
 order,tickets=buy(c,7);redeem(c,payload(c,tickets[0]));c.call('TicketReview',dict(ticketID=tickets[0],rating=rating,content=text),expected=201)
sql("UPDATE act SET actDate='"+historicalDate+"' WHERE actID=7;")
meta=dict(readyOrderID=ready,readyTicketIDs=rt,cancelledOrderID=cancelled,couponMusic=music,couponTheatre=theatre,fictional=True,createdThroughActualEndpoints=True)
(STATE/'portfolio-orders.json').write_text(json.dumps(meta,ensure_ascii=False,indent=2));print('Fictional order states and verified reviews created')
