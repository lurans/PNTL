package com.huawei.blackhole.network.common.constants;

/**
 * All key in configuration file should place here.
 */
public class Config {
    public static final String OPERATION_LOG = "api_operation_log";

    public static final String FSP_ADMIN_TENANT_NAME = "OS_TENANT_NAME";
    public static final String FSP_ADMIN_USER_NAME = "OS_USERNAME";
    public static final String FSP_ADMIN_PASSWORD = "OS_PASSWORD";
    public static final String FSP_ADMIN_AUTH_URL = "OS_AUTH_URL";
    public static final String FSP_CASCADING_REGION = "OS_REGION_NAME";
    public static final String FSP_CASCADIND_IP = "cascading_ip";

    public static final String FSP_KEY_CASCADIND_IP = "cascading_ip";
    public static final String FSP_KEY_OS_PASSWORD = "OS_PASSWORD";
    public static final String FSP_KEY_CNA_SSH_KEY = "cna_ssh_key";
    public static final String FSP_KEY_OS_AUTH_URL = "OS_AUTH_URL";
    public static final String FSP_KEY_OS_TENANT_NAME = "OS_TENANT_NAME";
    public static final String FSP_KEY_OS_USERNAME = "OS_USERNAME";

    // SSH Common
    private static final String SSH_POSTFIX_IP = "_host";
    private static final String SSH_POSTFIX_USER = "_user";
    private static final String SSH_POSTFIX_PASSWORD = "_passwd";
    private static final String SSH_POSTFIX_KEY_FILE = "_ssh_key";
    // CNA
    private static final String SSH_PREFIX_CNA = "cna";
    public static final String SSH_USER_CNA = SSH_PREFIX_CNA + SSH_POSTFIX_USER;
    public static final String SSH_PASSWORD_CNA = SSH_PREFIX_CNA + SSH_POSTFIX_PASSWORD;
    public static final String SSH_KEY_NAME_CNA = SSH_PREFIX_CNA + SSH_POSTFIX_KEY_FILE;
    // NGFW
    private static final String SSH_PREFIX_NGFW = "ngfw";
    public static final String HOST_NGFW = SSH_PREFIX_NGFW + SSH_POSTFIX_IP;
    public static final String SSH_USER_NGFW = SSH_PREFIX_NGFW + SSH_POSTFIX_USER;
    public static final String SSH_PASSWORD_NGFW = SSH_PREFIX_NGFW + SSH_POSTFIX_PASSWORD;
    public static final String SSH_KEY_NAME_NGFW = SSH_PREFIX_CNA + SSH_POSTFIX_KEY_FILE;
    // public static final String SSH_KEY_NAME_NGFW = SSH_PREFIX_NGFW +
    // SSH_POSTFIX_KEY_FILE;
    // L2GW
    private static final String SSH_PREFIX_L2GW = "l2gw";
    public static final String SSH_USER_L2GW = SSH_PREFIX_L2GW + SSH_POSTFIX_USER;
    public static final String SSH_PASSWORD_L2GW = SSH_PREFIX_L2GW + SSH_POSTFIX_PASSWORD;
    public static final String SSH_KEY_NAME_L2GW = SSH_PREFIX_CNA + SSH_POSTFIX_KEY_FILE;
    // RouterForwarder
    private static final String SSH_PREFIX_RF = "rf";
    public static final String SSH_USER_ROUTERFORWARDER = SSH_PREFIX_RF + SSH_POSTFIX_USER;
    public static final String SSH_PASSWORD_ROUTERFORWARDER = SSH_PREFIX_RF + SSH_POSTFIX_PASSWORD;
    public static final String SSH_KEY_NAME_ROUTERFORWARDER = SSH_PREFIX_CNA + SSH_POSTFIX_KEY_FILE;

    private static final String SSH_PREFIX_SNAT = "snat";
    public static final String SSH_USER_SNAT = SSH_PREFIX_SNAT + SSH_POSTFIX_USER;
    public static final String SSH_PASSWORD_SNAT = SSH_PREFIX_SNAT + SSH_POSTFIX_PASSWORD;
    public static final String SSH_KEY_NAME_SNAT = SSH_PREFIX_CNA + SSH_POSTFIX_KEY_FILE;
}
