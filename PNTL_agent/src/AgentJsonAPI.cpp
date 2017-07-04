
//#include <boost/exception/all.hpp>
#include <boost/property_tree/json_parser.hpp>

using namespace std;
// ʹ��boost��property_tree��չ�⴦��json��ʽ����.
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
// ����Agent���������ļ�, ��ɳ�ʼ������.
INT32 ParserLocalCfg(const char * pcJsonData, ServerAntAgentCfg_C * pcCfg)
{
    INT32 iRet = AGENT_OK;
    
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        UINT32 uiIp, uiPort, uiData;
        string strTemp;
        
        // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
        stringstream ssStringData(pcJsonData);

        // boost::property_tree����, ���ڴ洢json��ʽ����.
        ptree ptDataRoot, ptDataTmp;
        
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
        
        // ˢ����־Ŀ¼��, �ٴ�ӡ��ǰ����
        JSON_PARSER_INFO("JsonData[%s]", pcJsonData);

        // ����ServerAntServer����.
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

        // ����ServerAntCollector����.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntCollector");
        
        strTemp = ptDataTmp.get<string>("Protocol");
        if ("Kafka" == strTemp)
        {
            ptree ptEntry;
            ptree ptArray;
            KafkaConnectInfo_S stKafkaConnectInfo;

            // ��ȡ topic
            ptDataTmp.clear();
            ptDataTmp = ptDataRoot.get_child("ServerAntCollector.KafkaInfo");
            stKafkaConnectInfo.strTopic = ptDataTmp.get<string>("Topic");

            // ��ȡ BrokerList
            ptArray.clear();
            ptArray = ptDataRoot.get_child("ServerAntCollector.KafkaInfo.BrokerList");
            // ����ptArray, ������Broker
            for (ptree::iterator itFlow = ptArray.begin(); itFlow != ptArray.end(); itFlow++)
            {
                ptEntry = itFlow->second; // firstΪ��, boost��ʽ
                strTemp = ptEntry.data();

                // ����Ƿ����ظ���Broker��������Ϣ
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

            // ����kafka������Ϣ
            iRet = pcCfg->SetCollectorKafkaInfo(&stKafkaConnectInfo);
            if (iRet)
            {
                    JSON_PARSER_ERROR("SetCollectorKafkaInfo failed[%d]", iRet);
                    return iRet;
            }

            // ˢ�� Collector ����
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

// ����json��ʽ���ַ���, ���ڷ�����Server����Probe-listʱ�ύ��post data.
INT32 CreatProbeListRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData)
{
    INT32 iRet = AGENT_OK;

    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
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

    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
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
		
        ptDataRoot.put("vbond-ip", sal_inet_ntoa(uiIp));    // ������IP 
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

// ����json��ʽ���ַ���, ������Analyzer�ϱ���ʱ��Ϣ.
INT32 CreatLatencyReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData)
{

    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        ptree ptDataRoot, ptDataFlowArray, ptDataFlowEntry;
        ptree ptDataFlowEntryTemp;
        
        stringstream ssJsonData;

        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", LatencyReportSignature);
        
        // ���Flow Entry Array
        ptDataFlowArray.clear();
        
        // ����һ��Flow Entry������
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

            // ����topology-tag��Ϣ
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
            
            // ����time��Ϣ
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("t1", pstAgentFlowEntry->stFlowDetectResult.lT1);
            ptDataFlowEntryTemp.put("t2", pstAgentFlowEntry->stFlowDetectResult.lT2);
            ptDataFlowEntryTemp.put("t3", pstAgentFlowEntry->stFlowDetectResult.lT3);
            ptDataFlowEntryTemp.put("t4", pstAgentFlowEntry->stFlowDetectResult.lT4);
            ptDataFlowEntry.put_child("time", ptDataFlowEntryTemp);

            // ����statistics��Ϣ
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

            // ����json����, �ݲ�ʹ������, Collector��֧��.
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

// ����json��ʽ���ַ���, ������Analyzer�ϱ�������Ϣ.
INT32 CreatDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData)
{
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        ptree ptDataRoot, ptDataFlowArray, ptDataFlowEntry;
        ptree ptDataFlowEntryTemp;
        
        stringstream ssJsonData;

        ptDataRoot.clear();
        ptDataRoot.put("orgnizationSignature", DropReportSignature);

         // ���Flow Entry Array
        ptDataFlowArray.clear();

        // ����һ��Flow Entry������
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

            // ����topology-tag��Ϣ        
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

            // ����statistics��Ϣ
            ptDataFlowEntryTemp.clear();
            ptDataFlowEntryTemp.put("t", pstAgentFlowEntry->stFlowDetectResult.lT5);
            ptDataFlowEntryTemp.put("packet-sent", pstAgentFlowEntry->stFlowDetectResult.lPktSentCounter);
            ptDataFlowEntryTemp.put("packet-drops", pstAgentFlowEntry->stFlowDetectResult.lPktDropCounter);       
            ptDataFlowEntry.put_child("statistics", ptDataFlowEntryTemp);
            
            // ����json����
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
#if 0
        // ���Signature
        string strSignature =   ptDataRoot.get<string>("MessageSignature");
        
        // У��ǩ��
        if (0 == strSignature.compare(UrgentFlowIssueSignature))
        {
            string strAction    =   ptDataRoot.get<string>("Action");
            
            // У��action��content
            if( (0 != strAction.compare(UrgentFlowIssueAction)) )
            {
                JSON_PARSER_WARNING("Unsupported Action[%s].", strAction.c_str());
                // У��ʧ��
                return AGENT_E_ERROR;
            }
            
            // ��data�н�������,���stServerFlowKey, Ȼ�����FlowManager�ӿ����̽����.
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
	INT32 iRet = pcFlowManager -> FlowManagerAction(interval);
	return iRet;
}

