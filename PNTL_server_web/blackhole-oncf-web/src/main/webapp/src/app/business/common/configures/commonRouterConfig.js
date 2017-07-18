define([
        "lazy-load/lazyLoad",
        "ui-router/angular-ui-router"
    ],
    function (lazyLoadModule) {
        "use strict";
        //定义框架的路由配置module
        var commonConfig = [
            {
                name: "transfer",
                url: "/transfer?state",
                templateUrl: "src/app/business/common/views/manager.html",
                controller: "transfer.ctrl",
                scripts: {
                    'controllers': ['app/business/common/controllers/transferCtrl']
                }
            },
            {
                name: "blackhole",
                url: "/blackhole",
                templateUrl: "src/app/business/chkFlow/views/blackhole.html",
                controller: "blackhole.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/blackholeCtrl']
                }
            },
            {
                name: "blackhole.manager",
                url: "/manager",
                templateUrl: "src/app/business/common/views/manager.html",
                controller: "manager.ctrl",
                scripts: {
                    'controllers': ['app/business/common/controllers/managerCtrl']
                }
            },
            //delayInfo
            {
                name:"blackhole.manager.delayFlow",
                url:"/delayFlow",
                templateUrl:"src/app/business/chkFlow/views/delayFlow.html",
                controller:"delayFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/delayFlowCtrl'],
                    'services': [
                        'app/business/chkFlow/services/delayFlowService',
                        'app/business/chkFlow/services/chkFlowService']
                }
            },
            //lossInfo
            {
                name:"blackhole.manager.lossFlow",
                url:"/lossFlow",
                templateUrl:"src/app/business/chkFlow/views/lossFlow.html",
                controller:"lossFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/lossFlowCtrl'],
                    'services' :[
                        'app/business/chkFlow/services/lossFlowService']
                }
            },
            //eip
            {
                name:"blackhole.manager.eipFlow",
                url:"/eipFlow",
                templateUrl:"src/app/business/chkFlow/views/eipFlow.html",
                controller:"eipFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/eipFlowCtrl'],
                    'services': [
                        'app/business/chkFlow/services/chkFlowService']
                }
            },
            //vpn
            {
                name:"blackhole.manager.locationFlow",
                url:"/locationFlow",
                templateUrl:"src/app/business/chkFlow/views/locationFlow.html",
                controller:"locationFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/locationFlowCtrl'],
                    'services': [
                        'app/business/chkFlow/services/locationFlowService']
                }
            },
            {//参数配置
                name:"blackhole.manager.configFlow",
                url:"/configFlow",
                templateUrl:"src/app/business/chkFlow/views/configFlow.html",
                controller:"configFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/configFlowCtrl'],
                    'services' :[
                        'app/business/chkFlow/services/chkFlowService',
                        'app/business/chkFlow/services/configFlowService']
                }
            },
            {//编辑参数
                name:"blackhole.manager.configFlow.deployConfig",
                url:"/deployConfig",
                templateUrl:"src/app/business/chkFlow/views/deployConfig.html",
                controller:"deployConfig.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/deployConfigCtrl'],
                    'services': [
                        'app/business/chkFlow/services/configFlowService']
                }
            },
            {
                name:"blackhole.manager.configFlow.variableConfig",
                url:"/variableConfig",
                templateUrl:"src/app/business/chkFlow/views/variableConfig.html",
                controller:"variableConfig.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/variableConfigCtrl'],
                    'services': [
                        'app/business/chkFlow/services/configFlowService']
                }
            },
            {
                name:"blackhole.manager.warnFlow",
                url:"/warnFlow",
                templateUrl:"src/app/business/chkFlow/views/warnFlow.html",
                controller:"warnFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/warnFlowCtrl'],
                    'services' :[
                        'app/business/chkFlow/services/warnFlowService']
                }
            }
        ];
        var module = angular.module("common.config", ["ui.router"]);
        module = lazyLoadModule.makeLazy(module);
        module.tinyStateConfig({stateConfig: commonConfig});

        function getAttr(scope, attr) {
            var data = null;
            try {
                data = scope.$eval(attr);
            } catch (e) {
            }

            return data;
        }

        // 加载效果
        module.directive("localLoading", function () {
            var config = {
                restrict: 'EA',
                template: "<div></div>",
                link: function (scope, iElement, iAttrs) {
                    $(iElement).parent().css("position", "relative");
                    $(iElement).parent().addClass("clearfix");
                    iElement = $(iElement).find("div");
                    $(iElement).addClass("local-loading");
                    $(iElement).append("<div class='local-loading-gif'></div>");
                    var height = getAttr(scope, iAttrs.height) || "100%";
                    var width = getAttr(scope, iAttrs.width) || "100%";
                    var display = getAttr(scope, iAttrs.display);
                    $(iElement).css("height", height);
                    $(iElement).css("width", width);
                    if (!display) {
                        $(iElement).hide();
                    }
                    scope.$watch(iAttrs.display, function (newValue, oldValue) {
                        if (newValue) {
                            $(iElement).show();
                        }
                        else {
                            $(iElement).hide();
                        }
                    });
                }
            };

            return config;
        });

        return module;
    });
