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
向ServerAntsCollector上报探测结果时的数据通道, 当前支持kafka, 未来可扩充.
*/
typedef enum  tagCollectorProtocolType
{
    COLLECTOR_PROTOCOL_NULL = 0,   // 未配置
    COLLECTOR_PROTOCOL_KAFKA,       // 使用 kafka 方式上报数据
    COLLECTOR_PROTOCOL_MAX
} CollectorProtocolType_E;

/*
建立Kafka连接时需要的信息
*/
typedef struct tagKafkaConnectInfo
{
    vector <string> KafkaBrokerList;    // Kafka Broker列表(格式为 IP:Port)

    string strTopic;                    // 使用kafka传输消息的主题
} KafkaConnectInfo_S;


/*
UDP 协议需要源端口范围用于申请探测socket, 目的端口用于响应探测报文及填充探测
*/
typedef struct tagServerAntAgentProtocolUDP
{
    UINT32 uiDestPort;               // 探测报文的目的端口, 服务端监听端口, 用于监听探测报文并进行应答.
    UINT32 uiSrcPortMin;             // 客户端探测报文源端口范围Min, 用于发送探测报文并监听应答.
    UINT32 uiSrcPortMax;             // 客户端探测报文源端口范围Max, 用于发送探测报文并监听应答.
} ServerAntAgentProtocolUDP_S;


// ServerAntAgent全局配置信息
class ServerAntAgentCfg_C
{
private:
    /* ServerAnt地址信息,管理通道 */
    UINT32 uiServerIP;                //  ServerAntServer的IP地址, Agent向Server发起查询会话时使用.
    UINT32 uiServerDestPort;          //  ServerAntServer的端口地址, Agent向Server发起查询会话时使用.

    CollectorProtocolType_E eCollectorProtocol; // 与Collector通讯协议类型, 见CollectorProtocolType_E.
    KafkaConnectInfo_S stCollectorKafkaInfo; // 当使用Kafka建立连接时需要的信息

    UINT32 uiAgentIP;                 //  本Agent的数据面IP地址, Agent探测IP.
    UINT32 uiMgntIP;                  // 本Agent的管理面IP地址，Server向Agent推送消息时使用.
    UINT32 uiAgentDestPort;           //  本Agent的端口地址, Server向Agent推送消息时使用.
    string hostname;	                   // 本Ageng部署的节点主机名称

    /* Agent 全局周期控制 */
    UINT32 uiAgentPollingTimerPeriod; // Agent Polling周期, 单位为us, 默认100ms, 用于设定Agent定时器.
    UINT32 uiAgentReportPeriod;       // Agent向Collector上报周期, 单位Polling周期, 默认3000(300s).
    UINT32 uiAgentQueryPeriod;        // Agent向Server查询周期, 单位Polling周期, 默认30000(300s).

    /* Detect 控制 */
    UINT32 uiAgentDetectPeriod;       // Agent探测其他Agent周期, 单位Polling周期, 默认20(2s).
    UINT32 uiAgentDetectTimeout;      // Agent探测报文超时时间, 单位Polling周期, 默认10(1s).
    UINT32 uiAgentDetectDropThresh;   // Agent探测报文丢包门限, 单位为报文个数, 默认5(即连续5个探测报文超时即认为链接出现丢包).
    UINT32 uiPortCount;               // Agent探测报文的源端口范围
    UINT32 uiDscp;                    // Agent探测报文的Dscp值
    UINT32 uiBigPkgRate;              // Agent探测报文中大包占用的比例
    UINT32 uiMaxDelay;                // 最大时延，低于此值的数据不上报

    /* Detect 协议控制参数 */
    ServerAntAgentProtocolUDP_S stProtocolUDP; // UDP 探测报文全局设定,包括源端口范围及目的端口信息.

    sal_mutex_t AgentCfgLock;               // 互斥锁保护

public:
    ServerAntAgentCfg_C();                  // 类构造函数, 填充默认值.
    ~ServerAntAgentCfg_C();                 // 类析构函数, 释放必要资源.

    //INT32 init();       // 类初始化函数, 构造函数的补充, 暂不使用, 申请资源出错时返回错误.

    INT32 GetServerAddress(UINT32 * puiServerIP,
                           UINT32 * puiServerDestPort);        // 查询ServerAntServer地址信息.
    INT32 SetServerAddress(UINT32 uiNewServerIP,
                           UINT32 uiNewServerDestPort);         // 设置ServerAntServer地址信息, 非0有效.

