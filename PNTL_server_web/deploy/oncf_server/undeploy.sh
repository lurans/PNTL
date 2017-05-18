#!/bin/sh
ansible-playbook -i hosts undeploy.yml -k -K
