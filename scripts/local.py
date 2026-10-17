#!/usr/bin/env python3
"""Portable private lifecycle; never resets data or uploads source."""
from pathlib import Path
import os,sys,subprocess,shutil,socket,signal,time
ROOT=Path(__file__).resolve().parents[1]
STATE=Path(__import__('os').environ.get('NEWVIEW_STATE_DIR',str(ROOT.parent/'.newview-ticketing-local'))).expanduser().resolve()
RUN=STATE;RUN.mkdir(parents=True,exist_ok=True)
BASE=RUN/'tomcat';PID=RUN/'tomcat.pid'
def build():
 mvn=os.environ.get('NEWVIEW_MAVEN') or shutil.which('mvn')
 if not mvn:raise SystemExit('Install Maven 3.9 and add mvn to PATH')
 cache=os.environ.get('NEWVIEW_MAVEN_REPO',str(RUN/'m2'))
 with (RUN/'maven-build.log').open('w') as log:
  subprocess.run([mvn,'-B','-Dmaven.repo.local='+cache,'-f',str(ROOT/'app/pom.xml'),'-DskipTests','clean','package'],stdout=log,stderr=log,check=True)
 print('建置成功；日誌：'+str(RUN/'maven-build.log'))
def stop():
 if not PID.exists():print('No candidate Tomcat PID');return
 pid=int(PID.read_text())
 cmd=subprocess.run(['ps','-p',str(pid),'-o','command='],capture_output=True,text=True).stdout
 if cmd.strip():
  if str(BASE) not in cmd or 'org.apache.catalina.startup.Bootstrap' not in cmd:raise SystemExit('PID ownership mismatch; no process stopped')
  os.kill(pid,signal.SIGTERM)
  for _ in range(40):
   try:os.kill(pid,0)
   except ProcessLookupError:break
   time.sleep(.25)
  else:raise SystemExit('Tomcat still stopping; PID preserved')
 PID.unlink(missing_ok=True)
def start():
 if PID.exists():raise SystemExit('Candidate PID file exists; stop first')
 with socket.socket() as s:
  if s.connect_ex(('127.0.0.1',8083))==0:raise SystemExit('Port 8083 occupied; no service changed')
 home=Path(os.environ.get('CATALINA_HOME','/nonexistent')).expanduser().resolve()
 if not (home/'bin/catalina.sh').is_file():raise SystemExit('Set CATALINA_HOME to an installed Tomcat 9')
 version=subprocess.run([str(home/'bin/version.sh')],capture_output=True,text=True,check=True).stdout
 if 'Apache Tomcat/9.' not in version:raise SystemExit('Tomcat 9 required')
 envfile=RUN/'mysql.env'
 if not envfile.exists():raise SystemExit('Initialize candidate database first')
 settings=dict(line.split('=',1) for line in envfile.read_text().splitlines() if '=' in line)
 # Start only the candidate containers. No shared legacy container names.
 subprocess.run(['docker','start','newview-ticketing-mysql','newview-ticketing-redis'],check=True,stdout=subprocess.DEVNULL)
 for _ in range(60):
  p=subprocess.run(['docker','exec','newview-ticketing-mysql','sh','-c','MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE" -e "SELECT 1"'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
  if p.returncode==0:break
  time.sleep(.5)
 else:raise SystemExit('Candidate MySQL not ready')
 war=ROOT/'app/target/newview-0.0.1-SNAPSHOT.war'
 if not war.exists():raise SystemExit('Build first')
 for folder in ['conf','logs','temp','webapps','work']:(BASE/folder).mkdir(parents=True,exist_ok=True)
 for name in ['catalina.properties','logging.properties','web.xml','context.xml']:shutil.copy2(home/'conf'/name,BASE/'conf'/name)
 (BASE/'conf/server.xml').write_text('''<Server port="-1"><Service name="Catalina"><Connector address="127.0.0.1" port="8083" protocol="HTTP/1.1" connectionTimeout="20000" URIEncoding="UTF-8"/><Engine name="Catalina" defaultHost="localhost"><Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true"/></Engine></Service></Server>''')
 expanded=BASE/'webapps/newview'
 if expanded.exists():shutil.rmtree(expanded)
 shutil.copy2(war,BASE/'webapps/newview.war')
 env=os.environ.copy();env.update(CATALINA_HOME=str(home),CATALINA_BASE=str(BASE),NEWVIEW_DB_PASSWORD=settings['MYSQL_PASSWORD'])
 env['CATALINA_OPTS']=env.get('CATALINA_OPTS','')+' -Djava.awt.headless=true'
 with (RUN/'tomcat-console.log').open('a') as log:
  p=subprocess.Popen([str(home/'bin/catalina.sh'),'run'],env=env,stdout=log,stderr=log,start_new_session=True)
 PID.write_text(str(p.pid))
 print('Starting candidate: http://127.0.0.1:8083/newview/forum_home.html')
if __name__=='__main__':
 action=sys.argv[1] if len(sys.argv)==2 else ''
 if action=='build':build()
 elif action=='start':start()
 elif action=='stop':stop()
 else:raise SystemExit('Usage: python3 scripts/local.py build|start|stop')
