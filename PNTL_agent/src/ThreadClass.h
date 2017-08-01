#ifndef __SRC_ThreadClass_H__
#define __SRC_ThreadClass_H__

#include <pthread.h>
#include "Sal.h"

typedef struct tagThreadControl
{
    UINT32   uiThreadState;            // ��ǰThread״̬, ��Thread����ˢ��.
    UINT32   uiThreadInterval;         // Thread���м��, 0��ʾ�˳�, ��0��ʾ���м��. (-1)��ʾ���ͷ�CPU, ȫ������.
    UINT32   uiThreadDefaultInterval;  // ThreadĬ��ʱ����.��������ʱuiThreadInterval��Ĭ��ֵ. ��λus, Ĭ��100ms.
} ThreadControl_S;

typedef void (*Func)();

// Thread�ඨ��
class ThreadClass_C
{
private:
    UINT32   uiThreadUpdateInterval;  // ��λus. Thread״̬��ˢ��ʱ��.StopCallBack()�·���,�ThreadUpdateInterval us��
    // ThreadHandler()Ӧ�÷���, ����Thread�ᱻǿ����ֹ.
    UINT32   uiThreadState;           // Thread״̬��
    pthread_t ThreadFd;                     // Thread���.

public:
    ThreadClass_C();                        // ���캯��, ���Ĭ��ֵ.
    ~ThreadClass_C();                       // ��������, �ͷű�Ҫ��Դ.

    INT32 StopThread();                       // ֹͣ����.
    INT32 StartThread();                      // ��������.

    INT32 SetNewInterval(UINT32 uiNewInterval);     // �趨�¶�ʱ�����
    UINT32 GetCurrentInterval();                  // ��ѯ��ǰ��ʱ�����

    INT32 ThreadUpdateState(UINT32 uiNewState);     // ˢ������״̬��

    virtual INT32 ThreadHandler();                        // ������������
    virtual INT32 PreStopHandler();                       // StopThread����, ֪ͨThreadHandler�����˳�.
    virtual INT32 PreStartHandler();                      // StartThread����, ֪ͨThreadHandler����������.

};

#endif
