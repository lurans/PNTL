define(["app-remote/services/tipMsgService"], function (tipMessageService) {
    "use strict";
    function closeWidget(widget) {
        if (!widget) {
            return;
        }
        widget.destroy();
    }

    var exception = function () {
        var tipMessage = new tipMessageService();
        this.doException = function (response, widget) {
            tipMessage.alert("error", response.message);
        };

        //判断是否是一个真正的异常
        this.isException = function (response) {
            if (!response || /^2\d\d$/.test(response.status) || (response.responseText === "" && response.status !== 401)) {
                return false;
            }
            return true;
        };
    };

    return exception;
});