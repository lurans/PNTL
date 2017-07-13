
#ifndef __SRC_AgentJsonAPI_H__
#define __SRC_AgentJsonAPI_H__

#include <sstream>
#include "FlowManager.h"

// 解析Agent本地配置文件, 完成初始化配置.
extern INT32 ParserLocalCfg(const CHAR * pcJsonData, ServerAntAgentCfg_C * pcCfg);

// 解析json格式的字符串, 并下发到FlowManager, 负责处理Server主导下发的Urgent探测流.
extern INT32 ProcessUrgentFlowFromServer(const CHAR * pcJsonData, FlowManager_C* pcFlowManager);

// 解析json格式的字符串, 并下发到FlowManager, 负责处理向Server请求时Server回复的普通探测流.
extern INT32 ProcessNormalFlowFromServer(CHAR * pcJsonData, FlowManager_C* pcFlowManager);

// 生成json格式的字符串, 用于发起向Server请求Probe-list时提交的post data.
extern INT32 CreateProbeListRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData);

// 生成json格式的字符串, 用于向Analyzer上报延时信息.
extern INT32 CreateLatencyReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData, UINT32 maxDelay);

// 生成json格式的字符串, 用于向Analyzer上报丢包信息.
extern INT32 CreateDropReportData(AgentFlowTableEntry_S * pstAgentFlowEntry, stringstream * pssReportData);

extern INT32 ProcessActionFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager);

extern INT32 CreatAgentIPRequestPostData(ServerAntAgentCfg_C * pcCfg, stringstream * pssPostData);

extern INT32 ProcessConfigFlowFromServer(const char * pcJsonData, FlowManager_C* pcFlowManager);

#endif
