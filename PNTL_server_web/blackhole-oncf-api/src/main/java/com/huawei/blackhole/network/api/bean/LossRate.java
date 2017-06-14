package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.core.bean.Result;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LossRate implements Serializable{
    private static final long serialVersionUID = 7880158023453028072L;

    @JsonProperty("result")
    private static List<LossRateResult> result = new ArrayList<LossRateResult>();

    public LossRate(){
        this.result = getResult();
    }
    public static List<LossRateResult> getResult() {
        return result;
    }

    public void setResult(List<LossRateResult> result) {
        this.result = result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class LossRateResult implements Serializable{
        private static final long serialVersionUID = 317748549189407292L;

        @JsonProperty("src_ip")
        private String srcIp;
        @JsonProperty("dst_ip")
        private String dstIp;
        @JsonProperty("send_loss_rate")
        private String sendLossRate;
        @JsonProperty("send_pkgs")
        private String sendPkgs;
        @JsonProperty("recv_loss_rate")
        private String recvLossRate;
        @JsonProperty("recv_pkgs")
        private String recvPkgs;

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

        public String getSendLossRate() {
            return sendLossRate;
        }

        public void setSendLossRate(String sendLossRate) {
            this.sendLossRate = sendLossRate;
        }

        public String getSendPkgs() {
            return sendPkgs;
        }

        public void setSendPkgs(String sendPkgs) {
            this.sendPkgs = sendPkgs;
        }

        public String getRecvLossRate() {
            return recvLossRate;
        }

        public void setRecvLossRate(String recvLossRate) {
            this.recvLossRate = recvLossRate;
        }

        public String getRecvPkgs() {
            return recvPkgs;
        }

        public void setRecvPkgs(String recvPkgs) {
            this.recvPkgs = recvPkgs;
        }
    }

    /**
     * 将agent推送的丢包信息保存在内存中，用于UI展示
     * @param flow
     */
    public static void saveInfo(LossRateAgent.Flow flow){
        String srcIp = flow.getSip();
        String dstIp = flow.getDip();
        boolean hasData = false;

        LossRateResult newData = new LossRateResult();
        float rate = Float.parseFloat(flow.getSt().getPacketDrops()) / Float.parseFloat(flow.getSt().getPacketSent());
        DecimalFormat df2 = new DecimalFormat("###.00");

        newData.setSrcIp(srcIp);
        newData.setDstIp(dstIp);
        newData.setSendLossRate(df2.format(rate*100)+"%");
        newData.setSendPkgs(flow.getSt().getPacketSent());
        newData.setRecvLossRate("0");///TODO:暂时设为0
        newData.setRecvPkgs(flow.getSt().getPacketDrops());

        List<LossRateResult> resultList = LossRate.result;
        for (LossRateResult result : resultList){
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

    public static Result<LossRate> getLossRateInfo(){
        Result<LossRate> r = new Result<>();
        LossRate lossRate = new LossRate();
        List<LossRateResult> resultList = getResult();
        lossRate.setResult(resultList);

        r.setModel(lossRate);
        return r;
    }
}
