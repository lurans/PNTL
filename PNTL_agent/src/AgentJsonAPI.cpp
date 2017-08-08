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
    {
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

        "ProtocolUDP" :
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
        pcCfg->SetServerAddress(uiIp, uiPort);

        // ����ServerAntAgent����.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntAgent");
        strTemp = ptDataTmp.get<string>("MgntIP");
        uiIp = sal_inet_aton(strTemp.c_str());
        pcCfg->SetMgntIP(uiIp);


        strTemp = ptDataTmp.get<string>("AgentIP");
        uiIp = sal_inet_aton(strTemp.c_str());
        pcCfg->SetAgentAddress(uiIp);

        uiData = ptDataTmp.get<UINT32>("ReportPeriod");
        pcCfg->SetReportPeriod(uiData);

        uiData = ptDataTmp.get<UINT32>("QueryPeriod");
        pcCfg->SetQueryPeriod(uiData);

        uiData = ptDataTmp.get<UINT32>("DetectPeriod");
        pcCfg->SetDetectPeriod(uiData);

        uiData = ptDataTmp.get<UINT32>("DetectTimeoutPeriod");
        pcCfg->SetDetectTimeout(uiData);

        uiData = ptDataTmp.get<UINT32>("DetectDropThresh");
        pcCfg->SetDetectDropThresh(uiData);

        // ����ServerAntAgent.ProtocolUDP����.
        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("ServerAntAgent.ProtocolUDP");
        UINT32 uiSrcPortMin = ptDataTmp.get<UINT32>("SrcPortMin");
        UINT32 uiSrcPortMax = ptDataTmp.get<UINT32>("SrcPortMax");
        UINT32 uiDestPort   = ptDataTmp.get<UINT32>("DestPort");
        pcCfg->SetProtocolUDP(uiSrcPortMin, uiSrcPortMax, uiDestPort);

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

INT32 CreatAgentIPRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData)
{
    INT32 iRet = AGENT_OK;

    stringstream ssJsonData;
    ptree ptDataRoot;
    UINT32 uiIp, uiMgntIp;
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        pcCfg->GetAgentAddress(&uiIp);
        if (iRet)
        {
            JSON_PARSER_ERROR("GetAgentAddress failed[%d]", iRet);
            return iRet;
        }

        pcCfg->GetMgntIP(&uiMgntIp);
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
INT32 CreateLatencyReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData, UINT32 maxDelay, UINT32 bigPkgSize)
{
    INT64 max = pstAgentFlowEntry->stFlowDetectResult.lLatencyMax;
    if (-1 != max && 0 != maxDelay && maxDelay * 1000 > max)
    {
        JSON_PARSER_INFO("Max delay is [%d], less than threshold[%d], does not report.", max, maxDelay * 1000);
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
                if (bigPkgSize)
                {
                    ptDataFlowEntry.put("package-size", bigPkgSize);
                }
                else
                {
                    ptDataFlowEntry.put("package-size", BIG_PACKAGE_SIZE);
                }
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
INT32 CreateDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData, UINT32 bigPkgSize)
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
                if (bigPkgSize)
                {
                    ptDataFlowEntry.put("package-size", bigPkgSize);
                }
                else
                {
                    ptDataFlowEntry.put("package-size", BIG_PACKAGE_SIZE);
                }
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

// ����json��ʽ��flow array, ���·���FlowManager,
INT32 IssueFlowFromConfigFile(ptree ptFlowArray, FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;

    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        ServerFlowKey_S stNewServerFlowKey;
        string dip;
        // ����flow flow array, ��ɽ������·�
        for (ptree::iterator itFlow = ptFlowArray.begin(); itFlow != ptFlowArray.end(); itFlow++)
        {
            dip = itFlow->second.data(); // firstΪ��, boost��ʽ
            sal_memset(&stNewServerFlowKey, 0, sizeof(stNewServerFlowKey));

            iRet = GetFlowInfoFromConfigFile(dip, &stNewServerFlowKey, pcFlowManager->pcAgentCfg);
            if (iRet)
            {
                JSON_PARSER_ERROR("Get Flow Info From Json failed [%d]", iRet);
                return iRet;
            }

            // ��ͨ������ӵ����ñ�, �����õ�������Ч.
            iRet = pcFlowManager->ServerWorkingFlowTableAdd(&stNewServerFlowKey);
            if (AGENT_OK != iRet)
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

// ����Server�·���json��ʽflow����,ת����ServerFlowKey_S��ʽ
INT32 GetFlowInfoFromConfigFile(string dip, ServerFlowKey_S * pstNewServerFlowKey, ServerAntAgentCfg_C* pcAgentCfg)
{
    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        string strTemp;
        UINT32 uiDataTemp = 0;

        // ��ʼ��
        sal_memset(pstNewServerFlowKey, 0, sizeof(ServerFlowKey_S));

        pstNewServerFlowKey->uiUrgentFlow = 0;
        pstNewServerFlowKey->eProtocol = AGENT_DETECT_PROTOCOL_UDP;
        pstNewServerFlowKey->uiSrcIP = pcAgentCfg->GetAgentIP();

        pstNewServerFlowKey->uiDestIP = sal_inet_aton(dip.c_str());

        pstNewServerFlowKey->uiDscp          = pcAgentCfg->getDscp();
        pstNewServerFlowKey->uiSrcPortMin    = 32769;
        pstNewServerFlowKey->uiSrcPortMax    = 32868;
        pstNewServerFlowKey->uiSrcPortRange  = 5;

        pstNewServerFlowKey->stServerTopo.uiSvid   = 0;
        pstNewServerFlowKey->stServerTopo.uiDvid   = 0;
        pstNewServerFlowKey->stServerTopo.uiLevel  = 1;
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when GetFlowInfoFromConfigFile.", e.what());
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

INT32 ParseLocalAgentConfig(const char * pcJsonData, FlowManager_C * pcFlowManager)
{
    INT32 iRet = AGENT_OK;
    string strTemp;
    UINT32 data;
    // boost::property_tree����, ���ڴ洢json��ʽ����.
    ptree ptDataRoot, ptDataTmp;

    // boost���г��ִ�����׳��쳣, δ��catch���쳣�����ϱ�, ���յ��½���abort�˳�.
    try
    {
        // pcData�ַ���ת��stringstream��ʽ, �������boost::property_tree����.
        stringstream ssStringData(pcJsonData);
        read_json(ssStringData, ptDataRoot);

        data = ptDataRoot.get<UINT32>("probe_period");
        if (data)
        {
            pcFlowManager->pcAgentCfg->SetDetectPeriod(data);
            JSON_PARSER_INFO("Current probe_period is [%u].", pcFlowManager->pcAgentCfg->GetDetectPeriod());
        }
        else
        {
            JSON_PARSER_INFO("probe_period is [%u], will stop detect now. ", data);
            pcFlowManager->FlowManagerAction();
        }

        data = ptDataRoot.get<UINT32>("port_count");
        pcFlowManager->pcAgentCfg->SetPortCount(data);
        JSON_PARSER_INFO("Current port_count is [%u].", pcFlowManager->pcAgentCfg->GetPortCount());

        data = ptDataRoot.get<UINT32>("report_period");
        pcFlowManager->pcAgentCfg->SetReportPeriod(data);
        JSON_PARSER_INFO("Current report_period is [%u].", pcFlowManager->pcAgentCfg->GetReportPeriod());

        data = ptDataRoot.get<UINT32>("package_size");
		pcFlowManager->pcAgentCfg->SetBigPkgSize(data);
		JSON_PARSER_INFO("Current package_size is [%u].", pcFlowManager->pcAgentCfg->GetBigPkgSize());
		
        data = ptDataRoot.get<UINT32>("pkg_count");
        pcFlowManager->pcAgentCfg->SetBigPkgRate(data);
        JSON_PARSER_INFO("Current pkg_count is [%u].", pcFlowManager->pcAgentCfg->GetBigPkgRate());

        data = ptDataRoot.get<UINT32>("delay_threshold");
        pcFlowManager->pcAgentCfg->SetMaxDelay(data);
        JSON_PARSER_INFO("Current delay_threshold is [%u].", pcFlowManager->pcAgentCfg->GetMaxDelay());

        data = ptDataRoot.get<UINT32>("dscp");
        pcFlowManager->pcAgentCfg->SetDscp(data);
        JSON_PARSER_INFO("Current dscp is [%u].", pcFlowManager->pcAgentCfg->getDscp());

        data = ptDataRoot.get<UINT32>("lossPkg_timeout");
        pcFlowManager->pcAgentCfg->SetDetectTimeout(data);
        JSON_PARSER_INFO("Current lossPkg_timeout is [%u].", pcFlowManager->pcAgentCfg->GetDetectTimeout());

        strTemp = ptDataRoot.get<string>("kafka_ip");
        pcFlowManager->pcAgentCfg->SetKafkaIp(strTemp);
        JSON_PARSER_INFO("Current kafka_ip is [%s].", pcFlowManager->pcAgentCfg->GetKafkaIp().c_str());

        strTemp = ptDataRoot.get<string>("topic");
        pcFlowManager->pcAgentCfg->SetTopic(strTemp);
        JSON_PARSER_INFO("Current topic is [%s].", pcFlowManager->pcAgentCfg->GetTopic().c_str());

        data = ptDataRoot.get<UINT32>("vbondIp_flag");
        if (data)
        {
            JSON_PARSER_INFO("Set vbondIp_flag to [%u], will report agent ip in next interval.", data);
            SHOULD_REPORT_IP = 1;
        }
        JSON_PARSER_INFO("Current vbondIp_flag is [%u].", data);

        data = ptDataRoot.get<UINT32>("dropPkgThresh");
        pcFlowManager->pcAgentCfg->SetDetectDropThresh(data);
        JSON_PARSER_INFO("Current dropPkgThresh is [%u].", pcFlowManager->pcAgentCfg->GetDetectDropThresh());

        ptDataTmp.clear();
        ptDataTmp = ptDataRoot.get_child("pingList");
        bool flag = false;
        for (ptree::iterator itFlow = ptDataTmp.begin(); itFlow != ptDataTmp.end(); itFlow++)
        {
            strTemp = itFlow->first.data(); // firstΪ��, boost��ʽ
            if (0 == sal_strcmp(strTemp.c_str(), sal_inet_ntoa(pcFlowManager->pcAgentCfg->GetAgentIP())))
            {
                ptDataTmp =  itFlow->second;
                flag = true;
                break;
            }
            else
            {
                continue;
            }

        }

        if (!flag)
        {
            JSON_PARSER_ERROR("Can not find agent pingList config by ip [%s]. Maybe first start.", sal_inet_ntoa(pcFlowManager->pcAgentCfg->GetAgentIP()));
            return AGENT_OK;
        }
        pcFlowManager->ServerClearFlowTable();
        iRet = IssueFlowFromConfigFile(ptDataTmp, pcFlowManager);
        if (iRet)
        {
            JSON_PARSER_ERROR("Issue Flow From Json Flow Array failed [%d]. Flow info[%s]", iRet, pcJsonData);
            return iRet;
        }
        pcFlowManager->RefreshAgentTable();
    }
    catch (exception const & e)
    {
        JSON_PARSER_ERROR("Caught exception [%s] when ParseLocalAgentConfig. LocalConfig:[%s]", e.what(), pcJsonData);
        return AGENT_E_ERROR;
    }
    return iRet;
}