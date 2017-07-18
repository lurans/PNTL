package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.service.pntl.Pntl;
import javafx.scene.input.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PntlWarning implements Serializable{
    private static final long serialVersionUID = -8897848136750379361L;
    private static final Logger LOG = LoggerFactory.getLogger(PntlWarning.class);
    @JsonProperty("result")
    private static List<PntlWarnInfo> result = new ArrayList<PntlWarnInfo>();

    public static List<PntlWarnInfo> getResult() {
        return result;
    }

    public static void setResult(List<PntlWarnInfo> result) {
        PntlWarning.result = result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class PntlWarnInfo implements Serializable{
        @JsonProperty("az_id")
        private String azId;
        @JsonProperty("pod_id")
        private String podId;
        @JsonProperty("src_ip")
        private String srcIp;
        @JsonProperty("dst_ip")
        private String dstIp;
        @JsonProperty("time")
        private String time;
        @JsonProperty("delay")
        private String delay;
        @JsonProperty("lossRate")
        private String lossRate;

        @JsonProperty("start_time")
        private String starTime;
        @JsonProperty("end_time")
        private String endTime;

        public String getStarTime() {
            return starTime;
        }

        public void setStarTime(String starTime) {
            this.starTime = starTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getAzId() {
            return azId;
        }

        public void setAzId(String azId) {
            this.azId = azId;
        }

        public String getPodId() {
            return podId;
        }

        public void setPodId(String podId) {
            this.podId = podId;
        }

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

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDelay() {
            return delay;
        }

        public void setDelay(String delay) {
            this.delay = delay;
        }

        public String getLossRate() {
            return lossRate;
        }

        public void setLossRate(String lossRate) {
            this.lossRate = lossRate;
        }
    }

    public static void saveWarnToWarningList(Object newData){
        if (newData instanceof LossRate.LossRateResult){
            LossRate.LossRateResult r = (LossRate.LossRateResult)newData;

            PntlWarning.PntlWarnInfo warn = new PntlWarning.PntlWarnInfo();
            warn.setAzId("");
            warn.setPodId("");
            warn.setSrcIp(r.getSrcIp());
            warn.setDstIp(r.getDstIp());
            warn.setDelay("");
            warn.setLossRate(r.getSendLossRate());
            warn.setTime(r.getTimestamp());

            PntlWarning.getResult().add(warn);
        } else if (newData instanceof DelayInfo.DelayInfoResult){
            DelayInfo.DelayInfoResult r = (DelayInfo.DelayInfoResult)newData;

            PntlWarning.PntlWarnInfo warn = new PntlWarning.PntlWarnInfo();
            warn.setAzId("");
            warn.setPodId("");
            warn.setSrcIp(r.getSrcIp());
            warn.setDstIp(r.getDstIp());
            warn.setLossRate("");
            warn.setDelay(r.getSendRoundDelay());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            warn.setTime(dateFormat.format(r.getTimestamp()));

            PntlWarning.getResult().add(warn);
        }
    }

    private static boolean isGetAllWaringList(PntlWarnInfo param){
        return param.getAzId().isEmpty() && param.getPodId().isEmpty() && param.getSrcIp().isEmpty()
                && param.getDstIp().isEmpty() && param.getStarTime().isEmpty() && param.getEndTime().isEmpty();
    }

    private static boolean validParamCheck(PntlWarnInfo param){
        Pattern pattern = Pattern
                .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                        + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        if (!param.getSrcIp().isEmpty() && !pattern.matcher(param.getSrcIp()).matches()){
            return false;
        }

        if (!param.getDstIp().isEmpty() && !pattern.matcher(param.getDstIp()).matches()){
            return false;
        }

        /*时间不能一个空，一个不空*/
        if ((!param.getStarTime().isEmpty() && param.getEndTime().isEmpty())
                || (param.getStarTime().isEmpty() && !param.getEndTime().isEmpty())){
            return false;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm");
        try {
            Long d1 = df.parse(param.getStarTime()).getTime();
            Long d2 = df.parse(param.getEndTime()).getTime();
            if (d1 > d2){
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static List<PntlWarnInfo> getFilteredResultsByTime(String starTime, String endTime){
        List<PntlWarnInfo> filteredResult = new ArrayList<>();
        /*return all results*/
        if (starTime.isEmpty() && endTime.isEmpty()){
            return PntlWarning.getResult();
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm");
        try {
            Long d1 = df.parse(starTime).getTime();
            Long d2 = df.parse(endTime).getTime();

            for (PntlWarnInfo info : PntlWarning.getResult()){
                Long d = df.parse(info.getTime()).getTime();
                if (d >= d1 && d <= d2){
                    filteredResult.add(info);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return filteredResult;
    }

    private static List<PntlWarnInfo> getFilteredResultsByIps(List<PntlWarnInfo> result, String srcIp, String dstIp){
        List<PntlWarnInfo> filteredResult = new ArrayList<>();
        if (srcIp.isEmpty() && dstIp.isEmpty()){
            return result;
        }
        for (PntlWarnInfo info : result){
            if (!srcIp.isEmpty() && dstIp.isEmpty()){
                if (srcIp.equals(info.getSrcIp())){
                    filteredResult.add(info);
                }
            } else if (srcIp.isEmpty() && !dstIp.isEmpty()){
                if (dstIp.equals(info.getDstIp())){
                    filteredResult.add(info);
                }
            } else {
                if (srcIp.equals(info.getSrcIp()) && dstIp.equals(info.getDstIp())) {
                    filteredResult.add(info);
                }
            }
        }
        return filteredResult;
    }

    private static List<PntlWarnInfo> getFilteredResults(PntlWarnInfo param){
        ///TODO:AZ,POD暂时不做检索
        return getFilteredResultsByIps(getFilteredResultsByTime(param.getStarTime(), param.getEndTime()),
                param.getSrcIp(), param.getDstIp());
    }

    public static Result<Object> getWarnList(PntlWarnInfo param){
        Result<Object> result = new Result<>();
        if (isGetAllWaringList(param)){
            result.setModel(PntlWarning.getResult());
        } else{
            if (!validParamCheck(param)){
                result.addError("", "Input is invalid");
                return result;
            }

            result.setModel(getFilteredResults(param));
        }
        return result;
    }

    public static void refleshHistoryWarning() throws ParseException {
        List<PntlWarnInfo> resultList = PntlWarning.getResult();
        if (resultList == null || resultList.isEmpty()){
            return;
        }
        Iterator<PntlWarnInfo> it = resultList.iterator();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm");
        while (it.hasNext()){
            PntlWarnInfo p = it.next();
            Long intervalTime = System.currentTimeMillis()/1000 - df.parse(p.getTime()).getTime() ;
            if (intervalTime >= PntlInfo.MONITOR_INTERVAL_TIME_HISTORY){
                LOG.info("Remove history warning:" + p.getSrcIp() + "->" + p.getDstIp());
                it.remove();
            }
        }
    }
}
