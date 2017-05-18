import tracetool
import getIP

fileIp="ipt.txt"
fp = open(fileIp)
tracertLog = open("traceroute.log","w")
lines = fp.readlines()
localIp=getIP.getIpAddress('eth0')
for line in lines:
    hostIp = line.strip('\r\n')
    if hostIp != '':
        ret,tracertOut = tracetool.tracerouteCheck(hostIp)
        tracertLog.writelines("traceroute from %s to %s using ICMP\n"%(localIp,hostIp))
        tracertLog.writelines("%s\n"% item for item in tracertOut)
        tracertLog.writelines("\n")
        tracertLog.flush()
        print hostIp
    else:
        print "READ NULL CONTENT!"
tracertLog.close()
fp.close()