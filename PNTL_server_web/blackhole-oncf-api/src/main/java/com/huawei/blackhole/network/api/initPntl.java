package com.huawei.blackhole.network.api;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.service.PntlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class initPntl implements ServletContextListener{
    private static final Logger LOG = LoggerFactory.getLogger(initPntl.class);

    @Resource(name = "pntlService")
    private PntlService pntlService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        Result<String> result = new Result<>();
        try
        {
            result = pntlService.initPntlConfig();
        }catch (Exception e){
            LOG.error("Pntl Init Failed:"+e.getMessage());
            result.addError("", "Pntl Init Failed:"+e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
