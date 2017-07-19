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

// 构造函数, 初始化默认值.
ServerAntAgentCfg_C::ServerAntAgentCfg_C()
{
    AGENT_CFG_INFO("Creat a new ServerAntAgentCfg");

    /* ServerAnt地址信息,管理通道 */
    uiServerIP                  = sal_inet_aton("0.0.0.0"); //  ServerAntServer的IP地址, Agent向Server发起查询会话时使用.
    uiServerDestPort            = 0;                        //  ServerAntServer的端口地址, Agent向Server发起查询会话时使用.

    uiAgentIP                   = sal_inet_aton("0.0.0.0"); //  本Agent的IP地址, Server向Agent推送消息时使用.
    uiAgentDestPort             = 0;                        //  本Agent的端口地址, Server向Agent推送消息时使用.

    /* Agent 全局周期控制 */
    uiAgentPollingTimerPeriod   = 100000;   // Agent Polling周期, 单位为us, 默认100ms, 用于设定Agent定时器.
    uiAgentReportPeriod         = 3000;     // Agent向Collector上报周期, 单位Polling周期, 默认3000(300s, 5分钟).
    uiAgentQueryPeriod          = 9000;     // Agent向Server查询周期, 单位Polling周期, 默认36000(3600s, 1小时).
    // 当前Agent探测列表为空时, 查询周期会缩短为该值的1/1000, 最小间隔为300(30s).

    uiAgentDetectPeriod         = 20;       // Agent探测其他Agent周期, 单位Polling周期, 默认20(2s).
    uiAgentDetectTimeout        = 10;       // Agent探测报文超时时间, 单位Polling周期, 默认10(1s).
    uiAgentDetectDropThresh     = 5;        // Agent探测报文丢包门限, 单位为报文个数, 默认5(即连续5个探测报文超时即认为链接出现丢包).

    stProtocolUDP.uiDestPort    = 6000;                      // UDP探测的目的端口号, 需全局统一.
    stProtocolUDP.uiSrcPortMin  = 5000;                      // UDP探测源端口号范围, 初始化时会尝试绑定该端口.
    stProtocolUDP.uiSrcPortMax  = 5100;                      // UDP探测源端口号范围, 初始化时会尝试绑定该端口.
    uiDscp = 0;
    uiMaxDelay = 0;
    uiBigPkgRate = 0;
    uiPortCount = 0;
    AgentCfgLock = sal_mutex_create("ServerAntAgentCfg");

}

// 析构函数,释放资源
ServerAntAgentCfg_C::~ServerAntAgentCfg_C()
{
    AGENT_CFG_INFO("Destroy ServerAntAgentCfg");
    sal_mutex_destroy(AgentCfgLock);
}

INT32 ServerAntAgentCfg_C::GetServerAddress(UINT32 * puiServerIP,  UINT32 * puiServerDestPort)          // 查询ServerAntServer地址信息.
{
    LOCK();     //互斥锁保持数据一致
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
        UINT32 uiNewServerDestPort)             // 设置ServerAntServer地址信息, 非0有效.
{
    LOCK();     //互斥锁保证数据一致
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
        UINT32 * puiAgentDestPort)          // 查询ServerAntAgent地址信息.
{
    LOCK();     //互斥锁保持数据一致
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
        UINT32 uiNewAgentDestPort)             // 设置ServerAntAgent地址信息, 非0有效.
{
    LOCK();     //互斥锁保证数据一致
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
        UINT32 * puiDestPort)          // 查询UDP探测报文端口范围.
{
    LOCK();     //互斥锁保持数据一致
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
        UINT32 uiDestPort)             // 设定UDP探测报文端口范围, 只刷新非0端口
{
    LOCK();     //互斥锁保证数据一致
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

