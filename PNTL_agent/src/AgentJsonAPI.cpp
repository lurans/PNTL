
//#include <boost/exception/all.hpp>
#include <boost/property_tree/json_parser.hpp>

using namespace std;
// 使用boost的property_tree扩展库处理json格式数据.
using namespace boost::property_tree;

#include "Log.h"
#include "AgentJsonAPI.h"

/*
{
"ServerAntServer" :
    {
        "IP"    : "127.0.0.1", 
        "Port"  : "2400"
    },
"ServerAntCollector" :
    {
        "IP"    : "127.0.0.1", 
        "Port"  : "2400"
    },
"ServerAntAgent" :
    {
        "IP"    : "10.78.221.45", 
        "Port"  : "2400"
    }
}
*/
// 解析Agent本地配置文件, 完成初始化配置.
INT32 ParserLocalCfg(const char * pcJsonData, ServerAntAgentCfg_C * pcCfg)
{
    INT32 iRet = AGENT_OK;
    
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        UINT32 uiIp, uiPort, uiData;
        string strTemp;
        
        // pcData字符串转存stringstream格式, 方便后续boost::property_tree处理.
        stringstream ssStringData(pcJsonData);

        // boost::property_tree对象, 用于存储json格式数据.
        ptree ptDataRoot, ptDataTmp;
        
        read_json(ssStringData, ptDataRoot);
        
        // 解析LogCfg数据.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("LogCfg");
        strTemp = ptDataTmp.get<string>("LOG_DIR");
        iRet = SetNewLogDir(strTemp);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetNewLogDir failed[%d]", iRet);
            return iRet;
        }
        
        // 刷新日志目录后, 再打印当前配置
        JSON_PARSER_INFO("JsonData[%s]", pcJsonData);

        // 解析ServerAntServer数据.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntServer");
        strTemp = ptDataTmp.get<string>("IP");
        uiIp = sal_inet_aton(strTemp.c_str());
        uiPort = ptDataTmp.get<UINT32>("Port");
        iRet = pcCfg->SetServerAddress(uiIp, uiPort);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetServerAddress failed[%d]", iRet);
            return iRet;
        }

        // 解析ServerAntCollector数据.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntCollector");
        
        strTemp = ptDataTmp.get<string>("Protocol");
        if ("Kafka" == strTemp)
        {
            ptree ptEntry;
            ptree ptArray;
            KafkaConnectInfo_S stKafkaConnectInfo;

            // 读取 topic
            ptDataTmp.clear();
            ptDataTmp = ptDataRoot.get_child("ServerAntCollector.KafkaInfo");
            stKafkaConnectInfo.strTopic = ptDataTmp.get<string>("Topic");

            // 读取 BrokerList
            ptArray.clear();
            ptArray = ptDataRoot.get_child("ServerAntCollector.KafkaInfo.BrokerList");
            // 遍历ptArray, 逐个添加Broker
            for (ptree::iterator itFlow = ptArray.begin(); itFlow != ptArray.end(); itFlow++)
            {
                ptEntry = itFlow->second; // first为空, boost格式
                strTemp = ptEntry.data();

                // 检查是否有重复的Broker服务器信息
                vector<string>::iterator pstrKafkaBrokerInfo;
                for( pstrKafkaBrokerInfo = stKafkaConnectInfo.KafkaBrokerList.begin(); 
                     pstrKafkaBrokerInfo != stKafkaConnectInfo.KafkaBrokerList.end(); 
                     pstrKafkaBrokerInfo++ )
                {
                    if (strTemp == *pstrKafkaBrokerInfo)
                    {
                        MSG_CLIENT_INFO("Find Same Broker server address[%s] in cfg file", strTemp.c_str());
                        return AGENT_E_PARA;
                    }
                }
                stKafkaConnectInfo.KafkaBrokerList.push_back(strTemp);
            }

            // 保存kafka配置信息
            iRet = pcCfg->SetCollectorKafkaInfo(&stKafkaConnectInfo);
            if (iRet)
            {
                    JSON_PARSER_ERROR("SetCollectorKafkaInfo failed[%d]", iRet);
                    return iRet;
            }

            // 刷新 Collector 配置
            iRet = pcCfg->SetCollectorProtocol(COLLECTOR_PROTOCOL_KAFKA);
            if (iRet)
            {
                    JSON_PARSER_ERROR("SetCollectorKafkaInfo failed[%d]", iRet);
                    return iRet;
            }
        }
        else
        {
            JSON_PARSER_ERROR("Unknown Server Ants Collector Protocol[%s] In Cfg info", strTemp.c_str());
            return AGENT_E_PARA;
        }
        

        // 解析ServerAntAgent数据.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntAgent");
		strTemp = ptDataTmp.get<string>("MgntIP");
		
		uiIp = sal_inet_aton(strTemp.c_str());
        iRet = pcCfg->SetMgntIP(uiIp);
		if (iRet)
        {
            JSON_PARSER_ERROR("SetMnMgntIPgtIP failed[%d]", iRet);
            return iRet;
        }
		
        strTemp = ptDataTmp.get<string>("AgentIP");
        uiIp = sal_inet_aton(strTemp.c_str());
        uiPort = ptDataTmp.get<UINT32>("Port");
        iRet = pcCfg->SetAgentAddress(uiIp, uiPort);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetAgentAddress failed[%d]", iRet);
            return iRet;
        }
        uiData = ptDataTmp.get<UINT32>("PollingTimerPeriod");
        /*iRet = pcCfg->SetPollingTimerPeriod(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetPollingTimerPeriod failed[%d]", iRet);
            return iRet;
        }*/
        uiData = ptDataTmp.get<UINT32>("ReportPeriod");
        iRet = pcCfg->SetReportPeriod(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetReportPeriod failed[%d]", iRet);
            return iRet;
        }
        uiData = ptDataTmp.get<UINT32>("QueryPeriod");
        iRet = pcCfg->SetQueryPeriod(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetQueryPeriod failed[%d]", iRet);
            return iRet;
        }
        uiData = ptDataTmp.get<UINT32>("DetectPeriod");
        iRet = pcCfg->SetDetectPeriod(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDetectPeriod failed[%d]", iRet);
            return iRet;
        }
        uiData = ptDataTmp.get<UINT32>("DetectTimeoutPeriod");
        iRet = pcCfg->SetDetectTimeout(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDetectTimeout failed[%d]", iRet);
            return iRet;
        }
        uiData = ptDataTmp.get<UINT32>("DetectDropThresh");
        iRet = pcCfg->SetDetectDropThresh(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDetectDropThresh failed[%d]", iRet);
            return iRet;
        }

        // 解析ServerAntAgent.ProtocolUDP数据.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntAgent.ProtocolUDP");
        UINT32 uiSrcPortMin = ptDataTmp.get<UINT32>("SrcPortMin");
        UINT32 uiSrcPortMax = ptDataTmp.get<UINT32>("SrcPortMax");
        UINT32 uiDestPort   = ptDataTmp.get<UINT32>("DestPort");
        iRet = pcCfg->SetProtocolUDP(uiSrcPortMin, uiSrcPortMax, uiDestPort);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetProtocolUDP failed[%d]", iRet);
            return iRet;
        }
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when ParserLocalCfg. LocalCfg:[%s]", e.what(), pcJsonData);
        return AGENT_E_ERROR;
    }
    
    return iRet;
}

