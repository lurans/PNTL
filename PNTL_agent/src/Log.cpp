
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <sys/time.h>
#include <time.h>
#include <stdarg.h>

#include <sstream>      // C++ stream����
#include <syslog.h>     // д��ϵͳ��־

using namespace std;
#include "Log.h"

#define AGENT_LOG_TMPBUF_SIZE 512                     /* ������Ϣÿһ�����ĳ���,�����ó������ܻ��½� */

// Ԥ��δ������
#define AGENT_LOG_MODULE_LOCK(uiModule, uiLogType) AGENT_OK
#define AGENT_LOG_MODULE_UNLOCK(uiModule, uiLogType) AGENT_OK

// Logģ�����, δ����չ����.
typedef struct tagLogConfig
{

    /* ��־�Ƿ��¼��syslog */
    // syslog��Linuxϵͳ�б�׼��־����ģ��, ����ϵͳ��־�������ύ��syslog.
    // syslog�Ĳ�ͬpriority��facility��д�벻ͬ����־�ļ�, ��/etc/syslog-ng/syslog-ng.conf �ļ�����
    // sles 11.3 Ĭ������»ὫServerAntAgent��־д��/var/log/messages, warning/error��־�����д��/var/log/warn
    // OS���syslog����־����ѹ��ת��,���õ�����־��������.
    // ServerAntAgent����ʱ�����������info��־, ��������ʱ��������־����.
    UINT32   uiLogToSyslog;

    // �����ʹ��syslog, ���Խ�ServerAntAgent��־д�뵥������־�ļ�
    // ���ڷ�root�û��޷�����־ֱ��д��/var/logĿ¼, Ҳ�޷�������־ת��,������־�ļ����ŵ���ִ�г���ĵ�ǰĿ¼.
    string strLogFileNameNormal;    // ��¼������־
    string strLogFileNameWarning;   // ��¼warning��error��־
    string strLogFileNameError;     // ��¼error��־
    string strLogFileNameLossPacket;     // ��¼������־
    string strLogFileNameLatency;     // ��¼��ʱ��־

    /* ��־�Ƿ��ӡ���ն� */
    UINT32   uiLogToTty;
    UINT32   uiDebug;      /* ֵ�ֱ�Ϊ0,1,2,4  */

} LogConfig_S;

static LogConfig_S g_stLogConfig =
{
    AGENT_FALSE,
    "./logs/ServerAntAgent.log",
    "./logs/ServerAntAgentWarning.log",
    "./logs/ServerAntAgentError.log",
    "./logs/ServerAntAgentLossPacket.log",
    "./logs/ServerAntAgentLatency.log",
    AGENT_FALSE,
    4
};

#define AGENT_LOG_ERROR_PRINT(...) \
    do  \
    {   \
        INT32 _iRet_ = 0;   \
            _iRet_ = printf(__VA_ARGS__);    \
            if (0 > _iRet_)    \
            {   \
                (void)printf("Printf Error[%d]",_iRet_);\
                return AGENT_E_ERROR;   \
            }   \
    }while(0)

#define AGENT_LOG_CHECK_PARAM(ulModule, ulLogType)   \
    do  \
    {   \
        if ((ulModule) >= AGENT_MODULE_MAX)  \
        {   \
            AGENT_LOG_ERROR_PRINT("[%s]: Invalid Module Number:Mod[%u],Log[%u]\n", __FUNCTION__, (ulModule), (ulLogType));   \
            return AGENT_E_PARA;  \
        }   \
        if ((ulLogType) >= AGENT_LOG_TYPE_MAX)   \
        {   \
            AGENT_LOG_ERROR_PRINT("[%s]: Invalid LogType:Mod[%u],Log[%u]\n", __FUNCTION__, (ulModule), (ulLogType)); \
            return AGENT_E_PARA;  \
        }   \
    }while(0)

