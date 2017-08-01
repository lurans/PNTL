#ifndef __SRC_Log_H__
#define __SRC_Log_H__

#include "Sal.h"
#include "AgentCommon.h"

typedef enum tagAgentModule
{
    AGENT_MODULE_SAL  = 1,          // ϵͳ���书����־
    AGENT_MODULE_TIMER,             // TIMER ����ģ��
    AGENT_MODULE_INIT,              // ��ʼ������,main������
    AGENT_MODULE_COMMON,            // ����ģ��
    AGENT_MODULE_THREAD_CLASS,      // THREAD_CLASS ��
    AGENT_MODULE_HTTP_DAEMON,       // HTTP_DAEMON ��
    AGENT_MODULE_KAFKA_CLIENT,      // HTTP_DAEMON ��
    AGENT_MODULE_AGENT_CFG,         // ServerAntAgentCfg ��
    AGENT_MODULE_DETECT_WORKER,     // DetectWorker ��
    AGENT_MODULE_FLOW_MANAGER,      // FlowMananger ��
    AGENT_MODULE_MSG_SERVER,        // MessagePlatformServer ��
    AGENT_MODULE_MSG_CLIENT,        // MSG_CLIENT ģ��
    AGENT_MODULE_JSON_PARSER,       // JSON_PARSER ģ��(AgentJsonAPI)
    AGENT_MODULE_SAVE_REPORTDATA,
    AGENT_MODULE_FILE_NOTIFIER,
    AGENT_MODULE_MAX

} AgentModule_E;

typedef enum tagAgentLogType
{
    AGENT_LOG_TYPE_INFO  = 1,   // ��ʾ
    AGENT_LOG_TYPE_WARNING,     // �澯
    AGENT_LOG_TYPE_ERROR,       // ����
    AGENT_LOG_TYPE_LOSS_PACKET,       // ����
    AGENT_LOG_TYPE_LATENCY,       // ��ʱ
    AGENT_LOG_TYPE_MAX
} AgentLogType_E;

typedef enum  tagAgentLogMode
{
    AGENT_LOG_MODE_NORMAL = 0,   // ��־ֱ�Ӵ�ӡ���ն�,���ڵ��ó���ĵ�ǰĿ¼������־�ļ�.
    AGENT_LOG_MODE_DAEMON,       // ��־����ӡ���ն�,ֱ�Ӽ�¼��syslog.
    AGENT_LOG_MODE_MAX
} AgentLogMode_E;

extern INT32 SetNewLogMode(AgentLogMode_E eNewLogMode);
extern INT32 SetNewLogDir(string strNewDirPath);
extern void GetPrintTime(char *timestr);
extern INT32 AgentLogPrintf(AgentModule_E eModule, AgentLogType_E eLogType, const CHAR *szFormat, ...);
extern string GetLossLogFilePath();
extern string GetLatencyLogFilePath();

//__PRETTY_FUNCTION__
#if 1
#define MODULE_LOSS(module, format, ...)                                        \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_LOSS_PACKET, format, ##__VA_ARGS__);    \
} while(0)

#define MODULE_LATENCY(module, format, ...)                                        \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_LATENCY,  format, ##__VA_ARGS__);    \
} while(0)

#define MODULE_INFO(module, format, ...)                                        \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_INFO,                \
            "[Info]    " #module " [%s][%d] " format, __FUNCTION__, __LINE__, ##__VA_ARGS__);    \
} while(0)

#define MODULE_WARNING(module, format, ...)                                     \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_WARNING,             \
            "[Warning] " #module " [%s][%d] " format, __FUNCTION__, __LINE__, ##__VA_ARGS__); \
} while(0)

#define MODULE_ERROR(module, format, ...)                                       \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_ERROR,               \
            "[Error]   " #module " [%s][%d] " format, __FUNCTION__, __LINE__, ##__VA_ARGS__);   \
} while(0)
#else
#define MODULE_INFO(module, format, ...)                                        \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_INFO,                \
            "[Info]    "#module" [%s][%d] "format, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);    \
} while(0)

#define MODULE_WARNING(module, format, ...)                                     \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_WARNING,             \
            "[Warning] "#module" [%s][%d] "format, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__); \
} while(0)

