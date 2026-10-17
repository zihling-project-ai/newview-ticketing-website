#!/usr/bin/env python3
"""Private ticket ownership, real QR decoding, concurrent redemption and cancellation races."""
from pathlib import Path
import json,time,subprocess,tempfile,os,concurrent.futures,urllib.request,urllib.parse,urllib.error
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
STATE=Path(__import__('os').environ.get('NEWVIEW_STATE_DIR',str(ROOT.parent/'.newview-ticketing-local')))
exec((ROOT/'tests/verify_cart.py').read_text().split('try:\n a=register')[0])
prefix='ticket_'+str(time.time_ns());accounts=[prefix+'a',prefix+'b'];ids=[];activities=[];publishers=[];trigger='ticket_fault_'+str(time.time_ns());act=None

def purchase(c,quantity=1):
 modify(c,'add',act,quantity);request=key(c);order=checkout(c,request)['orderIDs'][0];return order,[x['orderListID'] for x in c.call('MemberOrders?orderID='+str(order))]
def get(c,ticket):return c.call('Tickets?ticketID='+str(ticket))
def payload(c,ticket):return get(c,ticket)['payload']
def redeem(c,value,expected=200,**extra):return c.call('RedeemLocalTicket',dict(payload=value,confirmSimulation='true',**extra),expected=expected)
def cancel(c,order,expected=200):return c.call('CancelLocalOrder',dict(orderID=order,confirmCancellation='true'),expected=expected)
def raw(c,path,data):
 request=urllib.request.Request(BASE+path,data=urllib.parse.urlencode(data).encode(),headers={'X-CSRF-Token':c.csrf})
 try:
  with c.opener.open(request,timeout=20) as response:return response.status,json.loads(response.read())
 except urllib.error.HTTPError as error:return error.code,{}
