
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

    /* ��־�Ƿ��ӡ���ն� */
    UINT32   uiLogToTty;

} LogConfig_S;

static LogConfig_S g_stLogConfig =
{
    AGENT_FALSE,
    "./logs/ServerAntAgent.log",
    "./logs/ServerAntAgentWarning.log",
    "./logs/ServerAntAgentError.log",
    AGENT_FALSE
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
    return AGENT_OK;
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

    sprintf((char *)timestr, "[%04u-%02u-%02u %02u:%02u:%02u.%03u]",
            tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday, tm->tm_hour,
            tm->tm_min, tm->tm_sec, (UINT32)tv.tv_usec/1000);
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

INT32 AgentLogSaveToFile( UINT32 ulModule, UINT32 ulLogType, const char *pcMsg )
{
    if (AGENT_TRUE == g_stLogConfig.uiLogToSyslog)
    {
        // д��syslog
        INT32 iPriority = LOG_INFO;
        switch (ulLogType)
        {
            case AGENT_LOG_TYPE_INFO:
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
        FILE  * pstFile         = NULL;
        UINT32 uiLength   = 0;
        UINT32 uiStrLen   = 0;

        uiStrLen = sal_strlen(pcMsg);

        pstFile = fopen(g_stLogConfig.strLogFileNameNormal.c_str(), "a+");
        if(NULL == pstFile)
        {
            AGENT_LOG_ERROR_PRINT("[%s][%u]: Log can't open file[%s]: %s [%d]\n", __FUNCTION__,  __LINE__,
                                  g_stLogConfig.strLogFileNameNormal.c_str(), strerror(errno), errno);
            return AGENT_E_ERROR;
        }
        /* ��logд���ļ� */
        uiLength = fwrite(pcMsg, sizeof(char), uiStrLen, pstFile);
        fflush(pstFile);
        fclose(pstFile);
        pstFile = NULL;

        if(   (AGENT_LOG_TYPE_WARNING == ulLogType)
                ||(AGENT_LOG_TYPE_ERROR == ulLogType))
        {
            pstFile = fopen(g_stLogConfig.strLogFileNameWarning.c_str(), "a+");
            if(NULL == pstFile)
            {
                AGENT_LOG_ERROR_PRINT("[%s][%u]: Log can't open file[%s]: %s [%d]\n", __FUNCTION__,  __LINE__,
                                      g_stLogConfig.strLogFileNameWarning.c_str(), strerror(errno), errno);
                return AGENT_E_ERROR;
            }
            /* ��logд���ļ� */
            uiLength = fwrite(pcMsg, sizeof(char), uiStrLen, pstFile);

            fflush(pstFile);
            fclose(pstFile);
            pstFile = NULL;
        }

        if( AGENT_LOG_TYPE_ERROR == ulLogType )
        {
            pstFile = fopen(g_stLogConfig.strLogFileNameError.c_str(), "a+");
            if(NULL == pstFile)
            {
                AGENT_LOG_ERROR_PRINT("[%s][%u]: Log can't open file[%s]: %s [%d]\n", __FUNCTION__,  __LINE__,
                                      g_stLogConfig.strLogFileNameError.c_str(), strerror(errno), errno);
                return AGENT_E_ERROR;
            }
            /* ��logд���ļ� */
            uiLength = fwrite(pcMsg, sizeof(char), uiStrLen, pstFile);

            fflush(pstFile);
            fclose(pstFile);
            pstFile = NULL;
        }
    }
}
#if 0
INT32 AgentLogPrintf(UINT32 ulModule, UINT32 ulLogType, const char *szFormat, ...)
{
    va_list         arg;
    char            acStrTmpstSendMsg[AGENT_LOG_TMPBUF_SIZE] = {0};
    char            acCurTime[32]   = {0};
    UINT32    uiStrLen = 0;

    UINT32    uiRet = AGENT_OK;

    /* ��μ��,���ִ����,��¼�쳣,����*/
    AGENT_LOG_CHECK_PARAM(ulModule, ulLogType);

    if (NULL == szFormat)
    {
        AGENT_LOG_ERROR_PRINT("[%s]:Module[%u] Log[%u] NULL Input\n", __FUNCTION__, ulModule, ulLogType);
        return AGENT_E_PARA;
    }

    /* ��¼ʱ���ǩ */
    sal_memset(acStrTmpstSendMsg, 0, sizeof(acStrTmpstSendMsg));
    GetPrintTime(acCurTime);

    /* ����ʱ��� */
    snprintf(acStrTmpstSendMsg, sizeof(acStrTmpstSendMsg), "%s ", acCurTime);


    /* ��¼ʱ���ǩ�Ľ���λ�� */
    uiStrLen = strlen(acStrTmpstSendMsg);

    va_start(arg, szFormat);
    vsnprintf((acStrTmpstSendMsg + strlen(acStrTmpstSendMsg)), (sizeof(acStrTmpstSendMsg) - strlen(acStrTmpstSendMsg) - 2),
              szFormat, arg);
    va_end(arg);

    /* ���û��ַ����еĻ��з�ͳһ���д���,������ӵ�ʱ���ǩ */
    AgentLogPreParser(&acStrTmpstSendMsg[uiStrLen], sizeof(acStrTmpstSendMsg));

    // �������Ȼ�����������SAL��ʵ�ֵ�, ����Ƕ��.
    if(AGENT_MODULE_SAL != ulModule)
    {
        /* ��ס��Ӧģ���log��Դ */
        AGENT_LOG_MODULE_LOCK(ulModule, ulLogType);
        printf(acStrTmpstSendMsg);
        AgentLogSaveToFile(ulModule, ulLogType, acStrTmpstSendMsg);
        AGENT_LOG_MODULE_UNLOCK(ulModule, ulLogType);
    }
    else
    {
        printf(acStrTmpstSendMsg);
        AgentLogSaveToFile(ulModule, ulLogType, acStrTmpstSendMsg);
    }

    return AGENT_OK;
}
#else
INT32 AgentLogPrintf(AgentModule_E eModule, AgentLogType_E eLogType, const char *szFormat, ...)
{

    va_list         arg;                                        // ���ڴ���䳤����
    char            acStackLogBuffer[AGENT_LOG_TMPBUF_SIZE];    // ����ʹ��ջ�е�buffer, С����
    char *          pacHeapLogBuffer = NULL;                    // �������ַ������ȳ���AGENT_LOG_TMPBUF_SIZEʱ, �Ӷ�������ռ�
    UINT32    uiStrLen = 0;                               // �����ַ�������
    stringstream    ssLogBuffer;                                // ����ƴ���ַ���
    char            acCurTime[32]   = {0};                      // ����ʱ���

    /* ��μ��,���ִ����,��¼�쳣,����*/
    AGENT_LOG_CHECK_PARAM(eModule, eLogType);

    if (NULL == szFormat)
    {
        AGENT_LOG_ERROR_PRINT("[%s]:Module[%u] Log[%u] NULL Input\n", __FUNCTION__, eModule, eLogType);
        return AGENT_E_PARA;
    }

    /* ����ʱ���ǩ */
    sal_memset(acStackLogBuffer, 0, sizeof(acStackLogBuffer));
    GetPrintTime(acCurTime);

    // ���Խ������ַ�������acStackLogBuffer��, �����ض������������ڴ�.
    va_start(arg, szFormat);
    uiStrLen = vsnprintf(acStackLogBuffer, sizeof(acStackLogBuffer),
                         szFormat, arg);
    va_end(arg);

    // �����ַ���̫�����ض�, ���������ڴ�
    va_start(arg, szFormat);
    if (AGENT_LOG_TMPBUF_SIZE <= uiStrLen)
    {
        // ��ֱ��ʹ��string��������Ϊstring�޷�ʶ��printf���͵�ͨ���,��%s,%d��.
        pacHeapLogBuffer = new char[uiStrLen + 2];
        // �ڴ�����ɹ�, ʹ�����ڴ滺����־��Ϣ
        if (pacHeapLogBuffer)
        {
            sal_memset(pacHeapLogBuffer, 0, (uiStrLen + 2));
            uiStrLen = vsnprintf(pacHeapLogBuffer, (uiStrLen + 1),
                                 szFormat, arg);
        }
        else
            printf("No enough heap memory for log, msg will be truncated");
    }
    va_end(arg);

    if (NULL == pacHeapLogBuffer)
    {
        /* ���û��ַ����еĻ��з�ͳһ���д���,�����з� */
        AgentLogPreParser(acStackLogBuffer, sizeof(acStackLogBuffer));
        /* ����ʱ��� */
        ssLogBuffer << acCurTime << " " << acStackLogBuffer << endl;
    }
    else
    {
        /* ���û��ַ����еĻ��з�ͳһ���д���,�����з� */
        AgentLogPreParser(pacHeapLogBuffer, uiStrLen + 2);
        /* ����ʱ��� */
        ssLogBuffer << acCurTime << " " << pacHeapLogBuffer << endl;

        /* �ͷ��ڴ� */
        delete [] pacHeapLogBuffer;
        pacHeapLogBuffer = NULL;
    }

    // �������Ȼ�����������SAL��ʵ�ֵ�, ����Ƕ��.
    if(AGENT_MODULE_SAL != eModule)
    {
        /* ��ס��Ӧģ���log��Դ */
        AGENT_LOG_MODULE_LOCK(eModule, eLogType);
        if (AGENT_TRUE == g_stLogConfig.uiLogToTty)
            printf(ssLogBuffer.str().c_str());
        AgentLogSaveToFile(eModule, eLogType, ssLogBuffer.str().c_str());
        AGENT_LOG_MODULE_UNLOCK(eModule, eLogType);
    }
    else
    {
        if (AGENT_TRUE == g_stLogConfig.uiLogToTty)
            printf(ssLogBuffer.str().c_str());
        AgentLogSaveToFile(eModule, eLogType, ssLogBuffer.str().c_str());
    }

    return AGENT_OK;
}
#endif

