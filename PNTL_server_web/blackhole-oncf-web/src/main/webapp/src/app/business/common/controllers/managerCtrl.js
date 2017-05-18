define([
    "language/common",
    "app/business/config/services/configService"
], function (i18n) {
    "use strict";

    var managerCtrl = ["$scope", "$rootScope", "$state", "$sce","$timeout","configServ",
        function ($scope, $rootScope, $state, $sce, $timeout,configServ) {
            $scope.i18n = i18n;

            /*为使左侧菜单能重复点击，先跳到空白页dmk*/
            $scope.go = function (state) {
                if (state !== $state.current.name) {
                    $state.go(state);
                }
                //else {
                //    $state.go("transfer", {state: state});
                //}
            };

            var server_ip_port = window.location.origin;
            var sso_ip_port = "";

            function getSsoIpPort(){
                var promise = configServ.getSsoConfig();
                promise.then(function(responseData) {
                    if(responseData.sso_ip !== null && responseData.sso_port !== null)
                        sso_ip_port = responseData.sso_ip + ":" + responseData.sso_port;
                    window.logoutPath = "https://" + sso_ip_port + "/unisso/logout?service=" + encodeURIComponent(server_ip_port + "/");
                    $rootScope.logoutUrl = window.logoutPath;
                },function(responseData){
                    //
                })
            }

            getSsoIpPort();
        }];
    var module = angular.module('common.config');
    module.tinyController('manager.ctrl', managerCtrl);

    return module;
});
