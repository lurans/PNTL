define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/warnFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";
        var warnFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "warnFlowServ","$window",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, warnFlowServ,$window) {
                $scope.i18n = i18n;
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
                        var az = $scope.azTextBox.value;
                        var pod = $scope.podTextBox.value;
                        var src = $scope.src_ip.value;
                        var dst = $scope.dst_ip.value;
                        var startTime = $("#start_time_id").widget().getDateTime();
                        var endTime = $("#end_time_id").widget().getDateTime();
                        var type = $("#type_id").widget().getSelectedLabel();
                        if(type === "请选择" || type === "Please choose"){
                            type = "";
                        }
                        if((startTime < endTime&&startTime != ""&&endTime != "")||(startTime === ""&&endTime === "")){
                            var searchData = {
                                "az_id":az,
                                "pod_id":pod,
                                "src_ip":src,
                                "dst_ip":dst,
                                "start_time":startTime,
                                "end_time":endTime,
                                "type":type
                            };
                            console.log(searchData);
                            postData(searchData);
                       }else if(startTime === ""){
                            alert(i18n.chkFlow_term_tip1);
                            $scope.search.disable = false;
                        }else if (endTime === ""){
                            alert(i18n.chkFlow_term_tip2);
                            $scope.search.disable = false;
                        }else if(startTime >= endTime){
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

                $scope.table = {
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
                function getTextLink()
                {
                    var textInfoPromise = warnFlowServ.getTextInfo();
                    textInfoPromise.then(function(responseData){
                        $scope.table.data = [];
                        for (var i = 0;i<responseData.length;i++){
                            if(responseData[i].delay != ""){
                                responseData[i].value = responseData[i].delay + "ms";
                                responseData[i].type = i18n.chkFlow_term_delayTime;
                            }else if(responseData[i].lossRate != ""){
                                responseData[i].value = responseData[i].lossRate;
                                responseData[i].type = i18n.chkFlow_term_packetsLossRate;
                            }
                        }
                        $scope.table.data = responseData;
                        $scope.table.totalRecords = responseData.length;

                    },function(responseData){
                        //showERRORMsg
                    });
                }
                var init = function()
                {
                    getTextLink();
                };
                init();
            }];

        var module = angular.module('common.config');
        module.tinyController('warnFlow.ctrl', warnFlowCtrl);
        return module;
    });