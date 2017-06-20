#ifndef __SRC_ServerAntAgentCfg_H__
#define __SRC_ServerAntAgentCfg_H__

#include "string"

#include "Sal.h"
#include "AgentCommon.h"
#include <vector>

/*
��ServerAntsCollector�ϱ�̽����ʱ������ͨ��, ��ǰ֧��kafka, δ��������.
*/
typedef enum  tagCollectorProtocolType
{
    COLLECTOR_PROTOCOL_NULL = 0,   // δ����
    COLLECTOR_PROTOCOL_KAFKA,       // ʹ�� kafka ��ʽ�ϱ�����
    COLLECTOR_PROTOCOL_MAX
}CollectorProtocolType_E;

/*
����Kafka����ʱ��Ҫ����Ϣ
*/
typedef struct tagKafkaConnectInfo
{
    vector <string> KafkaBrokerList;    // Kafka Broker�б�(��ʽΪ IP:Port)
    
    string strTopic;                    // ʹ��kafka������Ϣ������
}KafkaConnectInfo_S;


/*
UDP Э����ҪԴ�˿ڷ�Χ��������̽��socket, Ŀ�Ķ˿�������Ӧ̽�ⱨ�ļ����̽��
*/
typedef struct tagServerAntAgentProtocolUDP
{
    UINT32 uiDestPort;               // ̽�ⱨ�ĵ�Ŀ�Ķ˿�, ����˼����˿�, ���ڼ���̽�ⱨ�Ĳ�����Ӧ��.    
    UINT32 uiSrcPortMin;             // �ͻ���̽�ⱨ��Դ�˿ڷ�ΧMin, ���ڷ���̽�ⱨ�Ĳ�����Ӧ��.
    UINT32 uiSrcPortMax;             // �ͻ���̽�ⱨ��Դ�˿ڷ�ΧMax, ���ڷ���̽�ⱨ�Ĳ�����Ӧ��.
}ServerAntAgentProtocolUDP_S;


// ServerAntAgentȫ��������Ϣ
class ServerAntAgentCfg_C
{
private:
    /* ServerAnt��ַ��Ϣ,����ͨ�� */
    UINT32 uiServerIP;                //  ServerAntServer��IP��ַ, Agent��Server�����ѯ�Ựʱʹ��.
    UINT32 uiServerDestPort;          //  ServerAntServer�Ķ˿ڵ�ַ, Agent��Server�����ѯ�Ựʱʹ��.

    CollectorProtocolType_E eCollectorProtocol; // ��CollectorͨѶЭ������, ��CollectorProtocolType_E.
    KafkaConnectInfo_S stCollectorKafkaInfo; // ��ʹ��Kafka��������ʱ��Ҫ����Ϣ
    
    UINT32 uiAgentIP;                 //  ��Agent��IP��ַ, Server��Agent������Ϣʱʹ��.
    UINT32 uiAgentDestPort;           //  ��Agent�Ķ˿ڵ�ַ, Server��Agent������Ϣʱʹ��.
    
    /* Agent ȫ�����ڿ��� */
    UINT32 uiAgentPollingTimerPeriod; // Agent Polling����, ��λΪus, Ĭ��100ms, �����趨Agent��ʱ��.    
    UINT32 uiAgentReportPeriod;       // Agent��Collector�ϱ�����, ��λPolling����, Ĭ��3000(300s).
    UINT32 uiAgentQueryPeriod;        // Agent��Server��ѯ����, ��λPolling����, Ĭ��30000(300s).
    
    /* Detect ���� */
    UINT32 uiAgentDetectPeriod;       // Agent̽������Agent����, ��λPolling����, Ĭ��20(2s).
    UINT32 uiAgentDetectTimeout;      // Agent̽�ⱨ�ĳ�ʱʱ��, ��λPolling����, Ĭ��10(1s).
    UINT32 uiAgentDetectDropThresh;   // Agent̽�ⱨ�Ķ�������, ��λΪ���ĸ���, Ĭ��5(������5��̽�ⱨ�ĳ�ʱ����Ϊ���ӳ��ֶ���).

    /* Detect Э����Ʋ��� */
    ServerAntAgentProtocolUDP_S stProtocolUDP; // UDP ̽�ⱨ��ȫ���趨,����Դ�˿ڷ�Χ��Ŀ�Ķ˿���Ϣ.

    sal_mutex_t AgentCfgLock;               // ����������
    
public:
    ServerAntAgentCfg_C();                  // �๹�캯��, ���Ĭ��ֵ.
    ~ServerAntAgentCfg_C();                 // ����������, �ͷű�Ҫ��Դ.
    
