
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

void * MainTestTask(void * p)
{
    INT32 iRet = AGENT_OK;
    FlowManager_C * pcFlowManager = (FlowManager_C *)p;
    
    ServerFlowKey_S stNewServerFlowKey;  
    sal_memset(&stNewServerFlowKey, 0, sizeof(ServerFlowKey_S));

    stNewServerFlowKey.eProtocol = AGENT_DETECT_PROTOCOL_UDP;
    stNewServerFlowKey.uiSrcIP  = sal_inet_aton("172.25.3.15");
    stNewServerFlowKey.uiDestIP = sal_inet_aton("172.25.3.16");
    stNewServerFlowKey.uiSrcPortMin = 32769;
    stNewServerFlowKey.uiSrcPortMax = 32868;
    stNewServerFlowKey.uiSrcPortRange = 16;
    stNewServerFlowKey.uiDscp = 10;
    stNewServerFlowKey.uiUrgentFlow = AGENT_TRUE;
    
    stNewServerFlowKey.stServerTopo.uiLevel = 1;
    stNewServerFlowKey.stServerTopo.uiSvid = 9;
    stNewServerFlowKey.stServerTopo.uiDvid = 11;

    
    
    do
    {
        // ģ��Server�·�����̽����
        #if 0
        iRet = pcFlowManager->ServerWorkingFlowTableAdd(stNewServerFlowKey);
        if (iRet)        
            INIT_ERROR("FlowManager.ServerWorkingFlowTableAdd failed [%d]", iRet);
        #endif

        // ģ����ServerAntServer����Server
        #if 0
        iRet = RequestProbeListFromServer(pcFlowManager);
        if (iRet)        
            INIT_ERROR("RequestProbeListFromServer failed [%d]", iRet);
        #endif
        sal_sleep(120);
        
    } while(1);
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
    //INIT_INFO("-------- GetLocalCfg --------");
    iRet = GetLocalCfg(pcCfg);
    if (iRet)
    {
        if (pcCfg)
            delete pcCfg;
        pcCfg = NULL;
        
        INIT_ERROR("GetLocalCfg failed [%d]", iRet);
        return iRet;
    }

    // ��Serverע��Agent
    //INIT_INFO("-------- ReportToServer --------");
    iRet = ReportToServer(pcCfg);
    if (iRet)
    {
        if (pcCfg)
            delete pcCfg;
        pcCfg = NULL;
        
        INIT_ERROR("ReportToServer failed [%d]", iRet);
        return iRet;
    }

    // ��ȡServer��������Ϣ
    //INIT_INFO("-------- GetCfgFromServer --------");
    iRet = GetCfgFromServer(pcCfg);
    if (iRet)
    {
        if (pcCfg)
            delete pcCfg;
        pcCfg = NULL;
        
        INIT_ERROR("GetCfgFromServer failed [%d]", iRet);
        return iRet;
    }


    // ����MessagePlatformServer�˷���, ������Ӧ�ⲿ������Ϣ.
    // INIT_INFO("-------- Start MessagePlatformServer --------");
    UINT32 uiPort = 0;
    iRet = pcCfg->GetAgentAddress(NULL, &uiPort);
    if (iRet)
    {
        if (pcFlowManager)
            delete pcFlowManager;
        pcFlowManager = NULL;
        if (pcCfg)
            delete pcCfg;
        pcCfg = NULL;
        
        INIT_ERROR("GetAgentAddress  failed [%d]", iRet);
        return iRet;
    }
	
    pcFlowManager = new FlowManager_C;
    MessagePlatformServer_C * pcMsgServer = new MessagePlatformServer_C;
    iRet = pcMsgServer->Init(uiPort, pcFlowManager);
    if (iRet)
    {
        if (pcFlowManager)
            delete pcFlowManager;
        pcFlowManager = NULL;
        if (pcCfg)
            delete pcCfg;
        pcCfg = NULL;
        INIT_ERROR("Init MessagePlatformServer_C  failed [%d]", iRet);
    }

	INIT_INFO("Begin to report agent ip to server");
	iRet = ReportAgentIPToServer(pcCfg);
	int reportCount = 0;
	while (iRet)
	{
	    INIT_ERROR("Report Agent ip to Server fail[%d]", iRet);
		sleep(5);
		INIT_ERROR("Retry to report Agent ip to Server, time [%d]", reportCount + 1);
		iRet = ReportAgentIPToServer(pcCfg);
		reportCount++;
	}

	if (AGENT_OK == iRet)
	{
	    SHOULD_PROBE = 1;
		sleep(10);
	}

	// ����FlowManager����
    INIT_INFO("-------- Start FlowManager --------");
	iRet = pcFlowManager->Init(pcCfg);
    if (iRet)
    {
        if (pcCfg)
            delete pcCfg;
        pcCfg = NULL;
        
        INIT_ERROR("FlowManager.init failed [%d]", iRet);
        return iRet;
    }
	// ���ж����Ѿ��������, ��ʼ����.
    INIT_INFO("-------- Starting ServerAntAgent Complete --------");
	
	

	
#if 0
    pthread_t thread;
    INT32 error;
    error = pthread_create(&thread, NULL, MainTestTask, pcFlowManager);
    if(error)
    {
          INIT_ERROR("Create Thread failed[%d]: %s [%d]", iRet, strerror(errno), errno); 
    }
    sal_sleep(2);
#endif
    
    while(1)
    {
        sal_sleep(10);
        // δ���������������.
    }

    INIT_INFO("-------- Stopping ServerAntAgent Now --------");

    if (pcMsgServer)
        delete pcMsgServer;
    pcMsgServer = NULL;
    if (pcFlowManager)
        delete pcFlowManager;
    pcFlowManager = NULL;
    if (pcCfg)
        delete pcCfg;
    pcCfg = NULL;
    INIT_INFO("-------- ServerAntAgent Exit Now --------");

    return AGENT_OK;
}

INT32 SHOULD_PROBE = 0;

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

