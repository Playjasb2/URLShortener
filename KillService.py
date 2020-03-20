#!/usr/bin/python
import os, sys, subprocess

cwd = os.getcwd()
server = sys.argv[1]
file = open("./out/pids.pid", "r")
hostNum = "";

kill = False
for line in file:
    # get port:pid or Proxy:pid
    pid = line.strip('\n').split(":")

    if (pid[0] == "Server12345") and (server == "all" or server == "1" ):
        hostNum = "13"
        host = "dh2026pc" + hostNum + ".utm.utoronto.ca"
        remoteCommand = 'cd "{}";kill {};'.format(cwd, pid[1])
        print(remoteCommand)
        kill = True
    elif (pid[0] == "Server12346") and (server == "all" or server == "2"):
        hostNum = "14"
        host = "dh2026pc" + hostNum + ".utm.utoronto.ca"
        remoteCommand = 'cd "{}";kill {};'.format(cwd, pid[1])
        print(remoteCommand)
        kill = True
    elif (pid[0] == "Server12347") and (server == "all" or server == "3") :
        hostNum = "15"
        host = "dh2026pc" + hostNum + ".utm.utoronto.ca"
        remoteCommand = 'cd "{}";kill {};'.format(cwd, pid[1])
        print(remoteCommand)
        kill = True
    elif (pid[0] == "Proxy") and (server == "all" or server == "p" ):
        print("Killed Proxy")
        subprocess.Popen(["kill",pid[1]])
        continue
    elif (pid[0] == "Monitor") and (server == "all" or server == "m" ):
        print("Killed Monitor")
        subprocess.Popen(["kill",pid[1]])

    if(kill):
        subprocess.Popen(["ssh", host, remoteCommand])
        kill = False
    
    