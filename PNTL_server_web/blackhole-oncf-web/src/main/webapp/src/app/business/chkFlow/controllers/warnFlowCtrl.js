define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/warnFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";
        var warnFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "warnFlowServ","$window",
            function ($scope, $rootScope, $state, $sce, $compile, $timeout, warnFlowServ,$window) {
                $scope.i18n = i18n;
                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#search_id"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });
                $scope.azTextBox = {
                    "id": "akTextBox_id",
                    "value": i18n.chkFlow_term_no_support,
                    "tooltip":i18n.chkFlow_term_ak_tooltip,
                    "width":"130px",
                    "disable":true
                };
                $scope.podTextBox = {
                    "id": "podTextBox_id",
                    "value": i18n.chkFlow_term_no_support,
                    "tooltip":i18n.chkFlow_term_ak_tooltip,
                    "width":"130px",
                    "disable":true
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
                $scope.dateTime = {
                    "id1": "start_time_id",
                    "id2": "end_time_id",
                    "type" : "datetime",
                    "dateFormat" : "yy-mm-dd",
                    "timeFormat" : "hh:mm:ss",
                    "minDate1" : "",
                    "maxDate1" : "",
                    "minDate2" : "",
                    "maxDate2" : "",
                    "onCloseMin" : function(date) {
                        $scope.dateTime.minDate2 = date;
                        $scope.$digest();
                    },
                    "onCloseMax" : function(date) {
                        $scope.dateTime.maxDate1 = date;
                        $scope.$digest();
                    }
                };
                $scope.src_ip = {
                    "id": "src_ip_id",
                    "value": "",
                    "type" : "ipv4",
                    "tooltip":i18n.chkFlow_term_ip_tooltip,
                    "validate": [
                        {
                            "validFn" : "ipv4"
                        }]
                };
                $scope.dst_ip = {
                    "id": "dst_ip_id",
                    "value": "",
                    "type" : "ipv4",
                    "tooltip":i18n.chkFlow_term_ip_tooltip,
                    "validate": [
                        {
                            "validFn" : "ipv4"
                        }]
                };
                $scope.search = {
                    "id":"search_id",
                    "text" : i18n.chkFlow_term_search_btn,
                    "width":"50px",
                    "disable":false,
                    "iconsClass":{
                        "left":"icoMoon-search"
                    }
                };

                $scope.table = {
                    "id":"directiveTableId",
                    data : [], //初始数据为空
                    totalRecords:0,
                    displayLength:10,
                    paginationStyle:"full_numbers",
                    curPage:{"pageIndex":1},
                    "columns" : [{
                        "sTitle" : i18n.chkFlow_term_DateTime,
                        "sWidth":"16%",
                        "mData":"time",
                        "bSortable":false
                    }, {
                        "sTitle" : i18n.chkFlow_term_az,
                        "sWidth":"10%",
                        "mData":"az_id",
                        "bSortable":false
                    }, {
                        "sTitle" : i18n.chkFlow_term_pod,
                        "sWidth":"10%",
                        "mData":"pod_id",
                        "bSortable":false
                    }, {
                        "sTitle" : i18n.chkFlow_term_src_ip,
                        "sWidth":"20%",
                        "mData":"src_ip",
                        "bSortable":false
                    },{
                        "sTitle" : i18n.chkFlow_term_dst_ip,
                        "sWidth":"20%",
                        "mData":"dst_ip",
                        "bSortable":false
                    },{
                        "sTitle" : i18n.chkFlow_term_Type,
                        "sWidth":"12%",
                        "mData":"type",
                        "bSortable":false
                    },{
                        "sTitle" : i18n.chkFlow_term_Value,
                        "sWidth":"12%",
                        "mData":"value",
                        "bSortable":false
                    }
                    ],
                    callback:function (evtObj) {
                        $scope.status = "notFirstBtnOk";
                        var para = getValueFromInput();
                        para.offset = (evtObj.currentPage -1) * $scope.table.displayLength;
                        para.limit = $scope.table.displayLength;
                        postSearchData(para);
                    },
                    changeSelect:function (evtObj) {
                        $scope.status = "notFirstBtnOk";
                        var para = getValueFromInput();
                        para.limit = evtObj.displayLength;
                        para.offset = (evtObj.currentPage -1) * evtObj.displayLength;
                        postSearchData(para);
                    }
                };

                $scope.searchBtnOK = function () {
                    $scope.search.disable = true;
                    if (!window.tinyWidget.UnifyValid.FormValid((".container-fluid"))) {
                        divTip.option("content", i18n.chkFlow_term_input_valid);
                        divTip.show(1000);
                        $scope.search.disable = false;
                        return;
                    }
                    $scope.status = "firstBtnOK";
                    var searchData = getValueFromInput();
                    if (searchData === "") {
                        $scope.search.disable = false;
                    } else {
                        searchData.offset = 0;
                        searchData.limit = $scope.table.displayLength;
                        postSearchData(searchData);
                    }
                };

                var getValueFromInput = function(){
                    var az = $scope.azTextBox.value;
                    var pod = $scope.podTextBox.value;
                    var src = $scope.src_ip.value;
                    var dst = $scope.dst_ip.value;
                    var startTime = $("#start_time_id").widget().getDateTime();
                    var endTime = $("#end_time_id").widget().getDateTime();
                    var type = $("#type_id").widget().getSelectedLabel();
                    var selectType = "";
                    if(type === i18n.chkFlow_term_delayTime){
                        selectType ="2"
                    } else if(type === i18n.chkFlow_term_packetsLossRate){
                        selectType = "1"
                    } else{
                        selectType = "";
                    }
                    if((startTime !== ""&&endTime !== "")||(startTime === ""&&endTime === "")){
                        var searchData = {
                            "az_id":az,
                            "pod_id":pod,
                            "src_ip":src,
                            "dst_ip":dst,
                            "start_time":startTime,
                            "end_time":endTime,
                            "type":selectType
                        };
                        return searchData;
                    }else if(startTime === ""){
                        divTip.option("content",i18n.chkFlow_term_tip1);
                        divTip.show(1000);
                        return "";
                    }else if (endTime === ""){
                        divTip.option("content",i18n.chkFlow_term_tip2);
                        divTip.show(1000);
                        return "";
                    }
                };

                var setTableDataType = function(result,resultLength) {
                    for (var i = 0;i<resultLength;i++){
                        if(result[i].delay !== ""){
                            result[i].value = result[i].delay + "ms";
                            result[i].type = i18n.chkFlow_term_delayTime;
                        }else if(result[i].lossRate !== ""){
                            result[i].value = result[i].lossRate;
                            result[i].type = i18n.chkFlow_term_packetsLossRate;
                        }
                    }
                };

                var postSearchData = function(para){
                    if(!para){
                        para = {
                            "az_id":"",
                            "pod_id":"",
                            "src_ip":"",
                            "dst_ip":"",
                            "start_time":"",
                            "end_time":"",
                            "type":"",
                            "limit" : $scope.table.displayLength,
                            "offset" : 0
                        };
                    }
                    if(para.offset === 0){
                        $scope.table.curPage={"pageIndex":1};
                    }
                    $scope.table.totalRecords = 0;
                    $scope.table.data = [];
                    var result = [];

                    var promise = warnFlowServ.postSearchData(para);
                    promise.then(function(responseData){
                        $scope.table.totalRecords = parseInt(responseData.totalRecords);

                        result = responseData.result;
                        setTableDataType(result, result.length);
                        $scope.table.data = result;

                        if($scope.status === "firstBtnOK")
                        {
                            commonException.showMsg(i18n.chkFlow_term_submit_ok);
                        } else {
                            $scope.status = "notFirstBtnOK";
                        }
                        $scope.search.disable = false;
                    },function(responseData){
                        if($scope.status === "firstBtnOK")
                        {
                            commonException.showMsg(i18n.chkFlow_term_submit_err, "error");
                        } else {
                            commonException.showMsg(i18n.chkFlow_term_init_submit_err, "error");
                            $scope.status = "notFirstBtnOK";
                        }
                        $scope.search.disable = false;
                    });
                };

                var getInitialTotalData = function(){
                    var params = {
                        "limit" : $scope.table.displayLength,
                        "offset" : 0
                    };
                    $scope.table.totalRecords = 0;
                    $scope.table.data = [];
                    var tableData = [];
                    var length = 0;
                    var promise = warnFlowServ.getInitialTotalData(params);
                    promise.then(function(responseData){
                        $scope.table.totalRecords = parseInt(responseData.totalRecords);
                        tableData = responseData.result;
                        length = responseData.result.length;
                        setTableDataType(tableData, length);
                        $scope.table.data = tableData;
                    },function (responseData) {

                    });
                };

                var init = function()
                {
                    getInitialTotalData();
                };
                init();
            }];

        var module = angular.module('common.config');
        module.tinyController('warnFlow.ctrl', warnFlowCtrl);
        return module;
    });