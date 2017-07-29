package com.huawei.blackhole.network.extention.bean.pntl;


public class CommonInfo {
    private static String repoUrl;

    private static int reportPeriod;

    private static String kafkaIp;

    private static String topic;

    public static int getReportPeriod() {
        return reportPeriod;
    }

    public static void setReportPeriod(int reportPeriod) {
        CommonInfo.reportPeriod = reportPeriod;
    }

    public static String getRepoUrl() {
        return repoUrl;
    }

    public static void setRepoUrl(String repoUrl)
    {
        CommonInfo.repoUrl = repoUrl;
    }

    public static String getKafkaIp() {
        return kafkaIp;
    }

    public static void setKafkaIp(String kafkaIp) {
        CommonInfo.kafkaIp = kafkaIp;
    }

    public static String getTopic() {
        return topic;
    }

    public static void setTopic(String topic) {
        CommonInfo.topic = topic;
    }
}
