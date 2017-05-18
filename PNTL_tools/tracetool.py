#!/usr/bin/python
import subprocess
import searchAllRoad
import os
import subprocess
import regexExp

def simpleResult(list,packets_nums):
    startItem = ''
    cnt=0
    retList = []
    for item in list:
        if item.count('*') == packets_nums:
            if cnt == 0:
                startItem = item
            cnt = cnt +1
        else:
            if cnt > 0:
                retList.append(startItem + " " +bytes(cnt))
            regItem = regexExp.parseRouteNo(item)+'  '+regexExp.parseIp(item)
            retList.append(regItem)
            cnt = 0
    if cnt > 0:
        retList.append(startItem + " " +bytes(cnt))
    return retList

def checkTraceroute(hostIp,lastItem):
    if hostIp == regexExp.parseIp(lastItem):
        return 1
    else:
        return 0

def tracerouteCheck(ip,max_hops = 30, packets_num = 3,time_out = 3, packets_size = 40):
    command = "traceroute -I"+" -m "+bytes(max_hops)+" -q "+bytes(packets_num)+" -w "+bytes(time_out)+" "+ip+" "+bytes(packets_size)
    popen=subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    ret = popen.wait()
    out = popen.stdout.read().splitlines()[1:]
    out = simpleResult(out,packets_num)
    return ret,out

def dictSearch(dictRoad):
    for (key,value) in dictRoad.items():
        print key," : ",value

if __name__=="__main__":
    dir = 'log'
    if os.path.isdir(dir):
        pass
    else:
        os.makedirs(dir)
    fp = open("ipTest.txt")
    tracerouteLogName = dir + "/traceroute.log"
    tracerouteLog = open(tracerouteLogName, 'w')
    retRoad={}
    lines = fp.readlines()
    for line in lines:
        hostIp = line.strip('\r\n')
        if hostIp != '':
            retTraceroute, tracerouteOut = tracerouteCheck(hostIp)
            statTraceroute = ''
            listRoad = []
            if checkTraceroute(hostIp, tracerouteOut[-1]):
                statTraceroute = 'OK'
                listRoad = tracerouteOut[:-1]
            else:
                statTraceroute = 'FAILED'
                listRoad = tracerouteOut
            tracerouteLog.writelines("traceroute to %s using ICMP status = %s\n" % (hostIp, statTraceroute))
            tracerouteLog.writelines("%s\n" % item for item in tracerouteOut)
            tracerouteLog.flush()
            tupleRoad = tuple(listRoad)
            searchAllRoad.dictRoad(tupleRoad,hostIp,retRoad)
            print hostIp
        else:
            pass
    tracerouteLog.close()
    dictSearch(retRoad)
    fp.close()
