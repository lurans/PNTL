#!/bin/sh
ansible-playbook -i hosts modify.yml -k --become-user=root --ask-become-pass --become-method=su