/*
{
    "orgnizationSignature": "HuaweiDC3ServerAntsProbelistRequest",
    "serverIP": "10.1.1.1",
    "action": "get",
    "content": "probe-list"
}
*/
#define NormalFlowRequestSignature    "HuaweiDCAnts"
#define NormalFlowRequestAction       "RequestServerProbeList"

// 生成json格式的字符串, 用于发起向Server请求Probe-list时提交的post data.
INT32 CreatProbeListRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData)
{
    INT32 iRet = AGENT_OK;

    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        stringstream ssJsonData;
        ptree ptDataRoot, ptDataTemp;
        ptDataRoot.put("MessageSignature", NormalFlowRequestSignature);
        ptDataRoot.put("Action", NormalFlowRequestAction);
        ptDataRoot.put("target", "ServerTopology");
        ptDataRoot.put("scope", "global");
        
        UINT32 uiIp;
        iRet = pcCfg->GetAgentAddress(&uiIp, NULL);
        if (iRet)
        {
            JSON_PARSER_ERROR("GetAgentAddress failed[%d]", iRet);
            return iRet;
        }
        ptDataTemp.put("agent-ip", sal_inet_ntoa(uiIp));
        ptDataRoot.put_child("content", ptDataTemp);
        
        write_json(ssJsonData, ptDataRoot);
        (*pssPostData) << ssJsonData.str();
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when CreatProbeListRequestPostData.", e.what());
        return AGENT_E_ERROR;
    }

    return AGENT_OK;
}

