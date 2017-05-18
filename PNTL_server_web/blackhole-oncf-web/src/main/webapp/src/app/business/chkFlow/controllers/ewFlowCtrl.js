define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/chkFlowFixture"],
    function (i18n,commonException,  $, Step, _StepDirective, ViewMode) {
        "use strict";

        var ewFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "chkFlowServ",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, chkFlowServ) {
                $scope.i18n = i18n;

                $scope.noDataShow = true;
                $scope.statusShow = false;
                $scope.searchDisable = false;
                $scope.flowShow = false;

                $scope.l2gw_display = "L2-GW";

                //初始化值
                $scope.az_diplay = "AZ";
                $scope.pod_diplay = "POD";
                $scope.vm_diplay = "VM";
                var statusList = {"err":"ERROR", "processing":"PROCESSING", "end":"END"};
                $scope.net = {
                    "disable":true
                }
                $scope.srcIpName = $scope.i18n.chkFlow_term_source_ip;
                $scope.dstIpName = $scope.i18n.chkFlow_term_destination_ip;
                $scope.srcIp = {
                    "id":"srcIp",
                    "width":"160px",
                    "maxLength":15,
                    "value":"",
                    validate: [{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    }, {
                        "validFn": "ipv4",
                        "errorDetail":$scope.i18n.chkFlow_term_ip_valid
                    }]
                };
                $scope.dstIp = {
                    "id":"dstIp",
                    "width":"160px",
                    "maxLength":15,
                    "value":"",
                    validate: [{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    }, {
                        "validFn": "ipv4",
                        "errorDetail":$scope.i18n.chkFlow_term_ip_valid
                    }]
                };
                $scope.srcVmNet = {
                    "id":"dstVmNetId",
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
                $scope.dstVmNet = {
                    "id":"dstVmNetId",
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
                $scope.vm_net = {
                    "id":"vm_net",
                    "value":"VM ID",
                    "values":[{
                        selectId : 'VM ID',
                        label : 'VM ID',
                        checked : true
                    }, {
                        selectId : 'Network ID',
                        label : 'Subnet ID',
                        checked : false
                    }],
                    "change":function(){
                        divTip.hide();
                    },
                    "select":function(selectId, label){
                        if ("VM ID" === label){
                            $scope.srcVmNetName = i18n.chkFlow_term_src_vm_id;
                            $scope.dstVmNetName = i18n.chkFlow_term_dst_vm_id;
                        } else{
                            $scope.srcVmNetName = i18n.chkFlow_term_src_net_id;
                            $scope.dstVmNetName = i18n.chkFlow_term_dst_net_id;
                        }
                        $scope.vm_net.value = selectId;
                    }
                };

                var initInfo = null;
                
                function initL2gwStatusFlag(){
                    $scope.l2gw_inGroups = initInfo.l2gw_inGroups;
                    $scope.l2gw_outGroups = initInfo.l2gw_outGroups;
                    $scope.l2gw_inShowEnd = initInfo.l2gw_inShowEnd;
                    $scope.l2gw_outShowEnd = initInfo.l2gw_outShowEnd;
                    $scope.l2gw_inShow = initInfo.l2gw_inShow;
                    $scope.l2gw_outShow = initInfo.l2gw_outShow;
                    $scope.l2gw_inAz = initInfo.l2gw_inAz;
                    $scope.l2gw_outAz = initInfo.l2gw_outAz;
                    $scope.l2gw_inPod = initInfo.l2gw_inPod;
                    $scope.l2gw_outPod = initInfo.l2gw_outPod;
                    $scope.l2gw_inHostStyle = initInfo.l2gw_inHostStyle;
                    $scope.l2gw_outHostStyle = initInfo.l2gw_outHostStyle;
                    $scope.l2gw_inGroup = initInfo.l2gw_inGroup;
                    $scope.l2gw_outGroup = initInfo.l2gw_outGroup;
                    $scope.l2gw_outHost = initInfo.l2gw_outHost;
                    $scope.l2gw_inHost = initInfo.l2gw_inHost;
                }
                
                function initL3StatusFlag(){
                    $scope.cna_inGroups = initInfo.cna_inGroups;
                    $scope.cna_outGroups = initInfo.cna_outGroups;
                    $scope.cna_inShowEnd = initInfo.cna_inShowEnd;
                    $scope.cna_outShowEnd = initInfo.cna_outShowEnd;
                    $scope.cna_firstInShow = initInfo.cna_firstInShow;
                    $scope.cna_secdInShow = initInfo.cna_secdInShow;
                    $scope.cna_firstOutShow = initInfo.cna_firstOutShow;
                    $scope.cna_secdOutShow = initInfo.cna_secdOutShow;
                    $scope.cna_inHost1 = initInfo.cna_inHost1;
                    $scope.cna_inHost2 = initInfo.cna_inHost2;
                    $scope.cna_outHost1 = initInfo.cna_outHost1;
                    $scope.cna_outHost2 = initInfo.cna_outHost2;
                    $scope.cna_inHost1Style = initInfo.cna_inHost1Style;
                    $scope.cna_inHost2Style = initInfo.cna_inHost2Style;
                    $scope.cna_outHost1Style = initInfo.cna_outHost1Style;
                    $scope.cna_outHost2Style = initInfo.cna_outHost2Style;
                    $scope.cna_inHostType1 =initInfo.cna_inHostType1;
                    $scope.cna_inHostType2 =initInfo.cna_inHostType2;
                    $scope.cna_outHostType1 = initInfo.cna_outHostType1;
                    $scope.cna_outHostType2 =initInfo.cna_outHostType2;
                    $scope.cna_inAz1 = initInfo.cna_inAz1;
                    $scope.cna_inAz2 = initInfo.cna_inAz2;
                    $scope.cna_outAz1 = initInfo.cna_outAz1;
                    $scope.cna_outAz2 = initInfo.cna_outAz2;
                    $scope.cna_inPod1 = initInfo.cna_inPod1;
                    $scope.cna_inPod2 = initInfo.cna_inPod2;
                    $scope.cna_outPod1 = initInfo.cna_outPod1;
                    $scope.cna_outPod2 = initInfo.cna_outPod2;
                    $scope.cna_HostStyle = initInfo.cna_HostStyle;
                    $scope.cna_srcVm = initInfo.cna_srcVm;
                    $scope.cna_dstVm = initInfo.cna_dstVm;
                    $scope.cna_sameHost = initInfo.cna_sameHost;
                    $scope.cna_firstInGroup = initInfo.cna_firstInGroup;
                    $scope.cna_secondInGroup = initInfo.cna_secondInGroup;
                    $scope.cna_firstOutGroup = initInfo.cna_firstOutGroup;
                    $scope.cna_secondOutGroup = initInfo.cna_secondOutGroup;
                };
                
                function initStatusFlag(){
                    initL3StatusFlag();
                    initL2gwStatusFlag();

                    $scope.erroInfoShow = initInfo.erroInfoShow;
                    $scope.status = initInfo.status;
                    $scope.picStatus = initInfo.picStatus;
                    $scope.erroInfo = initInfo.erroInfo;
                    $scope.searchDisable = initInfo.searchDisable;
                    $scope.flowShow = initInfo.flowShow;

                    //ucd
                    $scope.noDataShow = initInfo.noDataShow;
                    $scope.statusShow = initInfo.statusShow;

                    //input
                    if (null === initInfo.srcVmNetName){
                        $scope.srcVmNetName = i18n.chkFlow_term_src_vm_id;
                        $scope.dstVmNetName = i18n.chkFlow_term_dst_vm_id;
                    } else{
                        $scope.srcVmNetName = initInfo.srcVmNetName;
                        $scope.dstVmNetName = initInfo.dstVmNetName;
                        $scope.srcIp.value = initInfo.srcIp_value;
                        $scope.dstIp.value = initInfo.dstIp_value;

                        $scope.srcVmNet.value = initInfo.srcVmNet_value;
                        $scope.dstVmNet.value = initInfo.dstVmNet_value;

                        var options = $scope.vm_net.values;
                        $scope.vm_net.values = [];
                        if ("VM ID" === initInfo.vm_net_value){
                            $scope.vm_net.value = "VM ID";
                            options[0].checked = true;
                            options[1].checked = false;
                        } else if ('Network ID' === initInfo.vm_net_value){
                            $scope.vm_net.value = "Network IP";
                            options[0].checked = false;
                            options[1].checked = true;
                        }
                        $scope.vm_net.values= options;
                    }
                };
                
                var flowInfo = {
                    "cna_inGroups":[],
                    "cna_outGroups":[],
                    "cna_inShowEnd":false,
                    "cna_outShowEnd":false,
                    "cna_firstInShow":false,
                    "cna_secdInShow":false,
                    "cna_firstOutShow":false,
                    "cna_secdOutShow":false,

                    "cna_inHost1":null,
                    "cna_inHost2":null,
                    "cna_outHost1":null,
                    "cna_outHost2":null,
                    "cna_inHost1Style":null,
                    "cna_inHost2Style":null,
                    "cna_outHost1Style":null,
                    "cna_outHost2Style":null,

                    "cna_inHostType1":null,
                    "cna_inHostType2":null,
                    "cna_outHostType1":null,
                    "cna_outHostType2":null,
                    "cna_inAz1":null,
                    "cna_inAz2":null,
                    "cna_outAz1":null,
                    "cna_outAz2":null,
                    "cna_inPod1":null,
                    "cna_inPod2":null,
                    "cna_outPod1":null,
                    "cna_outPod2":null,

                    //L2GW
                    "l2gw_inGroups":[],
                    "l2gw_outGroups":[],
                    "l2gw_inShowEnd":false,
                    "l2gw_outShowEnd":false,
                    "l2gw_inShow":false,
                    "l2gw_outShow":false,
                    "l2gw_inAz":null,
                    "l2gw_outAz":null,
                    "l2gw_inPod":null,
                    "l2gw_outPod":null,
                    "l2gw_inGroup":[],
                    "l2gw_outGroup":[],
                    "l2gw_inHostStyle":[],
                    "l2gw_outHostStyle":[],

                    "status":null,
                    "picStatus":null,
                    "erroInfo":null,
                    "searchDisable":false,
                    "erroInfoShow":false,
                    //input
                    "srcVmNetName":null,
                    "dstVmNetName":null,
                    "vm_net_value":null,
                    "srcIp_value":null,
                    "dstIp_value":null,
                    "srcVmNet_value":null,
                    "dstVmNet_value":null
                };
                
                function setL2gwFlowInfoContent(flowInfo){
                    flowInfo.l2gw_inGroups = $scope.l2gw_inGroups;
                    flowInfo.l2gw_outGroups = $scope.l2gw_outGroups;
                    flowInfo.l2gw_inShowEnd = $scope.l2gw_inShowEnd;
                    flowInfo.l2gw_outShowEnd = $scope.l2gw_outShowEnd;
                    flowInfo.l2gw_inShow = $scope.l2gw_inShow;
                    flowInfo.l2gw_outShow = $scope.l2gw_outShow;
                    flowInfo.l2gw_inAz = $scope.l2gw_inAz;
                    flowInfo.l2gw_outAz = $scope.l2gw_outAz;
                    flowInfo.l2gw_inPod = $scope.l2gw_inPod;
                    flowInfo.l2gw_outPod = $scope.l2gw_outPod;
                    flowInfo.l2gw_inGroup = $scope.l2gw_inGroup;
                    flowInfo.l2gw_outGroup = $scope.l2gw_outGroup;
                    flowInfo.l2gw_inHostStyle = $scope.l2gw_inHostStyle;
                    flowInfo.l2gw_outHostStyle = $scope.l2gw_outHostStyle;
                    flowInfo.l2gw_inHost = $scope.l2gw_inHost;
                    flowInfo.l2gw_outHost = $scope.l2gw_outHost;
                }
                
                function setL3FlowInfoContent(flowInfo){
                    flowInfo.cna_inGroups = $scope.cna_inGroups;
                    flowInfo.cna_outGroups = $scope.cna_outGroups;
                    flowInfo.cna_inShowEnd = $scope.cna_inShowEnd;
                    flowInfo.cna_outShowEnd = $scope.cna_outShowEnd;
                    flowInfo.cna_firstInShow = $scope.cna_firstInShow;
                    flowInfo.cna_secdInShow = $scope.cna_secdInShow;
                    flowInfo.cna_firstOutShow = $scope.cna_firstOutShow;
                    flowInfo.cna_secdOutShow = $scope.cna_secdOutShow;
                    flowInfo.cna_inHost1 = $scope.cna_inHost1;
                    flowInfo.cna_inHost2 = $scope.cna_inHost2;
                    flowInfo.cna_outHost1 = $scope.cna_outHost1;
                    flowInfo.cna_outHost2 = $scope.cna_outHost2;
                    flowInfo.cna_inHost1Style = $scope.cna_inHost1Style;
                    flowInfo.cna_inHost2Style = $scope.cna_inHost2Style;
                    flowInfo.cna_outHost1Style = $scope.cna_outHost1Style;
                    flowInfo.cna_outHost2Style = $scope.cna_outHost2Style;
                    flowInfo.cna_inHostType1 = $scope.cna_inHostType1;
                    flowInfo.cna_inHostType2 = $scope.cna_inHostType2;
                    flowInfo.cna_outHostType1 = $scope.cna_outHostType1;
                    flowInfo.cna_outHostType2 = $scope.cna_outHostType2;
                    flowInfo.cna_inAz1 = $scope.cna_inAz1;
                    flowInfo.cna_inAz2 = $scope.cna_inAz2;
                    flowInfo.cna_outAz1 = $scope.cna_outAz1;
                    flowInfo.cna_outAz2 = $scope.cna_outAz2;
                    flowInfo.cna_inPod1 = $scope.cna_inPod1;
                    flowInfo.cna_inPod2 = $scope.cna_inPod2;
                    flowInfo.cna_outPod1 = $scope.cna_outPod1;
                    flowInfo.cna_outPod2 = $scope.cna_outPod2;
                    flowInfo.cna_srcVm = $scope.cna_srcVm;
                    flowInfo.cna_dstVm = $scope.cna_dstVm;
                    flowInfo.cna_HostStyle = $scope.cna_HostStyle;
                    flowInfo.cna_sameHost = $scope.cna_sameHost;
                    flowInfo.cna_firstInGroup = $scope.cna_firstInGroup;
                    flowInfo.cna_secondInGroup = $scope.cna_secondInGroup;
                    flowInfo.cna_firstOutGroup = $scope.cna_firstOutGroup;
                    flowInfo.cna_secondOutGroup = $scope.cna_secondOutGroup
                };
                
                function setFlowInfoContent(flowInfo){
                    setL3FlowInfoContent(flowInfo)
                    setL2gwFlowInfoContent(flowInfo);

                    flowInfo.status = $scope.status;
                    flowInfo.picStatus = $scope.picStatus;
                    flowInfo.erroInfo = $scope.erroInfo;
                    flowInfo.searchDisable = $scope.searchDisable;
                    flowInfo.flowShow = $scope.flowShow;
                    flowInfo.erroInfoShow = $scope.erroInfoShow;
                    //input
                    flowInfo.srcVmNetName = $scope.srcVmNetName;
                    flowInfo.dstVmNetName = $scope.dstVmNetName;
                    flowInfo.vm_net_value = $scope.vm_net.value;
                    flowInfo.srcIp_value = $scope.srcIp.value;
                    flowInfo.dstIp_value = $scope.dstIp.value;
                    flowInfo.srcVmNet_value = $scope.srcVmNet.value;
                    flowInfo.dstVmNet_value =  $scope.dstVmNet.value;

                    //ucd
                    flowInfo.noDataShow = $scope.noDataShow;
                    flowInfo.statusShow = $scope.statusShow;
                };
                
                // 状态值，可选值为PROCESSING（处理中），END（处理完毕），ERROR（发生错误）
                function setTaskStatus(status){
                    $scope.noDataShow = false;
                    $scope.statusShow = true;
                    $scope.picStatus = "image_type_status ";
                    if (statusList.processing === status){
                        $scope.status = $scope.i18n.chkFlow_term_status_processing;
                        $scope.picStatus += "BUILDING";
                    } else if (statusList.end === status) {
                        $scope.statusShow = false;
                        commonException.showMsg($scope.i18n.chkFlow_term_status_end, "success");
                    } else {//ERROR
                        $scope.statusShow = true;
                        commonException.showMsg($scope.i18n.chkFlow_term_status_error, "error");
                    }
                };
                
                function setHostStyle(host1_num, host2_num, dataType){
                    var h_serv1 = 79*host1_num+10;
                    var h_serv2 = 79*host2_num+10;
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
                
                function setL2gwHostStyle(num, dataType){
                    var serv = 79*num+10;
                    if (1 == num){
                        serv += 20;
                    }

                    var hostStyle = {'height':serv + 'px'};
                    if ("l2gw_input" == dataType){
                        $scope.l2gw_inHostStyle =hostStyle;
                        $scope.cna_inHigh += serv;
                        $scope.l2gw_inShowEnd = true;
                    } else if ("l2gw_output" == dataType){
                        $scope.l2gw_outHostStyle = hostStyle;
                        $scope.cna_outHigh += serv;
                        $scope.l2gw_outShowEnd = true;
                    }
                };
                
                function statisL2gwDetailData(router, dataType){
                    if (null == router){
                        setErrorDisplayInfo("Request fail, data is null");
                        setTaskStatus("ERROR");
                        return;
                    }

                    var length = router.length;
                    var az = router[0].availability_zone;
                    var pod = router[0].pod;
                    var host = router[0].host_ip;

                    if ("l2gw_input" === dataType){
                        $scope.l2gw_inGroup = $scope.l2gw_inGroups;
                        $scope.l2gw_inShow = true;
                        $scope.l2gw_inAz = az;
                        $scope.l2gw_inPod = pod;
                        $scope.l2gw_inHost = host;
                    }else if("l2gw_output" === dataType){
                        $scope.l2gw_outGroup = $scope.l2gw_outGroups;
                        $scope.l2gw_outShow = true;
                        $scope.l2gw_outAz = az;
                        $scope.l2gw_outPod = pod;
                        $scope.l2gw_outHost = host;
                    }

                    setL2gwHostStyle(length, dataType);
                }
                
                function statisDetailData(router, dataType){
                    if (null == router){
                        setErrorDisplayInfo("Request fail, data is null");
                        setTaskStatus("ERROR");
                        return;
                    }

                    var length = router.length;
                    var host1 = router[0].host_ip;
                    var host2 = router[length-1].host_ip;
                    var hostType1 = router[0].host_type;
                    var hostType2 = router[length-1].host_type;
                    var az1 = router[0].availability_zone;
                    var az2 = router[length-1].availability_zone;
                    var pod1 = router[0].pod;
                    var pod2 = router[length-1].pod;
                    var host1_num = 0;
                    var host2_num = 0;
                    var firstGroup = [];
                    var secondGroup = [];
                    //跨服务器
                    if (host1 !== host2){
                        var src_vm = router[0].vm_ip;
                        var dst_vm = router[length-1].vm_ip;
                        for (var i=0;i < length; i++){
                            if (host1 === router[i].host_ip){
                                host1_num++;
                                firstGroup.push(router[i]);
                            }else{
                                secondGroup.push(router[i]);
                            }
                        }
                        host2_num = length - host1_num;

                        //todo:zlt
                        if ("cna_input" === dataType){
                            $scope.cna_firstInGroup = firstGroup;
                            $scope.cna_secondInGroup = secondGroup;
                        }else if("cna_output" === dataType){
                            $scope.cna_firstOutGroup = firstGroup;
                            $scope.cna_secondOutGroup = secondGroup;
                        }

                        if ("cna_input" === dataType){
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
                            $scope.cna_dstVm = dst_vm;
                            $scope.cna_sameHost = false;
                        } else if ("cna_output" === dataType){
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
                            $scope.cna_dstVm = dst_vm;
                            $scope.cna_sameHost = false;
                        }
                    } else{//单服务器
                        if ("cna_input" === dataType){
                            $scope.cna_firstInGroup = $scope.cna_inGroups;
                        }else if("cna_output" === dataType){
                            $scope.cna_firstOutGroup = $scope.cna_outGroups;
                        }
                        host1_num = length;
                        var vm_ip = new Array();
                        vm_ip=router[0].vm_ip.split(",");
                        var src_vm = vm_ip[0];
                        var dst_vm = vm_ip[1];
                        if ("cna_input" === dataType){
                            $scope.cna_firstInShow = true;
                            $scope.cna_secdInShow = false;
                            $scope.cna_inHost1 = host1;
                            $scope.cna_inHostType1 = hostType1;
                            $scope.cna_inAz1 = az1;
                            $scope.cna_inPod1 = pod1;
                            $scope.cna_srcVm = src_vm;
                            $scope.cna_dstVm = dst_vm;
                            $scope.cna_sameHost = true;
                        } else if ("cna_output" === dataType){
                            $scope.cna_firstOutShow = true;
                            $scope.cna_secdOutShow = false;
                            $scope.cna_outHost1 = host1;
                            $scope.cna_outHostType1 = hostType1;
                            $scope.cna_outAz1 = az1;
                            $scope.cna_outPod1 = pod1;
                            $scope.cna_srcVm = src_vm;
                            $scope.cna_dstVm = dst_vm;
                            $scope.cna_sameHost = true;
                        }
                    }

                    setHostStyle(host1_num, host2_num, dataType);

                };
                //统计host信息
                function statisData(){
                    statisDetailData($scope.cna_inGroups, "cna_input");
                    statisDetailData($scope.cna_outGroups, "cna_output");
                    if (null !== $scope.l2gw_inGroups && 0 !== $scope.l2gw_inGroups.length )
                        statisL2gwDetailData($scope.l2gw_inGroups, "l2gw_input");
                    if (null !== $scope.l2gw_outGroups && 0 !== $scope.l2gw_outGroups.length)
                        statisL2gwDetailData($scope.l2gw_outGroups, "l2gw_output");

                    if ($scope.cna_outHigh >= $scope.cna_inHigh) {
                        $scope.cna_HostStyle = {'margin-top': $scope.cna_outHigh + 20 + 'px'};
                    }
                    else{
                        $scope.cna_HostStyle = {'margin-top': $scope.cna_inHigh + 20 + 'px'};
                    }
                };

                //发生错误时，设置各个显示的状态和数据
                function setErrorDisplayInfo(errInfo){
                    $scope.erroInfo = errInfo;
                    $scope.erroInfoShow = true;
                    $scope.searchDisable = false;
                    $scope.cna_inGroups = [];
                    $scope.cna_outGroups = [];
                    $scope.l2gw_inGroups = [];
                    $scope.l2gw_outGroups = [];
                };
                
                function saveFlowResults(){
                    setFlowInfoContent(flowInfo);
                    chkFlowServ.setFlowInfo("ew", flowInfo);
                };
                
                function extractRouteData(data){
                    $scope.cna_inGroups = $scope.extractDetailRouteData(data, "cna_input");
                    $scope.cna_outGroups = $scope.extractDetailRouteData(data, "cna_output");

                    //L2GW
                    if(null !== typeof data.l2gw_input)
                        $scope.l2gw_inGroups = data.l2gw_input;
                    else
                        $scope.l2gw_inGroups = [];

                    if(null !== typeof data.l2gw_output)
                        $scope.l2gw_outGroups = data.l2gw_output;
                    else
                        $scope.l2gw_outGroups = [];
                };
                
                function getChkFlowInfo(id){
                    var promise = chkFlowServ.getNwFlowInfo(id);
                    promise.then(function(data){
                        setTaskStatus(data.status)

                        if (statusList.err === data.status){
                            $timeout.cancel(selectTimeout);
                            setErrorDisplayInfo(data.err_msg);
                            saveFlowResults();
                            return;
                        }
                        if (statusList.processing === data.status){
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

                        if (statusList.end === data.status){
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

                $scope.chkFlowSearch = {
                    "id":"chkFlowSearch",
                    "text":i18n.chkFlow_term_search,
                    "disable":$scope.searchDisable,
                    "width":"160px",
                    "iconsClass":{
                        "left":"icoMoon-search"
                    }
                };

                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#chkFlowSearch"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });

                $scope.inputChg = function(){
                    divTip.hide();
                };
                
                function clearL2gwLastResults(){
                    $scope.l2gw_inGroups = [];
                    $scope.l2gw_outGroups = [];
                    $scope.l2gw_inShow = false;
                    $scope.l2gw_outShow = false;
                    $scope.l2gw_inShowEnd = false;
                    $scope.l2gw_outShowEnd = false;
                    $scope.l2gw_inGroup = [];
                    $scope.l2gw_outGroup = [];
                }
                
                function clearL3LastResults(){
                    $scope.cna_inGroups = [];
                    $scope.cna_outGroups = [];
                    $scope.cna_firstInShow = false;
                    $scope.cna_firstOutShow = false;
                    $scope.cna_secdInShow = false;
                    $scope.cna_secdOutShow = false;
                    $scope.cna_outShowEnd = false;
                    $scope.cna_inShowEnd = false;
                    $scope.cna_sameHost = false;
                    $scope.cna_firstInGroup = [];
                    $scope.cna_secondInGroup = [];
                    $scope.cna_firstOutGroup = [];
                    $scope.cna_secondOutGroup = [];
                };
                
                function clearLastResults(){
                    clearL3LastResults();
                    clearL2gwLastResults();
                    $scope.erroInfoShow = false;
                    $scope.erroInfo = null;
                };
               
                function getParaFromInput(){
                    var srcIp = $scope.srcIp.value;
                    var dstIp = $scope.dstIp.value;
                    var srcNetId = null;
                    var dstNetId = null;
                    var srcVmId = null;
                    var dstVmId = null;

                    if ($scope.vm_net.value === "VM ID"){
                        srcVmId = $scope.srcVmNet.value;
                        dstVmId = $scope.dstVmNet.value;
                    } else{
                        srcNetId = $scope.srcVmNet.value;
                        dstNetId = $scope.dstVmNet.value;
                    }

                    var para = {"src_vm_ip":srcIp, "src_net_id":srcNetId, "src_vm_id":srcVmId,"dst_vm_ip":dstIp,
                        "dst_net_id":dstNetId, "dst_vm_id":dstVmId};
                    para = JSON.stringify(para);

                    return para;
                };

                var isSelectTimeout;
                var selectTimeout;

                //查询入口
                $scope.search = function(){
                    if (!window.tinyWidget.UnifyValid.FormValid((".input_content"))){
                        divTip.option("content",$scope.i18n.chkFlow_term_input_valid);
                        divTip.show(3000);
                        return;
                    }

                    clearLastResults();
                    setTaskStatus(statusList.processing);
                    $scope.searchDisable = true;
                    $scope.flowShow = false;
                    saveFlowResults();

                    isSelectTimeout = false;
                    selectTimeout = $timeout(function(){
                        isSelectTimeout = true;
                    }, 10*60*1000);

                    var para = getParaFromInput();
                    var promise = chkFlowServ.getVmTaskInfo(para);
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
                            setTaskStatus(statusList.err);
                            saveFlowResults();
                        }
                    }, function(responseData){
                        // 状态值，可选值为PROCESSING（处理中），END（处理完毕），ERROR（发生错误）
                        if (responseData.readyState === 0 && responseData.status === 0){
                            setErrorDisplayInfo("Search Timeout!");
                            setTaskStatus("ERROR");
                            saveFlowResults();
                            return;
                        }
                        var err_msg =responseData.status+":";
                        err_msg += responseData.responseText.slice(12, responseData.responseText.length-2);

                        setErrorDisplayInfo(err_msg);
                        setTaskStatus(statusList.err);
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
                
                function init(){
                    initInfo = chkFlowServ.getVmFlowInfo();
                    initStatusFlag();
                    if($scope.searchDisable)
                        $timeout(function(){init();},500);
                }
                init();
            }
        ]

        var module = angular.module('common.config');
        module.tinyController('ewFlow.ctrl', ewFlowCtrl);

        return module;
    });