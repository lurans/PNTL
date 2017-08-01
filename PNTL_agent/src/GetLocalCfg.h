
#ifndef __SRC_GetLocalCfg_H__
#define __SRC_GetLocalCfg_H__

#include "FlowManager.h"
#include "ServerAntAgentCfg.h"

// 获取Agent本地配置信息
extern INT32 GetLocalCfg(ServerAntAgentCfg_C * pcCfg);

extern void RecoverLossPktData(ServerAntAgentCfg_C *pcAgentCfg);

extern INT32 GetLocalAgentConfig(FlowManager_C * pcFlowManager);

#endif
