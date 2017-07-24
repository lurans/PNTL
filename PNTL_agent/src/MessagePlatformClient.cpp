

#include <curl/curl.h>
#include <sstream>
#include <string>
#include <stdlib.h>

using namespace std;

#include "Log.h"
#include "AgentJsonAPI.h"
#include "ServerAntAgentCfg.h"
#include "MessagePlatformClient.h"

// http ������ʱʱ��,��λs, ������ΪServerδ��Ӧ���ݵ��¹���
#define AGENT_REQUEST_TIMEOUT 30

size_t ReceiveResponce(void *ptr, size_t size, size_t nmemb, stringstream *pssResponce)
{
    char * pStr = (char *)ptr;

    if (strlen(pStr) != (nmemb + 2))
    {
        MSG_CLIENT_WARNING("ReceiveResponce wrong pStr size is [%u]", strlen(pStr));
    }

    char* newPStr = (char*)malloc(size * nmemb + 1);
    if (NULL == newPStr)
    {
        MSG_CLIENT_ERROR("Apply for size [%d] memory fail.", size * nmemb + 1);
        return 0;
    }
    sal_memset(newPStr, 0, size * nmemb + 1);
    memcpy(newPStr, pStr, size * nmemb);
    (*pssResponce) << newPStr;
    MSG_CLIENT_WARNING("ReceiveResponce..................strlen[%d].........................data:'%s'         nmemb:%d",
                       strlen(newPStr), newPStr, nmemb);
    free(newPStr);

    return size*nmemb;
}

const CHAR* SERVER_CERT_PATH = "/opt/huawei/ServerAntAgent/server_cert.pem";
const CHAR* AGENT_CERT_PATH = "/opt/huawei/ServerAntAgent/server.pem";
const CHAR* AGENT_KEY_PATH = "/opt/huawei/ServerAntAgent/server.key";
const CHAR* PEM_KEY_TYPE = "pem";
const CHAR* ACCEPT_TYPE = "Accept:application/json";
const CHAR* CONTENT_TYPE = "Content-Type:application/json";

// ͨ��http post�����ύ����
INT32 HttpPostData(stringstream * pssUrl, stringstream * pssPostData, stringstream *pssResponceData)
{
    INT32 iRet = AGENT_OK;
    string strTemp;

    CURL *curl;
    CURLcode res;
    char error_msg[CURL_ERROR_SIZE];
    struct curl_slist *headers = NULL;
    curl_global_init(CURL_GLOBAL_ALL);

    // ����һ��curl���
    curl = curl_easy_init();
    if (curl)
    {
        // ��ʼ����curl���.
        // ��ѡ��ӡ��ϸ��Ϣ, ������λ.
        res = curl_easy_setopt(curl, CURLOPT_VERBOSE, AGENT_FALSE);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_WARNING("curl easy setopt CURLOPT_VERBOSE failed[%d][%s]", res, curl_easy_strerror(res));
        }

        // ����curl���ִ���ʱ�ṩ��ϸ������Ϣ
        sal_memset(error_msg, 0, sizeof(error_msg));
        res = curl_easy_setopt(curl, CURLOPT_ERRORBUFFER, error_msg);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_WARNING("curl easy setopt CURLOPT_ERRORBUFFER failed[%d][%s]", res, curl_easy_strerror(res));
        }

        headers = curl_slist_append(headers, ACCEPT_TYPE);
        headers = curl_slist_append(headers, CONTENT_TYPE);
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);

        // ���÷����֤�飬�û���֤�����
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);
        curl_easy_setopt(curl,CURLOPT_CAINFO, SERVER_CERT_PATH);

        // ���ÿͻ���֤���˽Կ
        curl_easy_setopt(curl,CURLOPT_SSLCERT,AGENT_CERT_PATH);
        curl_easy_setopt(curl,CURLOPT_SSLCERTTYPE, PEM_KEY_TYPE);
        curl_easy_setopt(curl,CURLOPT_SSLKEY,AGENT_KEY_PATH);
        curl_easy_setopt(curl,CURLOPT_SSLKEYTYPE, PEM_KEY_TYPE);

        // �趨��ʱʱ��, ������ΪSERVER����Ӧ������.
        res = curl_easy_setopt(curl, CURLOPT_TIMEOUT, AGENT_REQUEST_TIMEOUT);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy setopt CURLOPT_TIMEOUT failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }

        // http�� "\n" �����⺬��, �ַ����еĻ��з�ȫ��Ҫ�滻��.
        string strOld = "\n";
        string strNew = " ";

        // �趨URL
        strTemp.clear();
        strTemp = pssUrl->str();
        sal_string_replace(&strTemp, &strOld, &strNew);
        MSG_CLIENT_INFO("URL:[%s]", strTemp.c_str());
        res = curl_easy_setopt(curl, CURLOPT_URL, strTemp.c_str());
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy setopt CURLOPT_URL failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }

        // �趨 post ����
        // ֱ��ʹ��ssPostData.str().c_str()ʱ, �ַ������п��ַ�, ʹ��stringתһ��.
        strTemp.clear();
        strTemp = pssPostData->str();
        sal_string_replace(&strTemp, &strOld, &strNew);

        MSG_CLIENT_INFO("PostFields:[%s]", strTemp.c_str());
        res = curl_easy_setopt(curl, CURLOPT_POSTFIELDS, strTemp.c_str());
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy setopt CURLOPT_POSTFIELDS failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }

        // �趨����response�ĺ���
        res = curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, ReceiveResponce);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy setopt CURLOPT_POSTFIELDS failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }
        pssResponceData->clear();
        pssResponceData->str("");
        // �趨ReceiveResponce�����Ĳ���
        res = curl_easy_setopt(curl, CURLOPT_WRITEDATA, pssResponceData);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy setopt CURLOPT_POSTFIELDS failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }

        // ִ���ύ����, ��������,�ύ����,�ȴ�Ӧ���.
        res = curl_easy_perform(curl);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy perform failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }
        // �ͷ�curl��Դ, �Ͽ�����.
        curl_easy_cleanup(curl);
    }
    else
    {
        MSG_CLIENT_ERROR("curl easy init failed");
        iRet = AGENT_E_ERROR;
    }
    curl_slist_free_all(headers);
    curl_global_cleanup();
    return iRet;
}

