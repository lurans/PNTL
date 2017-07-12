#ifndef __SRC_ServerAntAgentCfg_H__
#define __SRC_ServerAntAgentCfg_H__

#include "string"

#include "Sal.h"
#include "AgentCommon.h"
#include <vector>

const UINT32 MIN_PROBE_PERIOD = 60;
const UINT32 MAX_PROBE_PERIOD = 60 * 30;
const UINT32 MIN_REPORT_PERIOD = 60;
const UINT32 MAX_REPORT_PERIOD = 60 * 30;
const UINT32 MIN_PORT_COUNT = 1;
const UINT32 MAX_PORT_COUNT = 100;
const UINT32 MIN_DSCP = 0;
const UINT32 MAX_DSCP = 63;
const UINT32 MIN_LOSS_TIMEOUT = 1;
const UINT32 MAX_LOSS_TIMEOUT = 5;
const UINT32 MIN_BIG_PACKAGE_RATE = 0;
const UINT32 MAX_BIG_PACKAGE_RATE = 100;

/*
��ServerAntsCollector�ϱ�̽����ʱ������ͨ��, ��ǰ֧��kafka, δ��������.
*/
typedef enum  tagCollectorProtocolType
{
    COLLECTOR_PROTOCOL_NULL = 0,   // δ����
    COLLECTOR_PROTOCOL_KAFKA,       // ʹ�� kafka ��ʽ�ϱ�����
    COLLECTOR_PROTOCOL_MAX
} CollectorProtocolType_E;

/*
����Kafka����ʱ��Ҫ����Ϣ
*/
typedef struct tagKafkaConnectInfo
{
    vector <string> KafkaBrokerList;    // Kafka Broker�б�(��ʽΪ IP:Port)

    string strTopic;                    // ʹ��kafka������Ϣ������
} KafkaConnectInfo_S;


/*
UDP Э����ҪԴ�˿ڷ�Χ��������̽��socket, Ŀ�Ķ˿�������Ӧ̽�ⱨ�ļ����̽��
*/
typedef struct tagServerAntAgentProtocolUDP
{
    UINT32 uiDestPort;               // ̽�ⱨ�ĵ�Ŀ�Ķ˿�, ����˼����˿�, ���ڼ���̽�ⱨ�Ĳ�����Ӧ��.
    UINT32 uiSrcPortMin;             // �ͻ���̽�ⱨ��Դ�˿ڷ�ΧMin, ���ڷ���̽�ⱨ�Ĳ�����Ӧ��.
    UINT32 uiSrcPortMax;             // �ͻ���̽�ⱨ��Դ�˿ڷ�ΧMax, ���ڷ���̽�ⱨ�Ĳ�����Ӧ��.
} ServerAntAgentProtocolUDP_S;


// ServerAntAgentȫ��������Ϣ
class ServerAntAgentCfg_C
{
private:
    /* ServerAnt��ַ��Ϣ,����ͨ�� */
    UINT32 uiServerIP;                //  ServerAntServer��IP��ַ, Agent��Server�����ѯ�Ựʱʹ��.
    UINT32 uiServerDestPort;          //  ServerAntServer�Ķ˿ڵ�ַ, Agent��Server�����ѯ�Ựʱʹ��.

    CollectorProtocolType_E eCollectorProtocol; // ��CollectorͨѶЭ������, ��CollectorProtocolType_E.
    KafkaConnectInfo_S stCollectorKafkaInfo; // ��ʹ��Kafka��������ʱ��Ҫ����Ϣ

    UINT32 uiAgentIP;                 //  ��Agent��������IP��ַ, Agent̽��IP.
    UINT32 uiMgntIP;                  // ��Agent�Ĺ�����IP��ַ��Server��Agent������Ϣʱʹ��.
    UINT32 uiAgentDestPort;           //  ��Agent�Ķ˿ڵ�ַ, Server��Agent������Ϣʱʹ��.
    string hostname;	                   // ��Ageng����Ľڵ���������

    /* Agent ȫ�����ڿ��� */
    UINT32 uiAgentPollingTimerPeriod; // Agent Polling����, ��λΪus, Ĭ��100ms, �����趨Agent��ʱ��.
    UINT32 uiAgentReportPeriod;       // Agent��Collector�ϱ�����, ��λPolling����, Ĭ��3000(300s).
    UINT32 uiAgentQueryPeriod;        // Agent��Server��ѯ����, ��λPolling����, Ĭ��30000(300s).

