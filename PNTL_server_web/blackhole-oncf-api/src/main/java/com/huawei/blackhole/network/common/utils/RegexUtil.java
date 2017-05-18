package com.huawei.blackhole.network.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class RegexUtil {

    public static boolean isIp(String localIp) {
        if (localIp == null || localIp.length() < 7 || localIp.length() > 15 || "".equals(localIp)) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(localIp);
        return mat.matches();
    }

    public static boolean isPort(int port) {
        return 0 < port && port < 65536;
    }

    // https://aa.bb.com:443
    public static boolean validAuthUrl(String authUrl) {
        if (StringUtils.isEmpty(authUrl)) {
            return false;
        }
        Pattern pattern = Pattern.compile("([a-zA-Z]+)://(.+):([0-9]+)");
        return pattern.matcher(authUrl).matches();
    }

}
