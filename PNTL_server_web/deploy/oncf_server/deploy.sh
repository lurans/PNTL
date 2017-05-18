#!/bin/sh
ansible-playbook -i hosts install.yml --tags 'deploy' -k -K