#!/usr/bin/env python3
"""Local synthetic-member integration checks, with owned fixture cleanup."""
from pathlib import Path
import json,time,subprocess
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_social.py').read_text().split('anonymous=Client()')[0])
account='member_'+str(time.time_ns());email=account+'@example.test'
a=Client();b=Client()
data=dict(account=account,password='Synthetic-local-2026!',confirmPassword='Synthetic-local-2026!',name='本機測試',nickname='合成會員',birthdate='2000-01-02',cellphone='0912345678',email=email)
def must(value):assert value
try:
 check('Anonymous profile rejected',lambda:a.call('GetUserInfo',expected=401))
 a.csrf=a.call('RecoverySession')['csrf']
 check('Signup GET rejected',lambda:a.call('SignUp',expected=405))
 check('Signup CSRF required',lambda:a.call('SignUp',data,expected=403,csrf=False))
 check('Invalid birth rejected',lambda:a.call('SignUp',dict(data,birthdate='2027-01-01'),expected=400))
 check('Invalid phone rejected',lambda:a.call('SignUp',dict(data,cellphone='123'),expected=400))
 check('Password confirmation checked',lambda:a.call('SignUp',dict(data,confirmPassword='wrong'),expected=400))
 check('Short password rejected',lambda:a.call('SignUp',dict(data,password='abc',confirmPassword='abc'),expected=400))
 result=a.call('SignUp',data);code=result['localCode']
 check('Signup requires local verification',lambda:must(result['status']=='verificationRequired'))
 check('Unverified account cannot login',lambda:a.call('SignIn',dict(account=account,password=data['password']),expected=401))
 check('Wrong verification code rejected',lambda:a.call('SignUp',dict(action='verify',code='wrong'),expected=400))
 check('Correct code creates account',lambda:a.call('SignUp',dict(action='verify',code=code),expected=201))
 check('Verification replay rejected',lambda:a.call('SignUp',dict(action='verify',code=code),expected=400))
 check('Duplicate account rejected',lambda:a.call('SignUp',dict(data,email='different@example.test'),expected=409))
 check('Duplicate email rejected',lambda:a.call('SignUp',dict(data,account=account+'x'),expected=409))
 old=a.csrf; a.csrf=a.call('SignIn',dict(account=account,password=data['password']))['csrf']
 check('Login rotates CSRF',lambda:must(old!=a.csrf))
 profile=a.call('GetUserInfo')
 check('Profile excludes password hash',lambda:must('password' not in json.dumps(profile).lower()))
 check('Profile contains registered values',lambda:must(all(profile[k]==data[k] for k in ['account','name','nickname','email','cellphone','birthdate'])))
 check('Profile GET cannot mutate',lambda:a.call('UpdateUserInfo?nickname=bad',expected=405))
 check('Profile change requires CSRF',lambda:a.call('UpdateUserInfo',data,expected=403,csrf=False))
 updated=dict(data,nickname='更新合成會員',userID='1',account='yuchen',buyAuthority='2',speakAuthority='2',password='forged')
 check('Profile update accepted',lambda:a.call('UpdateUserInfo',updated))
 current=a.call('GetUserInfo')
 check('Forged ID account authorities ignored',lambda:must(current['account']==account and current['buyAuthority']==profile['buyAuthority'] and current['speakAuthority']==profile['speakAuthority']))
 check('Profile update persisted',lambda:must(current['nickname']=='更新合成會員'))
 b.login('yuchen');demo=b.call('GetUserInfo')
 check('Other member unchanged',lambda:must(demo['account']=='yuchen' and demo['nickname']!='更新合成會員'))
 check('Duplicate email on edit rejected',lambda:a.call('UpdateUserInfo',dict(data,email=demo['email']),expected=409))
 check('Invalid edit is atomic',lambda:a.call('UpdateUserInfo',dict(data,nickname='bad',birthdate='invalid'),expected=400))
 check('Failed edit preserves profile',lambda:must(a.call('GetUserInfo')['nickname']=='更新合成會員'))
 a.call('RecoverySession',{})
 check('Logout removes access',lambda:a.call('GetUserInfo',expected=401))
 a.csrf=a.call('RecoverySession')['csrf'];a.csrf=a.call('SignIn',dict(account=account,password=data['password']))['csrf']
 check('Login and profile persist after logout',lambda:must(a.call('GetUserInfo')['nickname']=='更新合成會員'))
finally:
 # Delete only the unique synthetic fixture owned by this run, never existing members.
 query="DELETE FROM user WHERE userAccount='"+account+"' AND userEmail='"+email+"';"
 cleanup=subprocess.run(['docker','exec','-i','newview-ticketing-mysql','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -u"$MYSQL_USER" "$MYSQL_DATABASE"'],input=query,text=True,capture_output=True)
 (ROOT/'docs/member-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
 if cleanup.returncode:raise RuntimeError('Fixture cleanup failed')
print('Member checks passed:',len(results))
