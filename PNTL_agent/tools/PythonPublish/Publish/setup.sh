#!/bin/sh

# ServerAntAgentService ��Ϣ
SAAS_SRC_TAR="ServerAntAgent.tar.gz"
SAAS_SRC_PWD="$(pwd)/ServerAntAgent"
SAAS_SRC_CFG="ServerAntAgent.cfg"

# Agent����boot��, boost�����谲װ,ֱ�ӽ�ѹ, ����ʱʹ��, ����ʱ����Ҫ. 
# ͨ��Ŀ¼���
THIRD_BOOST_NAME="boost"
THIRD_BOOST_TAR="boost_1_62_0.tar.bz2"
THIRD_BOOST_DIR="${SAAS_SRC_PWD}/third_group/boost_1_62_0"

# Agent����curl��, ������Զ��Server�ύhttp��post����, ������Ӧ��
# ͨ��pkg-config���
THIRD_CURL_NAME="libcurl"
THIRD_CURL_TAR="curl-7.50.3.tar.gz"
THIRD_CURL_DIR="${SAAS_SRC_PWD}/third_group/curl-7.50.3"

# Agent����microhttpd��, ���ڽ���Զ�˷����http����, ������Ӧ��
# ͨ��pkg-config���
THIRD_MICROHTTPD_NAME="libmicrohttpd"
THIRD_MICROHTTPD_TAR="libmicrohttpd-0.9.51.tar.gz"
THIRD_MICROHTTPD_DIR="${SAAS_SRC_PWD}/third_group/libmicrohttpd-0.9.51"

# Agent����rdkafka��, ������kafka������Ϣ.
# ͨ��pkg-config���
THIRD_RDKAFKA_NAME="rdkafka"
THIRD_RDKAFKA_TAR="librdkafka-master.tar.gz"
THIRD_RDKAFKA_DIR="${SAAS_SRC_PWD}/third_group/librdkafka-master"

# ���echo�Ƿ����
ECHO=$(which echo)

# ��ɽ�ѹ, ��װ, ���� ServerAntAgent�������.
# ɾ����Ŀ¼, ������ļ�ʱ�䲻ͬ���澯.
if [ -d "${SAAS_SRC_PWD}" ] ;then
    rm ${SAAS_SRC_PWD} -r
fi

${ECHO}  "Extract ${SAAS_SRC_TAR} in Agent ${Agent_ConnectInfo_IP} ... "
tar -xf ${SAAS_SRC_TAR}
${ECHO}  "Extract ${SAAS_SRC_TAR} success"

${ECHO}  "Entering directory ${SAAS_SRC_PWD}"
cd ${SAAS_SRC_PWD}

# ���boost�Ƿ��Ѿ���ѹ. boost���谲װ, ֻ���ѹ����, ֱ�Ӽ��Ŀ¼.
${ECHO} "Checking ${THIRD_BOOST_NAME} ..."
if [ -d "${THIRD_BOOST_DIR}" ]
then
    ${ECHO} "Found ${THIRD_BOOST_NAME}"
else
    ${ECHO} "Install ${THIRD_BOOST_NAME} start"
    ${ECHO} "Entering directory ${SAAS_SRC_PWD}/third_group"
    cd "${SAAS_SRC_PWD}/third_group"
    if test -e "./${THIRD_BOOST_TAR}" ; then
        ${ECHO} "Extract ${THIRD_BOOST_TAR} ..."
        tar -xf ${THIRD_BOOST_TAR}
        ${ECHO}  "Extract ${SAAS_SRC_TAR} success"
    else
        ${ECHO} "Error: Can not find ${THIRD_BOOST_TAR} in $(pwd)" >&2
        exit -1
    fi
    ${ECHO}  "Leaving directory ${SAAS_SRC_PWD}/third_group"
    ${ECHO} "Install ${THIRD_BOOST_NAME} success"
    cd "${SAAS_SRC_PWD}"
fi

