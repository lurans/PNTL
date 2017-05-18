#!/usr/bin/env python
# -*- coding: utf-8 -*-

#!/usr/bin/env python

# ------------------------------------------
# Python 函数定义

# 创建一个Logger对象,用于记录日志.
# 一个Logger对象对应一个Log模块和一个文件, 可选日志是否同时输出到终端
def CreateLogger(Name, LogFile, LogToFile, LogToStream):
    try:
        
        NewLogger = logging.getLogger(Name)
        NewLogger.setLevel(logging.DEBUG)
        
        Formatter = logging.Formatter("[%(asctime)s][%(name)s][%(levelname)s]\t[%(filename)s][%(lineno)3d]\t%(message)s")
        
        # log需要记录到日志文件
        if True == LogToFile:
            # mkdir for LogFile
            Path = os.path.dirname(LogFile)
            if False == os.path.exists(Path):
                os.makedirs(Path)
        
            FileHandler = logging.FileHandler(LogFile)
            FileHandler.setLevel(logging.DEBUG)
            FileHandler.setFormatter(Formatter)
            NewLogger.addHandler(FileHandler)
            
        # log需要打印到终端
        if True == LogToStream:
            StreamHandler = logging.StreamHandler()
            StreamHandler.setLevel(logging.DEBUG)
            StreamHandler.setFormatter(Formatter)
            NewLogger.addHandler(StreamHandler)
        
        NewLogger.info("")
        NewLogger.info("================= Logger[%s] Start Working Now ====================" % Name)
        NewLogger.info("")
        
    # Exception
    except Exception: 
        print "[%s] Exception in CreateLogger. Name:[%s], Path:[%s], ToFile[%s], ToStream[%s]. " % (datetime.datetime.now(), Name, LogFile, LogToFile, LogToStream)
        traceback.print_exc()
        return -1
    # No Exception    
    else:
        return NewLogger

# 根据Logger名称返回Logger对象.
def GetLogger(Name):
    try:
        # name与对象是一对一的, 不会重复创建.
        Logger = logging.getLogger(Name)
        
    # Exception
    except Exception: 
        print "[%s] Exception in GetLogger. Name:[%s] " % (datetime.datetime.now(), Name)
        traceback.print_exc()
        return -1
    # No Exception    
    else:
        return Logger
        
# 根据Logger名称销毁Logger对象, 预留, 尚未实现
def DestroyLogger(Name):
    try:
        # name与对象是一对一的, 不会重复创建.
        Logger = logging.getLogger(Name)
        Logger.info("")
        Logger.info("================= Logger[%s] Stop Working Now ====================" % Name)
        Logger.info("")
        #logging.shutdown()
        
    # Exception
    except Exception: 
        print "[%s] Exception in DestroyLogger. Name:[%s] " % (datetime.datetime.now(), Name)
        traceback.print_exc()
        return -1
    # No Exception    
    else:
        return 0

