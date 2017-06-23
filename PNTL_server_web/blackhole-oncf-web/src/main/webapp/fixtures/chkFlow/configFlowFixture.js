define(["can/util/fixture/fixture", "tiny-lib/underscore"], function (fixture, _) {
    "use strict";
    var variables = {
    "config":[{
        "name":1,
        "value":2,
        "updateTime":3
    },
    {
        "name":1,
        "value":2,
        "updateTime":3
    }]
    };

    fixture({
        "GET /rest/v1/variables":function(original, response) {
            response(200, "success", variables, {})
        }
    });
    return fixture;
});