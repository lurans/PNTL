define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/chkFlowFixture"],
    function (i18n, commonException, $, Step, _StepDirective, ViewMode) {
        "use strict";

        var eipFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "chkFlowServ",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, chkFlowServ) {
                $scope.i18n = i18n;

                $scope.noDataShow = true;
                $scope.statusShow = false;
                $scope.searchDisable = false;
                $scope.flowShow = false;

                $scope.snat_display = "Soft Nat";
                $scope.vroute_display = "vRouter";

                $scope.vmIpShow = true;
                $scope.az_diplay = "AZ";
                $scope.pod_diplay = "POD";
                $scope.vm_diplay = "VM";
                $scope.vm_net = {
                    "id":"vm_net",
                    "value":"VM ID",
                    "values":[{
                        selectId : 'VM ID',
                        label : 'VM ID',
                        checked : true
                    }, {
                        selectId : 'Public IP',
                        label : 'Elastic IP',
                        checked : false
                    }],
                    "change":function(){
                        divTip.hide();
                    },
                    "select":function(selectId, label){
                        if ("VM ID" === label) {
                            $scope.vmIpShow = true;
                        } else{
                            $scope.vmIpShow = false;
                        }
                        $scope.vm_net.value = selectId;
                    }
                };
                $scope.Vm = {
                    "id":"VmId",
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
                $scope.publicIp = {
                    "id":"publicIp",
                    "width":"160px",
                    "maxLength":15,
                    validate: [{
                        "validFn": "required",
                        "errorDetail":$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn": "ipv4",
                        "errorDetail":$scope.i18n.chkFlow_term_ip_valid
                    }]
                };
                $scope.fipRemoteIp = {
                    "id":"fipRemoteIp",
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

                var initFipInfo = null;
                $scope.inputChg = function(){
                    divTip.hide();
                };

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

                    //soft nat
                    dst.snat_inGroups = src.snat_inGroups;
                    dst.snat_inShowEnd = src.snat_inShowEnd;
                    dst.snat_inShow = src.snat_inShow;
                    dst.snat_inHostStyle = src.snat_inHostStyle;
                    dst.snat_inHost = src.snat_inHost;
                    dst.snat_inAz = src.snat_inAz;
                    dst.snat_inPod = src.snat_inPod;
                    dst.snat_inGroup = src.snat_inGroup;

                    dst.status = src.status;
                    dst.picStatus = src.picStatus;
                    dst.erroInfo = src.erroInfo;
                    dst.vmIpShow = src.vmIpShow;
                    dst.erroInfoShow = src.erroInfoShow;

                    //ucd
                    dst.noDataShow = src.noDataShow;
                    dst.statusShow = src.statusShow;
                    dst.searchDisable = src.searchDisable;
                    dst.flowShow = src.flowShow;
                }

                function initStatusFlag(){
                    copySrc2Dst(initFipInfo, $scope);

                    $scope.vmIpShow = initFipInfo.vmIpShow;
                    var options = $scope.vm_net.values;
                    $scope.vm_net.values = [];
                    if (null !== initFipInfo.Vm_value){
                        $scope.Vm.value = initFipInfo.Vm_value;
                        $scope.vm_net.value = "VM ID";
                        options[0].checked = true;
                        options[1].checked = false;
                    } else if(null !== initFipInfo.publicIp_value){
                        $scope.publicIp.value = initFipInfo.publicIp_value;
                        $scope.vm_net.value = "Public IP";
                        options[0].checked = false;
                        options[1].checked = true;
                    }
                    $scope.vm_net.values= options;
                    $scope.fipRemoteIp.value = initFipInfo.fipRemoteIp;
                }

                var flowInfo = {};
                function setFlowInfoContent(flowInfo){
                    copySrc2Dst($scope, flowInfo);

                    if ("VM ID" === $scope.vm_net.value){
                        flowInfo.publicIp_value = null;
                        flowInfo.Vm_value = $scope.Vm.value;
                    } else {
                        flowInfo.Vm_value = null;
                        flowInfo.publicIp_value = $scope.publicIp.value;
                    }
                    flowInfo.vm_net_value = $scope.vm_net.value;
                    flowInfo.vmIpShow = $scope.vmIpShow;
                    flowInfo.vpnShowFlag = $scope.vpnShowFlag;
                    flowInfo.fipRemoteIp = $scope.fipRemoteIp.value;
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
                    $scope.snat_inGroups = [];
                    $scope.snat_inGroup = [];
                };

                function setOtherHostStyle(num, dataType){
                    var serv = 79*num+10;
                    if (1 == num){
                        serv += 20;
                    }
                    var hostStyle = {'height':serv + 'px'};
                    if("snat_input" == dataType){
                        $scope.snat_inHostStyle =hostStyle;
                        $scope.cna_inHigh += serv;
                        $scope.snat_inShowEnd = true;
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

                    if ("snat_input" === dataType){
                        $scope.snat_inGroup = $scope.snat_inGroups;
                        $scope.snat_inShow = true;
                        $scope.snat_inHost = host;
                        $scope.snat_inAz = az;
                        $scope.snat_inPod = pod;
                    }

                    setOtherHostStyle(length, dataType);
                }

                function extractRouteData(data){
                    $scope.cna_inGroups = $scope.extractDetailRouteData(data, "cna_input");
                    $scope.cna_outGroups = $scope.extractDetailRouteData(data, "cna_output");
                    if(null !== typeof data.snat_input)
                        $scope.snat_inGroups = data.snat_input;
                    else
                        $scope.snat_inGroups = [];
                };

                //统计host信息
                function statisData(){
                    statisDetailData($scope.cna_inGroups, "cna_input");
                    statisDetailData($scope.cna_outGroups, "cna_output");
                    if (null !== $scope.snat_inGroups && 0 !== $scope.snat_inGroups.length)
                        statisOtherDetailData($scope.snat_inGroups, "snat_input");

                    if ($scope.cna_outHigh >= $scope.cna_inHigh)
                        $scope.cna_HostStyle = {'margin-top': $scope.cna_outHigh + 20 + 'px'};
                    else
                        $scope.cna_HostStyle = {'margin-top': $scope.cna_inHigh + 20 + 'px'};
                };

                function saveFlowResults(){
                    setFlowInfoContent(flowInfo);
                    chkFlowServ.setFlowInfo("fip", flowInfo);
                };

                function getChkFlowInfo(id){
                    var promise = chkFlowServ.getNwFlowInfo(id);
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

                function clearSoftNatLastResults(){
                    $scope.snat_inGroups = [];
                    $scope.snat_inShow = false;
                    $scope.snat_inShowEnd = false;
                    $scope.snat_inGroup = [];
                }

                function clearLastResults(){
                    clearL3LastResults();
                    clearSoftNatLastResults();

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

                    var vm_id = null;
                    var public_ip = null;
                    if ("VM ID" === $scope.vm_net.value){
                        vm_id = $scope.Vm.value;
                    } else{
                        public_ip = $scope.publicIp.value;
                    }
                    var remoteIp = $scope.fipRemoteIp.value;
                    para = {"vm_id":vm_id, "public_ip":public_ip, "remote_ip": remoteIp};

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
                
                $scope.searchBtn = function() {
                    if (!window.tinyWidget.UnifyValid.FormValid((".input_content"))){
                        divTip.option("content",$scope.i18n.chkFlow_term_input_valid);
                        divTip.show(30000);
                        return;
                    }

                    divTip.hide();
                    var para = getParaFromInput();
                    clearLastResults();
                    setTaskStatus("PROCESSING");
                    $scope.searchDisable = true;
                    $scope.flowShow = false;
                    saveFlowResults();
                    isSelectTimeout = false;

                    selectTimeout = $timeout(function(){
                        isSelectTimeout = true;
                    }, 10*60*1000);

                    searchFlowInfo(para, chkFlowServ.getFipTaskInfo);

                }

                function init(){
                    initFipInfo = chkFlowServ.getFipFlowInfo();
                    initStatusFlag();
                    if($scope.searchDisable)
                        $timeout(function(){init();},500);
                }

                init();
            }]

        var module = angular.module('common.config');
        module.tinyController('eipFlow.ctrl', eipFlowCtrl);

        return module;

    });