# 使用ssh登陆远端服务器, 并执行一个命令.
# 标准错误输出有内容时认为命令执行出错.
# 待优化异常处理,碰到过非法IP导致的挂死?
# 使用方法参考: http://docs.paramiko.org/en/2.0/
def ProcessAgentSSHCmd(IP, Port, User, Password, Path, Cmd, Logger):
    try:
        CmdFailed = False
        Ret = 0
        
        # 导入环境变量:Agent管理IP
        # 切换目录:
        # 前面两个命令执行成功的情况下执行用户输入的命令
        RealCmd ="LANG= && export Agent_ConnectInfo_IP="+ IP + " && cd " + Path + " && (" + Cmd +")"
        
        Logger.info("")
        Logger.info("\t+++++++++++++++++++++++++++++++++++++++++")
        Logger.info("\tConnecting to Agent[%s]" % (IP))
        
        # 建立ssh 客户端对象
        Client = paramiko.SSHClient()
        Client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        # 建立连接, 设置超时时间, 超时抛异常, 避免挂死. 
        Client.connect(IP, Port, User, Password, timeout=15)
        
        Logger.info("\tConnecting to Agent[%s] success." % (IP))
        Logger.info("\tBeginning to exec cmd[%s : %s] in Agent[%s]" % (Path, Cmd, IP))
        
        # stdin, stdout, stderr = Client.exec_command(RealCmd, timeout=300)
        # 通过ssh对象提交命令, 并记录执行结果, 暂时未添加超时检测(timeout 单位为s)
        stdin, stdout, stderr = Client.exec_command(RealCmd)
        # 打印执行结果
        Logger.info("\tResult: ")
        
        # 如果在远程启动service, 部分特殊格式字符会导致 stdout.readlines()接收不到EOF而挂死.
        out = stdout.readlines()
        for str in out:
            str = str.replace("\n", "")
            Logger.info("\t%s" % str)
        out = stderr.readlines()
        for str in out:
            str = str.replace("\n", "")
            # WARNING告警不当做执行失败(忽略大小写的warning)
            if -1 == str.upper().find("WARNING:"):
                CmdFailed = True
                Logger.error("\t%s" % str)
            else:
                Logger.warning("\t%s" % str)
                
        # 判断执行是否出错.
        if False == CmdFailed :
            Logger.info("\tExec cmd[%s : %s] in Agent[%s] success" % (Path, Cmd, IP))
            Ret = 0
        else:
            Logger.error("\tExec cmd[%s : %s] in Agent[%s] failed" % (Path, Cmd, IP))
            Ret = -1        
        Logger.info("\t+++++++++++++++++++++++++++++++++++++++++")
        #Logger.info("")
        Client.close()
        return Ret
        
    # Exception
    except Exception: 
        Logger.error("\tException in SSHCmd. Exec cmd[%s : %s] in Agent[%s]" % (Path, Cmd, IP))
        traceback.print_exc()
        return -1
    # No Exception    
    else:
        return 0
    
# 通过sftp下载文件
# 会尝试自动创建本地目录.
def ProcessAgentSSHDownload(IP, Port, User, Password, LocalFile, RemoteFile, Logger):
    try:
        Logger.info("")
        Logger.info("\t#########################################")
        
        # 创建本地文件目录
        Path = os.path.dirname(LocalFile)
        if False == os.path.exists(Path):
                os.makedirs(Path)
                
        Logger.info("\tConnecting to Agent[%s]" % (IP))
        # 创建sftp传输对象
        Transport = paramiko.Transport((IP, Port))
        Transport.connect(username = User, password = Password)
        sftp = paramiko.SFTPClient.from_transport(Transport)
        Logger.info("\tConnecting to Agent[%s] success" % (IP))
        
        # 启动sftp下载
        Logger.info("\tBeginning to download file")
        Logger.info("\tLocal: [%s] << Remote: [%s]:[%s]" %(LocalFile, IP, RemoteFile))
        sftp.get(RemoteFile, LocalFile)
        Logger.info("\tDownload file success")
        Logger.info("\t#########################################")
        #Logger.info("")
        
        Transport.close()
        return 0
        
    # Exception
    except Exception: 
        Logger.error("\tException in sftp download. Local: [%s] << Remote: [%s]:[%s]." % (LocalFile, IP, RemoteFile))
        traceback.print_exc()
        return -1
        
    # No Exception    
    else:
        return 0
        
# upload file through sftp
def ProcessAgentSSHUpload(IP, Port, User, Password, LocalFile, RemoteFile, Logger):
    try:
        Logger.info("")
        Logger.info("\t#########################################")
        
        # 创建远端文件目录
        RemotPath = os.path.dirname(RemoteFile)
        Cmd = "mkdir -p " + RemotPath
        Ret = ProcessAgentSSHCmd(IP, Port, User, Password, "/", Cmd, Logger)
        if 0 > Ret:
            Logger.error("\tMake remote dir[%s] for upload file[%s] failed[%d] in agent[%s]" % (RemotPath, RemoteFile, Ret, IP))
            return -1
        
        Logger.info("\tConnecting to Agent[%s]" % (IP))
        # 创建sftp传输对象
        Transport = paramiko.Transport((IP, Port))
        Transport.connect(username = User, password = Password)
        sftp = paramiko.SFTPClient.from_transport(Transport)
        Logger.info("\tConnecting to Agent[%s] success" % (IP))
        
        # 启动上传
        Logger.info("\tBeginning to upload file")
        Logger.info("\tLocal: [%s] >> Remote: [%s]:[%s]" %(LocalFile, IP, RemoteFile))
        sftp.put(LocalFile, RemoteFile)
        Logger.info("\tUpload file success" )
        Logger.info("\t#########################################")
        #Logger.info("")
        
        Transport.close()
        return 0
        
    # Exception
    except Exception: 
        Logger.error("\tException in sftp upload. Local: [%s] << Remote: [%s]:[%s]." % (LocalFile, IP, RemoteFile))
        traceback.print_exc()
        return -1
        
    # No Exception    
    else:
        return 0

        
