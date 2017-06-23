define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/chkFlowFixture"],
    function (i18n, commonException, $, Step, _StepDirective, ViewMode) {
        "use strict";

        var locationFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "locationFlowServ",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, locationFlowServ) {
                $scope.i18n = i18n;

                $scope.noDataShow = true;
                $scope.statusShow = false;
                $scope.searchDisable = false;
                $scope.flowShow = false;

                $scope.l2gw_display = "L2-GW";
                $scope.vroute_display = "vRouter";
                $scope.vpn_vmNetShow = true;
                $scope.az_diplay = "AZ";
                $scope.pod_diplay = "POD";
                $scope.vm_diplay = "VM";

                $scope.vpn_vm_net = {
                    "id":"vpn_vm_net",
                    "value":"VM ID",
                    "values":[{
                        selectId : 'VM ID',
                        label : 'VM ID',
                        checked : true
                    }, {
                        selectId : 'Net ID',
                        label : 'Subnet ID',
                        checked : false
                    }],
                    "change":function(){
                        divTip.hide();
                    },
                    "select":function(selectId, label){
                        if ("VM ID" === label) {
                            $scope.vpn_vmNetShow = true;
                        } else{
                            $scope.vpn_vmNetShow = false;
                        }
                        $scope.vpn_vm_net.value = selectId;
                    }
                };
                $scope.vmId = {
                    "id":"vpn_vmId",
                    "width":"160px",
                    "validate":[{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn": "rangeSize",
                        "params": ["1", "128"],
                        errorDetail:$scope.i18n.chkFlow_term_input_valid
                    },{
                        "validFn" :"regularCheck",
                        "params": "/^[A-Za-z0-9\\-]+$/",
                        errorDetail:$scope.i18n.chkFlow_term_input_valid
                    }]
                };
                $scope.netId = {
                    "id":"netId",
                    "width":"160px",
                    "value":"",
                    "validate":[{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn": "rangeSize",
                        "params": ["1", "128"],
                        errorDetail:$scope.i18n.chkFlow_term_input_valid
                    },{
                        "validFn" :"regularCheck",
                        "params": "/^[A-Za-z0-9\\-]+$/",
                        errorDetail:$scope.i18n.chkFlow_term_input_valid
                    }]
                };
                $scope.vmIp = {
                    "id":"vmIp",
                    "width":"160px",
                    "maxLength":15,
                    "validate":[{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn" :"ipv4",
                        errorDetail:$scope.i18n.chkFlow_term_input_valid
                    }]
                };
                $scope.vpnRemoteIp = {
                    "id":"vpnRemoteIp",
                    "width":"160px",
                    "maxLength":15,
                    "validate":[{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn" :"ipv4",
                        errorDetail:$scope.i18n.chkFlow_term_input_valid
                    }]
                };

                $scope.search = {
                    "id":"search_id",
                    "text":i18n.chkFlow_term_search,
                    "width":"160px",
                    "disable":false,
                    "iconsClass":{
                        "left":"icoMoon-search"
                    }
                };
                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#search_id"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });

                var initVpnInfo = null;
                $scope.inputChg = function(){
                    divTip.hide();
                };

                //将状态保留
                function copySrc2Dst(src, dst){
                    dst.cna_inGroups = src.cna_inGroups;
                    dst.cna_outGroups = src.cna_outGroups;
                    dst.cna_inShowEnd = src.cna_inShowEnd;
                    dst.cna_outShowEnd = src.cna_outShowEnd;
                    dst.cna_firstInShow = src.cna_firstInShow;
                    dst.cna_secdInShow = src.cna_secdInShow;
                    dst.cna_firstOutShow = src.cna_firstOutShow;
                    dst.cna_secdOutShow = src.cna_secdOutShow;
                    dst.cna_inHost1 = src.cna_inHost1;
                    dst.cna_inHost2 = src.cna_inHost2;
                    dst.cna_outHost1 = src.cna_outHost1;
                    dst.cna_outHost2 = src.cna_outHost2;
                    dst.cna_inHost1Style = src.cna_inHost1Style;
                    dst.cna_inHost2Style = src.cna_inHost2Style;
                    dst.cna_outHost1Style = src.cna_outHost1Style;
                    dst.cna_outHost2Style = src.cna_outHost2Style;
                    dst.cna_inHostType1 =src.cna_inHostType1;
                    dst.cna_inHostType2 =src.cna_inHostType2;
                    dst.cna_outHostType1 = src.cna_outHostType1;
                    dst.cna_outHostType2 =src.cna_outHostType2;
                    dst.cna_inAz1 = src.cna_inAz1;
                    dst.cna_inAz2 = src.cna_inAz2;
                    dst.cna_outAz1 = src.cna_outAz1;
                    dst.cna_outAz2 = src.cna_outAz2;
                    dst.cna_inPod1 = src.cna_inPod1;
                    dst.cna_inPod2 = src.cna_inPod2;
                    dst.cna_outPod1 = src.cna_outPod1;
                    dst.cna_outPod2 = src.cna_outPod2;
                    dst.cna_srcVm = src.cna_srcVm;
                    dst.cna_firstInGroup=src.cna_firstInGroup;
                    dst.cna_secondInGroup=src.cna_secondInGroup;
                    dst.cna_firstOutGroup=src.cna_firstOutGroup;
                    dst.cna_secondOutGroup=src.cna_secondOutGroup;
                    //vroute
                    dst.rf_inGroups = src.rf_inGroups;
                    dst.rf_outGroups = src.rf_outGroups;
                    dst.rf_inShowEnd = src.rf_inShowEnd;
                    dst.rf_outShowEnd = src.rf_outShowEnd;
                    dst.rf_inShow = src.rf_inShow;
                    dst.rf_outShow = src.rf_outShow;
                    dst.rf_inHostStyle = src.rf_inHostStyle;
                    dst.rf_outHostStyle = src.rf_outHostStyle;
                    dst.rf_inHost = src.rf_inHost;
                    dst.rf_outHost = src.rf_outHost;
                    dst.rf_inAz = src.rf_inAz;
                    dst.rf_outAz = src.rf_outAz;
                    dst.rf_inPod = src.rf_inPod;
                    dst.rf_outPod = src.rf_outPod;
                    dst.rf_inGroup = src.rf_inGroup;
                    dst.rf_outGroup = src.rf_outGroup;
                    //L2GW
                    dst.l2gw_inGroups = src.l2gw_inGroups;
                    dst.l2gw_inShowEnd = src.l2gw_inShowEnd;
                    dst.l2gw_inShow = src.l2gw_inShow;
                    dst.l2gw_inHostStyle = src.l2gw_inHostStyle;
                    dst.l2gw_inHost = src.l2gw_inHost;
                    dst.l2gw_inAz = src.l2gw_inAz;
                    dst.l2gw_inPod = src.l2gw_inPod;
                    dst.l2gw_inGroup = src.l2gw_inGroup;

                    dst.status = src.status;
                    dst.picStatus = src.picStatus;
                    dst.erroInfo = src.erroInfo;
                    dst.erroInfoShow = src.erroInfoShow;

                    //ucd
                    dst.noDataShow = src.noDataShow;
                    dst.statusShow = src.statusShow;

                    dst.searchDisable = src.searchDisable;
                    dst.flowShow = src.flowShow;
                }

                function initStatusFlag(){
                    //初始化L2,L3以及显示状态
                    copySrc2Dst(initVpnInfo, $scope);
                    //VPN
                    $scope.vpn_vmNetShow = initVpnInfo.vpn_vmNetShow;
                    //vpn_vm_net下拉菜单
                    var options = $scope.vpn_vm_net.values;
                    $scope.vpn_vm_net.values = [];
                    if (null !== initVpnInfo.vmId){
                        $scope.vmId.value = initVpnInfo.vmId;
                        $scope.netId.value = null;
                        $scope.vpn_vm_net.value = "VM ID";
                        options[0].checked = true;
                        options[1].checked = false;
                    } else if(null !== initVpnInfo.netId ){
                        $scope.vmId.value = null;
                        $scope.netId.value = initVpnInfo.netId;
                        $scope.vpn_vm_net.value = "Net ID";
                        options[0].checked = false;
                        options[1].checked = true;
                    }
                    //获取下拉菜单的值
                    $scope.vpn_vm_net.values= options;
                    //获取文本输入框vmIp的值
                    $scope.vmIp.value = initVpnInfo.vmIp;
                    //获取文本输入框vpnRemoteIp的值
                    $scope.vpnRemoteIp.value = initVpnInfo.vpnRemoteIp;
                }

                var flowInfo = {};
                function setFlowInfoContent(flowInfo){
                    copySrc2Dst($scope, flowInfo);

                    if ("VM ID" === $scope.vpn_vm_net.value){
                        flowInfo.vmId = $scope.vmId.value;
                        flowInfo.netId = null;
                    } else{
                        flowInfo.vmId = null;
                        flowInfo.netId = $scope.netId.value;
                    }
                    flowInfo.vpn_vm_net_value = $scope.vpn_vm_net.value;
                    flowInfo.vmIp = $scope.vmIp.value;
                    flowInfo.vpnRemoteIp = $scope.vpnRemoteIp.value;
                    flowInfo.vpn_vmNetShow = $scope.vpn_vmNetShow;
                };

                function setErrorDisplayInfo(errInfo){
                    $scope.erroInfo = errInfo;
                    $scope.erroInfoShow = true;
                    $scope.searchDisable = false;
                    $scope.cna_inGroups = [];
                    $scope.cna_outGroups = [];
                    $scope.cna_firstInGroup=[];
                    $scope.cna_secondInGroup=[];
                    $scope.cna_firstOutGroup=[];
                    $scope.cna_secondOutGroup=[];
                    $scope.rf_inGroups = [];
                    $scope.rf_outGroups = [];
                    $scope.rf_inGroup = [];
                    $scope.rf_outGroup = [];
                    $scope.l2gw_inGroups = [];
                    $scope.l2gw_inGroup = [];
                };

                function setOtherHostStyle(num, dataType){
                    var serv = 79*num+10;
                    if (1 == num){
                        serv += 20;
                    }

                    var hostStyle = {'height':serv + 'px'};
                    if ("rf_input" == dataType){
                        $scope.rf_inHostStyle =hostStyle;
                        $scope.cna_inHigh += serv;
                        $scope.rf_inShowEnd = true;
                    } else if ("rf_output" == dataType){
                        $scope.rf_outHostStyle = hostStyle;
                        $scope.cna_outHigh += serv;
                        $scope.rf_outShowEnd = true;
                    }else if("l2gw_input" == dataType){
                        $scope.l2gw_inHostStyle =hostStyle;
                        $scope.cna_inHigh += serv;
                        $scope.l2gw_inShowEnd = true;
                    }
                }

                function setHostStyle(host1_num, host2_num, dataType){
                    var h_serv1 = 79*host1_num+10;
                    var h_serv2 = 79*host2_num+15;
                    if (1 == host1_num){
                        h_serv1 += 20;
                    }
                    if (1 == host2_num){
                        h_serv2 += 20;
                    }

                    var Host1Style = {'height':h_serv1 + 'px'};
                    var Host2Style = {'height':h_serv2+'px'};
                    var cna_high = h_serv1 + h_serv2;
                    if ("cna_input" == dataType){
                        $scope.cna_inHost1Style =Host1Style
                        $scope.cna_inHost2Style = Host2Style;
                        $scope.cna_inHigh = cna_high;
                        $scope.cna_inShowEnd = true;
                    } else if ("cna_output" == dataType){
                        $scope.cna_outHost1Style = Host1Style;
                        $scope.cna_outHost2Style = Host2Style;
                        $scope.cna_outHigh = cna_high;
                        $scope.cna_outShowEnd = true;
                    }
                };

                function statisDetailData(router, dataType) {
                    if (null == router) {
                        setErrorDisplayInfo("Request fail, data is null");
                        setTaskStatus("ERROR");
                        return;
                    }

                    var length = router.length;
                    var host1 = router[0].host_ip;
                    var host2 = router[length - 1].host_ip;
                    var hostType1 = router[0].host_type;
                    var hostType2 = router[length - 1].host_type;
                    var az1 = router[0].availability_zone;
                    var az2 = router[length - 1].availability_zone;
                    var pod1 = router[0].pod;
                    var pod2 = router[length - 1].pod;
                    var host1_num = 0;
                    var host2_num = 0;
                    var src_vm = router[0].vm_ip;
                    var firstGroup = [];
                    var secondGroup = [];
                    //跨服务器
                    if (host1 !== host2) {
                        for (var i = 0; i < length; i++) {
                            if (host1 === router[i].host_ip) {
                                host1_num++;
                                firstGroup.push(router[i]);
                            }else{
                                secondGroup.push(router[i]);
                            }
                        }

                        if ("cna_input" === dataType){
                            $scope.cna_firstInGroup = firstGroup;
                            $scope.cna_secondInGroup = secondGroup;
                        }else if("cna_output" === dataType){
                            $scope.cna_firstOutGroup = firstGroup;
                            $scope.cna_secondOutGroup = secondGroup;
                        }
                        host2_num = length - host1_num;
                        if ("cna_input" === dataType) {
                            $scope.cna_firstInShow = true;
                            $scope.cna_secdInShow = true;
                            $scope.cna_inHost1 = host1;
                            $scope.cna_inHost2 = host2;
                            $scope.cna_inHostType1 = hostType1;
                            $scope.cna_inHostType2 = hostType2;
                            $scope.cna_inAz1 = az1;
                            $scope.cna_inAz2 = az2;
                            $scope.cna_inPod1 = pod1;
                            $scope.cna_inPod2 = pod2;
                            $scope.cna_srcVm = src_vm;
                        } else if ("cna_output" === dataType) {
                            $scope.cna_firstOutShow = true;
                            $scope.cna_secdOutShow = true;
                            $scope.cna_outHost1 = host1;
                            $scope.cna_outHost2 = host2;
                            $scope.cna_outHostType1 = hostType1;
                            $scope.cna_outHostType2 = hostType2;
                            $scope.cna_outAz1 = az1;
                            $scope.cna_outAz2 = az2;
                            $scope.cna_outPod1 = pod1;
                            $scope.cna_outPod2 = pod2;
                            $scope.cna_srcVm = src_vm;
                        }
                    } else {//单服务器
                        if ("cna_input" === dataType){
                            $scope.cna_firstInGroup = $scope.cna_inGroups;
                        }else if("cna_output" === dataType){
                            $scope.cna_firstOutGroup = $scope.cna_outGroups;
                        }
                        host1_num = length;
                        if ("cna_input" === dataType) {
                            $scope.cna_firstInShow = true;
                            $scope.cna_secdInShow = false;
                            $scope.cna_inHost1 = host1;
                            $scope.cna_inHostType1 = hostType1;
                            $scope.cna_inAz1 = az1;
                            $scope.cna_inPod1 = pod1;
                            $scope.cna_srcVm = src_vm;
                        } else if ("cna_output" === dataType) {
                            $scope.cna_firstOutShow = true;
                            $scope.cna_secdOutShow = false;
                            $scope.cna_outHost1 = host1;
                            $scope.cna_outHostType1 = hostType1;
                            $scope.cna_outAz1 = az1;
                            $scope.cna_outPod1 = pod1;
                            $scope.cna_srcVm = src_vm;
                        }
                    }

                    setHostStyle(host1_num, host2_num, dataType);
                };

                function statisOtherDetailData(router, dataType){
                    if (null == router){
                        setErrorDisplayInfo("Request fail, data is null");
                        setTaskStatus("ERROR");
                        return;
                    }

                    var length = router.length;
                    var az = router[0].availability_zone;
                    var pod = router[0].pod;
                    var host = router[0].host_ip;

                    if ("rf_input" === dataType){
                        $scope.rf_inGroup = $scope.rf_inGroups;
                        $scope.rf_inShow = true;
                        $scope.rf_inAz = az;
                        $scope.rf_inPod = pod;
                        $scope.rf_inHost = host;
                    }else if("rf_output" === dataType){
                        $scope.rf_outGroup = $scope.rf_outGroups;
                        $scope.rf_outShow = true;
                        $scope.rf_outAz = az;
                        $scope.rf_outPod = pod;
                        $scope.rf_outHost = host;
                    }else if("l2gw_input" === dataType){
                        $scope.l2gw_inGroup = $scope.l2gw_inGroups;
                        $scope.l2gw_inShow = true;
                        $scope.l2gw_inAz = az;
                        $scope.l2gw_inPod = pod;
                        $scope.l2gw_inHost = host;
                    }

                    setOtherHostStyle(length, dataType);
                }

                function extractRouteData(data){
                    $scope.cna_inGroups = $scope.extractDetailRouteData(data, "cna_input");
                    $scope.cna_outGroups = $scope.extractDetailRouteData(data, "cna_output");

                    if(null !== typeof data.rf_input)
                        $scope.rf_inGroups = data.rf_input;
                    else
                        $scope.rf_inGroups = [];
                    if(null !== typeof data.rf_output)
                        $scope.rf_outGroups = data.rf_output;
                    else
                        $scope.rf_outGroups = [];
                    if(null !== typeof data.l2gw_input)
                        $scope.l2gw_inGroups = data.l2gw_input;
                    else
                        $scope.l2gw_inGroups = [];
                };

                //统计host信息
                function statisData(){
                    statisDetailData($scope.cna_inGroups, "cna_input");
                    statisDetailData($scope.cna_outGroups, "cna_output");
                    if (null !== $scope.rf_inGroups && 0 !== $scope.rf_inGroups.length)
                        statisOtherDetailData($scope.rf_inGroups, "rf_input");
                    if (null !== $scope.rf_outGroups && 0 !== $scope.rf_outGroups.length)
                        statisOtherDetailData($scope.rf_outGroups, "rf_output");
                    if (null !== $scope.l2gw_inGroups && 0 !== $scope.l2gw_inGroups.length)
                        statisOtherDetailData($scope.l2gw_inGroups, "l2gw_input");

                    if ($scope.cna_outHigh >= $scope.cna_inHigh)
                        $scope.cna_HostStyle = {'margin-top': $scope.cna_outHigh + 20 + 'px'};
                    else
                        $scope.cna_HostStyle = {'margin-top': $scope.cna_inHigh + 20 + 'px'};
                };

                function saveFlowResults(){
                    setFlowInfoContent(flowInfo);
                    //serv1
                    locationFlowServ.setFlowInfo("vpn", flowInfo);
                };

                function getChkFlowInfo(id){
                    //serv2
                    var promise = locationFlowServ.getNwFlowInfo(id);
                    promise.then(function(data){
                        setTaskStatus(data.status);
                        if ("ERROR" === data.status){
                            $timeout.cancel(selectTimeout);
                            setErrorDisplayInfo(data.err_msg);
                            saveFlowResults();
                            return;
                        }

                        if ("PROCESSING" === data.status){
                            if(isSelectTimeout) {
                                setErrorDisplayInfo("Search Timeout!");
                                setTaskStatus("ERROR");
                            }else{
                                $timeout(function(){
                                    getChkFlowInfo(id);
                                }, 5000);
                            }
                            saveFlowResults();
                        }

                        if ("END" === data.status){
                            $timeout.cancel(selectTimeout);
                            extractRouteData(data);
                            statisData();
                            $scope.flowShow = true;
                            $scope.searchDisable = false;
                            saveFlowResults();
                        }
                    },function(data){
                        setErrorDisplayInfo(data.err_msg);
                        setTaskStatus("ERROR");
                    });
                };

                function clearL3LastResults(){
                    $scope.cna_inGroups = [];
                    $scope.cna_outGroups = [];
                    $scope.cna_firstInShow = false;
                    $scope.cna_firstOutShow = false;
                    $scope.cna_secdInShow = false;
                    $scope.cna_secdOutShow = false;
                    $scope.cna_outShowEnd = false;
                    $scope.cna_inShowEnd = false;
                    $scope.cna_firstInGroup=[];
                    $scope.cna_secondInGroup=[];
                    $scope.cna_firstOutGroup=[];
                    $scope.cna_secondOutGroup=[];
                };

                function clearVrouteLastResults(){
                    $scope.rf_inGroups = [];
                    $scope.rf_outGroups = [];
                    $scope.rf_inShow = false;
                    $scope.rf_outShow = false;
                    $scope.rf_outShowEnd = false;
                    $scope.rf_inShowEnd = false;
                    $scope.rf_inGroup = [];
                    $scope.rf_outGroup = [];
                }

                function clearL2gwLastResults(){
                    $scope.l2gw_inGroups = [];
                    $scope.l2gw_inShow = false;
                    $scope.l2gw_inShowEnd = false;
                    $scope.l2gw_inGroup = [];
                }

                function clearLastResults(){
                    clearL3LastResults();
                    clearVrouteLastResults();
                    clearL2gwLastResults();
                    $scope.erroInfoShow = false;
                    $scope.erroInfo = null;
                };

                // 状态值，可选值为PROCESSING（处理中），END（处理完毕），ERROR（发生错误）
                function setTaskStatus(status){
                    $scope.noDataShow = false;
                    $scope.statusShow = true;
                    $scope.picStatus = "image_type_status ";
                    if ("PROCESSING" === status){
                        $scope.status = $scope.i18n.chkFlow_term_status_processing;
                        $scope.picStatus += "BUILDING";
                    } else if ("END" === status) {
                        $scope.statusShow = false;
                        commonException.showMsg($scope.i18n.chkFlow_term_status_end, "success");
                    } else {//ERROR
                        $scope.statusShow = true;
                        commonException.showMsg($scope.i18n.chkFlow_term_status_error, "error");
                    }
                };

                function getParaFromInput(){
                    var para = null;

                    var vmId = null;
                    var netId = null;

                    if ("VM ID" === $scope.vpn_vm_net.value){
                        vmId = $scope.vmId.value;
                    } else {
                        netId = $scope.netId.value;
                    }
                    var vmIp = $scope.vmIp.value;
                    var remoteIp = $scope.vpnRemoteIp.value;
                    para = {"vm_id":vmId, "net_id":netId, "vm_ip":vmIp,"remote_ip": remoteIp};

                    para = JSON.stringify(para);

                    return para;
                };

                function searchFlowInfo(para, func){
                    var promise = func(para);
                    promise.then(function(responseData){
                        if (undefined === responseData.err_msg
                            && undefined !== responseData.task_id){
                            saveFlowResults();
                            $timeout(function(){
                                getChkFlowInfo(responseData.task_id);
                            }, 10000);
                        } else{
                            //fail
                            setErrorDisplayInfo(responseData.err_msg);
                            setTaskStatus("ERROR");
                            saveFlowResults();
                        }
                    }, function(responseData){
                        if (responseData.readyState === 0 && responseData.status === 0){
                            setErrorDisplayInfo("Search Timeout!");
                            setTaskStatus("ERROR");
                            saveFlowResults();
                            return;
                        }
                        var err_msg = responseData.status+":";
                        err_msg += responseData.responseText.slice(12, responseData.responseText.length-2);

                        setErrorDisplayInfo(err_msg);
                        setTaskStatus("ERROR");
                        saveFlowResults();
                    });
                };

                $scope.extractDetailRouteData = function(data, dataType){
                    var router = null;
                    var size = 0;
                    if ("cna_input" === dataType){
                        size = data.cna_input.length;
                        router = data.cna_input;
                    } else if ("cna_output" === dataType){
                        size = data.cna_output.length;
                        router = data.cna_output;
                    }
                    if (0 === size){
                        return null;
                    }

                    return router;
                }

                var isSelectTimeout;
                var selectTimeout;

                //2、点击Search按钮时，查询的入口
                $scope.searchBtn = function() {
                    //检验文本框输入的合法性,在button右边给出提示信息
                    if (!window.tinyWidget.UnifyValid.FormValid((".input_content"))){
                        divTip.option("content",$scope.i18n.chkFlow_term_input_valid);
                        divTip.show(30000);
                        return;
                    }
                    //输入合法，提示信息隐藏
                    divTip.hide();
                    //获取输入参数
                    var para = getParaFromInput();
                    //
                    clearLastResults();
                    setTaskStatus("PROCESSING");
                    $scope.searchDisable = true;
                    $scope.flowShow = false;
                    saveFlowResults();
                    isSelectTimeout = false;

                    selectTimeout = $timeout(function(){
                        isSelectTimeout = true;
                    }, 10*60*1000);
                    //serv3
                    searchFlowInfo(para, locationFlowServ.getVpnTaskInfo);
                }
                /*this.getVpnFlowInfo = function(){
                    return vpnFlowInfo;
                    var vpnFlowInfo = {//初始化值
                        "vpn_vmNetShow":true,
                        "vmId":null,
                        "netId":null,
                        "vmIp":null,
                        "vpnRemoteIp":null,
                        "searchDisable":false,
                        "flowShow":false
                    };
                }*/
                function init(){
                    //serv4
                    initVpnInfo = locationFlowServ.getVpnFlowInfo();
                    //将各个状态初始化
                    initStatusFlag();
                    if($scope.searchDisable)
                        $timeout(function(){init();},500);
                }
                //1、未点击Search按钮时，初始化进入init函数
                init();
            }]

        var module = angular.module('common.config');
        module.tinyController('locationFlow.ctrl', locationFlowCtrl);

        return module;

    });