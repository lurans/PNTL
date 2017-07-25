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
                    "maxSize":8*1024*1024,//单文件大小不超过 8M
                    "maxTotalSize":20*1024*1024,//总文件大小不小于 20M
                    "disable":false,
                    "multi" : "true",
                    "method": "post",
                    "fileType":".tar.gz;.sh;.yml",
                    "action" : "/rest/chkflow/uploadFiles", //文件上传地址路径
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            //commonException.showMsg(i18n.chkFlow_term_upload_err1, "error");
                            alert(i18n.chkFlow_term_upload_err1);
                        } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                            //commonException.showMsg(i18n.chkFlow_term_upload_err4, "error");
                            alert(i18n.chkFlow_term_upload_err4);
                        } else if("MAX_TOTAL_SIZE" === errorMsg){
                            //commonException.showMsg(i18n.chkFlow_term_upload_err4, "error");
                            alert(i18n.chkFlow_term_upload_err4);
                        }
                    },
                    "select" :  function(event,file,selectFileQueue) {
                        if(file.name != "ServerAntAgentForEuler.tar.gz"
                            && file.name != "ServerAntAgentForSles.tar.gz"
                            && file.name != "install_pntl.sh"
                            && file.name != "ipList.yml"){
                            //commonException.showMsg(i18n.chkFlow_term_upload_err5, "error");
                            alert(i18n.chkFlow_term_upload_err5);
                            file.empty()
                        }
                    },

                    "completeDefa" : function(event, result, selectFileQueue) {
                        var resultJson = JSON.parse(result);
                        selectFileQueue.forEach(function(item,index){
                            if(resultJson.hasOwnProperty("result")&&resultJson.result === "success"){
                                $("#installFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "success");
                                $("#installFileUpload_id").widget().setTotalProgress(index + 1, selectFileQueue.length);
                            }else {
                                commonException.showMsg(i18n.chkFlow_term_upload_err, "error");
                            }
                        })
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
                $scope.uninstallBtn = {
                    "id" : "uninstallBtnId",
                    "text" : i18n.chkFlow_term_uninstall_btn,
                    "disable":false
                };
                $scope.installBtn = {
                    "id" : "installBtnId",
                    "text" : i18n.chkFlow_term_install_btn,
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
                $scope.installBtnOK = function(){
                    $scope.installBtn.disable = true;
                    var para={};
                    postInstall(para);
                    //console.log('3');
                };
                $scope.uninstallBtnOK = function(){
                    $scope.uninstallBtn.disable = true;
                    var para={};
                    postUninstall(para);
                };
                $scope.probeStartBtnOK = function(){
                    $scope.probeStartBtn.disable = true;
                    var para={};
                    postProbeStart(para);
                };
                $scope.probeStopBtnOK = function(){
                    $scope.probeStopBtnOK.disable = true;
                    var para={};
                    postProbeStop(para);
                };
                var postUninstall = function(para){
                    var promise = configFlowServ.uninstall(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_exit_probe_ok);
                        $scope.uninstallBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_exit_probe_err, "error");
                        $scope.uninstallBtn.disable = false;
                    });
                };
                var postInstall = function(para){
                    var promise = configFlowServ.install(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkFlow_term_deploy_ok);
                        $scope.installBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_deploy_err, "error");
                        $scope.installBtn.disable = false;
                    });
                };
                var postProbeStart = function(para){
                    var promise = configFlowServ.startProbe(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkFlow_term_deploy_ok);
                        $scope.probeStartBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_deploy_err, "error");
                        $scope.probeStartBtn.disable = false;
                    });
                };
                var postProbeStop = function(para){
                    var promise = configFlowServ.stopProbe(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkFlow_term_deploy_ok);
                        $scope.probeStopBtn.disable = false;
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_deploy_err, "error");
                        $scope.probeStopBtn.disable = false;
                    });
                };
                function getParaFromInput(){
                    var ak = $scope.akTextBox.value;
                    var sk = $scope.skTextBox.value;
                    var ip = $scope.ipTextBox.value;

                    var para = {
                        "ak":ak,
                         "sk":sk,
                         "repo_url":ip
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