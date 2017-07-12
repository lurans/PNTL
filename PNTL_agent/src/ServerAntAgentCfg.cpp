

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

    eCollectorProtocol          = COLLECTOR_PROTOCOL_NULL;  // 与Collector通讯协议类型, 见CollectorProtocolType_E.
    stCollectorKafkaInfo.strTopic = "";                     //  kafka消息通道名.

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
    AgentCfgLock = sal_mutex_create("ServerAntAgentCfg");

}

// 析构函数,释放资源
ServerAntAgentCfg_C::~ServerAntAgentCfg_C()
{
    AGENT_CFG_INFO("Destroy ServerAntAgentCfg");

}

INT32 ServerAntAgentCfg_C::GetServerAddress(UINT32 * puiServerIP,
        UINT32 * puiServerDestPort)          // 查询ServerAntServer地址信息.
{
    LOCK();     //互斥锁保持数据一致
    if (puiServerIP)
        * puiServerIP = uiServerIP;
    if (puiServerDestPort)
        * puiServerDestPort = uiServerDestPort;
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::SetServerAddress(UINT32 uiNewServerIP,
        UINT32 uiNewServerDestPort)             // 设置ServerAntServer地址信息, 非0有效.
{
    LOCK();     //互斥锁保证数据一致
    if (uiNewServerIP)
        uiServerIP = uiNewServerIP;
    if (uiNewServerDestPort)
        uiServerDestPort = uiNewServerDestPort;
    UNLOCK();
    return AGENT_OK;
}

// 查询 ServerAntCollector 协议类型, 当前仅支持Kafka.
INT32 ServerAntAgentCfg_C::GetCollectorProtocol(CollectorProtocolType_E * peProtocol)
{
    LOCK();     //互斥锁保持数据一致
    if (peProtocol)
        * peProtocol = eCollectorProtocol;
    UNLOCK();
    return AGENT_OK;
}
// 设置 ServerAntCollector 协议类型, 非零有效
INT32 ServerAntAgentCfg_C::SetCollectorProtocol(CollectorProtocolType_E eNewProtocol)
{
    LOCK();     //互斥锁保持数据一致
    if (eNewProtocol)
        eCollectorProtocol = eNewProtocol;
    UNLOCK();
    return AGENT_OK;
}

// 查询ServerAntCollector的Kafka地址信息
INT32 ServerAntAgentCfg_C::GetCollectorKafkaInfo(
    KafkaConnectInfo_S * pstKafkaInfo)
{
    LOCK();     //互斥锁保持数据一致
    if (pstKafkaInfo)
        * pstKafkaInfo = stCollectorKafkaInfo;
    UNLOCK();
    return AGENT_OK;
}

// 设置ServerAntCollector的Kafka地址信息
INT32 ServerAntAgentCfg_C::SetCollectorKafkaInfo(
    KafkaConnectInfo_S * pstNewKafkaInfo)
{
    LOCK();     //互斥锁保持数据一致
    if (pstNewKafkaInfo)
        stCollectorKafkaInfo = * pstNewKafkaInfo;
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::GetAgentAddress(UINT32 * puiAgentIP,
        UINT32 * puiAgentDestPort)          // 查询ServerAntAgent地址信息.
{
    LOCK();     //互斥锁保持数据一致
    if (puiAgentIP)
        * puiAgentIP = uiAgentIP;
    if (puiAgentDestPort)
        * puiAgentDestPort = uiAgentDestPort;
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
        uiAgentIP = uiNewAgentIP;
    if (uiNewAgentDestPort)
        uiAgentDestPort = uiNewAgentDestPort;
    UNLOCK();
    return AGENT_OK;
}


INT32 ServerAntAgentCfg_C::GetProtocolUDP(UINT32 * puiSrcPortMin,
        UINT32 * puiSrcPortMax,
        UINT32 * puiDestPort)          // 查询UDP探测报文端口范围.
{
    LOCK();     //互斥锁保持数据一致
    if (puiSrcPortMin)
        * puiSrcPortMin = stProtocolUDP.uiSrcPortMin;
    if (puiSrcPortMax)
        * puiSrcPortMax = stProtocolUDP.uiSrcPortMax;
    if (puiDestPort)
        * puiDestPort    = stProtocolUDP.uiDestPort;
    UNLOCK();
    return AGENT_OK;
}

INT32 ServerAntAgentCfg_C::SetProtocolUDP(UINT32 uiSrcPortMin,
        UINT32 uiSrcPortMax,
        UINT32 uiDestPort)             // 设定UDP探测报文端口范围, 只刷新非0端口
{
    LOCK();     //互斥锁保证数据一致
    if (uiSrcPortMin)
        stProtocolUDP.uiSrcPortMin = uiSrcPortMin;
    if (uiSrcPortMax)
        stProtocolUDP.uiSrcPortMax = uiSrcPortMax;
    if (uiDestPort)
        stProtocolUDP.uiDestPort   = uiDestPort;
    UNLOCK();
    return AGENT_OK;
}

