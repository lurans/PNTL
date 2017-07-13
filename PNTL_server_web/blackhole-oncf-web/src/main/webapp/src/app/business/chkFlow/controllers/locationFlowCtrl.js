define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/chkFlowFixture"],
    function (i18n, commonException, $, Step, _StepDirective, ViewMode) {
        "use strict";

        var locationFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "locationFlowServ",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, locationFlowServ) {
                $scope.i18n = i18n;

                $scope.POD1_Style = null;

                $scope.first_POD=[];
                $scope.secd_POD=[];
                $scope.flowShow = false;
                $scope.POD1_Show = false;
                $scope.POD2_Show = false;
                $scope.POD1ShowEnd=false;
                $scope.POD2ShowEnd=false;

                $scope.az="AZ1";
                $scope.pod1="POD1";
                $scope.pod2="POD2";

                $scope.searchDisable = false;


                $scope.noDataShow = true;
                $scope.statusShow = false;

                $scope.az_diplay="AZ";
                $scope.pod_diplay="POD";

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

                $scope.extractDetailRouteData = function(data, dataType){
                    var router = [];
                    var size = 0;
                    for(var i in data){
                        if (dataType === data[i].POD)
                        {
                            size+=1;
                            router.push(data[i]);
                        }
                    }
                    if(size == 0)
                        return null;
                    return router;
                }
                function extractRouteData(data){
                    $scope.first_POD = $scope.extractDetailRouteData(data, "POD1");
                    $scope.secd_POD = $scope.extractDetailRouteData(data, "POD2");
                    $scope.flowShow=true;
                }
                function setHostStyle(routerlength,dataType){
                    var length_serv = 80*routerlength + 10;
                    if (1 == length){
                        length_serv += 20;
                    }
                    var HostStyle = {'height':length_serv + 'px'};
                    if ("POD1" == dataType){
                        $scope.POD1_style=HostStyle;
                        $scope.POD1ShowEnd=true;
                    }
                    else if("POD2" == dataType){
                        $scope.POD2_style=HostStyle;
                        $scope.POD2ShowEnd=true;
                    }
                }
                function statisDetailData(router,dataType){
                    var length=router.length;
                    setHostStyle(length,dataType);
                }
                function statisData(){
                    statisDetailData($scope.first_POD, "POD1");
                    statisDetailData($scope.secd_POD, "POD2");

                    var length_timeLine = ($scope.first_POD.length + $scope.secd_POD.length ) * 80 - 20;
                    var timeLineStyle = {'height':length_timeLine + 'px'};
                    $scope.time_line_style = timeLineStyle;
                }
                function init(){
                    $scope.noDataShow=false;
                    $scope.flowShow=true;
                    $scope.POD1_Show = true;
                    $scope.POD2_Show = true;
                    $scope.search.disable = false;

                    var initInfo = [
                        {
                            "AZ":"AZ1",
                            "POD":"POD1",
                            "ip":"192.168.1.1",
                            "packets":0,
                        },
                        {
                            "AZ":"AZ1",
                            "POD":"POD1",
                            "ip":"192.168.1.10",
                            "packets":0,
                        },
                        {
                            "AZ":"AZ1",
                            "POD":"POD1",
                            "ip":"192.168.1.1",
                            "packets":100,
                        },
                        {
                            "AZ":"AZ1",
                            "POD":"POD1",
                            "ip":"192.168.1.10",
                            "packets":0,
                        },
                        {
                            "AZ":"AZ1",
                            "POD":"POD2",
                            "ip":"192.168.2.1",
                            "packets":0,
                        },
                        {
                            "AZ":"AZ1",
                            "POD":"POD2",
                            "ip":"192.168.2.10",
                            "packets":0,
                        },
                        {
                            "AZ":"AZ1",
                            "POD":"POD2",
                            "ip":"192.168.2.19",
                            "packets":0,
                        }
                    ];

                    extractRouteData(initInfo);
                    statisData();
                }
                init();
        }]
        var module = angular.module('common.config');
        module.tinyController('locationFlow.ctrl', locationFlowCtrl);

        return module;

    });