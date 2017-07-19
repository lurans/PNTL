
//#include <Python.h>
#include <fstream>

using namespace std;

#include "Sal.h"
#include "Log.h"
#include "AgentJsonAPI.h"
#include "GetLocalCfg.h"
#include "ServerAntAgentCfg.h"
#include "MessagePlatformClient.h"

#define SERVER_ANT_CFG_FILE_NAME "ServerAntAgent.cfg"
#define SERVER_ANT_CFG_FILE_PATH "/etc/ServerAntAgent/"


string GetJsonDataFromFile(string sFileName, string sFilePath)
{

    INT32  iRet = AGENT_OK;
    stringstream ssCfgFileName;
    ifstream ifsAgentCfg;

    ssCfgFileName << sFileName;

    ifsAgentCfg.open(ssCfgFileName.str().c_str());
    if ( ifsAgentCfg.fail() )
    {
        INIT_INFO("No cfg file[%s] in current dir, trying [%s] ...", sFileName.c_str(), sFilePath.c_str());

        ssCfgFileName.clear();
        ssCfgFileName.str("");
        ssCfgFileName << sFilePath << sFileName;
        ifsAgentCfg.open(ssCfgFileName.str().c_str());
        if ( ifsAgentCfg.fail() )
        {
            INIT_ERROR("Can't open cfg file[%s]", ssCfgFileName.str().c_str());
            return "";
        }
    }

    INIT_INFO("Using cfg file[%s]", ssCfgFileName.str().c_str());

    string strCfgJsonData((istreambuf_iterator<char>(ifsAgentCfg)),  (istreambuf_iterator<char>()));

    ifsAgentCfg.close();

    return strCfgJsonData;

}

INT32 GetLocalCfg(ServerAntAgentCfg_C * pcCfg)
{

    INT32  iRet = AGENT_OK;
    string strCfgJsonData;
    stringstream ssCfgFileName;

    strCfgJsonData = GetJsonDataFromFile(SERVER_ANT_CFG_FILE_NAME, SERVER_ANT_CFG_FILE_PATH);
    if ("" == strCfgJsonData)
    {
        return AGENT_E_ERROR;
    }

    iRet = ParserLocalCfg(strCfgJsonData.c_str(), pcCfg);
    if (iRet)
    {
        INIT_ERROR("ParserLocalCfg failed[%d]", iRet);
        return AGENT_E_ERROR;
    }

    return AGENT_OK;
}

void RecoverLossPktData(ServerAntAgentCfg_C *pcAgentCfg)
{

    INT32  iRet = AGENT_OK;
    string sFilePath;
    string content = "";
    ifstream fileStream;
    string line = "";
    stringstream  ssReportData; // 車?車迆谷迆3谷json??那?谷?㊣“那y?Y

    sFilePath = GetLossLogFilePath();

    fileStream.open(sFilePath.c_str(), ios::in);
    if (fileStream.fail())
    {
        return;
    }

    while (getline(fileStream, line))
    {
        ssReportData.clear();
        ssReportData.str("");
        ssReportData << line;

        HTTP_DAEMON_INFO("RecoverLossPktData Read file content is:  [%s]", line.c_str());

        iRet = ReportDataToServer(pcAgentCfg, &ssReportData, REPORT_LOSSPKT_URL);
        if (AGENT_OK != iRet)
        {
                break;
        }
        sal_usleep(1);
    }
    fileStream.close();

    return;
}





