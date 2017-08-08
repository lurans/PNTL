#include "FileNotifier.h"
#include "AgentCommon.h"
#include "GetLocalCfg.h"
#include "Log.h"

FileNotifier_C::FileNotifier_C()
{
    notifierId = -1;
    wd = -1;
}

FileNotifier_C::~FileNotifier_C()
{
    INT32 iRet = inotify_rm_watch(notifierId, wd);
    if (0 > iRet)
    {
        FILE_NOTIFIER_ERROR("Remove watch fail[%d]", iRet);
    }

    if (-1 != notifierId)
    {
        iRet = close(notifierId);
        if (0 > iRet)
        {
            FILE_NOTIFIER_ERROR("close watch fail[%d]", iRet);
        }
    }
}

INT32 FileNotifier_C::Init()
{
    notifierId = inotify_init();
    if (0 > notifierId)
    {
        FILE_NOTIFIER_ERROR("Create a file notifier fail[%d]", notifierId);
        return AGENT_E_MEMORY;
    }

    wd = inotify_add_watch(notifierId, filePath.c_str(), IN_MODIFY);
    if (0 > wd)
    {
        FILE_NOTIFIER_ERROR("Create a watch Item fail[%d]", wd);
        return AGENT_E_ERROR;
    }
    INT32 iRet = StartThread();
    if(iRet)
    {
        FILE_NOTIFIER_ERROR("StartFileNotifierThread failed[%d]", iRet);
        return iRet;
    }
    return AGENT_OK;
}

INT32 FileNotifier_C::HandleEvent(struct inotify_event * event)
{
    if (event->mask & IN_MODIFY)
    {
        SHOULD_REFRESH_CONF = 1;
    }
    else if (event->mask & IN_IGNORED)
    {
        wd = inotify_add_watch(notifierId, filePath.c_str(), IN_MODIFY);
        if (0 > wd)
        {
            FILE_NOTIFIER_ERROR("Create a watch Item fail[%d]", wd);
            return AGENT_E_ERROR;
        }
        SHOULD_REFRESH_CONF = 1;
    }
}

INT32 FileNotifier_C::PreStopHandler()
{
    return 0;
}

INT32 FileNotifier_C::PreStartHandler()
{
    return 0;
}

INT32 FileNotifier_C::ThreadHandler()
{
    INT32 sizeRead = 0;
    CHAR* pBuf;
    while(GetCurrentInterval())
    {
        sizeRead = read(notifierId, buf, BUF_LEN);
        for (pBuf = buf; pBuf < buf + sizeRead;)
        {
            event = (struct inotify_event *) pBuf;
            HandleEvent(event);
            pBuf +=sizeof(struct inotify_event) + event->len;
        }
    }
}