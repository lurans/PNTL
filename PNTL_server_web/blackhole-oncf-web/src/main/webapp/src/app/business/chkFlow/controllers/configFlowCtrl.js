define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException, Step, _StepDirective, ViewMode) {
        "use strict";

        var configFlowCtrl = ["$scope","$rootScope", "$state", "$sce", "$compile", "$timeout", "configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;

                $scope.tablis=[
                    {
                        state : "blackhole.manager.configFlow.deployConfig",
                        text : i18n.chkFlow_term_deploy
                    },
                    {
                        state : "blackhole.manager.configFlow.variableConfig",
                        text : i18n.chkFlow_term_variable_config
                    }
                ];

                // $scope.tabs = [
                //     {
                //         title: i18n.chkFlow_term_deploy,
                //         disable: false,
                //         url: "../views/deployConfig.html",
                //         active: true,
                //         state: "blackhole.manager.configFlow.deployConfig"
                //     },
                //     {
                //         title: i18n.chkFlow_term_variable_config,
                //         disable: false,
                //         url: "../views/variableConfig.html",
                //         active: false,
                //         state: "blackhole.manager.configFlow.variableConfig"
                //     }
                // ];
                // $scope.data = [{"name":1,"value":2,"updateTime":3,"description":4},{"name":1,"value":2,"updateTime":3,"description":4}];
                // $scope.isFileCollapsed = true;
                // $scope.isDeployCollapsed = true;
                // $scope.isVariableCollapsed = true;
                // var divTip = new tinyWidget.Tip({
                //     content : "",
                //     element : ("#variableBtnId"),
                //     position : "right",
                //     width: 300,
                //     id : "searchTip",
                //     auto:false
                // });

                // $scope.installFileUpload = {
                //     "id":"installFileUpload_id",
                //     "inputValue":"",
                //     "fileObjName":"X-File",
                //     "maxSize":2*1024*1024,//文件大小不超过 1M
                //     "disable":false,
                //     "multi" : "true",
                //     "fileType":".tar.gz;.sh;.yml",
                //     "action" : "/rest/chkflow/uploadAgentPkg", //文件上传地址路径
                //     "selectError" : function(event,file,errorMsg) {
                //         if("INVALID_FILE_TYPE" === errorMsg) {
                //             alert("please select .yml .sh or .tar.gz file");
                //         } else if ("EXCEED_FILE_SIZE" === errorMsg) {
                //             alert("error", "file size exceed");
                //         }
                //     },
                //     "completeDefa" : function(event, result, selectFileQueue) {
                //         selectFileQueue.forEach(function(item,index){
                //             $("#installFileUpload_id").widget().setMultiQueueDetail(selectFileQueue[index].filePath,"success");
                //             $("#installFileUpload_id").widget().setTotalProgress(index+1,selectFileQueue.length);
                //         })
                //     }
                // };

                // $scope.variable = {
                //     "probeIntervalTime" : i18n.chkFlow_term_probe_interval_time_name,
                //     "probePortCount": i18n.chkFlow_term_probe_port_count_name,
                //     "reportIntervalTime": i18n.chkFlow_term_report_interval_time_name,
                //     "packetsNum" : i18n.chkFlow_term_probe_packets_number_name,
                //     "timeDelay" : i18n.chkFlow_term_probe_max_time_delay_name,
                //     "packetsLoss" : i18n.chkFlow_term_probe_max_loss_rate_name,
                //     "dscp":i18n.chkFlow_term_dscp_name,
                //     "lossPkgTimeOut":i18n.chkFlow_term_loss_pkg_timeout_name,
                // };

                // $scope.probeRoundTextBox = {
                //     "id": "probeRoundTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_probe_interval_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required",
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "rangeValue",
                //             "params" : [-1,60],
                //         }]
                // };
                // $scope.probePortTextBox = {
                //     "id": "probePortTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_probe_port_count_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required",
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "rangeValue",
                //             "params" : [1,50],
                //         }]
                // };
                // $scope.reportRoundTextBox = {
                //     "id": "reportRoundTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_report_interval_time_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required",
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "rangeValue",
                //             "params" : [1,60],
                //         }]
                // };
                // $scope.packetsNumTextBox = {
                //     "id": "packetsNumTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_packets_num_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required"
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "minValue",
                //             "params" : 1,
                //         }]
                // };
                // $scope.timeDelayTextBox = {
                //     "id": "timeDelayTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_max_time_delay_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required"
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "minValue",
                //             "params" : 0,
                //         }]
                // };
                // $scope.packetsLossTextBox = {
                //     "id": "packetsLossTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_max_loss_rate_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required"
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "minValue",
                //             "params" : 0,
                //         }]
                // };
                // $scope.dscpTextBox = {
                //     "id": "dscpTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_dscp_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required"
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "rangeValue",
                //             "params" : [0,63],
                //         }
                //     ]
                // };
                // $scope.lossPkgTimeOutTextBox = {
                //     "id": "lossPkgTimeOutTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_lossPkg_timeout_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required"
                //         },
                //         {
                //             "validFn" : "number",
                //         },
                //         {
                //             "validFn" : "minValue",
                //             "params" : 0,
                //         }]
                // };
                // $scope.akTextBox = {
                //     "id": "akTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_ak_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required"
                //         },
                //         {
                //             "validFn" : "maxSize",
                //             "params" : 30,
                //         },
                //         {
                //             "validFn" : "regularCheck",
                //             "params" : "/^[a-zA-Z0-9_]+$/",
                //             "errorDetail": i18n.chkFlow_term_sk_err,
                //         }]
                // };
                // $scope.skTextBox = {
                //     "id": "skTextBoxId",
                //     "value": "",
                //     "tooltip":i18n.chkFlow_term_sk_tooltip,
                //     "validate": [
                //         {
                //             "validFn" : "required"
                //         },
                //         {
                //             "validFn" : "maxSize",
                //             "params" : 30,
                //         },
                //         {
                //             "validFn" : "regularCheck",
                //             "params" : "/^[A-Za-z0-9_]+$/",
                //             "errorDetail": i18n.chkFlow_term_sk_err,
                //         }]
                // };
                // $scope.akSkBtn = {
                //     "id":"",
                //     "text":i18n.chkFlow_term_confirm,
                //     "disable":false,
                // }
                // $scope.deployBtn = {
                //     "id" : "deployBtnId",
                //     "text" : i18n.chkFlow_term_deploy_btn,
                //     "disable":false
                // };

                // $scope.probeExitBtn = {
                //     "id" : "probeExitBtnId",
                //     "text" : i18n.chkFlow_term_exit_probe_btn,
                //     "disable":false,
                // };

                // $scope.variableBtn = {
                //     "id" : "variableBtnId",
                //     "text" : i18n.chkFlow_term_confirm,
                //     "disable":false,
                // };

                // var postFirstDeploy = function(para){
                //     var promise = configFlowServ.firstDeploy(para);
                //     promise.then(function(responseData){
                //         //OK
                //         commonException.showMsg(i18n.chkFlow_term_deploy_ok);
                //         $scope.deployBtn.disable = false;
                //     },function(responseData){
                //         commonException.showMsg(i18n.chkFlow_term_deploy_err, "error");
                //         $scope.deployBtn.disable = false;
                //     });
                // };
                // $scope.deployBtnOK = function(){
                //     $scope.deployBtn.disable = true;
                //     var para={};
                //     postFirstDeploy(para);
                //     //console.log('3');
                // };

                // var postProbeExit = function(para){
                //     var promise = configFlowServ.probeExit(para);
                //     promise.then(function(responseData){
                //         commonException.showMsg(i18n.chkFlow_term_exit_probe_ok);
                //         $scope.probeExitBtn.disable = false;
                //     },function(responseData){
                //         commonException.showMsg(i18n.chkFlow_term_exit_probe_err, "error");
                //         $scope.probeExitBtn.disable = false;
                //     });
                // };
                // $scope.probeExitBtnOK = function(){
                //     $scope.probeExitBtn.disable = true;
                //     var para={};
                //     postProbeExit(para);
                // };

                // var postVariableConfig = function(para){
                //     var promise = configFlowServ.postVariableConfig(para);
                //     promise.then(function(responseData){
                //         commonException.showMsg(i18n.chkFlow_term_config_ok);
                //         $scope.variableBtn.disable = false;
                //     },function(responseData){
                //         //showERRORMsg
                //         commonException.showMsg(i18n.chkFlow_term_config_err, "error");
                //         $scope.variableBtn.disable = false;
                //     });
                // };
                // function getParaFromInput(){
                //     var probeRound = $scope.probeRoundTextBox.value;
                //     var probePort = $scope.probePortTextBox.value;
                //     var reportRound = $scope.reportRoundTextBox.value;
                //     var packetsNum = $scope.packetsNumTextBox.value;
                //     var timeDelay = $scope.timeDelayTextBox.value;
                //     var packetsLoss = $scope.packetsLossTextBox.value;
                //     var dscp = $scope.dscpTextBox.value;
                //     var lossPkgTimeOut = $scope.lossPkgTimeOutTextBox.value;
                //     var ak = $scope.akTextBox.value;
                //     var sk = $scope.skTextBox.value;
                //
                //     var para = {"probe_period":probeRound,
                //                 "port_count":probePort,
                //                 "report_period":reportRound,
                //                 "pkg_count":packetsNum,
                //                 "delay_threshold":timeDelay,
                //                 "lossRate_threshold":packetsLoss,
                //                 "dscp":dscp,
                //                 "lossPkg_timeout":lossPkgTimeOut,
                //                 "ak":ak,
                //                 "sk":sk};
                //     return para;
                // };
                // $scope.variableBtnOK = function(){
                //     if (!window.tinyWidget.UnifyValid.FormValid((".input_content"))){
                //         divTip.option("content",i18n.chkFlow_term_input_valid);
                //         divTip.show(30000);
                //         return;
                //     }
                //
                //     // $scope.variableBtn.disable = true;
                //     var para = getParaFromInput();
                //     var probe_round=parseInt(para.probe_interval)
                //     if(probe_round<60&&probe_round>0)
                //     {
                //         postVariableConfig(para);
                //     }
                //     else if(-1==probe_round||0==probe_round)
                //     {
                //         var tinyWindowOptions = {
                //             title : i18n.chkFlow_term_confirm_window,
                //             height : "250px",
                //             width : "400px",
                //             content: "<p>"+i18n.chkFlow_term_negative+"</p><p>"+i18n.chkFlow_term_zero+"</p>",
                //             resizable:true,
                //             buttons:[{
                //                 key:"btnOK",
                //                 label : 'OK',//按钮上显示的文字
                //                 focused : false,//默认焦点
                //                 handler : function(event) {//点击回调函数
                //                     postVariableConfig(para);
                //                     win.destroy();
                //                 }
                //             }, {
                //                 key:"btnCancel",
                //                 label : 'Cancel',
                //                 focused : true,
                //                 handler : function(event) {
                //                     win.destroy();
                //                     $scope.variableBtn.disable = false;
                //                 }
                //             }]
                //         };
                //         var win = new tinyWidget.Window(tinyWindowOptions);
                //         win.show();
                //     }
                // };

                // var getVariableConfig = function(){
                //      var promise = configFlowServ.getVariableConfig();
                //      promise.then(function(responseData){
                //          $scope.probeRoundTextBox.value = responseData.probe_period;
                //          $scope.probePortTextBox.value = responseData.port_count;
                //          $scope.reportRoundTextBox.value = responseData.report_period;
                //          $scope.packetsNumTextBox.value = responseData.pkg_count;
                //          $scope.timeDelayTextBox.value = responseData.delay_threshold;
                //          $scope.packetsLossTextBox.value = responseData.lossRate_threshold;
                //          $scope.dscpTextBox.value = responseData.dscp;
                //          $scope.lossPkgTimeOutTextBox.value = responseData.lossPkg_timeout;
                //      },function(responseData){
                //          //showERRORMsg
                //          commonException.showMsg(i18n.chkFlow_term_read_failed_config, "error");
                //      });
                //  };
                // function init(){
                //     getVariableConfig();
                // }
                // init();
            }
        ]

        var module = angular.module('common.config');
        module.tinyController('configFlow.ctrl', configFlowCtrl);
        return module;
        });