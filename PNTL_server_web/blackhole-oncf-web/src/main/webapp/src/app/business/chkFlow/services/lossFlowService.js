define([], function () {
    "use strict";
    var lossFlowService = function (exception, camel){
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
        this.getLossInfo = function(){
            var uri = rest_prefix + "/chkflow/lossRate";
            var promise = camel.get({
                "url": {
                    "s": uri
                },
                "timeout":60000
            });
            return promise;
        };
    };

    var lossFlowModule = angular.module('common.config');
    lossFlowModule.tinyService('lossFlowServ', lossFlowService);
});