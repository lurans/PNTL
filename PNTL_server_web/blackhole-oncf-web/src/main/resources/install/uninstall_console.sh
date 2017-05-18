#!/bin/bash
###############################################################
## @Company:      HUAWEI Tech. Co., Ltd.
#
## @Filename:     uninstall_console.sh
## @Usage:        sh uninstall_console.sh
## @Description:  uninstall console.
#
## @Options:      uninstall console.
## @History:      initial
## @Author:
## @Version:      v1.0
## @Created:      05.12.2015
##############################################################

##############################################################
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
    # 1。如果当前目录就是install文件所在位置，直接pwd取得绝对路径；
    # 2。而如果是从其他目录来调用install的情况，先cd到install文
    #    件所在目录,再取得install的绝对路径，并返回至原目录下。
    # 3。使用install调用该文件，使用的是当前目录路径
    if [ "` dirname "$0" `" = "" ] || [ "` dirname "$0" `" = "." ] ; then
        CURRENT_PATH="`pwd`"
    else
        cd ` dirname "$0" `
        CURRENT_PATH="`pwd`"
        cd - > /dev/null 2>&1
    fi
}

######################################################################
#  FUNCTION     : uninstallConsole
#  DESCRIPTION  : 卸载console
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
uninstallConsole()
{
    ##先修改权限再删除
    chmod -R 700 ${PRODUCT_PATH}
    ##删除程序目录
    rm -rf ${PRODUCT_PATH}
    ##删除日志文件（包括程序日志和运行脚本日志）
    rm -rf ${BASE_LOGGER_PATH}/${MODULE_NAME}
    rm -rf ${BASE_LOGGER_PATH}/monitor/${MODULE_NAME}.log
    ##删除console context
    sed -i "/${MODULE_NAME}/d" ${INSTALL_MODULE_SERVER_FILE} || return ${RETURN_CODE_ERROR}
    return 0
}

######################################################################
#  FUNCTION     : uninstallCron
#  DESCRIPTION  : 卸载cron
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    2
######################################################################
uninstallCron()
{
    # 判断
    echo 'uninstall console cron start.'
    local CRONTAB_FILE='/etc/crontab'
    if [ ! -f "${CRONTAB_FILE}" ]; then
        echo 'crontab does not exist.'
        return 2
    fi
    # 删除定时器
    echo 'delete console cron.'
    sudo /usr/bin/sed -ri '/tomcat_monitor/d' ${CRONTAB_FILE}
    echo 'uninstall console cron end.'
    return 0
}

#####################################################
#
# 卸载路径
#
#####################################################
getCurPath
cd "${CURRENT_PATH}/.."

##引入公共模块
. ./bin/util.sh

##新建日志文件(和安装一个文件，卸载时保留)
LOGGER_PATH=${BASE_LOGGER_PATH}/install
LOGGER_FILE=${LOGGER_PATH}/${MODULE_NAME}.log

#初始化
#执行日志目录初始化
initLogDir

##检查用户
chkUser

##server.xml文件
INSTALL_MODULE_SERVER_FILE=${TOMCAT_PATH}/conf/server.xml

logger "uninstall ${MODULE_PATH}"

# 卸载cron
uninstallCron

uninstallConsole || { logger "uninstall ${MODULE_PATH} failed."; exit "${RETURN_CODE_ERROR}"; }

logger "uninstall ${MODULE_PATH} success"

exit "${RETURN_CODE_SUCCESS}"