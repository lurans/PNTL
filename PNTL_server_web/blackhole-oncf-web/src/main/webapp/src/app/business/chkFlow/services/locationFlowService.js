define([], function () {
    "use strict";
    var service = function (exception, camel){
        var rest_prefix = window.rest_prefix;

        var locationFlowInfo = {
            //input
            "sourceIp_value":null,
            "remoteIp_value":null
        };
        this.getLocationFlowInfo = function(){
            return locationFlowInfo;
        }

    };
    var locationFlowModule = angular.module('common.config');
    locationFlowModule.tinyService('locationFlowServ', service);
})
