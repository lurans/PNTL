#!/bin/sh

CURRENTDIR=$(cd `dirname $0`; pwd)
TYPE="-oncf"
usage="Usage: sh $0 [-oncf|-playbook|-all]"

if [ $# -lt 1 ] ; then
    echo  $usage
    exit 1
else
    TYPE=$1
fi


function exec_cmd()
{
    while [ $# -gt 0 ]
    do
        echo "exec $1"
        $1
        if [ $? -ne 0 ];then
            echo "exec $1 failed."
            exit 1
        fi
        shift
    done
}

function assertExitCode()
{
    if [ $1 -ne 0 ];then
        exit 1
    fi
}
function package_blackhole()
{

    cmd_api[1]="tar -cvzf OpsNetworkChkFlow-${VERSION}.tar.gz oncf.war"
    
    exec_cmd "${cmd_api[@]}"
}

function package_playbook()
{
    sed -i "s?pkg_app_version:.*?pkg_app_version: ${VERSION}?" ${CURRENTDIR}/../deploy/oncf_server/vars/main.yml
    mkdir -p temp/deploy
    cp -r ${CURRENTDIR}/../deploy/oncf_server/* temp/deploy/
    cd temp && tar -cvzf ../OpsNetworkChkFlow-playbook-${VERSION}.tar.gz deploy && cd ../
    rm -rf temp/deploy/*

    cp -r ${CURRENTDIR}/../deploy/modify_sudoers/* temp/deploy
    cd temp && tar -cvzf ../modify-sudoers-playbook-2.0.0.tar.gz deploy && cd ../
    rm -rf temp

    cp ${CURRENTDIR}/../deploy/modify_sudoers/modify-sudoers-2.0.0.json ./
}

function package_blackhole_zip()
{

    zip OpsNetworkChkFlow-${VERSION}.zip OpsNetworkChkFlow-${VERSION}.tar.gz OpsNetworkChkFlow-playbook-${VERSION}.tar.gz OpsNetworkChkFlow-${VERSION}-release.json
    zip ModifySudoers-2.0.0.zip modify-sudoers-playbook-2.0.0.tar.gz modify-sudoers-2.0.0.json
}

function main()
{
    releaseNote=${CURRENTDIR}/../ReleaseNote/config.txt
    if [ -f ${releaseNote} ];then
        VERSION=$(grep "releaseVersion=" ${releaseNote} | awk -F '=' {'print $2'}| tr -d '\n\r')
    fi
    
    case $TYPE in
        "-oncf" )
            package_blackhole
            ;;
        "-playbook" )
            package_playbook
            ;;
        "-all" )
            package_blackhole_zip
            ;;
        * )
            echo $usage
            exit 1
            ;;
    esac
}

main
exit 0
