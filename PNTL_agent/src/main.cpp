#include <stdlib.h>
#include <errno.h>
#include <pthread.h>

using namespace std;
#include "Log.h"
#include "GetLocalCfg.h"
#include "ServerAntAgentCfg.h"
#include "MessagePlatform.h"
#include "MessagePlatformServer.h"
#include "FlowManager.h"
#include "MessagePlatformClient.h"
#include "AgentCommon.h"

void destroyServerCfgObj(ServerAntAgentCfg_C * pcCfg)
{
     if (NULL != pcCfg)
     {
        delete pcCfg;
     }
}

void destroyFlowManagerObj(FlowManager_C * pcFlowManager)
{
     if (NULL != pcFlowManager)
     {
        delete pcFlowManager;
     }
}

void destroyMessagePlatformServer(MessagePlatformServer_C * pcMsgServer)
{
    if (NULL != pcMsgServer)
     {
        delete pcMsgServer;
     }
}

// 启动ServerAntAgent业务
INT32 ServerAntAgent()
{
    INT32 iRet = 0;
    FlowManager_C * pcFlowManager = NULL;

    INIT_INFO("-------- Starting ServerAntAgent Now --------");

    // 创建ServerAntAgentCfg对象, 用于保存agent配置信息
    ServerAntAgentCfg_C * pcCfg = new ServerAntAgentCfg_C;

    // 获取本地配置信息
    INIT_INFO("-------- GetLocalCfg --------");
    iRet = GetLocalCfg(pcCfg);
    if (iRet)
    {
        destroyServerCfgObj(pcCfg);
        INIT_ERROR("GetLocalCfg failed [%d]", iRet);
        return iRet;
    }
    
    UINT32 uiPort = 0;
    iRet = pcCfg->GetAgentAddress(NULL, &uiPort);
    if (iRet)
    {
        destroyServerCfgObj(pcCfg);

        INIT_ERROR("GetAgentAddress  failed [%d]", iRet);
        return iRet;
    }

    // 启动FlowManager对象
    INIT_INFO("-------- Start FlowManager --------");
    pcFlowManager = new FlowManager_C;
    iRet = pcFlowManager->Init(pcCfg);
    if (iRet)
    {
        destroyFlowManagerObj(pcFlowManager);
        destroyServerCfgObj(pcCfg);
        INIT_ERROR("FlowManager.init failed [%d]", iRet);
        return iRet;
    }

    // 启动MessagePlatformServer端服务, 用于响应外部推送消息.
    INIT_INFO("-------- Start MessagePlatformServer --------");
    MessagePlatformServer_C * pcMsgServer = new MessagePlatformServer_C;
    iRet = pcMsgServer->Init(uiPort, pcFlowManager);
    if (iRet)
    {
        destroyFlowManagerObj(pcFlowManager);
        destroyServerCfgObj(pcCfg);
        destroyMessagePlatformServer(pcMsgServer);
        INIT_ERROR("Init MessagePlatformServer_C  failed [%d]", iRet);
        return iRet;
    }

    iRet = ReportAgentIPToServer(pcCfg);
    int reportCount = 1;
    while (iRet)
    {
        INIT_ERROR("Report Agent ip to Server fail[%d]", iRet);
        sleep(5);
        INIT_ERROR("Retry to report Agent ip to Server, time [%d]", ++reportCount);
        iRet = ReportAgentIPToServer(pcCfg);
    }

    // 所有对象已经启动完成, 开始工作.
    INIT_INFO("-------- Starting ServerAntAgent Complete --------");

    while(1)
    {
        sal_sleep(10);
        // 未来考虑诊断命令行.
    }

    INIT_INFO("-------- Stopping ServerAntAgent Now --------");

    destroyFlowManagerObj(pcFlowManager);
    destroyServerCfgObj(pcCfg);
	destroyMessagePlatformServer(pcMsgServer);

    INIT_INFO("-------- ServerAntAgent Exit Now --------");

    return AGENT_OK;
}

INT32 SHOULD_PROBE = 0;
INT32 SEND_BIG_PKG = 0;
INT32 CLEAR_BIG_PKG = 0;

// 程序入口, 默认直接启动.
// 不带参数时直接启动
// -d 作为守护进程启动
INT32 main (INT32 argc, char **argv)
{
    INT32 iRet = AGENT_OK;
    // 软件启动模式
    INT32 iStartAsDaemon = AGENT_FALSE;

    pid_t pid;

    SetNewLogMode(AGENT_LOG_MODE_NORMAL);

    // 参数解析
    if ( 2 <= argc)
    {
        string strTemp ;
        for (INT32 iIndex = 1; iIndex < argc; iIndex++ )
        {
            strTemp = argv[iIndex];
            if ( "-d" == strTemp )
            {
                iStartAsDaemon = AGENT_TRUE;
            }
            else
            {
                INIT_ERROR("-------- Unknown arg[%s] --------", strTemp.c_str());
                exit(-1);
            }
        }
    }

    if (iStartAsDaemon)
    {
        // 以守护进程的形式启动
        SetNewLogMode(AGENT_LOG_MODE_DAEMON);
        INIT_INFO("-------- StartAsDaemon --------");

        // 1. 创建子进程
        pid = fork();
        if (0 > pid)
        {
            INIT_ERROR("-------- fork error --------");
            exit(-1);
        }
        else if (0 < pid)
        {
            // 父进程处理, 直接退出
            exit(0);
        }
        else
        {
            // 子进程处理

            // 2. 子进程创建新会话, 不再受父进程会话影响.
            setsid();

            // 3. 改变子进程工作目录为指定目录
            // chdir("/");

            // 4. 重设文件权限掩码(创建文件时的默认权限).
            //umask(0);

            //5. 休眠2s, 等启动脚本配置cgroup等信息.
            sleep(2);

            // 6. 启动守护业务.
            iRet = ServerAntAgent();

            // 7. 业务处理完成,准备退出.

        }
    }
    else
    {
        // 直接启动
        iRet = ServerAntAgent();
    }

    if(iRet)
        exit(-1);
    else
        exit(0);
}

