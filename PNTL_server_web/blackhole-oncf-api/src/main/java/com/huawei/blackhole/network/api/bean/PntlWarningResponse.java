package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PntlWarningResponse implements Serializable {
    private static final long serialVersionUID = -5430653140723258371L;

    @JsonProperty("totalRecords")
    private int totalRecords;

    @JsonProperty("result")
    private List<PntlWarning.PntlWarnInfo> result = new ArrayList<PntlWarning.PntlWarnInfo>();

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<PntlWarning.PntlWarnInfo> getResult() {
        return result;
    }

    public void setResult(List<PntlWarning.PntlWarnInfo> result) {
        this.result = result;
    }

}
