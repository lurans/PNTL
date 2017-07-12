
#include <sstream>
#include <stdlib.h>
//#include <boost/property_tree/json_parser.hpp>

using namespace std;

#include "Log.h"
#include "AgentJsonAPI.h"
#include "MessagePlatformServer.h"

// 构造函数, 填充默认值.
MessagePlatformServer_C::MessagePlatformServer_C()
{
    MSG_SERVER_INFO("Creat a new MessagePlatformServer.");
    pcFlowManager = NULL;

}

// 析构函数, 释放必要资源.
MessagePlatformServer_C::~MessagePlatformServer_C()
{
    MSG_SERVER_INFO("Destroy an old MessagePlatformServer.");

    StopHttpDaemon();
    pcFlowManager = NULL;
}

// 根据参数完成初始化
INT32 MessagePlatformServer_C::Init(UINT32 uiNewPort, FlowManager_C* pcNewFlowManager)
{
    INT32 iRet = AGENT_OK;

    // 入参检查
    if(0 == uiNewPort
            || NULL == pcNewFlowManager)
    {
        MSG_SERVER_ERROR("Para Error: NewPort[%d], pcNewFlowManager[%u]", uiNewPort, pcNewFlowManager);
        return AGENT_E_PARA;
    }

    // 启动 httpd
    iRet = StartHttpDaemon(uiNewPort);
    if(iRet)
    {
        MSG_SERVER_ERROR("StartHttpDaemon failed[%d] on port[%u]", iRet, uiNewPort);
        return iRet;
    }

    // 初始化pcFlowManager.
    pcFlowManager =  pcNewFlowManager;

    return iRet;
}

/*
ServerAntServer 下发的紧急探测流格式
post
key = ServerAntAgent
data =
{
    "orgnizationSignature": "HuaweiDC3ServerAntsProbelistIssue",
    "serverIP": "10.1.1.1",
    "action": "post",
    "content": "probe-list",

    "flow": [
        {
        "urgent": "true:false",
        "sip": "",
        "dip": "",
        "ip-protocol": "icmp:tcp:udp",
        "sport-min": "",
        "sport-max": "",
        "sport-range": "1",
        "dscp": "",
        "topology-tag": {
            level": "1:2:3:4",
            svid": "",
            dvid": "",
        },

        {
        "urgent": "true:false",
        "sip": "",
        "dip": "",
        "ip-protocol": "icmp:tcp:udp",
        "sport-min": "",
        "sport-max": "",
        "sport-range": "1",
        "dscp": "",
        "topology-tag": {
            level": "1:2:3:4",
            svid": "",
            dvid": "",
        },
        ]
    },
}
理论上支持多个flow, 但是两个flow时data长度超过512byte, 会被http daemon截断,导致json parser失败.
*/
// POST 提交的key必须为ServerAntAgentName, 否则会返回错误.
#define ServerAntAgentName          "ServerAntsAgent"
#define ServerAntAgentAction        "ServerAntsAgentAction"
#define ServerAntsAgentIp           "ServerAntsAgentIp"
#define ServerAntsAgentConf         "ServerAntsAgentConf"

#if 1
// 使用json格式反馈post操作结果
// POST 处理成功时返回的信息
#define ResponcePageOK "{\"" ServerAntAgentName "States\":\"sucess\"}"
// POST 处理失败时返回的信息
#define ResponcePageError "{\"" ServerAntAgentName "States\":\"failed\"}"
// POST 收到不支持的key时返回的信息
#define ResponcePageUnsupported  "{\"" ServerAntAgentName "States\":\"unsupported\"}"
// POST 退出时返回的消息
#define ResponseExitOk "{\"" ServerAntAgentName "States\":\"exit sucess\"}"
#else
// POST 处理成功时返回的信息
#define ResponcePageOK "<html><head><title>"ServerAntAgentName"</title></head><body>Process Request Sucess</body></html>"
// POST 处理失败时返回的信息
#define ResponcePageError "<html><head><title>"ServerAntAgentName"</title></head><body>Process Request Failed</body></html>"
// POST 收到不支持的key时返回的信息
#define ResponcePageUnsupported  "<html><head><title>"ServerAntAgentName"</title></head><body>Unsupported Request</body></html>"
#endif

// Http Server Daemon POST操作回调处理函数
INT32 MessagePlatformServer_C::ProcessPostIterate(const char * pcKey, const char * pcData, UINT32 uiDataSize, string * pstrResponce)
{

    //MSG_SERVER_INFO("PostIterate. key:[%s],value[%s],size[%u]", pcKey, pcData, uiDataSize);

    INT32 iRet = AGENT_OK;

    // 入参检查
    if(NULL ==  pcKey || NULL == pcData || NULL ==  pstrResponce)
    {
        MSG_SERVER_ERROR("NULL Pointer for String");

        (* pstrResponce) = ResponcePageError;
        return AGENT_E_PARA;
    }

    if(0 == sal_strcmp(pcKey, ServerAntAgentName))
    {
        iRet = ProcessUrgentFlowFromServer(pcData, pcFlowManager);
        HandleResponse(iRet, pstrResponce);
        return iRet;
    }
    else if(0 == sal_strcmp(pcKey, ServerAntAgentAction))
    {
        MSG_SERVER_INFO("Begin to handle flowmanager action");
        iRet = ProcessActionFlowFromServer(pcData, pcFlowManager);
        HandleResponse(iRet, pstrResponce);
        return iRet;
    }
    else if(0 == sal_strcmp(pcKey, ServerAntsAgentIp))
    {
        SHOULD_PROBE = 1;
        (* pstrResponce) = ResponcePageOK;
        MSG_SERVER_INFO("PingList Has changed, request new pingList in next interval.");
    }
    else if (0 == sal_strcmp(pcKey, ServerAntsAgentConf))
    {
        iRet = ProcessConfigFlowFromServer(pcData, pcFlowManager);
        HandleResponse(iRet, pstrResponce);
		return iRet;
    }
    else
    {
        // 终止本次post处理, 忽略尚未处理的key/data值.
        MSG_SERVER_ERROR("Unsupported Post Key[%s]", pcKey);
        (* pstrResponce) = ResponcePageUnsupported;
        return AGENT_E_ERROR;
    }
}


void MessagePlatformServer_C::HandleResponse(INT32 iRet, string * pstrResponce)
{
    if(AGENT_EXIT == iRet)
    {
        (* pstrResponce) = ResponseExitOk;
    }
    else if(iRet)
    {
        (* pstrResponce) = ResponcePageError;
    }
    else
    {
        (* pstrResponce) = ResponcePageOK;
    }
}


