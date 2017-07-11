package com.huawei.blackhole.network.extention.service.pntl;

import com.huawei.blackhole.network.api.bean.DelayInfo;
import com.huawei.blackhole.network.api.bean.DelayInfoAgent;
import com.huawei.blackhole.network.api.bean.LossRate;
import com.huawei.blackhole.network.api.bean.LossRateAgent;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.IpListJson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("pntlInfoService")
public class PntlInfoService {
    public Result<Object> getLossRate() {
        Result<Object> result = new Result<>();
        result.setModel(LossRate.getResult());
        return result;
    }

    public Result<Object> getDelayInfo() {
        Result<Object> result = new Result<>();
        result.setModel(DelayInfo.getResult());
        return result;
    }

    /**
     * 将从agent收到的丢包率数据保存到内存
     * @param data
     * @return
     */
    public Result<String> saveLossRateData(LossRateAgent data){
        Result<String> result = new Result<>();

        List<LossRateAgent.Flow> flows = data.getFlow();
        if (flows == null || flows.size() == 0){
            result.setErrorMessage("lossRate flows is null");
            return result;
        }

        for (LossRateAgent.Flow flow : flows){
            if (flow.getSip() == null || flow.getDip() == null){
                result.setErrorMessage("src ip or dst ip is null");
                return result;
            }
            LossRate.saveInfo(flow);
            if (needTracerouteLossRate(flow)){
            //    new Pntl().startTraceroute(flow.getSip(), flow.getDip());
            }
        }
        return result;
    }

    /**
     * 将从agent收到的时延数据保存在内存
     * @param data
     * @return
     */
    public Result<String> saveDelayInfoData(DelayInfoAgent data){
        Result<String> result = new Result<>();
        List<DelayInfoAgent.Flow> flows = data.getFlow();

        if (flows == null || flows.size() == 0){
            result.setErrorMessage("delayInfo flows is null");
            return result;
        }

        for (DelayInfoAgent.Flow flow : flows){
            if (flow.getSip() == null || flow.getDip() == null){
                result.setErrorMessage("src ip or dst ip is null");
                return result;
            }
            DelayInfo.saveInfo(flow);
            if (needTracerouteDelay(flow)){
            //    new Pntl().startTraceroute(flow.getSip(), flow.getDip());
            }
        }
        return result;
    }

    private boolean needTracerouteDelay(DelayInfoAgent.Flow flow){
        Long t1 = Long.valueOf(flow.getTime().getT1());
        Long t2 = Long.valueOf(flow.getTime().getT2());
        Long t3 = Long.valueOf(flow.getTime().getT3());
        Long t4 = Long.valueOf(flow.getTime().getT4());

        if (t1 < 0 || t2 < 0 || t3 < 0 || t4 < 0){
            return true;
        }
        ///TODO:是否还有其他条件
        return false;
    }

    private boolean needTracerouteLossRate(LossRateAgent.Flow flow){
        float rate = Float.parseFloat(flow.getSt().getPacketDrops()) / Float.parseFloat(flow.getSt().getPacketSent());
        rate *= 100;
        if (rate >= 50){///TODO:阈值需要再考虑
            return true;
        }

        return false;
    }

}
