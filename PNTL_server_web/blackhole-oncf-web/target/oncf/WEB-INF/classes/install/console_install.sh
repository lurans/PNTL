#!/bin/bash
# Copyright Huawei Technologies Co., Ltd. 1998-2015. All rights reserved.
# Description
# 该脚本仅适用于服务上线部署安装（暂时不包括服务透传配置的修改），完成后还需到silvan注册服务和console的地址
# 1. 安装准备
#    1.1 规划安装目录
#    1.2 iam、silvan和memcache的IP
#    1.3 服务webapp的名称
# 2. 安装步骤
#    2.1 修改web.xml
#    2.2 修改application.properties
#    2.3 修改frameworkService.js
#    2.4 修改menu.html
#    2.5 修改rest_api_manager_framework.xml
#    2.6 修改tomcat的server.xml，新增console context
######################################################################

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

######################################################################
#   DESCRIPTION: 读取配置文件内容并替换相应的文件
#   CALLS      : 无
#   CALLED BY  : main
#   INPUT      : 无
#   OUTPUT     : 无
#   LOCAL VAR  : 无
#   USE GLOBVAR: 无
#   RETURN     : 无
#   CHANGE DIR : 无
######################################################################
installConsole()
{
    local configFile="$INSTALL_MODULE_CONFIG_FILE"
    local server_user=`sed '/^service.user=/!d;s/.*=//' ${configFile}`
    local webapp_name=`sed '/^service.webapp.name=/!d;s/.*=//' ${configFile}`
    local webapp_address=`sed '/^service.webapp.address=/!d;s/.*=//' ${configFile}`
    local oldconsole_address=`sed '/^service.oldconsole.address=/!d;s/.*=//' ${configFile}`
    local iam_ip=`sed '/^service.iam.ip=/!d;s/.*=//' ${configFile}`
    local iam_authui_port=`sed '/^service.iam.authui.port=/!d;s/.*=//' ${configFile}`
    local iam_port=`sed '/^service.iam.server.port=/!d;s/.*=//' ${configFile}`
    local iam_outer_address=`sed '/^service.iam.outer.address=/!d;s/.*=//' ${configFile}`
    local mem_ip=`sed '/^service.memcache.ip=/!d;s/.*=//' ${configFile}`
    local silvan_ip=`sed '/^service.silvan.ip=/!d;s/.*=//' ${configFile}`
    local silvan_port=`sed '/^service.silvan.port=/!d;s/.*=//' ${configFile}`
    local webapp_server_name=`sed '/^service.webapp.address=/!d;s/.*=//;s/\/[^\/]*\/$//' ${configFile}`
    ## 修改web.xml
    installWeb "$iam_ip" "$webapp_address" "$iam_authui_port" "$iam_port" "$webapp_server_name" "$oldconsole_address" "$iam_outer_address"
    ## 修改rest_api_manager_framework.xml中silvan和IAM信息
    installPassthrough "$silvan_ip" "$silvan_port" "$iam_ip" "$iam_port"
    ## 修改application.properties
    installApp "$mem_ip" "$silvan_ip" "$silvan_port"
    ## 修改console javascript文件
    installScript "$webapp_name"
    ## 修改menu.html
    installMenu "$webapp_name"
}

######################################################################
#  FUNCTION     : installSilvan
#  DESCRIPTION  : 修改rest_api_manager_framework.xml中silvan信息
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
installSilvan()
{
    local silvan_ip="$1"
    local silvan_port="$2"
    sed -ri ":label;N;s#(<api>.*?silvan/rest.*?<host>).*?(</host>.*?silvan/rest.*?</api>)#\1${silvan_ip}:${silvan_port}\2#g;b label" \
            "$INSTALL_MODULE_PASS_XML" || return ${RETURN_CODE_ERROR}
}

