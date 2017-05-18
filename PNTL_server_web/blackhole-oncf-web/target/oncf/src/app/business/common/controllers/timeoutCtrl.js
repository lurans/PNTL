define([
    "language/chkFlow"
], function (i18n, $) {
    "use strict";

    var timeoutCtrl = ["$scope", "$rootScope",
        function ($scope, $rootScope) {
            $scope.i18n = i18n;

        }];
    var module = angular.module('common.config');
    module.tinyController('timeout.ctrl', timeoutCtrl);

    return module;
});
