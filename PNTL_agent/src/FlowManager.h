
#ifndef __SRC_FlowManager_H__
#define __SRC_FlowManager_H__

//#include <string>

#include "ThreadClass.h"
#include "DetectWorker.h"
#include "KafkaClient.h"    // �ṩkafka�ͻ�����
#include "ServerAntAgentCfg.h"


// ÿ�����ĵ�̽����.(ʵʱˢ��)
typedef struct tagDetectResultPkt
{
    /* �Ự���� */
    UINT32   uiSessionState;      // �Ự״̬��: ������̽�ⱨ��, �ȴ�Ӧ����, ���յ�Ӧ����, ���ĳ�ʱ.
    UINT32   uiSequenceNumber;    // ���Ự�����к�.

    /* ̽���� */
    PacketTime_S    stT1;               //ʱ�����Ϣ, ̽�ⱨ�Ĵ�Sender����ʱ��.
    PacketTime_S    stT2;               //ʱ�����Ϣ, ̽�ⱨ�ĵ���Target��ʱ��.
    PacketTime_S    stT3;               //ʱ�����Ϣ, Ӧ���Ĵ�Target������ʱ��.
    PacketTime_S    stT4;               //ʱ�����Ϣ, Ӧ���ĵ���Sender��ʱ��.
} DetectResultPkt_S;

// ��������̽����, ׼���ϱ���Collector.
typedef struct tagDetectResult
{
    INT64    lT1;                        // ʱ�����Ϣ, ��λms, ��Epoch��ʼ, ��һ��̽�ⱨ�Ĵ�Sender����ʱ��.(ʵʱˢ��)
    INT64    lT2;                        // ʱ�����Ϣ, ��λms, ��Epoch��ʼ, ��һ��̽�ⱨ�ĵ���Target��ʱ��.(ʵʱˢ��)
    INT64    lT3;                        // ʱ�� us, Targetƽ������ʱ��(stT3 - stT2), (�ϱ�ʱ��������ˢ��)
    INT64    lT4;                        // ʱ�� us, Sender����ƽ������ʱ��(stT4 - stT1), RTT.(�ϱ�ʱ��������ˢ��)
    INT64    lT5;                        // ʱ�����Ϣ, ��λms, ��Epoch��ʼ, ������ϱ�ʱ��. (�ϱ�ʱ��������ˢ��).

    INT64    lDropNotesCounter;          // ����DropNotes�ϱ����� (ʵʱˢ��)
    INT64    lPktSentCounter;            // �ɹ����͵�̽�ⱨ���� (ʵʱˢ��)
    INT64    lPktDropCounter;            // û����ָ��ʱ�����յ�Ӧ���ĵı����� (ʵʱˢ��)
    INT64    lLatencyMin;                // ����̽�ⱨ��RTT����Сֵ (�ϱ�ʱ��������ˢ��)
    INT64    lLatencyMax;                // ����̽�ⱨ��RTT�����ֵ (�ϱ�ʱ��������ˢ��)
    INT64    lLatency50Percentile;       // ����̽�ⱨ��RTT��50%λ��(��λ��) (�ϱ�ʱ��������ˢ��)
    INT64    lLatency99Percentile;       // ����̽�ⱨ��RTT��99%λ�� (�ϱ�ʱ��������ˢ��)
    INT64    lLatencyStandardDeviation;  // ����̽�ⱨ��RTT�ı�׼�� (�ϱ�ʱ��������ˢ��)
} DetectResult_S;


// AgentFlowTable ֧�����, ��֧��ɾ��(�ᵼ��index����), ����enable/disable flow.
typedef struct tagAgentFlowTableEntry
{
    /* ����Ϣ6Ԫ�� */
    FlowKey_S   stFlowKey;

    UINT32 uiFlowState;   // ��ǰ��״̬, ����/��ͨ, ׷��/��ͨ, enable/disable.
    // Urgent��̽����ɺ������ϱ���disable, ����ɾ��, ����index�����.
    // ÿ������ͨ״̬�л��ɶ���״̬ʱ���������ϱ�.

    /* ԭʼ̽���� */
    vector <DetectResultPkt_S> vFlowDetectResultPkt;
    UINT32 uiFlowDropCounter;     // ����ǰ������������, �������޺󴥷������ϱ��¼�.
    UINT32 uiFlowTrackingCounter; // ������Tracking״̬ʱ, ÿ��һ��ʱ�䷢��һ��̽�ⱨ��.
    UINT32 uiUrgentFlowCounter;   // Urgent����ǰ̽�����, �������޺����Urgent̽��, �����ϱ��¼�.

    /* ׼���ϱ���̽���� */
    DetectResult_S stFlowDetectResult;
} AgentFlowTableEntry_S;


