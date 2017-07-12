
#include <sstream>
#include <stdlib.h>
//#include <boost/property_tree/json_parser.hpp>

using namespace std;

#include "Log.h"
#include "AgentJsonAPI.h"
#include "MessagePlatformServer.h"

// ���캯��, ���Ĭ��ֵ.
MessagePlatformServer_C::MessagePlatformServer_C()
{
    MSG_SERVER_INFO("Creat a new MessagePlatformServer.");
    pcFlowManager = NULL;

}

// ��������, �ͷű�Ҫ��Դ.
MessagePlatformServer_C::~MessagePlatformServer_C()
{
    MSG_SERVER_INFO("Destroy an old MessagePlatformServer.");

    StopHttpDaemon();
    pcFlowManager = NULL;
}

// ���ݲ�����ɳ�ʼ��
INT32 MessagePlatformServer_C::Init(UINT32 uiNewPort, FlowManager_C* pcNewFlowManager)
{
    INT32 iRet = AGENT_OK;

    // ��μ��
    if(0 == uiNewPort
            || NULL == pcNewFlowManager)
    {
        MSG_SERVER_ERROR("Para Error: NewPort[%d], pcNewFlowManager[%u]", uiNewPort, pcNewFlowManager);
        return AGENT_E_PARA;
    }

    // ���� httpd
    iRet = StartHttpDaemon(uiNewPort);
    if(iRet)
    {
        MSG_SERVER_ERROR("StartHttpDaemon failed[%d] on port[%u]", iRet, uiNewPort);
        return iRet;
    }

    // ��ʼ��pcFlowManager.
    pcFlowManager =  pcNewFlowManager;

    return iRet;
}

/*
ServerAntServer �·��Ľ���̽������ʽ
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
������֧�ֶ��flow, ��������flowʱdata���ȳ���512byte, �ᱻhttp daemon�ض�,����json parserʧ��.
*/
// POST �ύ��key����ΪServerAntAgentName, ����᷵�ش���.
#define ServerAntAgentName          "ServerAntsAgent"
#define ServerAntAgentAction        "ServerAntsAgentAction"
#define ServerAntsAgentIp           "ServerAntsAgentIp"
#define ServerAntsAgentConf         "ServerAntsAgentConf"

#if 1
// ʹ��json��ʽ����post�������
// POST ����ɹ�ʱ���ص���Ϣ
#define ResponcePageOK "{\"" ServerAntAgentName "States\":\"sucess\"}"
// POST ����ʧ��ʱ���ص���Ϣ
#define ResponcePageError "{\"" ServerAntAgentName "States\":\"failed\"}"
// POST �յ���֧�ֵ�keyʱ���ص���Ϣ
#define ResponcePageUnsupported  "{\"" ServerAntAgentName "States\":\"unsupported\"}"
// POST �˳�ʱ���ص���Ϣ
#define ResponseExitOk "{\"" ServerAntAgentName "States\":\"exit sucess\"}"
#else
// POST ����ɹ�ʱ���ص���Ϣ
#define ResponcePageOK "<html><head><title>"ServerAntAgentName"</title></head><body>Process Request Sucess</body></html>"
// POST ����ʧ��ʱ���ص���Ϣ
#define ResponcePageError "<html><head><title>"ServerAntAgentName"</title></head><body>Process Request Failed</body></html>"
// POST �յ���֧�ֵ�keyʱ���ص���Ϣ
#define ResponcePageUnsupported  "<html><head><title>"ServerAntAgentName"</title></head><body>Unsupported Request</body></html>"
#endif

// Http Server Daemon POST�����ص�������
INT32 MessagePlatformServer_C::ProcessPostIterate(const char * pcKey, const char * pcData, UINT32 uiDataSize, string * pstrResponce)
{

    //MSG_SERVER_INFO("PostIterate. key:[%s],value[%s],size[%u]", pcKey, pcData, uiDataSize);

    INT32 iRet = AGENT_OK;

    // ��μ��
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
        // ��ֹ����post����, ������δ�����key/dataֵ.
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


