define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var deployConfigCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;
                $scope.isDeployCollapsed = true;

                var divTip = new tinyWidget.Tip({
                    content : "",
                    element : ("#deployVariableConfigBtnId"),
                    position : "right",
                    width: 300,
                    id : "searchTip",
                    auto:false
                });

                $scope.deployFileUpload = {
                    "id":"deployFileUpload_id",
                    "inputValue":"",
                    "fileObjName":"X-File",
                    "maxSize":8*1024*1024,//单个文件大小不超过 8M
                    "maxTotalSize":20*1024*1024,
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
                            alert(i18n.chkFlow_term_upload_err5);
                            return false;
                        }
                    },

                    "completeDefa" : function(event, result, selectFileQueue) {
                        var resultJson = JSON.parse(result);
                        selectFileQueue.forEach(function(item,index){
                            if(resultJson.hasOwnProperty("result")&&resultJson.result === "success"){
                                $("#deployFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "success");
                                $("#deployFileUpload_id").widget().setTotalProgress(index + 1, selectFileQueue.length);
                            }else {
                                $("#deployFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "error");
                                $("#deployFileUpload_id").widget().setTotalProgress(0, selectFileQueue.length);
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
                $scope.repoIpTextBox = {
                    "id": "repoIpTextBoxId",
                    "value": "",
                    "type" : "ipv4",
                    "tooltip":i18n.chkFlow_term_ip_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "ipv4",
                        }]
                };
                $scope.kafkaTopicTextBox = {
                    "id": "kafkaTopicTextBoxId",
                    "value": "",
                    "tooltip":i18n.chkFlow_term_topic_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "maxSize",
                            "params" : 20
                        },
                        {
                            "validFn" : "regularCheck",
                            "params" : "/^[a-zA-Z0-9_]+$/",
                            "errorDetail": i18n.chkFlow_term_sk_err,
                        }]
                };
                $scope.kafkaIpTextBox = {
                    "id": "kafkaIpTextBoxId",
                    "value": "",
                    "type" : "ipv4",
                    "tooltip":i18n.chkFlow_term_ip_tooltip,
                    "validate": [
                        {
                            "validFn" : "required"
                        },
                        {
                            "validFn" : "ipv4"
                        }]
                };

                $scope.deployVariableConfigBtn = {
                    "id":"deployVariableConfigBtnId",
                    "text":i18n.chkFlow_term_submit,
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

                function getParaFromInput(){
                    var ak = $scope.akTextBox.value;
                    var sk = $scope.skTextBox.value;
                    var ip = $scope.repoIpTextBox.value;
                    var kafka_ip = $scope.kafkaIpTextBox.value;
                    var topic = $scope.kafkaTopicTextBox.value;

                    var para = {
                        "ak":ak,
                        "sk":sk,
                        "repo_url":ip,
                        "kafka_ip":kafka_ip,
                        "topic":topic
                    };
                    return para;
                };
                var getVariableConfig = function(){
                    var promise = configFlowServ.getVariableConfig();
                    promise.then(function(responseData){
                        $scope.akTextBox.value = responseData.ak;
                        $scope.skTextBox.value = responseData.sk;
                        $scope.repoIpTextBox.value = responseData.repo_url;
                        $scope.kafkaIpTextBox.value = responseData.kafka_ip;
                        $scope.kafkaTopicTextBox.value = responseData.topic;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkFlow_term_read_failed_config, "error");
                    });
                };
                var postDeployVariableConfig = function (para) {
                    var promise = configFlowServ.postDeployVariableConf(para);
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_config_ok);
                        $scope.deployVariableConfigBtn.disable = false;
                    },function(responseData){
                        //showERRORMsg
                        commonException.showMsg(i18n.chkFlow_term_config_err, "error");
                        $scope.deployVariableConfigBtn.disable = false;
                    });
                };
                var postInstall = function(para){
                    var promise = configFlowServ.install(para);
                    promise.then(function(responseData){
                        //OK
                        commonException.showMsg(i18n.chkFlow_term_deploy_ok);
                        $scope.installBtn.disable = false;
                        configFlowServ.hide();
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_deploy_err, "error");
                        $scope.installBtn.disable = false;
                        configFlowServ.hide();
                    });
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

                $scope.deployVariableConfigBtnOK = function () {
                    $scope.deployVariableConfigBtn.disable = true;
                    if (!window.tinyWidget.UnifyValid.FormValid((".level2Content"))){
                        divTip.option("content",i18n.chkFlow_term_input_valid);
                        divTip.show(1000);
                        $scope.deployVariableConfigBtn.disable = false;
                        return;
                    }
                    var para = getParaFromInput();
                    postDeployVariableConfig(para);
                };
                $scope.installBtnOK = function(){
                    $scope.installBtn.disable = true;
                    var installConfirmWindow = {
                        title:i18n.chkFlow_term_install_confirm,
                        height : "250px",
                        width : "400px",
                        content: "<p style='color: #999'><span style='font-size: 14px;color: #ff9955'>安装</span>：在首次部署时，安装并启动agent探测工具，其只对ipList.yml文件中的主机进行安装操作，安装过程需要1~2分钟，请耐心等待。</p><p style='text-align:center;margin-top: 15px;color: #999;font-size: 14px;'>确定安装？</p>",
                        closeable:false,
                        resizable:false,
                        buttons:[{
                            key:"btnOK",
                            label : i18n.chkFlow_term_ok,//按钮上显示的文字
                            focused : false,//默认焦点
                            handler : function(event) {//点击回调函数
                                installConfirmWin.destroy();
                                configFlowServ.show();
                                var para={};
                                postInstall(para);
                            }
                        }, {
                            key:"btnCancel",
                            label : i18n.chkFlow_term_cancel,
                            focused : true,
                            handler : function(event) {
                                installConfirmWin.destroy();
                                $scope.installBtn.disable = false;
                            }
                        }]
                    }
                    var installConfirmWin = new tinyWidget.Window(installConfirmWindow);
                    installConfirmWin.show();
                };
                $scope.uninstallBtnOK = function(){
                    $scope.uninstallBtn.disable = true;

                    var uninstallConfirmWindow = {
                        title:i18n.chkFlow_term_uninstall_confirm,
                        height : "250px",
                        width : "400px",
                        content: "<p style='color: #999'><span style='font-size: 14px;color: #ff9955'>卸载</span>：结束进程并删除agent探测工具，其只对ipList.yml文件中的主机进行卸载操作。</p><p style='text-align:center;margin-top: 30px;color: #999;font-size: 14px;'>确定卸载？</p>",
                        closeable:false,
                        resizable:false,
                        buttons:[{
                            key:"btnOK",
                            label : i18n.chkFlow_term_ok,//按钮上显示的文字
                            focused : false,//默认焦点
                            handler : function(event) {//点击回调函数
                                uninstallConfirmWin.destroy();
                                var para={};
                                postUninstall(para);
                            }
                        }, {
                            key:"btnCancel",
                            label : i18n.chkFlow_term_cancel,
                            focused : true,
                            handler : function(event) {
                                uninstallConfirmWin.destroy();
                                $scope.uninstallBtn.disable = false;
                            }
                        }]
                    }
                    var uninstallConfirmWin = new tinyWidget.Window(uninstallConfirmWindow);
                    uninstallConfirmWin.show();
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