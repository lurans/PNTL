#!/bin/python
import sys
import datetime
import os
import time
vm_src_ip = sys.argv[1]
vm_dst_ip = sys.argv[2]
phy_flag = sys.argv[3]
src_qvm_port = sys.argv[4]
dst_qvm_port = sys.argv[5]
direct = sys.argv[6]
taskid = sys.argv[7]
proto = sys.argv[8]
dvr_dev_id = sys.argv[9]
dvr_src_port = sys.argv[10]
dvr_dst_port = sys.argv[11]

timeOut = 2
ISOTIMEFORMAT='%Y-%m-%d %X'
timeStamp = time.strftime(ISOTIMEFORMAT, time.localtime())

outlog = taskid+'.log'
tmplog = 'tmp_'+taskid+'.log'
tmplog2 = 'tmp2_'+taskid+'.log'
tmplogTun = 'tmpTun_'+taskid+'.log'
tfile = "tcpdump"
if os.path.exists(outlog):
    os.remove(outlog)

if os.path.exists(tmplog):
    os.remove(tmplog)

if os.path.exists(tmplog2):
    os.remove(tmplog2)

if os.path.exists(tfile):
    shellCmd = 'chmod 777 ' + tfile
    os.popen(shellCmd)

def getBrPort(qvm_port):
    port_info = os.popen('brctl show | grep ' + qvm_port).readlines()
    if 0 == len(port_info):
        return None
    return port_info[0].split()[3]

def getTapPort(qvm_port):
    os.popen('brctl show > ' + tmplog)
    portInfo = open(tmplog, 'rU').readlines()
    for lineIndex in range(len(portInfo)):
        if qvm_port in portInfo[lineIndex]:
            break
    if lineIndex == len(portInfo):
        return None
    tapPort = portInfo[lineIndex + 1].split(' ')[0]#get ether addr from next line
    return tapPort

def getFlowProto(inputProto):
    flowProto = "0"
    if "icmp" == inputProto:
        flowProto = "1"
    elif "tcp" == inputProto:
        flowProto = "6"
    elif "udp" == inputProto:
        flowProto = "17"
    return flowProto
def excuShellCmd(sCmd, outfile, waittime):
    shellCmd = sCmd
    if None != outfile:
        shellCmd += ' > ' + outfile
    if None != waittime:
        shellCmd += ' & ' + "{ sleep " + str(waittime) + "; eval 'kill -9 $!' &>/dev/null; }"
    #print('excuting; ' + shellCmd)
    os.popen(shellCmd)

def getPortTcpDump(port, proto, src_ip, dst_ip, outfile, waittime):
    shellCmd = 'tcpdump -i ' + port + ' src host '+ src_ip +' and dst host '+ dst_ip +' and '+ proto +' -c 2 -n'
    excuShellCmd(shellCmd, outfile, waittime)

def getBondPortTcpDump(port, src_ip, dst_ip, outfile, waittime):
    shellCmd = 'tcpdump -i ' + port + ' src host '+ src_ip +' and dst host '+ dst_ip +' -c 2 -n'
    excuShellCmd(shellCmd, outfile, waittime)

def getVrPortInfo(dev_id,sub_port, proto, src_ip, dsp_ip, outfile, waittime ):
    shellCmd = 'ip net exec qrouter-' + dev_id +  ' tcpdump -i ' + sub_port +  \
                ' src host ' + src_ip + ' and dst host ' + dsp_ip + ' and '+ proto+ ' -c 2 -n'
    excuShellCmd(shellCmd, outfile, waittime)

def getOvsFlowInfo(proto, src_ip, dst_ip, outfile):
    flowProto = getFlowProto(proto)
    shellCmd = 'ovs-dpctl dump-flows|grep src=' + src_ip + '|grep dst='+ dst_ip +'|grep proto='+ flowProto + ' > ' +outfile
    os.popen(shellCmd)

def getOvsFlowInfoNoProto(src_ip, dst_ip, outfile):
    shellCmd = 'ovs-dpctl dump-flows|grep src=' + src_ip + '|grep dst='+ dst_ip + ' > ' +outfile
    os.popen(shellCmd)

def getOvsDvrFlowInfo(dvrmac, src_ip, dst_ip, outfile):
    flowProto = getFlowProto(proto)
    shellCmd = 'ovs-dpctl dump-flows|grep src=' + src_ip + '|grep dst='+ dst_ip +'|grep '+ dvrmac + ' > ' +outfile
    os.popen(shellCmd)

