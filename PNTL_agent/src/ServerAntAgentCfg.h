#ifndef __SRC_ServerAntAgentCfg_H__
#define __SRC_ServerAntAgentCfg_H__

#include <string>
#include "AgentCommon.h"

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

    UINT32 uiAgentIP;                 //  ��Agent��������IP��ַ, Agent̽��IP.
    UINT32 uiMgntIP;                  // ��Agent�Ĺ�����IP��ַ��Server��Agent������Ϣʱʹ��.

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
    string kafkaIp;
    string topic;
    UINT32 dropPkgThresh;
    UINT32 bigPkgSize;
    sal_mutex_t AgentCfgLock;               // ����������

public:

    ServerAntAgentCfg_C();                  // �๹�캯��, ���Ĭ��ֵ.
    ~ServerAntAgentCfg_C();                 // ����������, �ͷű�Ҫ��Դ.

    void GetServerAddress(UINT32 * puiServerIP,
                          UINT32 * puiServerDestPort);        // ��ѯServerAntServer��ַ��Ϣ.
    void SetServerAddress(UINT32 uiNewServerIP,
                          UINT32 uiNewServerDestPort);         // ����ServerAntServer��ַ��Ϣ, ��0��Ч.

    void GetAgentAddress(UINT32 * puiAgentIP);         // ��ѯServerAntAgent��ַ��Ϣ.
    void SetAgentAddress(UINT32 uiNewAgentIP);          // ����ServerAntAgent��ַ��Ϣ, ��0��Ч.

    void GetMgntIP(UINT32* puiMgntIP);
    void SetMgntIP(UINT32 uiNewMgntIP);         // �趨�����ip

    UINT32 GetPollingTimerPeriod();   // ��ѯPolling����
    INT32 SetPollingTimerPeriod(UINT32 uiNewPeriod);  //����Polling����, ����������ڲ�һ����ͬʱˢ�¶�ʱ��

    UINT32 GetDetectPeriod();                         // ��ѯDetect����
    void SetDetectPeriod(UINT32 uiNewPeriod);         // �趨Detect����

    UINT32 GetAgentIP();          // ��ѯServerAntAgent��ַ��Ϣ.

    UINT32 GetReportPeriod();                         // ��ѯReport����
    void SetReportPeriod(UINT32 uiNewPeriod);         // �趨Report����

    UINT32 GetQueryPeriod();                         // ��ѯquery����
    void SetQueryPeriod(UINT32 uiNewPeriod);         // �趨query����

    UINT32 GetDetectTimeout();                        // ��ѯDetect���ĳ�ʱʱ��
    void SetDetectTimeout(UINT32 uiNewPeriod);         // �趨Detect���ĳ�ʱʱ��

    UINT32 GetDetectDropThresh();                         // ��ѯDetect���Ķ�������
    void SetDetectDropThresh(UINT32 uiNewThresh);         // �趨Detect���Ķ�������

    void GetProtocolUDP(UINT32 * puiSrcPortMin,
                        UINT32 * puiSrcPortMax,
                        UINT32 * puiDestPort);           // ��ѯUDP̽�ⱨ�Ķ˿ڷ�Χ.
    void SetProtocolUDP(UINT32 uiSrcPortMin,
                        UINT32 uiSrcPortMax,
                        UINT32 uiDestPort);             // �趨UDP̽�ⱨ�Ķ˿ڷ�Χ, ֻˢ�·�0�˿�

    UINT32 GetPortCount();
    void SetPortCount(UINT32 newPortCount);

    UINT32 getDscp();
    void SetDscp(UINT32 newDscp);

    UINT32 GetBigPkgRate();
    void SetBigPkgRate(UINT32 newRate);

    UINT32 GetMaxDelay();
    void SetMaxDelay(UINT32 newMaxDelay);

    UINT32 GetBigPkgSize();
    void SetBigPkgSize(UINT32 newSize);

    string GetKafkaIp();
    void SetKafkaIp(string newIp);

    string GetTopic();

    void SetTopic(string newTopic);
};

#endif
