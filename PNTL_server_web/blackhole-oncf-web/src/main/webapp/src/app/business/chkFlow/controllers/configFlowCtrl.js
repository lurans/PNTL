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
                    },
                    {
                        state : "blackhole.manager.configFlow.upgradeConfig",
                        text : i18n.chkFlow_term_upgrade_config
                    }
                ];

            }
        ]

        var module = angular.module('common.config');
        module.tinyController('configFlow.ctrl', configFlowCtrl);
        return module;
        });