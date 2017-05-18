package com.huawei.blackhole.network.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.huawei.sso.client.session.SessionInitService;
import com.huawei.sso.common.exception.UnauthorizedServiceException;

public class SessionInitServiceImpl extends SessionInitService {

    @Override
    public void initUserSession(HttpServletRequest request, HttpServletResponse arg1, String userName)
            throws UnauthorizedServiceException {
        request.getSession().setAttribute("userName", userName);
    }
}