INT32 CreatAgentIPRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData)
{
    INT32 iRet = AGENT_OK;

    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        stringstream ssJsonData;
        ptree ptDataRoot;
        
        UINT32 uiIp, uiMgntIp;
        iRet = pcCfg->GetAgentAddress(&uiIp, NULL);
        if (iRet)
        {
            JSON_PARSER_ERROR("GetAgentAddress failed[%d]", iRet);
            return iRet;
        }

		iRet = pcCfg->GetMgntIP(&uiMgntIp);
		
        ptDataRoot.put("vbond-ip", sal_inet_ntoa(uiIp));    // 数据面IP 
		ptDataRoot.put("agent-ip", sal_inet_ntoa(uiMgntIp));
		
        write_json(ssJsonData, ptDataRoot);
        (*pssPostData) << ssJsonData.str();
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when CreatAgentIPRequestPostData.", e.what());
        return AGENT_E_ERROR;
    }

    return AGENT_OK;
}

/*
{
    "orgnizationSignature": "HuaweiDC3ServerAntsFull",
    "flow": 
    {
        
        "sip": "",
        "dip": "",
        "sport": "",
        "dport": ""
        "ip-protocol": "icmp:udp",
        "dscp": "",
        "urgent-flag": "0:1",
        "topology-tag": 
        {
            "level": "",
            "svid": "",
            "dvid": ""
        },
        "time": 
        {
            "t1": "",
            "t2": "",
            "t3": "",
            "t4": ""
        },
        "statistics": 
        {
            "packet-sent": "",
            "packet-drops": "",            
            "50percentile": "",
            "99percentile": ""
            "standard-deviation": ""
            "drop-notices": "",
        },
    },
}
*/
#define LatencyReportSignature    "HuaweiDC3ServerAntsFull"

