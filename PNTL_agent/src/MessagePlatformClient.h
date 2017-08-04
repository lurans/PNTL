#ifndef __SRC_MessagePlatformClient_H__
#define __SRC_MessagePlatformClient_H__

extern INT32 ReportDataToServer(ServerAntAgentCfg_C *pcAgentCfg, stringstream * pstrReportData,  string strUrl);

// …œ±®AgentIP÷¡Server
extern INT32 ReportAgentIPToServer(ServerAntAgentCfg_C * pcAgentCfg);

#endif
