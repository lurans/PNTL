package org.wcc.framework;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述应用上下文的类
 */
public final class AppContext {
    private static final ConcurrentHashMap<String, Object> TABLE = new ConcurrentHashMap<String, Object>();

    private static AppContext instance = new AppContext();

    private static final String APP_HOME_PATH = "beetle.application.home.path";

    private AppContext() {
        // TABLE = new Hashtable();
    }

    /**
     * 设置应用根路径
     * 
     * @param homePath
     *            --目标路径
     */
    public void setAppHomePath(String homePath) {
        bind(APP_HOME_PATH, homePath);
    }

    public String getAppHomePathDefineFromContext() {
        return (String) lookup(APP_HOME_PATH);
    }

    String getAppHome() {
        String fp = System.getProperty(APP_HOME_PATH);
        if (fp != null && fp.trim().length() > 0) {
            if (!fp.endsWith("/")) {
                fp = fp + "/";
            }
            return fp;
        } else {
            String ap = (String) AppContext.getInstance().lookup(APP_HOME_PATH);
            if (ap != null && ap.trim().length() > 0) {
                if (!ap.endsWith("/")) {
                    ap = ap + "/";
                }
                return ap;
            }
        }
        return "config/";
    }

    public Enumeration<String> getContextKeys() {
        return TABLE.keys();
    }

    /**
     * 绑定应用路径
     * 
     * @param name
     *            --表示应用路径的key
     * @param obj
     *            --路径字符串对象
     */
    public void bind(String name, Object obj) {
        TABLE.put(name, obj);
    }

    /**
     * 清空应用路径
     */
    public void close() {
        if (!TABLE.isEmpty()) {
            TABLE.clear();
        }
    }

    public Map<String, Object> getEnvironment() {
        return TABLE;
    }

    /**
     * 获取应用的路径
     * 
     * @param name
     *            --表示应用的key
     * @return Object --应用路径字符串对象
     */
    public Object lookup(String name) {
        return TABLE.get(name);
    }

    /**
     * 重绑定应用路径
     * 
     * @param name
     *            --表示应用的key
     * @param obj
     *            --应用路径字符串对象
     */
    public void rebind(String name, Object obj) {
        if (TABLE.containsKey(name)) {
            TABLE.remove(name);
        }
        TABLE.put(name, obj);
    }

    /**
     * 去除应用路径绑定
     * 
     * @param name
     *            --表示应用的key
     */
    public void unbind(String name) {
        if (TABLE.containsKey(name)) {
            TABLE.remove(name);
        }
    }

    public static AppContext getInstance() {
        return instance;
    }
}
