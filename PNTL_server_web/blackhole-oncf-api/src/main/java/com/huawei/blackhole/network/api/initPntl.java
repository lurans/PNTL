package com.huawei.blackhole.network.api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.service.PntlService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class initPntl implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContext rwp = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        PntlService pntlService= (PntlService) rwp.getBean("pntlService");
        Result<String> result = new Result<>();
        try
        {
            result = pntlService.initPntlConfig();
        }catch (Exception e){
            result.addError("", e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
