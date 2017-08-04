#ifndef __SRC_DetectWorker_H__
#define __SRC_DetectWorker_H__

#include <pthread.h>
#include <vector>
#include <netinet/in.h>
#include "ServerAntAgentCfg.h"

#include "Sal.h"
#include "ThreadClass.h"

#define SAL_INADDR_ANY INADDR_ANY

// dscp����6bit�ռ�
#define AGENT_MAX_DSCP_VALUE ((1L<<6) - 1)

// ServerAnt�����������Ϣ
typedef struct tagServerTopo
{
    UINT32    uiSvid;             // the source ID. DeviceID(Level == 1) RegionID(Level >1)
    UINT32    uiDvid;             // the destination ID. DeviceID(Level == 1) RegionID(Level >1)
    UINT32    uiLevel;            // 1:Device, 2:Pod, 3:DC, 4:AZ ...

    // ���������, �������key�Ƚ�.
    bool operator == (const tagServerTopo & other) const
    {
        if (  (other.uiSvid == uiSvid)
                &&(other.uiDvid == uiDvid)
                &&(other.uiLevel == uiLevel)
           )
            return AGENT_TRUE;
        else
            return AGENT_FALSE;
    }

    bool operator != (const tagServerTopo & other) const
    {
        if (  (other.uiSvid != uiSvid)
                ||(other.uiDvid != uiDvid)
                ||(other.uiLevel != uiLevel)
           )
            return AGENT_TRUE;
        else
            return AGENT_FALSE;
    }
} ServerTopo_S;

/* ����������Ϣ(key), ����������Ϣ����Ψһ��ʶ��һ���� */
typedef struct tagFlowKey
{
    UINT32    uiUrgentFlow;       // �����ȼ�.
    AgentDetectProtocolType_E eProtocol;// Э������, ��AgentDetectProtocolType_E,��ӻỰʱ����Э�����;����Ƿ���DetectWorker���Խ��бȽ�.
    UINT32    uiSrcIP;            // ̽��ԴIP, ��ӻỰʱ����Э�����;����Ƿ���DetectWorker���Խ��бȽ�.
    UINT32    uiDestIP;           // ̽��Ŀ��IP. ��ӻỰʱ����Э�����;����Ƿ���DetectWorker���Խ��бȽ�.
    UINT32    uiSrcPort;          // ̽��Դ�˿ں�. ��ӻỰʱ����Э�����;����Ƿ���DetectWorker���Խ��бȽ�.
    UINT32    uiDestPort;         // ̽��Ŀ�Ķ˿ں�.��ӻỰʱ����Э�����;����Ƿ���DetectWorker���Խ��бȽ�.
    UINT32    uiDscp;             // ̽�ⱨ�ĵ�DSCP.
    UINT32    uiIsBigPkg;        // �Ƿ���

    ServerTopo_S    stServerTopo;          // ServerAnt�����������Ϣ, ��Դ��Server, Agent��������, ������Ϊkey��һ����.

    UINT32    uiAgentFlowTableIndex; // ��ǰflow��agent Flow Table�е�����, ��flow��������ʱ����, ����AgentFlowTable����ٶ�.

    // ���������, �������key�Ƚ�.
    bool operator == (const tagFlowKey & other) const
    {
        if (  (other.uiAgentFlowTableIndex == uiAgentFlowTableIndex)
                &&(other.uiUrgentFlow == uiUrgentFlow)
                &&(other.eProtocol == eProtocol)
                &&(other.uiSrcIP == uiSrcIP)
                &&(other.uiDestIP == uiDestIP)
                &&(other.uiSrcPort == uiSrcPort)
                &&(other.uiDestPort == uiDestPort)
                &&(other.uiDscp == uiDscp)
                &&(other.stServerTopo == stServerTopo)
           )
            return AGENT_TRUE;
        else
            return AGENT_FALSE;
    }

    bool operator != (const tagFlowKey & other) const
    {
        if (  (other.uiAgentFlowTableIndex != uiAgentFlowTableIndex)
                ||(other.uiUrgentFlow != uiUrgentFlow)
                ||(other.eProtocol != eProtocol)
                ||(other.uiSrcIP != uiSrcIP)
                ||(other.uiDestIP != uiDestIP)
                ||(other.uiSrcPort != uiSrcPort)
                ||(other.uiDestPort != uiDestPort)
                ||(other.uiDscp != uiDscp)
                ||(other.stServerTopo != stServerTopo)
           )
            return AGENT_TRUE;
        else
            return AGENT_FALSE;
    }
} FlowKey_S;

// DetectWorkerSession_S.uiSessionState �Ự״̬��
enum
{
    SESSION_STATE_INITED  = 1,    // ��ɳ�ʼ��,������̽�ⱨ��.
    SESSION_STATE_SEND_FAIELD,    // ���ķ���ʧ��.
    SESSION_STATE_WAITING_REPLY,  // �ô�Ӧ����.
    SESSION_STATE_WAITING_CHECK,  // �Ѿ��յ�Ӧ����,����ѯ���.
    SESSION_STATE_TIMEOUT,        // �ỰӦ���ĳ�ʱ.
    SESSION_STATE_MAX
};

// worker��ɫ:sender(Client side), target(Server side)
enum
{
    WORKER_ROLE_CLIENT  = 0,    // sender(Client side) ����̽�ⱨ��
    WORKER_ROLE_SERVER,         // target(Server side) ��Ӧ̽�ⱨ��,����Ӧ����
    WORKER_ROLE_MAX
};

