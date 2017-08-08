
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

// ����json��ʽ���ַ���, ������Analyzer�ϱ���ʱ��Ϣ.
extern INT32 CreateLatencyReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData, UINT32 maxDelay, UINT32 bigPkgSize);

// ����json��ʽ���ַ���, ������Analyzer�ϱ�������Ϣ.
extern INT32 CreateDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData, UINT32 bigPkgSize);

extern INT32 CreatAgentIPRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData);

extern INT32 ParseLocalAgentConfig(const char * pcJsonData, FlowManager_C * pcFlowManager);

extern INT32 GetFlowInfoFromConfigFile(string dip, ServerFlowKey_S * pstNewServerFlowKey, ServerAntAgentCfg_C* pcAgentCfg);

extern UINT32 ParseProbePeriodConfig(const char * pcJsonData, FlowManager_C * pcFlowManager);

#endif