typedef struct tagServerFlowKey
{
    UINT32   uiUrgentFlow;        // �����ȼ�.

    AgentDetectProtocolType_E eProtocol;// Э������, Ŀǰ֧��UDP, ��AgentDetectProtocolType_E.
    UINT32   uiSrcIP;             // ̽��ԴIP.
    UINT32   uiDestIP;            // ̽��Ŀ��IP.
    UINT32   uiDscp;              // ̽����ʹ�õ�DSCP.
    UINT32   uiSrcPortMin;        // ̽����Դ�˿ڷ�Χ��ʼֵ, ��λ�ڵ�ǰAgent�Ѿ������Ķ˿ڷ�Χ. ��Ϊ0ʱ��ʹ��agent��������С�˿ں�.
    UINT32   uiSrcPortMax;        // ̽����Դ�˿ڷ�Χ���ֵ, ��λ�ڵ�ǰAgent�Ѿ������Ķ˿ڷ�Χ. ��Ϊ0ʱ��ʹ��agent���������˿ں�.
    UINT32   uiSrcPortRange;      // ÿ���ϱ������ڸ��ǵ�Դ�˿ڸ���. ��Ϊ0ʱ��ʹ��uiSrcPortMin to uiSrcPortMax����
    UINT32   uiDestPort;
    // ServerAnt�����������Ϣ, ��Դ��Server.
    ServerTopo_S stServerTopo;

    // ���������, �������key�Ƚ�.
    bool operator == (const tagServerFlowKey & other) const
    {
        if (  (other.uiUrgentFlow == uiUrgentFlow)
                &&(other.eProtocol == eProtocol)
                &&(other.uiDestPort == uiDestPort)
                &&(other.uiSrcIP == uiSrcIP)
                &&(other.uiDestIP == uiDestIP)
                &&(other.uiDscp == uiDscp)
                &&(other.uiSrcPortMin == uiSrcPortMin)
                &&(other.uiSrcPortMax == uiSrcPortMax)
                &&(other.uiSrcPortRange == uiSrcPortRange)
                &&(other.stServerTopo == stServerTopo)
           )
            return AGENT_TRUE;
        else
            return AGENT_FALSE;
    }

    bool operator != (const tagServerFlowKey & other) const
    {
        if (  (other.uiUrgentFlow != uiUrgentFlow)
                ||(other.eProtocol != eProtocol)
                ||(other.uiDestPort != uiDestPort)
                ||(other.uiSrcIP != uiSrcIP)
                ||(other.uiDestIP != uiDestIP)
                ||(other.uiDscp != uiDscp)
                ||(other.uiSrcPortMin != uiSrcPortMin)
                ||(other.uiSrcPortMax != uiSrcPortMax)
                ||(other.uiSrcPortRange != uiSrcPortRange)
                ||(other.stServerTopo != stServerTopo)
           )
            return AGENT_TRUE;
        else
            return AGENT_FALSE;
    }
} ServerFlowKey_S;

// ServerFlowTable ֧�����, ��֧��ɾ��(�ᵼ��index����)
typedef struct tagServerFlowTableEntry
{
    // ����Ϣkey, ��Դ��Server.
    ServerFlowKey_S   stServerFlowKey;

    // ��ǰServerFlow��ӦAgentFlow������Entry��������Χ.
    UINT32   uiAgentFlowIndexMin;
    UINT32   uiAgentFlowIndexMax;

    // AgentFlow�е�ǰenable�ĵ�������
    UINT32   uiAgentFlowWorkingIndexMin;
    UINT32   uiAgentFlowWorkingIndexMax;

} ServerFlowTableEntry_S;


// FlowManage�ඨ��, ����̽���������,������DetectWorker���̽��
class FlowManager_C : ThreadClass_C
{
private:
    // DetectWorker ��Դ����
    // ��ʼ�������޸�, ���û���
    DetectWorker_C * WorkerList_UDP;                 // UDP Target Worker, ���ڽ���̽�ⱨ�Ĳ�����Ӧ����.


    // Agent ʹ�õ�����.
    // �������������軥��, ���������������軥��. ��ǰ���̳߳���ʵ�ʲ��ᴥ������.
    vector <AgentFlowTableEntry_S> AgentFlowTable[2];   // ��������, һ��Ϊ��������, ��һ��Ϊ��������, �������, ������ɺ��ύ����.
    UINT32 uiAgentWorkingFlowTable;               // ��ǰ��������, 0��1, ����һ��Ϊ��������.
    sal_mutex_t stAgentFlowTableLock;                   // ��������������޸�uiWorkingFlowTableʱ��Ҫ����.

    // Agent������
    INT32 AgentClearFlowTable(UINT32 uiTableNumber);// ����ض�����
    INT32 AgentCommitCfgFlowTable();                      // �����������빤��������, ������Ч.��RefreshAgentFlowTable()����.
    INT32 AgentFlowTableAdd(
        UINT32 uiAgentFlowTableNumber,
        ServerFlowTableEntry_S * pstServerFlowEntry);   // ��AgentFlowTable�����Entry
    INT32 AgentRefreshFlowTable();                        // ˢ��Agent����, ��CommitServerCfgFlowTable()����.
    INT32 AgentFlowTableEntryAdjust();                    // ����range������һ��report���ڴ���Щ��.
    INT32 AgentFlowTableEntryClearResult
    (UINT32 uiAgentFlowIndex);                // ����ض�AgentFlow��̽����


