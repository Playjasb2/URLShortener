#!/usr/bin/python
import os, sys, subprocess,time

server = sys.argv[1]
hostNums=[
	"13", "14", "15"
	]
ports = [
    12345, 12346,12347
]
# current directory
cwd = os.getcwd()
    #turn on all servers
if ( server == "all"):
    subprocess.call(["javac", "ProxyServer.java"])
    subprocess.call(["echo" , "Compiled Java code"])

    #removes pids file
    subprocess.Popen(["rm", "out/pids.pid" ])

    for i in range(3):

        host = "dh2026pc" + hostNums[i] + ".utm.utoronto.ca"
        remoteCommand = 'cd "{}"; ./URLShortnerLocal.sh {};'.format(cwd, ports[i])
        print(remoteCommand)
        subprocess.Popen(["ssh", host, remoteCommand])


    p = subprocess.Popen(["java", "ProxyServer", "&"])

    subprocess.call(["echo" , "Proxy server is on"])
    # write pid of proxy to pids.pid
    subprocess.Popen(["echo  "+"Proxy:"+str(p.pid)+" >> out/pids.pid" ],shell=True)

    time.sleep(4)
    p = subprocess.Popen(["x-terminal-emulator -e python Monitor.py" ],shell=True)
    subprocess.Popen(["echo  "+"Monitor:"+str(p.pid)+" >> out/pids.pid" ],shell=True)
    #turn on proxy
elif (server == "p"):
    subprocess.call(["javac", "ProxyServer.java"])
    subprocess.call(["echo" , "Compiled Java code"])
    p = subprocess.Popen(["java", "ProxyServer", "&"])
    subprocess.call(["echo" , "Proxy server is on"])
    #turn on monitor
elif (server == "m"):
    
    p = subprocess.Popen(["x-terminal-emulator -e python Monitor.py" ],shell=True)
    subprocess.Popen(["echo  "+"Monitor:"+str(p.pid)+" >> out/pids.pid" ],shell=True)
else:
    i = int(server) -1
    host = "dh2026pc" + hostNums[i] + ".utm.utoronto.ca"
    remoteCommand = 'cd "{}"; ./URLShortnerLocal.sh {};'.format(cwd, ports[i])
    print(remoteCommand)
    subprocess.Popen(["ssh", host, remoteCommand])
