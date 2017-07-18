using namespace std;

#include "Log.h"
#include "ServerAntAgentCfg.h"
#include "AgentCommon.h"

#define LOCK() \
        if (AgentCfgLock) \
            sal_mutex_take(AgentCfgLock, sal_mutex_FOREVER)

#define UNLOCK() \
        if (AgentCfgLock) \
            sal_mutex_give(AgentCfgLock)

// ���캯��, ��ʼ��Ĭ��ֵ.
ServerAntAgentCfg_C::ServerAntAgentCfg_C()
{
    AGENT_CFG_INFO("Creat a new ServerAntAgentCfg");

    /* ServerAnt��ַ��Ϣ,����ͨ�� */
    uiServerIP                  = sal_inet_aton("0.0.0.0"); //  ServerAntServer��IP��ַ, Agent��Server�����ѯ�Ựʱʹ��.
    uiServerDestPort            = 0;                        //  ServerAntServer�Ķ˿ڵ�ַ, Agent��Server�����ѯ�Ựʱʹ��.

    uiAgentIP                   = sal_inet_aton("0.0.0.0"); //  ��Agent��IP��ַ, Server��Agent������Ϣʱʹ��.
    uiAgentDestPort             = 0;                        //  ��Agent�Ķ˿ڵ�ַ, Server��Agent������Ϣʱʹ��.

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
    AgentCfgLock = sal_mutex_create("ServerAntAgentCfg");

}

// ��������,�ͷ���Դ
ServerAntAgentCfg_C::~ServerAntAgentCfg_C()
{
    AGENT_CFG_INFO("Destroy ServerAntAgentCfg");
    sal_mutex_destroy(AgentCfgLock);
}

