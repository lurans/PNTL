
#ifndef __SRC_MessagePlatformClient_H__
#define __SRC_MessagePlatformClient_H__


// 向ServerAnrServer请求新的probe列表
extern INT32 RequestProbeListFromServer(FlowManager_C* pcFlowManager);

extern INT32 ReportDataToServer(stringstream * pstrReportData,  string strUrl);

// 上报AgentIP至Server
extern INT32 ReportAgentIPToServer(ServerAntAgentCfg_C * pcAgentCfg);

#endif
