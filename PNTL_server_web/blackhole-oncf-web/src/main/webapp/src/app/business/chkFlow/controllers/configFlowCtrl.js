define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var configFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;
                //$scope.data = [{"name":1,"value":2,"updateTime":3,"description":4},{"name":1,"value":2,"updateTime":3,"description":4}];
                $scope.configList = {
                    "id":"configList_id",
                    "data" : [], //初始数据为空
                    "value":null,
                    "curRow":null,
                    "displayLength": 10,
                    "totalRecords":0,
                    "showDetails":true,
                    "columnsDraggable":true,
                    "closeOthersDetails" :true,
                    "columns" : [
                        {
                            "sTitle":"",
                            "sWidth":"3%",
                            "mData":function(data){
                                return data.name;
                            },
                            "bSortable":false
                        },
                        {
                            "sTitle" : i18n.chkFlow_term_config_name,
                            "sWidth":"30%",
                            "mData":function(data){
                                return data.name;
                            },
                            "bSortable":false
                        },
                        {
                            "sTitle" : i18n.chkFlow_term_config_value,
                            "sWidth":"40%",
                            "mData":function(data){
                                return data.value;
                            },
                            "bSortable":false
                        },
                        {
                            "sTitle" : i18n.chkFlow_term_config_update_time,
                            "sWidth":"20%",
                            "mData":function(data){
                                return data.updateTime;
                            },
                            "bSortable":false
                        },
                        {
                            "sTitle" : i18n.chkFlow_term_config_op,
                            "sWidth" : "7%",
                            "mData":"op",
                            "sClass":"operation-column",
                            "bSortable":false
                        }
                    ],
                    "cellClickActive": function (event, trData) {
                        $scope.configList.value = trData;
                        $scope.configList.curRow = event.currentTarget;
                    },
                    "renderRow": function(nRow, aData, iDataIndex){
                        var html_compile = '<div><span ng-click="childModel.edit()">' +  i18n.chkFlow_term_manager + "</span></div>";
                        var link = $compile(html_compile);
                        var scope = $scope.$new(false);
                        scope.childModel = {
                            "edit":function(){
                                editConfigPage(aData);
                            }
                        };
                        var node = link(scope); // 获得经过angular编译后的dom对象
                        $("td[tdname='4']", nRow).html(node); //将进度条控件放到第4列（从0开始）
                    },
                    tip: true
                };

                function editConfigPage(data){
                    var id = data.id;
                    var newWindow = new tinyWidget.Window({
                        "winId":"edit_config_id",
                        "title":i18n.chkFlow_term_config_edit,
                        "params":{
                            "id":id
                        },
                        "content-type":"url",
                        "content":"src/app/business/chkFlow/views/editVariable.html",
                        "height":"auto",
                        "width": "900px",
                        resizable: true,
                        "buttons": null
                    });
                    newWindow.show();
                };

                function getSysGroupsList(){
                    var promise = configFlowServ.getVariablesList();
                    promise.then(function(responseData){
                        $scope.configList.data = responseData.config;
                        $scope.configList.totalRecords = responseData.config.length;
                        //detail
                        _.each(responseData.config, function (configDetail) {
                            configDetail.detail = {
                                contentType: "url",
                                content: "src/app/business/chkFlow/views/variableDetail.html"
                            }
                        })
                        $("#showWin").load();
                    },
                     function(responseData){
                        if(responseData.readyState == 0 && responseData.status == 0){
                            //showMsg(i18n.jobPlatform_term_get_user_fail,"error");
                            setTimeout("location.href='.'", 2000);
                            return;
                        }
                        //showMsg("error", "error");
                    });
                };

                function showMsg(_content, level) {
                    commException.showMsg(_content, level);
                };
                getSysGroupsList();

            }
        ]

        var module = angular.module('common.config');
        module.tinyController('configFlow.ctrl', configFlowCtrl);
        return module;
        });