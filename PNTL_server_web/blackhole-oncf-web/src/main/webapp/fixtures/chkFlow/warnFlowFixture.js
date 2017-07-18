define(["can/util/fixture/fixture", "tiny-lib/underscore"], function (fixture, _) {
    "use strict";

    var textData =  [{
        "time":"20170101",
        "az_id":"1546464563156464646460",
        "pod_id":"20",
        "dst_ip":"192.168.0.1",
        "delay":"",
        "lossRate":"10.00%",
        "src_ip":"192.168.1.1"
    }, {
        "time":"2017-01-01 16:48:20",
        "az_id":"1",
        "pod_id":"20",
        "src_ip":"192.168.1.1",
        "dst_ip":"192.168.0.1",
        "delay":"200",
        "lossRate":""
    }];

    fixture({
        "POST /rest/chkflow/warningList":function(original, response) {
            response(200, "success", textData, {})
        },
        "POST /rest/chkflow/warningListe":function(original, response) {
            response(200, "success", textData, {})
        },
    });
    return fixture;
});