// ʱ�����Ϣ. ��ֱ��ʹ��timeval����Ϊ�ֽ���ת���ӿ�(htonl)��ǰֻ֧��32λINT32.
typedef struct tagPacketTime
{
    UINT32   uiSec;           // ��
    UINT32   uiUsec;          // ΢��

    // ���������, �������ʱ�Ӽ���
    INT64 operator - (const tagPacketTime & other) const
    {
        return (INT64)uiUsec - (INT64)(other.uiUsec) + ((INT64)uiSec - (INT64)(other.uiSec)) * SECOND_USEC;
    }
} PacketTime_S;

// ̽�ⱨ�ĸ�ʽ �޸ĸýṹʱ���ͬ���޸�PacketHtoN()��PacketNtoH()����
typedef struct tagPacketInfo
{
    UINT32   uiSequenceNumber;    // �������к�,sender���ͱ��ĵ�ʱ������.
    UINT32   uiRole;
    PacketTime_S    stT1;               // sender�������ĵ�ʱ��
    PacketTime_S    stT2;               // target�յ����ĵ�ʱ��
    PacketTime_S    stT3;               // target����Ӧ���ĵ�ʱ��
    PacketTime_S    stT4;               // sender�յ�Ӧ���ĵ�ʱ��
} PacketInfo_S;

// DetectWorker ������Ϣ
typedef struct tagWorkerCfg
{
    AgentDetectProtocolType_E eProtocol;    // Э������, ��AgentDetectProtocolType_E, ����socketʱʹ��.
    UINT32  uiRole;

    UINT32   uiListenPort;                // ̽��ԴIP, ����socketʱʹ��.
    UINT32   uiSrcIP;                // ̽��ԴIP, ����socketʱʹ��.
    UINT32   uiDestIP;               // ̽��Ŀ��IP. Ԥ��TCP��չ
    UINT32   uiSrcPort;              // ̽��Դ�˿ں�. ����socketʱʹ��
    UINT32   uiDestPort;             // ̽��Ŀ�Ķ˿ں�. Ԥ��TCP��չ.
} WorkerCfg_S;

// DetectWorker �Ự��Ϣ
typedef struct tagDetectWorkerSession
{
    /* �Ự���� */
    UINT32   uiSessionState;     // �Ự״̬��: ������̽�ⱨ��, �ȴ�Ӧ����, ���յ�Ӧ����, ���ĳ�ʱ.
    UINT32   uiSequenceNumber;   // ���Ự�����к�.

    /* ����Ϣ6Ԫ�� */
    FlowKey_S   stFlowKey;

    /* ̽���� */
    PacketTime_S    stT1;                //ʱ�����Ϣ, ̽�ⱨ�Ĵ�Sender����ʱ��.
    PacketTime_S    stT2;                //ʱ�����Ϣ, ̽�ⱨ�ĵ���Target��ʱ��.
    PacketTime_S    stT3;                //ʱ�����Ϣ, Ӧ���Ĵ�Target������ʱ��.
    PacketTime_S    stT4;                //ʱ�����Ϣ, Ӧ���ĵ���Sender��ʱ��.
} DetectWorkerSession_S;

// DetectWorker�ඨ��,����̽�ⱨ�ķ��ͺͽ���.
class DetectWorker_C : ThreadClass_C
{
private:
    /*  */

    WorkerCfg_S stCfg;                              // ��ǰWorker���õ�̽��Э��.
    UINT32   uiSequenceNumber;                // ��Worker�ĵ�ǰ���к�,��ʼֵΪ�����.
    INT32 InitCfg(WorkerCfg_S stNewWorker);           // ��ʼ��stCfg, ��Init()����

    vector <DetectWorkerSession_S> SessionList;     // �Ự�б�, ������δ���̽��Ự.
    sal_mutex_t WorkerSessionLock;                  // �Ự������, ����SessionList


    INT32 WorkerSocket;                               // ��ǰWorkerʹ�õ�Socket.
    INT32 ReleaseSocket();                            // �ͷ�socket��Դ
    INT32 InitSocket();                               // ����stProtocol��Ϣ����socket��Դ.
    INT32 GetSocket();                                // ��ȡ��ǰsocket
    INT32 TxPacket(DetectWorkerSession_S*
                   pNewSession);               // �������ķ���.PushSession()ʱ����.
    INT32 TxUpdateSession(DetectWorkerSession_S*
                          pNewSession);               // ���ķ�����ɺ�, ˢ�»Ự״̬.

    /* Thread ʵ�ִ��� */
    INT32 ThreadHandler();                            // ������������
    INT32 PreStopHandler();                           // StopThread����, ֪ͨThreadHandler�����˳�.
    INT32 PreStartHandler();                          // StartThread����, ֪ͨThreadHandler����������.

    UINT32 uiHandlerDefaultInterval;          // Handler״̬ˢ��Ĭ������, ��λΪus
    INT32 RxUpdateSession
    (PacketInfo_S * pstPakcet);                 // Rx�����յ�Ӧ���ĺ�, ֪ͨworkerˢ�»Ự�б�, Rx����ʹ��

public:
    DetectWorker_C();                               // ���캯��, ���Ĭ��ֵ.
    ~DetectWorker_C();                              // ��������, �ͷű�Ҫ��Դ.
    ServerAntAgentCfg_C * pcAgentCfg;                   // agent_cfg
    INT32 Init(WorkerCfg_S stNewWorker, ServerAntAgentCfg_C *pcNewAgentCfg);         // ���������ɶ����ʼ��, FlowManageʹ��.
    INT32 PushSession(FlowKey_S stNewFlow);           // ���̽������, FlowManageʹ��.
    INT32 PopSession(DetectWorkerSession_S*
                     pOldSession);               // ��ѯ̽����, FlowManageʹ��.


};

#endif