// ������־�Ƿ��ӡ���ն�, ��־��¼��ϵͳ��־���Ƕ������־·��
INT32 SetNewLogMode(AgentLogMode_E eNewLogMode)
{
    // ��ͨ����
    if(AGENT_LOG_MODE_NORMAL == eNewLogMode)
    {
        // ��־��¼���ļ�
        g_stLogConfig.uiLogToSyslog = AGENT_FALSE;
        // ��־��ӡ����׼���
        g_stLogConfig.uiLogToTty    = AGENT_TRUE;
    }
    // �ػ�����ģʽ
    else if(AGENT_LOG_MODE_DAEMON == eNewLogMode)
    {
        // ��־��¼��ϵͳ��־
        //g_stLogConfig.uiLogToSyslog = AGENT_TRUE;
        // �ն˲�ϣ����־��¼��ϵͳ��־, ��������
        g_stLogConfig.uiLogToSyslog = AGENT_FALSE;

        // ��־����ӡ����׼���
        g_stLogConfig.uiLogToTty    = AGENT_FALSE;
    }
    else
    {
        AGENT_LOG_ERROR_PRINT("[%s]: Invalid LogMode:[%u]\n", __FUNCTION__, eNewLogMode);
        return AGENT_E_PARA;
    }
    return AGENT_OK;
}

// ������־��¼Ŀ¼, ����־����¼��ϵͳ��־ʱ��Ч
INT32 SetNewLogDir(string strNewDirPath)
{
    // ��ǰ��־��¼��ϵͳ��־, �޸���־Ŀ¼����Ч
    if(AGENT_TRUE == g_stLogConfig.uiLogToSyslog)
    {
        AGENT_LOG_ERROR_PRINT("[%s]: Set log dir to [%s] when enable LogToSyslog\n", __FUNCTION__, strNewDirPath.c_str());
    }

    g_stLogConfig.strLogFileNameNormal = strNewDirPath + "/ServerAntAgent.log";
    g_stLogConfig.strLogFileNameWarning= strNewDirPath + "/ServerAntAgentWarning.log";
    g_stLogConfig.strLogFileNameError  = strNewDirPath + "/ServerAntAgentError.log";
    g_stLogConfig.strLogFileNameLossPacket  = strNewDirPath + "/ServerAntAgentLossPacket.log";
    g_stLogConfig.strLogFileNameLatency  = strNewDirPath + "/ServerAntAgentLatency.log";

    return AGENT_OK;
}

// ������־��¼Ŀ¼, ����־����¼��ϵͳ��־ʱ��Ч
string GetLossLogFilePath()
{
    return g_stLogConfig.strLogFileNameLossPacket;
}

string GetLatencyLogFilePath()
{
    return g_stLogConfig.strLogFileNameLatency;
}

// ��ȡ��ǰʱ��, ���ڼ�¼��־
void GetPrintTime(char *timestr)
{
    struct timeval tv;
    time_t t;
    struct tm *tm;

    gettimeofday(&tv, NULL);
    t = (time_t)tv.tv_sec;
    tm = localtime(&t);
    if(NULL == tm)
    {
        return;
    }

    sprintf((char *)timestr, "%04u-%02u-%02u %02u:%02u:%02u",
            tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday, tm->tm_hour,
            tm->tm_min, tm->tm_sec);

    /*
        sprintf((char *)timestr, "[%04u-%02u-%02u %02u:%02u:%02u.%03u]",
                tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday, tm->tm_hour,
                tm->tm_min, tm->tm_sec, (UINT32)tv.tv_usec/1000);
    */

}


// ���ַ�����ʽ���д���,ȷ����־�ļ��и�ʽͳһ, ulBufSizeΪbuffer�ܴ�С, ����������
void AgentLogPreParser(char * pcStr, UINT32 ulBufSize)
{
    UINT32 uiStrLen = 0;
    UINT32 uiPreStrState = 0;

    /* �����ַ�����ͷ�Ŀո� */
    for (uiStrLen = 0; uiStrLen < (ulBufSize - 1); uiStrLen++)
    {
        if (' '  != pcStr[uiStrLen])
        {
            break;
        }
    }

    /*
       ���û���־�ַ�����ͷ��������Ż��ɿո�,������[%s]�е��������
       �����������
    */
    for (; uiStrLen < (ulBufSize - 1); uiStrLen++)
    {
        /* ������־ģ����ӵĺ���������Ϣ */
        if ('[' == pcStr[uiStrLen])
        {
            uiPreStrState = AGENT_TRUE;
            continue;
        }
        if (']' == pcStr[uiStrLen])
        {
            uiPreStrState = AGENT_FALSE;
            continue;
        }
        if (AGENT_TRUE == uiPreStrState)
        {
            continue;
        }
        /* �����û���־��ͷ�Ļ��з��������ַ��� */
        if (   ('\r' == pcStr[uiStrLen])
                || ('\n' == pcStr[uiStrLen])
                || (' '  == pcStr[uiStrLen]))
        {
            pcStr[uiStrLen] = ' ';
        }
        else
        {
            break;
        }
    }

    /* �����β�Ļ��з�, ���з�����ͳһ��� */
    uiStrLen = strlen(pcStr);
    if ('\n' == pcStr[uiStrLen - 1])
    {
        pcStr[uiStrLen - 1] = ' ';
    }

    return;
}

