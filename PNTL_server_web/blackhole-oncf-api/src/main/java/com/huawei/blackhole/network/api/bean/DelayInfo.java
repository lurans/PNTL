package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.common.constants.PntlInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelayInfo implements Serializable {
    private static final long serialVersionUID = 5115688643432800494L;
    @JsonProperty("result")
    private static List<DelayInfoResult> result = new ArrayList<DelayInfoResult>();

    public static List<DelayInfoResult> getResult() {
        return result;
    }

    public void setResult(List<DelayInfoResult> result) {
        this.result = result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class DelayInfoResult implements Serializable {
        private static final long serialVersionUID = -786990606399131420L;

        @JsonProperty("src_ip")
        private String srcIp;
        @JsonProperty("dst_ip")
        private String dstIp;
        @JsonProperty("send_delay")
        private String sendDelay;
        @JsonProperty("recv_delay")
        private String recvDelay;
        @JsonProperty("send_round_delay")
        private String sendRoundDelay;
        @JsonProperty("recv_round_delay")
        private String recvRoundDelay;

        private Long   timestamp;
        public String getSrcIp() {
            return srcIp;
        }

        public void setSrcIp(String srcIp) {
            this.srcIp = srcIp;
        }

        public String getDstIp() {
            return dstIp;
        }

        public void setDstIp(String dstIp) {
            this.dstIp = dstIp;
        }

        public String getSendDelay() {
            return sendDelay;
        }

        public void setSendDelay(String sendDelay) {
            this.sendDelay = sendDelay;
        }

        public String getRecvDelay() {
            return recvDelay;
        }

        public void setRecvDelay(String recvDelay) {
            this.recvDelay = recvDelay;
        }

        public String getSendRoundDelay() {
            return sendRoundDelay;
        }

        public void setSendRoundDelay(String sendRoundDelay) {
            this.sendRoundDelay = sendRoundDelay;
        }

        public String getRecvRoundDelay() {
            return recvRoundDelay;
        }

        public void setRecvRoundDelay(String recvRoundDelay) {
            this.recvRoundDelay = recvRoundDelay;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static void saveInfo(DelayInfoAgent.Flow flow){
        String srcIp = flow.getSip();
        String dstIp = flow.getDip();
        Long t1 = Long.valueOf(flow.getTime().getT1());
        Long t2 = Long.valueOf(flow.getTime().getT2());
        Long t3 = Long.valueOf(flow.getTime().getT3());
        Long t4 = Long.valueOf(flow.getTime().getT4());
        boolean hasData = false;

        DelayInfoResult newData = new DelayInfoResult();
        newData.setSrcIp(srcIp);
        newData.setDstIp(dstIp);
        newData.setSendDelay(String.valueOf(t2-t1));
        newData.setRecvDelay(String.valueOf(t4-t3));
        newData.setSendRoundDelay(String.valueOf(t4-t1));
        newData.setRecvRoundDelay("0");

        List<DelayInfoResult> resultList = getResult();
        for (DelayInfoResult result : resultList){
            if (result.getSrcIp().equals(srcIp) && result.getDstIp().equals(dstIp)){
                resultList.set(resultList.indexOf(result), newData);
                hasData = true;
                break;
            }
        }

        if (!hasData){
            resultList.add(newData);
        }
    }

    public static void reflesDelayInfoWarning(){
        List<DelayInfoResult> resultList = getResult();
        List<DelayInfoResult> delList = new ArrayList<>();
        for (DelayInfoResult result : resultList){
            Long intervalTime = System.currentTimeMillis()/1000 - result.getTimestamp();
            if (intervalTime >= PntlInfo.MONITOR_INTERVAL_TIME){//second
                delList.add(result);
            }
        }
        resultList.remove(delList);
    }
}
