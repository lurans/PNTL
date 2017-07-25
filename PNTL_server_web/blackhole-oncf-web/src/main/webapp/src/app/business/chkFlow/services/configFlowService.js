define([], function () {
    "use strict";
    var configFlowService = function (exception, camel){
        var rest_prefix = window.rest_prefix;

        this.install= function(data){
            var uri = rest_prefix + "/chkflow/pntlInit";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };

        this.postVariableConfig = function(data){
            var uri = rest_prefix + "/chkflow/pntlVariableConf";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };
        this.postAkSk = function(data){
            var uri = rest_prefix + "/chkflow/pntlAkSkConf";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };
        this.startProbe = function(data){
            var uri = rest_prefix + "/chkflow/startAgents";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };
        this.stopProbe = function(data){
            var uri = rest_prefix + "/chkflow/stopProbe";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };
        this.getVariableConfig = function(){
            var uri = rest_prefix + "/chkflow/pntlConf";
            var promise = camel.get({
                "url": {
                    "s": uri
                },
                "timeout":60000
            });
            return promise;
        };
        this.uninstall = function(data){
            var uri = rest_prefix + "/chkflow/exitProbe";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };
    };

    var configFlowModule = angular.module('common.config');
    configFlowModule.tinyService('configFlowServ', configFlowService);
});