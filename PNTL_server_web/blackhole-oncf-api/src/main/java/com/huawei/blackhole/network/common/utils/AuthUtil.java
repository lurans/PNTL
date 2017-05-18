package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.HostType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.CommonException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.pojo.AuthUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URISyntaxException;

public class AuthUtil {
    private static final ConfUtil CONF = ConfUtil.getInstance();

    public static String getUser(HttpServletRequest request) {
        String user = "";
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("userName") != null) {
            user = session.getAttribute("userName").toString();
        }
        return user;
    }

    public static AuthUser getKeyFile(String deviceType) throws ApplicationException {
        String user, password, keyFileName;
        if (HostType.CNA.equals(deviceType)) {
            user = Config.SSH_USER_CNA;
            password = Config.SSH_PASSWORD_CNA;
            keyFileName = Config.SSH_KEY_NAME_CNA;
        } else if (HostType.NGFW.equals(deviceType)) {
            user = Config.SSH_USER_NGFW;
            password = Config.SSH_PASSWORD_NGFW;
            keyFileName = Config.SSH_KEY_NAME_NGFW;
        } else if (HostType.L2GW.equals(deviceType)) {
            user = Config.SSH_USER_L2GW;
            password = Config.SSH_PASSWORD_L2GW;
            keyFileName = Config.SSH_KEY_NAME_L2GW;
        } else if (HostType.ROUTERFORWARDER.equals(deviceType)) {
            user = Config.SSH_USER_ROUTERFORWARDER;
            password = Config.SSH_PASSWORD_ROUTERFORWARDER;
            keyFileName = Config.SSH_KEY_NAME_ROUTERFORWARDER;
        } else if (HostType.SNAT.equals(deviceType)) {
            user = Config.SSH_USER_SNAT;
            password = Config.SSH_PASSWORD_SNAT;
            keyFileName = Config.SSH_KEY_NAME_SNAT;
        } else {
            String errMsg = String.format("unsupported host type : [%s]", deviceType);
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        }

        String sshUser = null;
        String sshPass = null;
        String sshKey = null;

        try {
            sshUser = CONF.getConfAsString(user);
            if (CONF.isConfExist(password)) {
                sshPass = WccCrypter.decryptDataByRootKey(CONF.getConfAsString(password));
            }
            if (CONF.isConfExist(keyFileName)) {
                sshKey = CONF.getConfAsString(keyFileName);
                sshKey = ThreadUtil.class.getClassLoader().getResource(sshKey).toURI().getPath();
            }
        } catch (ConfigLostException e) {
            throw new ApplicationException(e.getType(), e.getLocalizedMessage());
        } catch (CommonException e) {
            String errMsg = String.format("fail to get key file: %s", e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        } catch (URISyntaxException e) {
            String errMsg = String.format("fail to get key file: %s", e.getLocalizedMessage());
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        }
        if (sshPass == null && sshKey == null) {
            throw new ApplicationException(ExceptionType.CONF_ERR, "password and key can not all empty");
        }
        return new AuthUser(sshUser, sshPass, sshKey);
    }

}
