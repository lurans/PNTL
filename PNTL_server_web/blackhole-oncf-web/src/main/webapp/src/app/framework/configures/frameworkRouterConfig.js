/**
 * 定义的整体框架的 路由地址
 * Created on 13-12-25.
 */
define(["ui-router/angular-ui-router"], function () {
    "use strict";

    var serviceConfigs = ["$stateProvider", "$urlRouterProvider", function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/blackhole/manager/eipFlow");
    }];

    var frameworkConfig = angular.module("frm", ["ui.router"]);
    frameworkConfig.config(serviceConfigs);
    return frameworkConfig;
});