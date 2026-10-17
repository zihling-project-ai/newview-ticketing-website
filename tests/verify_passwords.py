#!/usr/bin/env python3
"""Exercise only unique synthetic credentials; never changes existing demo passwords."""
from pathlib import Path
import json,time,subprocess,tempfile
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
exec((ROOT/'tests/verify_social.py').read_text().split('anonymous=Client()')[0])
account='pw_'+str(time.time_ns());email=account+'@example.test'
initial='Initial-local-2026!';changed='Changed-local-2026!';reset='Reset-local-2026!'
registration=dict(account=account,email=email,password=initial,confirmPassword=initial,name='密碼測試',nickname='合成測試',birthdate='2000-01-01',cellphone='0912345678')
def must(x):assert x
def client():
 c=Client();c.csrf=c.call('RecoverySession')['csrf'];return c
def login(c,password):c.csrf=c.call('SignIn',{'account':account,'password':password})['csrf']
def credentials(p,**extra):return dict(newPassword=p,confirmNewPassword=p,**extra)
def issue(c):return c.call('RetrievePassword',dict(account=account,email=email))['localCode']
a=client();b=client();c=client()
try:
 code=a.call('SignUp',registration)['localCode'];a.call('SignUp',dict(action='verify',code=code),expected=201)
 login(a,initial);login(b,initial)
 check('Anonymous change rejected',lambda:c.call('UpdateUserPassword',credentials(changed,oldPassword=initial),expected=401))
 check('GET change rejected',lambda:a.call('UpdateUserPassword',expected=405))
 check('GET reset request rejected',lambda:c.call('RetrievePassword',expected=405))
 check('GET reset mutation rejected',lambda:c.call('VerifyPassword',expected=405))
 check('Change CSRF required',lambda:a.call('UpdateUserPassword',credentials(changed,oldPassword=initial),expected=403,csrf=False))
 check('Wrong original password rejected',lambda:a.call('UpdateUserPassword',credentials(changed,oldPassword='wrong'),expected=400))
 check('Missing original password rejected',lambda:a.call('UpdateUserPassword',credentials(changed),expected=400))
 check('Short new password rejected',lambda:a.call('UpdateUserPassword',credentials('short',oldPassword=initial),expected=400))
 check('Mismatched new passwords rejected',lambda:a.call('UpdateUserPassword',dict(newPassword=changed,confirmNewPassword='different',oldPassword=initial),expected=400))
 check('Identical old and new passwords rejected',lambda:a.call('UpdateUserPassword',credentials(initial,oldPassword=initial),expected=400))
 check('Reset without challenge rejected',lambda:c.call('VerifyPassword',credentials(reset,verificationCode='123456'),expected=400))
 check('Reset request CSRF required',lambda:c.call('RetrievePassword',dict(account=account,email=email),expected=403,csrf=False))
 check('Invalid email rejected',lambda:c.call('RetrievePassword',dict(account=account,email='invalid'),expected=400))
 stale_code=issue(c)
 check('Repeated reset request throttled',lambda:c.call('RetrievePassword',dict(account=account,email=email),expected=429))
 check('Password change succeeds with forged target ignored',lambda:a.call('UpdateUserPassword',credentials(changed,oldPassword=initial,userID='1',account='yuchen')))
 check('Current login revoked after change',lambda:a.call('GetUserInfo',expected=401))
 check('Other login revoked after change',lambda:b.call('GetUserInfo',expected=401))
 check('Old password stops working',lambda:b.call('SignIn',dict(account=account,password=initial),expected=401))
 check('New password login works',lambda:login(b,changed))
 check('Old reset challenge revoked by password change',lambda:c.call('VerifyPassword',credentials(reset,verificationCode=stale_code),expected=400))
 d=client();code=issue(d)
 check('Reset code bound to requesting session',lambda:client().call('VerifyPassword',credentials(reset,verificationCode=code),expected=400))
 check('Reset submit CSRF required',lambda:d.call('VerifyPassword',credentials(reset,verificationCode=code),expected=403,csrf=False))
 check('Wrong reset code rejected',lambda:d.call('VerifyPassword',credentials(reset,verificationCode='wrong'),expected=400))
 check('Reset confirmation mismatch rejected',lambda:d.call('VerifyPassword',dict(newPassword=reset,confirmNewPassword='wrong',verificationCode=code),expected=400))
 check('Reset cannot reuse current password',lambda:d.call('VerifyPassword',credentials(changed,verificationCode=code),expected=400))
 check('Reset leaves password unchanged before success',lambda:login(a,changed))
 check('Correct reset succeeds',lambda:d.call('VerifyPassword',credentials(reset,verificationCode=code,userID='1',email='forged@example.test')))
 check('Reset revokes first prior login',lambda:a.call('GetUserInfo',expected=401))
 check('Reset revokes second prior login',lambda:b.call('GetUserInfo',expected=401))
 d.csrf=d.call('RecoverySession')['csrf']
 check('Used reset code cannot replay',lambda:d.call('VerifyPassword',credentials('Replay-local-2026!',verificationCode=code),expected=400))
 check('Previous password rejected after reset',lambda:a.call('SignIn',dict(account=account,password=changed),expected=401))
 check('Reset password login succeeds',lambda:login(a,reset))
 check('Profile fields preserved',lambda:must(all(a.call('GetUserInfo')[k]==registration[k] for k in ['name','email','nickname','birthdate','cellphone'])))
 e=client();code=issue(e)
 for _ in range(5):e.call('VerifyPassword',credentials('Another-local-2026!',verificationCode='wrong'),expected=400)
 check('Five failures lock challenge even with correct code',lambda:e.call('VerifyPassword',credentials('Another-local-2026!',verificationCode=code),expected=400))
 f=client();result=f.call('RetrievePassword',dict(account=account,email='mismatch@example.test'))
 check('Mismatched email returns no code',lambda:must('localCode' not in result))
 check('Mismatched email cannot reset account',lambda:f.call('VerifyPassword',credentials(changed,verificationCode='000000'),expected=400))
 check('Existing demo member credentials unchanged',lambda:Client().login('yuchen'))
 # Expiration/one-use boundaries tested without waiting ten minutes or adding a server clock override.
 with tempfile.TemporaryDirectory() as tmp:
  source=ROOT/'app/src/main/java/com/newview/recovery/ResetChallenge.java'
  test=Path(tmp)/'ChallengeCheck.java';test.write_text('''import com.newview.recovery.ResetChallenge;
public class ChallengeCheck { public static void main(String[] a){
ResetChallenge c=new ResetChallenge(1,"stamp","email","123456",1000);
assert c.verify("123456",600999);c.consume();assert !c.verify("123456",600999);
c=new ResetChallenge(1,"stamp","email","123456",1000);assert !c.verify("123456",601000);
c=new ResetChallenge(1,"stamp","email","123456",1000);for(int i=0;i<5;i++)assert !c.verify("wrong",1001);assert !c.verify("123456",1002);
}}''')
  subprocess.run(['javac','-d',tmp,str(source),str(test)],check=True)
  check('Expiry boundary, consumption and attempt limit',lambda:subprocess.run(['java','-ea','-cp',tmp,'ChallengeCheck'],check=True))
finally:
 query="DELETE FROM user WHERE userAccount='"+account+"' AND userEmail='"+email+"';"
 cleanup=subprocess.run(['docker','exec','-i','newview-ticketing-mysql','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -u"$MYSQL_USER" "$MYSQL_DATABASE"'],input=query,text=True,capture_output=True)
 (ROOT/'docs/password-verification.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
 if cleanup.returncode:raise RuntimeError('Fixture cleanup failed')
print('Password checks passed:',len(results))