# 连接Agent, 并验证Agent可用. 当前只是记录Agent服务器时间
def ProcessAgentPrepareEnv(ConnectInfo, Logger):
    return ProcessAgentSSHCmd(ConnectInfo["IP"], ConnectInfo["Port"], ConnectInfo["User"], ConnectInfo["Password"], "/", "date", Logger)
            
# 完成所有文件上传, 任何一个文件上传失败返回错误.
def ProcessAgentUploadFileList(ConnectInfo, UploadFileList, Logger):
    Ret = 0
    for UploadFile in UploadFileList:
        Ret = ProcessAgentSSHUpload(ConnectInfo["IP"], ConnectInfo["Port"], ConnectInfo["User"], ConnectInfo["Password"], UploadFile["LocalFile"], UploadFile["RemotFile"], Logger)
        if 0 > Ret:
            Logger.error("\tProcessAgentSSHUpload[%s] in Agent[%s] failed[%d]" % (UploadFile["LocalFile"], ConnectInfo["IP"], Ret))
            break
    return Ret

# 完成所有文件下载, 任何一个文件下载失败返回错误.
def ProcessAgentDownloadFileList(ConnectInfo, DownloadFileList, Logger):
    Ret = 0
    for DownloadFile in DownloadFileList:
        Ret = ProcessAgentSSHDownload(ConnectInfo["IP"], ConnectInfo["Port"], ConnectInfo["User"], ConnectInfo["Password"], DownloadFile["LocalFile"], DownloadFile["RemotFile"], Logger)
        if 0 > Ret:
            Logger.error("\tProcessAgentSSHDownload[%s] in Agent[%s] failed[%d]" % (DownloadFile["RemotFile"], ConnectInfo["IP"], Ret))
            break
    return Ret
    
# 完成所有命令执行, 任何一个命令执行失败返回错误.
def ProcessAgentExecCmdList(ConnectInfo, CmdList, Logger):
    Ret = 0
    for Cmd in CmdList:
        Ret = ProcessAgentSSHCmd(ConnectInfo["IP"], ConnectInfo["Port"], ConnectInfo["User"], ConnectInfo["Password"], Cmd["Path"], Cmd["Cmd"], Logger)
        if 0 > Ret:
            Logger.error("\tProcessAgentSSHCmd[%s/%s] in Agent[%s] failed[%d]" % (Cmd["Path"], Cmd["Cmd"], ConnectInfo["IP"], Ret))
            break
    return Ret
    
# 处理一个Agent的所有操作 , 并行模式会影响日志记录的行为
def ProcessAgent(ConnectInfo, UploadFileList, CmdList, DownloadFileList, ManagerLogger, ParallelMode):
    try:
        AgentLoggerIsValid = False
        
        # 创建Agent对应的Log对象, 由ProcessAgent函数使用处理完成后
        # 串行模式下日志记录到文件, 同时打印到终端
        if False == ParallelMode:
            LogToFile   = True
            LogToStream = True
        else:
            LogToFile   = True
            LogToStream = False
        
        LogFileName = "./Log/" + ConnectInfo["IP"] + "/" + ConnectInfo["IP"] +".log"
        Logger = CreateLogger(ConnectInfo["IP"], LogFileName, LogToFile, LogToStream)
        if -1 == Logger:
            ManagerLogger.warning("CreateLogger for Agent[%s] failed" % (ConnectInfo["IP"]))
            return -1
            
        AgentLoggerIsValid = True
        
        # 准备Agent环境.
        Ret = ProcessAgentPrepareEnv(ConnectInfo, Logger)
        if 0 > Ret:
            Logger.error("ProcessAgentPrepareEnv in Agent[%s] failed[%d]" % (ConnectInfo["IP"], Ret))
            DestroyLogger(ConnectInfo["IP"])
            return Ret
        
        # 处理Agent的上传任务.
        Ret = ProcessAgentUploadFileList(ConnectInfo, UploadFileList, Logger)
        if 0 > Ret:
            Logger.error("ProcessAgentUploadFileList in Agent[%s] failed[%d]" % (ConnectInfo["IP"], Ret))
            DestroyLogger(ConnectInfo["IP"])
            return Ret
        
        # 处理Agent的命令列表.
        Ret = ProcessAgentExecCmdList(ConnectInfo, CmdList, Logger)
        if 0 > Ret:
            Logger.error("ProcessAgentExecCmdList in Agent[%s] failed[%d]" % (ConnectInfo["IP"], Ret))
            DestroyLogger(ConnectInfo["IP"])
            return Ret
        
        # 处理Agent的下载任务
        Ret = ProcessAgentDownloadFileList(ConnectInfo, DownloadFileList, Logger)
        if 0 > Ret:
            Logger.error("ProcessAgentDownloadFileList in Agent[%s] failed[%d]" % (ConnectInfo["IP"], Ret))
            DestroyLogger(ConnectInfo["IP"])
            return Ret
            
    # Exception
    except Exception:
        # Agent异常优先记录到Agent Logger, 当Agent Logger不可用时记录到ManagerLogger
        if True == AgentLoggerIsValid :
            Logger.error("Exception in ProcessAgent %s" % (ConnectInfo["IP"]))
        else:
            ManagerLogger.error("Exception in ProcessAgent %s" % (ConnectInfo["IP"]))
        traceback.print_exc()
        DestroyLogger(ConnectInfo["IP"])
        return -1
        
    # No Exception    
    else:
        DestroyLogger(ConnectInfo["IP"])
        return 0
        
    


