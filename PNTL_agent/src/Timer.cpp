
#include <errno.h>

using namespace std;

#include "Log.h"
#include "Timer.h"

#define CFG_LOCK() \
        if (stTimerCfgLock) \
            sal_mutex_take(stTimerCfgLock, sal_mutex_FOREVER)
            
#define CFG_UNLOCK() \
        if (stTimerCfgLock) \
            sal_mutex_give(stTimerCfgLock)


// ���캯��, ���Ĭ��ֵ.
Timer_C::Timer_C()
{
    //TIMER_INFO("Creat a new timer.");

    stTimerCfgLock = NULL;
    uiPerid = 0;    
    vSemList.clear(); //���֪ͨ�ź�������.
}

// ��������, �ͷű�Ҫ��Դ.
Timer_C::~Timer_C()
{
    //TIMER_INFO("Destroy an old Timer.");

    StopThread();       // ֹͣ����
    
    vSemList.clear();   // ���֪ͨ�ź�������.

    if (stTimerCfgLock) // �ͷŻ�����
        sal_mutex_destroy(stTimerCfgLock);
    
    stTimerCfgLock = NULL;
}

// ������������
INT32 Timer_C::ThreadHandler()
{

    TIMER_INFO("Timer Handler Thread start working.");
    
    while (uiPerid)
    {
        ThreadNotify();
        sal_usleep(uiPerid);
    }
    TIMER_INFO("Timer Handler Thread exiting.");
    
    return AGENT_OK;
}

// Thread��������, ֪ͨThreadHandler����׼��.
INT32 Timer_C::PreStartHandler()
{
    return AGENT_OK;
}

// Thread����ֹͣ, ֪ͨThreadHandler�����˳�.
INT32 Timer_C::PreStopHandler()
{
    CFG_LOCK();
    uiPerid = 0;        
    CFG_UNLOCK();
    
    return AGENT_OK;
}

// ���������ɶ����ʼ��
INT32 Timer_C::Init(UINT32 uiNewPeriod)
{
    INT32 iRet = AGENT_OK;

    if (0 == uiNewPeriod)
    {
        TIMER_ERROR("Set New Period to [%d]us failed", 
            uiNewPeriod);
        return AGENT_E_PARA;
    }

    if(stTimerCfgLock)
    {
        TIMER_ERROR("Do not reinit this timer. [%d] us.", uiNewPeriod);
        return AGENT_E_PARA;
    }
    
    TIMER_INFO("Init Timer: [%d] us.", uiNewPeriod);
    
    stTimerCfgLock = sal_mutex_create("TimerCfgLock");
    if (NULL == stTimerCfgLock )
    {
        TIMER_ERROR("Timer Init Failed: [%d] us.", uiNewPeriod);
        return AGENT_E_MEMORY;
    }
    
    iRet = SetPeriod(uiNewPeriod);
    if (iRet)
    {
        TIMER_ERROR("Timer Set Period Failed: [%d] us.", uiNewPeriod);
        return iRet;
    }    
    
    return iRet;
}


// Timer ֪ͨ����
INT32 Timer_C::ThreadNotify()
{
    vector<sal_sem_t>::iterator psem;
    CFG_LOCK();
    for(psem = vSemList.begin(); psem != vSemList.end(); psem++)
    {
        if ( NULL != *psem )
        {
            sal_sem_give(*psem);
        }
    }
    CFG_UNLOCK();
    return AGENT_OK;
}

// �趨�¶�ʱ�����
INT32 Timer_C::SetPeriod(UINT32 uiNewPeriod)
{
    INT32 iRet = AGENT_OK;
    if (0 == uiNewPeriod)
    {
        TIMER_ERROR("Do not Set Timer Period to [%d]us", 
            uiNewPeriod);
        return AGENT_E_PARA;
    }
    
    if (uiPerid != uiNewPeriod)
    {
        CFG_LOCK();
        uiPerid = uiNewPeriod;        
        CFG_UNLOCK();
    }
    
    iRet = SetNewInterval(uiNewPeriod);
    if (iRet)
    {
        TIMER_WARNING("Set Thread New Interval Failed: [%d]us.", uiNewPeriod);        
    }
    
    iRet = StartThread();
    if (iRet)
    {
        TIMER_ERROR("Start Timer Thread Failed: [%d]us.", uiNewPeriod);
        return iRet;
    }
    
    TIMER_INFO("Set New Period to [%d]us Sucess", 
            uiNewPeriod);
    
    return iRet;
}

// ��ѯ��ǰ��ʱ�����
UINT32 Timer_C::GetPeriod()
{
    UINT32 uiCurrentPeriod = 0;
    CFG_LOCK();
    uiCurrentPeriod = uiPerid;
    CFG_UNLOCK();
    return uiCurrentPeriod;
}

INT32 Timer_C::AddHandler(sal_sem_t sem)
{
    INT32 iRet = AGENT_OK;
    if (NULL == sem)
    {
        TIMER_ERROR("Add NULL Handler");
        return AGENT_E_PARA;
    }
   
    TIMER_INFO("Add Handler.");
    
    CFG_LOCK();
    DeleteHandler(sem); // ��ɾ��һ��, �����ظ�
    vSemList.push_back(sem);
    CFG_UNLOCK();
    
    return iRet;
}

INT32 Timer_C::DeleteHandler(sal_sem_t sem)
{
    INT32 iRet = AGENT_E_NOT_FOUND;

    if (NULL == sem)
    {
        TIMER_ERROR("Delete NULL Handler");
        return AGENT_E_PARA;
    }
    
    vector<sal_sem_t>::iterator psem;    
    CFG_LOCK();
    for(psem = vSemList.begin(); psem != vSemList.end(); psem++)
    {
        if ( sem == *psem )
        {
            TIMER_INFO("Delete a handler.");
            vSemList.erase(psem);
            iRet = AGENT_OK;
            break;
        }
    }
    CFG_UNLOCK();
    return iRet;
}



