define([], function () {
    "use strict";
    var configFlowService = function (exception, camel){
        var rest_prefix = window.rest_prefix;

        this.background = $('<div>').css({
            "z-index": 10000000000,
            "background": "#aaaaaa url('theme/default/images/mask-cover.png') 50% 50% repeat-x",
            "opacity": ".30",
            "filter": "Alpha(Opacity=30)",
            "position": "fixed",
            "top": 0,
            "left": 0,
            "width": "100%",
            "height": "100%"
        });
        this.loading = $('<div>').css({
            "z-index": 10000000000,
            "margin": "auto",
            "text-align": "center",
            "position": "fixed",
            "width": "100%",
            "height": "100%",
            "top": 0,
            "background-image": "url('theme/default/images/mask-loading.gif')",
            "background-repeat": "no-repeat",
            "background-position": "50%"
        });
        this.show = function () {
            $("body").append(this.background);
            $("body").append(this.loading);
        };
        this.hide = function () {
            this.background.remove();
            this.loading.remove();
        };

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