#!/bin/sh
ansible-playbook -i hosts reset.yml -k --become-user=root --ask-become-pass --become-method=su