#!/bin/sh

# version
CURRENT_DIR=$(cd `dirname $0`; pwd)
releaseNote=${CURRENT_DIR}/../ReleaseNote/config.txt
if [ -f ${releaseNote} ];then
    VERSION=$(grep "releaseVersion=" ${releaseNote} | awk -F '=' {'print $2'}| tr -d '\n\r')
fi

# Product name and version
OPS_TOOLS_VERSION="Public Cloud Solution "$PublicCloudSolutionVersion

# Component item code
SOFT_CODE="BINA21000V15"
# Component packages
APP_NAME="OpsNetworkChkFlow-"${VERSION}".zip"
CONF_NAME="ModifySudoers-2.0.0.zip"

# com tool location
COM_PATH="/home/com"
# Component work directory
SOFT_PATH=${COM_PATH}"/oncf"

echo
echo
echo "#######################"
echo ${OPS_TOOLS_VERSION}
echo ${APP_NAME}
echo ${CONF_NAME}
echo "#######################"
echo
echo

# 1. Clean work directory before work
rm ${SOFT_PATH}/*

# 2. Copy component packages to work directory
cp ${APP_NAME} ${SOFT_PATH}
cp ${CONF_NAME} ${SOFT_PATH}
ls -l ${SOFT_PATH}

# 3. Go to com tool directory
cd ${COM_PATH}
pwd

# 4. Call com tool
./com_linux.exe fetch -o ${SOFT_PATH}/template.xml -i ${SOFT_CODE} -w "${OPS_TOOLS_VERSION}"
./com_linux.exe genrev -o ${SOFT_PATH}/template_rev.xml -i ${SOFT_PATH}/template.xml -s ${VERSION} -p ${COM_PATH} -r ${VERSION}
./com_linux.exe archive -o ${SOFT_PATH}/archive.xml -i ${SOFT_PATH}/template_rev.xml

./com_linux.exe upbin -d ${SOFT_PATH}/${APP_NAME} -i ${SOFT_PATH}/archive.xml
./com_linux.exe upbin -d ${SOFT_PATH}/${CONF_NAME} -i ${SOFT_PATH}/archive.xml
