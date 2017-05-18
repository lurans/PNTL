#!/bin/sh
source /etc/profile

## modify limits settings of admin 
function modifyLimitsConf()
{
    limitsFile=/etc/security/limits.conf
    sed -i.bak '/#LIMITS_START/,/#LIMITS_END/d' $limitsFile 
cat <<-EOF >> $limitsFile
#LIMITS_START
{{ user_name }} soft nofile 65535
{{ user_name }} hard nofile 65535
#LIMITS_END
EOF
}


function modifyServiceUserUmask()
{
    serviceRcFile=/home/{{ user_name }}/.bashrc
    
    isExist=$(grep "umask 027" ${serviceRcFile})
    if [ -z "$isExist" ];then
        echo "umask 027" >> $serviceRcFile
    fi
}

modifyLimitsConf
modifyServiceUserUmask

