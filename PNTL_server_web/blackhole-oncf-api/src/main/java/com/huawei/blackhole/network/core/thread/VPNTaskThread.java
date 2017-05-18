package com.huawei.blackhole.network.core.thread;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.constants.TaskStatus;
import com.huawei.blackhole.network.common.constants.TaskTag;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.AuthUtil;
import com.huawei.blackhole.network.common.utils.JschUtil;
import com.huawei.blackhole.network.common.utils.ScpUtil;
import com.huawei.blackhole.network.common.utils.pojo.AuthUser;
import com.huawei.blackhole.network.core.bean.BaseFutureCallableResult;
import com.huawei.blackhole.network.core.bean.VPNTaskParam;

public class VPNTaskThread extends BaseThread implements Callable<BaseFutureCallableResult> {
    private static final Logger LOG = LoggerFactory.getLogger(VPNTaskThread.class);

    private String taskId;

    private VPNTaskParam vpnTaskParam;

    public VPNTaskThread(String taskId, VPNTaskParam vpnTaskParam) {
        super();
        this.taskId = taskId;
        this.vpnTaskParam = vpnTaskParam;
    }

    @Override
    public BaseFutureCallableResult call() throws Exception {
        final String hostIp = vpnTaskParam.getCommonParam().getHostIp();
        final String hostType = vpnTaskParam.getCommonParam().getHostType();

        BaseFutureCallableResult futureResult = new BaseFutureCallableResult(vpnTaskParam.getCommonParam(),
                vpnTaskParam.toString());
        AuthUser authUser = AuthUtil.getKeyFile(hostType);
        try {
            uploadVpnFile(hostIp, authUser);
            executeVpnUploadedFile(hostIp, authUser);
        } catch (ApplicationException e) {
            futureResult.setStatus(TaskStatus.ERROR);
            futureResult.setErrMsg(e.getLocalizedMessage());
            futureResult.setErrType(e.getType());
            return futureResult;
        }

        futureResult.setStatus(TaskStatus.EXECUTING);
        return futureResult;
    }

    private void uploadVpnFile(String hostIp, AuthUser authUser) throws ApplicationException {
        final String localFile = vpnTaskParam.getLocalFile();
        final String dstFileName = vpnTaskParam.getDstFileName();

        if (null == Resource.TCPDUMP_TOOL || null == localFile || null == dstFileName) {
            throw new ApplicationException(ExceptionType.SERVER_ERR, "can not find scripts on server");
        }

        JschUtil.submitCommand(hostIp, authUser, MKDIR);

        LOG.info("upload file to {} start", hostIp);
        ScpUtil.scpTo(hostIp, authUser, Resource.FLOW_COMMON, Resource.TARGET_COMMON);
        ScpUtil.scpTo(hostIp, authUser, localFile, Resource.TARGET_PATH + "/" + dstFileName);
        JschUtil.submitCommand(hostIp, authUser, CHMOD_700);
        LOG.info("upload all file end");

    }

    private void executeVpnUploadedFile(String hostIp, AuthUser authUser) throws ApplicationException {
        String cmdStr = getCommandTaskId(taskId);
        JschUtil.submitCommand(hostIp, authUser, cmdStr);
    }

    private String getCommandTaskId(String taskId) {
        String cmdParam = "";
        final String dstFileName = vpnTaskParam.getDstFileName();
        String vmIp = vpnTaskParam.getCommonParam().getVmIp();
        String qvmPort = vpnTaskParam.getQvmPort();
        String dvrId = vpnTaskParam.getDvrDevId();
        String dvrPortId = vpnTaskParam.getDvrPortId();
        String dvrPortMAc = vpnTaskParam.getDvrMac();
        String sgMac = vpnTaskParam.getSgMac();
        String remoteIp = vpnTaskParam.getRemoteIp();
        String cnaVtepIp = vpnTaskParam.getCnaVtepIp();
        String vrouterVtepIp = vpnTaskParam.getVrouteVtepIP();
        String l2gwVtepIp = vpnTaskParam.getL2gwVtepIp();

        if (taskId.contains(TaskTag.VPN_ELE_CNA_TAG)) {
            cmdParam = String.format("%s %s %s %s %s %s %s %s %s %s %s", taskId, vmIp, qvmPort, dvrId, dvrPortId,
                    dvrPortMAc, sgMac, remoteIp, cnaVtepIp, vrouterVtepIp, l2gwVtepIp);
        } else if (taskId.contains(TaskTag.VPN_ELE_L2GW_TAG)) {
            cmdParam = String.format("%s %s %s %s %s %s %s %s", taskId, vpnTaskParam.getRemoteIp(),
                    vpnTaskParam.getQvmMac(), vpnTaskParam.getSgMac(), vpnTaskParam.getVrouteVtepIP(),
                    vpnTaskParam.getL2gwVtepIp(), vpnTaskParam.getCnaVtepIp(), vpnTaskParam.getProvdSegmtId());
        } else if (taskId.contains(TaskTag.VPN_ELE_VROUTER_TAG)) {
            cmdParam = String.format("%s %s %s %s %s %s %s", taskId, vmIp, vpnTaskParam.getRemoteIp(),
                    vpnTaskParam.getSgMac(), vpnTaskParam.getCnaVtepIp(), vpnTaskParam.getVrouteVtepIP(), vpnTaskParam.getProvdSegmtId());
        }
        return String.format("cd ~/blackhole/chkflow && python %s %s", dstFileName, cmdParam);
    }

}