# 遍历Agent列表, 串行处理每个Agent的所有操作.
def TraverseAgent(AgentList, Logger):
    try:
        # 当前处理的Agent索引
        CurrentAgentIndex = 0
        # 处理成功的Agent个数
        ProcessAgentSucessCnt = 0
        # 遍历Agent列表
        for Agent in AgentList:
            CurrentAgentIndex = CurrentAgentIndex + 1
            Logger.info("-------------------------------")
            Logger.info("Processing Agent: %d / %d" % (CurrentAgentIndex, len(AgentList)))
                
            # 调用Agent处理函数, 串行模式日志默认打印到终端, 同时也会记录到文件
            Ret = ProcessAgent(Agent["ConnectInfo"], Agent["UploadFileList"] ,Agent["CmdList"] ,Agent["DownloadFileList"], Logger, False)
            if 0 > Ret:
                Logger.warning("ProcessAgent[%s] failed [%d]" % (Agent["ConnectInfo"]["IP"], Ret))
                Logger.info("-------------------------------")
                # return [Ret, CurrentAgentIndex, Agent["ConnectInfo"]["IP"]]
                continue
                
            # Agent处理成功
            Logger.info("Process Agent: %d / %d success" % (CurrentAgentIndex, len(AgentList)))
            Logger.info("-------------------------------")
            ProcessAgentSucessCnt = ProcessAgentSucessCnt + 1
            
        return ProcessAgentSucessCnt
            
    # Exception
    except Exception: 
        Logger.error("Exception in TraverseAgent index [%d]" % (CurrentAgentIndex))
        traceback.print_exc()
        
    # No Exception    
    #else:
        
    finally:
        return ProcessAgentSucessCnt
    
# 多线程模式并行处理所有Agent操作
# 待优化, 回收多个任务的处理结果并汇总. 避免agent任务未处理完时主任务退出.
def TraverseAgentThread(AgentList, Logger):
    try:
        # 当前处理的Agent索引
        CurrentAgentIndex = 0
        # 处理成功的Agent个数
        ProcessAgentSucessCnt = 0
        
        # thread list
        #threads = []
            
        # 遍历Agent列表
        for Agent in AgentList:
            CurrentAgentIndex = CurrentAgentIndex + 1
            Logger.info("-------------------------------")
            Logger.info("Processing Agent: %d / %d" % (CurrentAgentIndex, len(AgentList)))
                
            # 调用Agent处理函数, 多线程模式日志不打印到终端, 否则终端输出会很混乱. 日志会记录到文件
            thread = threading.Thread(target=ProcessAgent, args=(Agent["ConnectInfo"], Agent["UploadFileList"] ,Agent["CmdList"] ,Agent["DownloadFileList"], Logger, True))
            #threads.append(thread)
            Ret = thread.start()
            if Ret:
                Logger.warning("Start ProcessAgent thread for [%s] failed [%d]" % (Agent["ConnectInfo"]["IP"], Ret))
                Logger.info("-------------------------------")
                # return [Ret, CurrentAgentIndex, Agent["ConnectInfo"]["IP"]]
                continue
                
            # Agent处理成功
            Logger.info("Process Agent: %d / %d success" % (CurrentAgentIndex, len(AgentList)))
            Logger.info("-------------------------------")
            ProcessAgentSucessCnt = ProcessAgentSucessCnt + 1
        
        thread.join()
        return ProcessAgentSucessCnt
            
    # Exception
    except Exception: 
        Logger.error("Exception in TraverseAgentThread index [%d]" % (CurrentAgentIndex))
        traceback.print_exc()
        
    # No Exception    
    #else:
        
    finally:
        return ProcessAgentSucessCnt

