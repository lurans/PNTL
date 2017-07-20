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
                    "minSize":20*1024*1024,//文件大小不小于 20M
                    "disable":false,
                    "multi" : "true",
                    "method": "post",
                    "fileType":".tar.gz",
                    "action" : "/rest/chkflow/updateAgents", //文件上传地址路径
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            alert("please select .tar.gz file");
                        } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                            alert("error", "文件大小不小于20M");
                        }
                    },
                    "completeDefa" : function(event, result, selectFileQueue) {
                        selectFileQueue.forEach(function(item,index){
                            if(result.state === "success") {
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
                    "minSize":8*1024*1024,//文件大小不小于 20M
                    "action" : "/rest/chkflow/updateAgents", //文件上传地址路径
                    "fileObjName":"X-File",
                    "showSubmitBtn" : false,
                    "enableDetail" : false,
                    "method": "post",
                    "fileType":".yml",
                    "completeDefa" : function(event, result, selectFileQueue) {
                            if(result.state === "success") {
                                commonException.showMsg(i18n.chkFlow_term_upload_success, "success");
                            }else {
                                commonException.showMsg(i18n.chkFlow_term_upload_err, "error");
                            }
                    },
                    "selectError" : function(event,file,errorMsg) {
                        if("INVALID_FILE_TYPE" === errorMsg) {
                            alert("please select .yml file");
                        } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                            alert("error", "文件大小不小于8M");
                        }
                    },
                    "beforeSubmit" : function() {
                        var checkedkey = $("#radioGroup_id").widget().opChecked("checked");
                        //console.log(checkedkey);
                        var checkValue;
                        if(checkedkey === false){
                            alert("请选择添加和删除信息");
                            return;
                        }else if(checkedkey === "1"){
                            checkValue = "添加";
                        }else if(checkedkey === "2"){
                            checkValue = "删除";
                        }
                        //增加上传文件附带信息
                        $("#singleFileUpload_id").widget().addFormData(checkValue);
                    },
                    "layout" : "horizon",
                    "values" : [{
                        "key" : "1",
                        "text" : i18n.chkFlow_term_add,
                        "checked" : false,
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