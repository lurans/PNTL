define([], function () {
    "use strict";
    var service = function () {
        this.background = $('<div>').css({
            "z-index": 10000000000,
            "background": "#aaaaaa url('theme/default/images/mask-cover.png') 50% 50% repeat-x",
            "opacity": ".30",
            "filter": "Alpha(Opacity=30)",
            "position": "fixed",
            "top": 0,
            "left": 0,
            "width": "100%",
            "height": "100%"
        });
        this.loading = $('<div>').css({
            "z-index": 10000000000,
            "margin": "auto",
            "text-align": "center",
            "position": "fixed",
            "width": "100%",
            "height": "100%",
            "top": 0,
            "background-image": "url('theme/default/images/mask-loading.gif')",
            "background-repeat": "no-repeat",
            "background-position": "50%"
        });

        this.pageInitBackground = $('<div>').css({
            "z-index": 9999,
            "background": "#FFFFFF",
            "position": "fixed",
            "top": 0,
            "left": 0,
            "width": "100%",
            "height": "100%"
        });

        this.pageInitLoading = $('<div>').css({
            "z-index": 9999,
            "margin": "auto",
            "text-align": "center",
            "position": "fixed",
            "width": "100%",
            "height": "100%",
            "top": 0,
            "background-image": "url('theme/default/images/loading_big.gif')",
            "background-repeat": "no-repeat",
            "background-position": "50%"
        });

        this.show = function () {
            $("body").append(this.background);
            $("body").append(this.loading);
        };

        this.hide = function () {
            this.background.remove();
            this.loading.remove();
        };

        this.pageInitShow = function () {
            $("body").append(this.pageInitBackground);
            $("body").append(this.pageInitLoading);
        };

        this.pageInitHide = function () {
            this.pageInitBackground.remove();
            this.pageInitLoading.remove();
        };
    };

    return service;
});