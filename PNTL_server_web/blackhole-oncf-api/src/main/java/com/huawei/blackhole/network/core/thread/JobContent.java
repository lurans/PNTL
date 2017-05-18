package com.huawei.blackhole.network.core.thread;

public class JobContent {

    private String tenantId;

    private String resourceId;

    private String token;

    public JobContent(String tenantId, String resourceId, String token) {

        this.tenantId = tenantId;
        this.resourceId = resourceId;
        this.token = token;

    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "JobContent [tenantId=" + tenantId + ", resourceId="
                + resourceId + ", token=" + token + "]";
    }


}
