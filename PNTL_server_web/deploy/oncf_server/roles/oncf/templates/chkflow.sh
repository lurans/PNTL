#!/bin/sh

source /etc/profile
source ~/.bashrc

LOG_PATH=/var/log/{{ install_name }}/

chkflow_GREP=${TOMCAT_HOME}/bin/bootstrap.jar
TOMCAT_BIN=${TOMCAT_HOME}/bin
chkflow_PID_FILE={{ install_path }}/{{ service_name }}/{{ tool_name }}.pid
FLAG_FILE={{ install_path }}/upgrade_rollback_stop_api_flag
START_FLAG={{ install_path }}/{{ service_name }}/start_flag


startExit()
{
    local exitCode=$1
    
    ## remove START_FLAG file whatever success or failed
    rm -f $START_FLAG
    
    ## remove FLAG_FILE file when success
    if [ "$exitCode" -eq 0 ];then
        rm -f $FLAG_FILE
    fi
    
    exit $exitCode
}




version()
{
    echo "{{ pkg_app_version }}"
}

start() 
{   
    if [ -e ${START_FLAG} ];then
        echo "old starting process is not finished. please try again later." 
        startExit 3
    fi
    
    touch $START_FLAG
    
    isExist=$(ps -efww | grep "${chkflow_GREP}" | grep -v grep)
    if [ -n "$isExist" ];then
        echo "chkflow is already running. Exit." 
        startExit 0
    fi
    
    /bin/sh ${TOMCAT_BIN}/startup.sh > /dev/null 2>&1

    num=0
    MAXTIME=15
    flag=0
    while [ $num -lt $MAXTIME ]
    do
        sleep 2
        isExist=$(ps -efww | grep "${chkflow_GREP}" | grep -v grep)
        if [ -n "$isExist" ]; then
            echo "start chkflow succeed." 
            flag=1
            break
        else
            echo "start chkflow failed for $num times." 
            num=$((num + 1))
        fi
    done
    
    if [ $flag -ne 1 ];then
        echo "start chkflow failed" 
        startExit 1
    else
        startExit 0
    fi 
}

stop() 
{
  
    num=0
    MAXTIME=12
    flag=0
    while [ $num -lt $MAXTIME ]
    do
        id=$(ps -efww | grep "${chkflow_GREP}" | grep -v grep | awk '{print $2}')
        if [ -n "$id" ];then
            kill $id
        fi
        
        sleep 1
        isExist=$(ps -efww | grep "${chkflow_GREP}" | grep -v grep)
        if [ -z "$isExist" ]; then
            flag=1
            break
        else
            echo "stop chkflow failed for $num times." 
            num=$((num + 1))
            sleep 5
        fi
    done
    
    if [ $flag -ne 1 ];then
        echo "stop chkflow failed." 
        return 1
    else
        echo "stop chkflow succeed." 
        return 0
    fi


}

status() 
{
    isExist=$(ps -efww | grep "${chkflow_GREP}" | grep -v grep)
    if [ -n "$isExist" ]; then
        echo "chkflow is running" 
    else
        echo "chkflow is DOWN" 
    fi
}

if [ ! -d $LOG_PATH ];then
    mkdir -p $LOG_PATH
fi

user=$(whoami)
if [ x"$user" == x"root" ]; then
    echo -e "this script can not run by root, please run this script by normal user."
    exit 1
fi

#time=$(date "+%F %H:%M:%S")
#echo "[$time][$user] $0 $1" >> ${OUTPUT_LOG_FILE}

case "$1" in
    start)
        start
         ;;
    stop)
         stop
         ;;
    restart)
         stop
         start
         ;;
    status)
         status
         ;;
    version)
         version
         ;;
    *)
         echo "Usage: $0 {start|stop|restart|status|version}"
         exit 1
esac