######################################################################
#  FUNCTION     : installIam
#  DESCRIPTION  : 修改rest_api_manager_framework.xml中iam信息
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
installIam()
{
    local iam_ip="$1"
    local iam_port="$2"
    sed -ri ":label;N;s#(<api>.*?iam/assumeRoles.*?<host>).*?(</host>.*?/v3-huawei/xrole.*?</api>)#\1${iam_ip}:${iam_port}\2#g;b label" \
            "$INSTALL_MODULE_PASS_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(<api>.*?iam/v3/projects.*?<host>).*?(</host>.*?/v3/projects.*?</api>)#\1${iam_ip}:${iam_port}\2#g;b label" \
            "$INSTALL_MODULE_PASS_XML" || return ${RETURN_CODE_ERROR}
}



######################################################################
#  FUNCTION     : installPassthrough
#  DESCRIPTION  :  修改rest_api_manager_framework.xml中透传信息
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
installPassthrough()
{
    local silvan_ip="$1"
    local silvan_port="$2"
    local iam_ip="$3"
    local iam_port="$4"
    installSilvan "$silvan_ip" "$silvan_port"
    installIam "$iam_ip" "$iam_port"

}

######################################################################
#  FUNCTION     : installWeb
#  DESCRIPTION  :  修改web.xml
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
installWeb()
{
    local iam_ip="$1"
    local webapp_address="$2"
    local iam_authui_port="$3"
    local iam_port="$4"
    local webapp_server_name="$5"
    local oldconsole_address="$6"
    local iam_outer_address="$7"
    local index_file_name=$(ls ${PRODUCT_PATH}/${MODULE_PATH} | grep index.html)
    sed -ri -e "s#(https://).*?(/authui)#\1${iam_ip}:${iam_authui_port}\2#" \
            -e "s#(service=).*?(</param-value>)#\1${webapp_address}\2#" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(iamServerUrl.*?https://).*?(</param-value>.*?minRenewTime)#\1${iam_ip}:${iam_port}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(iamServerUrl.*?https://).*?(</param-value>.*?iamServerUrlPrefix)#\1${iam_ip}:${iam_port}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(iamServerLoginUrl.*?serverName.*?<param-value>).*?(</param-value>.*?authenticationRedirectStrategyClass)#\1${webapp_server_name}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(iamServerUrlPrefix.*?serverName.*?<param-value>).*?(</param-value>.*?hostnameVerifier)#\1${webapp_server_name}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(IamSSOFilter.*?https://).*?(</param-value>.*?IamSSOFilter)#\1${iam_ip}:${iam_port}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(oldConsoleHome.*?<param-value>).*?(</param-value>.*?forbidRoleList)#\1${oldconsole_address}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(should be internet.*?https://).*?(/authui.*?</param-value>)#\1${iam_outer_address}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -ri ":label;N;s#(iamServerUrlPrefix.*?https://).*?(/authui.*?hostnameVerifier)#\1${iam_ip}:${iam_port}\2#;b label" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
    sed -i "/<\/web-app>/i<welcome-file-list><welcome-file>${index_file_name}<\/welcome-file><\/welcome-file-list>" \
            "$INSTALL_MODULE_WEB_XML" || return ${RETURN_CODE_ERROR}
}

######################################################################
#  FUNCTION     : installApp
#  DESCRIPTION  : 修改application.properties
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
installApp()
{
    local mem_ip="$1"
    local silvan_ip="$2"
    local silvan_port="$3"
    sed -ri -e "s#(app.memcached.server=).*#\1${mem_ip}#" \
            -e "s#(app.pt.silvan.serverbyregion.uri=https://).*?(/silvan/rest)#\1${silvan_ip}:${silvan_port}\2#" \
            "$INSTALL_MODULE_APP_PROP" || return ${RETURN_CODE_ERROR}
}

######################################################################
#  FUNCTION     : installScript
#  DESCRIPTION  : 修改JavaScript
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
installScript()
{
    local webapp_name="$1"
    sed -ri "s#(/\{0,1\}).*?(rest/silvan/rest)#\1${webapp_name}\2#" "$INSTALL_MODULE_JAVASCRIPT" || return ${RETURN_CODE_ERROR}
}

######################################################################
#  FUNCTION     : installMenu
#  DESCRIPTION  : 修改menu.html
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
installMenu()
{
    local webapp_name="$1"
    sed -ri "s#(/\{0,1\}).*?(/logout)#\1${webapp_name}\2#" "$INSTALL_MODULE_MENU" || return ${RETURN_CODE_ERROR}
}

######################################################################
#  FUNCTION     : addContext
#  DESCRIPTION  :  在server.xml中新增console context配置以及参数调优
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  OUTPUT       : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
#  RETURN       :   成功    0
#                   失败    1
######################################################################
addContext()
{
    sed -ri "s#(</Host>)#<Context path=\"/${MODULE_PATH}\" docBase=\"${PRODUCT_PATH}/${MODULE_PATH}\" reloadable=\"false\"/>\n\1#" \
            ${INSTALL_MODULE_SERVER_FILE} || return ${RETURN_CODE_ERROR}
    sed -i "s/maxThreads=\"[0-9]*\".*SSLEnabled/maxThreads=\"750\" acceptCount=\"500\" minSpareThreads=\"25\" SSLEnabled/g" \
            ${INSTALL_MODULE_SERVER_FILE} || return ${RETURN_CODE_ERROR}
}

######################################################################
#  FUNCTION     : installCron
#  DESCRIPTION  : 初始化定时器
#  CALLS        : 无
#  CALLED BY    : 无
#  INPUT        : 无
#  READ GLOBVAR : 无
#  WRITE GLOBVAR: 无
######################################################################
installCron()
{
    logger "Install console cron start."
    local configFile="$INSTALL_MODULE_CONFIG_FILE"
    local CRONTAB_FILE='/etc/crontab'
    local CONSOLE_LOCAL_TIMED_INTERVAL=`sed '/^console.cron.timed.interval=/!d;s/.*=//' $configFile`
    if [ ! -f "${CRONTAB_FILE}" ]; then
        LOG_WARN "crontab does not exist."
        return 2
    fi
    # 删除定时器
    logger "delete console cron."
    sudo /usr/bin/sed -ri '/tomcat_monitor/d' ${CRONTAB_FILE}
    
    logger "add consle cron."
    if [ ${CONSOLE_LOCAL_TIMED_INTERVAL} -gt 0 ] && [ ${CONSOLE_LOCAL_TIMED_INTERVAL} -lt 60 ]; then 
        logger "Timer interval is ${CONSOLE_LOCAL_TIMED_INTERVAL} minutes.";
    else 
        CONSOLE_LOCAL_TIMED_INTERVAL = 20;
        logger "Interval timer is to take the default time, the default time is ${CONSOLE_LOCAL_TIMED_INTERVAL} minutes"; 
    fi
    sudo sh -c "echo '*/${CONSOLE_LOCAL_TIMED_INTERVAL} * * * * odmk ${productPath}/bin/tomcat_monitor.sh check' >> ${CRONTAB_FILE}"
    
    # 修改tomcat_mornitor中循环试错的次数
    logger "modify tomcat_monitor 's CONSOLE_LOOP_COUNTER."
    local CONSOLE_LOOP_COUNTER=`sed '/^console.cron.loop.counter=/!d;s/.*=//' $configFile`
    chmod 700 "${productPath}/bin/tomcat_monitor.sh"
    sed -ri "s/(CONSOLE_LOOP_COUNTER=)(.*)/\1${CONSOLE_LOOP_COUNTER}/" "${productPath}/bin/tomcat_monitor.sh"
    
    logger "restart cron."
    sudo /usr/sbin/rccron restart
    logger "Install console cron end."
    return 0
}

umask 0077

getCurPath
cd "${CURRENT_PATH}/.."

productPath="`pwd`"
##安装过程文件
INSTALL_MODULE_CONFIG_FILE=${productPath}/install/console.conf
##转化安装过程文件格式
dos2unix ${INSTALL_MODULE_CONFIG_FILE} > /dev/null 2>&1
##根据配置文件修改用户相关变量
TOMCAT_PATH=`sed '/^tomcat.path=/!d;s/.*=//' ${INSTALL_MODULE_CONFIG_FILE}`
SERVICE_USER=`sed '/^service.user=/!d;s/.*=//' ${INSTALL_MODULE_CONFIG_FILE}`
SERVICE_GROUP=`sed '/^service.user.group=/!d;s/.*=//' ${INSTALL_MODULE_CONFIG_FILE}`
MODULE_PATH=`sed '/^service.webapp.name=/!d;s/.*=//' ${INSTALL_MODULE_CONFIG_FILE}`

sed -ri "s#(PRODUCT_PATH=).*?#\1${productPath}#" ./bin/util.sh
sed -ri "s#(TOMCAT_PATH=).*?#\1${TOMCAT_PATH}#" ./bin/util.sh
sed -ri "s#(SERVICE_USER=).*?#\1${SERVICE_USER}#" ./bin/util.sh
sed -ri "s#(SERVICE_GROUP=).*?#\1${SERVICE_GROUP}#" ./bin/util.sh
sed -ri "s#(MODULE_PATH=).*?#\1${MODULE_PATH}#" ./bin/util.sh


##引入公共模块
. ./bin/util.sh

#初始化
#执行日志目录初始化
LOGGER_PATH=${BASE_LOGGER_PATH}/install
LOGGER_FILE=${LOGGER_PATH}/${MODULE_NAME}.log
initLogDir

##检查用户
chkUser

##server.xml文件
INSTALL_MODULE_SERVER_FILE=${TOMCAT_PATH}/conf/server.xml
##需要修改的程序文件
INSTALL_MODULE_UTIL_FILE=${PRODUCT_PATH}/bin/util.sh
INSTALL_MODULE_WEB_XML=${PRODUCT_PATH}/${MODULE_PATH}/WEB-INF/web.xml
INSTALL_MODULE_APP_PROP=${PRODUCT_PATH}/${MODULE_PATH}/WEB-INF/classes/config/application.properties
INSTALL_MODULE_PASS_XML=${PRODUCT_PATH}/${MODULE_PATH}/WEB-INF/classes/config/directrouters/rest_api_manager_framework.xml
INSTALL_MODULE_JAVASCRIPT=$(find ${PRODUCT_PATH}/${MODULE_PATH}/src/app/framework -regex .*frameworkService.js -exec readlink -f {} \;)
INSTALL_MODULE_MENU=$(find ${PRODUCT_PATH}/${MODULE_PATH}/src/app/framework -regex .*menus.html -exec readlink -f {} \;)

logger "Install ${MODULE_PATH}"

installConsole || { logger "Install ${MODULE_PATH} failed."; exit ${RETURN_CODE_ERROR}; }

##初始化cron
installCron

##在server.xml中新增console context配置
addContext

##删除安装过程文件过程
logger "Delete ${MODULE_PATH}.conf file"
rm -f "$INSTALL_MODULE_CONFIG_FILE"

##程序目录及程序文件权限控制
chmod -R 500 "${PRODUCT_PATH}"
##配置目录及配置文件权限控制
chmod 700 "${PRODUCT_PATH}"/${MODULE_PATH}/WEB-INF
chmod 700 "${PRODUCT_PATH}"/${MODULE_PATH}/WEB-INF/classes
chmod 700 "${PRODUCT_PATH}"/${MODULE_PATH}/WEB-INF/classes/config

chmod 700 "${PRODUCT_PATH}"/${MODULE_PATH}/WEB-INF/classes/config/directrouters
chmod 600 "${PRODUCT_PATH}"/${MODULE_PATH}/WEB-INF/classes/config/*.*
chmod 600 "${PRODUCT_PATH}"/${MODULE_PATH}/WEB-INF/classes/config/directrouters/*.*

chmod -R 700 ${TOMCAT_PATH}
find ${TOMCAT_PATH} -type f | xargs -i chmod 600 {}
find ${TOMCAT_PATH}/bin -name "*.sh" | xargs -i chmod 500 {}
find ${TOMCAT_PATH}/bin -name "*.jar" | xargs -i chmod 500 {}
find ${TOMCAT_PATH}/lib | xargs -i chmod 500 {}

chmod -R 500 ${TOMCAT_PATH}/bin

logger "Install ${MODULE_PATH} success"

exit "$RETURN_CODE_SUCCESS"