INT32 ServerAntAgentCfg_C::GetServerAddress(UINT32 * puiServerIP,  UINT32 * puiServerDestPort)          // ��ѯServerAntServer��ַ��Ϣ.
{
    LOCK();     //��������������һ��
    if (puiServerIP)
    {
        * puiServerIP = uiServerIP;
    }
    if (puiServerDestPort)
    {
        * puiServerDestPort = uiServerDestPort;
    }
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::SetServerAddress(UINT32 uiNewServerIP,
        UINT32 uiNewServerDestPort)             // ����ServerAntServer��ַ��Ϣ, ��0��Ч.
{
    LOCK();     //��������֤����һ��
    if (uiNewServerIP)
    {
        uiServerIP = uiNewServerIP;
    }
    if (uiNewServerDestPort)
    {
        uiServerDestPort = uiNewServerDestPort;
    }
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::GetAgentAddress(UINT32 * puiAgentIP,
        UINT32 * puiAgentDestPort)          // ��ѯServerAntAgent��ַ��Ϣ.
{
    LOCK();     //��������������һ��
    if (puiAgentIP)
    {
        * puiAgentIP = uiAgentIP;
    }
    if (puiAgentDestPort)
    {
        * puiAgentDestPort = uiAgentDestPort;
    }
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::GetMgntIP(UINT32* puiMgntIP)
{
    if (puiMgntIP)
    {
        *puiMgntIP = uiMgntIP;
    }
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::SetAgentAddress(UINT32 uiNewAgentIP,
        UINT32 uiNewAgentDestPort)             // ����ServerAntAgent��ַ��Ϣ, ��0��Ч.
{
    LOCK();     //��������֤����һ��
    if (uiNewAgentIP)
    {
        uiAgentIP = uiNewAgentIP;
    }
    if (uiNewAgentDestPort)
    {
        uiAgentDestPort = uiNewAgentDestPort;
    }
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::GetProtocolUDP(UINT32 * puiSrcPortMin,
        UINT32 * puiSrcPortMax,
        UINT32 * puiDestPort)          // ��ѯUDP̽�ⱨ�Ķ˿ڷ�Χ.
{
    LOCK();     //��������������һ��
    if (puiSrcPortMin)
    {
        * puiSrcPortMin = stProtocolUDP.uiSrcPortMin;
    }
    if (puiSrcPortMax)
    {
        * puiSrcPortMax = stProtocolUDP.uiSrcPortMax;
    }
    if (puiDestPort)
    {
        * puiDestPort    = stProtocolUDP.uiDestPort;
    }
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::SetProtocolUDP(UINT32 uiSrcPortMin,
        UINT32 uiSrcPortMax,
        UINT32 uiDestPort)             // �趨UDP̽�ⱨ�Ķ˿ڷ�Χ, ֻˢ�·�0�˿�
{
    LOCK();     //��������֤����һ��
    if (uiSrcPortMin)
    {
        stProtocolUDP.uiSrcPortMin = uiSrcPortMin;
    }
    if (uiSrcPortMax)
    {
        stProtocolUDP.uiSrcPortMax = uiSrcPortMax;
    }
    if (uiDestPort)
    {
        stProtocolUDP.uiDestPort   = uiDestPort;
    }
    UNLOCK();
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetPollingTimerPeriod()
{
    return uiAgentPollingTimerPeriod;
}

INT32 ServerAntAgentCfg_C::SetDetectPeriod(UINT32 uiNewPeriod)
{
    if (MIN_PROBE_PERIOD > uiNewPeriod || MAX_PROBE_PERIOD < uiNewPeriod)
    {
        return AGENT_E_ERROR;
    }
    uiAgentDetectPeriod = uiNewPeriod;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetDetectPeriod()
{
    return uiAgentDetectPeriod;
}

UINT32 ServerAntAgentCfg_C::GetAgentIP()
{
    return uiAgentIP;
}

INT32 ServerAntAgentCfg_C::SetMgntIP(UINT32 uiNewMgntIP)
{
    uiMgntIP = uiNewMgntIP;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetReportPeriod()
{
    return uiAgentReportPeriod;
}

INT32 ServerAntAgentCfg_C::SetReportPeriod(UINT32 uiNewPeriod)
{
    if (MIN_REPORT_PERIOD > uiNewPeriod || MAX_REPORT_PERIOD < uiNewPeriod || uiNewPeriod < GetDetectPeriod())
    {
        return AGENT_E_PARA;
    }
    uiAgentReportPeriod = uiNewPeriod;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetQueryPeriod()
{
    return uiAgentQueryPeriod;
}

INT32 ServerAntAgentCfg_C::SetQueryPeriod(UINT32 uiNewPeriod)
{
    uiAgentQueryPeriod = uiNewPeriod;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetDetectTimeout()
{
    return uiAgentDetectTimeout;
}

INT32 ServerAntAgentCfg_C::SetDetectTimeout(UINT32 uiNewPeriod)
{
    if (MIN_LOSS_TIMEOUT > uiNewPeriod || MAX_LOSS_TIMEOUT < uiNewPeriod)
    {
        return AGENT_E_PARA;
    }
    uiAgentDetectTimeout = uiNewPeriod;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetDetectDropThresh()
{
    return uiAgentDetectDropThresh;
}

INT32 ServerAntAgentCfg_C::SetDetectDropThresh(UINT32 uiNewThresh)
{
    uiAgentDetectDropThresh = uiNewThresh;
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::SetHostname(string newHostname)
{
    hostname = newHostname;
    return AGENT_OK;
}

string ServerAntAgentCfg_C::GetHostname()
{
    return hostname;
}

UINT32 ServerAntAgentCfg_C::GetPortCount()
{
    return uiPortCount;
}

INT32 ServerAntAgentCfg_C::SetPortCount(UINT32 newPortCount)
{
    if (MIN_PORT_COUNT > newPortCount || MAX_PORT_COUNT < newPortCount)
    {
        return AGENT_E_PARA;
    }
    uiPortCount = newPortCount;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::getDscp()
{
    return uiDscp;
}

INT32 ServerAntAgentCfg_C::SetDscp(UINT32 newDscp)
{
    if (MIN_DSCP > newDscp || MAX_DSCP < newDscp)
    {
        return AGENT_E_PARA;
    }
    uiDscp = newDscp;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetBigPkgRate()
{
    return uiBigPkgRate;
}

INT32 ServerAntAgentCfg_C::SetBigPkgRate(UINT32 newRate)
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
        return AGENT_E_PARA;
    }
    uiBigPkgRate = newRate;
    return AGENT_OK;
}

UINT32 ServerAntAgentCfg_C::GetMaxDelay()
{
    return uiMaxDelay;
}

INT32 ServerAntAgentCfg_C::SetMaxDelay(UINT32 newMaxDelay)
{
    uiMaxDelay = newMaxDelay;
    return AGENT_OK;
}

