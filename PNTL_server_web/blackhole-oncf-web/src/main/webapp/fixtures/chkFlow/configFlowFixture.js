define(["can/util/fixture/fixture", "tiny-lib/underscore"], function (fixture, _) {
    "use strict";
    var variables = {
        "probe_period":60,
        "port_count":2,
        "report_period":70,
        "pkg_count":2,
        "delay_threshold":200,
        "lossRate_threshold":100,
        "dscp":2,
        "lossPkg_timeout":2,
        "ak":"oiafsdf456775453",
        "sk":"oiafsdf456775iop",
        "ip":"192.168.1.1"
    };


    fixture({
        "POST /rest/chkflow/uploadAgentPkg":function(original, response) {
            response(200, "success", variables, {})
        },
        "POST /rest/chkflow/uploadIpList":function(original, response) {
            response(200, "success", variables, {})
        },
        "POST /rest/chkflow/exitProbe":function(original, response) {
            response(200, "success", variables, {})
        },
        "POST /rest/chkflow/startAgents":function(original, response) {
            response(200, "success", variables, {})
        },
        "POST /rest/chkflow/stopProbe":function(original, response) {
            response(200, "success", variables, {})
        },
        "POST /rest/chkflow/pntlInit":function(original, response) {
            response(200, "success", variables, {})
        },
        "POST /rest/chkflow/pntlVariableConf":function(original, response) {
            response(200, "success", variables, {})
        },
        "POST /rest/chkflow/pntlAkSkConf":function(original, response) {
            response(200, "success", variables, {})
        },
        "GET /rest/chkflow/pntlConf":function(original, response) {
            response(200, "success", variables, {})
        },
    });
    return fixture;
});