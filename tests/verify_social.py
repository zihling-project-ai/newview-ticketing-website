#!/usr/bin/env python3
"""HTTP integration checks against synthetic local data. Creates and cleans its own records."""
import urllib.request,urllib.error,urllib.parse,http.cookiejar,json,time
from pathlib import Path
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
BASE='http://127.0.0.1:8083/newview/'
results=[]
class Client:
 def __init__(self):self.opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()));self.csrf=''
 def call(self,path,data=None,expected=200,csrf=True):
  req=urllib.request.Request(BASE+path,data=urllib.parse.urlencode(data).encode() if data is not None else None,headers={'X-CSRF-Token':self.csrf} if csrf else {})
  try:
   with self.opener.open(req,timeout=20) as response:code=response.status;body=response.read().decode()
  except urllib.error.HTTPError as error:code=error.code;body=error.read().decode()
  assert code==expected,f'{path}: expected {expected}, got {code}: {body[:160]}'
  try:return json.loads(body)
  except json.JSONDecodeError:return body
 def login(self,account):
  self.csrf=self.call('RecoverySession')['csrf']
  self.csrf=self.call('SignIn',{'account':account,'password':'NewView-local-2026!'})['csrf']
def check(name,fn):
 try:fn();results.append({'name':name,'passed':True});print('PASS',name)
 except Exception as e:results.append({'name':name,'passed':False,'error':str(e)});print('FAIL',name,str(e));raise
anonymous=Client();alice=Client();bob=Client();pid=None
try:
 check('Local page responds',lambda:anonymous.call('forum_home.html'))
 check('Read feed without login',lambda:anonymous.call('GetPostServlet?action=getAll'))
 check('Read categories',lambda:anonymous.call('GetPostCategoryServlet?action=list'))
 check('Anonymous mutation rejected',lambda:anonymous.call('DoPostServlet',{'action':'add'},expected=401))
 check('GET cannot delete',lambda:anonymous.call('DeletePostServlet?postID=1',expected=405))
 check('Unverified whole-site route disabled',lambda:anonymous.call('OrdersServlet',expected=404))
 check('Incorrect password rejected',lambda:anonymous.call('SignIn',{'account':'yuchen','password':'wrong'},expected=401))
 check('Alice PBKDF2 login',lambda:alice.login('yuchen'))
 check('Bob PBKDF2 login',lambda:bob.login('yian'))
 check('Missing CSRF rejected',lambda:alice.call('DoPostServlet',{'action':'add'},expected=403,csrf=False))
 check('Plain-text boundary rejects HTML injection',lambda:alice.call('DoPostServlet',{'action':'add','postHeader':'test','postCategoryID':1,'postContent':'<script>alert(1)</script>'},expected=400))
 title='Recovery check '+str(time.time_ns())
 check('Create post without image',lambda:alice.call('DoPostServlet',{'action':'add','postHeader':title,'postCategoryID':1,'postContent':'Synthetic integration test','img_base64':''}))
 posts=alice.call('GetPostServlet?action=getAll');created=next(p for p in posts if p['postHeader']==title);pid=created['postID']
 check('Latest feed places new post first',lambda:None if posts[0]['postID']==pid else (_ for _ in ()).throw(AssertionError('newest post is not first')))
 check('Bob cannot edit Alice post',lambda:bob.call('DoPostServlet',{'action':'update','postID':pid,'postHeader':'changed','postCategoryID':1,'postContent':'bad'},expected=403))
 check('Bob cannot delete Alice post',lambda:bob.call('DeletePostServlet',{'postID':pid},expected=403))
 check('Author can edit own post',lambda:alice.call('DoPostServlet',{'action':'update','postID':pid,'postHeader':title+' edited','postCategoryID':1,'postContent':'Updated synthetic text','img_base64':''}))
 edited=next(p for p in alice.call('GetPostServlet?action=getAll') if p['postID']==pid)
 assert edited['postContent']=='Updated synthetic text'
 def reaction():
  counts=alice.call('DoLikeDislikeServlet',{'action':'like','postID':pid,'likeCount':99999,'dislikeCount':99999})
  assert counts=={'likeCount':1,'dislikeCount':0},counts
 check('Server ignores forged reaction counts',reaction)
 check('Repeated like is idempotent',reaction)
 counts=alice.call('DoLikeDislikeServlet',{'action':'dislike','postID':pid});assert counts=={'likeCount':0,'dislikeCount':1}
 results.append({'name':'Switch reaction without double counting','passed':True})
 check('Popular feed returns JSON',lambda:alice.call('GetPostServlet?action=getbylike'))
 message=alice.call('DoMessageServlet',{'action':'AddMsg','postID':pid,'MesContent':'Synthetic comment'})
 assert isinstance(message,dict) and message['postMessageID']
 mid=message['postMessageID'];results.append({'name':'Create comment returns valid JSON','passed':True})
 check('Read comments',lambda:alice.call('DoMessageServlet?action=GetAll&postID='+str(pid)))
 check('Bob cannot change Alice comment',lambda:bob.call('DoMessageServlet',{'action':'Update','postMessageID':mid,'userID':2,'mesContent':'forged'},expected=403))
 check('Bob cannot delete Alice comment',lambda:bob.call('DoMessageServlet',{'action':'Delete','postMessageID':mid},expected=403))
 check('Author can edit own comment',lambda:alice.call('DoMessageServlet',{'action':'Update','postMessageID':mid,'userID':2,'mesContent':'Updated own comment'}))
 comments=alice.call('DoMessageServlet?action=GetAll&postID='+str(pid));assert comments[0]['mesContent']=='Updated own comment'
 check('Author can delete own comment',lambda:alice.call('DoMessageServlet',{'action':'Delete','postMessageID':mid}))
 assert alice.call('DoMessageServlet?action=GetAll&postID='+str(pid))==[]
 # Leave another comment and reaction to exercise cascaded post deletion.
 bob.call('DoMessageServlet',{'action':'AddMsg','postID':pid,'MesContent':'Cascade check'})
 check('Author deletes post with associated comments and reactions',lambda:alice.call('DeletePostServlet',{'postID':pid}))
 assert all(p['postID']!=pid for p in alice.call('GetPostServlet?action=getAll'));pid=None
 check('Logout invalidates identity',lambda:alice.call('RecoverySession',{}))
 check('Mutation rejected after logout',lambda:alice.call('DoPostServlet',{'action':'add'},expected=401))
finally:
 if pid is not None:
  try:alice.call('DeletePostServlet',{'postID':pid})
  except Exception:pass
 (ROOT/'docs/http-verification.json').write_text(json.dumps({'base_url':BASE,'checks':results,'passed':all(x['passed'] for x in results)},ensure_ascii=False,indent=2))
print('All',len(results),'HTTP checks passed')
