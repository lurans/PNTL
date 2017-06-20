define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/chkFlowFixture"],
    function (i18n, commonException, $, Step, _StepDirective, ViewMode) {
        "use strict";

        var locationFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "locationFlowServ",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, locationFlowServ) {
                $scope.i18n = i18n;

                $scope.searchDisable = false;

                $scope.physicalIpShow = true;
                $scope.flowShow = false;

                $scope.noDataShow = true;
                $scope.statusShow = false;

                $scope.sourceIp = {
                    "id":"sourceIp",
                    "width":"240px",
                    "maxLength":15,
                    validate: [{
                        "validFn": "required",
                        "errorDetail":$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn": "ipv4",
                        "errorDetail":$scope.i18n.chkFlow_term_ip_valid
                    }]
                };
                $scope.remoteIp = {
                    "id":"remoteIp_id",
                    "width":"240px",
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

                $scope.inputChg = function(){
                    $scope.search.disable = false;
                };

                var initInfo = null;
/*
                function copySrc2Dst(src, dst){
                    dst.l2_inGroups = src.l2_inGroups;
                    dst.l2_outGroups = src.l2_outGroups;
                    dst.l2_inShowEnd = src.l2_inShowEnd;
                    dst.l2_outShowEnd = src.l2_outShowEnd;
                    dst.l2_firstInShow = src.l2_firstInShow;
                    dst.l2_secdInShow = src.l2_secdInShow;
                    dst.l2_firstOutShow = src.l2_firstOutShow;
                    dst.l2_secdOutShow = src.l2_secdOutShow;
                    dst.l2_inHost1 = src.l2_inHost1;
                    dst.l2_inHost2 = src.l2_inHost2;
                    dst.l2_outHost1 = src.l2_outHost1;
                    dst.l2_outHost2 = src.l2_outHost2;
                    dst.l2_inHost1Style = src.l2_inHost1Style;
                    dst.l2_inHost2Style = src.l2_inHost2Style;
                    dst.l2_outHost1Style = src.l2_outHost1Style;
                    dst.l2_outHost2Style = src.l2_outHost2Style;
                    dst.l2_inHostType1 =src.l2_inHostType1;
                    dst.l2_inHostType2 =src.l2_inHostType2;
                    dst.l2_outHostType1 = src.l2_outHostType1;
                    dst.l2_outHostType2 =src.l2_outHostType2;
                    dst.l2_inAz1 = src.l2_inAz1;
                    dst.l2_inAz2 = src.l2_inAz2;
                    dst.l2_outAz1 = src.l2_outAz1;
                    dst.l2_outAz2 = src.l2_outAz2;
                    dst.l2_inPod1 = src.l2_inPod1;
                    dst.l2_inPod2 = src.l2_inPod2;
                    dst.l2_outPod1 = src.l2_outPod1;
                    dst.l2_outPod2 = src.l2_outPod2;
                    dst.l2_srcVm = src.l2_srcVm;
                    dst.l2_firstInGroup=src.l2_firstInGroup;
                    dst.l2_secondInGroup=src.l2_secondInGroup;
                    dst.l2_firstOutGroup=src.l2_firstOutGroup;
                    dst.l2_secondOutGroup=src.l2_secondOutGroup;

                    dst.l3_inGroups = src.l3_inGroups;
                    dst.l3_outGroups = src.l3_outGroups;
                    dst.l3_inShowEnd = src.l3_inShowEnd;
                    dst.l3_outShowEnd = src.l3_outShowEnd;
                    dst.l3_firstInShow = src.l3_firstInShow;
                    dst.l3_secdInShow = src.l3_secdInShow;
                    dst.l3_firstOutShow = src.l3_firstOutShow;
                    dst.l3_secdOutShow = src.l3_secdOutShow;
                    dst.l3_inHost1 = src.l3_inHost1;
                    dst.l3_inHost2 = src.l3_inHost2;
                    dst.l3_outHost1 = src.l3_outHost1;
                    dst.l3_outHost2 = src.l3_outHost2;
                    dst.l3_inHost1Style = src.l3_inHost1Style;
                    dst.l3_inHost2Style = src.l3_inHost2Style;
                    dst.l3_outHost1Style = src.l3_outHost1Style;
                    dst.l3_outHost2Style = src.l3_outHost2Style;
                    dst.l3_inHostType1 =src.l3_inHostType1;
                    dst.l3_inHostType2 =src.l3_inHostType2;
                    dst.l3_outHostType1 = src.l3_outHostType1;
                    dst.l3_outHostType2 =src.l3_outHostType2;
                    dst.l3_inAz1 = src.l3_inAz1;
                    dst.l3_inAz2 = src.l3_inAz2;
                    dst.l3_outAz1 = src.l3_outAz1;
                    dst.l3_outAz2 = src.l3_outAz2;
                    dst.l3_inPod1 = src.l3_inPod1;
                    dst.l3_inPod2 = src.l3_inPod2;
                    dst.l3_outPod1 = src.l3_outPod1;
                    dst.l3_outPod2 = src.l3_outPod2;
                    dst.l3_srcVm = src.l3_srcVm;
                    dst.l3_firstInGroup=src.l3_firstInGroup;
                    dst.l3_secondInGroup=src.l3_secondInGroup;
                    dst.l3_firstOutGroup=src.l3_firstOutGroup;
                    dst.l3_secondOutGroup=src.l3_secondOutGroup;


                    //ucd
                    dst.physicalIpShow = src.physicalIpShow;
                    dst.searchDisable = src.searchDisable;
                    dst.flowShow = src.flowShow;

                    dst.statusShow = src.statusShow;
                    dst.noDataShow = src.noDataShow;

                    dst.status = src.status;
                    dst.picStatus = src.picStatus;
                    dst.erroInfoShow = src.erroInfoShow;
                    dst.erroInfo = src.erroInfo;
                }
                function initStatusFlag(){
                    copySrc2Dst(initInfo, $scope);
                    var options = $scope.physical_net.values;
                    $scope.physical_net.values = [];
                    if (null !== initInfo.physicalIp_value){
                        $scope.physicalIp.value = initInfo.physicalIp_value;
                        $scope.physical_net.value = "VM ID";
                        options[0].checked = true;
                        options[1].checked = false;
                    }
                    else if(null !== initInfo.publicIp_value){
                        $scope.publicIp.value = initInfo.publicIp_value;
                        $scope.physical_net.value = "Public IP";
                        options[0].checked = false;
                        options[1].checked = true;
                    }
                    $scope.physical_net.values= options;
                    $scope.remoteIp.value = initInfo.remoteIp;
                };*/

                function init(){
                    initInfo = locationFlowServ.getLocationFlowInfo();
                    if($scope.searchDisable)
                        $timeout(function(){init();},500);
                }
                init();
        }]
        var module = angular.module('common.config');
        module.tinyController('locationFlow.ctrl', locationFlowCtrl);

        return module;

    });