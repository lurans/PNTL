#ifndef __SRC_AgentCommon_H__
#define __SRC_AgentCommon_H__

#include "Sal.h"

enum
{
    AGENT_OK            = 0,
    AGENT_E_ERROR       = -1,
    AGENT_E_PARA        = -2,
    AGENT_E_NOT_FOUND   = -3,
    AGENT_E_MEMORY      = -4,
    AGENT_E_TIMER       = -5,
    AGENT_E_SOCKET      = -6,
    AGENT_E_THREAD      = -7,
    AGENT_E_HANDLER     = -8,
    AGENT_E_EXIST       = -9,
    AGENT_EXIT          = -10,
    AGENT_FILTER_DELAY  = -11
};

#define AGENT_ENABLE                         1
#define AGENT_DISABLE                        0

#define AGENT_TRUE                          1
#define AGENT_FALSE                         0
#define KAFKA_TOPIC_URL  "/mq/"

// Whether agent begin to query pingList
extern UINT32 SHOULD_DETECT_REPORT;

// 100, all big pkgs, 0, all small pkgs
extern UINT32 BIG_PKG_RATE;

extern UINT32 PROBE_INTERVAL;

extern UINT32 SHOULD_REPORT_IP;

const UINT32 BIG_PACKAGE_SIZE = 1000;
const UINT32 NORMAL_PACKAGE_SIZE = 40;

typedef enum  tagAgentDetectProtocolType
{
    AGENT_DETECT_PROTOCOL_NULL = 0,   // δ����
    AGENT_DETECT_PROTOCOL_ICMP,       // ICMP, Ԥ��Ҫ֧��
    AGENT_DETECT_PROTOCOL_UDP,        // UDP, ֧��
    AGENT_DETECT_PROTOCOL_TCP,        // TCP,�ݲ�֧��
    AGENT_DETECT_PROTOCOL_MAX
} AgentDetectProtocolType_E;

#endif