#define MODULE_ERROR(module, format, ...)                                       \
do                                                                              \
{                                                                               \
    AgentLogPrintf(AGENT_MODULE_ ## module, AGENT_LOG_TYPE_ERROR,               \
            "[Error]   "#module" [%s][%d] "format, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);   \
} while(0)
#endif
#define SAL_INFO(...)               MODULE_INFO(SAL,   __VA_ARGS__)
#define TIMER_INFO(...)             MODULE_INFO(TIMER,   __VA_ARGS__)
#define INIT_INFO(...)              MODULE_INFO(INIT,   __VA_ARGS__)
#define COMMON_INFO(...)            MODULE_INFO(COMMON,   __VA_ARGS__)
#define AGENT_CFG_INFO(...)         MODULE_INFO(AGENT_CFG,   __VA_ARGS__)
#define THREAD_CLASS_INFO(...)      MODULE_INFO(THREAD_CLASS,   __VA_ARGS__)
#define HTTP_DAEMON_INFO(...)       MODULE_INFO(HTTP_DAEMON,   __VA_ARGS__)
#define KAFKA_CLIENT_INFO(...)      MODULE_INFO(KAFKA_CLIENT,   __VA_ARGS__)
#define DETECT_WORKER_INFO(...)     MODULE_INFO(DETECT_WORKER,   __VA_ARGS__)
#define FLOW_MANAGER_INFO(...)      MODULE_INFO(FLOW_MANAGER,   __VA_ARGS__)
#define MSG_SERVER_INFO(...)        MODULE_INFO(MSG_SERVER,   __VA_ARGS__)
#define MSG_CLIENT_INFO(...)        MODULE_INFO(MSG_CLIENT,   __VA_ARGS__)
#define JSON_PARSER_INFO(...)       MODULE_INFO(JSON_PARSER,   __VA_ARGS__)
#define SAVE_LOSS_INFO(...)         MODULE_LOSS(SAVE_REPORTDATA,__VA_ARGS__)
#define SAVE_LATENCY_INFO(...)      MODULE_LATENCY(SAVE_REPORTDATA, __VA_ARGS__)
#define FILE_NOTIFIER_INFO(...)     MODULE_INFO(FILE_NOTIFIER, __VA_ARGS__)

#define SAL_WARNING(...)               MODULE_WARNING(SAL,   __VA_ARGS__)
#define TIMER_WARNING(...)             MODULE_WARNING(TIMER,   __VA_ARGS__)
#define INIT_WARNING(...)              MODULE_WARNING(INIT,   __VA_ARGS__)
#define COMMON_WARNING(...)            MODULE_WARNING(COMMON,   __VA_ARGS__)
#define THREAD_CLASS_WARNING(...)      MODULE_WARNING(THREAD_CLASS,   __VA_ARGS__)
#define HTTP_DAEMON_WARNING(...)       MODULE_WARNING(HTTP_DAEMON,   __VA_ARGS__)
#define KAFKA_CLIENT_WARNING(...)      MODULE_WARNING(KAFKA_CLIENT,   __VA_ARGS__)
#define AGENT_CFG_WARNING(...)         MODULE_WARNING(AGENT_CFG,   __VA_ARGS__)
#define DETECT_WORKER_WARNING(...)     MODULE_WARNING(DETECT_WORKER,   __VA_ARGS__)
#define FLOW_MANAGER_WARNING(...)      MODULE_WARNING(FLOW_MANAGER,   __VA_ARGS__)
#define MSG_SERVER_WARNING(...)        MODULE_WARNING(MSG_SERVER,   __VA_ARGS__)
#define MSG_CLIENT_WARNING(...)        MODULE_WARNING(MSG_CLIENT,   __VA_ARGS__)
#define JSON_PARSER_WARNING(...)       MODULE_WARNING(JSON_PARSER,   __VA_ARGS__)
#define FILE_NOTIFIER_WARNING(...)     MODULE_WARNING(FILE_NOTIFIER, __VA_ARGS__)

#define SAL_ERROR(...)               MODULE_ERROR(SAL,   __VA_ARGS__)
#define TIMER_ERROR(...)             MODULE_ERROR(TIMER,   __VA_ARGS__)
#define INIT_ERROR(...)              MODULE_ERROR(INIT,   __VA_ARGS__)
#define COMMON_ERROR(...)            MODULE_ERROR(COMMON,   __VA_ARGS__)
#define THREAD_CLASS_ERROR(...)      MODULE_ERROR(THREAD_CLASS,   __VA_ARGS__)
#define HTTP_DAEMON_ERROR(...)       MODULE_ERROR(HTTP_DAEMON,   __VA_ARGS__)
#define KAFKA_CLIENT_ERROR(...)      MODULE_ERROR(KAFKA_CLIENT,   __VA_ARGS__)
#define AGENT_CFG_ERROR(...)         MODULE_ERROR(AGENT_CFG,   __VA_ARGS__)
#define DETECT_WORKER_ERROR(...)     MODULE_ERROR(DETECT_WORKER,   __VA_ARGS__)
#define FLOW_MANAGER_ERROR(...)      MODULE_ERROR(FLOW_MANAGER,   __VA_ARGS__)
#define MSG_SERVER_ERROR(...)        MODULE_ERROR(MSG_SERVER,   __VA_ARGS__)
#define MSG_CLIENT_ERROR(...)        MODULE_ERROR(MSG_CLIENT,   __VA_ARGS__)
#define JSON_PARSER_ERROR(...)       MODULE_ERROR(JSON_PARSER,   __VA_ARGS__)
#define FILE_NOTIFIER_ERROR(...)     MODULE_ERROR(FILE_NOTIFIER, __VA_ARGS__)

#endif
