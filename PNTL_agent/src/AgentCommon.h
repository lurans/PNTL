#ifndef __SRC_AgentCommon_H__
#define __SRC_AgentCommon_H__

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
    AGENT_EXIT          = 10
};

#define AGENT_ENABLE                         1
#define AGENT_DISABLE                        0

#define AGENT_TRUE                          1
#define AGENT_FALSE                         0

extern INT32 SHOULD_PROBE;

typedef enum  tagAgentDetectProtocolType
{
    AGENT_DETECT_PROTOCOL_NULL = 0,   // δ����
    AGENT_DETECT_PROTOCOL_ICMP,       // ICMP, Ԥ��Ҫ֧��
    AGENT_DETECT_PROTOCOL_UDP,        // UDP, ֧��
    AGENT_DETECT_PROTOCOL_TCP,        // TCP,�ݲ�֧��
    AGENT_DETECT_PROTOCOL_MAX
}AgentDetectProtocolType_E;

#endif
