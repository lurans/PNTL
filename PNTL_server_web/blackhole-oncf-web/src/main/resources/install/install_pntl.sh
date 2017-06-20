#!/bin/bash

path="/home/GalaX8800"
agent_path="/opt/huawei/ServerAntAgent"

function clear_env()
{
	rm -rf $agent_path
}


function install_agent()
{
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

function install_script()
{
	local script_filename="pntl.py"
	
	cd $agent_path
	cp -f $path/$script_filename $agent_path
	chmod 777 $script_filename
	
}

clear_env
install_agent
#install_script