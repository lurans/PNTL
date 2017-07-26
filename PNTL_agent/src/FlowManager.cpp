
#include <math.h>       // �����׼��
#include <algorithm>    // ��������
#include <sys/time.h>   // ��ȡʱ��
#include <sstream>
#include <assert.h>
using namespace std;

#include "Log.h"
#include "AgentJsonAPI.h"
#include "MessagePlatformClient.h"
#include "FlowManager.h"
#include "AgentCommon.h"


// ��ʹ��ԭ��: ����������ServerFlowTableˢ�µ�AgentFlowTable.
// ���Ҫͬʱʹ��������, �����Ȼ�ȡSERVER_WORKING_FLOW_TABLE_LOCK()�ٻ�ȡAGENT_WORKING_FLOW_TABLE_LOCK,
// �ͷ�ʱ���ͷ�AGENT_WORKING_FLOW_TABLE_UNLOCK(),���ͷ�SERVER_WORKING_FLOW_TABLE_UNLOCK().

// Agent Flow Table
#define AGENT_WORKING_FLOW_TABLE_LOCK() \
        if (stAgentFlowTableLock) \
            sal_mutex_take(stAgentFlowTableLock, sal_mutex_FOREVER)

#define AGENT_WORKING_FLOW_TABLE_UNLOCK() \
        if (stAgentFlowTableLock) \
            sal_mutex_give(stAgentFlowTableLock)

#define AGENT_WORKING_FLOW_TABLE  (uiAgentWorkingFlowTable)


#define SERVER_WORKING_FLOW_TABLE  (uiServerWorkingFlowTable)


//Agent Flow Table Entry��uiFlowState bit����
// ��ǰEntry�Ƿ���Ч.
#define FLOW_ENTRY_STATE_ENABLE     (1L << 0)
// ��ǰEntry�Ƿ���׷��ģʽ, �ɶ�������.
#define FLOW_ENTRY_STATE_TRACKING   (1L << 1)
// ��ǰEntry�Ƿ��ڶ���ģʽ, �ɶ�������.
#define FLOW_ENTRY_STATE_DROPPING   (1L << 2)

#define FLOW_ENTRY_STATE_CHECK(state, flag)     ( (state) & (flag) )
#define FLOW_ENTRY_STATE_SET(state, flag)       ( (state) = ((state)|(flag)) )
#define FLOW_ENTRY_STATE_CLEAR(state, flag)     ( (state) = ((state)&(~(flag))) )


// ���캯��, ���г�Ա��ʼ��Ĭ��ֵ.
FlowManager_C::FlowManager_C()
{
    FLOW_MANAGER_INFO("Creat a new FlowManager");

    pcAgentCfg = NULL;

    // Worker ��ʼ��
    WorkerList_UDP =NULL;

    // ������
    uiAgentWorkingFlowTable = 0;
    stAgentFlowTableLock = NULL;
    AgentClearFlowTable();

    uiServerWorkingFlowTable = 0;
    stServerFlowTableLock = NULL;
    ServerClearFlowTable();

    // ҵ�����̴���
    uiNeedCheckResult = 0;
    uiLastCheckTimeCounter = 0;
    uiLastReportTimeCounter = 0;
    uiLastQuerytTimeCounter = 0;

}

// ��������,�ͷ���Դ
FlowManager_C::~FlowManager_C()
{
    FLOW_MANAGER_INFO("Destroy an old FlowManager");

    StopThread();

    // �������
    AgentClearFlowTable();
    ServerClearFlowTable();

    // �ͷŻ�����
    if ( stAgentFlowTableLock )
    {
        sal_mutex_destroy(stAgentFlowTableLock);
    }
    stAgentFlowTableLock = NULL;


    if (WorkerList_UDP)
    {
        delete WorkerList_UDP;
    }
    WorkerList_UDP = NULL;

}

INT32 FlowManager_C::Init(ServerAntAgentCfg_C * pcNewAgentCfg)
{
    INT32 iRet = AGENT_OK;

    // UDP Э���ʼ��
    UINT32 uiSrcPortMin = 0;
    UINT32 uiSrcPortMax = 0;
    UINT32 uiDestPort = 0;
    WorkerCfg_S stNewWorker;

    if(NULL == pcNewAgentCfg)
    {
        FLOW_MANAGER_ERROR("Null Point.");
        return AGENT_E_PARA;
    }

    if(NULL != pcAgentCfg)
    {
        FLOW_MANAGER_ERROR("Do not reinit this FlowManager.");
        return AGENT_E_PARA;
    }

    // �����ʼ��
    pcAgentCfg = pcNewAgentCfg;

    stAgentFlowTableLock = sal_mutex_create("Flow Manager FlowTableLock");
    if( NULL == stAgentFlowTableLock )
    {
        FLOW_MANAGER_ERROR("Create mutex failed");
        return AGENT_E_MEMORY;
    }

    pcAgentCfg->GetProtocolUDP(&uiSrcPortMin, &uiSrcPortMax, &uiDestPort);

    sal_memset(&stNewWorker, 0, sizeof(stNewWorker));
    stNewWorker.eProtocol  = AGENT_DETECT_PROTOCOL_UDP;
    stNewWorker.uiSrcPort   = uiSrcPortMin;
    stNewWorker.uiSrcIP     = SAL_INADDR_ANY;

    WorkerList_UDP = new DetectWorker_C;
    if( NULL == WorkerList_UDP )
    {
        return AGENT_E_MEMORY;
    }

    iRet = WorkerList_UDP->Init(stNewWorker, pcNewAgentCfg);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Init UDP target worker failed[%d], SIP[%s],SPort[%d]",
                           iRet, sal_inet_ntoa(stNewWorker.uiSrcIP), stNewWorker.uiSrcPort);
        return AGENT_E_PARA;
    }

    // ������������
    iRet = StartThread();
    if (iRet)
    {
        FLOW_MANAGER_ERROR("StartThread failed[%d]", iRet);
        return AGENT_E_PARA;
    }
    FLOW_MANAGER_INFO("init flow manager success");
    return iRet;
}

// Agent�������
// ����ض�����
INT32 FlowManager_C::AgentClearFlowTable()
{

    // ���ÿ�����еĽ����.
    vector<AgentFlowTableEntry_S>::iterator pAgentFlowEntry;
    for(pAgentFlowEntry = AgentFlowTable.begin();
            pAgentFlowEntry != AgentFlowTable.end();
            pAgentFlowEntry ++)
    {
        pAgentFlowEntry->vFlowDetectResultPkt.clear();
    }

    // �����������.
    AgentFlowTable.clear();

    return AGENT_OK;
}


