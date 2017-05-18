package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TokenResponse implements Serializable {
    private static final long serialVersionUID = 1677681567239177423L;

    @JsonProperty("token")
    private Token token;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "TokenResponse [token=" + token + "]";
    }

    public String getProjectId() {
        return this.token.getProject().getId();
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static final class Token implements Serializable {
        private static final long serialVersionUID = -4978899204315116824L;

        @JsonProperty("project")
        private Project project;

        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        @Override
        public String toString() {
            return "Token [project=" + project + "]";
        }

        public static final class Project implements Serializable {
            private static final long serialVersionUID = 6517743040137376852L;

            @JsonProperty("domain")
            private Domain domain;

            @JsonProperty("id")
            private String id;

            @JsonProperty("name")
            private String name;

            public Domain getDomain() {
                return domain;
            }

            public void setDomain(Domain domain) {
                this.domain = domain;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }


            @Override
            public String toString() {
                return "Project [domain=" + domain + ", id=" + id + ", name="
                        + name + "]";
            }


            public static final class Domain implements Serializable {
                private static final long serialVersionUID = 5964683180296239734L;

                @JsonProperty("id")
                private String id;

                @JsonProperty("name")
                private String name;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                @Override
                public String toString() {
                    return "Domain [id=" + id + ", name=" + name + "]";
                }

            }

        }

    }

}
