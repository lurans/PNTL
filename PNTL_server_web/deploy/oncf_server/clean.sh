#!/bin/sh
ansible-playbook -i hosts undeploy.yml -k -K
if [ $? -ne 0 ];then
    exit 1
fi
ansible-playbook -i hosts clean.yml -k -K
