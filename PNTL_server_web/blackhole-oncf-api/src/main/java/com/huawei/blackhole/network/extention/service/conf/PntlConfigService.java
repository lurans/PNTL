package com.huawei.blackhole.network.extention.service.conf;

import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.*;
import com.huawei.blackhole.network.common.utils.ExceptionUtil;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.core.bean.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
            Map<String, Object> data = pntlConfig.convertToMap();
            YamlUtil.setConf(data, Resource.PNTL_CONF);
        } catch (ApplicationException | InvalidParamException e) {
            String errMsg = "set config [" + Resource.NAME_CONF + "] failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        }
        return result;
    }

    private void validPntlConfig(PntlConfig pntlConfig)
            throws InvalidParamException{
        if (pntlConfig == null){
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "no data provided");
        }
    }


}
