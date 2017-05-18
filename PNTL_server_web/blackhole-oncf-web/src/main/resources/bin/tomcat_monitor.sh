#!/bin/bash
# Copyright Huawei Technologies Co., Ltd. 1998-2015. All rights reserved.
# Description
# 该脚本适用于启动、停止、重启tomcat
######################################################################

JVM_MEM_OPT="-server -Xms3072m -Xmx3072m -XX:PermSize=108m -XX:MaxPermSize=256m"

RETURN_CODE_SUCCESS=0
RETURN_CODE_ERROR=1


######################################################################
#   DESCRIPTION: 切换到当前目录
#   CALLS      : 无
#   CALLED BY  : main
#   INPUT      : 无
#   OUTPUT     : 无
#   LOCAL VAR  : 无
#   USE GLOBVAR: 无
#   RETURN     : 无
#   CHANGE DIR : 无
######################################################################
getCurPath()
{
    # 1 如果当前目录就是install文件所在位置，直接pwd取得绝对路径
    # 2 而如果是从其他目录来调用install的情况，先cd到install文件所在目录,再取得install的绝对路径，并返回至原目录下
    # 3 使用install调用该文件，使用的是当前目录路径
    if [ "` dirname "$0" `" = "" ] || [ "` dirname "$0" `" = "." ] ; then
        CURRENT_PATH="`pwd`"
    else
        cd ` dirname "$0" `
        CURRENT_PATH="`pwd`"
        cd - > /dev/null 2>&1
    fi
}

start_tomcat()
{
    pid=$(ps -ef |grep $INSTANCE_LABEL | grep -v grep |  awk '{print $2}')
    if [ -n "$pid" ] ; then
        logger "console already start"
        return 1
    fi
    export CATALINA_OPTS="$JVM_MEM_OPT -D$INSTANCE_LABEL"

    sh "${START_UP_SCRIPT}"
    
    return 0
}

stop_tomcat()
{
    export CATALINA_OPTS="-D$INSTANCE_LABEL"
    sh "${SHUT_DOWN_SCRIPT}"
    while ( true )
    do
        pid=$(ps -ef |grep $INSTANCE_LABEL | grep -v grep |  awk '{print $2}')
        if [ -n "$pid" ] ; then
            logger "try to kill console"
            kill -9 "$pid"
        else
            break
        fi
    done
    return 0
}

. /etc/profile

getCurPath
cd "${CURRENT_PATH}"

##引入公共模块
. ./util.sh

##新建运行日志文件
LOGGER_PATH=${BASE_LOGGER_PATH}/monitor
LOGGER_FILE=${LOGGER_PATH}/${MODULE_NAME}.log

#初始化
#执行日志目录初始化
initLogDir

##检查用户
chkUser

PROC_NAME="${MODULE_NAME}.main.proc"
INSTANCE_LABEL="tomcat.instance.name=$PROC_NAME"
START_UP_SCRIPT=${TOMCAT_PATH}/bin/startup.sh
SHUT_DOWN_SCRIPT=${TOMCAT_PATH}/bin/shutdown.sh

# 程序无法正常启动时,尝试次数
TOMCAT_LOOP_COUNTER=3

# action
ACTION=$1
shift

# init ret value for exit
RETVAL=0
# ensure action is specficed
[ -z "$ACTION" ] && die "no action is specficed"

# start
start()
{
    start_tomcat > /dev/null 2>&1
    if [ $? -eq 0 ] ; then
        RETVAL=0
        logger "Result:start tomcat success"
    else
        RETVAL=-1
        logger "Result:start tomcat fail"
    fi
    cd - >/dev/null 2>&1
}

# status
status()
{
    # do some work, such as send one msg to process 
    if internalStatus >/dev/null ; then
        logger "status check: normal"
        return 0
    else
        logger "status check: abnormal"
        return 2
    fi
}

# stop
stop()
{
    stop_tomcat > /dev/null 2>&1

    if [ $? -ne 0 ] ;then
        logger "Result:stop failed"
        RETVAL=-1
    else
        logger "Result:stop success"
        RETVAL=0
    fi
    cd - >/dev/null 2>&1
}


# restart
restart()
{
    stop
    sleep 2
    start
}

######################################################################
#  FUNCTION     : check
#  DESCRIPTION  : 检查程序状态，如果异常则启动程序. 若重复${TOMCAT_LOOP_COUNTER}次启动后，
#                 仍然检测状态异常，则返回异常.
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    2
######################################################################
check()
{    
    CURRENT_NUMBER=0
    for((; CURRENT_NUMBER < ${TOMCAT_LOOP_COUNTER}; CURRENT_NUMBER++));
    do
        status > /dev/null 2>&1
        if [ $? -eq 0 ] ;then
            logger "Result:check success, CURRENT_NUMBER is ${CURRENT_NUMBER}"
            return 0
        else
            logger "Result:check failed, CURRENT_NUMBER is ${CURRENT_NUMBER}. it will start."
            start > /dev/null 2>&1
            sleep 2
        fi
    done
    
    logger "Result:check failed."
    return 2
}

case "$ACTION" in
    start)
    start
    ;;
    stop)
    stop
    ;;
    status)
    status
    ;;
    restart)
    restart
    ;;
    check)
    check
    ;;
    *)
    die $"Usage: $0 {start|stop|status|restart|check}"
esac

exit $RETVAL