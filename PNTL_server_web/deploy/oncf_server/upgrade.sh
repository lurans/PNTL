#!/bin/sh
ansible-playbook -i hosts backup.yml -k -K
if [ $? -ne 0 ];then
    exit 1
fi
ansible-playbook -i hosts install.yml --tags 'deploy' -k -K
