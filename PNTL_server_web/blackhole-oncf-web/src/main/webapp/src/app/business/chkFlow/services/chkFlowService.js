define([], function () {
    "use strict";
    var service = function (exception, camel){
        var rest_prefix = window.rest_prefix;
        var ewFlowInfo = {
            //input
            "srcVmNetName":null,
            "dstVmNetName":null,
            "vm_net_value":null,
            "srcIp_value":null,
            "dstIp_value":null,
            "srcVmNet_value":null,
            "dstVmNet_value":null,
            "searchDisable":false,
            "flowShow":false
        };

        //south and north common info
        var snFlowInfo = {
            "fipOrVpn": "FIP"
        };

        var fipFlowInfo = {
            "vmIpShow":true,//初始化值
            "Vm_value":null,
            "publicIp_value":null,
            "fipRemoteIp":null,
            "searchDisable":false,
            "flowShow":false
        };

        var vpnFlowInfo = {//初始化值
            "vpn_vmNetShow":true,
            "vmId":null,
            "netId":null,
            "vmIp":null,
            "vpnRemoteIp":null,
            "searchDisable":false,
            "flowShow":false
        };

        this.getVmTaskInfo = function(data){
            var promise = camel.post({
                "url":{
                    "s": rest_prefix + "/chkflow/vm"
                },
                "params":data,
                "timeout":3*60000
            });
            return promise;
        };

        this.getNwFlowInfo = function(id){
            var promise = camel.get({
                "url":{
                    "s": rest_prefix + "/chkflow/{id}",
                    "o":{
                        "id": id
                    }
                },
                "timeout":60000
            });
            return promise;
        };

        this.getFipTaskInfo = function(data){
            var promise = camel.post({
                "url":{
                    "s": rest_prefix + "/chkflow/fip"
                },
                "params":data,
                "timeout":3*60000
            });
            return promise;
        };

        this.getVpnTaskInfo = function(data){
            var promise = camel.post({
                "url":{
                    "s": rest_prefix + "/chkflow/vpn"
                },
                "params":data,
                "timeout":3*60000
            });
            return promise;
        };

        this.getVmFlowInfo = function(){
            return ewFlowInfo;
        };

        this.getFipFlowInfo = function(){
            return fipFlowInfo;
        }

        this.getVpnFlowInfo = function(){
            return vpnFlowInfo;
        }

        this.getSnFlowInfo = function(){
            return snFlowInfo;
        }

        this.setFlowInfo = function(type, flowInfo){
            if ("ew" === type)    {
                this.setCommFlowInfo(ewFlowInfo, flowInfo);
                this.setVMCommFlowInfo(ewFlowInfo, flowInfo);
                this.setVmFlowInfo(flowInfo);
            } else {
                if ("fip" === type){
                    this.setCommFlowInfo(fipFlowInfo, flowInfo);
                    this.setFipCommFlowInfo(fipFlowInfo, flowInfo);
                    this.setFipFlowInfo(flowInfo);
                } else{//VPN
                    this.setCommFlowInfo(vpnFlowInfo, flowInfo);
                    this.setVpnCommFlowInfo(vpnFlowInfo, flowInfo);
                    this.setVpnFlowInfo(flowInfo);
                }
            }
        };

        function setL3CommFlowInfo(localFlowInfo, flowInfo) {
            localFlowInfo.cna_inGroups = flowInfo.cna_inGroups;
            localFlowInfo.cna_outGroups = flowInfo.cna_outGroups;
            localFlowInfo.cna_inShowEnd = flowInfo.cna_inShowEnd;
            localFlowInfo.cna_outShowEnd = flowInfo.cna_outShowEnd;
            localFlowInfo.erroInfoShow = flowInfo.erroInfoShow;
            localFlowInfo.cna_firstInShow = flowInfo.cna_firstInShow;
            localFlowInfo.cna_secdInShow = flowInfo.cna_secdInShow;
            localFlowInfo.cna_firstOutShow = flowInfo.cna_firstOutShow;
            localFlowInfo.cna_secdOutShow = flowInfo.cna_secdOutShow;

            localFlowInfo.cna_inHost1 = flowInfo.cna_inHost1;
            localFlowInfo.cna_inHost2 = flowInfo.cna_inHost2;
            localFlowInfo.cna_outHost1 = flowInfo.cna_outHost1;
            localFlowInfo.cna_outHost2 = flowInfo.cna_outHost2;
            localFlowInfo.cna_inHost1Style = flowInfo.cna_inHost1Style;
            localFlowInfo.cna_inHost2Style = flowInfo.cna_inHost2Style;
            localFlowInfo.cna_outHost1Style = flowInfo.cna_outHost1Style;
            localFlowInfo.cna_outHost2Style = flowInfo.cna_outHost2Style;

            localFlowInfo.cna_inHostType1 = flowInfo.cna_inHostType1;
            localFlowInfo.cna_inHostType2 = flowInfo.cna_inHostType2;
            localFlowInfo.cna_outHostType1 = flowInfo.cna_outHostType1;
            localFlowInfo.cna_outHostType2 = flowInfo.cna_outHostType2;
            localFlowInfo.cna_inAz1 = flowInfo.cna_inAz1;
            localFlowInfo.cna_inAz2 = flowInfo.cna_inAz2;
            localFlowInfo.cna_outAz1 = flowInfo.cna_outAz1;
            localFlowInfo.cna_outAz2 = flowInfo.cna_outAz2;
            localFlowInfo.cna_inPod1 = flowInfo.cna_inPod1;
            localFlowInfo.cna_inPod2 = flowInfo.cna_inPod2;
            localFlowInfo.cna_outPod1 = flowInfo.cna_outPod1;
            localFlowInfo.cna_outPod2 = flowInfo.cna_outPod2;
            localFlowInfo.cna_HostStyle = flowInfo.cna_HostStyle;
            localFlowInfo.cna_srcVm = flowInfo.cna_srcVm;
            localFlowInfo.cna_dstVm = flowInfo.cna_dstVm;
            localFlowInfo.cna_sameHost = flowInfo.cna_sameHost;
            localFlowInfo.cna_firstInGroup = flowInfo.cna_firstInGroup;
            localFlowInfo.cna_secondInGroup = flowInfo.cna_secondInGroup;
            localFlowInfo.cna_firstOutGroup = flowInfo.cna_firstOutGroup;
            localFlowInfo.cna_secondOutGroup = flowInfo.cna_secondOutGroup;
        };

        this.setVMCommFlowInfo = function (localFlowInfo, flowInfo){
            localFlowInfo.l2gw_inGroups = flowInfo.l2gw_inGroups;
            localFlowInfo.l2gw_outGroups = flowInfo.l2gw_outGroups;
            localFlowInfo.l2gw_inShowEnd = flowInfo.l2gw_inShowEnd;
            localFlowInfo.l2gw_outShowEnd = flowInfo.l2gw_outShowEnd;
            localFlowInfo.l2gw_inShow = flowInfo.l2gw_inShow;
            localFlowInfo.l2gw_outShow = flowInfo.l2gw_outShow;
            localFlowInfo.l2gw_inAz = flowInfo.l2gw_inAz;
            localFlowInfo.l2gw_outAz = flowInfo.l2gw_outAz;
            localFlowInfo.l2gw_inPod = flowInfo.l2gw_inPod;
            localFlowInfo.l2gw_outPod = flowInfo.l2gw_outPod;
            localFlowInfo.l2gw_inGroup = flowInfo.l2gw_inGroup;
            localFlowInfo.l2gw_outGroup = flowInfo.l2gw_outGroup;
            localFlowInfo.l2gw_inHostStyle = flowInfo.l2gw_inHostStyle;
            localFlowInfo.l2gw_outHostStyle = flowInfo.l2gw_outHostStyle;
            localFlowInfo.l2gw_inHost = flowInfo.l2gw_inHost;
            localFlowInfo.l2gw_outHost = flowInfo.l2gw_outHost;
        };

        this.setVpnCommFlowInfo = function (localFlowInfo, flowInfo){
            //vroute
            localFlowInfo.rf_inGroups = flowInfo.rf_inGroups;
            localFlowInfo.rf_outGroups = flowInfo.rf_outGroups;
            localFlowInfo.rf_inShowEnd = flowInfo.rf_inShowEnd;
            localFlowInfo.rf_outShowEnd = flowInfo.rf_outShowEnd;
            localFlowInfo.rf_inShow = flowInfo.rf_inShow;
            localFlowInfo.rf_outShow = flowInfo.rf_outShow;
            localFlowInfo.rf_inAz = flowInfo.rf_inAz;
            localFlowInfo.rf_outAz = flowInfo.rf_outAz;
            localFlowInfo.rf_inPod = flowInfo.rf_inPod;
            localFlowInfo.rf_outPod = flowInfo.rf_outPod;
            localFlowInfo.rf_inGroup = flowInfo.rf_inGroup;
            localFlowInfo.rf_outGroup = flowInfo.rf_outGroup;
            localFlowInfo.rf_inHostStyle = flowInfo.rf_inHostStyle;
            localFlowInfo.rf_outHostStyle = flowInfo.rf_outHostStyle;
            localFlowInfo.rf_inHost = flowInfo.rf_inHost;
            localFlowInfo.rf_outHost = flowInfo.rf_outHost;
            //L2GW
            localFlowInfo.l2gw_inGroups = flowInfo.l2gw_inGroups;
            localFlowInfo.l2gw_inShowEnd = flowInfo.l2gw_inShowEnd;
            localFlowInfo.l2gw_inShow = flowInfo.l2gw_inShow;
            localFlowInfo.l2gw_inAz = flowInfo.l2gw_inAz;
            localFlowInfo.l2gw_inPod = flowInfo.l2gw_inPod;
            localFlowInfo.l2gw_inGroup = flowInfo.l2gw_inGroup;
            localFlowInfo.l2gw_inHostStyle = flowInfo.l2gw_inHostStyle;
            localFlowInfo.l2gw_inHost = flowInfo.l2gw_inHost;
        };

        this.setFipCommFlowInfo = function (localFlowInfo, flowInfo){
            localFlowInfo.snat_inGroups = flowInfo.snat_inGroups;
            localFlowInfo.snat_inShowEnd = flowInfo.snat_inShowEnd;
            localFlowInfo.snat_inShow = flowInfo.snat_inShow;
            localFlowInfo.snat_inHost = flowInfo.snat_inHost;
            localFlowInfo.snat_inAz = flowInfo.snat_inAz;
            localFlowInfo.snat_inPod = flowInfo.snat_inPod;
            localFlowInfo.snat_inGroup = flowInfo.snat_inGroup;
            localFlowInfo.snat_inHostStyle = flowInfo.snat_inHostStyle;
        };

        this.setCommFlowInfo = function(localFlowInfo, flowInfo){
            setL3CommFlowInfo(localFlowInfo, flowInfo);

            localFlowInfo.status = flowInfo.status;
            localFlowInfo.picStatus = flowInfo.picStatus;
            localFlowInfo.erroInfo = flowInfo.erroInfo;
            localFlowInfo.searchDisable = flowInfo.searchDisable;
            localFlowInfo.flowShow = flowInfo.flowShow;
            localFlowInfo.erroInfoShow = flowInfo.erroInfoShow;

            localFlowInfo.noDataShow = flowInfo.noDataShow;
            localFlowInfo.statusShow = flowInfo.statusShow;
        };

        this.setVmFlowInfo = function(flowInfo){
            //input
            ewFlowInfo.srcVmNetName = flowInfo.srcVmNetName;
            ewFlowInfo.dstVmNetName = flowInfo.dstVmNetName;
            ewFlowInfo.vm_net_value = flowInfo.vm_net_value;
            ewFlowInfo.srcIp_value = flowInfo.srcIp_value;
            ewFlowInfo.dstIp_value = flowInfo.dstIp_value;

            ewFlowInfo.srcVmNet_value = flowInfo.srcVmNet_value;
            ewFlowInfo.dstVmNet_value = flowInfo.dstVmNet_value;
        };

        this.setFipFlowInfo = function(flowInfo){
            fipFlowInfo.vmIpShow = flowInfo.vmIpShow;
            fipFlowInfo.vm_net_value = flowInfo.vm_net_value;
            fipFlowInfo.Vm_value = flowInfo.Vm_value;
            fipFlowInfo.publicIp_value = flowInfo.publicIp_value;
            fipFlowInfo.fipRemoteIp = flowInfo.fipRemoteIp;
        };

        this.setVpnFlowInfo = function(flowInfo) {
            vpnFlowInfo.vpn_vmNetShow = flowInfo.vpn_vmNetShow;
            vpnFlowInfo.vpn_vm_net_value = flowInfo.vpn_vm_net_value;
            vpnFlowInfo.vmId = flowInfo.vmId;
            vpnFlowInfo.netId = flowInfo.netId;
            vpnFlowInfo.vmIp = flowInfo.vmIp;
            vpnFlowInfo.vpnRemoteIp = flowInfo.vpnRemoteIp;
        };

    };

    var chkFlowModule = angular.module('common.config');
    chkFlowModule.tinyService('chkFlowServ', service);
});