// ��AgentFlowTable�����Entry
void FlowManager_C::AgentFlowTableAdd(ServerFlowTableEntry_S * pstServerFlowEntry)
{
    UINT32 uiSrcPort    = 0;
    UINT32 uiAgentIndexCounter    = 0;
    AgentFlowTableEntry_S stNewAgentEntry;


    // stNewAgentEntry�а���C++��, ����ֱ��ʹ��sal_memset�����ʼ��. δ�������ع��ɶ���.
    sal_memset(&(stNewAgentEntry.stFlowKey), 0, sizeof(stNewAgentEntry.stFlowKey));
    stNewAgentEntry.uiFlowState = 0;

    sal_memset(&(stNewAgentEntry.stFlowDetectResult), 0, sizeof(stNewAgentEntry.stFlowDetectResult));
    stNewAgentEntry.vFlowDetectResultPkt.clear();
    stNewAgentEntry.uiFlowDropCounter = 0;
    stNewAgentEntry.uiFlowTrackingCounter= 0;

    // ˢ��key��Ϣ
    stNewAgentEntry.stFlowKey.uiUrgentFlow = pstServerFlowEntry->stServerFlowKey.uiUrgentFlow;
    stNewAgentEntry.stFlowKey.eProtocol = pstServerFlowEntry->stServerFlowKey.eProtocol;
    stNewAgentEntry.stFlowKey.uiSrcIP = pstServerFlowEntry->stServerFlowKey.uiSrcIP;
    stNewAgentEntry.stFlowKey.uiDestIP = pstServerFlowEntry->stServerFlowKey.uiDestIP;
    stNewAgentEntry.stFlowKey.uiDestPort = pstServerFlowEntry->stServerFlowKey.uiDestPort;
    stNewAgentEntry.stFlowKey.uiDscp = pstServerFlowEntry->stServerFlowKey.uiDscp;
    stNewAgentEntry.stFlowKey.stServerTopo = pstServerFlowEntry->stServerFlowKey.stServerTopo;
    stNewAgentEntry.stFlowKey.uiIsBigPkg = 0;

    // ˢ��������Ϣ
    stNewAgentEntry.stFlowKey.uiAgentFlowTableIndex = AgentFlowTable.size();

    // ˢ��Server Entry��Agent����.
    pstServerFlowEntry->uiAgentFlowIndexMin = stNewAgentEntry.stFlowKey.uiAgentFlowTableIndex;
    pstServerFlowEntry->uiAgentFlowWorkingIndexMin = stNewAgentEntry.stFlowKey.uiAgentFlowTableIndex;
    pstServerFlowEntry->uiAgentFlowWorkingIndexMax = pstServerFlowEntry->uiAgentFlowWorkingIndexMin
            + pstServerFlowEntry->stServerFlowKey.uiSrcPortRange - 1;

    for (uiSrcPort = pstServerFlowEntry->stServerFlowKey.uiSrcPortMin; uiSrcPort <= pstServerFlowEntry->stServerFlowKey.uiSrcPortMax; uiSrcPort++)
    {
        stNewAgentEntry.stFlowKey.uiSrcPort = uiSrcPort;

        // Ĭ�ϴ�uiSrcPortRange����
        if (uiAgentIndexCounter < (pstServerFlowEntry->stServerFlowKey.uiSrcPortRange))
        {
            FLOW_ENTRY_STATE_SET(stNewAgentEntry.uiFlowState, FLOW_ENTRY_STATE_ENABLE);
        }
        else
        {
            FLOW_ENTRY_STATE_CLEAR(stNewAgentEntry.uiFlowState, FLOW_ENTRY_STATE_ENABLE);
        }

        AgentFlowTable.push_back(stNewAgentEntry);
        stNewAgentEntry.stFlowKey.uiAgentFlowTableIndex ++ ;
        uiAgentIndexCounter ++ ;
    }
    pstServerFlowEntry->uiAgentFlowIndexMax = stNewAgentEntry.stFlowKey.uiAgentFlowTableIndex - 1;

    return;
}

// ����ض�AgentFlow��̽����
void FlowManager_C::AgentFlowTableEntryClearResult(UINT32 uiAgentFlowIndex)
{
    AGENT_WORKING_FLOW_TABLE_LOCK();
    AgentFlowTable[uiAgentFlowIndex].uiFlowState = 0;
    AgentFlowTable[uiAgentFlowIndex].vFlowDetectResultPkt.clear();
    AgentFlowTable[uiAgentFlowIndex].uiFlowDropCounter = 0;
    AgentFlowTable[uiAgentFlowIndex].uiFlowTrackingCounter = 0;

    sal_memset(&(AgentFlowTable[uiAgentFlowIndex].stFlowDetectResult), 0, sizeof(DetectResult_S));
    AGENT_WORKING_FLOW_TABLE_UNLOCK();

    return;
}

