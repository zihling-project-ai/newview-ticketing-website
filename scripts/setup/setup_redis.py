#!/usr/bin/env python3
"""Dedicated loopback Redis, ephemeral seat holds only."""
import subprocess
NAME='newview-ticketing-redis'
IMAGE='redis@sha256:858f009f9709ce576febc734aa78b8f6d624b82571f9ddb6bda4377c833b3499'
exists=subprocess.run(['docker','container','inspect',NAME],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode==0
if exists:subprocess.run(['docker','start',NAME],check=True,stdout=subprocess.DEVNULL)
else:subprocess.run(['docker','run','-d','--name',NAME,'-p','127.0.0.1:6383:6379',IMAGE,'redis-server','--save','','--appendonly','no'],check=True)
subprocess.run(['docker','exec',NAME,'redis-cli','PING'],check=True)
