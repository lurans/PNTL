package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.core.bean.Result;
/**
 * Created by y00214328 on 2017/5/19.
 */
public interface PntlService {
    Result<String> startPntl();

    Result<String> sendPingListToAgent(PntlConfig config);
}