// 生成json格式的字符串, 用于向Analyzer上报延时信息.
INT32 CreatLatencyReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData)
{

    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        ptree ptDataRoot, ptDataFlowArray, ptDataFlowEntry;
        ptree ptDataFlowEntryTemp;
        
        stringstream ssJsonData;

        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", LatencyReportSignature);
        
        // 清空Flow Entry Array
        ptDataFlowArray.clear();
        
        // 生成一个Flow Entry的数据
        {
            ptDataFlowEntry.clear();
            ptDataFlowEntry.put("sip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiSrcIP));
            ptDataFlowEntry.put("dip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiDestIP));
            ptDataFlowEntry.put("sport", pstAgentFlowEntry->stFlowKey.uiSrcPort);
            ptDataFlowEntry.put("dport", pstAgentFlowEntry->stFlowKey.uiDestPort);
            switch (pstAgentFlowEntry->stFlowKey.eProtocol)
            {
                case AGENT_DETECT_PROTOCOL_UDP:
                    ptDataFlowEntry.put("ip-protocol","udp");
                    break;
                case AGENT_DETECT_PROTOCOL_TCP:
                    ptDataFlowEntry.put("ip-protocol","tcp");
                    break;
                case AGENT_DETECT_PROTOCOL_ICMP:
                    ptDataFlowEntry.put("ip-protocol","icmp");
                    break;
                default:
                    ptDataFlowEntry.put("ip-protocol","null");
                    break;
            }
            ptDataFlowEntry.put("dscp",pstAgentFlowEntry->stFlowKey.uiDscp);
            ptDataFlowEntry.put("urgent-flag",pstAgentFlowEntry->stFlowKey.uiUrgentFlow);

            // 处理topology-tag信息
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("level", pstAgentFlowEntry->stFlowKey.stServerTopo.uiLevel);
#if 0
            ssJsonData.str("");
            ssJsonData <<"0x" << hex << setw(8) << setfill('0') << pstAgentFlowEntry->stFlowKey.stServerTopo.uiSvid;
            ptDataFlowEntryTemp.put("svid", ssJsonData.str().c_str());
            ssJsonData.str("");
            ssJsonData <<"0x" << hex << setw(8) << setfill('0') << pstAgentFlowEntry->stFlowKey.stServerTopo.uiDvid;
            ptDataFlowEntryTemp.put("dvid", ssJsonData.str().c_str());
#else
            ptDataFlowEntryTemp.put("sid", pstAgentFlowEntry->stFlowKey.stServerTopo.uiSvid);
            ptDataFlowEntryTemp.put("did", pstAgentFlowEntry->stFlowKey.stServerTopo.uiDvid);
#endif
            
            ptDataFlowEntry.put_child("topology-tag", ptDataFlowEntryTemp);
            
            // 处理time信息
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("t1", pstAgentFlowEntry->stFlowDetectResult.lT1);
            ptDataFlowEntryTemp.put("t2", pstAgentFlowEntry->stFlowDetectResult.lT2);
            ptDataFlowEntryTemp.put("t3", pstAgentFlowEntry->stFlowDetectResult.lT3);
            ptDataFlowEntryTemp.put("t4", pstAgentFlowEntry->stFlowDetectResult.lT4);
            ptDataFlowEntry.put_child("time", ptDataFlowEntryTemp);

            // 处理statistics信息
            ptDataFlowEntryTemp.clear();        
            ptDataFlowEntryTemp.put("packet-sent", pstAgentFlowEntry->stFlowDetectResult.lPktSentCounter);
            ptDataFlowEntryTemp.put("packet-drops", pstAgentFlowEntry->stFlowDetectResult.lPktDropCounter);
            ptDataFlowEntryTemp.put("50percentile", pstAgentFlowEntry->stFlowDetectResult.lLatency50Percentile);
            ptDataFlowEntryTemp.put("99percentile", pstAgentFlowEntry->stFlowDetectResult.lLatency99Percentile);            
            ptDataFlowEntryTemp.put("standard-deviation", pstAgentFlowEntry->stFlowDetectResult.lLatencyStandardDeviation);
            ptDataFlowEntryTemp.put("min", pstAgentFlowEntry->stFlowDetectResult.lLatencyMin);
            ptDataFlowEntryTemp.put("max", pstAgentFlowEntry->stFlowDetectResult.lLatencyMax);
            ptDataFlowEntryTemp.put("drop-notices", pstAgentFlowEntry->stFlowDetectResult.lDropNotesCounter);
            ptDataFlowEntry.put_child("statistics", ptDataFlowEntryTemp);

            // 加入json数组, 暂不使用数组, Collector不支持.
            ptDataFlowArray.push_back(make_pair("", ptDataFlowEntry));
        }

        ptDataRoot.put_child("flow", ptDataFlowArray);
        //ptDataRoot.put_child("flow", ptDataFlowEntry);

        ssJsonData.clear();
        ssJsonData.str("");
        write_json(ssJsonData, ptDataRoot);    
        (*pssReportData) << ssJsonData.str();
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when CreatLatencyReportData.", e.what());
        return AGENT_E_ERROR;
    }

    return AGENT_OK;
}


/*
{
    "orgnizationSignature": "HuaweiDC3ServerAntsDropNotice",
    "flow": [
        {
            "sip": "10.78.221.45",
            "dip": "10.78.221.46",
            "sport": "5002",
            "dport": "6000",
            "ip-protocol": "udp",
            "dscp": "20",
            "urgent-flag": "0",
            "topology-tag": {
                "level": "1",
                "svid": "0x00000064",
                "dvid": "0x000001f4"
            },
            "statistics": {
                "t": "1477538852",
                "packet-sent": "5",
                "packet-drops": "5"
            }
        }
    ]
}
*/
#define DropReportSignature    "HuaweiDC3ServerAntsDropNotice"

// 生成json格式的字符串, 用于向Analyzer上报丢包信息.
INT32 CreatDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData)
{
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        ptree ptDataRoot, ptDataFlowArray, ptDataFlowEntry;
        ptree ptDataFlowEntryTemp;
        
        stringstream ssJsonData;

        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", DropReportSignature);

         // 清空Flow Entry Array
        ptDataFlowArray.clear();

        // 生成一个Flow Entry的数据
        {
            ptDataFlowEntry.clear();
            
            ptDataFlowEntry.put("sip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiSrcIP));
            ptDataFlowEntry.put("dip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiDestIP));
            ptDataFlowEntry.put("sport", pstAgentFlowEntry->stFlowKey.uiSrcPort);
            ptDataFlowEntry.put("dport", pstAgentFlowEntry->stFlowKey.uiDestPort);
            switch (pstAgentFlowEntry->stFlowKey.eProtocol)
            {
                case AGENT_DETECT_PROTOCOL_UDP:
                    ptDataFlowEntry.put("ip-protocol","udp");
                    break;
                case AGENT_DETECT_PROTOCOL_TCP:
                    ptDataFlowEntry.put("ip-protocol","tcp");
                    break;
                case AGENT_DETECT_PROTOCOL_ICMP:
                    ptDataFlowEntry.put("ip-protocol","icmp");
                    break;
                default:
                    ptDataFlowEntry.put("ip-protocol","null");
                    break;
            }
            ptDataFlowEntry.put("dscp",pstAgentFlowEntry->stFlowKey.uiDscp);
            ptDataFlowEntry.put("urgent-flag",pstAgentFlowEntry->stFlowKey.uiUrgentFlow);

            // 处理topology-tag信息        
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("level", pstAgentFlowEntry->stFlowKey.stServerTopo.uiLevel);
#if 0
            ssJsonData.str("");
            ssJsonData <<"0x" << hex << setw(8) << setfill('0') << pstAgentFlowEntry->stFlowKey.stServerTopo.uiSvid;
            ptDataFlowEntryTemp.put("svid", ssJsonData.str().c_str());
            ssJsonData.str("");
            ssJsonData <<"0x" << hex << setw(8) << setfill('0') << pstAgentFlowEntry->stFlowKey.stServerTopo.uiDvid;
            ptDataFlowEntryTemp.put("dvid", ssJsonData.str().c_str());
#else
            ptDataFlowEntryTemp.put("sid", pstAgentFlowEntry->stFlowKey.stServerTopo.uiSvid);
            ptDataFlowEntryTemp.put("did", pstAgentFlowEntry->stFlowKey.stServerTopo.uiDvid);
#endif
            ptDataFlowEntry.put_child("topology-tag", ptDataFlowEntryTemp);

            // 处理statistics信息
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("t", pstAgentFlowEntry->stFlowDetectResult.lT5);
            ptDataFlowEntryTemp.put("packet-sent", pstAgentFlowEntry->stFlowDetectResult.lPktSentCounter);
            ptDataFlowEntryTemp.put("packet-drops", pstAgentFlowEntry->stFlowDetectResult.lPktDropCounter);       
            ptDataFlowEntry.put_child("statistics", ptDataFlowEntryTemp);
            
            // 加入json数组
            ptDataFlowArray.push_back(make_pair("", ptDataFlowEntry));
        }

        ptDataRoot.put_child("flow", ptDataFlowArray);
        //ptDataRoot.put_child("flow", ptDataFlowEntry);

        ssJsonData.clear();
        ssJsonData.str("");
        write_json(ssJsonData, ptDataRoot);    
        (*pssReportData) << ssJsonData.str();
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when CreatDropReportData.", e.what());
        return AGENT_E_ERROR;
    }

    return AGENT_OK;
}


/*
ServerAntServer 下发的紧急探测流格式
post 
key = ServerAntAgent
data = 
{
    "orgnizationSignature": "HuaweiDC3ServerAntsProbelistIssue",
    "serverIP": "10.1.1.1",
    "action": "post",
    "content": "probe-list",
    
    "flow": [
        {
        "urgent": "true:false",        
        "sip": "",
        "dip": "", 
        "ip-protocol": "icmp:tcp:udp",
        "sport-min": "",
        "sport-max": "",
        "sport-range": "1",
        "dscp": "",
        "topology-tag": {
            level": "1:2:3:4",
            svid": "",
            dvid": "",
        },

        {
        "urgent": "true:false",        
        "sip": "",
        "dip": "", 
        "ip-protocol": "icmp:tcp:udp",
        "sport-min": "",
        "sport-max": "",
        "sport-range": "1",
        "dscp": "",
        "topology-tag": {
            level": "1:2:3:4",
            svid": "",
            dvid": "",
        },
        ]
    },
}
理论上支持多个flow, 但是两个flow时data长度超过512byte, 会被http daemon截断,导致json parser失败.
*/


// 解析Server下发的json格式flow数据,转换成ServerFlowKey_S格式
INT32 GetFlowInfoFromJsonFlowEntry(ptree ptFlowEntry, ServerFlowKey_S * pstNewServerFlowKey)
{
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        string strTemp;
        ptree  ptFlowEntryTopo;
        UINT32 uiDataTemp = 0;

        // 初始化
        sal_memset(pstNewServerFlowKey, 0, sizeof(ServerFlowKey_S));
        
        // 解析Urgent
#if 0
        pstNewServerFlowKey->uiUrgentFlow   = ptFlowEntry.get<UINT32>("urgent-flag");
#else
        strTemp = ptFlowEntry.get<string>("urgent");
        if (0 == strTemp.compare("true"))
            pstNewServerFlowKey->uiUrgentFlow = 1;
        else
            pstNewServerFlowKey->uiUrgentFlow = 0;
#endif
        
        // 解析Protocol
        strTemp = ptFlowEntry.get<string>("ip-protocol");
        if (0 == strTemp.compare("udp"))
            pstNewServerFlowKey->eProtocol = AGENT_DETECT_PROTOCOL_UDP;                
        else if (0 == strTemp.compare("tcp"))
            pstNewServerFlowKey->eProtocol = AGENT_DETECT_PROTOCOL_TCP;
        else if (0 == strTemp.compare("icmp"))
            pstNewServerFlowKey->eProtocol = AGENT_DETECT_PROTOCOL_ICMP;
        else
            pstNewServerFlowKey->eProtocol = AGENT_DETECT_PROTOCOL_NULL;

        strTemp = ptFlowEntry.get<string>("sip");
        pstNewServerFlowKey->uiSrcIP = sal_inet_aton(strTemp.c_str());
        
        strTemp = ptFlowEntry.get<string>("dip");
        pstNewServerFlowKey->uiDestIP = sal_inet_aton(strTemp.c_str());

        pstNewServerFlowKey->uiDscp          = ptFlowEntry.get<UINT32>("dscp");
        pstNewServerFlowKey->uiSrcPortMin    = ptFlowEntry.get<UINT32>("sport-min");
        pstNewServerFlowKey->uiSrcPortMax    = ptFlowEntry.get<UINT32>("sport-max");
        pstNewServerFlowKey->uiSrcPortRange  = ptFlowEntry.get<UINT32>("sport-range");

        ptFlowEntryTopo = ptFlowEntry.get_child("topology-tag");
        #if 0
            //pstNewServerFlowKey->stServerTopo.uiSvid   = ptFlowEntryTopo.get<UINT32>("svid");
            uiDataTemp = 0;
            strTemp = ptFlowEntryTopo.get<string>("svid");
            sscanf(strTemp.c_str(), "0x%x", &uiDataTemp); 
            pstNewServerFlowKey->stServerTopo.uiSvid   = uiDataTemp;
            //pstNewServerFlowKey->stServerTopo.uiDvid   = ptFlowEntryTopo.get<UINT32>("dvid");
            uiDataTemp = 0;
            strTemp = ptFlowEntryTopo.get<string>("dvid");
            sscanf(strTemp.c_str(), "0x%x", &uiDataTemp); 
            pstNewServerFlowKey->stServerTopo.uiDvid   = uiDataTemp;
        #else
            pstNewServerFlowKey->stServerTopo.uiSvid   = ptFlowEntryTopo.get<UINT32>("src-id");
            pstNewServerFlowKey->stServerTopo.uiDvid   = ptFlowEntryTopo.get<UINT32>("dst-id");
        #endif
        
        pstNewServerFlowKey->stServerTopo.uiLevel  = ptFlowEntryTopo.get<UINT32>("level");
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when GetFlowInfoFromJsonFlowEntry.", e.what());
        return AGENT_E_ERROR;
    }

    JSON_PARSER_INFO("Get Flow From Server: sip[%s], sport[%u]-[%u], range[%u], dscp[%d], Urgent[%d], Protocol[%d]",
            sal_inet_ntoa(pstNewServerFlowKey->uiSrcIP), pstNewServerFlowKey->uiSrcPortMin, pstNewServerFlowKey->uiSrcPortMax, pstNewServerFlowKey->uiSrcPortRange,
            pstNewServerFlowKey->uiDscp, pstNewServerFlowKey->uiUrgentFlow,  pstNewServerFlowKey->eProtocol);

    JSON_PARSER_INFO("                      dip[%s], topy: Level[%u], Source id[%8u], Dest id[%8u]",
            sal_inet_ntoa(pstNewServerFlowKey->uiDestIP), pstNewServerFlowKey->stServerTopo.uiLevel,
            pstNewServerFlowKey->stServerTopo.uiSvid, pstNewServerFlowKey->stServerTopo.uiDvid);

    return AGENT_OK;
}

