#!/bin/sh

source /etc/profile

PORT={{ service_port }}
OUTPUT_LOG_FILE=${LOG_PATH}/{{ tool_name }}-cmd.log
FLAG_FILE={{ install_path }}/upgrade_rollback_stop_api_flag
SCRIPT={{ install_path }}/{{ service_name }}/{{ tool_name }}.sh

isPortExist=$(netstat -nlt | grep $PORT)
if [ -z "$isPortExist" ];then
    if [ ! -e $FLAG_FILE ];then
        echo "{{ service_name }} service is down, need start" 
        sh $SCRIPT restart
    fi
fi



