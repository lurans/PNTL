define([], function () {
    "use strict";
    $.fn.alert = function () {
        var self = $(this);
        self.find(".close").bind("click", function () {
            self.remove();
        });
    };

    var tipMessage = function ($timeout) {
        if (!$timeout) {
            $timeout = setTimeout;
        }
        
        var target = "#frame-cloud-messages-tips";
        var types = ["error", "success"];
        var images = {
            "success": {"url": "theme/default/images/cloud-tips-success.png"},
            "error": {"url": "theme/default/images/cloud-tips-error.png"}
        };
        var fade_duration = 2000;
        var auto_fade_alerts_delay = 5000;

        this.alert = function (type, message, marginLeft, width) {
            var messageTemplate = $('<div class="alert alert-block fade in frame-cloud-alert-block frame-normal-font-size"><a class="close frame-cloud-close" data-dismiss="alert">&times;</a><p></p></div>');
            if (type === types[0]) {
                messageTemplate.addClass("frame-cloud-alert-error");
            } else if (type === types[1]) {
                messageTemplate.addClass("frame-cloud-alert-success");
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
            }

            messageTemplate.find("p")
                .append('<img class="frame-cloud-message-img" src="' + images[type].url + '">')
                .append($.encoder.encodeForHTML(message));
            messageTemplate.alert();
            messageTemplate.hide().prependTo(target).fadeIn(100);
            this.autoDismissAlert(messageTemplate);
            return messageTemplate;
        };

        this.clearErrorMessages = function () {
            $(target + ' .alert.frame-cloud-alert-error').remove();
        };

        this.clearSuccessMessages = function () {
            $(target + ' .alert.frame-cloud-alert-success').remove();
        };

        this.clearAllMessages = function () {
            this.clearErrorMessages();
            this.clearSuccessMessages();
        };

        this.autoDismissAlerts = function () {
            var self = this;
            var $alerts = $(target + ' .alert');
            $alerts.each(function (index, alert) {
                var $alert = $(this),
                    types = $alert.attr('class').split(' '),
                    intersection = $.grep(types, function (value) {
                        return $.inArray(value, types) !== -1;
                    });
                // Check if alert should auto-fade
                if (intersection.length > 0) {
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
