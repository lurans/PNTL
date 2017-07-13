define([], function () {
    "use strict";
    var delayFlowService = function (exception, camel){
        var rest_prefix = window.rest_prefix;

        this.postIpList = function(data){
            var uri = rest_prefix + "/chkflow/ipList";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };
        this.getIpList = function(){
            var uri = rest_prefix + "/chkflow/ipList";
            var promise = camel.get({
                "url": {
                    "s": uri
                },

                "timeout":60000
            });
            return promise;
        };
        this.getDelayInfo = function(){
            var uri = rest_prefix + "/chkflow/delayInfo";
            var promise = camel.get({
                "url": {
                    "s": uri
                },
                "timeout":60000
            });
            return promise;
        };
    };

    var delayFlowModule = angular.module('common.config');
    delayFlowModule.tinyService('delayFlowServ', delayFlowService);
});