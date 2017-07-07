
#ifndef __SRC_MessagePlatformServer_H__
#define __SRC_MessagePlatformServer_H__

#include "FlowManager.h"
#include "HttpDaemon.h"

// MessagePlatformServer类定义, 负责启动消息中间件Server端服务.
// 对接收到的数据进行处理.并调用本地处理函数.
class MessagePlatformServer_C : HttpDaemon_C
{
private:

    FlowManager_C* pcFlowManager;                   // FlowManager对象指针, 收到消息解析完后需要调用pcFlowManager对象中的处理函数.

    INT32 ProcessPostIterate(
        const CHAR * pcKey,
        const CHAR * pcData,
        UINT32 uiDataSize,
        string * pstrResponce);            // Http Server Daemon POST操作处理函数
    void HandleResponse(INT32 iRet, string * pstrResponce);

public:
    MessagePlatformServer_C();                               // 构造函数, 填充默认值.
    ~MessagePlatformServer_C();                              // 析构函数, 释放必要资源.

    INT32 Init(
        UINT32 uiNewPort,
        FlowManager_C* pcFlowManager);                  // 根据参数完成初始化.

};

#endif
