define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var configFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;
                //$scope.data = [{"name":1,"value":2,"updateTime":3,"description":4},{"name":1,"value":2,"updateTime":3,"description":4}];
                $scope.isFileCollapsed = true;
                $scope.isDeployCollapsed = true;
                $scope.isVariableCollapsed = false;
                $scope.variable = {
                    "probeIntervalTime" : i18n.chkFlow_term_probe_interval_time_name,
                    "packetsNum" : i18n.chkFlow_term_probe_packets_number_name,
                    "timeDelay" : i18n.chkFlow_term_probe_max_time_delay_name,
                    "packetsLoss" : i18n.chkFlow_term_probe_max_loss_rate_name
                };
                $scope.probeRoundTextBox = {
                    "id": "probeRoundTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_probe_interval_tooltip,
                    "validate": [
                        {
                            "validFn" : "required",
                        },
                        {
                            "validFn" : "number",
                        },
                        {
                            "validFn" : "rangeValue",
                            "params" : [-1,60],
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
                            "validFn" : "number",
                        },
                        {
                            "validFn" : "minValue",
                            "params" : 1,
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
                            "validFn" : "number",
                        },
                        {
                            "validFn" : "minValue",
                            "params" : 0,
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
                            "validFn" : "number",
                        },
                        {
                            "validFn" : "minValue",
                            "params" : 0,
                        }]
                };
                $scope.deployBtn = {
                    "id" : "deployBtnId",
                    "text" : i18n.chkFlow_term_deploy_ok,
                    "disable":false,
                };
                $scope.variableBtn = {
                    "id" : "variableBtnId",
                    "text" : i18n.chkFlow_term_confirm,
                    "disable":false,
                };
                $scope.fileUpload = {
                    "action" : "upload_fles.html", //文件上传地址路径
                    "multi" : "true",
                    "completeDefa" : function(event, responseText) {
                        alert(responseText);
                    }
               }
                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#variableBtnId"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });
                var tabsLeft = new tinyWidget.Tabs({
                    "id" : "myTabsLeft",
                    "position" : "left",
                    //"closable" : true
                });

                /*function sleep (time) {
                    return new Promise((resolve) => setTimeout(resolve, time));
                }
                // 用法
                sleep(500).then(() => {
                    // 这里写sleep之后需要去做的事情
                })*/

                var postFirstDeploy = function(para)
                {
                    var promise = configFlowServ.firstDeploy(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkflow_term_deploy_ok);
                        $scope.deployBtn.disable = false;
                        //console.log('1');
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkflow_term_deploy_err, "error");
                        //$scope.deployBtn.disable = false;
                    });
                    //console.log('2');
                }
                $scope.deployBtnOK = function(){
                    $scope.deployBtn.disable = true;
                    var para={};
                    postFirstDeploy(para);
                    //console.log('3');
                }
                var postVariableConfig = function(para)
                {
                    var promise = configFlowServ.postVariableConfig(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkflow_term_config_ok);
                        $scope.variableBtn.disable = false;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkflow_term_config_err, "error");
                        $scope.variableBtn.disable = false;
                    });
                };
                function getParaFromInput(){
                    var probeRound = $scope.probeRoundTextBox.value;
                    var packetsNum = $scope.packetsNumTextBox.value;
                    var timeDelay = $scope.timeDelayTextBox.value;
                    var packetsLoss = $scope.packetsLossTextBox.value;
                    var para = {"probe_interval":probeRound, "pkg_count":packetsNum,
                                "delay_threshold":timeDelay,"lossRate_threshold":packetsLoss};
                    return para;
                }
                $scope.variableBtnOK = function()
                {
                    $scope.variableBtn.disable = true;
                    var para = getParaFromInput();
                    var probe_round=parseInt(para.probe_interval)
                    if(probe_round<60&&probe_round>0)
                    {
                        postVariableConfig(para);
                    }
                    else if(-1==probe_round||0==probe_round)
                    {
                        var tinyWindowOptions = {
                            title : i18n.chkFlow_term_confirm_window,
                            height : "250px",
                            width : "400px",
                            content: "<p>"+i18n.chkFlow_term_negative+"</p><p>"+i18n.chkFlow_term_zero+"</p>",
                            resizable:true,
                            buttons:[{
                                key:"btnOK",
                                label : 'OK',//按钮上显示的文字
                                focused : false,//默认焦点
                                handler : function(event) {//点击回调函数
                                    //console.log("Event Triggered for the ok button");
                                    postVariableConfig(para);
                                    win.destroy();
                                }
                            }, {
                                key:"btnCancel",
                                label : 'Cancel',
                                focused : true,
                                handler : function(event) {
                                    //console.log("Event triggered for the cancel button");
                                    win.destroy();
                                    $scope.variableBtn.disable = false;
                                }
                            }]
                        };
                        var win = new tinyWidget.Window(tinyWindowOptions);
                        win.show();
                    }
                    else
                    { //tips:输入非法
                        divTip.option("content",$scope.i18n.chkFlow_term_input_valid);
                        divTip.show(3000);
                        $scope.variableBtn.disable = false;
                        return;
                    }
                }
                var getVariableConfig = function()
                 {
                     var promise = configFlowServ.getVariableConfig();
                     promise.then(function(responseData){
                         $scope.probeRoundTextBox.value = responseData.probe_interval;
                         $scope.packetsNumTextBox.value = responseData.pkg_count;
                         $scope.timeDelayTextBox.value = responseData.delay_threshold;
                         $scope.packetsLossTextBox.value = responseData.lossRate_threshold;
                     },function(responseData){
                         //showERRORMsg
                         commonException.showMsg(i18n.chkFlow_term_read_failed_config, "error");
                     });
                 };
                function init()
                {
                    getVariableConfig();
                }
                init();
            }
        ]

        var module = angular.module('common.config');
        module.tinyController('configFlow.ctrl', configFlowCtrl);
        return module;
        });