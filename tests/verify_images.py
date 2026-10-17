#!/usr/bin/env python3
"""Multipart image regression checks; uses only new synthetic posts and cleans them."""
from pathlib import Path
import urllib.request,urllib.error,urllib.parse,http.cookiejar,json,time,struct,zlib,base64,secrets
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file());BASE='http://127.0.0.1:8083/newview/';results=[]
fixtures=ROOT/'tests/fixtures';fixtures.mkdir(exist_ok=True)
def png(width=96,height=64,color=(176,106,48)):
 def chunk(kind,data):return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)
 pixels=b''.join(b'\0'+bytes(color)*width for _ in range(height))
 return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',width,height,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b'')
PNG=(fixtures/'sample.png').read_bytes()
JPEG=(fixtures/'sample.jpg').read_bytes()
class Client:
 def __init__(self,account):
  self.opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()));self.csrf=''
  self.csrf=self.call('SignIn',{'account':account,'password':'NewView-local-2026!'})['csrf']
 def call(self,path,data=None,files=None,expected=200):
  headers={'X-CSRF-Token':self.csrf};body=None
  if files is not None:
   boundary='newview-'+secrets.token_hex(12);parts=[]
   for k,v in (data or {}).items():parts.append(('--'+boundary+'\r\nContent-Disposition: form-data; name="'+k+'"\r\n\r\n'+str(v)+'\r\n').encode())
   for name,mime,raw in files:
    parts.extend([('--'+boundary+'\r\nContent-Disposition: form-data; name="images"; filename="'+name+'"\r\nContent-Type: '+mime+'\r\n\r\n').encode(),raw,b'\r\n'])
   parts.append(('--'+boundary+'--\r\n').encode());body=b''.join(parts);headers['Content-Type']='multipart/form-data; boundary='+boundary
  elif data is not None:body=urllib.parse.urlencode(data).encode()
  req=urllib.request.Request(BASE+path,data=body,headers=headers)
  try:
   with self.opener.open(req,timeout=30) as response:code=response.status;text=response.read().decode()
  except urllib.error.HTTPError as error:code=error.code;text=error.read().decode()
  assert code==expected,f'{path}: expected {expected}, got {code}: {text[:120]}'
  try:return json.loads(text)
  except:return text
alice=Client('yuchen');bob=Client('yian');ids=[]
def check(name,fn):
 try:fn();results.append({'name':name,'passed':True});print('PASS',name)
 except Exception as e:results.append({'name':name,'passed':False,'error':str(e)});raise
def create(files):
 p=alice.call('DoPostServlet',{'action':'add','postHeader':'Image regression '+str(time.time_ns()),'postContent':'Synthetic image verification','postCategoryID':1,'replaceImages':'true','keepImageIds':'[]'},files=files);ids.append(p['postID']);return p['postID']
def detail(id):return alice.call('GetPostDetailServlet?postID='+str(id))
def update(id,keep=None,files=None,replace=True,expected=200):
 data={'action':'update','postID':id,'postHeader':'Image regression edited','postContent':'Preserved text','postCategoryID':1}
 if keep is not None:data['keepImageIds']=json.dumps(keep)
 if replace:data['replaceImages']='true'
 return alice.call('DoPostServlet',data,files=files,expected=expected)
try:
 id=create([('sample.png','image/png',PNG),('sample.jpg','image/jpeg',JPEG)])
 def roundtrip():
  images=detail(id)['images'];assert len(images)==2
  for item in images:assert item['url'].startswith('data:image/png;base64,') and base64.b64decode(item['url'].split(',',1)[1]).startswith(b'\x89PNG')
 check('PNG and JPEG multipart uploads return valid normalized images',roundtrip)
 first=detail(id)['images'];keep=[p['id'] for p in first]
 check('Edit text without image parameters preserves image IDs',lambda:update(id,files=[],replace=False))
 assert [p['id'] for p in detail(id)['images']]==keep
 check('Explicit keep preserves both images',lambda:update(id,keep,[]))
 assert [p['id'] for p in detail(id)['images']]==keep
 check('Remove one image without replacing the other',lambda:update(id,[keep[1]],[]))
 assert [p['id'] for p in detail(id)['images']]==[keep[1]]
 check('Replace removed slot with a new PNG',lambda:update(id,[keep[1]],[('new.png','image/png',png(color=(25,70,85)))]))
 assert len(detail(id)['images'])==2
 check('Remove all images explicitly',lambda:update(id,[],[]))
 assert detail(id)['images']==[]
 other=create([('other.png','image/png',PNG)]);otherpic=detail(other)['images'][0]['id']
 check('Reject borrowing image IDs from another post',lambda:update(id,[otherpic],[],expected=400))
 assert detail(id)['images']==[]
 check('Non-author cannot replace images',lambda:bob.call('DoPostServlet',{'action':'update','postID':other,'postHeader':'forged','postContent':'forged','postCategoryID':1,'replaceImages':'true'},files=[],expected=403))
 assert len(detail(other)['images'])==1
 fields={'action':'add','postHeader':'Invalid image test','postContent':'This must not be saved','postCategoryID':1}
 check('Reject SVG disguised as PNG',lambda:alice.call('DoPostServlet',fields,files=[('x.png','image/png',b'<svg xmlns="http://www.w3.org/2000/svg"></svg>')],expected=400))
 check('Reject corrupt PNG',lambda:alice.call('DoPostServlet',fields,files=[('x.png','image/png',PNG[:36])],expected=400))
 check('Reject more than three images',lambda:alice.call('DoPostServlet',fields,files=[('x.png','image/png',PNG)]*4,expected=400))
 check('Reject upload over 2 MB',lambda:alice.call('DoPostServlet',fields,files=[('big.png','image/png',b'x'*(2*1024*1024+1))],expected=413))
 check('Reject image dimensions over allowed limit',lambda:alice.call('DoPostServlet',fields,files=[('wide.png','image/png',png(6001,1))],expected=400))
 check('Missing post is a 404',lambda:alice.call('GetPostDetailServlet?postID=2147483647',expected=404))
 check('Three-image post is accepted',lambda:create([('x.png','image/png',PNG)]*3))
finally:
 for id in ids:
  try:alice.call('DeletePostServlet',{'postID':id})
  except Exception as e:results.append({'name':'cleanup '+str(id),'passed':False,'error':str(e)})
 (ROOT/'docs/image-verification.json').write_text(json.dumps({'checks':results,'passed':all(r['passed'] for r in results)},ensure_ascii=False,indent=2))
print('All',len(results),'image checks passed')