INT32 WriteToLogFile(const char *pcFileName, const char *pcMsg )
{
    // д�뵥������־�ļ�
    FILE  * pstFile         = NULL;
    UINT32 uiLength   = 0;
    UINT32 uiStrLen   = 0;

    uiStrLen = sal_strlen(pcMsg);

    pstFile = fopen(pcFileName, "a+");
    if(NULL == pstFile)
    {
        AGENT_LOG_ERROR_PRINT("[%s][%u]: Log can't open file[%s]: %s [%d]\n", __FUNCTION__,  __LINE__, pcFileName, strerror(errno), errno);
        return AGENT_E_ERROR;
    }
    /* ��logд���ļ� */
    uiLength = fwrite(pcMsg, sizeof(char), uiStrLen, pstFile);
    fflush(pstFile);
    fclose(pstFile);

    return AGENT_OK;
}

INT32 AgentLogSaveToFile(UINT32 ulLogType, const char *pcMsg )
{
    if (AGENT_TRUE == g_stLogConfig.uiLogToSyslog)
    {
        // д��syslog
        INT32 iPriority = LOG_INFO;
        switch (ulLogType)
        {
        case AGENT_LOG_TYPE_INFO:
        case AGENT_LOG_TYPE_LOSS_PACKET:
        case AGENT_LOG_TYPE_LATENCY:
            iPriority = LOG_INFO;
            break;

        case AGENT_LOG_TYPE_WARNING:
            iPriority = LOG_WARNING;
            break;

        case AGENT_LOG_TYPE_ERROR:
            iPriority = LOG_ERR;
            break;

        default :
            AGENT_LOG_ERROR_PRINT("[%s][%u]: Log can't support this logtype[%u] \n", __FUNCTION__,  __LINE__, ulLogType);
            return AGENT_E_PARA;
        }

        openlog("ServerAntAgent", LOG_CONS|LOG_PID, LOG_DAEMON);
        syslog(iPriority, pcMsg);
        closelog();
    }
    else
    {
        // д�뵥������־�ļ�
        const char *pcFileName = NULL;

        switch (ulLogType)
        {
        case AGENT_LOG_TYPE_INFO:
            pcFileName = g_stLogConfig.strLogFileNameNormal.c_str();
            break;

        case AGENT_LOG_TYPE_WARNING:
            pcFileName = g_stLogConfig.strLogFileNameWarning.c_str();
            break;

        case AGENT_LOG_TYPE_ERROR:
            pcFileName = g_stLogConfig.strLogFileNameError.c_str();
            break;

        case AGENT_LOG_TYPE_LOSS_PACKET:
            pcFileName = g_stLogConfig.strLogFileNameLossPacket.c_str();
            break;

        case AGENT_LOG_TYPE_LATENCY:
            pcFileName = g_stLogConfig.strLogFileNameLatency.c_str();
            break;

        default :
            AGENT_LOG_ERROR_PRINT("[%s][%u]: Log can't support this logtype[%u] \n", __FUNCTION__,  __LINE__, ulLogType);
            return AGENT_E_PARA;
        }

        WriteToLogFile(pcFileName, pcMsg);

    }
}

