
#ifndef __SRC_MessagePlatform_H__
#define __SRC_MessagePlatform_H__

#include "ServerAntAgentCfg.h"

// ��Serverע��Agent
extern INT32 ReportToServer(ServerAntAgentCfg_C * pcCfg);
// ��ȡServer��������Ϣ
extern INT32 GetCfgFromServer(ServerAntAgentCfg_C * pcCfg);

#endif
