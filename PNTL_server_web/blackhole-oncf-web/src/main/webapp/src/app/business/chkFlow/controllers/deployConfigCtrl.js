define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var deployConfigCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;
                $scope.isDeployCollapsed = true;
                $scope.installFileUpload = {
                    "id":"installFileUpload_id",
                    "inputValue":"",
                    "fileObjName":"X-File",
                    "maxSize":8*1024*1024,//单个文件大小不超过 8M
                    "maxTotalSize":20*1024*1024,
                    "disable":false,
                    "multi" : "true",
                    "method": "post",
                    "fileType":".tar.gz;.sh;.yml",
                    "action" : "/rest/chkflow/uploadFiles", //文件上传地址路径
                    "selectError" : function(event,file,errorMsg,selectFileQueue) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            alert(i18n.chkFlow_term_upload_err1);
                        } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                            alert(i18n.chkFlow_term_upload_err4);
                        } else if("MAX_TOTAL_SIZE" === errorMsg){
                            alert(i18n.chkFlow_term_upload_err4);
                        }
                    },
                    "select" :  function(event,file,selectFileQueue) {
                        for(var i = 0;i<selectFileQueue.length;i++){
                            if(selectFileQueue[i].fileName != "ServerAntAgentForEuler.tar.gz"
                                || selectFileQueue[i].fileName != "ServerAntAgentForSles.tar.gz"
                                || selectFileQueue[i].fileName != "install_pntl.sh"
                                || selectFileQueue[i].fileName != "ipList.yml"){
                                alert(i18n.chkFlow_term_upload_err5);
                            }
                        }
                    },
                    "completeDefa" : function(event, result, selectFileQueue) {
                        if(result.result === "success") {
                            $("#installFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "success");
                            $("#installFileUpload_id").widget().setTotalProgress(index + 1, selectFileQueue.length);
                        }else {
                            commonException.showMsg(i18n.chkFlow_term_upload_err, "error");
                        }
                    }
                };
                $scope.akTextBox = {
                    "id": "akTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_ak_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "maxSize",
                            "params" : 30
                        },
                        {
                            "validFn" : "regularCheck",
                            "params" : "/^[a-zA-Z0-9_]+$/",
                            "errorDetail": i18n.chkFlow_term_sk_err,
                        }]
                };
                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#akSkBtnId"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });
                $scope.akSkBtnOK = function () {
                    $scope.akSkBtn.disable = true;
                    if (!window.tinyWidget.UnifyValid.FormValid((".level2Content"))){
                        divTip.option("content",i18n.chkFlow_term_input_valid);
                        divTip.show(30000);
                        $scope.akSkBtn.disable = false;
                        return;
                    }
                    var para = getParaFromInput();
                    postAkSkBtn(para);
                };
                var postAkSkBtn = function (para) {
                    var promise = configFlowServ.postAkSk(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_config_ok);
                        $scope.akSkBtn.disable = false;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkFlow_term_config_err, "error");
                        $scope.akSkBtn.disable = false;
                    });
                };
                $scope.skTextBox = {
                    "id": "skTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_sk_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "maxSize",
                            "params" : 30
                        },
                        {
                            "validFn" : "regularCheck",
                            "params" : "/^[A-Za-z0-9_]+$/",
                            "errorDetail": i18n.chkFlow_term_sk_err,
                        }]
                };
                $scope.ipTextBox = {
                    "id": "ipTextBoxId",
                    "value": "",
                    "type" : "ipv4",
                    "tooltip":i18n.chkFlow_term_ip_tooltip,

                };
                $scope.akSkBtn = {
                    "id":"akSkBtnId",
                    "text":i18n.chkFlow_term_confirm,
                    "disable":false
                };
                $scope.probeExitBtn = {
                    "id" : "probeExitBtnId",
                    "text" : i18n.chkFlow_term_exit_probe_btn,
                    "disable":false
                };
                $scope.deployBtn = {
                    "id" : "deployBtnId",
                    "text" : i18n.chkFlow_term_deploy_btn,
                    "disable":false
                };
                $scope.deployBtnOK = function(){
                    $scope.deployBtn.disable = true;
                    var para={};
                    postFirstDeploy(para);
                    //console.log('3');
                };
                $scope.probeExitBtnOK = function(){
                    $scope.probeExitBtn.disable = true;
                    var para={};
                    postProbeExit(para);
                };
                var postProbeExit = function(para){
                    var promise = configFlowServ.probeExit(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_exit_probe_ok);
                        $scope.probeExitBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_exit_probe_err, "error");
                        $scope.probeExitBtn.disable = false;
                    });
                };
                var postFirstDeploy = function(para){
                    var promise = configFlowServ.firstDeploy(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkFlow_term_deploy_ok);
                        $scope.deployBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_deploy_err, "error");
                        $scope.deployBtn.disable = false;
                    });
                };
                function getParaFromInput(){
                    var ak = $scope.akTextBox.value;
                    var sk = $scope.skTextBox.value;
                    var ip = $scope.ipTextBox.value;

                    var para = {
                        "ak":ak,
                         "sk":sk,
                         "ip":ip
                    };
                    return para;
                };
                var getVariableConfig = function(){
                    var promise = configFlowServ.getVariableConfig();
                    promise.then(function(responseData){
                        $scope.akTextBox.value = responseData.ak;
                        $scope.skTextBox.value = responseData.sk;
                        $scope.ipTextBox.value = responseData.ip;
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
        ];

        var module = angular.module('common.config');
        module.tinyController('deployConfig.ctrl', deployConfigCtrl);
        return module;
        });