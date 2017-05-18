/*global define*/
define([
    "language/common"
], function (i18n) {
    "use strict";

    var ctrl = ["$scope", "$state", "$stateParams",
        function ($scope, $state, $stateParams) {
            $scope.i18n = i18n;

            /*此页作为中转，跳到目标页面*/
            $scope.$on("$viewContentLoaded", function () {
                if ($stateParams.state) {
                    $state.go($stateParams.state, {state: ""});
                }
            });
        }];

    var module = angular.module('common.config');
    module.tinyController('transfer.ctrl', ctrl);
    return module;
});