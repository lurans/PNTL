package com.huawei.blackhole.network.core.thread;

import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.HostType;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.constants.TaskStatus;
import com.huawei.blackhole.network.common.constants.TaskTag;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.AuthUtil;
import com.huawei.blackhole.network.common.utils.JschUtil;
import com.huawei.blackhole.network.common.utils.ParamUtil;
import com.huawei.blackhole.network.common.utils.ScpUtil;
import com.huawei.blackhole.network.common.utils.pojo.AuthUser;
import com.huawei.blackhole.network.core.bean.BaseFutureCallableResult;
import com.huawei.blackhole.network.core.bean.CommonCallableParam;
import com.huawei.blackhole.network.core.bean.EwL2GWParam;
import com.huawei.blackhole.network.core.bean.EwTaskParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class EWTaskThread extends BaseThread implements Callable<BaseFutureCallableResult> {

    private static final Logger LOG = LoggerFactory.getLogger(EWTaskThread.class);

    private static final String CNA_TASK = "l3";
    private static final String L2GW_TASK = "l2gw";

    private String taskId;

    private EwTaskParam meVmTaskParam;

    private EwTaskParam otherVmTaskParam;

    private EwL2GWParam vmL2GWParam;

    public EWTaskThread(String taskId, EwTaskParam meVmTaskParam, EwTaskParam otherVmTaskParam) {
        super();
        this.taskId = taskId;
        this.meVmTaskParam = meVmTaskParam;
        this.otherVmTaskParam = otherVmTaskParam;
    }

    public EWTaskThread(String taskId, EwL2GWParam vmL2GWParam, EwTaskParam srcVmTaskParam,
                        EwTaskParam dstVmTaskParam) {
        super();
        this.taskId = taskId;
        this.vmL2GWParam = vmL2GWParam;
        this.meVmTaskParam = srcVmTaskParam;
        this.otherVmTaskParam = dstVmTaskParam;
    }

    public EWTaskThread(String taskId, EwL2GWParam vmL2GWParam) {
        super();
        this.taskId = taskId;
        this.vmL2GWParam = vmL2GWParam;
    }

    @Override
    public BaseFutureCallableResult call() throws Exception {
        String vmTaskType = getVmTaskType();
        if (vmTaskType == null) {
            throw new ApplicationException(ExceptionType.SERVER_ERR, "no such task");
        }

        switch (vmTaskType) {
            case CNA_TASK: {
                return callCNATask();
            }
            case L2GW_TASK: {
                return callL2GWTask();
            }
        }
        throw new ApplicationException(ExceptionType.SERVER_ERR, "unsupported task");
    }

    private BaseFutureCallableResult callL2GWTask() throws ApplicationException {
        String hostIp = vmL2GWParam.getCommonParam().getHostIp();

        CommonCallableParam cmdParam = vmL2GWParam.getCommonParam();
        String commandMsg = vmL2GWParam.toString();

        BaseFutureCallableResult futureResult = new BaseFutureCallableResult(cmdParam, commandMsg);
        AuthUser authUser = AuthUtil.getKeyFile(HostType.L2GW);
        try {
            uploadL2GWFile(hostIp, authUser);
            executeL2GWUploadedFile(hostIp, authUser);
        } catch (ApplicationException e) {
            LOG.error("EW L2GW task failed : ", e);
            futureResult.setStatus(TaskStatus.ERROR);
            futureResult.setErrMsg(e.getLocalizedMessage());
            futureResult.setErrType(e.getType());
            return futureResult;
        }

        futureResult.setStatus(TaskStatus.EXECUTING);
        return futureResult;
    }

    private void uploadL2GWFile(String hostIp, AuthUser anthUser) throws ApplicationException {
        if (StringUtils.isEmpty(Resource.TCPDUMP_TOOL) || StringUtils.isEmpty(Resource.FLOW_VXLAN_L2GW)
                || StringUtils.isEmpty(Resource.FLOW_COMMON)) {
            throw new ApplicationException(ExceptionType.SERVER_ERR, "can not find scripts on server");
        }

        JschUtil.submitCommand(hostIp, anthUser, MKDIR);

        LOG.info("upload file to {} start", hostIp);

        ScpUtil.scpTo(hostIp, anthUser, Resource.FLOW_COMMON, Resource.TARGET_COMMON);
        ScpUtil.scpTo(hostIp, anthUser, Resource.FLOW_VXLAN_L2GW, Resource.TARGET_VXLAN_L2GW);
        JschUtil.submitCommand(hostIp, anthUser, CHMOD_700);
        LOG.info("upload all file end");
    }

    private void executeL2GWUploadedFile(String hostIp, AuthUser authUser) throws ApplicationException {
        String srcVmIp = vmL2GWParam.getSrcVmIp();
        String srcVmMac = vmL2GWParam.getSrcVmMac();
        String dstVmIp = vmL2GWParam.getDstVmIp();
        String dstVmMac = vmL2GWParam.getDstVmMac();
        String nodeFlag = vmL2GWParam.getNodeFlag();
        String srcVtepIp = vmL2GWParam.getSrcVtepIp();
        String dstVtepIp = vmL2GWParam.getDstVtepIp();
        String lgwVtepIp = vmL2GWParam.getL2gwVtepIp();
        String srcProvdSegmtId = vmL2GWParam.getSrcProvdSegmtId();
        String dstProvdSegmtId = vmL2GWParam.getDstProvdSegmtId();

        String cmdParam = String.format("%s %s %s %s %s %s %s %s %s %s %s", taskId, srcVmIp, srcVmMac, dstVmIp, dstVmMac,
                nodeFlag, srcVtepIp, dstVtepIp, lgwVtepIp, srcProvdSegmtId, dstProvdSegmtId);

        String cmdStr = String.format("cd ~/blackhole/chkflow && python %s %s", Resource.NAME_FLOW_VXLAN_L2GW,
                cmdParam);

        JschUtil.submitCommand(hostIp, authUser, cmdStr);
    }

    private BaseFutureCallableResult callCNATask() throws ApplicationException {
        String hostIp = meVmTaskParam.getCommonParam().getHostIp();
        String commandMsg = meVmTaskParam.toString() + ";" + otherVmTaskParam.toString();
        CommonCallableParam cmdParam = meVmTaskParam.getCommonParam();
        BaseFutureCallableResult futureResult = new BaseFutureCallableResult(cmdParam, commandMsg);
        AuthUser authUser = AuthUtil.getKeyFile(HostType.CNA);

        try {
            uploadFile(hostIp, authUser);
            executeUploadedFile(hostIp, authUser);
        } catch (ApplicationException e) {
            LOG.error("EW CNA task failed : ", e);
            futureResult.setStatus(TaskStatus.ERROR);
            futureResult.setErrMsg(e.getLocalizedMessage());
            futureResult.setErrType(e.getType());
            return futureResult;
        }

        futureResult.setStatus(TaskStatus.EXECUTING);
        return futureResult;
    }

    private void uploadFile(String hostIp, AuthUser authUser) throws ApplicationException {
        if (StringUtils.isEmpty(Resource.TCPDUMP_TOOL) || StringUtils.isEmpty(Resource.FLOW_VM_CNA_L3)
                || StringUtils.isEmpty(Resource.FLOW_VM_CNA_L2) || StringUtils.isEmpty(Resource.FLOW_COMMON)) {
            LOG.error("can not find upload file in ansible server");
            throw new ApplicationException(ExceptionType.SERVER_ERR, "can not find scripts on server");
        }

        JschUtil.submitCommand(hostIp, authUser, MKDIR);

        LOG.info("upload file to {} start", hostIp);
        ScpUtil.scpTo(hostIp, authUser, Resource.FLOW_COMMON, Resource.TARGET_COMMON);
        ScpUtil.scpTo(hostIp, authUser, Resource.FLOW_VM_CNA_L3, Resource.TARGET_VM_CNA_L3);
        JschUtil.submitCommand(hostIp, authUser, CHMOD_700);
        LOG.info("upload all file end");
    }

    private void executeUploadedFile(String hostIp, AuthUser authUser) throws ApplicationException {
        String cmdStr = null;
        String cmdParam = null;

        String sameHost = Constants.FALSE;
        if (meVmTaskParam.getCommonParam().getHostIp().equals(otherVmTaskParam.getCommonParam().getHostIp())) {
            sameHost = Constants.TRUE;
        }

        String sameSubnet = Constants.FALSE;
        if (meVmTaskParam.getSubnetId() != null && meVmTaskParam.getSubnetId().equals(otherVmTaskParam.getSubnetId())) {
            sameSubnet = Constants.TRUE;
        }

        String meVmIp = meVmTaskParam.getCommonParam().getVmIp();
        String meMac = meVmTaskParam.getMacAddr();
        String otherVmIp = otherVmTaskParam.getCommonParam().getVmIp();
        String otherVmMac = otherVmTaskParam.getMacAddr();

        String meQvmPort = meVmTaskParam.getQvmPort();
        String meDvrDevId = ParamUtil.getParam(meVmTaskParam.getDvrDevId());

        String meDvrSrcPort = ParamUtil.getParam(meVmTaskParam.getDvrSrcPort());
        String meDvrPortMac = ParamUtil.getParam(meVmTaskParam.getDvrSrcMac());
        String otherDvrDstPort = ParamUtil.getParam(meVmTaskParam.getDvrDstPort());
        String otherDvrPortMac = ParamUtil.getParam(meVmTaskParam.getDvrDstMac());

        String meCnaVtepIp = ParamUtil.getParam(meVmTaskParam.getVtepIp());
        String otherCnaVtepIp = ParamUtil.getParam(otherVmTaskParam.getVtepIp());

        String meL2gwVtepIp = ParamUtil.getParam(meVmTaskParam.getL2gwVtepIp());
        String otherL2gwVtepIp = ParamUtil.getParam(otherVmTaskParam.getL2gwVtepIp());

        String isInSameAz = ParamUtil.getParam(meVmTaskParam.getIsInSameAz());

        cmdParam = String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s", taskId, sameHost, meVmIp,
                meMac, otherVmIp, otherVmMac, meQvmPort, meDvrDevId, meDvrSrcPort, meDvrPortMac, otherDvrDstPort,
                otherDvrPortMac, meCnaVtepIp, otherCnaVtepIp, meL2gwVtepIp, otherL2gwVtepIp, isInSameAz, sameSubnet);

        cmdStr = String.format("cd ~/blackhole/chkflow && python %s %s", Resource.NAME_FLOW_VM_CNA_L3, cmdParam);

        JschUtil.submitCommand(hostIp, authUser, cmdStr);
    }

    private String getVmTaskType() {
        if (taskId.contains(TaskTag.EW_ELE_L2GW_TAG)) {
            return L2GW_TASK;
        }
        if (taskId.contains(TaskTag.EW_ELE_CNA_TAG)) {
            return CNA_TASK;
        }
        return null;
    }
}
