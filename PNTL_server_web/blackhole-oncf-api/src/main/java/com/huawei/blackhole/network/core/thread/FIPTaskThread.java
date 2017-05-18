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
import com.huawei.blackhole.network.core.bean.FIPTaskParam;

public class FIPTaskThread extends BaseThread implements Callable<BaseFutureCallableResult> {

    private static final Logger LOG = LoggerFactory.getLogger(FIPTaskThread.class);

    private String taskId;

    private FIPTaskParam fipTaskParam;

    public FIPTaskThread(String taskId, FIPTaskParam fipTaskParam) {
        super();
        this.taskId = taskId;
        this.fipTaskParam = fipTaskParam;
    }

    @Override
    public BaseFutureCallableResult call() throws Exception {

        final String hostIp = fipTaskParam.getCommonParam().getHostIp();
        String hostType = fipTaskParam.getCommonParam().getHostType();

        AuthUser authUser = AuthUtil.getKeyFile(hostType);
        BaseFutureCallableResult futureResult = new BaseFutureCallableResult(fipTaskParam.getCommonParam(),
                fipTaskParam.toString());
        try {
            uploadFile(hostIp, authUser);
            executeUploadedFile(hostIp, authUser);
        } catch (ApplicationException e) {
            futureResult.setStatus(TaskStatus.ERROR);
            futureResult.setErrMsg(e.getLocalizedMessage());
            futureResult.setErrType(e.getType());
            return futureResult;
        }
        futureResult.setStatus(TaskStatus.EXECUTING);
        return futureResult;
    }

    private void uploadFile(String hostIp, AuthUser authUser) throws ApplicationException {
        if (null == Resource.TCPDUMP_TOOL || null == Resource.FLOW_EIP_CNA || null == Resource.FLOW_COMMON) {
            throw new ApplicationException(ExceptionType.SERVER_ERR, "can not find scripts on server");
        }

        JschUtil.submitCommand(hostIp, authUser, MKDIR);

        String localScriptFile = fipTaskParam.getLocalScriptFile();
        String remoteScript = fipTaskParam.getRemoteScriptName();

        LOG.info("upload file to {} start", hostIp);
        ScpUtil.scpTo(hostIp, authUser, Resource.FLOW_COMMON, Resource.TARGET_COMMON);
        ScpUtil.scpTo(hostIp, authUser, localScriptFile, Resource.TARGET_PATH + "/" + remoteScript);
        JschUtil.submitCommand(hostIp, authUser, CHMOD_700);
        LOG.info("upload all file end");
    }

    private void executeUploadedFile(String hostIp, AuthUser authUser) throws ApplicationException {
        String cmdStr = getCmdByTaskId();
        JschUtil.submitCommand(hostIp, authUser, cmdStr);
    }

    private String getCmdByTaskId() {
        String cmdParam = "";
        String remoteIp = fipTaskParam.getRemoteIp();
        String fip = fipTaskParam.getfIp();
        String eip = fipTaskParam.getEipFloatingIp();
        if (taskId.contains(TaskTag.EIP_ELE_CNA_TAG)) {
            String vmip = fipTaskParam.getVmIp();
            String qvmPort = fipTaskParam.getQvmPort();
            String dvrPortId = fipTaskParam.getDvrPort();
            String dvrId = fipTaskParam.getDvrDevId();

            String gatewayMac = fipTaskParam.getDvrMac();
            String fipNsId = fipTaskParam.getFipNsId();
            String fipPortId = fipTaskParam.getFipPortId();
            String fgMac = fipTaskParam.getFgMac();
            String hasSnat = fipTaskParam.isHasSnat() ? "true" : "false";

            cmdParam = String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s", taskId, vmip, fip, eip, qvmPort,
                    dvrPortId, dvrId, remoteIp, gatewayMac, fipNsId, fipPortId, fgMac, hasSnat);
        } else if (taskId.contains(TaskTag.EIP_ELE_SNAT_TAG)) {
            String provdSegmtId = fipTaskParam.getProvdSegmtId();
            cmdParam = String.format("%s %s %s %s %s", taskId, eip, fip, remoteIp, provdSegmtId);
        }
        return String.format("cd ~/blackhole/chkflow && python %s %s", fipTaskParam.getRemoteScriptName(), cmdParam);
    }
}
