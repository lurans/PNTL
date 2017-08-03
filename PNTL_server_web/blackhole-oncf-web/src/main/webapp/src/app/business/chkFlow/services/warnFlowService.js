/**
 * Created by xWX457942 on 2017/7/6.
 */
define([], function () {
    "use strict";
    var warnFlowService = function (exception, camel){
        var rest_prefix = window.rest_prefix;
        this.getTextInfo = function(){
            var uri = rest_prefix + "/chkflow/warningList";
            var promise = camel.get({
                "url": {
                    "s": uri
                },
                "timeout":60000
            });
            return promise;
        };
        this.postSearchData = function (data) {
            var uri = rest_prefix + "/chkflow/warningList";
            var promise = camel.post({
                "url": {
                    "s": uri
                },
                "params":data,
                "timeout":60000
            });
            return promise;
        };
        this.getInitialTotalData = function (params) {
            var uri = rest_prefix + "/chkflow/warningList";
            var promise = camel.get({
                "url": {
                    "s": uri
                },
                "params":params,
                "timeout":60000
            });
            return promise;
        };
    };

    var warnFlowModule = angular.module('common.config');
    warnFlowModule.tinyService('warnFlowServ',warnFlowService );
});