    //INT32 init();       // ���ʼ������, ���캯���Ĳ���, �ݲ�ʹ��, ������Դ����ʱ���ش���.
    
    INT32 GetServerAddress(UINT32 * puiServerIP, 
                     UINT32 * puiServerDestPort);        // ��ѯServerAntServer��ַ��Ϣ.                     
    INT32 SetServerAddress(UINT32 uiNewServerIP, 
                    UINT32 uiNewServerDestPort);         // ����ServerAntServer��ַ��Ϣ, ��0��Ч.

    INT32 GetCollectorProtocol(CollectorProtocolType_E * peProtocol);     // ��ѯ ServerAntCollector Э������, ��ǰ��֧��Kafka.
    INT32 SetCollectorProtocol(CollectorProtocolType_E eNewProtocol);      // ���� ServerAntCollector Э������.
                    
    INT32 GetCollectorKafkaInfo(
                    KafkaConnectInfo_S * pstKafkaInfo);     // ��ѯServerAntCollector��Kafak��ַ��Ϣ.
    INT32 SetCollectorKafkaInfo(
                    KafkaConnectInfo_S * pstNewKafkaInfo);      // ����ServerAntCollector��Kafka��ַ��Ϣ

    INT32 GetAgentAddress(UINT32 * puiAgentIP, 
                     UINT32 * puiAgentDestPort);         // ��ѯServerAntAgent��ַ��Ϣ.
    INT32 SetAgentAddress(UINT32 uiNewAgentIP, 
                    UINT32 uiNewAgentDestPort);          // ����ServerAntAgent��ַ��Ϣ, ��0��Ч.
    
    UINT32 GetPollingTimerPeriod()   // ��ѯPolling����
    {
        return uiAgentPollingTimerPeriod;
    }
    INT32 SetPollingTimerPeriod(UINT32 uiNewPeriod);  //����Polling����, ����������ڲ�һ����ͬʱˢ�¶�ʱ��

    UINT32 GetDetectPeriod()                         // ��ѯDetect����
    {
        return uiAgentDetectPeriod;
    }
    INT32 SetDetectPeriod(UINT32 uiNewPeriod)         // �趨Detect����
    {
        uiAgentDetectPeriod = uiNewPeriod;
        return AGENT_OK;
    }

    INT32 GetAgentIP()          // ��ѯServerAntAgent��ַ��Ϣ.
    {
        return uiAgentIP;
    }
    UINT32 GetReportPeriod()                         // ��ѯReport����
    {
        return uiAgentReportPeriod;
    }
    INT32 SetReportPeriod(UINT32 uiNewPeriod)         // �趨Report����
    {
        uiAgentReportPeriod = uiNewPeriod;
        return AGENT_OK;
    }
    
    UINT32 GetQueryPeriod()                         // ��ѯquery����
    {
        return uiAgentQueryPeriod;
    }
    INT32 SetQueryPeriod(UINT32 uiNewPeriod)         // �趨query����
    {
        uiAgentQueryPeriod = uiNewPeriod;
        return AGENT_OK;
    }
    
    UINT32 GetDetectTimeout()                         // ��ѯDetect���ĳ�ʱʱ��
    {
        return uiAgentDetectTimeout;
    }
    INT32 SetDetectTimeout(UINT32 uiNewPeriod)         // �趨Detect���ĳ�ʱʱ��
    {
        uiAgentDetectTimeout = uiNewPeriod;
        return AGENT_OK;
    }
    
    UINT32 GetDetectDropThresh()                         // ��ѯDetect���Ķ�������
    {
        return uiAgentDetectDropThresh;
    }
    INT32 SetDetectDropThresh(UINT32 uiNewThresh)         // �趨Detect���Ķ�������
    {
        uiAgentDetectDropThresh = uiNewThresh;
        return AGENT_OK;
    }

    INT32 GetProtocolUDP(UINT32 * puiSrcPortMin,
                    UINT32 * puiSrcPortMax,
                    UINT32 * puiDestPort);           // ��ѯUDP̽�ⱨ�Ķ˿ڷ�Χ.
                    
    INT32 SetProtocolUDP(UINT32 uiSrcPortMin, 
                    UINT32 uiSrcPortMax, 
                    UINT32 uiDestPort);             // �趨UDP̽�ⱨ�Ķ˿ڷ�Χ, ֻˢ�·�0�˿�
};


#endif
