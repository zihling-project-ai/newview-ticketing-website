#!/usr/bin/env python3
"""Read-only activity API and member favorites cross-page contract."""
from pathlib import Path
import json,time
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_member_library.py').read_text().split('try:\n a=register')[0])
prefix='activity_'+str(time.time_ns());accounts=[prefix+'a',prefix+'b'];ids=[];act=None;other=None
try:
 a=register(accounts[0]);b=register(accounts[1]);anonymous=Client()
 act=int(sql("INSERT INTO act(actName,actIntroduce,actContent,actPrice,actDate,cityAddress) VALUES('"+prefix+"','Synthetic intro','<script>plain text fixture</script>',700,'2026-12-01','Synthetic venue');SELECT LAST_INSERT_ID();"))
 other=int(sql("INSERT INTO act(actName) VALUES('"+prefix+"empty');SELECT LAST_INSERT_ID();"))
 for ext in ['png','jpg']:
  data=(ROOT/'tests/fixtures'/('sample.'+ext)).read_bytes();sql(f"INSERT INTO actpic(actID,actPic) VALUES({act},X'{data.hex()}');")
 pics=[int(x) for x in sql(f'SELECT actPicID FROM actpic WHERE actID={act} ORDER BY actPicID;').splitlines()]
 bad=int(sql(f"INSERT INTO actpic(actID,actPic) VALUES({other},X'3c7376673e');SELECT LAST_INSERT_ID();"))
 listing=anonymous.call('ActivityList');detail=anonymous.call('ActivityDetail?actID='+str(act))
 check('Anonymous activity list available',lambda:must(any(x['actID']==act for x in listing)))
 check('List omits full content',lambda:must(all('actContent' not in x for x in listing)))
 check('Anonymous activity detail available',lambda:must(detail['actName']==prefix and detail['actPrice']==700))
 check('Raw content remains data, not injected markup',lambda:must(detail['actContent']=='<script>plain text fixture</script>'))
 check('No member credentials in activity payload',lambda:must(not any(k in json.dumps(detail).lower() for k in ['userpassword','useremail','passwordstamp'])))
 check('Stable image ordering',lambda:must(detail['imageIDs']==pics))
 for pic in pics:
  check('PNG/JPEG image normalized to PNG '+str(pic),lambda pic=pic:must(anonymous.opener.open(BASE+f'ActivityImage?actID={act}&picID={pic}').read().startswith(b'\x89PNG')))
 check('Default cover image available anonymously',lambda:must(anonymous.opener.open(BASE+'ActivityImage?actID='+str(act)).headers['Content-Type']=='image/png'))
 check('Image cannot be borrowed from another activity',lambda:anonymous.call(f'ActivityImage?actID={act}&picID={bad}',expected=404))
 check('Corrupt image safely rejected',lambda:anonymous.call(f'ActivityImage?actID={other}',expected=404))
 check('Missing detail ID rejected',lambda:anonymous.call('ActivityDetail',expected=400))
 check('Malformed ID rejected',lambda:anonymous.call('ActivityDetail?actID=abc',expected=400))
 check('Negative ID rejected',lambda:anonymous.call('ActivityDetail?actID=-1',expected=400))
 check('Missing activity returns 404',lambda:anonymous.call('ActivityDetail?actID=2147483647',expected=404))
 check('Malformed picture ID rejected',lambda:anonymous.call(f'ActivityImage?actID={act}&picID=invalid',expected=400))
 check('Null metadata supported',lambda:must(anonymous.call('ActivityDetail?actID='+str(other))['actName']==prefix+'empty'))
 check('Activity API cannot create or update',lambda:anonymous.call('ActivityDetail',dict(actID=act,action='update'),expected=405))
 check('Catalog POST cannot mutate',lambda:anonymous.call('ActivityList',dict(action='delete',actID=act),expected=405))
 check('Legacy mixed-write servlet stays blocked',lambda:anonymous.call('SearchSe?action=delete',expected=404))
 check('Anonymous favorite state is false',lambda:must(int(detail['favorite'])==0))
 check('Anonymous favorite write rejected',lambda:anonymous.call('MemberFavorites',dict(action='add',actID=act),expected=401))
 check('Logged in add through shared favorite endpoint',lambda:a.call('MemberFavorites',dict(action='add',actID=act)))
 check('Detail reflects member favorite',lambda:must(int(a.call('ActivityDetail?actID='+str(act))['favorite'])==1))
 check('List reflects member favorite',lambda:must(int(next(x for x in a.call('ActivityList') if x['actID']==act)['favorite'])==1))
 check('Member collection matches activity page',lambda:must(a.call('MemberFavorites')[0]['actID']==act))
 check('Favorite not exposed to other member',lambda:must(int(b.call('ActivityDetail?actID='+str(act))['favorite'])==0))
 check('Forged user ID cannot reveal favorite',lambda:must(int(b.call(f'ActivityDetail?actID={act}&userID={ids[0]}')['favorite'])==0))
 check('Anonymous cannot reveal member favorite',lambda:must(int(anonymous.call('ActivityDetail?actID='+str(act))['favorite'])==0))
 check('Remove favorite succeeds',lambda:a.call('MemberFavorites',dict(action='remove',actID=act)))
 check('Detail reflects removal',lambda:must(int(a.call('ActivityDetail?actID='+str(act))['favorite'])==0))
 check('Member collection reflects removal',lambda:must(a.call('MemberFavorites')==[]))
finally:
 if ids:
  clause=','.join(map(str,ids));sql(f'DELETE FROM myLike WHERE userID IN ({clause});DELETE FROM user WHERE userID IN ({clause});')
 for activity in [act,other]:
  if activity:sql(f'DELETE FROM actpic WHERE actID={activity};DELETE FROM act WHERE actID={activity};')
 (ROOT/'docs/activity-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Activity checks passed:',len(results))