    INT32 GetCollectorProtocol(CollectorProtocolType_E * peProtocol);     // 查询 ServerAntCollector 协议类型, 当前仅支持Kafka.
    INT32 SetCollectorProtocol(CollectorProtocolType_E eNewProtocol);      // 设置 ServerAntCollector 协议类型.

    INT32 GetCollectorKafkaInfo(
        KafkaConnectInfo_S * pstKafkaInfo);     // 查询ServerAntCollector的Kafak地址信息.
    INT32 SetCollectorKafkaInfo(
        KafkaConnectInfo_S * pstNewKafkaInfo);      // 设置ServerAntCollector的Kafka地址信息

    INT32 GetAgentAddress(UINT32 * puiAgentIP,
                          UINT32 * puiAgentDestPort);         // 查询ServerAntAgent地址信息.
    INT32 GetMgntIP(UINT32* puiMgntIP);
    INT32 SetAgentAddress(UINT32 uiNewAgentIP,
                          UINT32 uiNewAgentDestPort);          // 设置ServerAntAgent地址信息, 非0有效.

    UINT32 GetPollingTimerPeriod()   // 查询Polling周期
    {
        return uiAgentPollingTimerPeriod;
    }
    INT32 SetPollingTimerPeriod(UINT32 uiNewPeriod);  //设置Polling周期, 如跟已有周期不一致则同时刷新定时器

    UINT32 GetDetectPeriod()                         // 查询Detect周期
    {
        return uiAgentDetectPeriod;
    }
    INT32 SetDetectPeriod(UINT32 uiNewPeriod)         // 设定Detect周期
    {
        if (MIN_PROBE_PERIOD > uiNewPeriod || MAX_PROBE_PERIOD < uiNewPeriod)
        {
            return AGENT_E_ERROR;
        }
        uiAgentDetectPeriod = uiNewPeriod;
        return AGENT_OK;
    }

    UINT32 GetAgentIP()          // 查询ServerAntAgent地址信息.
    {
        return uiAgentIP;
    }

    INT32 SetMgntIP(UINT32 uiNewMgntIP)         // 设定管理口ip
    {
        uiMgntIP = uiNewMgntIP;
        return AGENT_OK;
    }

    UINT32 GetReportPeriod()                         // 查询Report周期
    {
        return uiAgentReportPeriod;
    }
    INT32 SetReportPeriod(UINT32 uiNewPeriod)         // 设定Report周期
    {
        if (MIN_REPORT_PERIOD > uiNewPeriod || MAX_REPORT_PERIOD < uiNewPeriod || uiNewPeriod < GetDetectPeriod())
        {
            return AGENT_E_ERROR;
        }
        uiAgentReportPeriod = uiNewPeriod;
        return AGENT_OK;
    }

    UINT32 GetQueryPeriod()                         // 查询query周期
    {
        return uiAgentQueryPeriod;
    }
    INT32 SetQueryPeriod(UINT32 uiNewPeriod)         // 设定query周期
    {
        uiAgentQueryPeriod = uiNewPeriod;
        return AGENT_OK;
    }

    UINT32 GetDetectTimeout()                         // 查询Detect报文超时时间
    {
        return uiAgentDetectTimeout;
    }
    INT32 SetDetectTimeout(UINT32 uiNewPeriod)         // 设定Detect报文超时时间
    {
        if (MIN_LOSS_TIMEOUT > uiNewPeriod || MAX_LOSS_TIMEOUT < uiNewPeriod)
        {
            return AGENT_E_ERROR;
        }
        uiAgentDetectTimeout = uiNewPeriod;
        return AGENT_OK;
    }

    UINT32 GetDetectDropThresh()                         // 查询Detect报文丢包门限
    {
        return uiAgentDetectDropThresh;
    }
    INT32 SetDetectDropThresh(UINT32 uiNewThresh)         // 设定Detect报文丢包门限
    {
        uiAgentDetectDropThresh = uiNewThresh;
        return AGENT_OK;
    }

    INT32 GetProtocolUDP(UINT32 * puiSrcPortMin,
                         UINT32 * puiSrcPortMax,
                         UINT32 * puiDestPort);           // 查询UDP探测报文端口范围.

    INT32 SetProtocolUDP(UINT32 uiSrcPortMin,
                         UINT32 uiSrcPortMax,
                         UINT32 uiDestPort);             // 设定UDP探测报文端口范围, 只刷新非0端口
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
