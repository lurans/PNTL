//浏览器版本检测
(function () {
    "use strict";
    function device(ua) {
        var os = this.os = {},
            browser = this.browser = {},
            webkit = ua.match(/Web[kK]it[\/]{0,1}([\d.]+)/),
            android = ua.match(/(Android);?[\s\/]+([\d.]+)?/),
            osx = !!ua.match(/\(Macintosh\; Intel /),
            ipad = ua.match(/(iPad).*OS\s([\d_]+)/),
            ipod = ua.match(/(iPod)(.*OS\s([\d_]+))?/),
            iphone = !ipad && ua.match(/(iPhone\sOS)\s([\d_]+)/),
            webos = ua.match(/(webOS|hpwOS)[\s\/]([\d.]+)/),
            touchpad = webos && ua.match(/TouchPad/),
            kindle = ua.match(/Kindle\/([\d.]+)/),
            silk = ua.match(/Silk\/([\d._]+)/),
            blackberry = ua.match(/(BlackBerry).*Version\/([\d.]+)/),
            bb10 = ua.match(/(BB10).*Version\/([\d.]+)/),
            rimtabletos = ua.match(/(RIM\sTablet\sOS)\s([\d.]+)/),
            playbook = ua.match(/PlayBook/),
            uc = ua.match(/UCBrowser\/([\w.\s]+)/),
            chrome = ua.match(/Chrome\/([\d.]+)/) || ua.match(/CriOS\/([\d.]+)/),
            firefox = ua.match(/Firefox\/([\d.]+)/),
            ie = ua.match(/MSIE\s([\d.]+)/) || ua.match(/Trident\/[\d](?=[^\?]+).*rv:([0-9.].)/),
            webview = !chrome && ua.match(/(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/),
            safari = webview || ua.match(/Version\/([\d.]+)([^S](Safari)|[^M]*(Mobile)[^S]*(Safari))/),
            orientation = Math.abs(window.orientation);

        if (browser.webkit = !!webkit) {
            browser.version = webkit[1];
        }

        if (android) {
            os.android = true;
            os.version = android[2];
        }
        if (iphone && !ipod) {
            os.ios = os.iphone = true;
            os.version = iphone[2].replace(/_/g, '.');
        }
        if (ipad) {
            os.ios = os.ipad = true;
            os.version = ipad[2].replace(/_/g, '.');
        }
        if (ipod) {
            os.ios = os.ipod = true;
            os.version = ipod[3] ? ipod[3].replace(/_/g, '.') : null;
        }
        if (webos) {
            os.webos = true;
            os.version = webos[2];
        }
        if (touchpad) {
            os.touchpad = true;
        }
        if (blackberry) {
            os.blackberry = true;
            os.version = blackberry[2];
        }
        if (bb10) {
            os.bb10 = true;
            os.version = bb10[2];
        }
        if (rimtabletos) {
            os.rimtabletos = true;
            os.version = rimtabletos[2];
        }
        if (playbook) {
            browser.playbook = true;
        }
        if (uc) {
            os.uc = true;
            os.ucversion = uc[1];
        }
        if (kindle) {
            os.kindle = true;
            os.version = kindle[1];
        }
        if (silk) {
            browser.silk = true;
            browser.version = silk[1];
        }
        if (!silk && os.android && ua.match(/Kindle Fire/)) {
            browser.silk = true;
        }
        if (orientation !== 90) {
            os.protrait = true;
        }
        if (orientation === 90) {
            os.landscape = true;
        }

        if (chrome) {
            browser.chrome = true;
            browser.version = chrome[1];
        }
        if (firefox) {
            browser.firefox = true;
            browser.version = firefox[1];
        }
        if (ie) {
            browser.ie = true;
            browser.version = ie[1];
        }
        if (safari && (osx || os.ios)) {
            browser.safari = true;
            if (osx) {
                browser.version = safari[1];
            }
        }
        if (webview) {
            browser.webview = true;
        }

        os.tablet = !!(ipad || playbook || (android && !ua.match(/Mobile/)) ||
        (firefox && ua.match(/Tablet/)) || (ie && !ua.match(/Phone/) && ua.match(/Touch/)));
        os.phone = !!(!os.tablet && !os.ipod && (android || iphone || webos || blackberry || bb10 ||
        (chrome && ua.match(/Android/)) || (chrome && ua.match(/CriOS\/([\d.]+)/)) ||
        (firefox && ua.match(/Mobile/)) || (ie && ua.match(/Touch/))));
    }

    function getBrowserInfo() {
        var Msie = /(msie\s|trident.*rv:)([\w.]+)/;
        var Firefox = /(firefox)\/([\w.]+)/;
        var Chrome = /(chrome)\/([\w.]+)/;
        var Opera = /(opera).+version\/([\w.]+)/;
        var Safari = /version\/([\w.]+).*(safari)/;

        var agent = navigator.userAgent.toLowerCase();
        var match = Msie.exec(agent);
        if (match) {
            return {
                browser: "IE",
                version: match[2] || "0"
            };
        }
        match = Firefox.exec(agent);
        if (match) {
            return {
                browser: match[1] || "",
                version: match[2] || "0"
            };
        }
        match = Chrome.exec(agent);
        if (match) {
            return {
                browser: match[1] || "",
                version: match[2] || "0"
            };
        }
        match = Opera.exec(agent);
        if (match) {
            return {
                browser: match[1] || "",
                version: match[2] || "0"
            };
        }
        match = Safari.exec(agent);
        if (match) {
            return {
                browser: match[2] || "",
                version: match[1] || "0"
            };
        }
        return "false";
    }

    var checkBrowser = function () {
        window.Device = new device(navigator.userAgent);
        var os = window.Device.os;
        if (os.phone || os.ios || os.android || os.iphone) {
            return true;
        }
        var currentInfo = getBrowserInfo();
        if ("false" === currentInfo) {
            return false;
        }

        var browser = currentInfo.browser.toLowerCase();
        var version = currentInfo.version;

        var supportArray = {"ie": "9.0", "firefox": "30", "chrome": "31", "safari": "5"};

        var flag = false;
        for (var index in supportArray) {
            if (browser && browser.toLowerCase() === index) {
                var ver = supportArray[index];
                if (parseFloat(version) >= parseFloat(ver)) {
                    flag = true;
                }
                return flag;
            }
        }
        return flag;
    };

    if (checkBrowser() === false) {
        window.location.href = '/error/supportBrowsers.html';
    } else {
        return true;
    }

})();