    /* Detect ���� */
    UINT32 uiAgentDetectPeriod;       // Agent̽������Agent����, ��λPolling����, Ĭ��20(2s).
    UINT32 uiAgentDetectTimeout;      // Agent̽�ⱨ�ĳ�ʱʱ��, ��λPolling����, Ĭ��10(1s).
    UINT32 uiAgentDetectDropThresh;   // Agent̽�ⱨ�Ķ�������, ��λΪ���ĸ���, Ĭ��5(������5��̽�ⱨ�ĳ�ʱ����Ϊ���ӳ��ֶ���).
    UINT32 uiPortCount;               // Agent̽�ⱨ�ĵ�Դ�˿ڷ�Χ
    UINT32 uiDscp;                    // Agent̽�ⱨ�ĵ�Dscpֵ
    UINT32 uiBigPkgRate;              // Agent̽�ⱨ���д��ռ�õı���
    UINT32 uiMaxDelay;                // ���ʱ�ӣ����ڴ�ֵ�����ݲ��ϱ�

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
    INT32 GetMgntIP(UINT32* puiMgntIP);
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
        if (MIN_PROBE_PERIOD > uiNewPeriod || MAX_PROBE_PERIOD < uiNewPeriod)
        {
            return AGENT_E_ERROR;
        }
        uiAgentDetectPeriod = uiNewPeriod;
        return AGENT_OK;
    }

    UINT32 GetAgentIP()          // ��ѯServerAntAgent��ַ��Ϣ.
    {
        return uiAgentIP;
    }

    INT32 SetMgntIP(UINT32 uiNewMgntIP)         // �趨�����ip
    {
        uiMgntIP = uiNewMgntIP;
        return AGENT_OK;
    }

    UINT32 GetReportPeriod()                         // ��ѯReport����
    {
        return uiAgentReportPeriod;
    }
    INT32 SetReportPeriod(UINT32 uiNewPeriod)         // �趨Report����
    {
        if (MIN_REPORT_PERIOD > uiNewPeriod || MAX_REPORT_PERIOD < uiNewPeriod || uiNewPeriod < GetDetectPeriod())
        {
            return AGENT_E_ERROR;
        }
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
        if (MIN_LOSS_TIMEOUT > uiNewPeriod || MAX_LOSS_TIMEOUT < uiNewPeriod)
        {
            return AGENT_E_ERROR;
        }
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
    INT32 SetNewServerCfg();

    INT32 SetHostname(string newHostname)
    {
        hostname = newHostname;
        return AGENT_OK;
    }

    string GetHostname()
    {
        return hostname;
    }

    UINT32 GetPortCount()
    {
        return uiPortCount;
    }

    INT32 SetPortCount(UINT32 newPortCount)
    {
        if (MIN_PORT_COUNT > newPortCount || MAX_PORT_COUNT < newPortCount)
        {
            return AGENT_E_ERROR;
        }
        uiPortCount = newPortCount;
        return AGENT_OK;
    }
    UINT32 getDscp()
    {
        return uiDscp;
    }

    INT32 SetDscp(UINT32 newDscp)
    {
        if (MIN_DSCP > newDscp || MAX_DSCP < newDscp)
        {
            return AGENT_E_ERROR;
        }
        uiDscp = newDscp;
        return AGENT_OK;
    }

    UINT32 GetBigPkgRate()
    {
        return uiBigPkgRate;
    }

    INT32 SetBigPkgRate(UINT32 newRate)
    {
        if (MIN_BIG_PACKAGE_RATE == newRate)
        {
            SEND_BIG_PKG = 0;
            CLEAR_BIG_PKG = 1;
        }
		else if (MAX_BIG_PACKAGE_RATE == newRate)
		{
		    SEND_BIG_PKG = 1;
            CLEAR_BIG_PKG = 0;
		}
		else
		{
		    return AGENT_E_ERROR;
		}
		uiBigPkgRate = newRate;
        return AGENT_OK;
    }

    UINT32 GetMaxDelay()
    {
        return uiMaxDelay;
    }

    INT32 SetMaxDelay(UINT32 newMaxDelay)
    {
        uiMaxDelay = newMaxDelay;
        return AGENT_OK;
    }
};


#endif
