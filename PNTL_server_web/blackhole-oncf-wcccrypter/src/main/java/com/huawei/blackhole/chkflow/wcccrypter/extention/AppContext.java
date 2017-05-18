package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AppContext {
    private static final ConcurrentHashMap<String, Object> TABLE = new ConcurrentHashMap<String, Object>();
    private static AppContext instance = new AppContext();

    public void setAppHomePath(String homePath) {
        bind("beetle.application.home.path", homePath);
    }

    public String getAppHomePathDefineFromContext() {
        return (String) lookup("beetle.application.home.path");
    }

    String getAppHome() {
        String fp = System.getProperty("beetle.application.home.path");
        if ((fp != null) && (fp.trim().length() > 0)) {
            if (!fp.endsWith("/")) {
                fp = fp + "/";
            }
            return fp;
        }
        String ap = (String) getInstance().lookup("beetle.application.home.path");
        if ((ap != null) && (ap.trim().length() > 0)) {
            if (!ap.endsWith("/")) {
                ap = ap + "/";
            }
            return ap;
        }
        return "config/";
    }

    public Enumeration<String> getContextKeys() {
        return TABLE.keys();
    }

    public void bind(String name, Object obj) {
        TABLE.put(name, obj);
    }

    public void close() {
        if (!TABLE.isEmpty()) {
            TABLE.clear();
        }
    }

    public Map<String, Object> getEnvironment() {
        return TABLE;
    }

    public Object lookup(String name) {
        return TABLE.get(name);
    }

    public void rebind(String name, Object obj) {
        if (TABLE.containsKey(name)) {
            TABLE.remove(name);
        }
        TABLE.put(name, obj);
    }

    public void unbind(String name) {
        if (TABLE.containsKey(name)) {
            TABLE.remove(name);
        }
    }

    public static AppContext getInstance() {
        return instance;
    }
}