// 解析json格式的flow array, 并下发到FlowManager,
INT32 IssueFlowFromJsonFlowArray(ptree ptFlowArray, FlowManager_C* pcFlowManager, UINT32 uiIsUrgentFlow)
{
    INT32 iRet = AGENT_OK;
    
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        ServerFlowKey_S stNewServerFlowKey;
        ptree ptFlowEntry;

        // 遍历flow flow array, 完成解析和下发
        for (ptree::iterator itFlow = ptFlowArray.begin(); itFlow != ptFlowArray.end(); itFlow++)
        {
            ptFlowEntry = itFlow->second; // first为空, boost格式

            sal_memset(&stNewServerFlowKey, 0, sizeof(stNewServerFlowKey));

            iRet = GetFlowInfoFromJsonFlowEntry(ptFlowEntry, &stNewServerFlowKey);
            if (iRet)
            {
                JSON_PARSER_ERROR("Get Flow Info From Json failed [%d]", iRet);
                return iRet;
            }

            // 普通流程添加到配置表, 待配置倒换后生效.
	   iRet = pcFlowManager->ServerWorkingFlowTableAdd(stNewServerFlowKey);
            if (iRet)
            {
                JSON_PARSER_ERROR("Add New ServerWorkingFlowTable failed [%d]", iRet);
                return iRet;
            }
        }
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when IssueFlowFromJsonFlowArray.", e.what());
        return AGENT_E_ERROR;
    }
    return iRet;
}


