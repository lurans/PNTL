using namespace std;

#include "Log.h"
#include "ServerAntAgentCfg.h"

// ���캯��, ��ʼ��Ĭ��ֵ.
ServerAntAgentCfg_C::ServerAntAgentCfg_C()
{
    AGENT_CFG_INFO("Creat a new ServerAntAgentCfg");

    /* ServerAnt��ַ��Ϣ,����ͨ�� */
    uiServerIP                  = sal_inet_aton("0.0.0.0"); //  ServerAntServer��IP��ַ, Agent��Server�����ѯ�Ựʱʹ��.
    uiServerDestPort            = 0;                        //  ServerAntServer�Ķ˿ڵ�ַ, Agent��Server�����ѯ�Ựʱʹ��.

    uiAgentIP                   = sal_inet_aton("0.0.0.0"); //  ��Agent��IP��ַ, Server��Agent������Ϣʱʹ��.

    /* Agent ȫ�����ڿ��� */
    uiAgentPollingTimerPeriod   = 100000;   // Agent Polling����, ��λΪus, Ĭ��100ms, �����趨Agent��ʱ��.
    uiAgentReportPeriod         = 3000;     // Agent��Collector�ϱ�����, ��λPolling����, Ĭ��3000(300s, 5����).
    uiAgentQueryPeriod          = 9000;     // Agent��Server��ѯ����, ��λPolling����, Ĭ��36000(3600s, 1Сʱ).
    // ��ǰAgent̽���б�Ϊ��ʱ, ��ѯ���ڻ�����Ϊ��ֵ��1/1000, ��С���Ϊ300(30s).

    uiAgentDetectPeriod         = 20;       // Agent̽������Agent����, ��λPolling����, Ĭ��20(2s).
    uiAgentDetectTimeout        = 10;       // Agent̽�ⱨ�ĳ�ʱʱ��, ��λPolling����, Ĭ��10(1s).
    uiAgentDetectDropThresh     = 5;        // Agent̽�ⱨ�Ķ�������, ��λΪ���ĸ���, Ĭ��5(������5��̽�ⱨ�ĳ�ʱ����Ϊ���ӳ��ֶ���).

    stProtocolUDP.uiDestPort    = 6000;                      // UDP̽���Ŀ�Ķ˿ں�, ��ȫ��ͳһ.
    stProtocolUDP.uiSrcPortMin  = 5000;                      // UDP̽��Դ�˿ںŷ�Χ, ��ʼ��ʱ�᳢�԰󶨸ö˿�.
    stProtocolUDP.uiSrcPortMax  = 5100;                      // UDP̽��Դ�˿ںŷ�Χ, ��ʼ��ʱ�᳢�԰󶨸ö˿�.
    uiDscp = 0;
    uiMaxDelay = 0;
    uiBigPkgRate = 0;
    uiPortCount = 0;
}

// ��������,�ͷ���Դ
ServerAntAgentCfg_C::~ServerAntAgentCfg_C()
{
    AGENT_CFG_INFO("Destroy ServerAntAgentCfg");
}

void ServerAntAgentCfg_C::GetServerAddress(UINT32 * puiServerIP,  UINT32 * puiServerDestPort)
{

    *puiServerIP = uiServerIP;
    *puiServerDestPort = uiServerDestPort;
    return;
}

void ServerAntAgentCfg_C::SetServerAddress(UINT32 uiNewServerIP, UINT32 uiNewServerDestPort)
{
    uiServerIP = uiNewServerIP;
    uiServerDestPort = uiNewServerDestPort;
    return;
}

void ServerAntAgentCfg_C::GetAgentAddress(UINT32 * puiAgentIP)          // ��ѯServerAntAgent��ַ��Ϣ.
{
    *puiAgentIP = uiAgentIP;
    return;
}

void ServerAntAgentCfg_C::GetMgntIP(UINT32* puiMgntIP)
{
    *puiMgntIP = uiMgntIP;
    return;
}

void ServerAntAgentCfg_C::SetAgentAddress(UINT32 uiNewAgentIP)
{
    uiAgentIP = uiNewAgentIP;
    return;
}

