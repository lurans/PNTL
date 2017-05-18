#!/bin/sh
source /etc/profile

## modify service user's umask
function modifyServiceUserUmask()
{
    serviceRcFile=/home/{{ user_name }}/.bashrc
    
    isExist=$(grep "umask 027" ${serviceRcFile})
    if [ -z "$isExist" ];then
        echo "umask 027" >> $serviceRcFile
    fi
}

modifyServiceUserUmask