def printInfo2File(src_ip, dst_ip, br_port, packages, timeStamp, outfile):
    routeInfo = '%s %s %s %s %s' % (src_ip, dst_ip, br_port, packages, timeStamp)
    print >> open(outfile, 'a'), routeInfo

def getVbond_mac(phy_ip):
    os.popen('ifconfig > ' + tmplog)
    portInfo = open(tmplog, 'rU').readlines()
    for lineIndex in range(len(portInfo)):
        if phy_ip in portInfo[lineIndex]:
            break
    if lineIndex == len(portInfo):
        return None

    retMacAddr = portInfo[lineIndex - 1].split(' ')[0]
    return retMacAddr

def getVbond_port(vbondName):
    os.popen('ifconfig > ' + tmplog)
    portInfo = open(tmplog, 'rU').readlines()
    for lineIndex in range(len(portInfo)):
        if vbondName in portInfo[lineIndex]:
            break
    if lineIndex == len(portInfo):
        return None

    bondName = portInfo[lineIndex].split(' ')[0]
    return bondName

def getDvr_mac(dvr_devid,dvr_port):
    os.popen('ip net exec qrouter-' + dvr_devid + ' ip add > '  + tmplog)
    portInfo = open(tmplog, 'rU').readlines()
    for lineIndex in range(len(portInfo)):
        if dvr_port in portInfo[lineIndex]:
            break
    if lineIndex == len(portInfo):
        return None

    retMacAddr = portInfo[lineIndex + 1].split(' ')[5]#get ether addr from next line
    return retMacAddr

def isPortExist(portname):
    os.popen('ifconfig > ' + tmplog)
    portInfo = open(tmplog, 'rU').readlines()
    for lineIndex in range(len(portInfo)):
        if portname in portInfo[lineIndex]:
            break
    if lineIndex == len(portInfo):
        return False
    return True



def mainGetRoute():

    first_name = None
    qvm_port = None
    subnet_src_port = None
    subnet_dst_port = None
    flag_inner = None#in one host inner is 1;diff host inner is 0

    if None == direct:
        return

    if 'src' == direct:
        first_name = 'src:'
        qvm_port = src_qvm_port
        subnet_src_port = dvr_src_port
        subnet_dst_port = dvr_dst_port
    else:
        first_name = 'dst:'
        qvm_port = dst_qvm_port

    if 'true' == phy_flag:
        flag_inner = 1
    else:
        flag_inner = 0

#    print(first_name, end= '\n', file = open(outlog, 'w'))
    print >> open(outlog, 'a'), first_name

    # get br_port
    br_port = getBrPort(qvm_port)
#    tap_port = getTapPort(qvm_port)
    if None == br_port:
        shellCmd = 'echo END >> ' +outlog
        os.popen(shellCmd)
        shellCmd = 'chmod 777 ' + outlog
        os.popen(shellCmd)
        return

    src_ip = vm_src_ip
    dst_ip = vm_dst_ip
    DVR_FLAG = False
    # get tap prot tcpdump info
#    if None !=tap_port:
#        getPortTcpDump(tap_port, proto,src_ip, dst_ip, tmplog, timeOut)
 #       packages = len(open(tmplog, 'rU').readlines())
