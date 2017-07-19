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
                    "disable":false,
                    "multi" : "true",
                    "method": "post",
                    "fileType":".tar.gz",
                    "action" : "/rest/chkflow/uploadFiles", //文件上传地址路径
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            alert("please select .tar.gz file");
                        }
                    },
                    "completeDefa" : function(event, result, selectFileQueue) {
                        selectFileQueue.forEach(function(item,index){
                            if(result.state === "success") {
                                $("#installFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "success");
                                $("#installFileUpload_id").widget().setTotalProgress(index + 1, selectFileQueue.length);
                            }else {
                                alert("上传失败");
                            }

                            //tip
                        })
                    }
                };
                $scope.singleFileUpload = {
                    "id" : "singleFileUpload_id",
                    "action" : "", //文件上传地址路径
                    "showSubmitBtn" : false,
                    "enableDetail" : false,
                    "method": "post",
                    "fileType":".yml",
                    "completeDefa" : function(event, result, selectFileQueue) {
                        selectFileQueue.forEach(function(item,index){
                            if(result.state === "success") {
                                $("#singleFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath, "success");
                                $("#singleFileUpload_id").widget().setTotalProgress(index + 1, selectFileQueue.length);
                            }else {
                                alert("上传失败");
                            }

                        })
                    },
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            alert("please select .yml file");
                        }
                    },
                    "beforeSubmit" : function() {
                        //增加上传文件附带信息（用户textbox的输入值）
                        $("#singleFileUpload_id").widget().addFormData($scope.singleFileUpload.value);
                    },
                    "textboxId" : "myTxt",
                    "value":"",
                    "text" : "submit",
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