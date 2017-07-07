
#ifndef __SRC_MessagePlatformServer_H__
#define __SRC_MessagePlatformServer_H__

#include "FlowManager.h"
#include "HttpDaemon.h"

// MessagePlatformServer�ඨ��, ����������Ϣ�м��Server�˷���.
// �Խ��յ������ݽ��д���.�����ñ��ش�����.
class MessagePlatformServer_C : HttpDaemon_C
{
private:

    FlowManager_C* pcFlowManager;                   // FlowManager����ָ��, �յ���Ϣ���������Ҫ����pcFlowManager�����еĴ�����.

    INT32 ProcessPostIterate(
        const CHAR * pcKey,
        const CHAR * pcData,
        UINT32 uiDataSize,
        string * pstrResponce);            // Http Server Daemon POST����������
    void HandleResponse(INT32 iRet, string * pstrResponce);

public:
    MessagePlatformServer_C();                               // ���캯��, ���Ĭ��ֵ.
    ~MessagePlatformServer_C();                              // ��������, �ͷű�Ҫ��Դ.

    INT32 Init(
        UINT32 uiNewPort,
        FlowManager_C* pcFlowManager);                  // ���ݲ�����ɳ�ʼ��.

};

#endif
