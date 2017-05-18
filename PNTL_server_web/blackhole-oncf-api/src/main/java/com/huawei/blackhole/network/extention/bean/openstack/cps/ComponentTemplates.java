package com.huawei.blackhole.network.extention.bean.openstack.cps;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentTemplates implements Serializable {
    private static final long serialVersionUID = 7184459704764129288L;
    
    @JsonProperty("templates")
    private List<Template> templates;

    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Template implements Serializable {
        private static final long serialVersionUID = -8365031107855070076L;
        
        @JsonProperty("hamode")
        private String hamode;
        @JsonProperty("name")
        private String name;
        @JsonProperty("service")
        private String service;
        
        public String getHamode() {
            return hamode;
        }
        public void setHamode(String hamode) {
            this.hamode = hamode;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getService() {
            return service;
        }
        public void setService(String service) {
            this.service = service;
        }
    }
}