#define UrgentFlowIssueSignature    "HuaweiDC3ServerAntsProbelistIssue"
#define UrgentFlowIssueAction       "post"

// 解析json格式的字符串, 并下发到FlowManager, 负责处理Server主导下发的Urgent探测流.
INT32 ProcessUrgentFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        // pcData字符串转存stringstream格式, 方便后续boost::property_tree处理.
        stringstream ssStringData(pcJsonData);

        // boost::property_tree对象, 用于存储json格式数据.
        ptree ptDataRoot, ptFlowArray;
        read_json(ssStringData, ptDataRoot);

        // 检查pt中的解析结果
#if 0
        // 检查Signature
        string strSignature =   ptDataRoot.get<string>("MessageSignature");
        
        // 校验签名
        if (0 == strSignature.compare(UrgentFlowIssueSignature))
        {
            string strAction    =   ptDataRoot.get<string>("Action");
            
            // 校验action和content
            if( (0 != strAction.compare(UrgentFlowIssueAction)) )
            {
                JSON_PARSER_WARNING("Unsupported Action[%s].", strAction.c_str());
                // 校验失败
                return AGENT_E_ERROR;
            }
            
            // 从data中解析数据,填充stServerFlowKey, 然后调用FlowManager接口添加探测流.
            ptFlowArray.clear();
            ptFlowArray = ptDataRoot.get_child("flows");
            iRet = IssueFlowFromJsonFlowArray(ptFlowArray, pcFlowManager, AGENT_TRUE);
            if (iRet)
            {
                JSON_PARSER_ERROR("Issue Flow From Json Flow Array failed [%d]", iRet);
                return iRet;
            }
                    
            return iRet;
        }
        else
        {
            JSON_PARSER_WARNING("Unsupported Signature:[%s]", strSignature.c_str());        
            return AGENT_E_PARA;
        }