// ����range������һ���ϱ����ڴ���Щ��.
INT32 FlowManager_C::AgentFlowTableEntryAdjust()
{

    INT32 iRet = AGENT_OK;
    UINT32 uiAgentFlowIndex = 0;
    UINT32 uiSrcPortRange = 0;

    // ��������ServerFlowTable
    vector<ServerFlowTableEntry_S>::iterator pServerEntry;
    for(pServerEntry = ServerFlowTable.begin();
            pServerEntry != ServerFlowTable.end();
            pServerEntry++)
    {

        // �رձ�ServerFlow��Ӧ�����е�AgentFlow, ������ն�Ӧ��ͳ�ƺ�״̬
        for ( uiAgentFlowIndex = pServerEntry->uiAgentFlowIndexMin;
                uiAgentFlowIndex <= pServerEntry->uiAgentFlowIndexMax;
                uiAgentFlowIndex ++)
        {
            AgentFlowTableEntryClearResult(uiAgentFlowIndex);
        }

        // ����range������һ��̽���AgentFlow
        if (0 != pcAgentCfg->GetPortCount())
        {
            uiSrcPortRange = pcAgentCfg->GetPortCount();
        }
        else
        {
            uiSrcPortRange = pServerEntry->stServerFlowKey.uiSrcPortRange;
        }
		
        if ( pServerEntry->uiAgentFlowWorkingIndexMax + uiSrcPortRange <= pServerEntry->uiAgentFlowIndexMax)
        {
            pServerEntry->uiAgentFlowWorkingIndexMin = pServerEntry->uiAgentFlowWorkingIndexMax + 1;
            pServerEntry->uiAgentFlowWorkingIndexMax = pServerEntry->uiAgentFlowWorkingIndexMax + uiSrcPortRange;

            // ����һ����Ҫ̽���AgentFlow
            for ( uiAgentFlowIndex = pServerEntry->uiAgentFlowWorkingIndexMin;
                    uiAgentFlowIndex <= pServerEntry->uiAgentFlowWorkingIndexMax;
                    uiAgentFlowIndex ++)
            {
                FLOW_ENTRY_STATE_SET(AgentFlowTable[uiAgentFlowIndex].uiFlowState, FLOW_ENTRY_STATE_ENABLE);
            }
        }
        else
        {
            if (pServerEntry->uiAgentFlowWorkingIndexMax + 1 <= pServerEntry->uiAgentFlowIndexMax)
            {
                pServerEntry->uiAgentFlowWorkingIndexMin = pServerEntry->uiAgentFlowWorkingIndexMax + 1;
                pServerEntry->uiAgentFlowWorkingIndexMax = pServerEntry->uiAgentFlowIndexMin +
                        pServerEntry->uiAgentFlowWorkingIndexMax + uiSrcPortRange - pServerEntry->uiAgentFlowIndexMax - 1;

                // ����һ����Ҫ̽���AgentFlow
                for ( uiAgentFlowIndex = pServerEntry->uiAgentFlowWorkingIndexMin;
                        uiAgentFlowIndex <= pServerEntry->uiAgentFlowIndexMax;
                        uiAgentFlowIndex ++)
                {
                    FLOW_ENTRY_STATE_SET(AgentFlowTable[uiAgentFlowIndex].uiFlowState, FLOW_ENTRY_STATE_ENABLE);
                }
                for ( uiAgentFlowIndex = pServerEntry->uiAgentFlowIndexMin;
                        uiAgentFlowIndex <= pServerEntry->uiAgentFlowWorkingIndexMax;
                        uiAgentFlowIndex ++)
                {
                    FLOW_ENTRY_STATE_SET(AgentFlowTable[uiAgentFlowIndex].uiFlowState,  FLOW_ENTRY_STATE_ENABLE);
                }

            }
            else
            {
                pServerEntry->uiAgentFlowWorkingIndexMin = pServerEntry->uiAgentFlowIndexMin;
                pServerEntry->uiAgentFlowWorkingIndexMax = pServerEntry->uiAgentFlowIndexMin + uiSrcPortRange - 1;
                // ����һ����Ҫ̽���AgentFlow
                for ( uiAgentFlowIndex = pServerEntry->uiAgentFlowWorkingIndexMin;
                        uiAgentFlowIndex <= pServerEntry->uiAgentFlowWorkingIndexMax;
                        uiAgentFlowIndex ++)
                {
                    FLOW_ENTRY_STATE_SET(AgentFlowTable[uiAgentFlowIndex].uiFlowState,
                                         FLOW_ENTRY_STATE_ENABLE);
                }
            }
        }
    }
    return AGENT_OK;
}

// Server�������
// ����ض�����
INT32 FlowManager_C::ServerClearFlowTable()
{
    // �����������.
    ServerFlowTable.clear();

    return AGENT_OK;
}


// ��ServerFlowTable�����Entryǰ��Ԥ����, ������μ�鼰������ʼ��
INT32 FlowManager_C::ServerFlowTablePreAdd(ServerFlowKey_S * pstNewServerFlowKey, ServerFlowTableEntry_S * pstNewServerFlowEntry)
{
    INT32 iRet = AGENT_OK;
    UINT32 uiSrcPortMin = 0;
    UINT32 uiSrcPortMax = 0;
    UINT32 uiAgentSrcPortRange = 0;
    UINT32 uiDestPort = 0;

    sal_memset(pstNewServerFlowEntry, 0, sizeof(ServerFlowTableEntry_S));

    if (AGENT_DETECT_PROTOCOL_UDP == pstNewServerFlowKey->eProtocol)
    {
        // ��ȡ��ǰAgentȫ��Դ�˿ڷ�Χ.
        pcAgentCfg->GetProtocolUDP(&uiSrcPortMin, &uiSrcPortMax, &uiDestPort);

        // ���Դ�˿ں�Ĭ��ֵ

        if (0 == pstNewServerFlowKey->uiSrcPortMin)
        {
            FLOW_MANAGER_INFO("SrcPortMin is 0, Using Default SrcPortMin[%u].",
                              uiSrcPortMin);
            pstNewServerFlowKey->uiSrcPortMin = uiSrcPortMin;
        }

        if (0 == pstNewServerFlowKey->uiSrcPortMax)
        {
            FLOW_MANAGER_INFO("SrcPortMax is 0, Using Default SrcPortMin[%u].",
                              uiSrcPortMax);
            pstNewServerFlowKey->uiSrcPortMax = uiSrcPortMax;
        }

        pstNewServerFlowKey->uiDestPort = uiDestPort;
        FLOW_MANAGER_INFO("uiDestPort is 0, Using Default uiDestPort[%u]++++++++++++++++++++++++++++=.",uiDestPort);

        uiAgentSrcPortRange = pstNewServerFlowKey->uiSrcPortMax - pstNewServerFlowKey->uiSrcPortMin + 1;

        if (0 == pstNewServerFlowKey->uiSrcPortRange)
        {
            FLOW_MANAGER_INFO("SrcPortRange is 0, Using Default SrcPortMin[%u]: [%u]-[%u].",
                              uiAgentSrcPortRange, pstNewServerFlowKey->uiSrcPortMin, pstNewServerFlowKey->uiSrcPortMax);
            pstNewServerFlowKey->uiSrcPortRange = uiAgentSrcPortRange;
        }

        // ��μ��
        if (pstNewServerFlowKey->uiSrcPortMin > pstNewServerFlowKey->uiSrcPortMax)

        {
            FLOW_MANAGER_ERROR("SrcPortMin[%u] is bigger than SrcPortMax[%u]",
                               pstNewServerFlowKey->uiSrcPortMin, pstNewServerFlowKey->uiSrcPortMax);
            return AGENT_E_PARA;
        }

        if (  (uiSrcPortMin > pstNewServerFlowKey->uiSrcPortMin)
                ||(uiSrcPortMax < pstNewServerFlowKey->uiSrcPortMax))
        {
            FLOW_MANAGER_ERROR("SrcPortMin[%u] - SrcPortMax[%u] from server is too larger than this agent: SrcPortMin[%u] - SrcPortMax[%u]",
                               pstNewServerFlowKey->uiSrcPortMin, pstNewServerFlowKey->uiSrcPortMax, uiSrcPortMin, uiSrcPortMax);
            return AGENT_E_PARA;
        }
		
        // ���Range��Χ
        if ( uiAgentSrcPortRange < pstNewServerFlowKey->uiSrcPortRange )
        {
            FLOW_MANAGER_ERROR("SrcPortRange[%u] from server is too larger than Flow range [%u]: SrcPortMin[%u] - SrcPortMax[%u]",
                               pstNewServerFlowKey->uiSrcPortRange, uiAgentSrcPortRange, pstNewServerFlowKey->uiSrcPortMin, pstNewServerFlowKey->uiSrcPortMax);
            return AGENT_E_PARA;
        }

        // ���dscp�Ƿ�Ϸ�
        if (pstNewServerFlowKey->uiDscp > AGENT_MAX_DSCP_VALUE)
        {
            FLOW_MANAGER_ERROR("Dscp[%d] is bigger than the max value[%d]", pstNewServerFlowKey->uiDscp, AGENT_MAX_DSCP_VALUE);
            return AGENT_E_PARA;
        }

        pstNewServerFlowEntry->stServerFlowKey = * pstNewServerFlowKey;

    }
    else
    {
        FLOW_MANAGER_ERROR("Unsupported Protocol[%d]", pstNewServerFlowKey->eProtocol);
        iRet = AGENT_E_PARA;
    }
    return iRet;
}


