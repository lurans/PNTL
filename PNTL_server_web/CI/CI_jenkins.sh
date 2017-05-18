#!/bin/sh

function main(){

    CURRENTDIR=$(cd `dirname $0`; pwd)
    RepositoriesDir=/ci/Repositories/com/huawei/blackhole

    releaseNote=${CURRENTDIR}/../ReleaseNote/config.txt
    if [ -f ${releaseNote} ];then
        VERSION=$(grep "releaseVersion=" ${releaseNote} | awk -F '=' {'print $2'}| tr -d '\n\r')
    fi


    console="${RepositoriesDir}/blackhole-oncf-web/${VERSION}/blackhole-oncf-web-${VERSION}.war"
    cp ${console} ./oncf.war

	cd ../ReleaseNote && python generate_release_json.py

	cd ../CI && mv  ../ReleaseNote/*.json ./

	#package.sh 
	sh CI_package.sh -oncf
	sh CI_package.sh -playbook
	sh CI_package.sh -all
	rm -rf *.war *.tar.gz *.json
	}

main
exit 0
