#!/bin/sh
ansible-playbook -i hosts rollback.yml -k -K
