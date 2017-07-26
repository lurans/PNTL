#include <stdlib.h>
#include <errno.h>
#include <pthread.h>

using namespace std;
#include "Log.h"
#include "GetLocalCfg.h"
#include "ServerAntAgentCfg.h"
#include "MessagePlatform.h"
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
    UINT32 uiPort = 0;
    FlowManager_C * pcFlowManager = NULL;

    INIT_INFO("-------- Starting ServerAntAgent Now --------");

    // ����ServerAntAgentCfg����, ���ڱ���agent������Ϣ
    ServerAntAgentCfg_C * pcCfg = new ServerAntAgentCfg_C;
    if (NULL == pcCfg)
    {
        return AGENT_E_MEMORY;
    }

    // ��ȡ����������Ϣ
    INIT_INFO("-------- GetLocalCfg --------");
    iRet = GetLocalCfg(pcCfg);
    if (AGENT_OK != iRet)
    {
        destroyServerCfgObj(pcCfg);
        INIT_ERROR("GetLocalCfg failed [%d]", iRet);
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

    // ���ж����Ѿ��������, ��ʼ����.
    INIT_INFO("-------- Starting ServerAntAgent Complete --------");
    
	iRet = ReportAgentIPToServer(pcCfg);
    UINT32 reportCount = 1;
    while (iRet)
    {
        INIT_ERROR("Report Agent ip to Server fail[%d]", iRet);
        sleep(5);
        INIT_ERROR("Retry to report Agent ip to Server, time [%u]", ++reportCount);
        iRet = ReportAgentIPToServer(pcCfg);
    }

    iRet = RequestConfigFromServer(pcFlowManager);
	reportCount = 1;
	while (iRet)
    {
        INIT_ERROR("Request Agent config from Server fail[%d]", iRet);
        sleep(5);
        INIT_ERROR("Retry to request Agent config from Server, time [%u]", ++reportCount);
        iRet = RequestConfigFromServer(pcFlowManager);
    }

    while(1)
    {
        sal_sleep(10);
        // δ���������������.
    }

    INIT_INFO("-------- Stopping ServerAntAgent Now --------");

    destroyFlowManagerObj(pcFlowManager);
    destroyServerCfgObj(pcCfg);

    INIT_INFO("-------- ServerAntAgent Exit Now --------");

    return AGENT_OK;
}

UINT32 BIG_PKG_RATE = 0;
UINT32 SHOULD_PROBE = 0;

UINT32 SHOULD_QUERY_CONF = 0;

UINT32 SHOULD_REPORT_IP = 0;

UINT32 PROBE_INTERVAL = 9999;

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

