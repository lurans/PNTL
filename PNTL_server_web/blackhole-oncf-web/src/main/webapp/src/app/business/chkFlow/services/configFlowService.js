define([], function () {
    "use strict";
    var configFlowService = function (exception, camel){
        var rest_prefix = window.rest_prefix;

        this.getVariablesList = function(){
            var uri = rest_prefix + "/v1/variables";
            var promise = camel.get({
                "url": {
                    "s": uri
                },
                "timeout":60000
            });
            return promise;
        };
    };

    var configFlowModule = angular.module('common.config');
    configFlowModule.tinyService('configFlowServ', configFlowService);
});