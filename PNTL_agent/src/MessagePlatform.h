
#ifndef __SRC_MessagePlatform_H__
#define __SRC_MessagePlatform_H__

#include "ServerAntAgentCfg.h"

// 向Server注册Agent
extern INT32 ReportToServer(ServerAntAgentCfg_C * pcCfg);
// 获取Server端配置信息
extern INT32 GetCfgFromServer(ServerAntAgentCfg_C * pcCfg);

#endif
