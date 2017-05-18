#!/bin/bash

if [ $# -ne 1 ]; then
    echo -e "usage: sh $0 [package_version]"
    echo -e "for example: sh $0 0.1.6"
    exit 1
fi

version=$1
package_list="chkflow-console.war chkflow-server.war"
jre_file="server-jre-8u77-linux-x64.tar.gz"
tomcat_file="apache-tomcat-7.0.68.tar.gz"
target_dir="OpsNetworkChkFlow-${version}"
target_file="${target_dir}.zip"

for file in ${jre_file} ${tomcat_file} ${package_list[*]} install.sh install.ini
do
    if [ ! -f ${file} ]; then
        echo -e ${file} not exist.
        exit 1
    fi
done

dos2unix install.*
chmod +x install.sh
sed -i 's/=.*/=""/g' install.ini
echo ${version} > version.log
rm -rf ${target_dir} ${target_file}
mkdir ${target_dir}
mv ${jre_file} ${tomcat_file} ${package_list[*]} install.sh install.ini version.log ${target_dir}
#tar czvf ${target_file} ${target_dir}
zip -r ${target_file} ${target_dir}
if [ $? -ne 0 ]; then
    echo -e "package failed."
    exit 1
fi
mv ${target_dir}/* .
rm -rf ${target_dir}
echo -e "\npackage successfully. your package file is ${target_file}"
