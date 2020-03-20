import os, sys, subprocess, socket, time

num = int(sys.argv[1])
ports = [(12348,13),(12349,14),(12350,15)]

subprocess.Popen(["python KillService.py " + str(num) ],shell=True)
subprocess.Popen(["python Orchastrate.py " + str(num) ],shell=True)

time.sleep(3)

# Create a socket object
host = "dh2026pc"+str(ports[num%3][1])+".utm.utoronto.ca"
try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Define the port on which you want to connect
    # connect to the server on local computer
    
    
    s.connect((host, ports[num%3][0]))
    # s.setblocking(False)
    # receive data from the server
    s.send("HEAL")
    s.send("\n")
    print(s.recv(1024))

    # close the connection
    s.close()

except:
    print("\nCould not connect to Server "+ str(num%3))
    

try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)


    proxy = "dh2026pc12.utm.utoronto.ca"
    port = 1238 #1235
    s.connect((proxy, port))

    s.send("ALLOW")
    s.send("\n")
    print(s.recv(1024).strip("\n"))

except:
    print("Proxy is down!!")
    
