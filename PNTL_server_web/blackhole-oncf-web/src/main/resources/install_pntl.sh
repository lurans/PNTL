#!/bin/bash

path="/root"
agent_path="/opt/huawei/ServerAntAgent"

function clear_env()
{
    if [ -f "/opt/huawei/ServerAntAgent/StopService.sh" ];then
        cd $agent_path
        sh -x StopService.sh
    fi
	#rm -rf $agent_path
}


function install_agent()
{
    cd $path
	local tar_filename=$(ls ServerAntAgentFor*.tar.gz)
	local filename="ServerAntAgentSetup"
	local install_file="InstallService.sh"
	
	
	cd $path
	if [ ! -f "${tar_filename}" ];then
		exit 1
	fi
	
	tar -xzf $tar_filename
	cd $filename
	chmod 777 *
	sh -x env.sh
	sh -x $install_file
	
}

clear_env
install_agent