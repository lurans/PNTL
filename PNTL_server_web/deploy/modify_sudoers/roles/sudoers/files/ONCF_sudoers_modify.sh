#!/bin/bash

SUDOERS_FILE="/etc/sudoers"

func_ensure_line_in_file() # $1 file $2 line
{
FILE=$1
LINE=$2

if [ -r ${FILE} ]; then
    grep -q "$(echo -e "${LINE}")" ${FILE}; GREP_RC=$?
    if [ ${GREP_RC} != "0" ]; then
        echo -e "add LINE [${LINE}] to ${FILE}"
        echo -e "${LINE}" >>${FILE} 2>/dev/null; ECHO_RC=$?
        if [ ${ECHO_RC} != "0" ]; then
            echo -e "ERROR: Cannot write to [${FILE}]."
            exit 1
        fi
    else
        echo -e "Ok line [${LINE}] is in File [${FILE}]."
    fi
else
    echo -e "ERROR: Cannot read file [${FILE}]."
    exit 1
fi
}

func_ensure_CMD_in_file() # $1 file $2 line
{
FILE=$1
LINE=$2
HEAD=$(echo -e $2 | cut -d= -f1)
CMD=$(echo -e $2 | cut -d= -f2)

if [ -r ${FILE} ]; then
    grep -q "^$(echo -e "${HEAD}")" ${FILE}; GREP_RC=$?
    if [ ${GREP_RC} != "0" ]; then
        echo -e "add LINE [${LINE}] to ${FILE}"
        echo -e "${LINE}" >>${FILE} 2>/dev/null; ECHO_RC=$?
        if [ ${ECHO_RC} != "0" ]; then
            echo -e "ERROR: Cannot write to [${FILE}]."
            exit 1
        fi
    else
        echo -e "Ok line [${HEAD}] is in File [${FILE}]."
        CMD_OLD=$(grep "^$(echo -e "${HEAD}")" ${FILE} | cut -d= -f2)

        arr=("\/bin\/ip" "\/home\/GalaX8800\/blackhole\/chkflow\/tcpdump_*" "\/home\/fsp\/blackhole\/chkflow\/tcpdump_*" "\/usr\/bin\/ovs-dpctl" "\/usr\/bin\/timeout" "\/usr\/bin\/ovs-vsctl" "\/usr\/bin\/ovs-ofctl")

        echo ${CMD_OLD} | sed -e 's/,/\n/g' 1> cmd

        for value in ${arr[*]}
        do
            sed -i '/'${value}'/d' cmd
        done

        CMD_OLD=$(cat cmd | tr "\n" "," | sed -e 's/,$//g')
        rm -rf cmd

        if [ -z ${CMD_OLD} ]; then
            CMD_NEW=$(echo ${CMD} | sed -e 's/,/\n/g' | sort -u | tr "\n" "," | sed -e 's/,$//g')
        else
            CMD_NEW=$(echo ${CMD},${CMD_OLD} | sed -e 's/,/\n/g' | sort -u | tr "\n" "," | sed -e 's/,$//g')
        fi

        echo -e "add CMD [${CMD_NEW}] to ${FILE}"
        sed -i "s#${HEAD}.*#${HEAD}=${CMD_NEW}#" ${FILE}; SED_RC=$?
        if [ ${SED_RC} != "0" ]; then
            echo -e "ERROR: Cannot write to [${FILE}]."
            exit 1
        fi
    fi
else
    echo -e "ERROR: Cannot read file [${FILE}]."
    exit 1
fi
}

if [ $(whoami) != "root" ]; then
    echo -e "ERROR: Please execute as user root.";
    exit 1
fi

if [ $(grep -q gandalf /etc/passwd; echo $?) -eq 0 ]; then
    USER_NAME="GALAX"
    user_name="gandalf"
    cmd_list="/bin/ip netns exec qrouter-???????????????????????????????????? timeout [1-9] /home/oncf/tcpdump -s 60 -c [1-3]*,/bin/ip netns exec fip-???????????????????????????????????? timeout [1-9] /home/oncf/tcpdump -s 60 -c [1-3]*,/usr/bin/timeout [1-9] /home/oncf/tcpdump -s 60 -c [1-3]*,/usr/bin/ovs-dpctl dump-flows"
elif [ $(grep -q fsp /etc/passwd; echo $?) -eq 0 ]; then
    USER_NAME="FSP"
    user_name="fsp"
    cmd_list="/usr/bin/timeout [1-9] /home/oncf/tcpdump -s 60 -c [1-3]*,/usr/bin/ovs-dpctl dump-flows,/usr/bin/ovs-vsctl list-ports br-bond,/usr/bin/ovs-vsctl list-br"
else
    echo -e "ERROR: This script can only run on FSP or CNA node."
    exit 1
fi

func_ensure_line_in_file ${SUDOERS_FILE} "User_Alias ${USER_NAME}=${user_name}"
func_ensure_line_in_file ${SUDOERS_FILE} "Host_Alias HOSTS=ALL"
func_ensure_line_in_file ${SUDOERS_FILE} "Runas_Alias OP=root"
func_ensure_CMD_in_file ${SUDOERS_FILE} "Cmnd_Alias CMD=${cmd_list}"
func_ensure_line_in_file ${SUDOERS_FILE} "${USER_NAME} HOSTS=(OP) NOPASSWD:CMD"