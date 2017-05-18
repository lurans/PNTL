package com.huawei.blackhole.network.common.utils.pojo;

public class AuthUser {
    private String user;
    private String pass;
    private String key;

    public AuthUser(String user, String pass, String key) {
        super();
        this.user = user;
        this.pass = pass;
        this.key = key;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String toString() {
        return String.format("user, %s, pass: %s, key:%s", user, pass, key);
    }

}