INT32 AgentLogPrintf(AgentModule_E eModule, AgentLogType_E eLogType, const char *szFormat, ...)
{

    va_list         arg;                                        // ���ڴ���䳤����
    char            acStackLogBuffer[AGENT_LOG_TMPBUF_SIZE];    // ����ʹ��ջ�е�buffer, С����
    char *          pacHeapLogBuffer = NULL;                    // �������ַ������ȳ���AGENT_LOG_TMPBUF_SIZEʱ, �Ӷ�������ռ�
    UINT32    uiStrLen = 0;                               // �����ַ�������
    stringstream    ssLogBuffer;                                // ����ƴ���ַ���
    char            acCurTime[32]   = {0};                      // ����ʱ���
    INT32       iRet = AGENT_E_NOT_FOUND;

    /* ��μ��,���ִ����,��¼�쳣,����*/
    AGENT_LOG_CHECK_PARAM(eModule, eLogType);

    if (NULL == szFormat)
    {
        AGENT_LOG_ERROR_PRINT("[%s]:Module[%u] Log[%u] NULL Input\n", __FUNCTION__, eModule, eLogType);
        return AGENT_E_PARA;
    }

    switch (g_stLogConfig.uiDebug)
    {
    case 0:
        break;

    case 1:
        if (AGENT_LOG_TYPE_ERROR == eLogType)
        {
            iRet  = AGENT_OK;
        }
        break;

    case 2:
        if (AGENT_LOG_TYPE_WARNING == eLogType || AGENT_LOG_TYPE_ERROR == eLogType)
        {
            iRet  = AGENT_OK;
        }
        break;

    default :
        iRet =  AGENT_OK;
        break;
    }

    if (iRet !=  AGENT_OK)
    {
        return iRet;
    }

    /* ����ʱ���ǩ */
    sal_memset(acStackLogBuffer, 0, sizeof(acStackLogBuffer));
    GetPrintTime(acCurTime);

    // ���Խ������ַ�������acStackLogBuffer��, �����ض������������ڴ�.
    va_start(arg, szFormat);
    uiStrLen = vsnprintf(acStackLogBuffer, sizeof(acStackLogBuffer), szFormat, arg);
    va_end(arg);

    // �����ַ���̫�����ض�, ���������ڴ�
    va_start(arg, szFormat);
    if (AGENT_LOG_TMPBUF_SIZE <= uiStrLen)
    {
        // ��ֱ��ʹ��string��������Ϊstring�޷�ʶ��printf���͵�ͨ���,��%s,%d��.
        pacHeapLogBuffer = new char[uiStrLen + 2];
        // �ڴ�����ɹ�, ʹ�����ڴ滺����־��Ϣ
        if (NULL != pacHeapLogBuffer)
        {
            sal_memset(pacHeapLogBuffer, 0, (uiStrLen + 2));
            uiStrLen = vsnprintf(pacHeapLogBuffer, (uiStrLen + 1), szFormat, arg);
        }
        else
        {
            printf("No enough heap memory for log, msg will be truncated");
        }
    }

    va_end(arg);

    if (NULL == pacHeapLogBuffer)
    {
        /* ���û��ַ����еĻ��з�ͳһ���д���,�����з� */
        AgentLogPreParser(acStackLogBuffer, sizeof(acStackLogBuffer));

        if (AGENT_MODULE_SAVE_REPORTDATA != eModule)
        {
            /* ����ʱ��� */
            ssLogBuffer << acCurTime << " " << acStackLogBuffer << endl;
        }
        else
        {
            ssLogBuffer << acStackLogBuffer << endl;
        }
    }
    else
    {
        /* ���û��ַ����еĻ��з�ͳһ���д���,�����з� */
        AgentLogPreParser(pacHeapLogBuffer, uiStrLen + 2);

        if (AGENT_MODULE_SAVE_REPORTDATA != eModule)
        {
            /* ����ʱ��� */
            ssLogBuffer << acCurTime << " " << pacHeapLogBuffer << endl;
        }
        else
        {
            /* ���涪����Ϣ����Ҫʱ�����Ϣ */
            ssLogBuffer << pacHeapLogBuffer << endl;
        }

        /* �ͷ��ڴ� */
        delete [] pacHeapLogBuffer;
        pacHeapLogBuffer = NULL;
    }

    if (AGENT_TRUE == g_stLogConfig.uiLogToTty)
        printf(ssLogBuffer.str().c_str());
    AgentLogSaveToFile(eLogType, ssLogBuffer.str().c_str());

    return AGENT_OK;
}

