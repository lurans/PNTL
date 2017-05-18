package com.huawei.blackhole.network.extention.service.sso;

import com.huawei.blackhole.network.api.bean.SsoConfig;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.common.utils.RegexUtil;
import com.huawei.blackhole.network.core.bean.Result;
import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service("ssoConfiger")
public class SsoConfiger {
    private static Logger LOG = LoggerFactory.getLogger(SsoConfiger.class);

    public Result<Object> getSsoConfig() {
        Result<Object> result = new Result<Object>();
        SsoConfig ssoConfig;
        try {
            ssoConfig = getSsoInfoFromWebXml(true); // 获取sso配置
        } catch (ApplicationException e) {
            String errMsg = e.toString();
            LOG.error(errMsg, e);
            result.addError("", errMsg);
            return result;
        }
        if (ssoConfig.setted()) {
            result.setModel(ssoConfig);
        } else {
            result.setModel(new SsoConfig());
        }
        return result;

    }

    /**
     * 判断当前sso配置能否登录
     *
     * @param request
     * @return
     */
    public Result<Map<String, String>> isSsoLogin(HttpServletRequest request) {
        Result<Map<String, String>> result = new Result<Map<String, String>>();
        Map<String, String> data = new HashMap<String, String>();

        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("userName") != null) {
            data.put("sso_login", Boolean.TRUE.toString());
            data.put("sso_user", session.getAttribute("userName").toString());
        } else {
            data.put("sso_login", Boolean.FALSE.toString());
        }

        result.setModel(data);
        return result;
    }

    private SsoConfig getSsoInfoFromWebXml(boolean isGetConfig) throws ApplicationException {
        SsoConfig ssoConfig = new SsoConfig();
        File webXml = new File(FileUtil.getWebinfPath() + Constants.WEB_XML);
        BufferedReader bf = null;
        try {
//            bf = new BufferedReader(new FileReader(webXml));
            bf = new BufferedReader(new InputStreamReader(new FileInputStream(webXml), Charsets.UTF_8));
            String line = null;

            String httpsStr = "https://";
            int httpsIdx = -1;
            String ssoEnd = "/unisso";
            int httpStrLen = httpsStr.length();

            while ((line = bf.readLine()) != null) {
                if ((httpsIdx = line.indexOf(httpsStr)) != -1) {
                    int end = line.indexOf(ssoEnd);
                    if (end != -1) {
                        String full = line.substring(httpsIdx + httpStrLen, end);
                        String[] ipPort = full.split(":");
                        if (ipPort.length != 2) {
                            throw new ApplicationException(ExceptionType.CONF_ERR, "config in web.xml error : " + full);
                        }
                        if (!RegexUtil.isIp(ipPort[0])) {
                            throw new ApplicationException(ExceptionType.CONF_ERR,
                                    "config in web.xml error : invalid format of IP : " + ipPort[0]);
                        }
                        ssoConfig.setSsoIp(ipPort[0]);
                        try {
                            ssoConfig.setSsoPort(ipPort[1]);
                        } catch (ClassCastException e) {
                            throw new ApplicationException(ExceptionType.CONF_ERR,
                                    "config in web.xml error : invalid format of Port : " + ipPort[1]);
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            String errMsg = "fial to get SSO config : " + e.getLocalizedMessage();
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    bf = null;
                }
            }
            bf = null;
        }
        return ssoConfig;
    }

}
