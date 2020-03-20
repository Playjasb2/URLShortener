import subprocess

subprocess.Popen(["ab -n 1000 -c 100 -g load.tsv http://dh2026pc12.utm.utoronto.ca:1235/b6 > singlebenchmark.txt" ],shell=True)


lst = ["01","02","03","04"]

for i in range(4):
	host = "dh2026pc" + lst[i] + ".utm.utoronto.ca"
        remoteCommand = 'ab -n 1000 -c 100 -g load{}.tsv http://dh2026pc12.utm.utoronto.ca:1235/b6 '.format(lst[i])
        print(remoteCommand)
        subprocess.Popen(["ssh", host, remoteCommand])
