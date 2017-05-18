#ifndef __SRC_HttpDaemon_H__
#define __SRC_HttpDaemon_H__

#include <microhttpd.h>
#include <string>


// HttpDaemon�ඨ��, ���������͹���Http Server Daemon����. 
// ��Ϣ�м��Server�˹��ܻ�����httpd.
class HttpDaemon_C
{
private:
    /*  */
    UINT32        uiPort;                     // ��HttpDaemonҪ�󶨵Ķ˿ں�.
    
    struct MHD_Daemon   *pstDaemon;                 // http daemon������
    
public:
    HttpDaemon_C();                                 // ���캯��, ���Ĭ��ֵ.
    ~HttpDaemon_C();                                // ��������, �ͷű�Ҫ��Դ.

    const CHAR * pcResponcePageOK;                  // OK ʱĬ�Ϸ��ص�ҳ��
    const CHAR * pcResponcePageError;               // �������ʱ���ص�ҳ��    
    const CHAR * pcResponcePageUnsupported;         // ��֧�ֲ���ʱ���ص�ҳ��

    INT32 StartHttpDaemon(UINT32 uiNewPort);    // �����������http daemon
    INT32 StopHttpDaemon();                           // ֹͣhttp daemon
    
    UINT32 GetCurrentServerPort()
    {
        if (pstDaemon)
            return uiPort;
        else
            return 0;
    };                                              // ��ȡ��ǰDaemon״̬��ʹ�õ�TCP�˿ں�

    virtual INT32 ProcessPostIterate(
                    const CHAR * pcKey, 
                    const CHAR * pcData,
                    UINT32 uiDataSize,
                    string * pstrResponce);            // Http Server Daemon POST����������
};

#endif