#        port_name = 'TAPport:' + tap_port
 #       printInfo2File(src_ip, dst_ip, port_name, str(packages), timeStamp, outlog)
    # get qvm prot tcpdump info
    getPortTcpDump(br_port, proto,src_ip, dst_ip, tmplog, timeOut)
    packages = len(open(tmplog, 'rU').readlines())
    port_name = 'VMport:' + br_port
    printInfo2File(src_ip, dst_ip, port_name, str(packages), timeStamp, outlog)

    # get dvr namespase port info
    dvr_src_port_name = dvr_dst_port_name = ""
    dvr_in_packages = dvr_out_packages = ""
    if 'None' != dvr_dev_id and 'None' != dvr_src_port and 'None' != dvr_dst_port and 'src' == direct:
        subnet_src_port = 'qr-'+ subnet_src_port
        subnet_dst_port = 'qr-'+ subnet_dst_port
        getVrPortInfo(dvr_dev_id, subnet_src_port, proto,src_ip, dst_ip, tmplog, timeOut)
        getVrPortInfo(dvr_dev_id, subnet_dst_port, proto,src_ip, dst_ip, tmplog2, timeOut)

        dvr_in_packages = len(open(tmplog, 'rU').readlines())
        dvr_out_packages = len(open(tmplog2, 'rU').readlines())

        dvr_src_port_name = 'DvrInport:' + subnet_src_port
        dvr_dst_port_name = 'DvrOutport:' + subnet_dst_port
        DVR_FLAG = True

    # get ovs flow info
    findFlow = True
    packages = 0
    phy_src_ip = None
    phy_dst_ip = None
    tmpLog2Info = ""
    if False == isPortExist("br-int") or False == isPortExist("br-tun"):
        os.system('cat '+outlog)
        shellCmd = 'echo END >> ' +outlog
        os.popen(shellCmd)
        shellCmd = 'chmod 777 ' + outlog
        os.popen(shellCmd)
        return
    #judge if has DVR
    if False == DVR_FLAG:
        packagesBef = packagesAft = 0
        #get flow first time
        getOvsFlowInfoNoProto(src_ip, dst_ip, tmplog)
        tmpLogInfo = open(tmplog, 'rU').readlines()

        if 0 == len(tmpLogInfo):
            printInfo2File(src_ip, dst_ip, "OVS:br-int", str(0), timeStamp, outlog)
            findFlow = False
            if 1 !=  flag_inner:
                printInfo2File(src_ip, dst_ip, "OVS:br-tun", str(0), timeStamp, outlog)

        if True == findFlow:
            for aLine in tmpLogInfo:
                if 'packets:' in aLine:
                    packagesBef += int(aLine.split('packets:')[1].split(',')[0])
        #get flow second time
            time.sleep(2)
            getOvsFlowInfoNoProto(src_ip, dst_ip, tmplog2)
            tmpLog2Info = open(tmplog2, 'rU').readlines()
            if 0 == len(tmpLog2Info):
                printInfo2File(src_ip, dst_ip, "OVS:br-int", str(0), timeStamp, outlog)
                findFlow = False
                if 1 !=  flag_inner:
                    printInfo2File(src_ip, dst_ip, "OVS:br-tun", str(0), timeStamp, outlog)
        if  True == findFlow:
            for aLine in tmpLog2Info:
                if 'packets:' in aLine:
                    packagesAft += int(aLine.split('packets:')[1].split(',')[0])

            packages  = (packagesAft - packagesBef)
            printInfo2File(src_ip, dst_ip, "OVS:br-int", packages, timeStamp, outlog)
            if 1 !=  flag_inner:
                for aLine in tmpLog2Info:
                    if 'tunnel' in aLine:
                         phy_src_ip = aLine.split('tunnel')[1].split(',')[1].split('src=')[1]
                         phy_dst_ip = aLine.split('tunnel')[1].split(',')[2].split('dst=')[1]
                         printInfo2File(phy_src_ip, phy_dst_ip, "OVS:br-tun", packages, timeStamp, outlog)
                         break
    else:
        packagesFirstBef = packagesSecondBef = packagesFirstAft = packagesSecondAft = 0
        tmpLogTunInfo = ""
        packagesFirst = packagesSecond = 0
        #get flow first time
        inportmac = getDvr_mac(dvr_dev_id,subnet_src_port)
        outportmac = getDvr_mac(dvr_dev_id,subnet_dst_port)
        getOvsDvrFlowInfo(inportmac,src_ip, dst_ip, tmplog)
        getOvsDvrFlowInfo(outportmac,src_ip, dst_ip, tmplog2)
        tmpLogInfo = open(tmplog, 'rU').readlines()
        tmpLogInfo2 = open(tmplog2, 'rU').readlines()
        if 0 == len(tmpLogInfo) or 0 == len(tmpLogInfo2):
            printInfo2File(src_ip, dst_ip, "OVS:br-int-BefDvr", str(0), timeStamp, outlog)
            printInfo2File(src_ip, dst_ip,dvr_src_port_name, str(dvr_in_packages), timeStamp, outlog)
            printInfo2File(src_ip, dst_ip, dvr_dst_port_name, str(dvr_out_packages), timeStamp, outlog)
            printInfo2File(src_ip, dst_ip, "OVS:br-int-AftDvr",  str(0), timeStamp, outlog)
            findFlow = False
            if 1 !=  flag_inner:
                printInfo2File(src_ip, dst_ip, "OVS:br-tun", str(0), timeStamp, outlog)
        if True == findFlow:
            for aLine in tmpLogInfo:
                if 'packets:' in aLine:
                    packagesFirstBef += int(aLine.split('packets:')[1].split(',')[0])
            for aLine in tmpLogInfo2:
                if 'packets:' in aLine:
                    packagesSecondBef += int(aLine.split('packets:')[1].split(',')[0])

        #get flow second time
            time.sleep(2)
            getOvsDvrFlowInfo(inportmac,src_ip, dst_ip, tmplog)
            getOvsDvrFlowInfo(outportmac,src_ip, dst_ip, tmplog2)
            getOvsFlowInfoNoProto(src_ip, dst_ip, tmplogTun)
            tmpLogInfo = open(tmplog, 'rU').readlines()
            tmpLogInfo2 = open(tmplog2, 'rU').readlines()
            tmpLogTunInfo = open(tmplogTun, 'rU').readlines()
            if 0 == len(tmpLogInfo) or 0 == len(tmpLogInfo2):
                printInfo2File(src_ip, dst_ip, "OVS:br-int-BefDvr", str(0), timeStamp, outlog)
                printInfo2File(src_ip, dst_ip,dvr_src_port_name, str(dvr_in_packages), timeStamp, outlog)
                printInfo2File(src_ip, dst_ip, dvr_dst_port_name, str(dvr_out_packages), timeStamp, outlog)
                printInfo2File(src_ip, dst_ip, "OVS:br-int-AftDvr",  str(0), timeStamp, outlog)
                findFlow = False
                if 1 !=  flag_inner:
                    printInfo2File(src_ip, dst_ip, "OVS:br-tun",  str(0), timeStamp, outlog)
        if  True == findFlow:
            for aLine in tmpLogInfo:
                if 'packets:' in aLine:
                    packagesFirstAft += int(aLine.split('packets:')[1].split(',')[0])
            for aLine in tmpLogInfo2:
                if 'packets:' in aLine:
                    packagesSecondAft += int(aLine.split('packets:')[1].split(',')[0])

            packagesFirst = (packagesFirstAft - packagesFirstBef)
            packagesSecond  = (packagesSecondAft - packagesSecondBef)
            packages = packagesFirst + packagesSecond
            printInfo2File(src_ip, dst_ip, "OVS:br-int-BefDvr", packagesFirst, timeStamp, outlog)
            printInfo2File(src_ip, dst_ip,dvr_src_port_name, str(dvr_in_packages), timeStamp, outlog)
            printInfo2File(src_ip, dst_ip, dvr_dst_port_name, str(dvr_out_packages), timeStamp, outlog)
            printInfo2File(src_ip, dst_ip, "OVS:br-int-AftDvr", packagesSecond, timeStamp, outlog)
            if 1 !=  flag_inner:
                for aLine in tmpLogTunInfo:
                    if 'tunnel' in aLine:
                         phy_src_ip = aLine.split('tunnel')[1].split(',')[1].split('src=')[1]
                         phy_dst_ip = aLine.split('tunnel')[1].split(',')[2].split('dst=')[1]
                         printInfo2File(phy_src_ip, phy_dst_ip, "OVS:br-tun", packages, timeStamp, outlog)
                         break
    #get eth port
    if 1 !=  flag_inner:
        if True == findFlow:
            vbondname = None
            if 'src' == direct:
                vbondname = getVbond_mac(phy_src_ip)
            else:
                vbondname = getVbond_mac(phy_dst_ip)
            getBondPortTcpDump(vbondname,phy_src_ip, phy_dst_ip, tmplog, timeOut)
            packages = open(tmplog, 'rU').readlines()
            port_name = 'HostPort:'+vbondname
            printInfo2File(phy_src_ip, phy_dst_ip, port_name, str(len(packages)), timeStamp, outlog)
        else:
            v_name = "v_"
            v_port  = getVbond_port(v_name)
            port_name = 'HostPort:'+v_port
            printInfo2File(src_ip, dst_ip, port_name, str(0), timeStamp, outlog)
        os.system('cat '+outlog)

    #remove tmp file
#    if os.path.exists(outlog):
#        os.remove(outlog)

    if os.path.exists(tmplog):
        os.remove(tmplog)

    if os.path.exists(tmplog2):
        os.remove(tmplog2)
    if vm_src_ip != sys.argv[1]:
        shellCmd = 'echo END >> ' +outlog
        os.popen(shellCmd)
        shellCmd = 'chmod 777 ' + outlog
        os.popen(shellCmd)
mainGetRoute()
vm_src_ip = sys.argv[2]
vm_dst_ip = sys.argv[1]
src_qvm_port = sys.argv[5]
dst_qvm_port = sys.argv[4]
if "src" == sys.argv[6]:
    direct = "dst"
else:
    direct = "src"
dvr_dev_id = sys.argv[9]
dvr_src_port = sys.argv[11]
dvr_dst_port = sys.argv[10]
mainGetRoute()





















































