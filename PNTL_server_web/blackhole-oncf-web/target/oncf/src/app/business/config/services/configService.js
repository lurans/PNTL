define([], function () {
    "use strict";
    var service = function (exception, camel){
        var rest_prefix = window.rest_prefix;

        this.getConfig = function(){
            var promise = camel.get({
                "url": {
                    "s": rest_prefix + "/chkflow/config"
                },
                "timeout": 60000
            });
            return promise;
        };

        this.setConfig = function(data){
            var promise = camel.put({
                "url":{
                    "s": rest_prefix + "/chkflow/config"
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };

        this.getSsoConfig = function(){
            var promise = camel.get({
                "url": {
                    "s": rest_prefix + "/chkflow/sso"
                },
                "timeout": 60000
            });
            return promise;
        };


        this.restarTomcat = function(){
            var promise = camel.post({
                "url": {
                    "s": rest_prefix + "/chkflow/tomcat"
                },
                "timeout": 7*60000
            });
            return promise;
        };

        this.getFspToken=function(data){
            var promise = camel.post({
                "url": {
                    "s": rest_prefix + "/chkflow/token"
                },
                "params":data,
                "timeout": 3*60000
            });
            return promise;
        };

    };

    var chkFlowModule = angular.module('common.config');
    chkFlowModule.tinyService('configServ', service);
});
