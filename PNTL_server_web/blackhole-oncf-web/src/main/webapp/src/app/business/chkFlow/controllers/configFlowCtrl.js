define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var configFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;
                //$scope.data = [{"name":1,"value":2,"updateTime":3,"description":4},{"name":1,"value":2,"updateTime":3,"description":4}];
                $scope.isDeployCollapsed = true;
                $scope.isVariableCollapsed = true;
                $scope.isFileCollapsed = true;
                $scope.variable = {
                    "detectIntervalTime" : i18n.chkFlow_term_detect_interval_time_name,
                    "packetsNum" : i18n.chkFlow_term_detect_packets_number_name,
                    "timeDelay" : i18n.chkFlow_term_detect_max_time_delay_name,
                    "packetsLoss" : i18n.chkFlow_term_detect_max_loss_rate_name
                };
                $scope.detectRoundTextBox = {
                    "id": "detectRoundTextBoxId",
                    "value": "",
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number",
                        }]
                };
                $scope.packetsNumTextBox = {
                    "id": "packetsNumTextBoxId",
                    "value": "",
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number",
                        }]
                };
                $scope.timeDelayTextBox = {
                    "id": "timeDelayTextBoxId",
                    "value": "",
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number",
                        }]
                };
                $scope.packetsLossTextBox = {
                    "id": "packetsLossTextBoxId",
                    "value": "",
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "number",
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
                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#variableBtnId"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });

                function sleep (time) {
                    return new Promise((resolve) => setTimeout(resolve, time));
                }

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
                    var promise = configFlowServ.variableConfig(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkflow_term_config_ok);
                        $scope.variableBtn.disable = false;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkflow_term_config_err, "error");
                        $scope.variableBtn.disable = false;
                    });
                }
                function getParaFromInput(){
                    var detectRound = $scope.detectRoundTextBox.value;
                    var packetsNum = $scope.packetsNumTextBox.value;
                    var timeDelay = $scope.timeDelayTextBox.value;
                    var packetsLoss = $scope.packetsLossTextBox.value;
                    var para = {"probe_interval":detectRound, "pkg_count":packetsNum,
                                "delay_threshold":timeDelay,"lossRate_threshold":packetsLoss};
                    return para;
                }
                $scope.variableBtnOK = function()
                {
                    $scope.variableBtn.disable = true;
                    var para = getParaFromInput();
                    var detect_round=parseInt(para.probe_interval)
                    if(detect_round<60&&detect_round>0)
                    {
                        postVariableConfig(para);
                    }
                    else if(-1==detect_round||0==detect_round)
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
                $scope.fileUpload = {
                    "action" : "upload_fles.html", //文件上传地址路径
                    "multi" : "true",
                    "completeDefa" : function(event, responseText) {
                        alert(responseText);
                    }
               }
            }
        ]

        var module = angular.module('common.config');
        module.tinyController('configFlow.ctrl', configFlowCtrl);
        return module;
        });