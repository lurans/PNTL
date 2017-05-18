/*
 * 文 件 名:  KeystoneAuth.java
 * 版 本 号:  V1.0.0
 * 版    权:  Huawei Technologies Co., Ltd. Copyright 1988-2008,  All rights reserved
 * 创建日期:  2015-3-12
 */
package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneAuthV3.AuthIdentity.AssumeRole;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneAuthV3.AuthScope.AuthDomain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 鉴权类
 */
@JsonRootName("auth")
public class KeystoneAuthV3 {

    private AuthIdentity identity;

    private AuthScope scope;

    /**
     * 默认构造函数
     */
    public KeystoneAuthV3(AssumeRole assume, AuthScope scope) {
        this.identity = AuthIdentity.createCredentialType(assume);
        this.scope = scope;
    }

    /**
     * 默认构造函数
     */
    public KeystoneAuthV3(String id, String password, AuthScope scope) {
        this.identity = AuthIdentity.createCredentialType(id, password);
        this.scope = scope;
    }

    public KeystoneAuthV3(String token, AuthScope scope) {
        this.identity = AuthIdentity.createCredentialType(token);
        this.scope = scope;
    }

    /**
     * 默认构造函数
     */
    public KeystoneAuthV3(String name, String password, String domainId, String domainName, AuthScope scope) {
        this.identity = AuthIdentity.createCredentialType(name, password, domainId, domainName);
        this.scope = scope;
    }

    public AuthScope getScope() {
        return scope;
    }

    public void setScope(AuthScope scope) {
        this.scope = scope;
    }

    public AuthIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(AuthIdentity identity) {
        this.identity = identity;
    }

    /**
     * AuthIdentity
     */
    public static final class AuthIdentity {

        private AuthPassword password;

        private AuthToken token;

        @JsonProperty("hw_assume_role")
        private AssumeRole assumeRole;
        private List<String> methods = new ArrayList<String>();

        /**
         * createCredentialType
         *
         * @param String username
         * @param String password
         * @return AuthIdentity
         */
        static AuthIdentity createCredentialType(String username, String password) {
            AuthIdentity identity = new AuthIdentity();
            identity.password = new AuthPassword(username, password);
            identity.methods.add("password");
            return identity;
        }

        /**
         * createCredentialType
         *
         * @param String username
         * @param String password
         * @param String domainName
         * @return AuthIdentity
         */
        static AuthIdentity createCredentialType(String username, String password, String domainId, String domainName) {
            AuthIdentity identity = new AuthIdentity();
            identity.password = new AuthPassword(username, password, domainId, domainName);
            identity.methods.add("password");
            return identity;
        }

        /**
         * createCredentialType
         *
         * @param String token
         * @return AuthIdentity
         */
        static AuthIdentity createCredentialType(String token) {
            AuthIdentity identity = new AuthIdentity();
            identity.token = new AuthToken(token);
            identity.methods.add("token");
            return identity;

        }

        /**
         * createCredentialType
         *
         * @param AssumeRole assume
         * @return AuthIdentity
         */
        static AuthIdentity createCredentialType(AssumeRole assume) {
            AuthIdentity identity = new AuthIdentity();
            identity.assumeRole = assume;
            identity.methods.add("hw_assume_role");
            return identity;

        }

        public AssumeRole getAssumeRole() {
            return assumeRole;
        }

        public void setAssumeRole(AssumeRole assumeRole) {
            this.assumeRole = assumeRole;
        }

        public AuthPassword getPassword() {
            return password;
        }

        public void setPassword(AuthPassword password) {
            this.password = password;
        }

        public AuthToken getToken() {
            return token;
        }

        public void setToken(AuthToken token) {
            this.token = token;
        }

        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        /**
         * AuthPassword
         */
        public static final class AuthPassword {

            private AuthUser user;

            public AuthPassword(String username, String password) {
                this.user = new AuthUser(username, password);
            }