// ��ServerWorkingFlowTable�����Entry, ��Server�·���Ϣ����. һ���������Urgent Entry
INT32 FlowManager_C::ServerWorkingFlowTableAdd(ServerFlowKey_S *pstNewServerFlowKey)
{
    INT32 iRet = AGENT_OK;
    ServerFlowTableEntry_S stNewServerFlowEntry;

    iRet = ServerFlowTablePreAdd(pstNewServerFlowKey, &stNewServerFlowEntry);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Server Flow Table Pre Add failed[%d]", iRet);
        return iRet;
    }

    // ��鹤�������Ƿ����ظ�����, Urgent�����ظ�
    vector<ServerFlowTableEntry_S>::iterator pServerFlowEntry;
    for(pServerFlowEntry = ServerFlowTable.begin();
            pServerFlowEntry != ServerFlowTable.end();
            pServerFlowEntry++)
    {
        if (stNewServerFlowEntry.stServerFlowKey == pServerFlowEntry->stServerFlowKey)
        {
            FLOW_MANAGER_WARNING("This flow already exist");
            return AGENT_OK;
        }
    }

    // ServerTable�����һ�ݼ�¼, ����query���ڵ���ˢ��agent��ʱ����δ���̽���Urgent�����.
    ServerFlowTable.push_back(stNewServerFlowEntry);

    return iRet;
}

// ����ʱ�Ƿ������̽������.
INT32 FlowManager_C::DetectCheck(UINT32 uiCounter)
{
    UINT32 uiTimeCost = 0 ;

    // uiCounter �������
    uiTimeCost = (uiCounter >= uiLastCheckTimeCounter)
                 ? uiCounter - uiLastCheckTimeCounter
                 : uiCounter + ((UINT32)(-1) - uiLastCheckTimeCounter + 1);

    if ( uiTimeCost >= pcAgentCfg->GetDetectPeriod())
    {
        uiLastCheckTimeCounter = uiCounter;
        uiNeedCheckResult = AGENT_ENABLE;
        return AGENT_ENABLE;
    }
    else
    {
        return AGENT_DISABLE;
    }
}

// ������̽��.
INT32 FlowManager_C::DoDetect()
{
    INT32 iRet = AGENT_OK;
    FlowKey_S stFlowKey;

    //FLOW_MANAGER_INFO("Start UDP Detect Now");

    // ��ǰֻ����udpЭ��, ��ȡudp worker list��С
    vector<AgentFlowTableEntry_S>::iterator pAgentFlowEntry;
    for(pAgentFlowEntry = AgentFlowTable.begin();
            pAgentFlowEntry != AgentFlowTable.end();
            pAgentFlowEntry++)
    {
        // ��ǰֻ����udpЭ��
        if (AGENT_DETECT_PROTOCOL_UDP == pAgentFlowEntry->stFlowKey.eProtocol)
        {
            // ֻ����enable��Entry
            if (FLOW_ENTRY_STATE_CHECK(pAgentFlowEntry->uiFlowState, FLOW_ENTRY_STATE_ENABLE))
            {

                // ���Խ��,������ڴ�.
                if (NULL !=  WorkerList_UDP)
                {
                    stFlowKey = pAgentFlowEntry->stFlowKey;
                    FLOW_MANAGER_INFO("destip is %s, destPort is %u, src port is %u", sal_inet_ntoa(stFlowKey.uiDestIP), stFlowKey.uiDestPort, stFlowKey.uiSrcPort);

                    iRet = WorkerList_UDP->PushSession(stFlowKey);
                    if (iRet && AGENT_E_SOCKET != iRet)
                    {
                        FLOW_MANAGER_ERROR("Push Session to Worker Failed[%d], SrcPort[%u]", iRet, pAgentFlowEntry->stFlowKey.uiSrcPort);
                        continue;
                    }
                }
                else
                {
                    FLOW_MANAGER_ERROR("UDP  is over udp worker list, SrcPort[%u].", pAgentFlowEntry->stFlowKey.uiSrcPort);
                    continue;
                }
            }
        }
    }

    if (AGENT_E_SOCKET == iRet)
    {
        iRet = AGENT_OK;
    }
	
    return iRet;
}

// ����Ǵ�ʱ��ü��̽����. ̽������ʱ��+timeoutʱ��.
INT32 FlowManager_C::DetectResultCheck(UINT32 uiCounter)
{
    UINT32 uiTimeCost = 0 ;

    // uiCounter �������
    uiTimeCost = (uiCounter >= uiLastCheckTimeCounter)
                 ? uiCounter - uiLastCheckTimeCounter
                 : uiCounter + ((UINT32)(-1) - uiLastCheckTimeCounter + 1);

    if ( AGENT_ENABLE == uiNeedCheckResult
            &&(uiTimeCost >= pcAgentCfg->GetDetectTimeout()))
    {
        uiNeedCheckResult = AGENT_FALSE;
        return AGENT_ENABLE;
    }
    else
    {
        return AGENT_DISABLE;
    }
}

