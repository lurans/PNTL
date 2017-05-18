import re

def parseLoss(out):
    regLoss = r'\d+%'
    packetsLoss = re.search(regLoss,out)
    if packetsLoss is not None:
        packetsLoss = packetsLoss.group()
        return packetsLoss
    else:
        return ""

def parseTime(out):
    regTime = r'\d+\.\d+\/\d+\.\d+\/\d+\.\d+\/\d+\.\d+ ms'
    packetsTime = re.search(regTime,out)
    if packetsTime is not None:
        packetsTime = packetsTime.group()
        return packetsTime
    else:
        return ""

def parseIp(out):
    regIp = r'\d+\.\d+\.\d+\.\d+'
    packetsIp =  re.search(regIp,out)
    if packetsIp is not None:
        packetsIp = packetsIp.group()
        return packetsIp
    else:
        return ""

def parseRouteNo(out):
    regRouteNo = r'\d+'
    packetsRouteNo = re.search(regRouteNo,out)
    if packetsRouteNo is not None:
        packetsRouteNo = packetsRouteNo.group()
        return packetsRouteNo
    else:
        return ""