#!/bin/bash
########################################################################
#
#   FUNCTION   : main
#   DESCRIPTION: copy dir to
#   CALLS      : 无
#   CALLED BY  : 无
#   INPUT      : 无
#   OUTPUT     : 无
#   LOCAL VAR  : 无
#   USE GLOBVAR: 无
#   RETURN     : 无
#   CHANGE DIR : 无
#######################################################################

#####################################################
# 用户相关变量
######################################################
SERVICE_USER=odmk
SERVICE_GROUP=odmk
MODULE_NAME=console
MODULE_PATH=webapps

#####################################################
#
# 安装路径
#
#####################################################
PRODUCT_PATH=/opt/${MODULE_NAME}
TOMCAT_PATH=/opt/tomcat7059

RETURN_CODE_SUCCESS=0
RETURN_CODE_ERROR=1

#对应缺省：目录700 文件600权限
umask 0077

LOGMAXSIZE=5120
BASE_LOGGER_PATH=/var/log/${MODULE_NAME}

######################################################################
#  FUNCTION     : chkUser
#  DESCRIPTION  : 检查当前用户是否是$SERVICE_USER
#  CALLS        : 无
#  CALLED BY    : 任何需要调用此函数的地方
#  INPUT        : $1    想要检查的用户名
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   0   成功
#                   1   失败
######################################################################
chkUser()
{
    logger_without_echo "check current user"
    local curUser=$(/usr/bin/whoami | /usr/bin/awk '{print $1}')
    if [ "$curUser" = "$SERVICE_USER" ]; then
       logger_without_echo "check current user success"
       return 0
    else
       die "${MODULE_NAME} can only run by ${SERVICE_USER}"
    fi
}

######################################################################
#  FUNCTION     : initLogDir
#  DESCRIPTION  : 创建日志目录
#  CALLS        : 无
#  CALLED BY    : 本脚本初始化日志
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       : 无
######################################################################
initLogDir()
{
    if [ -e "$LOGGER_PATH" ]; then
        return 0
    else
        mkdir -p ${LOGGER_PATH}
        echo "init log dir success."
    fi
}

######################################################################
#  FUNCTION     : status
#  DESCRIPTION  : 检查当前进程状态
#  CALLS        : 无
#  CALLED BY    : 任何需要调用此函数的地方
#  INPUT        : $1    想要检查的进程名
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   0   成功
#                   1   失败
######################################################################
internalStatus()
{
    # do some work, such as send one msg to process
    local pid=$(ps -ww -eo pid,cmd | grep -w "${MODULE_NAME}.main.proc" |grep -w java | grep -vwE "grep|vi|vim|tail|cat" | awk '{print $1}' | head -1)
    RETVAL=${RETURN_CODE_ERROR}
    [ -n "$pid" ] && RETVAL=${RETURN_CODE_SUCCESS}
    if [ "$RETVAL" -eq ${RETURN_CODE_SUCCESS} ]; then
        logger_without_echo "normal"
    else
        logger_without_echo "abnormal"
    fi
    return "$RETVAL"
}

######################################################################
#  FUNCTION     : logger_without_echo
#  DESCRIPTION  : 记录日志到对应文件中，不输出到终端。
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       : 无
######################################################################
logger_without_echo()
{
    local logsize=0
    if [ -e "$LOGGER_FILE" ]; then
        logsize=`ls -lk ${LOGGER_FILE} | awk -F " " '{print $5}'`
    else
        touch ${LOGGER_FILE}
        chown ${SERVICE_USER}: ${LOGGER_FILE}
        chmod 600 ${LOGGER_FILE}
    fi

    if [ "$logsize" -gt "$LOGMAXSIZE" ]; then
        # 每次删除10000行，约300K
        sed -i '1,10000d' "$LOGGER_FILE"
    fi
    echo "[` date -d today +\"%Y-%m-%d %H:%M:%S\"`,000] $*" >>"$LOGGER_FILE"

}
######################################################################
#  FUNCTION     : logger
#  DESCRIPTION  : 记录日志到对应文件中，同时输出到终端。
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       : 无
######################################################################
logger()
{
    logger_without_echo $*
    echo "$*"
}

######################################################################
#  FUNCTION     : die
#  DESCRIPTION  : 记录日志并退出程序。
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       : 无
######################################################################
die()
{
    logger "$*"
    exit ${RETURN_CODE_ERROR}
}