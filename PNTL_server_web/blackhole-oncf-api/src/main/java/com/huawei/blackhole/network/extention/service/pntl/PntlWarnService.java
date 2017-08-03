package com.huawei.blackhole.network.extention.service.pntl;

import com.huawei.blackhole.network.api.bean.DelayInfo;
import com.huawei.blackhole.network.api.bean.DelayInfoAgent;
import com.huawei.blackhole.network.api.bean.LossRate;
import com.huawei.blackhole.network.api.bean.LossRateAgent;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.service.PntlService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("pntlWarnService")
public class PntlWarnService {
    @Resource(name = "pntlService")
    private PntlService pntlService;
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
            if (StringUtils.isEmpty(flow.getSip()) || StringUtils.isEmpty(flow.getDip())){
                result.setErrorMessage("src ip or dst ip is null");
                return result;
            }

            if (!pntlService.checkIpIsExist(flow.getSip()) || !pntlService.checkIpIsExist(flow.getDip())){
                result.setErrorMessage("src ip or dst ip is not existed in hostlist");
                return result;
            }
            LossRate.saveInfo(flow);
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
            if (StringUtils.isEmpty(flow.getSip()) || StringUtils.isEmpty(flow.getDip())){
                result.setErrorMessage("src ip or dst ip is null");
                return result;
            }
            if (!pntlService.checkIpIsExist(flow.getSip()) || !pntlService.checkIpIsExist(flow.getDip())){
                result.setErrorMessage("src ip or dst ip is not existed in hostlist");
                return result;
            }
            DelayInfo.saveInfo(flow);
        }
        return result;
    }
}