# ���pkg-config�Ƿ����
PKG_CONFIG=$(which pkg-config)
if test "x$PKG_CONFIG" != x; then
    # ʹ��pkg-config���libcurl
    ${ECHO} "Checking  ${THIRD_CURL_NAME} ..."
    ${PKG_CONFIG} --exists ${THIRD_CURL_NAME}
    if [ 0 -eq $? ]
    then
        ${ECHO} "Found ${THIRD_CURL_NAME}"
    else
        ${ECHO} "Install ${THIRD_CURL_NAME} start"
        ${ECHO}  "Entering directory ${SAAS_SRC_PWD}/third_group"
        cd "${SAAS_SRC_PWD}/third_group"
        if test -e "./${THIRD_CURL_TAR}" ; then
            ${ECHO} "Extract ${THIRD_CURL_TAR} ..."
            tar -xf ${THIRD_CURL_TAR}
            ${ECHO}  "Extract ${THIRD_CURL_TAR} success"
            
            cd  "${THIRD_CURL_DIR}"
            ./configure
            if [ 0 -ne $? ]; then 
                ${ECHO} "Configure for  ${THIRD_CURL_TAR} Failed" >&2
                exit -1
            fi
            make
            if [ 0 -ne $? ]; then 
                ${ECHO} "Make for  ${THIRD_CURL_TAR} Failed" >&2
                exit -1
            fi
            make install
            if [ 0 -ne $? ]; then 
                ${ECHO} "Make install for  ${THIRD_CURL_TAR} Failed" >&2
                exit -1
            fi
            # ������ldconfig�Ĵ������.
            ldconfig > /dev/null 2>&1
        else
            ${ECHO} "Error: Can not find ${THIRD_CURL_TAR} in $(pwd)" >&2
            exit -1
        fi
        ${ECHO}  "Leaving directory ${SAAS_SRC_PWD}/third_group"
        ${ECHO} "Install lib ${THIRD_CURL_NAME} success"
        cd "${SAAS_SRC_PWD}"
    fi
    
    # ʹ��pkg-config���libmicrohttpd
    ${ECHO} "Checking  ${THIRD_MICROHTTPD_NAME} ..."
    ${PKG_CONFIG} --exists ${THIRD_MICROHTTPD_NAME}
    if [ 0 -eq $? ] 
    then
        ${ECHO} "Found ${THIRD_MICROHTTPD_NAME}"
    else
        ${ECHO} "Install ${THIRD_MICROHTTPD_NAME} start"
        ${ECHO}  "Entering directory ${SAAS_SRC_PWD}/third_group"
        cd "${SAAS_SRC_PWD}/third_group"
        if test -e "./${THIRD_MICROHTTPD_TAR}" ; then
            ${ECHO} "Extract ${THIRD_MICROHTTPD_TAR} ..."
            tar -xf ${THIRD_MICROHTTPD_TAR}
            ${ECHO}  "Extract ${THIRD_MICROHTTPD_TAR} success"
            
            cd  "${THIRD_MICROHTTPD_DIR}"
            ./configure
            if [ 0 -ne $? ]; then 
                ${ECHO} "Configure for  ${THIRD_MICROHTTPD_DIR} Failed" >&2
                exit -1
            fi
            make
            if [ 0 -ne $? ]; then 
                ${ECHO} "Make for  ${THIRD_MICROHTTPD_DIR} Failed" >&2
                exit -1
            fi
            make install
            if [ 0 -ne $? ]; then 
                ${ECHO} "Make install for  ${THIRD_MICROHTTPD_DIR} Failed" >&2
                exit -1
            fi
            # ������ldconfig�Ĵ������.
            ldconfig > /dev/null 2>&1
        else
            ${ECHO} "Error: Can not find ${THIRD_MICROHTTPD_TAR} in $(pwd)" >&2
            exit -1
        fi
        ${ECHO}  "Leaving directory ${SAAS_SRC_PWD}/third_group"
        ${ECHO} "Install lib ${THIRD_MICROHTTPD_NAME} success"
        cd "${SAAS_SRC_PWD}"
    fi
    
    # ʹ��pkg-config���rdkafka
    ${ECHO} "Checking  ${THIRD_RDKAFKA_NAME} ..."
    ${PKG_CONFIG} --exists ${THIRD_RDKAFKA_NAME}
    if [ 0 -eq $? ] 
    then
        ${ECHO} "Found ${THIRD_RDKAFKA_NAME}"
    else
        ${ECHO} "Install ${THIRD_RDKAFKA_NAME} start"
        ${ECHO}  "Entering directory ${SAAS_SRC_PWD}/third_group"
        cd "${SAAS_SRC_PWD}/third_group"
        if test -e "./${THIRD_RDKAFKA_TAR}" ; then
            ${ECHO} "Extract ${THIRD_RDKAFKA_TAR} ..."
            tar -xf ${THIRD_RDKAFKA_TAR}
            ${ECHO}  "Extract ${THIRD_RDKAFKA_TAR} success"
            
            cd  "${THIRD_RDKAFKA_DIR}"
            ./configure
            if [ 0 -ne $? ]; then 
                ${ECHO} "Configure for  ${THIRD_RDKAFKA_DIR} Failed" >&2
                exit -1
            fi
            make
            if [ 0 -ne $? ]; then 
                ${ECHO} "Make for  ${THIRD_RDKAFKA_DIR} Failed" >&2
                exit -1
            fi
            make install
            if [ 0 -ne $? ]; then 
                ${ECHO} "Make install for  ${THIRD_RDKAFKA_DIR} Failed" >&2
                exit -1
            fi
            # ������ldconfig�Ĵ������.
            ldconfig > /dev/null 2>&1
        else
            ${ECHO} "Error: Can not find ${THIRD_RDKAFKA_TAR} in $(pwd)" >&2
            exit -1
        fi
        ${ECHO}  "Leaving directory ${SAAS_SRC_PWD}/third_group"
        ${ECHO} "Install lib ${THIRD_RDKAFKA_NAME} success"
        cd "${SAAS_SRC_PWD}"
    fi