// �����׼��
INT32 FlowManager_C::FlowComputeSD(INT64 * plSampleData, UINT32 uiSampleNumber, INT64 lSampleMeanValue,
                                   INT64 * plStandardDeviation)
{
    UINT32 uiSampleIndex = 0;
    INT64 lVariance = 0;  //����

    assert(0 != uiSampleNumber);

    *plStandardDeviation = 0;

    for (uiSampleIndex = 0; uiSampleIndex < uiSampleNumber; uiSampleIndex ++)
    {
        lVariance += pow((plSampleData[uiSampleIndex] - lSampleMeanValue), 2);
    }
    lVariance = lVariance / uiSampleNumber; // ����
    *plStandardDeviation = sqrt(lVariance); // ��׼��

    return AGENT_OK;
}

// ����ͳ������,׼���ϱ�
INT32 FlowManager_C::FlowPrepareReport(UINT32 uiFlowTableIndex)
{
    INT32 iRet = AGENT_OK;

    // ̽����������.
    UINT32 uiResultNumber = 0;

    // ��Ч��������.
    UINT32 uiSampleNumber = 0;

    INT64 lDataTemp = 0;         // ��������.
    INT64 * plT3Temp = NULL;     // ʱ�� us, Targetƽ������ʱ��(stT3 - stT2)
    INT64 lT3Sum = 0;
    INT64 * plT4Temp = NULL;     // ʱ�� us, Sender����ƽ������ʱ��(stT4 - stT1), RTT
    INT64 lT4Sum = 0;
    INT64 lStandardDeviation = 0;// ʱ�ӱ�׼��


    // ̽����������.
    uiResultNumber = AgentFlowTable[uiFlowTableIndex].vFlowDetectResultPkt.size();

    // �����������ʱ��
    {
        struct timeval tm;
        sal_memset(&tm, 0, sizeof(tm));
        gettimeofday(&tm,NULL); // ��ȡ��ǰʱ��
        // ��msΪ��λ�����ϱ�.
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT5 = (INT64)tm.tv_sec * SECOND_MSEC
                + (INT64)tm.tv_usec / MILLISECOND_USEC;
    }

    // û��̽������
    if (0 == uiResultNumber)
    {
        return AGENT_OK;
    }

    plT3Temp = new INT64[uiResultNumber];
    if (NULL == plT3Temp)
    {

        FLOW_MANAGER_ERROR("No enough memory.[%u]", uiResultNumber);
        return AGENT_E_MEMORY;
    }

    plT4Temp = new INT64[uiResultNumber];
    if (NULL == plT4Temp)
    {
        delete [] plT3Temp;
        plT3Temp = NULL;

        FLOW_MANAGER_ERROR("No enough memory.[%u]", uiResultNumber);
        return AGENT_E_MEMORY;
    }

    sal_memset(plT3Temp, 0, sizeof(INT64)*uiResultNumber);
    sal_memset(plT4Temp, 0, sizeof(INT64)*uiResultNumber);

    // ����ʱ��, �޳�û���յ�Ӧ�������
    vector<DetectResultPkt_S>::iterator pDetectResultPkt;
    for(pDetectResultPkt = AgentFlowTable[uiFlowTableIndex].vFlowDetectResultPkt.begin();
            pDetectResultPkt != AgentFlowTable[uiFlowTableIndex].vFlowDetectResultPkt.end();
            pDetectResultPkt ++)
    {
        if ( SESSION_STATE_WAITING_CHECK == pDetectResultPkt->uiSessionState )
        {
            // С��0��ʱ�Ӳ�����, ����os�ڵ���ϵͳʱ��, ���Ը��쳣����.
            // ����ʱ����us��, ʹ��us���м���.
            lDataTemp = pDetectResultPkt->stT3 - pDetectResultPkt->stT2;
            if (0 <= lDataTemp)
            {
                plT3Temp[uiSampleNumber] = lDataTemp; //us
                lT3Sum += lDataTemp;
            }

            lDataTemp = pDetectResultPkt->stT4 - pDetectResultPkt->stT1;
            if (0 <= lDataTemp)
            {
                plT4Temp[uiSampleNumber] = lDataTemp; //us
                lT4Sum += lDataTemp;
            }

            //FLOW_MANAGER_INFO("Target Latency[%u]us, RTT[%u]us, uiDataIndex[%u]", plT3Temp[uiSampleNumber], plT4Temp[uiSampleNumber], uiSampleNumber);
            uiSampleNumber ++;
        }
    }
    // û���յ�һ����ЧӦ����, û����Чʱ������.
    if (0 == uiSampleNumber)
    {
        delete [] plT3Temp;
        plT3Temp = NULL;
        delete [] plT4Temp;
        plT4Temp = NULL;

        // ����ʱ����ϢΪ-1.
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT2 = -1;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT3 = -1;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT4 = -1;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatencyMin = -1;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatencyMax = -1;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatency50Percentile = -1;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatency99Percentile = -1;

        return AGENT_OK;
    }
    // ��plT3Temp(Target) ����ƽ��ֵ
    AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT3 = lT3Sum / uiSampleNumber;


    // ��plT4Temp(rtt) ����ƽ��ֵ.
    lDataTemp = lT4Sum / uiSampleNumber;
    AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT4 = lDataTemp;


    // ��plT4Temp(rtt) �����׼��
    iRet = FlowComputeSD(plT4Temp, uiSampleNumber, lDataTemp, &lStandardDeviation);
    if (iRet)
    {
        delete [] plT3Temp;
        plT3Temp = NULL;
        delete [] plT4Temp;
        plT4Temp = NULL;

        FLOW_MANAGER_ERROR("Flow Sort Result failed[%d]", iRet);
        return iRet;
    }
    AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatencyStandardDeviation = lStandardDeviation;

    // ��plT4Temp(rtt) ���д�С��������
    sort(plT4Temp, plT4Temp + uiSampleNumber);

    // ��ȡplT4Temp(rtt)��Сֵ
    lDataTemp = 0;
    AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatencyMin = plT4Temp[lDataTemp];

    // ��ȡplT4Temp(rtt)���ֵ
    // uiSampleNumber �� 0
    lDataTemp = uiSampleNumber - 1;
    AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatencyMax = plT4Temp[lDataTemp];

    // ��ȡplT4Temp(rtt)��λ��
    if( 2 <= uiSampleNumber)
    {
        lDataTemp = uiSampleNumber / 2 - 1;
    }
    else
    {
        lDataTemp = 0;
    }
    AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatency50Percentile = plT4Temp[lDataTemp];

    // ��ȡplT4Temp(rtt)99%λ��
    if( 2 <= uiSampleNumber)
    {
        lDataTemp = uiSampleNumber*99/100 - 1;
    }
    else
    {
        lDataTemp = 0;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lLatency99Percentile = plT4Temp[lDataTemp];
    }
    

    delete [] plT3Temp;
    plT3Temp = NULL;
    delete [] plT4Temp;
    plT4Temp = NULL;

    return AGENT_OK;
}



