package com.huawei.blackhole.network.common.constants;

public class PntlInfo {
    public static final String URL_IP = "https://8.15.4.96:8243";//beta
    //public static final String URL_IP = "https://192.168.211.98:8243";//alpha

    public static final String CMDB_URL_SUFFIX = "/v1/cloud-cmdb/hosts";

    public static final String SCRIPT_SEND_URL_SUFFIX = "/CloudAgent/v1/api/cmdset/scriptSend";

    public static final String AGENT_LOG_URL_SUFFIX = "/CloudAgent/v1/api/cmdset/logSend";

    public static final String CMD_SET_URL_SUFFIX = "/CloudAgent/v1/api/cmdset";

    public static final String AGENT_INFO_BY_IP = "/CloudAgent/v1/api/info/agent/by/ip";

    public static final String TOKEN_URL_SUFFIX = "/token";
    //this is beta environment's address

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String SERVER_ANTS_ANGENT = "ServerAntsAgent";
    public static final String X_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String AUTH = "Authorization";
    public static final String GRANT_TYPE = "grant_type";
    public static final int    MONITOR_INTERVAL_TIME = 5*60;//second

}
