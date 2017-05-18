define(["app-remote/services/tipMsgService",
    "app-remote/services/tinyWindowTipMsgService",
    "language/common"],
    function (tipMessageService, windowTipService,i18n) {
    "use strict";

    var exception = {};
    /*先判断响应体的code，没code判断响应体的message，响应体不是JSON，则判断整个响应体
     *响应体不包含错误信息，则判断状态码
     *错误信息映射，兼容对象形式和字符串形式
     */
    exception.doException = function (response, exceptionI18n, windowId, params) {
        params = params || {};
        var message = this.getMessage(response, exceptionI18n);
        this.showMsg(message, "error", windowId, params);
    };
    exception.showMsg = function (message, type, windowId, params) {
        params = params || {};
        var marginLeft = params.marginLeft || 0;

        //弹出框内的报错，要把弹出框的ID传递进来
        if (windowId) {
            var windowTip = new windowTipService(windowId);
            windowTip.alert(type || "success", message);
        }
        else {
            var tipMessage = new tipMessageService();
            tipMessage.alert(type || "success", message, marginLeft);
        }
    };
    exception.getMessage = function (response, exceptionI18n) {
        var message = "";
        try {
            var responseObj = JSON.parse(response.responseText);
            //响应体格式如{error:{message,code}} 或 {message,code}
            var code = responseObj.error && responseObj.error.code || responseObj.code;
            //响应体里有code并且有错误信息映射，则根据code取映射，否则取响应体里的message
            var messageObj = (code && exceptionI18n) ? exceptionI18n[code] : "";
            //错误信息映射，可能是一个对象，也可能是字符串，也可能映射不到
            message = messageObj && messageObj.desc || messageObj;
        } catch (e) {
            //响应体不是JSON
        }
        message = message || {};
        return message;
    };
    exception.getCode = function (response) {
        var code = 0;
        try {
            var responseObj = JSON.parse(response.responseText);
            code = responseObj.error && responseObj.error.code || responseObj.code;
        } catch (e) {
        }
        //响应体没有错误码，返回状态码
        return code;
    };

    return exception;
});