package com.huawei.blackhole.network.common.constants;

public class PntlInfo {
    //public static final String URL_IP = "https://cas.lf.hwclouds.com:8243";//beta
    public static final String URL_IP = "https://8.15.4.96:8243";//beta
    //public static final String URL_IP = "https://192.168.211.98:8243";//alpha

    public static final String CMDB_URL_SUFFIX = "/v1/cloud-cmdb/hosts";

    public static final String SCRIPT_SEND_URL_SUFFIX = "/CloudAgent/v1/api/cmdset/scriptSend";

    public static final String CMD_SET_URL_SUFFIX = "/CloudAgent/v1/api/cmdset";

    public static final String AGENT_INFO_BY_IP = "/CloudAgent/v1/api/info/agent/by/ip";

    public static final String DFS_URL_SUFFIX = "/dfs/upload/sync";

    public static final String TOKEN_URL_SUFFIX = "/token";
    //this is beta environment's address

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String X_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String AUTH = "Authorization";
    public static final String GRANT_TYPE = "grant_type";
    public static final int    MONITOR_INTERVAL_TIME_NEWEST = 5*60;//second
    public static final int    MONITOR_INTERVAL_TIME_HISTORY = 7*24*60*12;//seven days
    public static final int    NOTIFY_AGENT_TO_GET_PINGLIST_TIME = 3;

    public static final String USERNAME = "user_name";
    public static final String SERVICENAME = "service_name";
    public static final String BEARER = "Bearer";
    public static final String OPS_USERNAME = "c00000000";
    public static final String PNTL_SERVICENAME = "pntl";
    public static final String PNTL_ROOT_NAME = "root";
    public static final String PTNL_UPLOADER_SPACE = "c00000000";

    public static final String AGENT_EULER = "ServerAntAgentForEuler.tar.gz";
    public static final String AGENT_SUSE  = "ServerAntAgentForSles.tar.gz";
    public static final String AGENT_INSTALL_FILENAME = "install_pntl.sh";
    public static final String PNTL_CONF = "pntlConfig.yml";

    public static final String AGENT_CONF = "agentConfig.cfg";
    public static final String PNTL_IPLIST_CONF = "ipList.yml";
    public static final String PNTL_UPDATE_IPLIST_CONFIG = "updateIpList.yml";
    public static final String PNTL_UPDATE_TYPE_ADD = "add";
    public static final String PNTL_UPDATE_TYPE_DEL = "del";

    public static final String PNTL_AGENT_STATUS_SUCC = "success";
    public static final String PNTL_AGENT_STATUS_FAIL = "fail";

    public static final String OS_SUSE = "SUSE";
    public static final String OS_EULER = "EULER";
}
