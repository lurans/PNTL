#include <boost/property_tree/json_parser.hpp>

using namespace std;
// ʹ��boost��property_tree��չ�⴦��json��ʽ����.
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
// ����Agent���������ļ�, ��ɳ�ʼ������.
INT32 ParserLocalCfg(const char * pcJsonData, ServerAntAgentCfg_C * pcCfg)
{
    INT32 iRet = AGENT_OK;
    UINT32 uiIp, uiPort, uiData;
    string strTemp;
	// boost::property_tree����, ���ڴ洢json��ʽ����.
    ptree ptDataRoot, ptDataTmp;
	
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
        stringstream ssStringData(pcJsonData);
        read_json(ssStringData, ptDataRoot);

        // ����LogCfg����.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("LogCfg");
        strTemp = ptDataTmp.get<string>("LOG_DIR");
        iRet = SetNewLogDir(strTemp);
        if (iRet)
        {
            JSON_PARSER_ERROR("SetNewLogDir failed[%d]", iRet);
            return iRet;
        }

        // ����ServerAntServer����.
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

        // ����ServerAntAgent����.
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

        // ����ServerAntAgent.ProtocolUDP����.
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

// ����json��ʽ���ַ���, ���ڷ�����Server����Probe-listʱ�ύ��post data.
INT32 CreateProbeListRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData)
{
    INT32 iRet = AGENT_OK;
    stringstream ssJsonData;
    ptree ptDataRoot, ptDataTemp;
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
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
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        iRet = pcCfg->GetAgentAddress(&uiIp, NULL);
        if (iRet)
        {
            JSON_PARSER_ERROR("GetAgentAddress failed[%d]", iRet);
            return iRet;
        }

        iRet = pcCfg->GetMgntIP(&uiMgntIp);
        ptDataRoot.put("vbond_ip", sal_inet_ntoa(uiIp));    // ������IP
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

// ����json��ʽ���ַ���, ������Analyzer�ϱ���ʱ��Ϣ.
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
    char            acCurTime[32]   = {0};                      // ����ʱ���

    stringstream ssJsonData;
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", LatencyReportSignature);

        // ���Flow Entry Array
        ptDataFlowArray.clear();

        // ����һ��Flow Entry������
        {
            GetPrintTime(acCurTime);
            ptDataFlowEntry.clear();
            ptDataFlowEntry.put("sip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiSrcIP));
            ptDataFlowEntry.put("dip", sal_inet_ntoa(pstAgentFlowEntry->stFlowKey.uiDestIP));
            ptDataFlowEntry.put("sport", pstAgentFlowEntry->stFlowKey.uiSrcPort);
            ptDataFlowEntry.put("time", acCurTime);

            // ����time��Ϣ
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("t1", pstAgentFlowEntry->stFlowDetectResult.lT1);
            ptDataFlowEntryTemp.put("t2", pstAgentFlowEntry->stFlowDetectResult.lT2);
            ptDataFlowEntryTemp.put("t3", pstAgentFlowEntry->stFlowDetectResult.lT3);
            ptDataFlowEntryTemp.put("t4", pstAgentFlowEntry->stFlowDetectResult.lT4);
            ptDataFlowEntry.put_child("times", ptDataFlowEntryTemp);

            // ����statistics��Ϣ
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

            // ����json����, �ݲ�ʹ������, Collector��֧��.
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

// ����json��ʽ���ַ���, ������Analyzer�ϱ�������Ϣ.
INT32 CreateDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData)
{
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    char            acCurTime[32]   = {0};                      // ����ʱ���
    ptree ptDataRoot, ptDataFlowArray, ptDataFlowEntry;
    stringstream ssJsonData;
    try
    {
        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", DropReportSignature);

        // ���Flow Entry Array
        ptDataFlowArray.clear();

        // ����һ��Flow Entry������
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
            

            // ����json����
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
ServerAntServer �·��Ľ���̽������ʽ
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
������֧�ֶ��flow, ��������flowʱdata���ȳ���512byte, �ᱻhttp daemon�ض�,����json parserʧ��.
*/


// ����Server�·���json��ʽflow����,ת����ServerFlowKey_S��ʽ
INT32 GetFlowInfoFromJsonFlowEntry(ptree ptFlowEntry, ServerFlowKey_S * pstNewServerFlowKey)
{
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        string strTemp;
        ptree  ptFlowEntryTopo;
        UINT32 uiDataTemp = 0;

        // ��ʼ��
        sal_memset(pstNewServerFlowKey, 0, sizeof(ServerFlowKey_S));

        // ����Urgent
#if 0
        pstNewServerFlowKey->uiUrgentFlow   = ptFlowEntry.get<UINT32>("urgent-flag");
#else
        strTemp = ptFlowEntry.get<string>("urgent");
        if (0 == strTemp.compare("true"))
            pstNewServerFlowKey->uiUrgentFlow = 1;
        else
            pstNewServerFlowKey->uiUrgentFlow = 0;
#endif

        // ����Protocol
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

// ����json��ʽ��flow array, ���·���FlowManager,
INT32 IssueFlowFromJsonFlowArray(ptree ptFlowArray, FlowManager_C* pcFlowManager, UINT32 uiIsUrgentFlow)
{
    INT32 iRet = AGENT_OK;

    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        ServerFlowKey_S stNewServerFlowKey;
        ptree ptFlowEntry;

        // ����flow flow array, ��ɽ������·�
        for (ptree::iterator itFlow = ptFlowArray.begin(); itFlow != ptFlowArray.end(); itFlow++)
        {
            ptFlowEntry = itFlow->second; // firstΪ��, boost��ʽ

            sal_memset(&stNewServerFlowKey, 0, sizeof(stNewServerFlowKey));

            iRet = GetFlowInfoFromJsonFlowEntry(ptFlowEntry, &stNewServerFlowKey);
            if (iRet)
            {
                JSON_PARSER_ERROR("Get Flow Info From Json failed [%d]", iRet);
                return iRet;
            }

            // ��ͨ������ӵ����ñ�, �����õ�������Ч.
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

// ����json��ʽ���ַ���, ���·���FlowManager, ������Server�����·���Urgent̽����.
INT32 ProcessUrgentFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
        stringstream ssStringData(pcJsonData);

        // boost::property_tree����, ���ڴ洢json��ʽ����.
        ptree ptDataRoot, ptFlowArray;
        read_json(ssStringData, ptDataRoot);

        // ���pt�еĽ������

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
ServerAntServer �ظ�����ͨ̽������ʽ
�ظ�����, ����key.

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
֧�ֶ��flow��array��ʽ�·�.
*/

#define NormalFlowReplaySignature    "HuaweiDC3ServerAntsProbelistReply"
#define NormalFlowReplayAction       "reply"

// ����json��ʽ���ַ���, ���·���FlowManager, ��������Server����ʱServer�ظ�����ͨ̽����.
INT32 ProcessNormalFlowFromServer(char * pcJsonData, FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {

        // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
        stringstream ssStringData(pcJsonData);

        // boost::property_tree����, ���ڴ洢json��ʽ����.
        ptree ptDataRoot, ptFlowArray;
        read_json(ssStringData, ptDataRoot);

        // ���pt�еĽ������
#if 0
        // ���Signature
        string strSignature =   ptDataRoot.get<string>("MessageSignature");

        // У��ǩ��
        if (0 == strSignature.compare(NormalFlowReplaySignature))
        {
            string strAction    =   ptDataRoot.get<string>("Action");

            // У��action��content
            if( (0 != strAction.compare(NormalFlowReplayAction)) )
            {
                JSON_PARSER_WARNING("Unsupported Action[%s].", strAction.c_str());
                // У��ʧ��
                return AGENT_E_ERROR;
            }

            // ��data�н�������,���stServerFlowKey, Ȼ�����FlowManager�ӿ����̽����.
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
        // ��data�н�������,���stServerFlowKey, Ȼ�����FlowManager�ӿ����̽����.
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
	���մ�Server���·�����ѯ����
	Ϊ0�Ļ���ֹͣ̽��
	��ʽΪ
	{
		"probe_interval":"0"
	}
*/
INT32 ProcessActionFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager)
{
    // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
    stringstream ssStringData(pcJsonData);

    // boost::property_tree����, ���ڴ洢json��ʽ����.
    ptree ptDataRoot;
	UINT32 interval;
    try
	{
        // ��ֹJson��Ϣ�岻�淶
        read_json(ssStringData, ptDataRoot);
        // ��ֹû����ֵ�������ֵ
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
	���մ�Server���·������ò���
	��ʽΪ
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
    // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
    stringstream ssStringData(pcJsonData);

    // boost::property_tree����, ���ڴ洢json��ʽ����.
    ptree ptDataRoot;
	UINT32 interval;
	INT32 iRet = AGENT_OK;
    try
	{
        // ��ֹJson��Ϣ�岻�淶
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
    // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
    stringstream ssStringData(pcJsonData);

    // boost::property_tree����, ���ڴ洢json��ʽ����.
    ptree ptDataRoot;
	UINT32 interval;
	INT32 iRet = AGENT_OK;
    try
	{
        // ��ֹJson��Ϣ�岻�淶
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

