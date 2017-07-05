package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelayInfo implements Serializable {
    private static final long serialVersionUID = 5115688643432800494L;
    private static final Logger LOG = LoggerFactory.getLogger(LossRate.class);
    @JsonProperty("result")
    private static List<DelayInfoResult> result = new ArrayList<DelayInfoResult>();

    private static long delayThreshold = 0;

    public static List<DelayInfoResult> getResult() {
        return result;
    }

    public static void setResult(List<DelayInfoResult> result) {
        DelayInfo.result = result;
    }

    public static long getDelayThreshold() {
        return delayThreshold;
    }

    public static void setDelayThreshold(long delayThreshold) {
        DelayInfo.delayThreshold = delayThreshold;
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

        private Long timestamp;
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
        Long t3 = Long.valueOf(flow.getTime().getT3());//对端接收到发送时间
        Long t4 = Long.valueOf(flow.getTime().getT4());//本端发送到接收时间
        boolean hasData = false;

        if (t4 < DelayInfo.getDelayThreshold()){
            return;
        }

        DelayInfoResult newData = new DelayInfoResult();
        newData.setSrcIp(srcIp);
        newData.setDstIp(dstIp);
        newData.setSendDelay(String.valueOf(t2-t1));
        newData.setRecvDelay(String.valueOf(t3));
        newData.setSendRoundDelay(String.valueOf(t4));
        newData.setRecvRoundDelay("0");
        newData.setTimestamp(System.currentTimeMillis()/1000);

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

    public static void refleshDelayInfoWarning(){
        List<DelayInfoResult> resultList = getResult();
        if (resultList == null){
            LOG.error("delayInfo is null");
            return;
        }

        Iterator<DelayInfoResult> it = resultList.iterator();
        while (it.hasNext()){
            DelayInfoResult delayInfo = it.next();
            Long intervalTime = System.currentTimeMillis()/1000 - delayInfo.getTimestamp();
            if (intervalTime >= PntlInfo.MONITOR_INTERVAL_TIME){
                LOG.info("Remove warning:" + delayInfo.getSrcIp() +" -> " + delayInfo.getDstIp());
                it.remove();
            }
        }
    }
}