const string HTTPS_PREFIX = "https://";
const string COLON = ":";
const string PINGLIST_URL = "/rest/chkflow/pingList";
const string AGENT_IP_URL = "/rest/chkflow/agentIp";
const string AGENT_CONFIG_URL = "/rest/chkflow/pntlServerInfo";

// ��ServerAnrServer�����µ�probe�б�
INT32 RequestProbeListFromServer(FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;

    // �����ύ��URL��ַ
    stringstream ssUrl;
    // ������Ҫpost������,json��ʽ�ַ���, ��jsonģ������.
    stringstream ssPostData;
    // ����post��response(��ѯ���),json��ʽ�ַ���, ��������jsonģ�鴦��.
    stringstream ssResponceData;
    char * pcJsonData = NULL;

    // ���� URL
    UINT32 uiServerIP = 0;
    UINT32 uiServerPort = 0;
    iRet = pcFlowManager->pcAgentCfg->GetServerAddress(&uiServerIP, &uiServerPort);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Get Server Address failed[%d]", iRet);
        return iRet;
    }
    ssUrl.clear();
    ssUrl << HTTPS_PREFIX << sal_inet_ntoa(uiServerIP) << COLON << uiServerPort << PINGLIST_URL;

    MSG_CLIENT_INFO("URL [%s]", ssUrl.str().c_str());
    // ����post����
    ssPostData.clear();
    ssPostData.str("");

    iRet = CreateProbeListRequestPostData(pcFlowManager->pcAgentCfg, &ssPostData);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Creat Probe-List Request Post Data failed[%d]", iRet);
        return iRet;
    }

    ssResponceData.clear();
    ssResponceData.str("");
    iRet = HttpPostData(&ssUrl, &ssPostData, &ssResponceData);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Http Post Data failed[%d]", iRet);
        return iRet;
    }

    // �ַ�����ʽת��.
    string strResponceData = ssResponceData.str();
    pcJsonData = (char *)strResponceData.c_str();

    // ����response����
    iRet = ProcessNormalFlowFromServer( pcJsonData, pcFlowManager);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Process Normal Flow From Server failed[%d], pcJsonData: [%s]", iRet, pcJsonData);
        return iRet;
    }
    return iRet;
}