#else
        ptFlowArray.clear();
        ptFlowArray = ptDataRoot.get_child("flows");
        iRet = IssueFlowFromJsonFlowArray(ptFlowArray, pcFlowManager, AGENT_TRUE);
        if (iRet)
        {
            JSON_PARSER_ERROR("Issue Flow From Json Flow Array failed [%d], Flow info[%s]", iRet, pcJsonData);
            return iRet;
        }
#endif
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when ProcessUrgentFlowFromServer. Flow info[%s]", e.what(), pcJsonData);
        return AGENT_E_ERROR;
    }
    
    return iRet;
}


/*
ServerAntServer 回复的普通探测流格式
回复数据, 无需key.

{
    "orgnizationSignature": "HuaweiDC3ServerAntsProbelistReply",
    "serverIP": "10.1.1.1",
    "action": "reply",
    "content": "probe-list",
    
    "flow": [
        {
        "urgent": "true:false",        
        "sip": "",
        "dip": "", 
        "ip-protocol": "icmp:tcp:udp",
        "sport-min": "",
        "sport-max": "",
        "sport-range": "1",
        "dscp": "",
        "topology-tag": {
            level": "1:2:3:4",
            svid": "",
            dvid": "",
        },

        {
        "urgent": "true:false",        
        "sip": "",
        "dip": "", 
        "ip-protocol": "icmp:tcp:udp",
        "sport-min": "",
        "sport-max": "",
        "sport-range": "1",
        "dscp": "",
        "topology-tag": {
            level": "1:2:3:4",
            svid": "",
            dvid": "",
        },
        ]
    },
}
支持多个flow以array形式下发.
*/

