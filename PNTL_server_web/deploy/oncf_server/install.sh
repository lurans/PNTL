#!/bin/sh
ansible-playbook -i hosts install.yml --tags $1 -k -K