#ifndef __SRC_Timer_H__
#define __SRC_Timer_H__

#include <pthread.h>
#include <vector>

#include "Sal.h"
#include "AgentCommon.h"
#include "ThreadClass.h"

// ��ʱ���ඨ��
class Timer_C : ThreadClass_C
{
private:
    /*  */
    sal_mutex_t         stTimerCfgLock;     // ��������������
    UINT32        uiPerid;            // ��ǰ��ʱ������, ��λus.
    vector <sal_sem_t>  vSemList;           // ֪ͨ�ź�������

    /* Thread ʵ�ִ��� */
    INT32 ThreadHandler();                        // ������������    
    INT32 PreStopHandler();                       // StopThread����, ֪ͨThreadHandler�����˳�.
    INT32 PreStartHandler();                      // StartThread����, ֪ͨThreadHandler����������.

    INT32 ThreadNotify();                          // ��ʱ������֪ͨ����
    
public:
    Timer_C();                                  // ���캯��, ���Ĭ��ֵ.
    ~Timer_C();                                 // ��������, �ͷű�Ҫ��Դ.

    INT32 Init(UINT32 uiNewPeriod);          // ���������ɶ����ʼ��    

    INT32 SetPeriod(UINT32 uiNewPeriod);    // �趨�¶�ʱ�����
    UINT32 GetPeriod();                   // ��ѯ��ǰ��ʱ�����
    INT32 AddHandler(sal_sem_t sem);              // ���֪ͨ�ź���
    INT32 DeleteHandler(sal_sem_t sem);           // ɾ��֪ͨ�ź���
};

#endif
