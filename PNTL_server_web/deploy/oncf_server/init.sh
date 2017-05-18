#!/bin/sh
ansible-playbook -vv -i hosts init.yml -k --become-user=root --ask-become-pass --become-method=su
