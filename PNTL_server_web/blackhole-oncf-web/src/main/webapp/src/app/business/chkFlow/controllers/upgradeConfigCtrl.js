define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var upgradeConfigCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ) {
                $scope.i18n = i18n;
                $scope.installFileUpload = {
                    "id":"installFileUpload_id1",
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
                        if(file.name != "ServerAntAgentForEuler.tar.gz"
                            && file.name != "ServerAntAgentForSles.tar.gz"){
                            //commonException.showMsg(i18n.chkFlow_term_upload_err5, "error");
                            alert(i18n.chkFlow_term_upload_err5);
                            file.empty()
                        }
                    },
                    "completeDefa" : function(event, result, selectFileQueue) {
                        var resultJson = JSON.parse(result);
                        selectFileQueue.forEach(function(item,index){
                            if(resultJson.hasOwnProperty("result")&&resultJson.result === "success"){
                                $("#installFileUpload_id1").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "success");
                                $("#installFileUpload_id1").widget().setTotalProgress(index + 1, selectFileQueue.length);
                            }else {
                                commonException.showMsg(i18n.chkFlow_term_upload_err, "error");
                            }

                            //tip
                        })
                    }
                };

                $scope.singleFileUpload = {
                    "id" : "singleFileUpload_id",
                    "id1" : "radioGroup_id",
                    "maxSize":2*1024*1024,//文件大小小于 2M
                    "action" : "/rest/chkflow/updateAgents", //文件上传地址路径
                    "fileObjName":"X-File",
                    "showSubmitBtn" : false,
                    "enableDetail" : false,
                    "method": "post",
                    "fileType":".yml",
                    "completeDefa" : function(event, result, selectFileQueue) {
                        var resultJson = JSON.parse(result);
                        if(resultJson.hasOwnProperty("result")&&resultJson.result === "success"){
                            commonException.showMsg(i18n.chkFlow_term_upload_success, "success");
                        }else {
                            commonException.showMsg(i18n.chkFlow_term_upload_err, "error");
                        }
                    },
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            //commonException.showMsg(i18n.chkFlow_term_upload_err3, "error");
                            alert(i18n.chkFlow_term_upload_err3);
                        } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                            //commonException.showMsg(i18n.chkFlow_term_upload_err4, "error");
                            alert(i18n.chkFlow_term_upload_err4);
                        }
                    },
                    "beforeSubmit" : function(event,file) {
                        var checkedkey = $("#radioGroup_id").widget().opChecked("checked");
                        var checkValue;
                        if(checkedkey === "1"){
                            checkValue = "add";
                        }else if(checkedkey === "2"){
                            checkValue = "del";
                        };
                        var adddition = {
                            "X-file":file,
                            "operation":checkValue
                        };
                        //增加上传文件附带信息
                        $("#singleFileUpload_id").widget().addFormData(adddition);
                    },
                    "layout" : "horizon",
                    "values" : [{
                        "key" : "1",
                        "text" : i18n.chkFlow_term_add,
                        "checked" : true,
                        "disable" : false
                    },{
                        "key" : "2",
                        "text" : i18n.chkFlow_term_delete,
                        "checked" : false,
                        "disable" : false
                    }],
                    "spacing" : {
                        "width" : "60px"
                    },
                    "text" : i18n.chkFlow_term_confirm,
                    "submitClick" : function() {
                        $("#singleFileUpload_id").widget().submit();
                    }

                }
            }
        ];
        var module = angular.module('common.config');
        module.tinyController('upgradeConfig.ctrl', upgradeConfigCtrl);
        return module;
    });