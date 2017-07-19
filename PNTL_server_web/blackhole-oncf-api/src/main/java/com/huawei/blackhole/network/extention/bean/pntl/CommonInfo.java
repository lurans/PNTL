package com.huawei.blackhole.network.extention.bean.pntl;


public class CommonInfo {
    private static String repoUrl;

    private static int reportPeriod;

    private static String getPingList;

    public static int getReportPeriod() {
        return reportPeriod;
    }

    public static void setReportPeriod(int reportPeriod) {
        CommonInfo.reportPeriod = reportPeriod;
    }

    public static String getRepoUrl() {
        return repoUrl;
    }

    public static String getGetPingList() {
        return getPingList;
    }

    public static void setGetPingList(String getPingList) {
        CommonInfo.getPingList = getPingList;
    }

    public static void setRepoUrl(String repoUrl)
    {
        CommonInfo.repoUrl = repoUrl;
    }
}
