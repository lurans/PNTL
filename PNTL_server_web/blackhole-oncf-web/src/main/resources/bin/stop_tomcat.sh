#!/bin/bash
# Copyright Huawei Technologies Co., Ltd. 1998-2015. All rights reserved.
# Description 该脚本适用于停止tomcat
######################################################################

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

. /etc/profile

##切换到当前路径
getCurPath
cd "${CURRENT_PATH}"

sh tomcat_monitor.sh stop