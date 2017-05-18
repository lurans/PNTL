/**
 * Created on 5/30/15.
 */

define([],
    function() {
        "use strict";
        var hwsModule = angular.module('hws', ['ng']);
        hwsModule.directive('hwsHref', function() {
            return {
                //设置优先级，保证在ng-href之后执行
                priority: 100,
                link: function(scope, elem, attrs) {
                    //只能使用click处理，如果使用模板的话，可能页面渲染的时候所需的值还未进行初始化
                    elem.bind('click', function() {
                        var href = elem.attr("href");
                        elem.attr("href", scope.genHWSHref(href));
                    });
                }
            };
        });

        hwsModule.directive('localeHref', function() {
            return {
                //设置优先级，保证在ng-href之后执行
                priority: 100,
                link: function(scope, elem, attrs) {
                    //只能使用click处理，如果使用模板的话，可能页面渲染的时候所需的值还未进行初始化
                    elem.bind('click', function() {
                        var href = elem.attr("href");
                        elem.attr("href", scope.genHWSHref(href, 'locale'));
                    });
                }
            };
        });
    });
