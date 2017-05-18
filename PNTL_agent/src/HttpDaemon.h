#ifndef __SRC_HttpDaemon_H__
#define __SRC_HttpDaemon_H__

#include <microhttpd.h>
#include <string>


// HttpDaemon类定义, 负责启动和管理Http Server Daemon服务. 
// 消息中间件Server端功能会依赖httpd.
class HttpDaemon_C
{
private:
    /*  */
    UINT32        uiPort;                     // 本HttpDaemon要绑定的端口号.
    
    struct MHD_Daemon   *pstDaemon;                 // http daemon任务句柄
    
public:
    HttpDaemon_C();                                 // 构造函数, 填充默认值.
    ~HttpDaemon_C();                                // 析构函数, 释放必要资源.

    const CHAR * pcResponcePageOK;                  // OK 时默认返回的页面
    const CHAR * pcResponcePageError;               // 处理出错时返回的页面    
    const CHAR * pcResponcePageUnsupported;         // 不支持操作时返回的页面

    INT32 StartHttpDaemon(UINT32 uiNewPort);    // 根据入参启动http daemon
    INT32 StopHttpDaemon();                           // 停止http daemon
    
    UINT32 GetCurrentServerPort()
    {
        if (pstDaemon)
            return uiPort;
        else
            return 0;
    };                                              // 获取当前Daemon状态及使用的TCP端口号

    virtual INT32 ProcessPostIterate(
                    const CHAR * pcKey, 
                    const CHAR * pcData,
                    UINT32 uiDataSize,
                    string * pstrResponce);            // Http Server Daemon POST操作处理函数
};

#endif

