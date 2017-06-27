define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var configFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;
                //$scope.data = [{"name":1,"value":2,"updateTime":3,"description":4},{"name":1,"value":2,"updateTime":3,"description":4}];
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
                $scope.btn = {
                    "id" : "configBtnId",
                    "text" : i18n.chkFlow_term_confirm
                };

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