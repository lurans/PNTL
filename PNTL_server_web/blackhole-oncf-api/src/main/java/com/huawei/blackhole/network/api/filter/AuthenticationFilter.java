package com.huawei.blackhole.network.api.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.ConfUtil;
import com.huawei.blackhole.network.core.thread.ChkflowServiceStartup;

public class AuthenticationFilter extends HttpServlet implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final Logger OPERATE_LOG = LoggerFactory.getLogger(Config.OPERATION_LOG);

    private static ChkflowServiceStartup chkflowServiceStartup = null;

    private List<String> whiteList = new ArrayList<String>();

    private static final long serialVersionUID = 3518202960043112772L;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ConfUtil CONF = ConfUtil.getInstance();
        try {
            whiteList = CONF.getConfAsStringList("white_list");
        } catch (ConfigLostException e) {
            LOG.warn("ignore exception : ", e);
        }
        updateWhiteList();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) rsp;

        String reqUrl = request.getRequestURL().toString();
        String reqMethod = request.getMethod();

        if (!reqMethod.equals("GET") && !reqMethod.equals("POST")) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED.getCode());
            loginFail(response);
            return;
        }

        // 来访者的IP、主机名
        String remoteAddr = request.getRemoteAddr();

        if (!whiteList.contains(remoteAddr)) {
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED.getCode());
            OPERATE_LOG.info(String.format("%s %s from %s with response: %s", reqMethod, reqUrl, remoteAddr,
                    response.getStatus()));
            return;
        }
        if (null == chkflowServiceStartup) {
            chkflowServiceStartup = (ChkflowServiceStartup) getBean(request, "chkflowServiceStartup");
        }
        if (!ChkflowServiceStartup.isStarted()) {
            response.sendError(500, "chkflow server has not totally started, please wait for 1-2 minutes");
            return;
        }

        chain.doFilter(request, response);

        OPERATE_LOG.info(String.format("%s %s from %s with response: %s", reqMethod, reqUrl, remoteAddr,
                response.getStatus()));
    }

    public String updateWhiteList() {
        String ip = "";
        try {
            Enumeration<?> e1 = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e1.nextElement();
                Enumeration<?> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress ia = (InetAddress) e2.nextElement();
                    if (ia instanceof Inet6Address)
                        continue;
                    whiteList.add(ia.getHostAddress());
                }
            }
        } catch (SocketException e) {
            LOG.error("get local host ip fail", e);
        }
        return ip;
    }

    private void loginFail(HttpServletResponse response) {
        PrintWriter out = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            out = response.getWriter();
            out.flush();
            out.close();
        } catch (IOException e) {
            LOG.error("AuthFilter loginfail ioexception :{}", e);
        }
    }

    private Object getBean(HttpServletRequest request, String bean) {
        ServletContext servletContext = request.getSession().getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        if (null == ctx) {
            return null;
        }
        return ctx.getBean(bean);
    }

}
