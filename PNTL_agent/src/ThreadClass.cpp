
#include <errno.h>

using namespace std;

#include "Sal.h"
#include "AgentCommon.h"
#include "Log.h"

#include "ThreadClass.h"

enum
{
    THREAD_STATE_STOPED  = 0,  // ����δ�������Ѿ�ֹͣ
    THREAD_STATE_WORKING,      // �������ڹ���
    THREAD_STATE_MAX
};

// ��ֹ����ʱ, �ȴ����������˳��ĳ��Դ���.
#define THREAD_STOP_COUNTER             10
// Ĭ������,100ms.
#define THREAD_DEFAULT_UPDATE_INTERVAL  100000
// ��С����10ms, һ��tick
#define THREAD_UPDATE_MIN               10000


void * ThreadFun(void * p)
{
    INT32 iRet = AGENT_OK;
    ThreadClass_C * pcThread =  (ThreadClass_C *)p;    // ��������Ķ���.

    pcThread->ThreadUpdateState(THREAD_STATE_WORKING);
    if(iRet)
    {
        THREAD_CLASS_ERROR("Thread Update State To WORKING failed[%d]", iRet);
        pthread_exit(NULL);

    }
    //THREAD_CLASS_INFO("Thread start working.");

    iRet = pcThread->ThreadHandler();
    if(iRet)
    {
        THREAD_CLASS_WARNING("Thread Handler Return Faile[%d]", iRet);
    }

    iRet = pcThread->ThreadUpdateState(THREAD_STATE_STOPED);
    if(iRet)
    {
        THREAD_CLASS_WARNING("Thread Update State To STOP failed[%d]", iRet);
    }
    THREAD_CLASS_INFO("Thread exiting.");
}


// ���캯��, ���Ĭ��ֵ.
ThreadClass_C::ThreadClass_C()
{
    // THREAD_CLASS_INFO("Creat a new ThreadClass.");

    uiThreadUpdateInterval = THREAD_DEFAULT_UPDATE_INTERVAL; // 100ms
    uiThreadState = THREAD_STATE_WORKING;
    ThreadFd = 0;
}

// ��������, �ͷű�Ҫ��Դ.
ThreadClass_C::~ThreadClass_C()
{
    // THREAD_CLASS_INFO("Destroy an old ThreadClass.");

    StopThread();           // ֹͣ����
}


// �趨������
INT32 ThreadClass_C::SetNewInterval(UINT32 uiNewInterval)
{
    INT32 iRet = AGENT_OK;

    //THREAD_CLASS_INFO("Set Thread Update Interval to [%d] us", uiNewInterval);
    uiThreadUpdateInterval = uiNewInterval;

    return iRet;
}

// ��ѯ��ǰ������
UINT32 ThreadClass_C::GetCurrentInterval()
{
    return uiThreadUpdateInterval;
}

// ��������.
INT32 ThreadClass_C::StartThread()
{
    INT32 iRet = AGENT_OK;

    if (ThreadFd)   // ��ֹͣ����.
        StopThread();

    //THREAD_CLASS_INFO("Start a Thread with ThreadUpdateInterval [%d]us", uiThreadUpdateInterval);

    iRet = ThreadUpdateState(THREAD_STATE_STOPED); // �ָ�����Ĭ��״̬
    if(iRet)
    {
        THREAD_CLASS_ERROR("Thread Update State failed[%d]", iRet);
        ThreadFd = 0;
        return iRet;
    }

    iRet = PreStartHandler();
    if(iRet)
    {
        THREAD_CLASS_ERROR("Pre Start Handler failed[%d]", iRet);
        ThreadFd = 0;
        return AGENT_E_HANDLER;
    }

    iRet = pthread_create(&ThreadFd, NULL, ThreadFun, this);  //��������
    if(iRet)    // ��������ʧ��
    {
        THREAD_CLASS_ERROR("Create Thread failed[%d]: %s [%d]", iRet, strerror(errno), errno);
        ThreadFd = 0;
        return AGENT_E_THREAD;
    }
    return iRet;
}

// ֹͣ����.
INT32 ThreadClass_C::StopThread()
{
    UINT32 uiInterval = GetCurrentInterval()/2 + THREAD_UPDATE_MIN;  // ����������
    UINT32 uiStopCounter = THREAD_STOP_COUNTER;
    INT32 iRet = AGENT_OK;

    if (ThreadFd)
    {
        THREAD_CLASS_INFO("Stop Thread. Waiting handler's exit. Check Every [%d]us", uiInterval);

        iRet = PreStopHandler();// ֪ͨHandler�����˳�.
        if(iRet)
        {
            THREAD_CLASS_WARNING("Pre Stop Handler failed[%d]", iRet);
        }

        do
        {
            sal_usleep(uiInterval);
            uiStopCounter --;
        }
        while ( uiThreadState && uiStopCounter);      // �ȴ���������ֹͣ

        if (0 == uiStopCounter)  //����û������ֹͣ, ����ǿ���˳�.
        {
            THREAD_CLASS_WARNING("Stop Thread failed after [%d]us, Force Stop ...", uiInterval * THREAD_STOP_COUNTER);
            iRet = pthread_cancel(ThreadFd);
            if (iRet)
            {
                THREAD_CLASS_ERROR("Force Stop Thread failed[%d]: %s [%d]", iRet, strerror(errno), errno);
                iRet = AGENT_E_ERROR;
            }

            iRet = ThreadUpdateState(THREAD_STATE_STOPED); // �ָ�����Ĭ��״̬
            if(iRet)
            {
                THREAD_CLASS_ERROR("Thread Update State failed[%d]", iRet);
                ThreadFd = 0;
            }
            THREAD_CLASS_WARNING("Force Stop Thread Sucess");
        }
        else
        {
            THREAD_CLASS_INFO("Stop Thread Sucess. Counter cost [%d]", THREAD_STOP_COUNTER - uiStopCounter);
        }
    }
    ThreadFd = 0;
    return iRet;
}

// ˢ������״̬
INT32 ThreadClass_C::ThreadUpdateState(UINT32 uiNewState)
{
    if (THREAD_STATE_MAX <= uiNewState)
    {
        THREAD_CLASS_ERROR("Thread update to State [%d] failed",
                           uiNewState);
        return AGENT_E_PARA;
    }
    uiThreadState = uiNewState;

    return AGENT_OK;
}

// Thread�ص�����.
// PreStopHandler()ִ�к�, ThreadHandler()��Ҫ��GetCurrentInterval() us�������˳�.
INT32 ThreadClass_C::ThreadHandler()
{
    while (GetCurrentInterval())
    {
        THREAD_CLASS_WARNING("Thread Handler use default func");
        sal_usleep(GetCurrentInterval());
    }
    return AGENT_OK;
}

// Thread��������, ֪ͨThreadHandler����׼��.
INT32 ThreadClass_C::PreStartHandler()
{
    THREAD_CLASS_WARNING("Pre Start Handler use default func");
}

// Thread����ֹͣ, ֪ͨThreadHandler�����˳�.
INT32 ThreadClass_C::PreStopHandler()
{
    THREAD_CLASS_WARNING("Pre Stop Handler use default func");
    SetNewInterval(0);
}



