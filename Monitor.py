# Import socket module
import socket, time , sched, subprocess

switch = 0
ports = [(12348,13),(12349,14),(12350,15)]
servers = [0,1,2]
fails = []
print("Monitor is on")

def signal(port,num):
    global switch
    switch = 1
    print("Server: " + str(num) + " host:dh2026pc1"+str(port[1])+"utm.utoronto.ca "+ " using port " + str(port[0])  + " is unresponsive" )
    print("\n\nDo you want to Revive server or Divert request traffic (Use commands \'R\' or \'D\'): ")
    while True:
        command = raw_input()
        if( command == "R"):
            subprocess.Popen(["python KillService.py &" + str(num) ],shell=True)
            subprocess.Popen(["python Orchastrate.py &" + str(num) ],shell=True)
            print("\n\nwiat a second while we try to Revive the server")
            servers.append(num-1)
            fails.remove((port,num))
            time.sleep(10)
            break
        elif( command == "D"):
            print("Divert")

            
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)


                proxy = "dh2026pc12.utm.utoronto.ca"
                port = 1238 #1235
                s.connect((proxy, port))

                s.send("DIVERT:" + str(num) )
                s.send("\n")
                print(s.recv(1024).strip("\n"))

            except:
                print("Proxy is down!!")
                subprocess.Popen(["python KillService.py all"],shell=True)
            break
        print("Use commands \'R\' or \'D\' :" )


def printStatus(status, num, host):
    print("Server: " + str(num) +" host:"+host+ " is " + str(status))

def ping(port,num):
    global servers
    # Create a socket object
    host = "dh2026pc"+str(port[1])+".utm.utoronto.ca"
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # Define the port on which you want to connect
        # connect to the server on local computer
        
        
        s.connect((host, port[0]))
        # s.setblocking(False)
        # receive data from the server
        s.send("PING")
        s.send("\n")
        printStatus(s.recv(1024), num,host)

        # close the connection
        s.close()

    except:
        printStatus("off", num,host)
        servers.remove(num-1)
        fails.append((port,num))
        signal(port,num)

while (True):
    
    subprocess.call(["clear"])
    for i in servers:
        ping(ports[i],i+1)
    for server in fails:
        print("Server: " + str(server[1]) + " using port " + str(server[0])  + " is unresponsive" )
    time.sleep(5)

