/**
 * urlDeal
 * Created on 2015/7/16.
 */
(function () {
    "use strict";
    function getUrlParameter(paramKey) {
        var sPageURL = window.location.search.substring(1);
        if (sPageURL) {
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++) {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] === paramKey) {
                    return sParameterName[1];
                }
            }
        }
    }

    //the first is the defualt
    var supportLanguage = ['zh-cn','en-us'];

    function getLanguage(key) {
        var result = getUrlParameter(key);

        if (!window.urlParams) {
            window.urlParams = {};
        }
        if (!result) {
            result = getCookie(key);
        } else {
            setCookie(key, result);
        }

        if (supportLanguage.indexOf(result) >= 0) {
            window.urlParams.lang = result;
        } else {
            window.urlParams.lang = supportLanguage[0];
        }

        if(window.urlParams.lang === 'zh-cn') {
            window.tinyLanguageToken = 'zh';
        } else {
            window.tinyLanguageToken = 'en';
        }
    }

    function trimEmpty(value) {
        if (!value) {
            return '';
        }
        return value.replace(/(^\s*)|(\s*$)/g, '');
    }

    function getCookie(key) {
        if (!document.cookie) {
            return null;
        }
        var consoleCookies = document.cookie.split(';');
        var cookie;
        for (var i = 0; i < consoleCookies.length; i++) {
            cookie = consoleCookies[i].split('=');
            if (cookie && cookie.length >= 2 && key === trimEmpty(cookie[0])) {
                return trimEmpty(cookie[1]);
            }
        }
    }

    function setCookie(cname, cvalue) {
        document.cookie = cname + '=' + cvalue + ';path=/';
    }

    window.bussinessVersion = getCookie('ttl');
    window.frameworkVersion = getCookie('ttf');
    getLanguage('locale');
})();