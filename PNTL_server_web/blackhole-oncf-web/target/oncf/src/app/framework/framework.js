/**
 * 主框架module， 该模块依赖于angularjs ng module和tiny wcc module
 * 所有的service router都在这里进行统一的配置
 * Created on 13-12-25.
 */
define(["ui-router/angular-ui-router",
        "language-remote/widgetsLanguage", 
	    "app-remote/framework/directive/hwsDirective",
        "app-remote/services/maskService",
        "app-remote/services/httpService",
        "app-remote/services/cookieService",
        "app-remote/services/exceptionService",
        "app-remote/services/msgService",
        "app-remote/framework/controllers/serviceCtrl",
        "app-remote/framework/controllers/menusCtrl",
        "app-remote/framework/services/frameworkService",
        "app-remote/services/utilService",
        "app/framework/configures/frameworkRouterConfig",
        "app/business/common/configures/commonRouterConfig"],

        function (router, widgetsLanguage, hws, mask, http, storage, exception, message, serviceCtrl,
                  menusCtrl, frameworkServ, utilService, frameworkConfig, routerConfig) {
        "use strict";

        // 设置控件国际化语言
        if (!window.tinyLanguage) {
            window.tinyLanguage = {};
        }
        window.tinyLanguage.language = widgetsLanguage;

        //注入框架的配置文件
        var dependency = [
            "ng",
            "wcc",
            "ui.router",
            "hws",
            frameworkConfig.name,
            routerConfig.name
        ];

        var framework = angular.module("framework", dependency);

        //如果是全局部署服务，请将该值设置为global(MOS请将该值设置为MOS)，页面区域信息将显示该值且将不再显示region下拉列表，否则设置为''
        framework.value('globalRegionName', '');
        framework.value('currentService', '');
        framework.value('favoriteServiceMax', 5);
        framework.value('heartbeatInterval', 5*60*1000);

        framework.controller("serviceCtrl", serviceCtrl);
        framework.controller("menusCtrl", menusCtrl);

        framework.service("mask", mask);
        framework.service("camel", http);
        framework.service("exception", exception);
        framework.service("storage", storage);
        framework.service("message", message);
        framework.service("frameworkService", frameworkServ);
        framework.service("utilService", utilService);

        window.appWebPath = "";

        //提供controllerPrivider, compileProvider常量
        framework.config(["$controllerProvider", "$compileProvider", "$sceDelegateProvider",
            function ($controllerProvider, $compileProvider, $sceDelegateProvider) {
            framework.controllerProvider = $controllerProvider;
            framework.compileProvider = $compileProvider;
            $sceDelegateProvider.resourceUrlWhitelist([
                // Allow same origin resource loads.
                'self',
                // Allow loading from our assets domain.  Notice the difference between * and **.
                'https://static.console.hwclouds.com/framework/**',
                'http://static.console.hwclouds.com/framework/**']);
        }]);

        //Tiny校验位置定制化处理
        window.tinyWidget.UnifyValid.defaultTipPos = "top";
        return framework;
    });