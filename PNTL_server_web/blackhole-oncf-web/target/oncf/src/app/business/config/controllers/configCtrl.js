define(["language/chkFlow",
        "app/business/common/services/commonException"],
    function (i18n, commonException) {
        "use strict";

        var configCtrl = ["$scope","$rootScope", "configServ","$timeout",
            function ($scope, $rootScope, configServ, $timeout) {
                $scope.i18n = i18n;
                var keySelect = false;
                var keyExist = false;


                $scope.btn = {
                    finishTxt:i18n.chkFlow_term_finish,
                    cancelTxt:i18n.chkFlow_term_cancel,

                    finishDisable:false,
                    cancelDisable:false
                };
                $scope.finish = function() {
                    if (!window.tinyWidget.UnifyValid.FormValid((".input_content"))){
                        return;
                    }
                    if(!keySelect || (keySelect&&keyExist)) {
                        disableInput();
                        commonException.showMsg(i18n.chkFlow_term_verify, "success");
                        verifyFspToken();
                    }
                };
                $scope.cancel = function(){
                    window.location.href = window.location.origin;
                };

                //default help tip
                var defaultTooltip={
                    "content":"",
                    "width":300,
                    "position":"right",
                    "auto":"true"
                }

                //Cascading
                $scope.proxyIp = {
                    "id":"proxyIp_id",
                    "width":"300px",
                    "maxLength":15,
                    "value":"",
                    "disable":false,
                    validate: [{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    }, {
                        "validFn": "ipv4",
                        "errorDetail":$scope.i18n.chkFlow_term_ip_valid
                    }],
                    "tooltip":_.extend(_.clone(defaultTooltip),{"content":i18n.chkFlow_term_help_proxy_ip})
                };

                $scope.tenantName = {
                    "id":"tenantName",
                    "width":"300px",
                    "maxLength":32,
                    "value":"",
                    "disable":false,
                    validate: [{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn": "regularCheck",
                        "params": "/^[A-Za-z_]\\w*$/",
                        errorDetail:$scope.i18n.chkFlow_term_name_rule
                    }],
                    "tooltip":_.extend(_.clone(defaultTooltip),{"content":i18n.chkFlow_term_help_tenant_name})
                };

                $scope.username = {
                    "id":"username",
                    "width":"300px",
                    "maxLength":32,
                    "value":"",
                    "disable":false,
                    validate: [{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    },{
                        "validFn": "regularCheck",
                        "params": "/^[A-Za-z_]\\w*$/",
                        errorDetail:$scope.i18n.chkFlow_term_name_rule
                    }],
                    "tooltip":_.extend(_.clone(defaultTooltip),{"content":i18n.chkFlow_term_help_username})
                };

                $scope.password = {
                    "id":"password",
                    "type":"password",
                    "width":"300px",
                    "maxLength":32,
                    "value":"",
                    "disable":false,
                    validate: [{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_input_null
                    }],
                    "tooltip":_.extend(_.clone(defaultTooltip),{"content":i18n.chkFlow_term_help_password})
                };

                $scope.authUrl = {
                    "id":"authUrl",
                    "width":"300px",
                    "maxLength":128,
                    "value":"",
                    "disable":false,
                    validate: [{
                        "validFn": "required",
                        errorDetail:$scope.i18n.chkFlow_term_auth_url_input_null
                    },{
                        "validFn": "url",
                        errorDetail:$scope.i18n.chkFlow_term_invalid_url
                    },{
                        "validFn":"regularCheck",
                        "params":"/:([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$/",
                        errorDetail:$scope.i18n.chkFlow_term_port_valid
                    }],
                    "tooltip":_.extend(_.clone(defaultTooltip),{"content":i18n.chkFlow_term_help_auth_url})
                };

                //Private Key
                $scope.keyFile = {
                    "id" : "keyFile_id",
                    "inputValue":"",
                    "fileObjName":"X-File",
                    "maxSize":1*1024*1024,//1M
                    "showSubmitBtn":false,
                    "disable":false,
                    "action":"/rest/chkflow/key",
                    "selectError": function(event,file,errorMsg){
                        keySelect = true;
                        keyExist = false;
                        if("EXCEED_FILE_SIZE" === errorMsg) {
                            $("#keyFile_id").widget().setDetail("error", i18n.chkFlow_term_exceed_file_size);
                        }
                    },
                    "completeDefa":function(event, responseText) {
                        $("#keyFile_id").widget().setDetail("custom");
                        if( "verify" === JSON.parse($(responseText).text()).type ) {
                            if( undefined === JSON.parse($(responseText).text()).err_msg ) {
                                commonException.showMsg(i18n.chkFlow_term_verify_success,"success");
                                restartTomcatDiag();
                            }else {
                                commonException.showMsg(i18n.chkFlow_term_file_verify_fail, "error");
                                resetInputDisable();
                            }
                        }else if( "submit" === JSON.parse($(responseText).text()).type ){
                            if( undefined === JSON.parse($(responseText).text()).err_msg ) {
                                restarTomcat();
                            }else {
                                commonException.showMsg(i18n.chkFlow_term_upload_fail, "error");
                                resetInputDisable();
                            }
                        }else{
                            commonException.showMsg(responseText.err_msg, "error");
                            resetInputDisable();
                        }
                    },
                    "select":function(event,file) {
                        keySelect = true;
                        var pos = file.name.indexOf(".");
                        if(pos !== -1) {
                            $("#keyFile_id").widget().setDetail("error", i18n.chkFlow_term_key_format_fail);
                            keyExist = false;
                            return false;
                        }
                        var re = new RegExp("[\u4e00-\u9fa5]");
                        if(re.test(file.name)){
                            $("#keyFile_id").widget().setDetail("error", i18n.chkFlow_term_key_name_contain_zh);
                            keyExist = false;
                            return false;
                        }
                        $("#keyFile_id").widget().setDetail("custom","");
                        keyExist = true;
                        return true;
                    },
                    "tooltip":_.extend(_.clone(defaultTooltip),{"content":i18n.chkFlow_term_help_key_file}),
                    "cancel":function() {
                        keySelect = false;
                        keyExist = false;
                        $("#keyFile_id").widget().setDetail("custom",i18n.chkFlow_term_no_file_selected);
                    }
                };

                function disableInput(){
                    $scope.btn.finishDisable = true;
                    $scope.btn.cancelDisable = true;
                    $scope.proxyIp.disable = true;
                    $scope.tenantName.disable = true;
                    $scope.username.disable = true;
                    $scope.password.disable = true;
                    $scope.authUrl.disable = true;
                    $scope.keyFile.disable = true;
                }

                function resetInputDisable(){
                    $scope.btn.finishDisable = false;
                    $scope.btn.cancelDisable = false;
                    $scope.proxyIp.disable = false;
                    $scope.tenantName.disable = false;
                    $scope.username.disable = false;
                    $scope.password.disable = false;
                    $scope.authUrl.disable = false;
                    $scope.keyFile.disable = false;
                }

                function getFspTokenInfo(){
                    var para = null;

                    var proxyIp = null;
                    var tenantName = null;
                    var username = null;
                    var password = null;
                    var authUrl = null;

                    proxyIp = $scope.proxyIp.value;
                    tenantName = $scope.tenantName.value;
                    username = $scope.username.value;
                    password = $scope.password.value;
                    authUrl = $scope.authUrl.value;

                    para={"cascading_ip":proxyIp, "os_tenant_name":tenantName, "os_username":username, "os_password":password, "os_auth_url":authUrl};
                    para = JSON.stringify(para);

                    return para;
                }

                function getParaFromInput(){
                    var para = null;

                    var proxyIp = null;
                    var tenantName = null;
                    var username = null;
                    var password = null;
                    var authUrl = null;
                    var sshKey = null;

                    proxyIp = $scope.proxyIp.value;
                    tenantName = $scope.tenantName.value;
                    username = $scope.username.value;
                    password = $scope.password.value;
                    authUrl = $scope.authUrl.value;
                    sshKey = $scope.keyFile.fileObjName;

                    para={"cascading_ip":proxyIp, "os_tenant_name":tenantName, "os_username":username, "os_password":password,
                        "os_auth_url":authUrl, "cna_ssh_key":sshKey};
                    para = JSON.stringify(para);

                    return para;
                }

                function restarTomcat(){
                    var promise = configServ.restarTomcat();
                    promise.then(function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_restart_tomcat_success, "success");
                        $timeout(function(){window.location.href = window.location.origin;}, 2000);
                    }, function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_restart_tomcat_fail, "error");
                        resetInputDisable();
                    });
                }

                function restartTomcatDiag(){
                    var newWin = new tinyWidget.Window({
                        "winId": "restart_tomcat_id",
                        "title": i18n.chkFlow_term_restart_tomcat,
                        "content-type": "url",
                        "content": "src/app/business/config/views/tomcat.html",
                        "height": "auto",
                        "width": "400px",
                        resizable: true,
                        "buttons": [
                            {
                                label: i18n.chkFlow_term_restart,
                                default: true,
                                handler: function (event) {
                                    newWin.destroy();
                                    configFile();
                                }
                            }, {
                                label: i18n.chkFlow_term_cancel,
                                default: true,
                                handler: function (event) {
                                    newWin.destroy();
                                    resetInputDisable();
                                }
                            }
                        ]
                    });
                    newWin.show();
                }

                function verifyKeyFile(){
                    $("#keyFile_id").widget().addFormData("verify");
                    $("#keyFile_id").widget().submit();
                }

                function configKeyFile(){
                    $("#keyFile_id").widget().addFormData("submit");
                    $("#keyFile_id").widget().submit();
                }

                function configFile(){
                    var para = getParaFromInput();
                    var promise = configServ.setConfig(para);
                    promise.then(function(responseData){
                        if(keySelect)
                            configKeyFile();
                        else
                            restarTomcat();
                    }, function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_config_fail, "error");
                        resetInputDisable();
                    });
                }

                function verifyFspToken() {
                    var para=getFspTokenInfo();
                    var promise = configServ.getFspToken(para);
                    promise.then(function(responseData){
                        if(undefined === responseData.err_msg ){
                            if(keySelect) {
                                verifyKeyFile();
                            }else{
                                commonException.showMsg(i18n.chkFlow_term_verify_success,"success");
                                restartTomcatDiag();
                            }
                        }else{
                            commonException.showMsg(i18n.chkFlow_term_fsp_token_fail,"error");
                            resetInputDisable();
                        }
                    },function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_fsp_token_fail,"error");
                        resetInputDisable();
                    });
                }

                function getConfig(){
                    var promise = configServ.getConfig();
                    promise.then(function(responseData){
                        $scope.proxyIp.value = responseData.cascading_ip;
                        $scope.tenantName.value = responseData.os_tenant_name;
                        $scope.username.value = responseData.os_username;
                        $scope.password.value = responseData.os_password;
                        $scope.authUrl.value = responseData.os_auth_url;
                        $scope.keyFile.inputValue = responseData.cna_ssh_key;
                    }, function(responseData){
                        commonException.showMsg(i18n.chkFlow_term_config_info_fail,"error");
                    });
                }

                var server_ip_port = window.location.origin;
                var sso_ip_port = "";

                function getSsoIpPort(){
                    var promise = configServ.getSsoConfig();
                    promise.then(function(responseData) {
                        if(responseData.sso_ip !== null && responseData.sso_port !== null)
                            sso_ip_port = responseData.sso_ip + ":" + responseData.sso_port;
                        window.logoutPath = "https://" + sso_ip_port + "/unisso/logout?service=" + encodeURIComponent(server_ip_port + "/");
                        $rootScope.logoutUrl = window.logoutPath;
                    },function(responseData){
                        //
                    })
                }

                function init(){
                    getSsoIpPort();
                    getConfig();
                }

                init();
            }];


        var module = angular.module('common.config');
        module.tinyController('config.ctrl', configCtrl);

        return module;

    });