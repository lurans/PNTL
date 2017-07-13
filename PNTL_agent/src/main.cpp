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

// ����ServerAntAgentҵ��
INT32 ServerAntAgent()
{
    INT32 iRet = 0;
    FlowManager_C * pcFlowManager = NULL;

    INIT_INFO("-------- Starting ServerAntAgent Now --------");

    // ����ServerAntAgentCfg����, ���ڱ���agent������Ϣ
    ServerAntAgentCfg_C * pcCfg = new ServerAntAgentCfg_C;

    // ��ȡ����������Ϣ
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

    // ����FlowManager����
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

    // ����MessagePlatformServer�˷���, ������Ӧ�ⲿ������Ϣ.
    INIT_INFO("-------- Start MessagePlatformServer --------");
    MessagePlatformServer_C * pcMsgServer = new MessagePlatformServer_C;
    iRet = pcMsgServer->Init(uiPort, pcFlowManager);
    if (iRet)
    {
        destroyFlowManagerObj(pcFlowManager);
        destroyServerCfgObj(pcCfg);
        delete pcMsgServer;
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

    if (AGENT_OK == iRet)
    {
        UINT32 delayTime = 10 + rand() % 30;
        INIT_INFO("Query pingList will be in [%u] seconds.", delayTime);
        sleep(delayTime);
        SHOULD_PROBE = 1;
    }

    // ���ж����Ѿ��������, ��ʼ����.
    INIT_INFO("-------- Starting ServerAntAgent Complete --------");

    while(1)
    {
        sal_sleep(10);
        // δ���������������.
    }

    INIT_INFO("-------- Stopping ServerAntAgent Now --------");

    if (pcMsgServer)
        delete pcMsgServer;
    pcMsgServer = NULL;

    destroyFlowManagerObj(pcFlowManager);
    destroyServerCfgObj(pcCfg);

    INIT_INFO("-------- ServerAntAgent Exit Now --------");

    return AGENT_OK;
}

INT32 SHOULD_PROBE = 0;
INT32 SEND_BIG_PKG = 0;
INT32 CLEAR_BIG_PKG = 0;

// �������, Ĭ��ֱ������.
// ��������ʱֱ������
// -d ��Ϊ�ػ���������
INT32 main (INT32 argc, char **argv)
{
    INT32 iRet = AGENT_OK;
    // �������ģʽ
    INT32 iStartAsDaemon = AGENT_FALSE;

    pid_t pid;

    SetNewLogMode(AGENT_LOG_MODE_NORMAL);

    // ��������
    if ( 2 <= argc)
    {
        INT32 iIndex = 0;
        string strTemp ;
        for (iIndex = 1; iIndex < argc; iIndex++ )
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
        // ���ػ����̵���ʽ����
        SetNewLogMode(AGENT_LOG_MODE_DAEMON);
        INIT_INFO("-------- StartAsDaemon --------");

        // 1. �����ӽ���
        pid = fork();
        if (0 > pid)
        {
            INIT_ERROR("-------- fork error --------");
            exit(-1);
        }
        else if (0 < pid)
        {
            // �����̴���, ֱ���˳�
            exit(0);
        }
        else
        {
            // �ӽ��̴���

            // 2. �ӽ��̴����»Ự, �����ܸ����̻ỰӰ��.
            setsid();

            // 3. �ı��ӽ��̹���Ŀ¼Ϊָ��Ŀ¼
            // chdir("/");

            // 4. �����ļ�Ȩ������(�����ļ�ʱ��Ĭ��Ȩ��).
            //umask(0);

            //5. ����2s, �������ű�����cgroup����Ϣ.
            sleep(2);

            // 6. �����ػ�ҵ��.
            iRet = ServerAntAgent();

            // 7. ҵ�������,׼���˳�.

        }
    }
    else
    {
        // ֱ������
        iRet = ServerAntAgent();
    }

    if(iRet)
        exit(-1);
    else
        exit(0);
}

