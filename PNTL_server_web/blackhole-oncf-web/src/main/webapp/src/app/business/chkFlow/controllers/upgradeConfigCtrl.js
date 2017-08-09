define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var upgradeConfigCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ) {
                $scope.i18n = i18n;

                $scope.installFileUpload = {
                    "id":"installFileUpload_id",
                    "inputValue":"",
                    "fileObjName":"X-File",
                    "maxSize":8*1024*1024,//单文件大小不超过 8M
                    "maxTotalSize":16*1024*1024,//总文件大小不小于 16M
                    "disable":false,
                    "multi" : "true",
                    "method": "post",
                    "fileType":".tar.gz",
                    "action" : "/rest/chkflow/uploadFiles", //文件上传地址路径
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            //commonException.showMsg(i18n.chkFlow_term_upload_err2, "error");
                            alert(i18n.chkFlow_term_upload_err2);
                        } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                            //commonException.showMsg(i18n.chkFlow_term_upload_err4, "error");
                            alert(i18n.chkFlow_term_upload_err4);
                        } else if("MAX_TOTAL_SIZE" === errorMsg){
                            //commonException.showMsg(i18n.chkFlow_term_upload_err4, "error");
                            alert(i18n.chkFlow_term_upload_err4);
                        }
                    },
                    "select" :  function(event,file,selectFileQueue) {
                        if(file.name !== "ServerAntAgentForEuler.tar.gz"
                            && file.name !== "ServerAntAgentForSles.tar.gz"){
                            alert(i18n.chkFlow_term_upload_err5);
                            return false;
                        }
                    },
                    "completeDefa" : function(event, result, selectFileQueue) {
                        var resultJson = JSON.parse(result);
                        selectFileQueue.forEach(function(item,index){
                            if(resultJson.hasOwnProperty("result")&&resultJson.result === "success"){
                                $("#installFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "success");
                                $("#installFileUpload_id").widget().setTotalProgress(index + 1, selectFileQueue.length);
                            }else {
                                $("#installFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "error");
                                $("#installFileUpload_id").widget().setTotalProgress(0, selectFileQueue.length);
                                commonException.showMsg(i18n.chkFlow_term_upload_err, "error");
                            }
                        })
                    }
                };

                $scope.singleFileUpload = {
                    "id" : "singleFileUpload_id",
                    "radioGroupId" : "radioGroup_id",
                    "submitId" :"submit_id",
                    "text" : i18n.chkFlow_term_submit,
                    "disable" : false,
                    "maxSize":2*1024*1024,//文件大小小于 2M
                    "action" : "/rest/chkflow/updateAgents", //文件上传地址路径
                    "fileObjName":"X-File",
                    "showSubmitBtn" : false,
                    "enableDetail" : false,
                    "inputValue":"",
                    "method": "post",
                    "fileType":".yml",
                    "completeDefa" : function(event, result) {
                        if(data.match("^\{(.+:.+,*){1,}\}$")){
                            var resultJson = JSON.parse(result);
                            if(resultJson.hasOwnProperty("result")&&resultJson.result === "success"){
                                configFlowServ.hide();
                                $scope.singleFileUpload.disable = false;
                                commonException.showMsg(i18n.chkFlow_term_ip_upgrade_success);
                            }else{
                                configFlowServ.hide();
                                $scope.singleFileUpload.disable = false;
                                commonException.showMsg(i18n.chkFlow_term_ip_upgrade_fail, "error");
                            }
                        }else{
                            var succStr="{'result':'success'}";
                            if(-1 !== result.indexOf(succStr)){
                                configFlowServ.hide();
                                $scope.singleFileUpload.disable = false;
                                commonException.showMsg(i18n.chkFlow_term_ip_upgrade_success);
                            }else {
                                configFlowServ.hide();
                                $scope.singleFileUpload.disable = false;
                                commonException.showMsg(i18n.chkFlow_term_ip_upgrade_fail, "error");
                            }
                        }
                    },
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            alert(i18n.chkFlow_term_upload_err3);
                        } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                            alert(i18n.chkFlow_term_upload_err4);
                        }
                    },
                    "select" :  function(event,file,selectFileQueue) {
                        $scope.file = file;
                    },
                    "beforeSubmit" : function(event,file) {
                        var checkedkey = $("#radioGroup_id").widget().opChecked("checked");
                        var checkValue={"op":""};
                        if(checkedkey === "add"){
                            checkValue.op = "add";
                        }else if(checkedkey === "del"){
                            checkValue.op = "del";
                        }
                        //增加上传文件附带信息
                        $("#singleFileUpload_id").widget().addFormData(checkValue);
                        $("input[name='tinyFormDatas']").prop("name","operation");
                    },
                    "layout" : "horizon",
                    "values" : [{
                        "key" : "add",
                        "text" : i18n.chkFlow_term_add,
                        "checked" : true,
                        "disable" : false
                    },{
                        "key" : "del",
                        "text" : i18n.chkFlow_term_delete,
                        "checked" : false,
                        "disable" : false
                    }],
                    "spacing" : {
                        "width" : "60px"
                    },
                    "submitClick" : function() {
                        $scope.singleFileUpload.disable = true;
                        if(typeof($scope.file) != "undefined"){
                            var installConfirmWindow = {
                                title:i18n.chkFlow_term_upload_confirm,
                                height : "250px",
                                width : "400px",
                                content: "<p style='color: #999'><span style='font-size: 14px;color: #ff9955'>" + i18n.chkFlow_term_file_upload + "</span>" + i18n.chkFlow_term_upload_explain + "</p><p style='text-align:center;margin-top: 15px;color: #999;font-size: 14px;'>" + i18n.chkFlow_term_confirm_upload + "</p>",
                                closeable:false,
                                resizable:false,
                                buttons:[{
                                    key:"btnOK",
                                    label : i18n.chkFlow_term_ok,//按钮上显示的文字
                                    focused : false,//默认焦点
                                    handler : function(event) {//点击回调函数
                                        installConfirmWin.destroy();
                                        configFlowServ.show();
                                        $("#singleFileUpload_id").widget().submit();
                                    }
                                }, {
                                    key:"btnCancel",
                                    label : i18n.chkFlow_term_cancel,
                                    focused : true,
                                    handler : function(event) {
                                        installConfirmWin.destroy();
                                        $scope.singleFileUpload.disable = false;
                                    }
                                }]
                            };
                            var installConfirmWin = new tinyWidget.Window(installConfirmWindow);
                            installConfirmWin.show();
                        }else {
                            commonException.showMsg(i18n.chkFlow_term_no_file_selected, "error");
                            $scope.singleFileUpload.disable = false;
                        }

                    }
                };
            }
        ];
        var module = angular.module('common.config');
        module.tinyController('upgradeConfig.ctrl', upgradeConfigCtrl);
        return module;
    });