// �����ϱ��ӿ�
INT32 FlowManager_C::FlowDropReport(UINT32 uiFlowTableIndex)
{
    INT32 iRet = AGENT_OK;
    AgentFlowTableEntry_S * pstAgentFlowEntry = NULL;
    stringstream  ssReportData; // ��������json��ʽ�ϱ�����

    iRet = FlowPrepareReport(uiFlowTableIndex);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Flow Prepare Report failed[%d]", iRet);
        return iRet;
    }

    ssReportData.clear();
    ssReportData.str("");

    pstAgentFlowEntry = &(AgentFlowTable[uiFlowTableIndex]);
    pstAgentFlowEntry->stFlowDetectResult.lDropNotesCounter ++;

    iRet = CreateDropReportData(pstAgentFlowEntry, &ssReportData);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Create Drop Report Data failed[%d]", iRet);
        return iRet;
    }


    iRet = ReportDataToServer(pcAgentCfg, &ssReportData, REPORT_LOSSPKT_URL);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Flow Report Data failed[%d]", iRet);
        return iRet;
    }
    return AGENT_OK;
}

// ��ʱ�ϱ��ӿ�
INT32 FlowManager_C::FlowLatencyReport(UINT32 uiFlowTableIndex, UINT32 maxDelay)
{
    INT32 iRet = AGENT_OK;
    AgentFlowTableEntry_S * pstAgentFlowEntry = NULL;
    stringstream  ssReportData; // ��������json��ʽ�ϱ�����
    string strReportData;       // ���ڻ��������ϱ����ַ�����Ϣ

    iRet = FlowPrepareReport(uiFlowTableIndex);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Flow Prepare Report failed[%d]", iRet);
        return iRet;
    }

    ssReportData.clear();
    ssReportData.str("");

    pstAgentFlowEntry = &(AgentFlowTable[uiFlowTableIndex]);
    iRet = CreateLatencyReportData(pstAgentFlowEntry, &ssReportData, maxDelay);

    if (AGENT_FILTER_DELAY == iRet)
    {
        FLOW_MANAGER_INFO("Current flow's delay time is less than threshold, return [%d]", iRet);
        return AGENT_FILTER_DELAY;
    }
    else if (iRet)
    {
        FLOW_MANAGER_ERROR("Creat Latency Report Data[%d]", iRet);
        return iRet;
    }
    strReportData = ssReportData.str();

    SAVE_LATENCY_INFO("%s", ssReportData.str().c_str());

    iRet = ReportDataToServer(pcAgentCfg, &ssReportData, REPORT_LATENCY_URL);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Flow Report Data failed[%d]", iRet);
        return iRet;
    }

    return AGENT_OK;
}

// ��������, �������������ϱ�,ͬʱ����׷�ٱ���.
INT32 FlowManager_C::FlowDropNotice(UINT32 uiFlowTableIndex)
{
    INT32 iRet = AGENT_OK;

   
    FLOW_ENTRY_STATE_SET(AgentFlowTable[uiFlowTableIndex].uiFlowState, FLOW_ENTRY_STATE_TRACKING);
    AgentFlowTable[uiFlowTableIndex].uiFlowTrackingCounter = 0;

    iRet = FlowDropReport(uiFlowTableIndex);
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Flow Drop Report failed[%d]", iRet);
    }

    return iRet;
}

// ÿһ��̽����ɺ�ĺ�������.
INT32 FlowManager_C::DetectResultProcess(UINT32 uiFlowTableIndex)
{
    INT32 iRet = AGENT_OK;
    DetectResultPkt_S stDetectResultPkt;
    UINT32 uiUrgentFlow = 0;


    // ��ȡ�ո�ѹ�������̽����.
    stDetectResultPkt = AgentFlowTable[uiFlowTableIndex].vFlowDetectResultPkt.back();

    // ����ǵ�һ��̽�ⱨ��, ˢ��̽��ʱ��.
    // ����һ�����ķ���ʧ��,��lT1=0.
    // ����һ�����ķ��ͳɹ�,���Ƕ���. ��lT1Ϊ����ʱ��, lT2=0.
    if (1 == AgentFlowTable[uiFlowTableIndex].vFlowDetectResultPkt.size())
    {
       
        // ʱ�����msΪ��λ�����ϱ�.
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT1 = (INT64)stDetectResultPkt.stT1.uiSec * SECOND_MSEC
                + (INT64)stDetectResultPkt.stT1.uiUsec / MILLISECOND_USEC;
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lT2 = (INT64)stDetectResultPkt.stT2.uiSec * SECOND_MSEC
                + (INT64)stDetectResultPkt.stT2.uiUsec / MILLISECOND_USEC;
    }

    // ˢ������ͳ����Ϣ
    // ���ͳɹ�
    if ( SESSION_STATE_SEND_FAIELD != stDetectResultPkt.uiSessionState)
    {
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lPktSentCounter ++;
    }

    // ����
    if ( SESSION_STATE_TIMEOUT == stDetectResultPkt.uiSessionState )
    {
        AgentFlowTable[uiFlowTableIndex].stFlowDetectResult.lPktDropCounter ++;
        AgentFlowTable[uiFlowTableIndex].uiFlowDropCounter ++;
     
    }
    else if ( SESSION_STATE_WAITING_CHECK == stDetectResultPkt.uiSessionState ) //δ����
    {
        // ȡ������״̬.
        AgentFlowTable[uiFlowTableIndex].uiFlowDropCounter = 0;
        FLOW_ENTRY_STATE_CLEAR(AgentFlowTable[uiFlowTableIndex].uiFlowState, FLOW_ENTRY_STATE_DROPPING);
    }


    // ��ͨ����������, �������������ϱ�,ͬʱ����׷�ٱ���.
    if ( !(FLOW_ENTRY_STATE_CHECK(AgentFlowTable[uiFlowTableIndex].uiFlowState, FLOW_ENTRY_STATE_DROPPING))
            && (pcAgentCfg->GetDetectDropThresh() <= AgentFlowTable[uiFlowTableIndex].uiFlowDropCounter))
    {
        // ���붪��״̬
        iRet = FlowDropNotice(uiFlowTableIndex);
        if (iRet)
        {
            FLOW_MANAGER_ERROR("FlowDropNotice failed[%d], index[%d]", iRet, uiFlowTableIndex);
        }
    }

    return AGENT_OK;
}

