/**
 * Created on 14-2-27.
 */
define(['bootstrap/bootstrap.min', 'app-remote/framework/localization/config'], function (bootstrap, localizationConfig) {
    "use strict";
    var ctrl = function ($rootScope, frameworkService, favoriteServiceMax,
                         heartbeatInterval, globalRegionName, currentService, $sce) {
        var i18nSubRegRex =  /\{\s*([^\|\}]+?)\s*(?:\|([^\}]*))?\s*\}/g;

        $rootScope.i18nReplace = function (s, o){
            if(!s || !o) {
                return;
            }
            return ((s.replace) ? s.replace(i18nSubRegRex, function (match, key) {
                return (!angular.isUndefined(o[key])) ? o[key] : match;
            }) : s);
        };

        // 切换language
        $rootScope.languages ={
            'en-us':"English",
            'zh-cn':"简体中文"
        };
        $rootScope.changeLanguage =function(language){
            var href = $rootScope.delUrlParameter(window.location.href, 'locale');
            href = $rootScope.addOrReplaceUrlParameter(href, 'locale',language);
            if(href === window.location.href) {
                window.location.reload();
            } else {
                window.location.href = href;
            }
        };

        $rootScope.logoutUrl = window.logoutPath;

        $rootScope.user_head_href = {
            "url": 'theme/default/images/user-head.png'
        };
        $rootScope.attention = {
            "url": 'theme/default/images/attention.png'
        };
    };


    ctrl.$injector = ["$rootScope", "frameworkService", "globalRegionName", "currentService"];
    return ctrl;
});