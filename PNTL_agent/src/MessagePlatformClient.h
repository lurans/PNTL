
#ifndef __SRC_MessagePlatformClient_H__
#define __SRC_MessagePlatformClient_H__


// ��ServerAnrServer�����µ�probe�б�
extern INT32 RequestProbeListFromServer(FlowManager_C* pcFlowManager);

extern INT32 ReportDataToServer(stringstream * pstrReportData,  string strUrl);

// �ϱ�AgentIP��Server
extern INT32 ReportAgentIPToServer(ServerAntAgentCfg_C * pcAgentCfg);

#endif
