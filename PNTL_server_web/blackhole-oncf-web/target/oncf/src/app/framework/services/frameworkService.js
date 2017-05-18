/**
 * Created on 2015/1/31.
 */
define(["fixtures/frameworkFixture"], function () {
    "use strict";
    var service = function ($q, camel) {
        var rest_prefix = window.rest_prefix;

        this.getSsoLogin = function(){
            var promise = camel.get({
                "url": {
                    "s": rest_prefix + "/chkflow/ssoLogin"
                },
                "timeout": 60000
            });
            return promise;
        };

        this.isConfigValid = function(){
            var promise = camel.get({
                "url": {
                    "s": rest_prefix + "/chkflow/token"
                },
                "timeout": 60000
            });
            return promise;
        };
    };

    service.$injector = ["$q", "camel"];
    return service;
});