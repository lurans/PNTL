#!/bin/sh

# version
CURRENT_DIR=$(cd `dirname $0`; pwd)
releaseNote=${CURRENT_DIR}/../ReleaseNote/config.txt
if [ -f ${releaseNote} ];then
    VERSION=$(grep "releaseVersion=" ${releaseNote} | awk -F '=' {'print $2'}| tr -d '\n\r')
    #VERSION="testVersion_2"
fi

# server configuration
RSYNC_PASS_PATH="/home/hws_publish/rsync-client.pass"
RSYNC_SERVER="rsync@10.106.128.79::cloud_package"

JENKINS_SERVER="http://10.106.128.81:8080/jenkins/"
JENKINS_JOB="OpsTools_Chief_Dispatcher"

# app info
APP_NAME="OpsNetworkChkFlow"
APP_CATEGORY="OpsTools"
PKG_NAME=${APP_NAME}"-"${VERSION}".zip"
CONF_NAME="ModifySudoers-2.0.0.zip"
PKG_LIST=( ${PKG_NAME} ${CONF_NAME} )

# client configuration
BASE_DIR="/home/hws_publish/"
PKG_DIR=${BASE_DIR}${APP_NAME}"/"${VERSION}
#PKG_NAME="CI_publish.sh"

##########################################################################################################
# Main work flow
##########################################################################################################
# 1. create dir for current vision
mkdir $PKG_DIR

# 2. copy package to work directory
for pkg in ${PKG_LIST[@]}
do
    cp $pkg $PKG_DIR
done

# 3. push to server
# rsync -avurR --partial --progress --delete --password-file=/home/hws_publish/rsync-client.pass {version} rsync@10.106.128.79::cloud_package/OpsTools/OpsNetworkChkFlow
cd $BASE_DIR$APP_NAME
RSYNC_SERVER_TGT=$RSYNC_SERVER"/"$APP_CATEGORY"/"$APP_NAME
rsync -avurR --partial --progress --delete --password-file=$RSYNC_PASS_PATH $VERSION $RSYNC_SERVER_TGT

# 4. trigger job to publish package to external net
# java -jar jenkins-cli.jar -s http://10.106.128.81:8080/jenkins/ build OpsTools_Chief_Dispatcher -p pack_path=OpsTools/OpsNetworkChkFlow -p pack_version=testVersion_123
cd $BASE_DIR
JENKINS_SERVER_TGT=$APP_CATEGORY"/"$APP_NAME
java -jar jenkins-cli.jar -s $JENKINS_SERVER build $JENKINS_JOB -p pack_path=$JENKINS_SERVER_TGT -p pack_version=$VERSION