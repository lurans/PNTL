/**
 * 框架Controller， 设置服务菜单的视图控制逻辑
 * Created on 13-12-25.
 */
define(['language-remote/framework',
    'app/business/common/services/commonException'], function (i18n,commonException) {
    "use strict";

    var ctrl = function ($rootScope, $state, $stateParams,mask,frameworkService,$timeout, $sce) {

        $rootScope.supportLanguage = [["en-us", "English"], ["zh-cn", "中文(简体)"]];

        $rootScope.i18n = i18n;
        $rootScope.language = window.urlParams.lang;
        $rootScope.languageName = getLanguageName($rootScope.language, $rootScope.supportLanguage);
        mask.pageInitShow();

        $rootScope.menus = {
            url: "src/app/framework/views/menus.html"
        };
        $rootScope.footer = {
            url: "src/app/framework/views/footer.html"
        };

        $rootScope.$state = $state;
        $rootScope.$stateParams = $stateParams;

        //通过点击事件关闭错误提示
        $rootScope.closeMenusFavoriteError = function() {
            $rootScope.favoriteError = false;
        };

        //动态设置中间层高度
        angular.element(window).bind("resize", setContentMinHeight);
        $rootScope.$on('$viewContentLoaded', function() {
            setContentMinHeight();
            mask.pageInitHide();
        });

        //导航随水平滚动条滚动
        angular.element(window).bind("scroll", setMenuLeft);

        function getLanguageName(key, languages) {
            if(languages) {
                for(var i=0; i<languages.length; i++) {
                    if(key === languages[i][0]) {
                        return languages[i][1];
                    }
                }
            }

            return null;
        }
        function setContentMinHeight() {
            var height1 = $(window).height();
            //UI规范窗口最小高度600
            if(height1 < 600) {
                height1 = 600;
            }
            var height2 = $('#service-content').css('padding-top') || '60px';
            height2 = height2.replace('px', '');
            var height3 = $('#console-top-footer').height() || 101;
            $('#service-content').css('min-height', height1 - height2 - height3);
        }

        function setMenuLeft() {
            if($('#service-menus .menu-top-line').css('display') === 'none') {
                return;
            }
            $('#service-menus').css('left', (0 - $(window).scrollLeft()) + 'px');
            var scrollTop = $(window).scrollTop();
            if(scrollTop > 78) {
                scrollTop = 78;
                $('#service-menus .console-topbar-btn-right').css('display', 'block');
            } else {
                scrollTop = 0;
                $('#service-menus .console-topbar-btn-right').css('display', 'none');
            }

            $('#service-menus').css('top', (0 - scrollTop) + 'px');
            $('.framework-scrolling').css('top',  (140 - scrollTop) + 'px');
        }

        $rootScope.genHWSHref = function(href, flag){
            if(!href || href === '' || href === '#') {
                return href;
            }

            if(!flag) {
                href = $rootScope.addOrReplaceUrlParameter(href, 'agencyId', $rootScope.getUrlParameter('agencyId', true));

                var region = $rootScope.getUrlParameter('region', true);
                if(region && region !== '' && region !== 'null') {
                    href = $rootScope.addOrReplaceUrlParameter(href, 'region', region);
                }
                //处理国际化
                href = $rootScope.addOrReplaceUrlParameter(href, 'locale', window.urlParams.lang);
            } else if(flag === 'locale') {
                //处理国际化
                href = $rootScope.addOrReplaceUrlParameter(href, 'locale', window.urlParams.lang);
            }

            var indexHash = href.indexOf('#/');
            //没有#号
            if(indexHash === -1) {
                return href;
            }
            var indexQuery = href.indexOf('?');
            //没有查询参数
            if(indexQuery === -1) {
                return href.replace('#/', '?hws_route_url=');
            } else if(indexHash < indexQuery) { //#号在?前面
                href = href.replace('?', '&');
                return href.replace('#/', '?hws_route_url=');
            } else { //#号在?后面
                var tmpHrefs = href.split('#/');
                tmpHrefs[1] = tmpHrefs[1].replace('?', '&');
                return tmpHrefs[0] + '&hws_route_url=' + tmpHrefs[1];
            }
        };

        //!value为true时删除参数
        $rootScope.addOrReplaceUrlParameter = function(href, key, value) {
            if(!href || !key) {
                return href;
            }
            var hrefs = href.split('#/');
            var hrefPostfix = '';
            //将#后面的路由信息拼接到URL最后
            if(hrefs.length > 1) {
                hrefPostfix = '#/' + hrefs[1];
            }

            hrefs[0] = $rootScope.delUrlParameter(hrefs[0], key);
            if(value) {
                if(hrefs[0].indexOf('?') !== -1) {
                    hrefs[0] = hrefs[0] + '&' + key + '=' + value;
                } else {
                    hrefs[0] = hrefs[0] + '?' + key + '=' + value;
                }
            }

            return hrefs[0] + hrefPostfix;

        };

        $rootScope.getUrlParameter = function(paramKey, scopeFlag) {
            var sPageURL = window.location.search.substring(1);
            if(sPageURL) {
                var sURLVariables = sPageURL.split('&');
                for (var i = 0; i < sURLVariables.length; i++) {
                    var sParameterName = sURLVariables[i].split('=');
                    if (sParameterName[0] === paramKey) {
                        return sParameterName[1];
                    }
                }
            }
            if(scopeFlag) {
                if(paramKey === 'agencyId') {
                    return $rootScope.userId;
                } else if(paramKey === 'region') {
                    return encodeURIComponent($rootScope.projectName || '');
                }
            } else {
                return null;
            }
        };

        $rootScope.delUrlParameter = function(url, name){
            return url
                .replace(new RegExp('[?&]' + name + '=[^&#]*(#.*)?$'), '$1')
                .replace(new RegExp('([?&])' + name + '=[^&]*&'), '$1');
        };

        //Timeout Remind
        var myTime;

        function isSsoLogin() {
            var promise = frameworkService.getSsoLogin();
            promise.then(function(responseData){
                if(responseData.sso_login === "true") {
                    $rootScope.username = responseData.sso_user;
                    $rootScope.logoutShow = true;
                    myTime = $timeout(function(){Timeout();}, 15*60*1000);
                    document.documentElement.onkeydown = resetTime;
                    document.documentElement.onclick = resetTime;
                }
            },function(responseData){
            });
        }

        function resetTime() {
            $timeout.cancel(myTime);
            myTime = $timeout(function(){Timeout();}, 15*60*1000);
        }

        function Timeout() {
            timeoutRemindDiag();
        }

        function timeoutRemindDiag(){
            var newWin = new tinyWidget.Window({
                "winId": "timeout_id",
                "title": i18n.chkFlow_term_timeout,
                "content-type": "url",
                "content": "src/app/business/common/views/timeout.html",
                "height": "auto",
                "width": "400px",
                "close":function(){
                    newWin.destroy();
                    window.location.href = window.logoutPath;
                },
                resizable: true,
                "buttons": [
                    {
                        label: i18n.chkFlow_term_timeout_OK,
                        default: true,
                        handler: function (event) {
                            newWin.destroy();
                            window.location.href = window.logoutPath;
                        }
                    }
                ]
            });
            newWin.show();
        }

        //MDK FSP Configuration
        function verifyConfig(){
            var promise = frameworkService.isConfigValid();
            promise.then(function(responseData){
                if ( undefined != responseData.err_msg)
                    commonException.showMsg(i18n.chkflow_term_config_error, "error");
            },function(responseData){
                commonException.showMsg(i18n.chkflow_term_config_error, "error");
            });
        }

        isSsoLogin();
        verifyConfig();
    };
    ctrl.$injector = ["$rootScope", "$state", "$stateParams"];
    return ctrl;
});