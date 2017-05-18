/**
 * Created on 13-12-18.
 */
define(["can/util/fixture/fixture", "tiny-lib/underscore"], function (fixture, _) {
    var regions = {
        "regions": [{
            "seqId": 1000008,
            "id": "eastchina",
            "name": "Magdeburg",
            "created": "2015-05-30 18:10:15",
            "lastModified": "2015-05-30 18:10:15",
            "locale": "zh-CN",
            "active": true
        }, {
            "seqId": 1000010,
            "id": "northchina",
            "name": "Biere",
            "created": "2015-05-30 18:12:24",
            "lastModified": "2015-05-30 18:12:24",
            "locale": "zh-CN",
            "active": true
        }], "total": 3
    };
    var user = {
        "id": "d324524ce04b41858e6689f41dfcd4fe",
        "name": "Stephan",
        "region": "eastchina"
    };

    var ssoLogin = {
        "sso_login":"true",
        "sso_user":"blackhole"
    };

    var config = {
        "valid":"true"
    };

    fixture({
        "GET /rest/chkflow/ssoLogin":function(original, response){
            response(200, "success", ssoLogin, {})
        },
        "GET /rest/chkflow/token":function(original, response){
            response(200, "success", config, {})
        },
        "GET /rest/v1.0/regions": function (original, response) {
            response(200, "success", regions, {});
        },
        "GET /me": function (original, response) {
            response(403, "success", user, {});
        },
        "GET /rest/changeRegion":function(original, response){
            response(200, "success", {"result":'success'}, {});
        }
    });

    return fixture;
});