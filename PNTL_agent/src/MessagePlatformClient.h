
#ifndef __SRC_MessagePlatformClient_H__
#define __SRC_MessagePlatformClient_H__


// ��ServerAnrServer�����µ�probe�б�
extern INT32 RequestProbeListFromServer(FlowManager_C* pcFlowManager);

extern INT32 ReportDataToServer(ServerAntAgentCfg_C *pcAgentCfg, stringstream * pstrReportData,  string strUrl);

// �ϱ�AgentIP��Server
extern INT32 ReportAgentIPToServer(ServerAntAgentCfg_C * pcAgentCfg);

extern INT32 RequestConfigFromServer(FlowManager_C* pcFlowManager);
#endif
