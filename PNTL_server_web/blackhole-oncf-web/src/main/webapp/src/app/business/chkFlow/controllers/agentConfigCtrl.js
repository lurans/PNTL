define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var variableConfigCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;
                $scope.isVariableCollapsed = true;

                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#variableSubmitBtnId"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });

                $scope.variable = {
                    "probeIntervalTime" : i18n.chkFlow_term_probe_interval_time_name,
                    "probePortCount": i18n.chkFlow_term_probe_port_count_name,
                    "reportIntervalTime": i18n.chkFlow_term_report_interval_time_name,
                    "packetsNum" : i18n.chkFlow_term_probe_packets_number_name,
                    "timeDelay" : i18n.chkFlow_term_probe_max_time_delay_name,
                    "packetsLossRate" : i18n.chkFlow_term_probe_max_loss_rate_name,
                    "dscp":i18n.chkFlow_term_dscp_name,
                    "lossPkgTimeOut":i18n.chkFlow_term_loss_pkg_timeout_name,
                    "lossPkgNum":i18n.chkFlow_term_max_loss_pkg_num_name,
                    "pkgSize":i18n.chkFlow_term_pkg_size
                };
               
                $scope.probeRoundTextBox = {
                    "id": "probeRoundTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_probe_interval_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [1,120]
                        }]
                };
                $scope.probePortTextBox = {
                    "id": "probePortTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_probe_port_count_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [1,100]
                        }]
                };
                $scope.reportRoundTextBox = {
                    "id": "reportRoundTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_report_interval_time_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [5,300]
                        }]
                };
                $scope.packetsNumTextBox = {
                    "id": "packetsNumTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_max_loss_rate_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "integer"
                        },
                        {
                            "validFn" : "regularCheck",
                            "params" : "/0|100/"
                        }]
                };
                $scope.timeDelayTextBox = {
                    "id": "timeDelayTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_max_time_delay_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [1,2000]
                        }]
                };
                $scope.packetsLossRateTextBox = {
                    "id": "packetsLossRateTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_packets_num_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [1,100]
                        }
                        ]
                };
                $scope.dscpTextBox = {
                    "id": "dscpTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_dscp_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [0,63]
                        }
                    ]
                };
                $scope.lossPkgTimeOutTextBox = {
                    "id": "lossPkgTimeOutTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_lossPkg_timeout_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        }, 
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [1,5]
                        }]
                };
                $scope.lossPkgNumTextBox = {
                    "id": "lossPkgNumTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_lossPkg_num_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [1,10]
                        }]
                };
                $scope.pkgSizeTextBox = {
                    "id": "pkgSizeTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_pkg_size_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [40,2000]
                        }]
                };


                $scope.variableResetBtn = {
                    "id" : "variableResetBtnId",
                    "text" : i18n.chkFlow_term_reset_btn,
                    "disable":false
                };
                $scope.variableSubmitBtn = {
                    "id" : "variableSubmitBtnId",
                    "text" : i18n.chkFlow_term_submit,
                    "disable":false
                };
                $scope.probeStartBtn = {
                    "id" : "probeStartID",
                    "text" : i18n.chkFlow_term_start_Probe_btn,
                    "disable":false
                };
                $scope.probeStopBtn = {
                    "id" : "probeStopID",
                    "text" : i18n.chkFlow_term_stop_Probe_btn,
                    "disable":false
                };

                function getParaFromInput(){
                    var probeRound = $scope.probeRoundTextBox.value;
                    var probePort = $scope.probePortTextBox.value;
                    var reportRound = $scope.reportRoundTextBox.value;
                    var packetsNum = $scope.packetsNumTextBox.value;
                    var timeDelay = $scope.timeDelayTextBox.value;
                    var pkgLossRate = $scope.packetsLossRateTextBox.value;
                    var dscp = $scope.dscpTextBox.value;
                    var lossPkgTimeOut = $scope.lossPkgTimeOutTextBox.value;
                    var dropPkgThresh = $scope.lossPkgNumTextBox.value;
                    var package_size = $scope.pkgSizeTextBox.value;

                    var probeRoundNumber = parseInt(probeRound);
                    var reportRoundNumber = parseInt(reportRound);
                    if(probeRoundNumber >= reportRoundNumber){
                        var para1 = "";
                        $scope.variableSubmitBtn.disable = false;
                        return para1;
                    }else{
                        var para = {"probe_period":probeRound,
                            "port_count":probePort,
                            "report_period":reportRound,
                            "pkg_count":packetsNum,
                            "delay_threshold":timeDelay,
                            "lossRate_threshold":pkgLossRate,
                            "dscp":dscp,
                            "lossPkg_timeout":lossPkgTimeOut,
                            "dropPkgThresh":dropPkgThresh,
                            "package_size":package_size
                        };
                        return para;
                    }
                };
                var getVariableConfig = function(){
                    var promise = configFlowServ.getVariableConfig();
                    promise.then(function(responseData){
                        $scope.probeRoundTextBox.value = responseData.probe_period;
                        $scope.probePortTextBox.value = responseData.port_count;
                        $scope.reportRoundTextBox.value = responseData.report_period;
                        $scope.packetsNumTextBox.value = responseData.pkg_count;
                        $scope.timeDelayTextBox.value = responseData.delay_threshold;
                        $scope.packetsLossRateTextBox.value = responseData.lossRate_threshold;
                        $scope.dscpTextBox.value = responseData.dscp;
                        $scope.lossPkgTimeOutTextBox.value = responseData.lossPkg_timeout;
                        $scope.lossPkgNumTextBox.value = responseData.dropPkgThresh;
                        $scope.pkgSizeTextBox.value = responseData.package_size;

                        if($scope.status != "first"){
                            commonException.showMsg(i18n.chkFlow_term_reset_ok);
                        } else{
                            $scope.status = "notFirst";
                        }

                        $scope.variableResetBtn.disable = false;
                    },function(responseData){
                        if($scope.status != "first"){
                            commonException.showMsg(i18n.chkFlow_term_reset_err,"error");
                        } else{
                            commonException.showMsg(i18n.chkFlow_term_read_failed_config, "error");
                            $scope.status = "notFirst";
                        }

                        $scope.variableResetBtn.disable = false;
                    });
                };
                var postVariableConfig = function(para){
                    var promise = configFlowServ.postAgentVariableConfig(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_config_ok);
                        $scope.variableSubmitBtn.disable = false;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkFlow_term_config_err, "error");
                        $scope.variableSubmitBtn.disable = false;
                    });
                };
                var postProbeStart = function(para){
                    var promise = configFlowServ.startProbe(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkFlow_term_start_ok);
                        $scope.probeStartBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_start_err, "error");
                        $scope.probeStartBtn.disable = false;
                    });
                };
                var postProbeStop = function(para){
                    var promise = configFlowServ.stopProbe(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkFlow_term_stop_ok);
                        $scope.probeStopBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_stop_err, "error");
                        $scope.probeStopBtn.disable = false;
                    });
                };

                $scope.variableResetBtnOK = function(){
                    $scope.variableResetBtn.disable = true;
                    getVariableConfig();
                };
                $scope.variableSubmitBtnOK = function(){
                    $scope.variableSubmitBtn.disable = true;
                    if (!window.tinyWidget.UnifyValid.FormValid((".input_content"))){
                        divTip.option("content",i18n.chkFlow_term_input_valid);
                        divTip.show(1000);
                        $scope.variableSubmitBtn.disable = false;
                        return;
                    }
                    var para = getParaFromInput();
                    if(para === ""){
                        divTip.option("content",i18n.chkFlow_term_dscp_tip);
                        divTip.show(1000);
                    }else {
                        postVariableConfig(para);
                    }


                };
                $scope.probeStartBtnOK = function(){
                    $scope.probeStartBtn.disable = true;
                    var installConfirmWindow = {
                        title:i18n.chkFlow_term_start_probe_confirm,
                        height : "250px",
                        width : "400px",
                        content: "<p style='color: #999'><span style='font-size: 14px;color: #ff9955'>" + i18n.chkFlow_term_start_Probe_btn + "</span>" + i18n.chkFlow_term_start_Probel_explain + "</p><p style='text-align:center;margin-top: 15px;color: #999;font-size: 14px;'>" + i18n.chkFlow_term_confirm_start + "</p>",
                        closeable:false,
                        resizable:false,
                        buttons:[{
                            key:"btnOK",
                            label : i18n.chkFlow_term_ok,//按钮上显示的文字
                            focused : false,//默认焦点
                            handler : function(event) {//点击回调函数
                                installConfirmWin.destroy();
                                var para={};
                                postProbeStart(para);
                            }
                        }, {
                            key:"btnCancel",
                            label : i18n.chkFlow_term_cancel,
                            focused : true,
                            handler : function(event) {
                                installConfirmWin.destroy();
                                $scope.probeStartBtn.disable = false;
                            }
                        }]
                    }
                    var installConfirmWin = new tinyWidget.Window(installConfirmWindow);
                    installConfirmWin.show();
                };
                $scope.probeStopBtnOK = function(){
                    $scope.probeStopBtnOK.disable = true;
                    var installConfirmWindow = {
                        title:i18n.chkFlow_term_stop_probe_confirm,
                        height : "250px",
                        width : "400px",
                        content: "<p style='color: #999'><span style='font-size: 14px;color: #ff9955'>" + i18n.chkFlow_term_stop_Probe_btn + "</span>" + i18n.chkFlow_term_stop_Probel_explain + "</p><p style='text-align:center;margin-top: 15px;color: #999;font-size: 14px;'>" + i18n.chkFlow_term_confirm_stop + "</p>",
                        closeable:false,
                        resizable:false,
                        buttons:[{
                            key:"btnOK",
                            label : i18n.chkFlow_term_ok,//按钮上显示的文字
                            focused : false,//默认焦点
                            handler : function(event) {//点击回调函数
                                installConfirmWin.destroy();
                                var para={};
                                postProbeStop(para);
                            }
                        }, {
                            key:"btnCancel",
                            label : i18n.chkFlow_term_cancel,
                            focused : true,
                            handler : function(event) {
                                installConfirmWin.destroy();
                                $scope.probeStopBtnOK.disable = false;
                            }
                        }]
                    }
                    var installConfirmWin = new tinyWidget.Window(installConfirmWindow);
                    installConfirmWin.show();
                };

                function init(){
                    $scope.status = "first";
                    getVariableConfig();
                }
                init();
            }
        ]

        var module = angular.module('common.config');
        module.tinyController('variableConfig.ctrl', variableConfigCtrl);
        return module;
        });