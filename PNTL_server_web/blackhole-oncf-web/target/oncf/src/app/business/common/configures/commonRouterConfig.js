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
            //east and west direction
            {
                name:"blackhole.manager.ewFlow",
                url:"/ewFlow",
                templateUrl:"src/app/business/chkFlow/views/ewFlow.html",
                controller:"ewFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/ewFlowCtrl'],
                    'services': [
                        'app/business/chkFlow/services/chkFlowService']
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
                name:"blackhole.manager.vpnFlow",
                url:"/vpnFlow",
                templateUrl:"src/app/business/chkFlow/views/vpnFlow.html",
                controller:"vpnFlow.ctrl",
                scripts: {
                    'controllers': ['app/business/chkFlow/controllers/vpnFlowCtrl'],
                    'services': [
                        'app/business/chkFlow/services/chkFlowService']
                }
            },{
                name:"blackhole.config",
                url:"/config",
                templateUrl:"src/app/business/config/views/config.html",
                controller:"config.ctrl",
                scripts: {
                    'controllers': ['app/business/config/controllers/configCtrl'],
                    'services': [
                        'app/business/config/services/configService']
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
