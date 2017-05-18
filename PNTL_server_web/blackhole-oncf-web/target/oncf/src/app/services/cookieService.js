//对cookie和本地存储操作的工具类
//保证在F5刷新时依然生效，只支持在会话阶段有效，当退出时强制销毁这些对象
define(function () {
    var cookieStorage = {
        "cache": [],
        "setItem": function (key, value, ttl) {
            var cookieString = key + "=" + encodeURIComponent(value);
            //判断是否设置过期时间
            if (ttl > 0) {
                var date = new Date();
                date.setTime(date.getTime + ttl);
                cookieString = cookieString + "; expire=" + date.toGMTString();
            }
            this.cache.push(key);
            document.cookie = cookieString;
        },
        "getItem": function (key) {
            var cookies = document.cookie.split("; ");
            var arr = null;
            var i = 0;
            while (i < cookies.length) {
                arr = cookies[i++].split("=");
                if (arr[0] == key) {
                    return decodeURIComponent(arr[1]);
                }
            }
            return "";
        },
        "removeItem": function (key) {
            var date = new Date();
            date.setTime(date.getTime() - 10000);
            document.cookie = key + "=v; expire=" + date.toGMTString();
        },
        "clear": function () {
            var cache = this.cache;
            var i = 0;
            while (i < cache.length) {
                this.removeItem(cache[i++]);
            }
        }
    };
    window.cookieStorage = window.cookieStorage || cookieStorage;
    var storage = function () {
        var storage = window.cookieStorage;
        if (typeof(Storage) !== "undefined") {
            storage = window.sessionStorage;
        }
        this.storage = storage;
        this.cookieStorage = cookieStorage;
    };
    storage.prototype.add = function (key, value, ttl) {
        var storage = this.storage;
        if (typeof(value) === "object") {
            value = JSON.stringify(value);
        }
        storage.setItem(key, value, ttl);
    };

    storage.prototype.get = function (key) {
        var storage = this.storage;
        var value = storage.getItem(key);
        try {
            return JSON.parse(value);
        }
        catch (e) {
            return value;
        }
    };

    storage.prototype.del = function (key) {
        var storage = this.storage;
        storage.removeItem(key);
    };

    //在页面点击注销的时候调用该API
    storage.prototype.flush = function () {
        var storage = this.storage;
        storage.clear();
    };

    return storage;
});