/**
 * 消息提示框
 * MessageBox的公共服务
 */
define([], function () {
    "use strict";

    var encoder = $.encoder;
    //这里不采用依赖注入的方式是必须在module中去加载，影响性能
    var service = function () {
        this.exceptionMsg = function (code, desc, cause, solution) {
            var options = {
                "type": "error",
                "content": "异常码: " + encoder.encodeForHTML(code) + "<br/>" + "异常描述: " + encoder.encodeForHTML(desc),
                "width": "360px",
                "height": "200px"
            };
            var msg = new tinyWidget.Message(options);
            msg.show();
        };

        this.oneBtnMsg = function (options) {
            var configs = {
                "type": options.type || "prompt",
                "content": encoder.encodeForHTML(options.message),
                "width": "360px",
                "height": "200px",
                "buttons": [
                    {
                        "label": '确定',
                        "focused": true,
                        "handler": function (event) {
                            msg.destroy();
                            options.callback && options.callback();
                        }
                    }
                ]
            };
            var msg = new tinyWidget.Message(configs);
            msg.show();
        };

        this.twoBtnMsg = function (options) {
            var configs = {
                "type": options.type || "warn",
                "content": encoder.encodeForHTML(options.message),
                "width": "360px",
                "height": "200px",
                "buttons": [
                    {
                        "label": '确定',
                        "focused": false,
                        "handler": function (event) {
                            msg.destroy();
                            options.callback && options.callback();
                        }
                    },
                    {
                        "label": '取消',
                        "focused": true,
                        "handler": function (event) {
                            msg.destroy();
                        }
                    }
                ]
            };
            var msg = new tinyWidget.Message(configs);
            msg.show();
        };
    };

    return service;
});