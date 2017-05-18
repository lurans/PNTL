/**
 * 基于jquery ajax定义最原始通信类，它默认提供了GET, POST, DELETE, PUT(可以自定义)
 * 默认支持JSON格式的媒体类型
 *
 * 所有方法返回的是支持promise接口的对象（见：http://api.jquery.com/jQuery.ajax/）
 * """
 * The jqXHR objects returned by $.ajax() as of jQuery 1.5 implement the Promise interface, giving them all the properties, methods, and behavior of a Promise (see Deferred object for more information).
 * """
 * REST 规范参考：
 *
 */
define(['language-remote/framework'], function (i18n) {
    "use strict";

    var subRegRex = /\{\s*([^\|\}]+?)\s*(?:\|([^\}]*))?\s*\}/g;
    /**
     * Restful接口参数解析，
     * 如/app/kkk/{id}
     * sub("/app/kkk/{id}", {id: 124})
     */
    var sub = function (s, o) {
        return ((s.replace) ? s.replace(subRegRex, function (match, key) {
            return (!angular.isUndefined(o[key])) ? o[key] : match;
        }) : s);
    };
    var TIME_OUT = 10 * 60 * 1000;
    var redirect302 = function (xhr) {
        if (xhr.status === 200 && xhr.getResponseHeader('HW-AJAX-REDIRECT')) {
            window.location.reload();
        }
    };

    var redirect403 = function (xhr) {
        if (xhr.status === 403 && xhr.getResponseHeader('HW-IAM-FORBIDDEN')) {
            if($('#console_frame_forbidden_confirm').length === 0) {
                var options = {
                    type : "error",
                    content : i18n.console_term_errorForbidden_msg,
                    width : "550px",
                    modal : true,
                    "append-to" : "body",
                    open:function(evt){
                    },
                    close:function(evt){
                        window.location.href = i18n.console_term_consoleHome_link;
                    },
                    buttons:[{
                        key:"console_frame_forbidden_confirm",
                        label : i18n.console_term_confirm_button,
                        focused : true,
                        handler : function(event) {
                            msg.destroy();
                            window.location.href = i18n.console_term_consoleHome_link;
                        }
                    }]
                };

                var msg = new tinyWidget.Message(options);
                msg.show();
            }
        }
    };

    var service = function (mask, $q, storage, $rootScope) {

        this.get = function (config) {
            var deferred = $q.defer();
            var settings = {
                "type": "GET",
                "contentType": "application/json; charset=UTF-8",
                "timeout": config.timeout || TIME_OUT,
                "headers": {
                    "X-Language": window.urlParams.lang || "",
                    "cftk": storage.cookieStorage.getItem("cftk") || "",
                    "AgencyId": $rootScope.getUrlParameter('agencyId', true) || "",
                    "ProjectName": $rootScope.getUrlParameter('region', true) || "",
                    "region": $rootScope.selectRegionId || ""
                },
                "url": !angular.isString(config.url) ? sub(config.url.s, config.url.o) : config.url,
                "data": config.params || {},
                "beforeSend": function (request, setting) {
                    //是否支持阴影遮罩
                    if (config.mask) {
                        mask.show();
                    }
                    config.beforeSend && config.beforeSend(request, setting);
                },
                "complete": function (xhr, status) {
                    if (config.mask) {
                        mask.hide();
                    }
                    redirect302(xhr);
                    redirect403(xhr);
                }
            };
            if(config.contentType) {
                settings.contentType = config.contentType;
            }
            if(config.dataType) {
                settings.dataType = config.dataType;
            }
            var $ajax = $.ajax(settings);
            $ajax.success(function () {
                deferred.resolve.apply(deferred, arguments);
            }).error(function () {
                deferred.reject.apply(deferred, arguments);
            });
            return deferred.promise;
        };

        this.post = function (config) {
            var deferred = $q.defer();
            var settings = {
                "type": "POST",
                "contentType": "application/json; charset=UTF-8",
                "timeout": config.timeout || TIME_OUT,
                "headers": {
                    "X-Language": window.urlParams.lang,
                    "cftk": storage.cookieStorage.getItem("cftk") || "",
                    "AgencyId": $rootScope.getUrlParameter('agencyId', true) || "",
                    "ProjectName": $rootScope.getUrlParameter('region', true) || "",
                    "region": $rootScope.selectRegionId || ""
                },
                "url": !angular.isString(config.url) ? sub(config.url.s, config.url.o) : config.url,
                "data": typeof config.params === "string" ? config.params : JSON.stringify(config.params),
                "beforeSend": function (request, setting) {
                    //是否支持阴影遮罩
                    if (config.mask) {
                        mask.show();
                    }
                    config.beforeSend && config.beforeSend(request, setting);
                },
                "complete": function (xhr, status) {
                    if (config.mask) {
                        mask.hide();
                    }
                    redirect302(xhr);
                    redirect403(xhr);
                }
            };
            if(config.contentType) {
                settings.contentType = config.contentType;
            }
            if(config.dataType) {
                settings.dataType = config.dataType;
            }
            var $ajax = $.ajax(settings);
            $ajax.success(function () {
                deferred.resolve.apply(deferred, arguments);
            }).error(function () {
                deferred.reject.apply(deferred, arguments);
            });
            return deferred.promise;
        };

        this.deleter = function (config) {
            var deferred = $q.defer();
            var settings = {
                "type": "DELETE",
                "contentType": "application/json; charset=UTF-8",
                "timeout": config.timeout || TIME_OUT,
                "headers": {
                    "X-Language": window.urlParams.lang,
                    "cftk": storage.cookieStorage.getItem("cftk") || "",
                    "AgencyId": $rootScope.getUrlParameter('agencyId', true) || "",
                    "ProjectName": $rootScope.getUrlParameter('region', true) || "",
                    "region": $rootScope.selectRegionId || ""
                },
                "url": !angular.isString(config.url) ? sub(config.url.s, config.url.o) : config.url,
                "data": !config.params ? null : ('string' == typeof config.params ? config.params : JSON.stringify(config.params || {})),
                "beforeSend": function (request, setting) {
                    //是否支持阴影遮罩
                    if (config.mask) {
                        mask.show();
                    }
                    config.beforeSend && config.beforeSend(request, setting);
                },
                "complete": function (xhr, status) {
                    if (config.mask) {
                        mask.hide();
                    }
                    redirect302(xhr);
                    redirect403(xhr);
                }
            };
            if(config.contentType) {
                settings.contentType = config.contentType;
            }
            if(config.dataType) {
                settings.dataType = config.dataType;
            }
            var $ajax = $.ajax(settings);
            $ajax.success(function () {
                deferred.resolve.apply(deferred, arguments);
            }).error(function () {
                deferred.reject.apply(deferred, arguments);
            });
            return deferred.promise;
        };
        this.put = function (config) {
            var deferred = $q.defer();
            var settings = {
                "type": "PUT",
                "contentType": "application/json; charset=UTF-8",
                "timeout": config.timeout || TIME_OUT,
                "headers": {
                    "X-Language": window.urlParams.lang,
                    "cftk": storage.cookieStorage.getItem("cftk") || "",
                    "AgencyId": $rootScope.getUrlParameter('agencyId', true) || "",
                    "ProjectName": $rootScope.getUrlParameter('region', true) || "",
                    "region": $rootScope.selectRegionId || ""
                },
                "url": !angular.isString(config.url) ? sub(config.url.s, config.url.o) : config.url,
                "data": typeof config.params === "string" ? config.params : JSON.stringify(config.params || {}),
                "beforeSend": function (request, setting) {
                    //是否支持阴影遮罩
                    if (config.mask) {
                        mask.show();
                    }
                    config.beforeSend && config.beforeSend(request, setting);
                },
                "complete": function (xhr, status) {
                    if (config.mask) {
                        mask.hide();
                    }
                    redirect302(xhr);
                    redirect403(xhr);
                }
            };
            if(config.contentType) {
                settings.contentType = config.contentType;
            }
            if(config.dataType) {
                settings.dataType = config.dataType;
            }
            var $ajax = $.ajax(settings);
            $ajax.success(function () {
                deferred.resolve.apply(deferred, arguments);
            }).error(function () {
                deferred.reject.apply(deferred, arguments);
            });
            return deferred.promise;
        };

        this.patch = function (config) {
            var deferred = $q.defer();
            var settings = {
                "type": "PATCH",
                "contentType": "application/json; charset=UTF-8",
                "timeout": config.timeout || TIME_OUT,
                "headers": {
                    "X-Language": window.urlParams.lang,
                    "cftk": storage.cookieStorage.getItem("cftk") || "",
                    "AgencyId": $rootScope.getUrlParameter('agencyId', true) || "",
                    "ProjectName": $rootScope.getUrlParameter('region', true) || "",
                    "region": $rootScope.selectRegionId || ""
                },
                "url": !angular.isString(config.url) ? sub(config.url.s, config.url.o) : config.url,
                "data": typeof config.params === "string" ? config.params : JSON.stringify(config.params || {}),
                "beforeSend": function (request, setting) {
                    //是否支持阴影遮罩
                    if (config.mask) {
                        mask.show();
                    }
                    config.beforeSend && config.beforeSend(request, setting);
                },
                "complete": function (xhr, status) {
                    if (config.mask) {
                        mask.hide();
                    }
                    redirect302(xhr);
                    redirect403(xhr);
                }
            };
            if(config.contentType) {
                settings.contentType = config.contentType;
            }
            if(config.dataType) {
                settings.dataType = config.dataType;
            }
            var $ajax = $.ajax(settings);
            $ajax.success(function () {
                deferred.resolve.apply(deferred, arguments);
            }).error(function () {
                deferred.reject.apply(deferred, arguments);
            });
            return deferred.promise;
        };

        //返回jqXHR ，而非 $q.deffer().promise，使得如果状态码是2XX，调用的地方有办法获取具体状态码
        this.ajax = function (config) {
            var settings = {
                "type": config.type,
                "contentType": "application/json; charset=UTF-8",
                "timeout": config.timeout || TIME_OUT,
                "headers": {
                    "X-Language": window.urlParams.lang,
                    "cftk": storage.cookieStorage.getItem("cftk") || "",
                    "AgencyId": $rootScope.getUrlParameter('agencyId', true) || "",
                    "ProjectName": $rootScope.getUrlParameter('region', true) || "",
                    "region": $rootScope.selectRegionId || ""
                },
                "url": !angular.isString(config.url) ? sub(config.url.s, config.url.o) : config.url,
                "data": typeof config.params === "string" ? config.params : JSON.stringify(config.params || {}),
                "beforeSend": function (request, setting) {
                    //是否支持阴影遮罩
                    if (config.mask) {
                        mask.show();
                    }
                    config.beforeSend && config.beforeSend(request, setting);
                },
                "complete": function (xhr, status) {
                    if (config.mask) {
                        mask.hide();
                    }
                    redirect302(xhr);
                    redirect403(xhr);
                }
            };
            if(config.contentType) {
                settings.contentType = config.contentType;
            }
            if(config.dataType) {
                settings.dataType = config.dataType;
            }
            var $ajax = $.ajax(settings);
            return $ajax;
        };
    };

    service.$injector = ["mask", "$q", "storage", "$rootScope"];
    return service;
});