#!/usr/bin/env python3
from pathlib import Path
import subprocess,os,tempfile
ROOT=next(p for p in Path(__file__).resolve().parents if (p/'app/pom.xml').is_file())
STATE=Path(__import__('os').environ.get('NEWVIEW_STATE_DIR',str(ROOT.parent/'.newview-ticketing-local')))
cp=os.pathsep.join([str(ROOT/'app/target/classes')]+[str(p) for p in (Path(os.environ.get('NEWVIEW_MAVEN_REPO',str(STATE/'m2')))/'com/google/zxing').glob('*/3.5.3/*.jar')])
with tempfile.TemporaryDirectory() as tmp:
 subprocess.run(['javac','-proc:none','-cp',cp,'-d',tmp,str(ROOT/'tests/fixtures/QRVerifiedProbe.java')],check=True)
 subprocess.run(['java','-Djava.awt.headless=true','-cp',tmp+os.pathsep+cp,'QRVerifiedProbe'],check=True)
