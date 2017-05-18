package com.huawei.blackhole.network.core.resource;

public class AppStatus {
    private static boolean started = false;

    public static void started() {
        started = true;
    }

    public static void stop() {
        started = false;
    }

    public static boolean isStarted() {
        return started;
    }
}
