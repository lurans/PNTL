package com.huawei.blackhole.network.common.constants;

public class Constants {
    public static final int RESULT_SERVICE_MAX_SIZE = 10; // 结果最大维护记录数量

    public static final long RECORD_OVERTIME = 7 * 60 * 1000; // 7 min

    public static final long SEARCH_INTERVAL = 5 * 1000; // search every 5 s
    // search overtime 7 min
    public static final long SEARCH_OVERTIME = 7 * 60 * 1000; // 7 min
    // search number
    public static final long SEARCH_NUMBER = SEARCH_OVERTIME / SEARCH_INTERVAL;
    // 5min
    public static final long SUBMIT_OVERTIME = 5 * 60 * 1000;
    // wait submit time
    public static final long SUBMIT_NUMBER = SUBMIT_OVERTIME / SEARCH_INTERVAL;

    public static final long KEY_FILE_MAX_SIZE = 1000 * 1000; // 1Mb

    public static final String WEBAPP = "webapp";

    public static final String ROUTER_DEVICE_OWNER = "network:router_interface_distributed";

    public static final String INTERFACE_PUBLIC = "public";

    public static final String FC_NOVA_COMPUTE = "fc-nova-compute";

    public static final String HOST_FILE_PATH = "/etc/hosts";

    public static final String SYMBOL_COMMA = ",";

    public static final String SYMBOL_DOT = ".";

    public static final String SYMBOL_COLON = ":";

    public static final int NUM_ONE = 1;

    public static final int NUM_TWO = 2;

    public static final int NUM_THREE = 3;

    public static final String VM_STATUS_ACTIVE = "ACTIVE";

    public static final String RESULT_SUCCESS = "success";

    public static final String RESULT_FAILED = "failded";

    public static final String FORM_FILE = "X-File";

    public static final String PARAM_IP = "ip";

    public static final String PARAM_USER = "user";

    public static final String PARAM_PASSWORD = "password";

    public static final String INSTANCE_HASTATUS_ACTIVE = "active";

    public static final String NETWORK_NAME_EXTERNAL_OM = "external_om";

    public static final String HTTP_PREFIX = "http://";

    public static final String VRM_PORT = "7070";

    public static final String SSO_SSO_TAG = "sso_sso_url_tag";

    public static final String SSO_LOCAL_TAG = "sso_local_url_tag";

    public static final String REPLACE_STR_SH = "replace-str.sh";

    public static final String SSO_WEB_XML = "sso-web.xml";

    public static final String NO_SSO_WEB_XML = "no-sso-web.xml";

    public static final String WEB_XML = "web.xml";

    public static final String VPN_PORT_SG_DEVICE_OWNER = "network:router_centralized_snat";

    public static final String NODE_FLAG_INPUT = "input";

    public static final String NODE_FLAG_OUTPUT = "output";

    public static final String TEMPLATE_VROUTER = "neutron-vrouter01";
    public static final String TEMPLATE_L2GW = "neutron-l2-gateway-agent";
    public static final String TEMPLATE_SNAT = "neutron-l3-nat-agent01";

    public static final String KEY_STATUS_SUBMIT = "submit";
    public static final String KEY_STATUS_VERIFY = "verify";

    public static final String TRUE = "true";
    public static final String FALSE = "false";

}