            public AuthPassword(String username, String password, String domainId, String domainName) {
                this.user = new AuthUser(username, password, new AuthDomain(domainId, domainName));
            }

            public AuthUser getUser() {
                return user;
            }

            public void setUser(AuthUser user) {
                this.user = user;
            }

            /**
             * AuthUser
             */
            public static final class AuthUser {

                private String id;

                private String name;

                private String password;

                private AuthDomain domain;

                /**
                 * 默认构造函数
                 */
                public AuthUser(String id, String password) {
                    super();
                    this.id = id;
                    this.password = password;
                }

                /**
                 * 默认构造函数
                 */
                public AuthUser(String name, String password, AuthDomain domain) {
                    super();
                    this.name = name;
                    this.password = password;
                    this.domain = domain;
                }

                public AuthDomain getDomain() {
                    return domain;
                }

                public void setDomain(AuthDomain domain) {
                    this.domain = domain;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getPassword() {
                    return password;
                }

                public void setPassword(String password) {
                    this.password = password;
                }
            }
        }

        /**
         * AuthToken
         */
        public static final class AuthToken {

            private String id;

            /**
             * 默认构造函数
             */
            public AuthToken(String token) {
                this.id = token;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

        }

        /**
         * AssumeRole
         */
        public static final class AssumeRole {

            //非必选
            @JsonProperty("token_id")
            private String tokenId;

            @JsonProperty("expires_at")
            private Date expires;

            @JsonProperty("domain_id")
            private String domainId;

            @JsonProperty("xrole_name")
            private String xRole;

            /**
             * 默认构造函数
             */
            public AssumeRole(String tokenId, Date expires, String domainId, String xRole) {
                super();
                this.tokenId = tokenId;
                this.expires = (Date) expires.clone();
                this.domainId = domainId;
                this.xRole = xRole;
            }

            public String getTokenId() {
                return tokenId;
            }

            public void setTokenId(String tokenId) {
                this.tokenId = tokenId;
            }

            public Date getExpires() {
                return (Date) expires.clone();
            }

            public void setExpires(Date expires) {
                this.expires = (Date) expires.clone();
            }

            public String getDomainId() {
                return domainId;
            }

            public void setDomainId(String domainId) {
                this.domainId = domainId;
            }

            public String getxRole() {
                return xRole;
            }

            public void setxRole(String xRole) {
                this.xRole = xRole;
            }

        }
    }

    /**
     * AuthScope
     */
    public static final class AuthScope {

        @JsonProperty("project")
        private ScopeProject project;

        @JsonProperty("domain")
        private AuthDomain domain;

        public AuthScope(AuthDomain domain) {
            this.domain = domain;
        }

        public AuthScope(ScopeProject project) {
            this.project = project;
        }

        public ScopeProject getProject() {
            return project;
        }

        public void setProject(ScopeProject project) {
            this.project = project;
        }

        public AuthDomain getDomain() {
            return domain;
        }

        public void setDomain(AuthDomain domain) {
            this.domain = domain;
        }

        /**
         * ScopeProject
         */
        public static final class ScopeProject {

            private AuthDomain domain;

            @JsonProperty
            private String id;

            @JsonProperty
            private String name;

            /**
             * 默认构造函数
             */
            public ScopeProject(String id) {
                this.id = id;
            }

            /**
             * 默认构造函数
             */
            public ScopeProject(String name, AuthDomain domain) {
                super();
                this.name = name;
                this.domain = domain;
            }

            public AuthDomain getDomain() {
                return domain;
            }

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }

        /**
         * AuthDomain
         */
        public static final class AuthDomain {

            @JsonProperty
            private String id;

            @JsonProperty
            private String name;

            /**
             * 默认构造函数
             */
            public AuthDomain(AuthDomain domain) {
                if (null == domain)
                    this.name = "Default";
                else
                    this.name = domain.getId();
            }

            /**
             * 默认构造函数
             */
            public AuthDomain(String id, String name) {
                super();
                this.id = id;
                this.name = name;
            }

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }
}