// ��ServerAnrServer�����µ�probe�б�
INT32 ReportDataToServer(ServerAntAgentCfg_C *pcAgentCfg, stringstream * pstrReportData, string strUrl)
{
    INT32 iRet = AGENT_OK;

    // �����ύ��URL��ַ
    stringstream ssUrl;
    // ����post��response(��ѯ���),json��ʽ�ַ���, ��������jsonģ�鴦��.
    stringstream ssResponceData;
    const char * pcJsonData = NULL;

    // ���� URL
    UINT32 uiServerIP = 0;
    UINT32 uiServerPort = 0;

    iRet = pcAgentCfg->GetServerAddress(&uiServerIP, &uiServerPort);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Get Server Address failed[%d]", iRet);
        return iRet;
    }
    ssUrl.clear();
    ssUrl << HTTPS_PREFIX << sal_inet_ntoa(uiServerIP) << COLON << uiServerPort << strUrl;

    ssResponceData.clear();
    ssResponceData.str("");
    iRet = HttpPostData(&ssUrl, pstrReportData, &ssResponceData);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Http Post Data failed[%d]", iRet);
        return iRet;
    }

    // �ַ�����ʽת��.
    string strResponceData = ssResponceData.str();
    pcJsonData = strResponceData.c_str();

    // ����response����
    MSG_CLIENT_INFO("Responce [%s]", strResponceData.c_str());

    return iRet;
}

INT32 ReportAgentIPToServer(ServerAntAgentCfg_C * pcAgentCfg)
{
    INT32 iRet = AGENT_OK;

    // �����ύ��URL��ַ
    stringstream ssUrl;
    // ������Ҫpost������,json��ʽ�ַ���, ��jsonģ������.
    stringstream ssPostData;
    // ����post��response(��ѯ���),json��ʽ�ַ���, ��������jsonģ�鴦��.
    stringstream ssResponceData;
    char * pcJsonData = NULL;

    // ���� URL
    UINT32 uiServerIP = 0;
    UINT32 uiServerPort = 0;
    iRet = pcAgentCfg->GetServerAddress(&uiServerIP, &uiServerPort);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Get Server Address failed[%d]", iRet);
        return iRet;
    }

    ssUrl.clear();
    ssUrl << HTTPS_PREFIX << sal_inet_ntoa(uiServerIP) << COLON << uiServerPort << AGENT_IP_URL;
    MSG_CLIENT_INFO("URL [%s]", ssUrl.str().c_str());


    // ����post����
    ssPostData.clear();
    ssPostData.str("");
    iRet = CreatAgentIPRequestPostData(pcAgentCfg, &ssPostData);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Creat Report Agent IP Request Post Data failed[%d]", iRet);
        return iRet;
    }
    MSG_CLIENT_INFO("PostData [%s]", ssPostData.str().c_str());

    ssResponceData.clear();
    ssResponceData.str("");
    iRet = HttpPostData(&ssUrl, &ssPostData, &ssResponceData);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Http Post Data failed[%d]", iRet);
        return iRet;
    }

    // �ַ�����ʽת��.
    string strResponceData = ssResponceData.str();
    pcJsonData = (char *)strResponceData.c_str();

    // ����response����
    MSG_CLIENT_INFO("Responce [%s]", strResponceData.c_str());
    return iRet;
}

// ��ServerAnrServer�����µ�����
INT32 RequestConfigFromServer(FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;

    // �����ύ��URL��ַ
    stringstream ssUrl;
    // ������Ҫpost������,json��ʽ�ַ���, ��jsonģ������.
    stringstream ssPostData;
    // ����post��response(��ѯ���),json��ʽ�ַ���, ��������jsonģ�鴦��.
    stringstream ssResponceData;
    char * pcJsonData = NULL;

    // ���� URL
    UINT32 uiServerIP = 0;
    UINT32 uiServerPort = 0;
    iRet = pcFlowManager->pcAgentCfg->GetServerAddress(&uiServerIP, &uiServerPort);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Get Server Address failed[%d]", iRet);
        return iRet;
    }
    ssUrl.clear();
    ssUrl << HTTPS_PREFIX << sal_inet_ntoa(uiServerIP) << COLON << uiServerPort << AGENT_CONFIG_URL;

    // ����post����
    ssPostData.clear();
    ssPostData.str("{}");

    ssResponceData.clear();
    ssResponceData.str("");
    iRet = HttpPostData(&ssUrl, &ssPostData, &ssResponceData);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Http Post Data failed[%d]", iRet);
        return iRet;
    }

    // �ַ�����ʽת��.
    string strResponceData = ssResponceData.str();
    pcJsonData = (char *)strResponceData.c_str();

    // ����response����
    iRet = ProcessServerConfigFlowFromServer( pcJsonData, pcFlowManager);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Process Server config Flow From Server failed[%d]", iRet);
        return iRet;
    }
    return iRet;
}