/**
 * 工程的module加载配置文件
 * module的基础路径为"工程名/"
 * Created on 13-12-12.
 */
"use strict";
require.config({
    "baseUrl": "./",
    "waitSeconds": 0,
    "paths": {
        "can": "lib/can", //桩路径
        "app": "src/app", //app文件夹路径
        "app-remote": "src/app", //app文件夹路径
        "ui-router": "lib/angular-ui/ui-router",
        "bootstrap": "lib/bootstrap2.3.2/js",
        "moment": "lib/moment/moment.min",
        "language": "i18n/default/" + window.urlParams.lang,
        "language-remote": "i18n/default/" + window.urlParams.lang,
        "lazy-load": "lib/lazy-load",
        "fixtures": "fixtures",
        "tiny-extra":"lib/tiny-extra"
    },
    "priority": [
        "angular"
    ]
});

//window.rest_prefix="/blackhole-chkflow-console/rest"
/*区分中英文加载不通的样式文件*/
function loadCss(filename) {
    var fileref = document.createElement('link');
    fileref.setAttribute("rel", "stylesheet");
    fileref.setAttribute("type", "text/css");
    fileref.setAttribute("href", filename);
    document.getElementsByTagName("head")[0].appendChild(fileref);
}
//不要修改格式，hash路径插件识别url键名，修改为其他形式会hash不到路径
var businessCssConfig = {
    "zh-cn": {
        "url": "theme/default/css/chkFlow.css"
    },
    "en-us": {
        "url": "theme/default/css/chkFlowEn.css"
    }
};
var businessCss = businessCssConfig[window.urlParams.lang] || businessCssConfig["en-us"];
window.bussinessVersion && (businessCss.url += ('?ttl=' + window.bussinessVersion));
loadCss(businessCss.url);

/**
 * 主启动类，手动给html element绑定module
 */
require(["config"], function () {
    $.extend(window.configData);
    var GLOBAL_CONFIG = window.configData || {};
    window.GLOBAL_CONFIG = GLOBAL_CONFIG;
    window.rest_prefix = "/rest";

    require(["app/framework/framework"], function (app) {
        angular.bootstrap($("html"), [app.name]);
    });
});