    // Server�·�������
    // �������������軥��, ���������������軥��. ��ǰ���̳߳���ʵ�ʲ��ᴥ������.
    vector <ServerFlowTableEntry_S> ServerFlowTable[2]; // ��������, һ��Ϊ��������, ��һ��Ϊ��������, �������, ������ɺ��ύ����.
    UINT32 uiServerWorkingFlowTable;              // ��ǰ��������, 0��1, ����һ��Ϊ��������.
    sal_mutex_t stServerFlowTableLock;                  // ��������������޸�uiWorkingServerFlowTableʱ��Ҫ����.
    UINT32 uiServerFlowTableIsEmpty;              // Server����Ϊ��ʱ, ������ѯ����Ϊ����ֵ��1/1000, ��С���Ϊ30s.

    // Server������
    INT32 ServerClearFlowTable(UINT32 uiTableNumber);   // ����ض�����
    INT32 ServerFlowTablePreAdd(
        ServerFlowKey_S * pstNewServerFlowKey,
        ServerFlowTableEntry_S * pstNewServerFlowEntry);    // ��ServerFlowTable�����Entryǰ��Ԥ����, ������μ�鼰������ʼ��

    INT32 ServerCommitCfgFlowTable();                         // �ύ��������, ��DoQuery()����.
    // �����������빤��������, ������Ч, ����ˢ��Agent����.

    // ҵ��������
    UINT32 uiNeedCheckResult;                     // �Ƿ���δ�ռ�̽��������?
    UINT32 uiLastCheckTimeCounter;                // ���һ������̽�����̵�ʱ���
    INT32 DetectCheck(UINT32 counter);              // ����ʱ�Ƿ������̽������.
    INT32 DoDetect();                                     // ������̽��.

    INT32 DetectResultCheck(UINT32 counter);        // ����Ǵ�ʱ��ü��̽����. ̽������ʱ��+timeoutʱ��.
    INT32 GetDetectResult();                              // �����ռ���̽����.
    INT32 DetectResultProcess(UINT32 uiFlowTableIndex); // ÿһ��̽����ɺ�ĺ�������.
    INT32 FlowDropNotice(UINT32 uiFlowTableIndex);      // ��������, �������������ϱ�,ͬʱ����׷�ٱ���.
    UINT32 uiLastReportTimeCounter;               // ���һ�������ϱ�Collector��ʱ���
    INT32 ReportCheck(UINT32 counter);              // ����ʱ�Ƿ�������ϱ�Collector����.
    INT32 DoReport();                                     // �������ϱ�.
    INT32 FlowComputeSD(
        INT64 * plSampleData,
        UINT32 uiSampleNumber,
        INT64 lSampleMeanValue,
        INT64 * plStandardDeviation);                    // �����׼��
    INT32 FlowPrepareReport(UINT32 uiFlowTableIndex);   // ����ͳ������,׼���ϱ�
    INT32 FlowReportData(string * pstrReportData);      // ͳһ�ϱ��ӿ�
    INT32 FlowDropReport(UINT32 uiFlowTableIndex);      // �����ϱ��ӿ�
    INT32 FlowLatencyReport(UINT32 uiFlowTableIndex, UINT32 maxDelay);   // ��ʱ�ϱ��ӿ�

    UINT32 uiLastQuerytTimeCounter;           // ���һ��������ѯServer��ʱ���
    INT32 QueryCheck(UINT32 counter);           // ����ʱ�Ƿ��������ѯServer��������.
    INT32 DoQuery();                                  // ������Serverˢ����������.
    INT32 GetFlowFromServer
    (ServerFlowKey_S * pstNewFlow);         // ��Server��ȡһ���µ�Flow.

    /* Thread ʵ�ִ��� */
    INT32 ThreadHandler();                        // ������������
    INT32 PreStopHandler();                       // StopThread����, ֪ͨThreadHandler�����˳�.
    INT32 PreStartHandler();                      // StartThread����, ֪ͨThreadHandler����������.

public:
    FlowManager_C();                               // ���캯��, ���Ĭ��ֵ.
    ~FlowManager_C();                              // ��������, �ͷű�Ҫ��Դ.

    // ȫ��AgentCfg��Ϣ
    ServerAntAgentCfg_C * pcAgentCfg;                   // agent_cfg

    INT32 Init(ServerAntAgentCfg_C * pcNewAgentCfg);   // ��ʼ������

    INT32 ServerWorkingFlowTableAdd
    (ServerFlowKey_S stServerFlowKey);       // ��ServerWorkingFlowTable�����Urgent Entry, ��Server�·���Ϣ����

    INT32 FlowManagerAction(INT32 interval);	    // ���ݲ�����ͣFlowManager
};

#endif
