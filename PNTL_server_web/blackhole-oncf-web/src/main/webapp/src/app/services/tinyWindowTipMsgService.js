define([], function () {
    "use strict";
    $.fn.alert = function () {
        var self = $(this);
        self.find(".close").bind("click", function () {
            self.remove();
        });
    };

    var tipMessage = function (winId, $timeout) {
        if (!$timeout) {
            $timeout = setTimeout;
        }
        var target;
        var gWidth;
        if(winId) {
            target = "#" + winId;
            gWidth = $(target).width();
        }
        var types = ["error", "success"];
        var images = {
            "success": {"url": "theme/default/images/cloud-tips-success.png"},
            "error": {"url": "theme/default/images/cloud-tips-error.png"}
        };
        var fade_duration = 2000;
        var auto_fade_alerts_delay = 5000;

        this.alert = function (type, message, marginLeft, width) {
            if(!target) {
                return;
            }
            var messageTemplate = $('<div class="frame-cloud-window-messages-out"><div class="alert alert-block fade in frame-cloud-alert-block frame-cloud-alert-error frame-normal-font-size"><a class="close frame-cloud-close" data-dismiss="alert">&times;</a><p></p></div></div>');
            if (type === types[0]) {
                messageTemplate.children().first().addClass("frame-cloud-alert-error");
            } else if (type === types[1]) {
                messageTemplate.children().first().addClass("frame-cloud-alert-success");
            } else {
                return;
            }
            if (marginLeft) {
                messageTemplate.css({
                    "margin-left": marginLeft
                });
            }
            if (width) {
                messageTemplate.css({
                    "width": width
                });
            } else if (gWidth) {
                messageTemplate.css({
                    "width": gWidth
                });
            }

            messageTemplate.find("p")
                .append('<img class="frame-cloud-message-img" src="' + images[type].url + '">')
                .append($.encoder.encodeForHTML(message));
            messageTemplate.alert();
            this.clearAllMessages();
            messageTemplate.hide().insertBefore(target).fadeIn(100);
            this.autoDismissAlert(messageTemplate);
            return messageTemplate;
        };

        this.clearAllMessages = function () {
            var $prev = $(target).prev();
            if($prev.hasClass('frame-cloud-window-messages-out')) {
                $prev.remove();
            }
        };

        this.autoDismissAlerts = function () {
            var self = this;
            var $alerts = $(target).prevAll();
            $alerts.each(function (index, alert) {
                var $alert = $(this);
                if($alert.hasClass('frame-cloud-window-messages-out')) {
                    self.autoDismissAlert($alert);
                }
            });
        };

        this.autoDismissAlert = function (itemMessage) {
            $timeout(function () {
                itemMessage.fadeOut(fade_duration).remove();
            }, auto_fade_alerts_delay);
        };

        this.init = function () {
            var self = this;
            $('a.ajax-modal').click(function () {
                self.clearAllMessages();
            });
            self.autoDismissAlerts();
        };
        this.init();
    };
    return tipMessage;
});