// �����ռ���̽����.
INT32 FlowManager_C::GetDetectResult()
{
    INT32 iRet = AGENT_OK;
    UINT32 uiAgentFlowTableSize = 0;
    UINT32 uiAgentFlowTableIndex = 0;
    DetectWorkerSession_S stWorkerSession;
    DetectResultPkt_S   stDetectResultPkt;
    // ��ǰֻ����udpЭ��, ���� udp worker list

    // �����worker�����д��ռ��ĻỰ.
    if (NULL  == WorkerList_UDP)
    {
        return AGENT_E_NOT_FOUND;
    }

    // ̽�������д����.

    uiAgentFlowTableSize = AgentFlowTable.size();

    do
    {
        sal_memset(&stWorkerSession, 0, sizeof(stWorkerSession));

        iRet = WorkerList_UDP->PopSession(&stWorkerSession);
        if( iRet && (AGENT_E_NOT_FOUND != iRet))
        {
            FLOW_MANAGER_ERROR("Worker PopSession Failed[%d]", iRet);
            continue;
        }
        else if (AGENT_E_NOT_FOUND == iRet)
        {
            break;
        }
        uiAgentFlowTableIndex = stWorkerSession.stFlowKey.uiAgentFlowTableIndex;
        FLOW_MANAGER_INFO("Worker PopSession uiAgentFlowTableIndex: %u", uiAgentFlowTableIndex);
        // ���FlowTableIndex�Ƿ���Ч
        if (uiAgentFlowTableIndex < uiAgentFlowTableSize)
        {
            sal_memset(&stDetectResultPkt, 0, sizeof(stDetectResultPkt));
            stDetectResultPkt.uiSessionState = stWorkerSession.uiSessionState;
            stDetectResultPkt.uiSequenceNumber = stWorkerSession.uiSequenceNumber;
            stDetectResultPkt.stT1 = stWorkerSession.stT1;
            stDetectResultPkt.stT2 = stWorkerSession.stT2;
            stDetectResultPkt.stT3 = stWorkerSession.stT3;
            stDetectResultPkt.stT4 = stWorkerSession.stT4;

            FLOW_MANAGER_INFO("Worker PopSession.. %u.. %u... %u.. %u.....%u.. %u... %u.. %u",
                              stWorkerSession.stT1.uiSec,stWorkerSession.stT2.uiSec,stWorkerSession.stT3.uiSec,stWorkerSession.stT4.uiSec,
                              stWorkerSession.stT1.uiUsec,stWorkerSession.stT2.uiUsec,stWorkerSession.stT3.uiUsec,stWorkerSession.stT4.uiUsec);


            if (0 == FLOW_ENTRY_STATE_CHECK(AgentFlowTable[uiAgentFlowTableIndex].uiFlowState, FLOW_ENTRY_STATE_ENABLE))
            {
                continue;
            }

            FLOW_MANAGER_INFO("Worker PopSession.. +++++++++++++++++%d,%d", AgentFlowTable.size(),
                              AgentFlowTable[uiAgentFlowTableIndex].vFlowDetectResultPkt.size());

            // ������д��̽����
            AgentFlowTable[uiAgentFlowTableIndex].vFlowDetectResultPkt.push_back(stDetectResultPkt);

            // ��Ըռ����̽�������д���, ���ж���,Urgent���¼�����.
            iRet = DetectResultProcess(uiAgentFlowTableIndex);
            if (iRet)
            {
                FLOW_MANAGER_WARNING("DetectResultProcess failed [%d]", iRet);
                continue;
            }
        }
        else
        {
            FLOW_MANAGER_INFO("FlowTableIndex[%u] is over size[%d]. Maybe DoQuery just clear the agent flow table",
                              uiAgentFlowTableIndex, uiAgentFlowTableSize);
            continue;
        }

    }
    while( AGENT_E_NOT_FOUND != iRet );

    return AGENT_OK;
}

// ����ʱ�Ƿ�������ϱ�Collector����.
INT32 FlowManager_C::ReportCheck(UINT32 uiCounter)
{
    UINT32 uiTimeCost = 0 ;

    // uiCounter �������
    uiTimeCost = (uiCounter >= uiLastReportTimeCounter)
                 ? uiCounter - uiLastReportTimeCounter
                 : uiCounter + ((UINT32)(-1) - uiLastReportTimeCounter + 1);

    if ( uiTimeCost >= pcAgentCfg->GetReportPeriod())
    {
        uiLastReportTimeCounter = uiCounter;
        return AGENT_ENABLE;
    }
    else
    {
        return AGENT_DISABLE;
    }
}

// ������̽�����ϱ�.
INT32 FlowManager_C::DoReport()
{
    INT32 iRet = AGENT_OK;
    UINT32 uiFlowTableIndex = 0;
    //FLOW_MANAGER_INFO("Start Report Now");

    for(uiFlowTableIndex = 0; uiFlowTableIndex < AgentFlowTable.size(); uiFlowTableIndex++)
    {
        if ((FLOW_ENTRY_STATE_CHECK(AgentFlowTable[uiFlowTableIndex].uiFlowState, FLOW_ENTRY_STATE_ENABLE)))
        {
            iRet = FlowLatencyReport(uiFlowTableIndex, pcAgentCfg->GetMaxDelay());
            if (AGENT_FILTER_DELAY == iRet)
            {
                continue ;
            }
            else if (iRet)
            {
                FLOW_MANAGER_ERROR("Flow Latency Report failed[%d], index[%u]", iRet, uiFlowTableIndex);
            }
        }
        
    }

    // ����range������һ���ϱ�����ʹ��AgentFlowTable�е���Щ��.
    iRet = AgentFlowTableEntryAdjust();
    if (iRet)
    {
        FLOW_MANAGER_ERROR("Flow Working Entry Adjust failed[%d]", iRet);
    }
    return iRet;
}

