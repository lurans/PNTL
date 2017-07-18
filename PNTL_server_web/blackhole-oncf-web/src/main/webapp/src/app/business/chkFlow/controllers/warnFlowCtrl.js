define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/warnFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";
        var warnFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "warnFlowServ","$window","$interval",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, warnFlowServ,$window,$interval) {
                // $scope.btnCtrl = {
                //     text: "click me",
                //     desc: "fxxk u",
                //     doSth: function(evt) {
                //         console.log(evt);
                //     }
                // };
                $scope.i18n = i18n;
                var searchData = {
                    "az_id":"",
                    "pod_id":"",
                    "src_ip":"",
                    "dst_ip":"",
                    "start_time":"",
                    "end_time":"",
                    "type":""
                };
                $scope.search = {
                    "id":"search_id",
                    "text" : i18n.chkFlow_term_search_btn,
                    "width":"50px",
                    "disable":false,
                    "iconsClass":{
                        "left":"icoMoon-search"
                    },
                    "searchBtn":function () {
                        $scope.search.disable = true;
                        var az = $("#akTextBox_id").widget().getValue();
                        var pod = $("#podTextBox_id").widget().getValue();
                        var src = $("#src_ip_id").widget().getValue();
                        var dst = $("#dst_ip_id").widget().getValue();
                        var startTime = $("#start_time_id").widget().getDateTime();
                        var endTime = $("#end_time_id").widget().getDateTime();
                        var type = $("#type_id").widget().getSelectedLabel();
                        if((startTime<endTime&&startTime != ""&&endTime != "")||(startTime === ""&&endTime === "")){
                            searchData.az_id = az;
                            searchData.dst_ip = dst;
                            searchData.src_ip = src;
                            searchData.pod_id = pod;
                            searchData.start_time = startTime;
                            searchData.end_time = endTime;
                            searchData.type = type;
                            console.log(searchData);
                            postData(searchData);
                       }else if(startTime === ""){
                            alert(i18n.chkFlow_term_tip1);
                            $scope.search.disable = false;
                        }else if (endTime === ""){
                            alert(i18n.chkFlow_term_tip2);
                            $scope.search.disable = false;
                        }else if(startTime>endTime){
                            alert(i18n.chkFlow_term_tip3);
                            $scope.search.disable = false;
                        }
                    }

                };
                var postData = function(para){
                    var promise = warnFlowServ.postSearchData(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_submit_ok);
                        $scope.search.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_submit_err, "error");
                        $scope.search.disable = false;
                    });
                };
                $scope.azTextBox = {
                    "id": "akTextBox_id",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_ak_tooltip,
                    "width":"170px"
                };
                $scope.podTextBox = {
                    "id": "podTextBox_id",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_ak_tooltip,
                    "width":"170px"
                };
                $scope.type = {
                    "id":"type_id",
                    "values":[{
                        selectId : "1",
                        label : i18n.chkFlow_term_choose,
                        checked : true
                    }, {
                            selectId : "2",
                            label : i18n.chkFlow_term_delayTime,
                            checked : false
                    },{
                        selectId : "3",
                        label : i18n.chkFlow_term_packetsLossRate,
                        checked : false
                    }]
                };
                $scope.start_time = {
                    "id": "start_time_id",
                    "type" : "datetime",
                    "dateFormat" : "yy-mm-dd",
                    "timeFormat" : "hh:mm:ss"
                };
                $scope.end_time = {
                    "id": "end_time_id",
                    "type" : "datetime",
                    "dateFormat" : "yy-mm-dd",
                    "timeFormat" : "hh:mm:ss"
                };
                $scope.src_ip = {
                    "id": "src_ip_id",
                    "value": "",
                    "type" : "ipv4",
                    "tooltip":i18n.chkFlow_term_ip_tooltip
                };
                $scope.dst_ip = {
                    "id": "dst_ip_id",
                    "value": "",
                    "type" : "ipv4",
                    "tooltip":i18n.chkFlow_term_ip_tooltip
                }

                $scope.model = {
                    "id":"directivetableId",
                    data : [], //初始数据为空
                    totalRecords:0,
                    "columns" : [{
                        "sTitle" : i18n.chkFlow_term_DateTime,
                        "sWidth":"20%",
                        "mData":"time"
                    }, {
                        "sTitle" : "AZ",
                        "sWidth":"10%",
                        "mData":"az_id",
                        "bSortable":false
                    }, {
                        "sTitle" : "POD",
                        "sWidth":"10%",
                        "mData":"pod_id",
                        "bSortable":false
                    }, {
                        "sTitle" : "Src IP",
                        "sWidth":"15%",
                        "mData":"src_ip",
                        "bSortable":false
                    },{
                        "sTitle" : "Dst IP",
                        "sWidth":"15%",
                        "mData":"dst_ip",
                        "bSortable":false
                    },{
                        "sTitle" : i18n.chkFlow_term_Type,
                        "sWidth":"15%",
                        "mData":"type",
                        "bSortable":false
                    },{
                        "sTitle" : i18n.chkFlow_term_Value,
                        "sWidth":"150px",
                        "mData":"value",
                        "bSortable":false
                    }
                    ]};
                // $scope.reloadRoute = function () {
                //     $window.location.reload();
                //
                //
                // };
                function getTextLink()
                {
                    var textInfoPromise = warnFlowServ.getTextInfo();
                    textInfoPromise.then(function(responseData){
                        for (var i = 0;i<responseData.length;i++){
                            if(responseData[i].delay != ""){
                                responseData[i].value = responseData[i].delay + "ms";
                                responseData[i].type = i18n.chkFlow_term_delayTime;
                                $scope.model.data = [];
                                $scope.model.data = responseData;
                            }else if(responseData[i].lossRate != ""){
                                responseData[i].value = responseData[i].lossRate;
                                responseData[i].type = i18n.chkFlow_term_packetsLossRate;
                                $scope.model.data = [];
                                $scope.model.data = responseData;
                            }
                        }
                        $scope.model.totalRecords = responseData.length;

                    },function(responseData){
                        //showERRORMsg

                    });
                }
                var init = function()
                {
                    getTextLink();
                };
                init();
                var autoRefresh = $interval(getTextLink, 60000);
                $scope.stopAutoRefresh = function () {
                    if (autoRefresh) {
                        $interval.cancel(autoRefresh);
                        autoRefresh = null;
                    }
                }
                $scope.$on('$destroy', function (angularEvent, current, previous) {
                    $scope.stopAutoRefresh();
                });



            }];

        var module = angular.module('common.config');
        module.tinyController('warnFlow.ctrl', warnFlowCtrl);
        return module;
    });