try:
 a=register(accounts[0]);b=register(accounts[1]);anon=Client()
 act=int(sql(f"INSERT INTO act(actName,actPrice) VALUES('{prefix}',100);SELECT LAST_INSERT_ID();"));activities=[act]
 order,tickets=purchase(a,2);ticket=tickets[0];code=payload(a,ticket)
 check('Anonymous ticket list rejected',lambda:anon.call('Tickets',expected=401))
 check('Anonymous QR rejected',lambda:anon.call('TicketQR?ticketID='+str(ticket),expected=401))
 check('Anonymous redemption rejected',lambda:redeem(anon,code,expected=401))
 check('Owner ticket list shows own tickets',lambda:must(len(a.call('Tickets'))==2))
 check('Ticket list does not expose QR tokens',lambda:must(all('payload' not in t and 'localTicketToken' not in t for t in a.call('Tickets'))))
 check('Other member cannot read detail',lambda:b.call('Tickets?ticketID='+str(ticket),expected=404))
 check('Other member cannot read QR',lambda:b.call('TicketQR?ticketID='+str(ticket),expected=404))
 check('Forged user ID cannot reveal list',lambda:must(b.call('Tickets?userID='+str(ids[0]))==[]))
 check('Other member cannot redeem copied token',lambda:redeem(b,code,expected=404))
 check('Missing ticket ID on QR rejected',lambda:a.call('TicketQR',expected=400))
 check('Malformed ticket ID rejected',lambda:a.call('Tickets?ticketID=abc',expected=400))
 check('Missing ticket returns 404',lambda:a.call('Tickets?ticketID=2147483647',expected=404))
 check('GET redemption is forbidden',lambda:a.call('RedeemLocalTicket?payload='+urllib.parse.quote(code),expected=405))
 check('QR/detail reads do not redeem',lambda:must(get(a,ticket)['usedType']==0))
 check('Opaque code contains no name email or user ID field',lambda:must(code.startswith('NEWVIEW-LOCAL-TICKET:') and len(code)==len('NEWVIEW-LOCAL-TICKET:')+43 and '@' not in code and 'http' not in code))
 check('Distinct tickets have distinct tokens',lambda:must(code!=payload(a,tickets[1])))
 check('Token remains stable across reads',lambda:must(payload(a,ticket)==code))
 with a.opener.open(BASE+'TicketQR?ticketID='+str(ticket)) as response:png=response.read();mime=response.headers['Content-Type']
 check('QR endpoint returns PNG',lambda:must(mime=='image/png' and png.startswith(b'\x89PNG')))
 with tempfile.TemporaryDirectory() as tmp:
  path=Path(tmp);(path/'ticket.png').write_bytes(png)
  (path/'DecodeTicket.java').write_text('''import javax.imageio.ImageIO;import java.io.File;import com.google.zxing.*;import com.google.zxing.common.HybridBinarizer;import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
public class DecodeTicket {public static void main(String[] args)throws Exception{System.out.print(new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new File(args[0]))))),java.util.Collections.singletonMap(DecodeHintType.TRY_HARDER,Boolean.TRUE)).getText());}}''')
  cp=os.pathsep.join(str(p) for p in (Path(os.environ.get('NEWVIEW_MAVEN_REPO',str(STATE/'m2')))/'com/google/zxing').glob('*/3.5.3/*.jar'))
  subprocess.run(['javac','-proc:none','-cp',cp,'-d',tmp,str(path/'DecodeTicket.java')],check=True)
  decoded=subprocess.check_output(['java','-Djava.awt.headless=true','-cp',tmp+os.pathsep+cp,'DecodeTicket',str(path/'ticket.png')],text=True)
  check('Actual QR decodes to exact local ticket payload',lambda:must(decoded==code))
  extra_order,extra_tickets=purchase(a,10)
  def batch_qr():
   for sample in extra_tickets:
    expected=payload(a,sample)
    with a.opener.open(BASE+'TicketQR?ticketID='+str(sample)) as response:(path/'ticket.png').write_bytes(response.read())
    decoded=subprocess.check_output(['java','-Djava.awt.headless=true','-cp',tmp+os.pathsep+cp,'DecodeTicket',str(path/'ticket.png')],text=True)
    assert decoded==expected
  check('Ten additional random ticket QR images decode correctly',batch_qr)
 check('Redemption CSRF required',lambda:a.call('RedeemLocalTicket',dict(payload=code,confirmSimulation='true'),expected=403,csrf=False))
 check('Explicit simulation confirmation required',lambda:a.call('RedeemLocalTicket',dict(payload=code),expected=400))
 check('Invalid QR payload rejected',lambda:redeem(a,'https://example.test/',expected=400))
 check('Unknown well-formed token rejected',lambda:redeem(a,'NEWVIEW-LOCAL-TICKET:'+'A'*43,expected=404))
 with concurrent.futures.ThreadPoolExecutor(2) as pool:race=list(pool.map(lambda _:raw(a,'RedeemLocalTicket',dict(payload=code,confirmSimulation='true')),range(2)))
 check('Concurrent redemption succeeds exactly once',lambda:must(sorted(x[0] for x in race)==[200,409]))
 used=get(a,ticket)
 check('Redemption timestamp and used state persisted',lambda:must(used['usedType']==1 and bool(used['redeemedAt']) and not used['canRedeem']))
 check('Used ticket detail omits active payload',lambda:must('payload' not in used))
 check('Used QR no longer served',lambda:a.call('TicketQR?ticketID='+str(ticket),expected=409))
 check('Repeated redemption rejected',lambda:redeem(a,code,expected=409))
 check('Repeat cannot change original redemption timestamp',lambda:must(get(a,ticket)['redeemedAt']==used['redeemedAt']))
 check('Partially used order cannot be cancelled',lambda:cancel(a,order,expected=409))
 check('Other ticket in partially used order still redeemable',lambda:redeem(a,payload(a,tickets[1])))
 check('All-used order marked used',lambda:must(get(a,ticket)['orderState']==1))
 cancelled,ct=purchase(a);cancelled_code=payload(a,ct[0]);cancel(a,cancelled)
 check('Cancelled ticket remains queryable history',lambda:must(get(a,ct[0])['orderState']==2 and not get(a,ct[0])['canRedeem']))
 check('Cancelled QR unavailable',lambda:a.call('TicketQR?ticketID='+str(ct[0]),expected=409))
 check('Copied QR from cancelled order cannot redeem',lambda:redeem(a,cancelled_code,expected=409))
 race_order,rt=purchase(a);race_code=payload(a,rt[0])
 with concurrent.futures.ThreadPoolExecutor(2) as pool:
  tasks=[pool.submit(raw,a,'RedeemLocalTicket',dict(payload=race_code,confirmSimulation='true')),pool.submit(raw,a,'CancelLocalOrder',dict(orderID=race_order,confirmCancellation='true'))];out=[x.result() for x in tasks]
 final=get(a,rt[0])
 check('Cancel versus redeem race permits exactly one action',lambda:must(sorted(x[0] for x in out)==[200,409] and ((final['orderState']==2 and final['usedType']==0) or (final['orderState']==1 and final['usedType']==1))))
 fault,ft=purchase(a);fault_code=payload(a,ft[0])
 trigger_sql(f"DELIMITER //\nCREATE TRIGGER {trigger} BEFORE UPDATE ON orders FOR EACH ROW BEGIN IF NEW.orderID={fault} AND NEW.ordType=1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic ticket redemption rollback'; END IF; END//\nDELIMITER ;\n")
 check('Injected order-state failure rejects redemption',lambda:redeem(a,fault_code,expected=500))
 check('Failed redemption rolls back used flag and timestamp',lambda:must(get(a,ft[0])['usedType']==0 and not get(a,ft[0]).get('redeemedAt') and get(a,ft[0])['orderState']==3))
 trigger_sql('DROP TRIGGER '+trigger+';')
 check('Redemption can retry after rollback',lambda:redeem(a,fault_code))
 legacy=int(sql(f'INSERT INTO orders(userID,ordType,ordTotal,actQuantity) VALUES({ids[0]},3,100,1);SELECT LAST_INSERT_ID();'))
 legacy_ticket=int(sql(f'INSERT INTO orderlist(orderID,actID,actTotal,usedType) VALUES({legacy},{act},100,0);SELECT LAST_INSERT_ID();'))
 check('Legacy unmarked ticket is history only',lambda:must(not get(a,legacy_ticket)['canRedeem']))
 check('Legacy placeholder QR not served',lambda:a.call('TicketQR?ticketID='+str(legacy_ticket),expected=409))
 a.call('RecoverySession',{})
 check('Logout revokes ticket access',lambda:a.call('Tickets',expected=401))
finally:
 trigger_sql('DROP TRIGGER IF EXISTS '+trigger+';')
 if ids:
  users=','.join(map(str,ids));sql(f'DELETE m FROM local_simulation_order m JOIN orders o ON o.orderID=m.orderID WHERE o.userID IN ({users});DELETE l FROM orderlist l JOIN orders o ON o.orderID=l.orderID WHERE o.userID IN ({users});DELETE FROM orders WHERE userID IN ({users});DELETE FROM local_checkout_receipt WHERE userID IN ({users});DELETE FROM cartact WHERE userID IN ({users});DELETE FROM user WHERE userID IN ({users});')
 if act:sql(f'DELETE FROM act WHERE actID={act};')
 (ROOT/'docs/ticket-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Ticket checks passed:',len(results))