#define NormalFlowReplaySignature    "HuaweiDC3ServerAntsProbelistReply"
#define NormalFlowReplayAction       "reply"

// 解析json格式的字符串, 并下发到FlowManager, 负责处理向Server请求时Server回复的普通探测流.
INT32 ProcessNormalFlowFromServer(char * pcJsonData, FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
    
        // pcData字符串转存stringstream格式, 方便后续boost::property_tree处理.
        stringstream ssStringData(pcJsonData);

        // boost::property_tree对象, 用于存储json格式数据.
        ptree ptDataRoot, ptFlowArray;
        read_json(ssStringData, ptDataRoot);

        // 检查pt中的解析结果
#if 0
        // 检查Signature
        string strSignature =   ptDataRoot.get<string>("MessageSignature");
        
        // 校验签名
        if (0 == strSignature.compare(NormalFlowReplaySignature))
        {
            string strAction    =   ptDataRoot.get<string>("Action");
            
            // 校验action和content
            if( (0 != strAction.compare(NormalFlowReplayAction)) )
            {
                JSON_PARSER_WARNING("Unsupported Action[%s].", strAction.c_str());
                // 校验失败
                return AGENT_E_ERROR;
            }
            
            // 从data中解析数据,填充stServerFlowKey, 然后调用FlowManager接口添加探测流.
            ptFlowArray.clear();
            ptFlowArray = ptDataRoot.get_child("flow");
            iRet = IssueFlowFromJsonFlowArray(ptFlowArray, pcFlowManager, AGENT_FALSE);
            if (iRet)
            {
                JSON_PARSER_ERROR("Issue Flow From Json Flow Array failed [%d]", iRet);
                return iRet;
            }
                    
            return iRet;
        }
        else
        {
            JSON_PARSER_WARNING("Unsupported Signature:[%s]", strSignature.c_str());        
            return AGENT_E_PARA;
        }
#else
        // 从data中解析数据,填充stServerFlowKey, 然后调用FlowManager接口添加探测流.
        ptFlowArray.clear();
        ptFlowArray = ptDataRoot.get_child("flow");
        iRet = IssueFlowFromJsonFlowArray(ptFlowArray, pcFlowManager, AGENT_FALSE);
        if (iRet)
        {
            JSON_PARSER_ERROR("Issue Flow From Json Flow Array failed [%d]. Flow info[%s]", iRet, pcJsonData);
            return iRet;
        }
#endif  
    }

    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when ProcessNormalFlowFromServer. Flow info[%s]", e.what(), pcJsonData);
        return AGENT_E_ERROR;
    }
    return iRet;

}

/*
	接收从Server端下发的轮询周期
	为0的话，停止探测
	格式为
	{
		"probe_interval":"0"
	}
*/
INT32 ProcessActionFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager)
{
	
	// pcData字符串转存stringstream格式, 方便后续boost::property_tree处理.
    stringstream ssStringData(pcJsonData);

    // boost::property_tree对象, 用于存储json格式数据.
    ptree ptDataRoot;
	UINT32 interval;
    try 
	{
        // 防止Json消息体不规范
        read_json(ssStringData, ptDataRoot);
        // 防止没有设值，传入空值
        interval = ptDataRoot.get<UINT32>("probe_interval");
    }
	catch (exception const & e)
	{
        JSON_PARSER_ERROR("Parse Json message[%s] error [%s].", pcJsonData, e.what());
        return AGENT_E_ERROR;
	}
	INT32 iRet = pcFlowManager -> FlowManagerAction(interval);
	return iRet;
}

