define(["language-remote/framework"], function (i18n) {
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
        var types = ["danger", "error", "success", "warning", "info"];
        var fade_duration = 2000;
        var auto_fade_alerts_delay = 10000;

        this.alert = function (type, message, extra_tags) {
            var messageTemplate = $('<div class="alert alert-block fade in"><a class="close" data-dismiss="alert">&times;</a><p><strong></strong></p></div>');
            if (type === "danger") {
                messageTemplate.addClass("alert-danger");
                messageTemplate.find("strong").text((i18n.console_term_dangerous_value || "危险") + ":");
            }
            if (type === "error") {
                messageTemplate.addClass("alert-error");
                messageTemplate.find("strong").text((i18n.console_term_failure_value || "失败") + ":");
            }
            if (type === "success") {
                messageTemplate.addClass("alert-success");
                messageTemplate.find("strong").text((i18n.console_term_successful_value || "成功") + ":");
            }
            if (type === "warning") {
                messageTemplate.addClass("alert-warning");
                messageTemplate.find("strong").text((i18n.console_term_warning_value || "警告") + ":");
            }
            if (type === "info") {
                messageTemplate.addClass("alert-info");
                messageTemplate.find("strong").text((i18n.console_term_note_value || "提示") + ":");
            }
            messageTemplate.find("p").append($.encoder.encodeForHTML(message));
            messageTemplate.alert();
            messageTemplate.hide().prependTo("#service-content .messages").fadeIn(100);
            this.autoDismissAlert(messageTemplate);
            return messageTemplate;
        };

        this.clearDangerMessages = function () {
            $('#service-content .messages .alert.alert-danger').remove();
        };

        this.clearWarningMessages = function () {
            $('#service-content .messages .alert.alert-warning').remove();
        };

        this.clearInfoMessages = function () {
            $('#service-content .messages .alert.alert-info').remove();
        };

        this.clearErrorMessages = function () {
            $('#service-content .messages .alert.alert-error').remove();
        };

        this.clearSuccessMessages = function () {
            $('#service-content .messages .alert.alert-success').remove();
        };

        this.clearAllMessages = function () {
            this.clearDangerMessages();
            this.clearWarningMessages();
            this.clearInfoMessages();
            this.clearErrorMessages();
            this.clearSuccessMessages();
        };

        this.autoDismissAlerts = function () {
            var self = this;
            var $alerts = $('#service-content .messages .alert');
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
            $('#service-content .messages').css({
                "right": ($("#service-content").width() - $("#service-content>div[ui-view]").width()) / 2 + "px"
            });
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
