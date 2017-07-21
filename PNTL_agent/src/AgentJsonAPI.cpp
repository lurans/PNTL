#include <boost/property_tree/json_parser.hpp>

using namespace std;
// 使用boost的property_tree扩展库处理json格式数据.
using namespace boost::property_tree;

#include "Log.h"
#include "AgentCommon.h"
#include "AgentJsonAPI.h"

/*
{
"LogCfg" :
    {
                "LOG_DIR"       : "/opt/huawei/logs/ServerAntAgent"
    },
"ServerAntServer" :
    {^M
        "IP"    : "8.15.4.11", 
        "Port"  : 8888
    },
"ServerAntAgent" :
    {
        "AgentIP"    : "0.0.0.0",
        "MgntIP"     : "0.0.0.0",
        "Hostname"      :       "SZV1000278559",
        "Port"  : 31001,
        
        "PollingTimerPeriod"    : 100000,
        "ReportPeriod"          : 300,
        "QueryPeriod"           : 120,
        "DetectPeriod"          : 60,
        "DetectTimeoutPeriod"   : 1,
        "DetectDropThresh"      : 2,       
        
        "ProtocolUDP" :^M
            {
                "DestPort"  : 6000,
                "SrcPortMin": 32769,
                "SrcPortMax": 32868
            }
    }
}

*/
// 解析Agent本地配置文件, 完成初始化配置.
INT32 ParserLocalCfg(const char * pcJsonData, ServerAntAgentCfg_C * pcCfg)
{
    INT32 iRet = AGENT_OK;
    UINT32 uiIp, uiPort, uiData;
    string strTemp;
	// boost::property_tree对象, 用于存储json格式数据.
    ptree ptDataRoot, ptDataTmp;
	
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        // pcData字符串转存stringstream格式, 方便后续boost::property_tree处理.
        stringstream ssStringData(pcJsonData);
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

        // 解析ServerAntServer数据.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntServer");
        strTemp = ptDataTmp.get<string>("IP");
        uiIp = sal_inet_aton(strTemp.c_str());
        uiPort = ptDataTmp.get<UINT32>("Port");
        iRet = pcCfg->SetServerAddress(uiIp, uiPort);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetServerAddress and port failed[%d]", iRet);
            return iRet;
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

        strTemp = ptDataTmp.get<string>("Hostname");
        iRet = pcCfg->SetHostname(strTemp);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetHostname failed[%d]", iRet);
            return iRet;
        }

        strTemp = ptDataTmp.get<string>("AgentIP");
        uiIp = sal_inet_aton(strTemp.c_str());
        uiPort = ptDataTmp.get<UINT32>("Port");
        iRet = pcCfg->SetAgentAddress(uiIp, uiPort);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetAgentAddress and port failed[%d]", iRet);
            return iRet;
        }

        uiData = ptDataTmp.get<UINT32>("ReportPeriod");
        iRet = pcCfg->SetReportPeriod(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetReportPeriod[%u] failed[%d], range should be in [%u, %u]", uiData, iRet, MIN_REPORT_PERIOD, MAX_REPORT_PERIOD);
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
            JSON_PARSER_ERROR("set detect period[%u] faild[%d], range should be in [%u, %u]", uiData, iRet, MIN_PROBE_PERIOD, MAX_PROBE_PERIOD);
            return iRet;
        }
		
        uiData = ptDataTmp.get<UINT32>("DetectTimeoutPeriod");
        iRet = pcCfg->SetDetectTimeout(uiData);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDetectTimeout[%u] failed[%d], range should be in [%u, %u]", uiData, iRet, MIN_LOSS_TIMEOUT, MAX_LOSS_TIMEOUT);
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



#define NormalFlowRequestSignature    "HuaweiDCAnts"
#define NormalFlowRequestAction       "RequestServerProbeList"

// 生成json格式的字符串, 用于发起向Server请求Probe-list时提交的post data.
INT32 CreateProbeListRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData)
{
    INT32 iRet = AGENT_OK;
    stringstream ssJsonData;
    ptree ptDataRoot, ptDataTemp;
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
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
        JSON_PARSER_ERROR("Caught exception [%s] when CreateProbeListRequestPostData.", e.what());
        return AGENT_E_ERROR;
    }
    return AGENT_OK;
}

