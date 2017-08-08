#ifndef __SRC_FILENOTIFIER_H__
#define __SRC_FILENOTIFIER_H__

#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/inotify.h>
#include <limits.h>
#include <fcntl.h>

#include "Sal.h"
#include "ThreadClass.h"

using namespace std;

const UINT32 BUF_LEN = 128;
const string filePath = "/opt/huawei/ServerAntAgent/agentConfig.cfg";

class FileNotifier_C : ThreadClass_C
{
private:
    INT32 notifierId;
    INT32 wd;
    CHAR buf[BUF_LEN];
    struct inotify_event *event;

    INT32 ThreadHandler();
    INT32 PreStopHandler();
    INT32 PreStartHandler();
    INT32 HandleEvent(struct inotify_event * event);
public:
    FileNotifier_C();
    ~FileNotifier_C();
    INT32 Init();
};

#endif
