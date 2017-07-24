

#include <curl/curl.h>
#include <sstream>
#include <string>
#include <stdlib.h>

using namespace std;

#include "Log.h"
#include "AgentJsonAPI.h"
#include "ServerAntAgentCfg.h"
#include "MessagePlatformClient.h"

// http 请求处理超时时间,单位s, 避免因为Server未响应数据导致挂死
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

// 通过http post操作提交数据
INT32 HttpPostData(stringstream * pssUrl, stringstream * pssPostData, stringstream *pssResponceData)
{
    INT32 iRet = AGENT_OK;
    string strTemp;

    CURL *curl;
    CURLcode res;
    char error_msg[CURL_ERROR_SIZE];
    struct curl_slist *headers = NULL;
    curl_global_init(CURL_GLOBAL_ALL);

    // 创建一个curl句柄
    curl = curl_easy_init();
    if (curl)
    {
        // 开始配置curl句柄.
        // 可选打印详细信息, 帮助定位.
        res = curl_easy_setopt(curl, CURLOPT_VERBOSE, AGENT_FALSE);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_WARNING("curl easy setopt CURLOPT_VERBOSE failed[%d][%s]", res, curl_easy_strerror(res));
        }

        // 配置curl出现错误时提供详细错误信息
        sal_memset(error_msg, 0, sizeof(error_msg));
        res = curl_easy_setopt(curl, CURLOPT_ERRORBUFFER, error_msg);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_WARNING("curl easy setopt CURLOPT_ERRORBUFFER failed[%d][%s]", res, curl_easy_strerror(res));
        }

        headers = curl_slist_append(headers, ACCEPT_TYPE);
        headers = curl_slist_append(headers, CONTENT_TYPE);
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);

        // 设置服务端证书，用户认证服务端
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);
        curl_easy_setopt(curl,CURLOPT_CAINFO, SERVER_CERT_PATH);

        // 设置客户端证书和私钥
        curl_easy_setopt(curl,CURLOPT_SSLCERT,AGENT_CERT_PATH);
        curl_easy_setopt(curl,CURLOPT_SSLCERTTYPE, PEM_KEY_TYPE);
        curl_easy_setopt(curl,CURLOPT_SSLKEY,AGENT_KEY_PATH);
        curl_easy_setopt(curl,CURLOPT_SSLKEYTYPE, PEM_KEY_TYPE);

        // 设定超时时间, 避免因为SERVER无响应而挂死.
        res = curl_easy_setopt(curl, CURLOPT_TIMEOUT, AGENT_REQUEST_TIMEOUT);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy setopt CURLOPT_TIMEOUT failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }

        // http中 "\n" 有特殊含义, 字符串中的换行符全部要替换掉.
        string strOld = "\n";
        string strNew = " ";

        // 设定URL
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

        // 设定 post 数据
        // 直接使用ssPostData.str().c_str()时, 字符串中有空字符, 使用string转一下.
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

        // 设定接收response的函数
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
        // 设定ReceiveResponce函数的参数
        res = curl_easy_setopt(curl, CURLOPT_WRITEDATA, pssResponceData);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy setopt CURLOPT_POSTFIELDS failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }

        // 执行提交操作, 建立连接,提交数据,等待应答等.
        res = curl_easy_perform(curl);
        if(CURLE_OK != res)
        {
            MSG_CLIENT_ERROR("curl easy perform failed[%d][%s],detail info[%s]", res, curl_easy_strerror(res), error_msg);
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            curl_global_cleanup();
            return AGENT_E_ERROR;
        }
        // 释放curl资源, 断开连接.
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

// 向ServerAnrServer请求新的probe列表
INT32 RequestProbeListFromServer(FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;

    // 用于提交的URL地址
    stringstream ssUrl;
    // 保存需要post的数据,json格式字符串, 由json模块生成.
    stringstream ssPostData;
    // 保存post的response(查询结果),json格式字符串, 后续交给json模块处理.
    stringstream ssResponceData;
    char * pcJsonData = NULL;

    // 生成 URL
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
    // 生成post数据
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

    // 字符串格式转换.
    string strResponceData = ssResponceData.str();
    pcJsonData = (char *)strResponceData.c_str();

    // 处理response数据
    iRet = ProcessNormalFlowFromServer( pcJsonData, pcFlowManager);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Process Normal Flow From Server failed[%d], pcJsonData: [%s]", iRet, pcJsonData);
        return iRet;
    }
    return iRet;
}


// 向ServerAnrServer请求新的probe列表
INT32 ReportDataToServer(ServerAntAgentCfg_C *pcAgentCfg, stringstream * pstrReportData, string strUrl)
{
    INT32 iRet = AGENT_OK;

    // 用于提交的URL地址
    stringstream ssUrl;
    // 保存post的response(查询结果),json格式字符串, 后续交给json模块处理.
    stringstream ssResponceData;
    const char * pcJsonData = NULL;

    // 生成 URL
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

    // 字符串格式转换.
    string strResponceData = ssResponceData.str();
    pcJsonData = strResponceData.c_str();

    // 处理response数据
    MSG_CLIENT_INFO("Responce [%s]", strResponceData.c_str());

    return iRet;
}

INT32 ReportAgentIPToServer(ServerAntAgentCfg_C * pcAgentCfg)
{
    INT32 iRet = AGENT_OK;

    // 用于提交的URL地址
    stringstream ssUrl;
    // 保存需要post的数据,json格式字符串, 由json模块生成.
    stringstream ssPostData;
    // 保存post的response(查询结果),json格式字符串, 后续交给json模块处理.
    stringstream ssResponceData;
    char * pcJsonData = NULL;

    // 生成 URL
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


    // 生成post数据
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

    // 字符串格式转换.
    string strResponceData = ssResponceData.str();
    pcJsonData = (char *)strResponceData.c_str();

    // 处理response数据
    MSG_CLIENT_INFO("Responce [%s]", strResponceData.c_str());
    return iRet;
}

// 向ServerAnrServer请求新的配置
INT32 RequestConfigFromServer(FlowManager_C* pcFlowManager)
{
    INT32 iRet = AGENT_OK;

    // 用于提交的URL地址
    stringstream ssUrl;
    // 保存需要post的数据,json格式字符串, 由json模块生成.
    stringstream ssPostData;
    // 保存post的response(查询结果),json格式字符串, 后续交给json模块处理.
    stringstream ssResponceData;
    char * pcJsonData = NULL;

    // 生成 URL
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

    // 生成post数据
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

    // 字符串格式转换.
    string strResponceData = ssResponceData.str();
    pcJsonData = (char *)strResponceData.c_str();

    // 处理response数据
    iRet = ProcessServerConfigFlowFromServer( pcJsonData, pcFlowManager);
    if (iRet)
    {
        MSG_CLIENT_ERROR("Process Server config Flow From Server failed[%d]", iRet);
        return iRet;
    }
    return iRet;
}