#!/bin/sh

CURRENTDIR=$(cd `dirname $0`; pwd)


USER={{ user_name }}
GROUP={{ group_name }}
PASSWD=$1

if [ -z "$USER" -o -z "$GROUP" -o -z "$PASSWD" ] ; then
    echo "username or groupname or userPasswd in params file is empty"
    exit 1
fi


assertExitCode()
{
    exitCode="$1"
    action="$2"
    if [ $exitCode -ne 0 ];then
        echo "$action failed."
        echo "FAILED"
        exit 1
    fi
}

createUser()
{   
    tmp=$(grep "^${GROUP}:" /etc/group)
    if [ -z "$tmp" ];then        
        /usr/sbin/groupadd ${GROUP}
        assertExitCode $? "add new group $GROUP"
    fi
    
    tmp=$(grep "^${USER}:" /etc/passwd)
    if [ -z "$tmp" ];then
        /usr/sbin/useradd ${USER} -m -d /home/${USER} -g ${GROUP}
        assertExitCode $? "add new user $USER"

        
        echo "${PASSWD}" | passwd --stdin ${USER}
        assertExitCode $? "change user[$USER] password"
        
    fi

    /usr/sbin/usermod -G wheel ${USER}
    
}

createUser
echo "SUCCESS"