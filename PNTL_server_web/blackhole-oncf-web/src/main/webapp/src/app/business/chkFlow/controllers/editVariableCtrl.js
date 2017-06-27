define(["language/chkFlow",
        "app/business/common/services/commonException",
        "fixtures/chkFlow/configFlowFixture"],
    function (i18n, commonException,Step, _StepDirective, ViewMode) {
        "use strict";

        var editVariableCtrl = ["$scope","$rootScope","$state","$sce", "$compile", "$timeout","configFlowServ",
            function($scope, $rootScope, $state, $sce, $compile, $timeout, configFlowServ){
                $scope.i18n = i18n;

                $scope.editVariableBtn = {
                    "id":"editVariableBtn_id",
                    "text":i18n.chkFlow_term_edit_variable_btn,
                    "minWidth":"60px",
                    "tipWidth":"60px",
                    "disable":false
                };
            }];

        var editVariablemodule = angular.module('common.config');
        editVariablemodule.tinyController('editVariable.ctrl', editVariableCtrl);
        return editVariablemodule;
    });