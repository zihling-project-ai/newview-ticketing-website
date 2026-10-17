#!/usr/bin/env python3
"""Initialize only an empty, isolated NewView database. Never resets existing data."""
from pathlib import Path
import json,secrets,subprocess,time
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
STATE=Path(__import__('os').environ.get('NEWVIEW_STATE_DIR',str(ROOT.parent/'.newview-ticketing-local'))).expanduser().resolve()
NAME='newview-ticketing-mysql'
IMAGE='mysql:8.0@sha256:7dcddc01f13bab2f15cde676d44d01f61fc9f99fe7785e86196dfc07d358ae2b'
envfile=STATE/'mysql.env';envfile.parent.mkdir(parents=True,exist_ok=True)
if not envfile.exists():
 settings={'MYSQL_ROOT_PASSWORD':secrets.token_urlsafe(24),'MYSQL_DATABASE':'newview_recovery','MYSQL_USER':'newview','MYSQL_PASSWORD':secrets.token_urlsafe(24)}
 envfile.write_text('\n'.join(k+'='+v for k,v in settings.items())+'\n');envfile.chmod(0o600)
exists=subprocess.run(['docker','container','inspect',NAME],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode==0
if exists:subprocess.run(['docker','start',NAME],check=True,stdout=subprocess.DEVNULL)
else:subprocess.run(['docker','run','-d','--name',NAME,'--env-file',str(envfile),'-p','127.0.0.1:3309:3306','--mount','type=volume,source=newview-ticketing-mysql-data,target=/var/lib/mysql',IMAGE,'--character-set-server=utf8mb4','--collation-server=utf8mb4_unicode_ci'],check=True)
def sql(text):
 return subprocess.run(['docker','exec','-i',NAME,'sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -N -u"$MYSQL_USER" --default-character-set=utf8mb4 "$MYSQL_DATABASE"'],input=text,capture_output=True,text=True)
for _ in range(60):
 result=sql('SELECT 1;')
 if result.returncode==0:break
 time.sleep(.5)
else:raise SystemExit('MySQL did not become ready')
tz=subprocess.run(['docker','exec','-i',NAME,'sh','-c','MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot'],input="SET PERSIST time_zone='+08:00';\n",capture_output=True,text=True)
if tz.returncode:raise SystemExit('Cannot configure local timezone: '+tz.stderr)
count=sql('SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE();')
if count.returncode:raise SystemExit(count.stderr)
if int(count.stdout.strip())>0:print('Database already has tables; no data was changed')
else:
 for p in sorted((ROOT/'database').glob('[0-9][0-9]-*.sql')):
  result=sql(p.read_text())
  if result.returncode:raise SystemExit('Import failed in '+p.name+': '+result.stderr)
  print('Imported',p.name)
print('Local MySQL ready on 127.0.0.1:3309')
