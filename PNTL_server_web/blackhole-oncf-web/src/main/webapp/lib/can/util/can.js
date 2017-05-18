define([], function () {
    var can = window.can || {};
    if (typeof GLOBALCAN === 'undefined' || GLOBALCAN !== false) {
        window.can = can;
    }
    can.isDeferred = function (obj) {
        var isFunction = this.isFunction;
        // Returns `true` if something looks like a deferred.
        return obj && isFunction(obj.then) && isFunction(obj.pipe);
    };

    var cid = 0;
    can.cid = function (object, name) {
        if (object._cid) {
            return object._cid
        } else {
            return object._cid = (name || "" ) + (++cid)
        }
    };
    can.each = function (elements, callback, context) {
        var i = 0, key;
        if (elements) {
            if (typeof elements.length === 'number' && elements.pop) {
                if (elements.attr) {
                    elements.attr('length');
                }
                for (key = elements.length; i < key; i++) {
                    if (callback.call(context || elements[i], elements[i], i, elements) === false) {
                        break;
                    }
                }
            } else if (elements.hasOwnProperty) {
                for (key in elements) {
                    if (elements.hasOwnProperty(key)) {
                        if (callback.call(context || elements[key], elements[key], key, elements) === false) {
                            break;
                        }
                    }
                }
            }
        }
        return elements;
    };

    // _jQuery node list._
    $.extend(can, $, {
        trigger: function (obj, event, args) {
            if (obj.trigger) {
                obj.trigger(event, args);
            } else {
                $.event.trigger(event, args, obj, true);
            }
        },
        addEvent: function (ev, cb) {
            $([this]).bind(ev, cb);
            return this;
        },
        removeEvent: function (ev, cb) {
            $([this]).unbind(ev, cb);
            return this;
        },
        // jquery caches fragments, we always needs a new one
        buildFragment: function (elems, context) {
            var oldFragment = $.buildFragment,
                ret;

            elems = [elems];
            // Set context per 1.8 logic
            context = context || document;
            context = !context.nodeType && context[0] || context;
            context = context.ownerDocument || context;

            ret = oldFragment.call(jQuery, elems, context);

            return ret.cacheable ? $.clone(ret.fragment) : ret.fragment || ret;
        },
        $: $,
        each: can.each
    });

    // Wrap binding functions.
    $.each(['bind', 'unbind', 'undelegate', 'delegate'], function (i, func) {
        can[func] = function () {
            var t = this[func] ? this : $([this]);
            t[func].apply(t, arguments);
            return this;
        };
    });

    // Wrap modifier functions.
    $.each(["append", "filter", "addClass", "remove", "data", "get"], function (i, name) {
        can[name] = function (wrapped) {
            return wrapped[name].apply(wrapped, can.makeArray(arguments).slice(1));
        };
    });

    // Memory safe destruction.
    var oldClean = $.cleanData;

    $.cleanData = function (elems) {
        $.each(elems, function (i, elem) {
            if (elem) {
                can.trigger(elem, "destroyed", [], false);
            }
        });
        oldClean(elems);
    };

    // ##string.js
    // _Miscellaneous string utility functions._

    // Several of the methods in this plugin use code adapated from Prototype
    // © 2005-2007 Sam Stephenson
    var strUndHash = /_|-/,
        strColons = /\=\=/,
        strWords = /([A-Z]+)([A-Z][a-z])/g,
        strLowUp = /([a-z\d])([A-Z])/g,
        strDash = /([a-z\d])([A-Z])/g,
        strReplacer = /\{([^\}]+)\}/g,
        strQuote = /"/g,
        strSingleQuote = /'/g,

    // Returns the `prop` property from `obj`.
    // If `add` is true and `prop` doesn't exist in `obj`, create it as an
    // empty object.
        getNext = function (obj, prop, add) {
            var result = obj[prop];

            if (result === undefined && add === true) {
                result = obj[prop] = {}
            }
            return result
        },

    // Returns `true` if the object can have properties (no `null`s).
        isContainer = function (current) {
            return (/^f|^o/).test(typeof current);
        };
    can.extend(can, {
        // Escapes strings for HTML.
        /**
         * @function can.esc
         * @parent can.util
         *
         * `can.esc(string)` escapes a string for insertion into html.
         *
         *     can.esc( "<foo>&<bar>" ) //-> "&lt;foo&lt;&amp;&lt;bar&lt;"
         */
        esc: function (content) {
            // Convert bad values into empty strings
            var isInvalid = content === null || content === undefined || (isNaN(content) && ("" + content === 'NaN'));
            return ( "" + ( isInvalid ? '' : content ) )
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(strQuote, '&#34;')
                .replace(strSingleQuote, "&#39;");
        },

        /**
         * @function can.getObject
         * @parent can.util
         * Gets an object from a string.  It can also modify objects on the
         * 'object path' by removing or adding properties.
         *
         *     Foo = {Bar: {Zar: {"Ted"}}}
         *     can.getObject("Foo.Bar.Zar") //-> "Ted"
         *
         * @param {String} name the name of the object to look for
         * @param {Array} [roots] an array of root objects to look for the
         *   name.  If roots is not provided, the window is used.
         * @param {Boolean} [add] true to add missing objects to
         *  the path. false to remove found properties. undefined to
         *  not modify the root object
         * @return {Object} The object.
         */
        getObject: function (name, roots, add) {

            // The parts of the name we are looking up
            // `['App','Models','Recipe']`
            var parts = name ? name.split('.') : [],
                length = parts.length,
                current,
                r = 0,
                i, container, rootsLength;

            // Make sure roots is an `array`.
            roots = can.isArray(roots) ? roots : [roots || window];

            rootsLength = roots.length;

            if (!length) {
                return roots[0];
            }

            // For each root, mark it as current.
            for (r; r < rootsLength; r++) {
                current = roots[r];
                container = undefined;

                // Walk current to the 2nd to last object or until there
                // is not a container.
                for (i = 0; i < length && isContainer(current); i++) {
                    container = current;
                    current = getNext(container, parts[i]);
                }

                // If we found property break cycle
                if (container !== undefined && current !== undefined) {
                    break
                }
            }

            // Remove property from found container
            if (add === false && current !== undefined) {
                delete container[parts[i - 1]]
            }

            // When adding property add it to the first root
            if (add === true && current === undefined) {
                current = roots[0]

                for (i = 0; i < length && isContainer(current); i++) {
                    current = getNext(current, parts[i], true);
                }
            }

            return current;
        },
        // Capitalizes a string.
        /**
         * @function can.capitalize
         * @parent can.util
         * @description Capitalize the first letter of a string.
         * @signature `can.capitalize(str)`
         * @param {String} str The string to capitalize.
         * @return {String} The string with the first letter capitalized.
         *
         *        can.capitalize('candy is fun!'); //-> Returns: 'Candy is fun!'
         *
         */
        capitalize: function (s, cache) {
            // Used to make newId.
            return s.charAt(0).toUpperCase() + s.slice(1);
        },

        // Underscores a string.
        /**
         * @function can.underscore
         * @parent can.util
         *
         * Underscores a string.
         *
         *     can.underscore("OneTwo") //-> "one_two"
         *
         * @param {String} s
         * @return {String} the underscored string
         */
        underscore: function (s) {
            return s
                .replace(strColons, '/')
                .replace(strWords, '$1_$2')
                .replace(strLowUp, '$1_$2')
                .replace(strDash, '_')
                .toLowerCase();
        },
        // Micro-templating.
        /**
         * @function can.sub
         * @parent can.util
         *
         * Returns a string with {param} replaced values from data.
         *
         *     can.sub("foo {bar}",{bar: "far"})
         *     //-> "foo far"
         *
         * @param {String} s The string to replace
         * @param {Object} data The data to be used to look for properties.  If it's an array, multiple
         * objects can be used.
         * @param {Boolean} [remove] if a match is found, remove the property from the object
         * @return {String} The converted string or `null` if any data to render are `undefined` or `null`
         */
        sub: function (str, data, remove) {
            var obs = [];

            str = str || '';

            obs.push(str.replace(strReplacer, function (whole, inside) {

                // Convert inside to type.
                var ob = can.getObject(inside, data, remove === true ? false : undefined);

                if (ob === undefined || ob === null) {
                    obs = null;
                    return "";
                }

                // If a container, push into objs (which will return objects found).
                if (isContainer(ob) && obs) {
                    obs.push(ob);
                    return "";
                }

                return "" + ob;
            }));

            return obs === null ? obs : (obs.length <= 1 ? obs[0] : obs);
        },

        // These regex's are used throughout the rest of can, so let's make
        // them available.
        replacer: strReplacer,
        undHash: strUndHash
    });

    var isArray = can.isArray,
    // essentially returns an object that has all the must have comparisons ...
    // must haves, do not return true when provided undefined
        cleanSet = function (obj, compares) {
            var copy = can.extend({}, obj);
            for (var prop in copy) {
                var compare = compares[prop] === undefined ? compares["*"] : compares[prop];
                if (same(copy[prop], undefined, compare)) {
                    delete copy[prop]
                }
            }
            return copy;
        },
        propCount = function (obj) {
            var count = 0;
            for (var prop in obj) count++;
            return count;
        };

    /**
     * @hide
     * @page can.Object can.Object
     * @parent can.util
     *
     * @body
     * Object contains several helper methods that
     * help compare objects.
     *
     * ## same
     *
     * Returns true if two objects are similar.
     *
     *     can.Object.same({foo: "bar"} , {bar: "foo"}) //-> false
     *
     * ## subset
     *
     * Returns true if an object is a set of another set.
     *
     *     can.Object.subset({}, {foo: "bar"} ) //-> true
     *
     * ## subsets
     *
     * Returns the subsets of an object
     *
     *     can.Object.subsets({userId: 20},
     *                      [
     *                       {userId: 20, limit: 30},
     *                       {userId: 5},
     *                       {}
     *                      ])
     *              //->    [{userId: 20, limit: 30}]
     */
    can.Object = {};

    /**
     * @function can.Object.same
     * @parent can.util
     * @description Checks if two objects are the same.
     * @signature `can.Object.same(a, b, compares, aParent, bParent, deep)`
     * @param {Object} a An object to compare against `b`.
     * @param {Object} b An object to compare against `a`.
     * @param {Object} [compares] An object that specifies how to compare properties.
     * The keys of the `compares` object are names of properties in the objects to compare,
     * and the values are functions that compare those properties. You can also pass `'i'`
     * to compare values as case-insensitive strings, or `null` not to compare the properties
     * at all.
     * @return {{boolean}} Whether the two objects have the same properties and values.
     *
     * @body
     * This function does not work with objects that create circular references.
     *
     * ## Examples
     * @codestart
     * can.Object.same({name: "Justin"}, {name: "JUSTIN"}) //-> false
     *
     * // ignore the name property
     * can.Object.same({name: "Brian"}, {name: "JUSTIN"}, {name: null}) //-> true
     *
     * // ignore case
     * can.Object.same({name: "Justin"}, {name: "JUSTIN"}, {name: "i"}) //-> true
     *
     * // deep rule
     * can.Object.same({ person : { name: "Justin" } },
     *     { person : { name: "JUSTIN" } },
     *     { person : { name: "i"      } }) //-> true
     *
     * // supplied compare function
     * can.Object.same({age: "Thirty"},
     *     {age: 30},
     *     {age: function( a, b ){
 *     if( a == "Thirty" ) {
 *         a = 30
 *     }
 *     if( b == "Thirty" ) {
 *         b = 30
 *     }
 *     return a === b;
 * }})      //-> true
     * @codeend
     */
    var same = can.Object.same = function (a, b, compares, aParent, bParent, deep) {
        var aType = typeof a,
            aArray = isArray(a),
            comparesType = typeof compares,
            compare;

        if (comparesType == 'string' || compares === null) {
            compares = compareMethods[compares];
            comparesType = 'function'
        }
        if (comparesType == 'function') {
            return compares(a, b, aParent, bParent)
        }
        compares = compares || {};

        if (a instanceof Date) {
            return a === b;
        }
        if (deep === -1) {
            return aType === 'object' || a === b;
        }
        if (aType !== typeof  b || aArray !== isArray(b)) {
            return false;
        }
        if (a === b) {
            return true;
        }
        if (aArray) {
            if (a.length !== b.length) {
                return false;
            }
            for (var i = 0; i < a.length; i++) {
                compare = compares[i] === undefined ? compares["*"] : compares[i]
                if (!same(a[i], b[i], a, b, compare)) {
                    return false;
                }
            }
            return true;
        } else if (aType === "object" || aType === 'function') {
            var bCopy = can.extend({}, b);
            for (var prop in a) {
                compare = compares[prop] === undefined ? compares["*"] : compares[prop];
                if (!same(a[prop], b[prop], compare, a, b, deep === false ? -1 : undefined)) {
                    return false;
                }
                delete bCopy[prop];
            }
            // go through bCopy props ... if there is no compare .. return false
            for (prop in bCopy) {
                if (compares[prop] === undefined || !same(undefined, b[prop], compares[prop], a, b, deep === false ? -1 : undefined)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    };

    /**
     * @function can.Object.subsets
     * @parent can.util
     * @description Returns the sets in 'sets' that are a subset of checkSet
     * @codestart
     * can.Object.subsets({userId: 20},
     * [
     *    {userId: 20, limit: 30},
     *    {userId: 5},
     *    {}
     * ]) //-> [{userId: 20, limit: 30}]
     * @codeend
     * @signature `can.Object.subsets(checkSet, sets, compares)`
     * @param {Object} checkSet
     * @param {Object} sets
     * @param {Object} compares
     */
    can.Object.subsets = function (checkSet, sets, compares) {
        var len = sets.length,
            subsets = [],
            checkPropCount = propCount(checkSet),
            setLength;

        for (var i = 0; i < len; i++) {
            //check this subset
            var set = sets[i];
            if (can.Object.subset(checkSet, set, compares)) {
                subsets.push(set)
            }
        }
        return subsets;
    };
    /**
     * @function can.Object.subset
     * @parent can.util
     * @description Compares if subset is a subset of set. Returns true if an object is a set of another set
     * @codestart
     * can.Object.subset({}, {foo: "bar"} ) //-> true
     * @codeend
     * @signature `can.Object.subset(subset, set, compares)`
     * @param {Object} subset
     * @param {Object} set
     * @param {Object} compares
     * @returns {Boolean} Whether or not subset is a subset of set
     */
    can.Object.subset = function (subset, set, compares) {
        // go through set {type: 'folder'} and make sure every property
        // is in subset {type: 'folder', parentId :5}
        // then make sure that set has fewer properties
        // make sure we are only checking 'important' properties
        // in subset (ones that have to have a value)

        var setPropCount = 0,
            compares = compares || {};

        for (var prop in set) {

            if (!same(subset[prop], set[prop], compares[prop], subset, set)) {
                return false;
            }
        }
        return true;
    };


    var compareMethods = {
        "null": function () {
            return true;
        },
        i: function (a, b) {
            return ("" + a).toLowerCase() == ("" + b).toLowerCase()
        }
    };

    can.dev = {
        regexps: {
            colons: /::/,
            words: /([A-Z]+)([A-Z][a-z])/g,
            lowerUpper: /([a-z\d])([A-Z])/g,
            dash: /([a-z\d])([A-Z])/g
        },
        underscore: function (s) {
            var regs = this.regexps;
            return s.replace(regs.colons, '/').
                replace(regs.words, '$1_$2').
                replace(regs.lowerUpper, '$1_$2').
                replace(regs.dash, '_').toLowerCase();
        },

        logLevel: require.s.contexts["_"].config["logLevel"] || 0,
        /**
         * @function require.dev.warn
         * @parent require.dev
         *
         * @signature `require.dev.warn(out)`
         * @param {String} out the message
         *
         * @body
         * Adds a warning message to the console.
         *
         *     require.dev.warn("something evil");
         *
         */
        warn: function (out) {
            var ll = can.dev.logLevel;
            if (ll < 2) {
                Array.prototype.unshift.call(arguments, 'require.js WARN:');
                if (window.console && console.warn) {
                    if(typeof(console.warn) === 'function') {
                        this._logger("warn", Array.prototype.slice.call(arguments));
                    } else {
                        console.warn("dev.js WARNING: " + out);
                    }
                } else if (window.console && console.log) {
                    if(typeof(console.log) === 'function') {
                        this._logger("log", Array.prototype.slice.call(arguments));
                    } else {
                        console.log("dev.js WARNING: " + out);
                    }
                } else if (window.opera && window.opera.postError) {
                    opera.postError("require.js WARNING: " + out);
                }
            }

        },
        /**
         * @function require.dev.log
         * @parent require.dev
         *
         * @signature `require.dev.log(out)`
         * @param {String} out the message
         *
         * @body
         * Adds a message to the console.
         *
         *     require.dev.log("hi");
         *
         */
        log: function (out) {
            var ll = can.dev.logLevel;
            if (ll < 1) {
                if (window.console && console.log) {
                    if(typeof(console.log) === 'function') {
                        Array.prototype.unshift.call(arguments, 'dev.js INFO:');
                        this._logger("log", Array.prototype.slice.call(arguments));
                    } else {
                        console.log("dev.js INFO: " + out);
                    }
                } else if (window.opera && window.opera.postError) {
                    opera.postError("dev.js INFO: " + out);
                }
            }
        },
        _logger: function (type, arr) {
            if (console.log.apply) {
                console[type].apply(console, arr)
            } else {
                console[type](arr)
            }
        }
    };


    can.VERSION = '@EDGE';
    return can;
});
