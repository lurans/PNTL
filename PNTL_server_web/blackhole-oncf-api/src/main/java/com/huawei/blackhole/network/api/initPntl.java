package com.huawei.blackhole.network.api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.service.PntlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class InitPntl implements ServletContextListener{
    private static final Logger LOG = LoggerFactory.getLogger(InitPntl.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        Result<String> result = new Result<>();
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        PntlService pntlService= (PntlService) rwp.getBean("pntlService");
        try {
            result = pntlService.initPntl();
        }catch (Exception e){
            LOG.error("Pntl Init Failed");
            result.addError("", "Pntl Init Failed");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