INT32 CreatAgentIPRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData)
{
    INT32 iRet = AGENT_OK;

    stringstream ssJsonData;
    ptree ptDataRoot;
    UINT32 uiIp, uiMgntIp;
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        iRet = pcCfg->GetAgentAddress(&uiIp, NULL);
        if (iRet)
        {
            JSON_PARSER_ERROR("GetAgentAddress failed[%d]", iRet);
            return iRet;
        }

        iRet = pcCfg->GetMgntIP(&uiMgntIp);
        ptDataRoot.put("vbond_ip", sal_inet_ntoa(uiIp));    // 数据面IP
        ptDataRoot.put("agent_ip", sal_inet_ntoa(uiMgntIp));
		
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
    "flow":
    {

        "sip": "",
        "dip": "",
        "sport": "",
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
INT32 CreateLatencyReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData, UINT32 maxDelay)
{
    INT64 max = pstAgentFlowEntry->stFlowDetectResult.lLatencyMax;
    if (0 != maxDelay && maxDelay > max)
    {
        JSON_PARSER_INFO("Max delay is [%d], less than threshold[%d], does not report.", max, maxDelay);
        return AGENT_FILTER_DELAY;
    }

    ptree ptDataRoot, ptDataFlowArray, ptDataFlowEntry;
    ptree ptDataFlowEntryTemp;
    char            acCurTime[32]   = {0};                      // 缓存时间戳

    stringstream ssJsonData;
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    try
    {
        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", LatencyReportSignature);

        // 清空Flow Entry Array
        ptDataFlowArray.clear();

        // 生成一个Flow Entry的数据
        {
            GetPrintTime(acCurTime);
            ptDataFlowEntry.clear();
            ptDataFlowEntry.put("sip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiSrcIP));
            ptDataFlowEntry.put("dip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiDestIP));
            ptDataFlowEntry.put("sport", pstAgentFlowEntry->stFlowKey.uiSrcPort);
            ptDataFlowEntry.put("time", acCurTime);

            // 处理time信息
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("t1", pstAgentFlowEntry->stFlowDetectResult.lT1);
            ptDataFlowEntryTemp.put("t2", pstAgentFlowEntry->stFlowDetectResult.lT2);
            ptDataFlowEntryTemp.put("t3", pstAgentFlowEntry->stFlowDetectResult.lT3);
            ptDataFlowEntryTemp.put("t4", pstAgentFlowEntry->stFlowDetectResult.lT4);
            ptDataFlowEntry.put_child("times", ptDataFlowEntryTemp);

            // 处理statistics信息
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("packet-sent", pstAgentFlowEntry->stFlowDetectResult.lPktSentCounter);
            ptDataFlowEntryTemp.put("packet-drops", pstAgentFlowEntry->stFlowDetectResult.lPktDropCounter);
            ptDataFlowEntryTemp.put("50percentile", pstAgentFlowEntry->stFlowDetectResult.lLatency50Percentile);
            ptDataFlowEntryTemp.put("99percentile", pstAgentFlowEntry->stFlowDetectResult.lLatency99Percentile);
            ptDataFlowEntryTemp.put("standard-deviation", pstAgentFlowEntry->stFlowDetectResult.lLatencyStandardDeviation);
            ptDataFlowEntryTemp.put("min", pstAgentFlowEntry->stFlowDetectResult.lLatencyMin);
            ptDataFlowEntryTemp.put("max", max);
            ptDataFlowEntryTemp.put("drop-notices", pstAgentFlowEntry->stFlowDetectResult.lDropNotesCounter);
            ptDataFlowEntry.put_child("statistics", ptDataFlowEntryTemp);
            if (pstAgentFlowEntry->stFlowKey.uiIsBigPkg)
            {
                ptDataFlowEntry.put("package-size", BIG_PACKAGE_SIZE);
            }
            else
            {
                ptDataFlowEntry.put("package-size", NORMAL_PACKAGE_SIZE);
            }

            // 加入json数组, 暂不使用数组, Collector不支持.
            ptDataFlowArray.push_back(make_pair("", ptDataFlowEntry));
        }

        ptDataRoot.put_child("flow", ptDataFlowArray);

        ssJsonData.clear();
        ssJsonData.str("");
        write_json(ssJsonData, ptDataRoot);
        (*pssReportData) << ssJsonData.str();
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when CreateLatencyReportData.", e.what());
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

{
    "flow": [
        {
            "sip": "10.78.221.45",
            "dip": "10.78.221.46",
            "sport": "5002",
            "packet-sent": "5",
            "packet-drops": "5"
        }
    ]
}


*/
#define DropReportSignature    "HuaweiDC3ServerAntsDropNotice"

// 生成json格式的字符串, 用于向Analyzer上报丢包信息.
INT32 CreateDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData)
{
    // boost库中出现错误会抛出异常, 未被catch的异常会逐级上报, 最终导致进程abort退出.
    char            acCurTime[32]   = {0};                      // 缓存时间戳
    ptree ptDataRoot, ptDataFlowArray, ptDataFlowEntry;
    stringstream ssJsonData;
    try
    {
        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", DropReportSignature);

        // 清空Flow Entry Array
        ptDataFlowArray.clear();

        // 生成一个Flow Entry的数据
        {
            ptDataFlowEntry.clear();
            GetPrintTime(acCurTime);
            ptDataFlowEntry.put("sip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiSrcIP));
            ptDataFlowEntry.put("dip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiDestIP));
            ptDataFlowEntry.put("sport", pstAgentFlowEntry->stFlowKey.uiSrcPort);
          
            ptDataFlowEntry.put("time", acCurTime);
            ptDataFlowEntry.put("packet-sent", pstAgentFlowEntry->stFlowDetectResult.lPktSentCounter);
            ptDataFlowEntry.put("packet-drops", pstAgentFlowEntry->stFlowDetectResult.lPktDropCounter);

            if (pstAgentFlowEntry->stFlowKey.uiIsBigPkg)
            {
                ptDataFlowEntry.put("package-size", BIG_PACKAGE_SIZE);
            }
            else
            {
                ptDataFlowEntry.put("package-size", NORMAL_PACKAGE_SIZE);
            }
            

            // 加入json数组
            ptDataFlowArray.push_back(make_pair("", ptDataFlowEntry));
        }

        ptDataRoot.put_child("flow", ptDataFlowArray);

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

        ptFlowArray.clear();
        ptFlowArray = ptDataRoot.get_child("flows");
        iRet = IssueFlowFromJsonFlowArray(ptFlowArray, pcFlowManager, AGENT_TRUE);
        if (iRet)
        {
            JSON_PARSER_ERROR("Issue Flow From Json Flow Array failed [%d], Flow info[%s]", iRet, pcJsonData);
            return iRet;
        }
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
	INT32 iRet = pcFlowManager -> FlowManagerAction((INT32)interval);
	return iRet;
}

/*
	接收从Server端下发的配置参数
	格式为
	{
		"probe_period":"0",
		"port_count" : "5",
		"report_period" : "",
		"delay_threshold":"",
		"dscp":"",
		"lossPkg_timeout":"",
		"package_rate":""
	}
*/
INT32 ProcessServerConfigFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager)
{
    // pcData字符串转存stringstream格式, 方便后续boost::property_tree处理.
    stringstream ssStringData(pcJsonData);

    // boost::property_tree对象, 用于存储json格式数据.
    ptree ptDataRoot;
	UINT32 interval;
	INT32 iRet = AGENT_OK;
    try
	{
        // 防止Json消息体不规范
        read_json(ssStringData, ptDataRoot);

		interval = ptDataRoot.get<UINT32>("pingListFlag");
		SHOULD_PROBE = interval;
		JSON_PARSER_INFO("SHOULD_PROBE %d", SHOULD_PROBE);
        if (SHOULD_PROBE)
        {
            JSON_PARSER_INFO("will soon begin to get pingList");
        }
        interval = ptDataRoot.get<UINT32>("probe_period");
		iRet = pcFlowManager->pcAgentCfg->SetDetectPeriod(interval);
		if (iRet)
		{
		    JSON_PARSER_ERROR("SetDectPeriod[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_PROBE_PERIOD, MAX_PROBE_PERIOD);
		    return AGENT_E_PARA;
		}
		JSON_PARSER_INFO("Current probe period is %u", pcFlowManager->pcAgentCfg->GetDetectPeriod());
		iRet = pcFlowManager->FlowManagerAction((INT32)interval);
		
        interval = ptDataRoot.get<UINT32>("port_count");
        iRet = pcFlowManager->pcAgentCfg->SetPortCount(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetPortCount[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_PORT_COUNT, MAX_PORT_COUNT);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current port count is %u", pcFlowManager->pcAgentCfg->GetPortCount());

        interval = ptDataRoot.get<UINT32>("report_period");
        iRet = pcFlowManager->pcAgentCfg->SetReportPeriod(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetReportPeriod[%u] failed[%d], range should be in [%u, %u] and >= than detectPeriod[%u]", interval, iRet, MIN_REPORT_PERIOD, MAX_REPORT_PERIOD, pcFlowManager->pcAgentCfg->GetDetectPeriod());
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current report period is %u", pcFlowManager->pcAgentCfg->GetReportPeriod());

        interval = ptDataRoot.get<UINT32>("delay_threshold");
        iRet = pcFlowManager->pcAgentCfg->SetMaxDelay(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetMaxDelay[%u] failed[%d]");
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current delay threshold is %u", pcFlowManager->pcAgentCfg->GetMaxDelay());

        interval = ptDataRoot.get<UINT32>("dscp");
        iRet = pcFlowManager->pcAgentCfg->SetDscp(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDscp[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_DSCP, MAX_DSCP);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current dscp is %u", pcFlowManager->pcAgentCfg->getDscp());

        interval = ptDataRoot.get<UINT32>("lossPkg_timeout");
        iRet = pcFlowManager->pcAgentCfg->SetDetectTimeout(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDetectTimeout[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_LOSS_TIMEOUT, MAX_LOSS_TIMEOUT);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current lossPkg timeout is %u", pcFlowManager->pcAgentCfg->GetDetectTimeout());

        interval = ptDataRoot.get<UINT32>("pkg_count");
		//interval = ptDataRoot.get<UINT32>("bigPkg_rate");
        iRet = pcFlowManager->pcAgentCfg->SetBigPkgRate(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetBigPkgRate[%u] failed[%d], value should be %u or %u.", interval, iRet, MIN_BIG_PACKAGE_RATE, MAX_BIG_PACKAGE_RATE);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current package rate is %u", pcFlowManager->pcAgentCfg->GetBigPkgRate());
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Parse Json message[%s] error [%s].", pcJsonData, e.what());
        return AGENT_E_ERROR;
    }
    return AGENT_OK;
}

INT32 ProcessConfigFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager)
{
    // pcData字符串转存stringstream格式, 方便后续boost::property_tree处理.
    stringstream ssStringData(pcJsonData);

    // boost::property_tree对象, 用于存储json格式数据.
    ptree ptDataRoot;
	UINT32 interval;
	INT32 iRet = AGENT_OK;
    try
	{
        // 防止Json消息体不规范
        read_json(ssStringData, ptDataRoot);
        interval = ptDataRoot.get<UINT32>("probe_period");
		iRet = pcFlowManager->pcAgentCfg->SetDetectPeriod(interval);
		if (iRet)
		{
		    JSON_PARSER_ERROR("SetDectPeriod[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_PROBE_PERIOD, MAX_PROBE_PERIOD);
		    return AGENT_E_PARA;
		}
		JSON_PARSER_INFO("Current probe period is %u", pcFlowManager->pcAgentCfg->GetDetectPeriod());

        interval = ptDataRoot.get<UINT32>("port_count");
        iRet = pcFlowManager->pcAgentCfg->SetPortCount(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetPortCount[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_PORT_COUNT, MAX_PORT_COUNT);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current port count is %u", pcFlowManager->pcAgentCfg->GetPortCount());

        interval = ptDataRoot.get<UINT32>("report_period");
        iRet = pcFlowManager->pcAgentCfg->SetReportPeriod(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetReportPeriod[%u] failed[%d], range should be in [%u, %u] and >= than detectPeriod[%u]", interval, iRet, MIN_REPORT_PERIOD, MAX_REPORT_PERIOD, pcFlowManager->pcAgentCfg->GetDetectPeriod());
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current report period is %u", pcFlowManager->pcAgentCfg->GetReportPeriod());

        interval = ptDataRoot.get<UINT32>("delay_threshold");
        iRet = pcFlowManager->pcAgentCfg->SetMaxDelay(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetMaxDelay[%u] failed[%d]");
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current delay threshold is %u", pcFlowManager->pcAgentCfg->GetMaxDelay());

        interval = ptDataRoot.get<UINT32>("dscp");
        iRet = pcFlowManager->pcAgentCfg->SetDscp(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDscp[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_DSCP, MAX_DSCP);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current dscp is %u", pcFlowManager->pcAgentCfg->getDscp());

        interval = ptDataRoot.get<UINT32>("lossPkg_timeout");
        iRet = pcFlowManager->pcAgentCfg->SetDetectTimeout(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetDetectTimeout[%u] failed[%d], range should be in [%u, %u]", interval, iRet, MIN_LOSS_TIMEOUT, MAX_LOSS_TIMEOUT);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current lossPkg timeout is %u", pcFlowManager->pcAgentCfg->GetDetectTimeout());

        interval = ptDataRoot.get<UINT32>("package_rate");
        iRet = pcFlowManager->pcAgentCfg->SetBigPkgRate(interval);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetBigPkgRate[%u] failed[%d], range should be %u or %u.", interval, iRet, MIN_BIG_PACKAGE_RATE, MAX_BIG_PACKAGE_RATE);
            return AGENT_E_PARA;
        }
        JSON_PARSER_INFO("Current package rate is %u", pcFlowManager->pcAgentCfg->GetBigPkgRate());
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Parse Json message[%s] error [%s].", pcJsonData, e.what());
		
        return AGENT_E_ERROR;
    }
    return AGENT_OK;
}