void ServerAntAgentCfg_C::GetProtocolUDP(UINT32 * puiSrcPortMin,
        UINT32 * puiSrcPortMax,
        UINT32 * puiDestPort)          // ��ѯUDP̽�ⱨ�Ķ˿ڷ�Χ.
{
    *puiSrcPortMin = stProtocolUDP.uiSrcPortMin;
    *puiSrcPortMax = stProtocolUDP.uiSrcPortMax;
    *puiDestPort    = stProtocolUDP.uiDestPort;
    return;
}

void ServerAntAgentCfg_C::SetProtocolUDP(UINT32 uiSrcPortMin,
        UINT32 uiSrcPortMax,
        UINT32 uiDestPort)             // �趨UDP̽�ⱨ�Ķ˿ڷ�Χ, ֻˢ�·�0�˿�
{
    stProtocolUDP.uiSrcPortMin = uiSrcPortMin;
    stProtocolUDP.uiSrcPortMax = uiSrcPortMax;
    stProtocolUDP.uiDestPort   = uiDestPort;
    return;
}

UINT32 ServerAntAgentCfg_C::GetPollingTimerPeriod()
{
    return uiAgentPollingTimerPeriod;
}

void ServerAntAgentCfg_C::SetDetectPeriod(UINT32 uiNewPeriod)
{
    uiAgentDetectPeriod = uiNewPeriod;
}

UINT32 ServerAntAgentCfg_C::GetDetectPeriod()
{
    return uiAgentDetectPeriod;
}

UINT32 ServerAntAgentCfg_C::GetAgentIP()
{
    return uiAgentIP;
}

void ServerAntAgentCfg_C::SetMgntIP(UINT32 uiNewMgntIP)
{
    uiMgntIP = uiNewMgntIP;
}

UINT32 ServerAntAgentCfg_C::GetReportPeriod()
{
    return uiAgentReportPeriod;
}

void ServerAntAgentCfg_C::SetReportPeriod(UINT32 uiNewPeriod)
{
    uiAgentReportPeriod = uiNewPeriod;
}

UINT32 ServerAntAgentCfg_C::GetQueryPeriod()
{
    return uiAgentQueryPeriod;
}

void ServerAntAgentCfg_C::SetQueryPeriod(UINT32 uiNewPeriod)
{
    uiAgentQueryPeriod = uiNewPeriod;
}

UINT32 ServerAntAgentCfg_C::GetDetectTimeout()
{
    return uiAgentDetectTimeout;
}

void ServerAntAgentCfg_C::SetDetectTimeout(UINT32 uiNewPeriod)
{
    uiAgentDetectTimeout = uiNewPeriod;
}

UINT32 ServerAntAgentCfg_C::GetDetectDropThresh()
{
    return uiAgentDetectDropThresh;
}

void ServerAntAgentCfg_C::SetDetectDropThresh(UINT32 uiNewThresh)
{
    uiAgentDetectDropThresh = uiNewThresh;
}

UINT32 ServerAntAgentCfg_C::GetPortCount()
{
    return uiPortCount;
}

void ServerAntAgentCfg_C::SetPortCount(UINT32 newPortCount)
{
    uiPortCount = newPortCount;
}

UINT32 ServerAntAgentCfg_C::getDscp()
{
    return uiDscp;
}

void ServerAntAgentCfg_C::SetDscp(UINT32 newDscp)
{
    uiDscp = newDscp;
}

UINT32 ServerAntAgentCfg_C::GetBigPkgRate()
{
    return uiBigPkgRate;
}

void ServerAntAgentCfg_C::SetBigPkgRate(UINT32 newRate)
{
    if (GetBigPkgRate() != newRate)
    {
        BIG_PKG_RATE = 1;
    }
    uiBigPkgRate = newRate;
}

UINT32 ServerAntAgentCfg_C::GetMaxDelay()
{
    return uiMaxDelay;
}

void ServerAntAgentCfg_C::SetMaxDelay(UINT32 newMaxDelay)
{
    uiMaxDelay = newMaxDelay;
}

