#ifndef __SRC_Timer_H__
#define __SRC_Timer_H__

#include <pthread.h>
#include <vector>

#include "Sal.h"
#include "AgentCommon.h"
#include "ThreadClass.h"

// 定时器类定义
class Timer_C : ThreadClass_C
{
private:
    /*  */
    sal_mutex_t         stTimerCfgLock;     // 互斥锁保护配置
    UINT32        uiPerid;            // 当前定时器周期, 单位us.
    vector <sal_sem_t>  vSemList;           // 通知信号量链表

    /* Thread 实现代码 */
    INT32 ThreadHandler();                        // 任务主处理函数    
    INT32 PreStopHandler();                       // StopThread触发, 通知ThreadHandler主动退出.
    INT32 PreStartHandler();                      // StartThread触发, 通知ThreadHandler即将被调用.

    INT32 ThreadNotify();                          // 定时器任务通知函数
    
public:
    Timer_C();                                  // 构造函数, 填充默认值.
    ~Timer_C();                                 // 析构函数, 释放必要资源.

    INT32 Init(UINT32 uiNewPeriod);          // 根据入参完成对象初始化    

    INT32 SetPeriod(UINT32 uiNewPeriod);    // 设定新定时器间隔
    UINT32 GetPeriod();                   // 查询当前定时器间隔
    INT32 AddHandler(sal_sem_t sem);              // 添加通知信号量
    INT32 DeleteHandler(sal_sem_t sem);           // 删除通知信号量
};

#endif
