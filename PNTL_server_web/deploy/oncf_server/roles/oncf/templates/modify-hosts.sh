#!/bin/sh
source /etc/profile

## modify /etc/hosts
function modifyHostsFile()
{
    hosts_file='/etc/hosts'
    sed -i.bak '/#IAM_START/,/#IAM_END/d' $hosts_file 
cat <<-EOF >> $hosts_file
#FSP_START
{{ cinder_ip }} {{ cinder_site }}
#FSP_END
EOF
}

modifyHostsFile