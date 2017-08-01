#include "FileNotifier.h"
#include "AgentCommon.h"
#include "GetLocalCfg.h"
#include "Log.h"

FileNotifier_C::FileNotifier_C()
{
    FILE_NOTIFIER_INFO("Create a FileNotifier.");
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

INT32 FileNotifier_C::Init(FlowManager_C* pcFlowManager)
{
    manager = pcFlowManager;
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
	FILE_NOTIFIER_INFO("Init success.");
	return AGENT_OK;
}

INT32 FileNotifier_C::HandleEvent(struct inotify_event * event)
{
    if (event->mask & IN_MODIFY)
    {
        FILE_NOTIFIER_INFO("Config file is changed, refreash config.");
        GetLocalAgentConfig(manager);
    }
	else if (event->mask & IN_IGNORED)
	{
	    FILE_NOTIFIER_INFO("File maybe altered by vim, readd filewatch.");
	    wd = inotify_add_watch(notifierId, filePath.c_str(), IN_MODIFY);
		if (0 > wd)
		{
			FILE_NOTIFIER_ERROR("Create a watch Item fail[%d]", wd);
			return AGENT_E_ERROR;
		}
		GetLocalAgentConfig(manager);
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
		for (pBuf = buf;pBuf < buf + sizeRead;)
		{
		    event = (struct inotify_event *) pBuf;
			HandleEvent(event);
			pBuf +=sizeof(struct inotify_event) + event->len;  
		}
    }
}