# 读取本地配置文件
def GetAgentCfg(CfgFile, Logger):
    try:
        # 打开配置文件
        fp = file(CfgFile)
        # 读取文件内容, 并按照json格式解析
        JsonData = json.load(fp)
        # 关闭配置文件
        fp.close
        
        return JsonData
        
    # Exception
    except Exception: 
        Logger.error("Exception in GetAgentCfg [%s]" % (CfgFile))
        traceback.print_exc()
        return -1
        
    # No Exception    
    # else: 

# 主入口函数
def main(AgentCfgFile):

    # 1. 创建Log对象,记录管理信息, 同时记录到文件和终端
    ManagerLogger = CreateLogger("Manager", "./Log/ManagerLog.log", True, True)
    if -1 == ManagerLogger:
        print "CreateLogger for Manager failed"
        return -1
    
    # 2. 读取Agent配置文件
    AgentCfg = GetAgentCfg(AgentCfgFile, ManagerLogger)
    if -1 == AgentCfg: 
        ManagerLogger.error("GetAgentCfg failed")
        return -1
    
    # 3. 打印基本配置信息
    # 配置文件的版本信息
    ManagerLogger.info("AgentCfg Ver: %s" % AgentCfg["ver"])
    # 打印Agent 总数
    ManagerLogger.info("Total Agents Number: [%d]" % len(AgentCfg['AgentList']))
    # 以json格式打印所有配置信息, 调试使用
    # ManagerLogger.info("AgentCfg Details: \n%s" % json.dumps(AgentCfg, indent=2))
    
    # 4. 处理所有Agent操作
    if "true" != AgentCfg["ParallelMode"]: 
        ManagerLogger.info("Process Agent In Sequence Mode")
        # 传统的串行处理, 分析所有Agent执行结果.
        Ret = TraverseAgent(AgentCfg["AgentList"], ManagerLogger)
        if Ret != len(AgentCfg['AgentList']): 
            ManagerLogger.error("Process [%d]/[%d] Agents Finished" % (Ret, len(AgentCfg['AgentList'])))
        else:
            ManagerLogger.info("Process [%d]/[%d] Agents Finished" % (Ret, len(AgentCfg['AgentList'])))
    else:
        ManagerLogger.info("Process Agent In Parallel Mode")
        # 每个Agent一个线程, 并行执行, 每个Agent执行结果请查看对应Agent的日志文件.
        Ret = TraverseAgentThread(AgentCfg["AgentList"], ManagerLogger)
        if Ret != len(AgentCfg['AgentList']): 
            ManagerLogger.error("Process [%d]/[%d] Agents Finished" % (Ret, len(AgentCfg['AgentList'])))
        else:
            ManagerLogger.info("Process [%d]/[%d] Agents Finished" % (Ret, len(AgentCfg['AgentList'])))
    
    # 销毁ManagerLogger对象
    DestroyLogger("Manager")
    return 0
    
# ------------------------------------------
# prepare python
# load sys
import sys
reload(sys)
# 设定默认编码为utf8
sys.setdefaultencoding("utf8")
# add path
#sys.path.append('./')

# 使用路径及文件夹相关处理
import os
# 支持异常时打印调用栈
import traceback
# 获取系统时间,Logging模块异常处理时使用
import datetime
# 使用日志处理功能
import logging

# 使用ssh客户端及sftp传输功能
import paramiko
# 使用多任务支持并行处理
import threading
# 支持json格式数据解析
import json

# 主函数入口, 入参为Agent配置文件, 可以添加路径, 默认在当前目录查找.
main("AgentList.cfg")
