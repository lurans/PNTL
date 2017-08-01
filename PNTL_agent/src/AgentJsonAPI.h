
#ifndef __SRC_AgentJsonAPI_H__
#define __SRC_AgentJsonAPI_H__
#include <boost/property_tree/json_parser.hpp>

using namespace std;
// ʹ��boost��property_tree��չ�⴦��json��ʽ����.
using namespace boost::property_tree;
#include <sstream>
#include "FlowManager.h"

// ����Agent���������ļ�, ��ɳ�ʼ������.
extern INT32 ParserLocalCfg(const CHAR * pcJsonData, ServerAntAgentCfg_C * pcCfg);

// ����json��ʽ���ַ���, ���·���FlowManager, ������Server�����·���Urgent̽����.
extern INT32 ProcessUrgentFlowFromServer(const CHAR * pcJsonData, FlowManager_C* pcFlowManager);

// ����json��ʽ���ַ���, ���·���FlowManager, ��������Server����ʱServer�ظ�����ͨ̽����.
extern INT32 ProcessNormalFlowFromServer(CHAR * pcJsonData, FlowManager_C* pcFlowManager);

// ����json��ʽ���ַ���, ���ڷ�����Server����Probe-listʱ�ύ��post data.
extern INT32 CreateProbeListRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData);

// ����json��ʽ���ַ���, ������Analyzer�ϱ���ʱ��Ϣ.
extern INT32 CreateLatencyReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData, UINT32 maxDelay);

// ����json��ʽ���ַ���, ������Analyzer�ϱ�������Ϣ.
extern INT32 CreateDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData);

extern INT32 ProcessActionFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager);

extern INT32 CreatAgentIPRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData);

extern INT32 ProcessConfigFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager);

extern void SaveLossRateToFile(AgentFlowTableEntry_S * pstAgentFlowEntry);

extern INT32 ProcessServerConfigFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager);

extern INT32 ParseLocalAgentConfig(const char * pcJsonData, FlowManager_C * pcFlowManager);

extern INT32 GetFlowInfoFromConfigFile(string dip, ServerFlowKey_S * pstNewServerFlowKey, ServerAntAgentCfg_C* pcAgentCfg);

#endif
