package com.huawei.blackhole.network.extention.service.conf;

import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.*;
import com.huawei.blackhole.network.common.utils.ExceptionUtil;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.core.bean.Result;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Service("pntlConfigService")
public class PntlConfigService {
    private static Logger LOGGER = LoggerFactory.getLogger(PntlConfigService.class);

    public Result<PntlConfig> getPntlConfig() {
        Result<PntlConfig> result = new Result<PntlConfig>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) YamlUtil.getConf(Resource.PNTL_CONF);

            PntlConfig pntlConfig = new PntlConfig();
            pntlConfig.setByMap(data);
            result.setModel(pntlConfig);
        } catch (ConfigLostException e) {
            String errMsg = Resource.NAME_CONF + " not found : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (ClassCastException e) {
            String errMsg = "invalid format: " + Resource.NAME_CONF;
            LOGGER.error(errMsg, e);
            result.addError("", ExceptionUtil.prefix(ExceptionType.CLIENT_ERR) + errMsg);
        } catch (InvalidFormatException e) {
            String errMsg = "invalid format: " + Resource.NAME_CONF;
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (ApplicationException e) {
            String errMsg = "fail to get configuration: " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        }
        return result;
    }

    public Result<String> setPntlConfig(PntlConfig pntlConfig) {
        Result<String> result = new Result<String>();
        try {
            validPntlConfig(pntlConfig);
            pntlConfig.setBasicToken(genBasicToken(pntlConfig.getAk(), pntlConfig.getSk()));
            Map<String, Object> data = pntlConfig.convertToMap();
            YamlUtil.setConf(data, Resource.PNTL_CONF);
        } catch (ApplicationException | InvalidParamException e) {
            String errMsg = "set config [" + Resource.NAME_CONF + "] failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (Exception e){
            result.addError("", "parameter is invalid");
        }
        return result;
    }

    private void validPntlConfig(PntlConfig pntlConfig)
            throws InvalidParamException, Exception {
        if (pntlConfig == null){
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "no data provided");
        }

        try {
            int probe_period = Integer.valueOf(pntlConfig.getProbePeriod());
            if (probe_period < 0 || probe_period > 60) {
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "probe period is invalid");
            }

            int port_count = Integer.valueOf(pntlConfig.getPortCount());
            if (port_count < 1 || port_count > 50){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "port count is invalid");
            }
            int report_period = Integer.valueOf(pntlConfig.getReportPeriod());
            if (report_period < 1 || report_period > 60){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "report period is invalid");
            }
            int pkg_count = Integer.valueOf(pntlConfig.getPkgCount());
            if (pkg_count <= 0){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "package count is invalid");
            }
            int delay_threshold = Integer.valueOf(pntlConfig.getDelayThreshold());
            if (delay_threshold < 0){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "delay threshold is invalid");
            }
            int lossRate_threshold = Integer.valueOf(pntlConfig.getLossRateThreshold());
            if (lossRate_threshold <= 0 || lossRate_threshold > 100){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "lossRate threshold is invalid");
            }
            int dscp = Integer.valueOf(pntlConfig.getDscp());
            if (dscp < 0 || dscp > 63){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "dscp is invalid");
            }
            int lossPkg_timeout = Integer.valueOf(pntlConfig.getLossPkgTimeout());
            if (lossPkg_timeout < 0){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "loss package timeout is invalid");
            }
        } catch (Exception e){
            throw new Exception();
        }
    }

    public String genBasicToken(String ak, String sk){
        String str = ak + ":" + sk;
        byte[] encodeBasic64 = Base64.encodeBase64(str.getBytes());
        return new String(encodeBasic64);
    }
}
