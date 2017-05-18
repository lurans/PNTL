define(["language/chkFlow",
        "fixtures/chkFlow/chkFlowFixture"],
    function (i18n, $, Step, _StepDirective, ViewMode) {
        "use strict";

        var blackholeCtrl = ["$scope",
            function ($scope) {
                $scope.i18n = i18n;

            }];

        var module = angular.module('common.config');
        module.tinyController('blackhole.ctrl', blackholeCtrl);

        return module;

    });