fi

${ECHO} "Checking  ${SAAS_SRC_CFG} ..."
if [ -e ${SAAS_SRC_CFG} ]
then
    ${ECHO} "Found ${SAAS_SRC_CFG}"
    ${ECHO} "Update AgentIP in ${SAAS_SRC_CFG} to ${Agent_ConnectInfo_IP}"
    
    # Agent IPĬ����0.0.0.0��ʾ, �˴��滻��Agent��ʵ�ʹ���IP
    sed -i "s/0.0.0.0/${Agent_ConnectInfo_IP}/g" ${SAAS_SRC_CFG}
    ${ECHO} "Update ${SAAS_SRC_CFG} success"
else
    ${ECHO} "Error: Can not find ${SAAS_SRC_CFG} in $(pwd)" >&2
    exit -1
fi


${ECHO} "Start Compile ServerAntAgent"

make clean
if [ 0 -ne $? ]; then 
    ${ECHO} "Make clean for ServerAntAgent Failed" >&2
    exit -1
fi

make
if [ 0 -ne $? ]; then 
    ${ECHO} "Make for ServerAntAgent Failed" >&2
    exit -1
fi

${ECHO} "Start Install ServerAntAgent"
# paramikoԶ������service, �����ʽ��ӡ�ᵼ��paramiko����
# ������־������־�ļ�, ������־�������ظ�paramiko, Manager�˸����Ƿ���ڴ�����־�ж�ִ���Ƿ�ɹ�
make install > install.log
if [ 0 -eq $? ]
then 
    ${ECHO} "Make install for ServerAntAgent success" >&1
    exit 0
else
    ${ECHO} "Make install for ServerAntAgent failed! Check the install.log" >&2
    exit -1
fi