// ������Serverˢ����������.
INT32 FlowManager_C::DoQuery()
{
    INT32 iRet = AGENT_OK;

    FLOW_MANAGER_INFO("Start Query Server Now");

    // ���Server��������
    ServerClearFlowTable();

    iRet = RequestProbeListFromServer(this);
    if (AGENT_OK != iRet)
    {
        FLOW_MANAGER_WARNING("RequestProbeListFromServer[%d]", iRet);
        return iRet;
    }

    // ���Agent���ñ�
    AgentClearFlowTable();

    // ��鹤�������Ƿ����ظ�����, Urgent�����ظ�
    vector<ServerFlowTableEntry_S>::iterator pServerFlowEntry;
    for(pServerFlowEntry = ServerFlowTable.begin();
            pServerFlowEntry != ServerFlowTable.end();
            pServerFlowEntry++)
    {
        AgentFlowTableAdd(&(*pServerFlowEntry));
    }

    FLOW_MANAGER_INFO("Query Server Finished");

    return iRet;
}



// Thread�ص�����.
// PreStopHandler()ִ�к�, ThreadHandler()��Ҫ��GetCurrentInterval() us�������˳�.
INT32 FlowManager_C::ThreadHandler()
{
    INT32 iRet = AGENT_OK;
    UINT32 counter = 0;
    uiLastCheckTimeCounter = counter;
    uiLastReportTimeCounter = counter;
    uiLastQuerytTimeCounter = counter;
	
    while (GetCurrentInterval())
    {
        // ��ǰ�����Ƿ������̽������.
        if (DetectCheck(counter))
        {
            if (BIG_PKG_RATE)
            {
                // ������������������Ҫ��������
                SetPkgFlag();
                BIG_PKG_RATE = 0;
            }

            // ����̽������.
            iRet = DoDetect();
            if (iRet)
            {
                FLOW_MANAGER_WARNING("Do UDP Detect failed[%d]", iRet);
            }
        }

        // ��ǰ�����Ƿ���ռ�̽����
        if (DetectResultCheck(counter))
        {
            // �����ռ�̽��������
            iRet = GetDetectResult();
            if (iRet)
            {
                FLOW_MANAGER_WARNING("Get UDP Detect Result failed[%d]", iRet);
            }
        }

        // ��ǰ�����Ƿ�������ϱ�Collector����
        if (ReportCheck(counter))
        {
            // �����ϱ�Collector����
            iRet = DoReport();
            if (iRet)
            {
                FLOW_MANAGER_WARNING("Do UDP Report failed[%d]", iRet);
            }
        }

        // ��ǰ�����Ƿ�ò�ѯServer����

        if (SHOULD_PROBE)
        {
            SHOULD_PROBE = 0;
            // ������ѯServer��������.
            iRet = DoQuery();
            if (iRet)
            {
                FLOW_MANAGER_WARNING("Do Query failed[%d]", iRet);
				SHOULD_PROBE = 1;
            }
        }
		
        if (SHOULD_QUERY_CONF)
        {
            SHOULD_QUERY_CONF = 0;
            iRet = DoQueryConfig();
            if (iRet)
            {
                FLOW_MANAGER_WARNING("Do Query Config failed[%d]", iRet);
				SHOULD_QUERY_CONF = 1;
            }
        }

        if (SHOULD_REPORT_IP)
        {
            SHOULD_REPORT_IP = 0;
            iRet = ReportAgentIPToServer(this->pcAgentCfg);
            if (iRet)
            {
                FLOW_MANAGER_WARNING("ReportAgentIPToServer failed[%d]", iRet);
				SHOULD_REPORT_IP = 1;
            }
        }

        if (PROBE_INTERVAL != 9999)
        {
            this->pcAgentCfg->SetDetectPeriod(PROBE_INTERVAL);
            PROBE_INTERVAL = 9999;
        }

        sleep(1);
        counter ++;
    }
    return AGENT_OK;
}

// Thread��������, ֪ͨThreadHandler����׼��.
INT32 FlowManager_C::PreStartHandler()
{
    INT32 iRet = AGENT_OK;
    iRet = SetNewInterval(pcAgentCfg->GetPollingTimerPeriod());
    if (iRet)
    {
        FLOW_MANAGER_ERROR("SetNewInterval failed[%d], Interval[%d]", iRet, pcAgentCfg->GetPollingTimerPeriod());
        return AGENT_E_PARA;
    }
    return iRet;
}

// Thread����ֹͣ, ֪ͨThreadHandler�����˳�.
INT32 FlowManager_C::PreStopHandler()
{
    SetNewInterval(0);
    return AGENT_OK;
}

INT32 FlowManager_C::FlowManagerAction(INT32 interval)
{
    INT32 iRet = AGENT_OK;

    if (0 > interval)
    {
        FLOW_MANAGER_ERROR("Interval value[%d] is out of range, return.", interval);
        return AGENT_E_ERROR;
    }

    UINT32 oldInterval = GetCurrentInterval();
    UINT32 newInterval;
    stringstream ss;
    ss << interval;
    ss >> newInterval;
    switch(oldInterval)
    {
        case 0:
            if (newInterval)
            {
                // ����FlowManager
                SetNewInterval(newInterval);
                FLOW_MANAGER_INFO("Set CurrentInterval to [%d] success.", newInterval);
                StartThread();
                FLOW_MANAGER_INFO("Start flowmanager thread success");
            }
            else
            {
                // �Ѿ�ֹͣ�������ٴ�ֹͣ��ֱ�ӷ���
                FLOW_MANAGER_INFO("CurrentInterval is alread 0, return.");
            }
            break;
        default:
            // �����µļ��ʱ��
            SetNewInterval(newInterval);
            FLOW_MANAGER_INFO("Set CurrentInterval from [%d] to [%d] success.", oldInterval, newInterval);
            break;
    }

    return iRet;
}

void FlowManager_C::SetPkgFlag()
{
    vector<AgentFlowTableEntry_S>::iterator pAgentFlowEntry;
    for(pAgentFlowEntry = AgentFlowTable.begin(); pAgentFlowEntry != AgentFlowTable.end();
            pAgentFlowEntry++)
    {
        pAgentFlowEntry->stFlowKey.uiIsBigPkg = !pAgentFlowEntry->stFlowKey.uiIsBigPkg;
    }
}

INT32 FlowManager_C::DoQueryConfig()
{
    INT32 iRet = AGENT_OK;
    iRet = RequestConfigFromServer(this);
    if (AGENT_OK != iRet)
    {
        FLOW_MANAGER_ERROR("RequestConfigFromServer[%d]", iRet);
        return iRet;
    }
    return iRet;
}

