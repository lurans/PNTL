package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("serial")
@JsonRootName("token")
public class KeystoneTokenV3 implements OpenStackEntity {
    private static final long serialVersionUID = 7988045492427786037L;

    @JsonProperty("id")
    public String id;
    @JsonProperty("roles")
    List<KeystoneRoleV3> roles;
    @JsonProperty("expires_at")
    @JsonSerialize(using = IAMDateSerializer.class)
    @JsonDeserialize(using = IAMDateDeserializer.class)
    private Date expires;
    @JsonProperty("issued_at")
    @JsonSerialize(using = IAMDateSerializer.class)
    @JsonDeserialize(using = IAMDateDeserializer.class)
    private Date issued;
    @JsonProperty("methods")
    private List<String> methods;
    @JsonProperty("project")
    private KeystoneProjectV3 project;

    @JsonProperty
    private List<KeystoneCatalog> catalog;

    private KeystoneUser user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpires() {
        return (Date) expires.clone();
    }

    public void setExpires(Date expires) {
        this.expires = (Date) expires.clone();
    }

    public Date getIssued() {
        return (Date) issued.clone();
    }

    public void setIssued(Date issued) {
        this.issued = (Date) issued.clone();
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<KeystoneRoleV3> getRoles() {
        return roles;
    }

    public void setRoles(List<KeystoneRoleV3> roles) {
        this.roles = roles;
    }

    public KeystoneProjectV3 getProject() {
        return project;
    }

    public void setProject(KeystoneProjectV3 project) {
        this.project = project;
    }

    public List<KeystoneCatalog> getCatalog() {
        return catalog;
    }

    public void setCatalog(List<KeystoneCatalog> catalog) {
        this.catalog = catalog;
    }

    public KeystoneUser getUser() {
        return user;
    }

    public void setUser(KeystoneUser user) {
        this.user = user;
    }

    public String getDomainId() {
        if (user != null && user.getDomain() != null) {
            return user.getDomain().getId();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public String getDomainName() {
        if (user != null && user.getDomain() != null) {
            return user.getDomain().getName();
        } else {
            return null;
        }
    }

    public String getProjectId() {
        if (project != null) {
            return project.getId();
        } else {
            return null;
        }
    }

    public List<String> getRolesName() {
        List<String> rolesName = new ArrayList<String>();
        for (int i = 0; i < roles.size(); i++) {
            rolesName.add(roles.get(i).getName());
        }
        return rolesName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeystoneRoleV3 extends BasicResourceEntity {
        @Override
        public String toString() {
            return this.getName();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeystoneUser extends BasicResourceEntity {
        private KeystoneDomainV3 domain;

        public KeystoneDomainV3 getDomain() {
            return domain;
        }

        public void setDomain(KeystoneDomainV3 domain) {
            this.domain = domain;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeystoneProjectV3 extends BasicResourceEntity {
        @JsonProperty
        private KeystoneDomainV3 domain;

        public KeystoneDomainV3 getDomain() {
            return domain;
        }

        public void setDomain(KeystoneDomainV3 domain) {
            this.domain = domain;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeystoneDomainV3 extends BasicResourceEntity {

    }
}
