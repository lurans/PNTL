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
                    element : ("#variableBtnId"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });
                // var minData = 60;
                // $scope.$watch(function () {
                //     return $scope.probeRoundTextBox.value;
                // },function (newvalue,oldvalue) {
                //     minData = newvalue;
                //     console.log(newvalue,oldvalue);
                //     console.log(minData);
                // })
                $scope.variable = {
                    "probeIntervalTime" : i18n.chkFlow_term_probe_interval_time_name,
                    "probePortCount": i18n.chkFlow_term_probe_port_count_name,
                    "reportIntervalTime": i18n.chkFlow_term_report_interval_time_name,
                    "packetsNum" : i18n.chkFlow_term_probe_packets_number_name,
                    "timeDelay" : i18n.chkFlow_term_probe_max_time_delay_name,
                    "packetsLoss" : i18n.chkFlow_term_probe_max_loss_rate_name,
                    "dscp":i18n.chkFlow_term_dscp_name,
                    "lossPkgTimeOut":i18n.chkFlow_term_loss_pkg_timeout_name,
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
                            "params" : [60,1800]
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
                            "params" : [60,1800]
                        }]
                };
                $scope.packetsNumTextBox = {
                    "id": "packetsNumTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_packets_num_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "integer"
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [0,100]
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
                            "validFn" : "minValue",
                            "params" : 0
                        }]
                };
                $scope.packetsLossTextBox = {
                    "id": "packetsLossTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_max_loss_rate_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "regularCheck",
                            "params" : "/0|100/"
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
                            "validFn" : "minValue",
                            "params" : 0
                        }]
                };
                $scope.variableBtn = {
                    "id" : "variableBtnId",
                    "text" : i18n.chkFlow_term_confirm,
                    "disable":false
                };
                $scope.variableBtnOK = function(){
                    $scope.variableBtn.disable = true;
                    if (!window.tinyWidget.UnifyValid.FormValid((".input_content"))){
                        divTip.option("content",i18n.chkFlow_term_input_valid);
                        divTip.show(30000);
                        $scope.variableBtn.disable = false;
                        return;
                    }

                    // $scope.variableBtn.disable = true;
                    var para = getParaFromInput();
                    var probe_round=parseInt(para.probe_period);
                    postVariableConfig(para);
                    // if(probe_round<60&&probe_round>0)
                    // {
                    //     postVariableConfig(para);
                    // }
                    // else if(-1==probe_round||0==probe_round)
                    // {
                    //     var tinyWindowOptions = {
                    //         title : i18n.chkFlow_term_confirm_window,
                    //         height : "250px",
                    //         width : "400px",
                    //         content: "<p>"+i18n.chkFlow_term_negative+"</p><p>"+i18n.chkFlow_term_zero+"</p>",
                    //         resizable:true,
                    //         buttons:[{
                    //             key:"btnOK",
                    //             label : 'OK',//按钮上显示的文字
                    //             focused : false,//默认焦点
                    //             handler : function(event) {//点击回调函数
                    //                 postVariableConfig(para);
                    //                 win.destroy();
                    //             }
                    //         }, {
                    //             key:"btnCancel",
                    //             label : 'Cancel',
                    //             focused : true,
                    //             handler : function(event) {
                    //                 win.destroy();
                    //                 $scope.variableBtn.disable = false;
                    //             }
                    //         }]
                    //     };
                    //     var win = new tinyWidget.Window(tinyWindowOptions);
                    //     win.show();
                    // }
                };
                // $scope.variableBtn.disable = true;
                function getParaFromInput(){
                    var probeRound = $scope.probeRoundTextBox.value;
                    var probePort = $scope.probePortTextBox.value;
                    var reportRound = $scope.reportRoundTextBox.value;
                    var packetsNum = $scope.packetsNumTextBox.value;
                    var timeDelay = $scope.timeDelayTextBox.value;
                    var packetsLoss = $scope.packetsLossTextBox.value;
                    var dscp = $scope.dscpTextBox.value;
                    var lossPkgTimeOut = $scope.lossPkgTimeOutTextBox.value;
                    // var ak = $scope.akTextBox.value;
                    // var sk = $scope.skTextBox.value;
                    if(probeRound>reportRound){
                        alert(i18n.chkFlow_term_alert_tip)
                        $scope.variableBtn.disable = false;
                    }else{
                        var para = {"probe_period":probeRound,
                            "port_count":probePort,
                            "report_period":reportRound,
                            "pkg_count":packetsNum,
                            "delay_threshold":timeDelay,
                            "lossRate_threshold":packetsLoss,
                            "dscp":dscp,
                            "lossPkg_timeout":lossPkgTimeOut
                            // "ak":ak,
                            //  "sk":sk
                        };
                    }
                    return para;
                };
                var postVariableConfig = function(para){
                    var promise = configFlowServ.postVariableConfig(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_config_ok);
                        $scope.variableBtn.disable = false;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkFlow_term_config_err, "error");
                        $scope.variableBtn.disable = false;
                    });
                };
                var getVariableConfig = function(){
                    var promise = configFlowServ.getVariableConfig();
                    promise.then(function(responseData){
                        $scope.probeRoundTextBox.value = responseData.probe_period;
                        $scope.probePortTextBox.value = responseData.port_count;
                        $scope.reportRoundTextBox.value = responseData.report_period;
                        $scope.packetsNumTextBox.value = responseData.pkg_count;
                        $scope.timeDelayTextBox.value = responseData.delay_threshold;
                        $scope.packetsLossTextBox.value = responseData.lossRate_threshold;
                        $scope.dscpTextBox.value = responseData.dscp;
                        $scope.lossPkgTimeOutTextBox.value = responseData.lossPkg_timeout;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkFlow_term_read_failed_config, "error");
                    });
                };
                function init(){
                    getVariableConfig();
                }
                init();
              

            }
        ]

        var module = angular.module('common.config');
        module.tinyController('variableConfig.ctrl', variableConfigCtrl);
        return module;
        });