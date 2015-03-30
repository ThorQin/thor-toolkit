/// <reference path="jquery.d.ts" />
// Embedded JSON2
var JSON;
if (!JSON) {
    JSON = {};
}
(function () {
    "use strict";
    function f(n) {
        return n < 10 ? '0' + n : n;
    }
    if (typeof Date.prototype.toJSON !== 'function') {
        Date.prototype.toJSON = function (key) {
            return isFinite(this.valueOf()) ? this.getUTCFullYear() + '-' + f(this.getUTCMonth() + 1) + '-' + f(this.getUTCDate()) + 'T' + f(this.getUTCHours()) + ':' + f(this.getUTCMinutes()) + ':' + f(this.getUTCSeconds()) + 'Z' : null;
        };
        String.prototype.toJSON = Number.prototype.toJSON = Boolean.prototype.toJSON = function (key) {
            return this.valueOf();
        };
    }
    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g, gap, indent, meta = {
        '\b': '\\b',
        '\t': '\\t',
        '\n': '\\n',
        '\f': '\\f',
        '\r': '\\r',
        '"': '\\"',
        '\\': '\\\\'
    }, rep;
    function quote(string) {
        escapable.lastIndex = 0;
        return escapable.test(string) ? '"' + string.replace(escapable, function (a) {
            var c = meta[a];
            return typeof c === 'string' ? c : '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
        }) + '"' : '"' + string + '"';
    }
    function str(key, holder) {
        var i, k, v, length, mind = gap, partial, value = holder[key];
        if (value && typeof value === 'object' && typeof value.toJSON === 'function') {
            value = value.toJSON(key);
        }
        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }
        switch (typeof value) {
            case 'string':
                return quote(value);
            case 'number':
                return isFinite(value) ? String(value) : 'null';
            case 'boolean':
            case 'null':
                return String(value);
            case 'object':
                if (!value) {
                    return 'null';
                }
                gap += indent;
                partial = [];
                if (Object.prototype.toString.apply(value) === '[object Array]') {
                    length = value.length;
                    for (i = 0; i < length; i += 1) {
                        partial[i] = str(i, value) || 'null';
                    }
                    v = partial.length === 0 ? '[]' : gap ? '[\n' + gap + partial.join(',\n' + gap) + '\n' + mind + ']' : '[' + partial.join(',') + ']';
                    gap = mind;
                    return v;
                }
                if (rep && typeof rep === 'object') {
                    length = rep.length;
                    for (i = 0; i < length; i += 1) {
                        if (typeof rep[i] === 'string') {
                            k = rep[i];
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                }
                else {
                    for (k in value) {
                        if (Object.prototype.hasOwnProperty.call(value, k)) {
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                }
                v = partial.length === 0 ? '{}' : gap ? '{\n' + gap + partial.join(',\n' + gap) + '\n' + mind + '}' : '{' + partial.join(',') + '}';
                gap = mind;
                return v;
        }
    }
    if (typeof JSON.stringify !== 'function') {
        JSON.stringify = function (value, replacer, space) {
            var i;
            gap = '';
            indent = '';
            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    indent += ' ';
                }
            }
            else if (typeof space === 'string') {
                indent = space;
            }
            rep = replacer;
            if (replacer && typeof replacer !== 'function' && (typeof replacer !== 'object' || typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }
            return str('', { '': value });
        };
    }
    if (typeof JSON.parse !== 'function') {
        JSON.parse = function (text, reviver) {
            var j;
            function walk(holder, key) {
                var k, v, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.prototype.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            }
                            else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }
            text = String(text);
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }
            if (/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {
                j = eval('(' + text + ')');
                return typeof reviver === 'function' ? walk({ '': j }, '') : j;
            }
            throw new SyntaxError('JSON.parse');
        };
    }
}());
// End of JSON2
if (typeof Array.prototype.indexOf !== "function") {
    Array.prototype.indexOf = function (searchElement, fromIndex) {
        var from = (typeof fromIndex === "number" ? fromIndex : 0);
        for (var i = from; i < this.length; i++) {
            if (this[i] === searchElement)
                return i;
        }
        return -1;
    };
}
var tui;
(function (tui) {
    tui.KEY_BACK = 8;
    tui.KEY_TAB = 9;
    tui.KEY_ENTER = 13;
    tui.KEY_SHIFT = 16;
    tui.KEY_CTRL = 17;
    tui.KEY_ALT = 18;
    tui.KEY_PAUSE = 19;
    tui.KEY_CAPS = 20;
    tui.KEY_ESC = 27;
    tui.KEY_SPACE = 32;
    tui.KEY_PRIOR = 33;
    tui.KEY_NEXT = 34;
    tui.KEY_END = 35;
    tui.KEY_HOME = 36;
    tui.KEY_LEFT = 37;
    tui.KEY_UP = 38;
    tui.KEY_RIGHT = 39;
    tui.KEY_DOWN = 40;
    tui.KEY_PRINT = 44;
    tui.KEY_INSERT = 45;
    tui.KEY_DELETE = 46;
    tui.CONTROL_KEYS = {
        9: "Tab",
        13: "Enter",
        16: "Shift",
        17: "Ctrl",
        18: "Alt",
        19: "Pause",
        20: "Caps",
        27: "Escape",
        33: "Prior",
        34: "Next",
        35: "End",
        36: "Home",
        37: "Left",
        38: "Up",
        39: "Right",
        40: "Down",
        45: "Insert",
        112: "F1",
        113: "F2",
        114: "F3",
        115: "F4",
        116: "F5",
        117: "F6",
        118: "F7",
        119: "F8",
        120: "F9",
        121: "F10",
        122: "F11",
        123: "F12"
    };
    tui.undef = (function (undefined) {
        return typeof undefined;
    })();
    tui.undefVal = (function (undefined) {
        return undefined;
    })();
    tui.lang = (function () {
        return (navigator.language || navigator.browserLanguage || navigator.userLanguage).toLowerCase();
    })();
    var _translate = {};
    function registerTranslator(lang, translator) {
        if (typeof translator === "function")
            _translate[lang] = translator;
        else if (typeof translator === "object" && translator !== null) {
            _translate[lang] = function (str) {
                return translator[str] || str;
            };
        }
    }
    tui.registerTranslator = registerTranslator;
    /**
     * Multi-language support, translate source text to specified language(default use tui.lang setting)
     * @param str {string} source text
     * @param lang {string} if specified then use this parameter as objective language otherwise use tui.lang as objective language
     */
    function str(str, lang) {
        if (!lang) {
            if (!tui.lang)
                return str;
            else
                lang = tui.lang;
        }
        var func = _translate[lang];
        if (typeof func === "function") {
            return func(str);
        }
        else
            return str;
    }
    tui.str = str;
    tui.uuid = (function () {
        var id = 0;
        return function () {
            var uid = 'tuid' + id++;
            return uid;
        };
    })();
    /**
     * Base object, all other control extended from this base class.
     */
    var EventObject = (function () {
        function EventObject() {
            this._events = {};
        }
        EventObject.prototype.bind = function (eventName, handler, priority) {
            if (!eventName)
                return;
            if (!this._events[eventName]) {
                this._events[eventName] = [];
            }
            var handlers = this._events[eventName];
            for (var i = 0; i < handlers.length; i++) {
                if (handlers[i] === handler)
                    return;
            }
            if (priority)
                handlers.push(handler);
            else
                handlers.splice(0, 0, handler);
        };
        EventObject.prototype.unbind = function (eventName, handler) {
            if (!eventName)
                return;
            var handlers = this._events[eventName];
            if (handler) {
                for (var i = 0; i < handlers.length; i++) {
                    if (handler === handlers[i]) {
                        handlers.splice(i, 1);
                        return;
                    }
                }
            }
            else {
                handlers.length = 0;
            }
        };
        /**
         * Register event handler.
         * @param {string} eventName
         * @param {callback} callback Which handler to be registered
         * @param {boolean} priority If true then handler will be triggered firstly
         */
        EventObject.prototype.on = function (eventName, callback, priority) {
            if (priority === void 0) { priority = false; }
            var envs = eventName.split(/\s+/);
            for (var i = 0; i < envs.length; i++) {
                var v = envs[i];
                this.bind(v, callback, priority);
            }
        };
        /**
         * Register event handler.
         * @param eventName
         * @param callback Which handler to be registered but event only can be trigered once
         * @param priority If true then handler will be triggered firstly
         */
        EventObject.prototype.once = function (eventName, callback, priority) {
            if (priority === void 0) { priority = false; }
            callback.isOnce = true;
            this.on(eventName, callback, priority);
        };
        /**
         * Unregister event handler.
         * @param eventName
         * @param callback Which handler to be unregistered if don't specified then unregister all handler
         */
        EventObject.prototype.off = function (eventName, callback) {
            var envs = eventName.split(/\s+/);
            for (var i = 0; i < envs.length; i++) {
                var v = envs[i];
                this.unbind(v, callback);
            }
        };
        /**
         * Fire event. If some handler process return false then cancel the event channe and return false either
         * @param {string} eventName
         * @param {any[]} param
         */
        EventObject.prototype.fire = function (eventName, data) {
            // srcElement: HTMLElement, e?: JQueryEventObject, ...param: any[]
            var array = this._events[eventName];
            if (!array) {
                return;
            }
            var _data = null;
            if (data) {
                _data = data;
                _data["name"] = eventName;
            }
            else
                _data = { "name": eventName };
            var removeArray = [];
            for (var i = 0; i < array.length; i++) {
                var handler = array[i];
                if (handler.isOnce)
                    removeArray.push(handler);
                var val = handler.call(this, _data);
                if (typeof val === "boolean" && !val)
                    return false;
            }
            for (var i = 0; i < removeArray.length; i++) {
                this.off(eventName, removeArray[i]);
            }
        };
        return EventObject;
    })();
    tui.EventObject = EventObject;
    var _eventObject = new EventObject();
    function on(eventName, callback, priority) {
        if (priority === void 0) { priority = false; }
        _eventObject.on(eventName, callback, priority);
    }
    tui.on = on;
    function once(eventName, callback, priority) {
        if (priority === void 0) { priority = false; }
        _eventObject.once(eventName, callback, priority);
    }
    tui.once = once;
    function off(eventName, callback) {
        _eventObject.off(eventName, callback);
    }
    tui.off = off;
    function fire(eventName, data) {
        return EventObject.prototype.fire.call(_eventObject, eventName, data);
    }
    tui.fire = fire;
    function parseBoolean(string) {
        if (typeof string === tui.undef)
            return false;
        switch (String(string).toLowerCase()) {
            case "true":
            case "1":
            case "yes":
            case "y":
                return true;
            default:
                return false;
        }
    }
    tui.parseBoolean = parseBoolean;
    function toElement(html, withParent) {
        if (withParent === void 0) { withParent = false; }
        var div = document.createElement('div');
        div.innerHTML = $.trim(html);
        if (withParent)
            return div;
        var el = div.firstChild;
        return div.removeChild(el);
    }
    tui.toElement = toElement;
    function removeNode(node) {
        node.parentNode && node.parentNode.removeChild(node);
    }
    tui.removeNode = removeNode;
    /**
     * Get or set a HTMLElement's text content, return Element's text content.
     * @param elem {HTMLElement or ID of the element} Objective element
     * @param text {string or other object that can be translated to string}
     */
    function elementText(elem, text) {
        if (typeof elem === "string")
            elem = document.getElementById(elem);
        if (elem) {
            if (typeof text !== "undefined") {
                elem.innerHTML = "";
                elem.appendChild(document.createTextNode(text));
                return text;
            }
            if (typeof elem.textContent !== "undefined")
                return elem.textContent;
            var buf = "";
            for (var i = 0; i < elem.childNodes.length; i++) {
                var c = elem.childNodes[i];
                if (c.nodeName.toLowerCase() === "#text") {
                    buf += c.nodeValue;
                }
                else
                    buf += elementText(c);
            }
            return buf;
        }
        else
            return null;
    }
    tui.elementText = elementText;
    function relativePosition(srcObj, offsetParent) {
        if (!offsetParent.nodeName && !offsetParent.tagName)
            throw new Error("Offset parent must be an html element.");
        var result = { x: srcObj.offsetLeft, y: srcObj.offsetTop };
        var obj = srcObj.offsetParent;
        while (obj) {
            if (obj === offsetParent)
                return result;
            result.x += ((obj.offsetLeft || 0) + (obj.clientLeft || 0) - (obj.scrollLeft || 0));
            result.y += ((obj.offsetTop || 0) + (obj.clientTop || 0) - (obj.scrollTop || 0));
            if (obj.nodeName.toLowerCase() === "body" && obj.offsetParent === null)
                obj = obj.parentElement;
            else
                obj = obj.offsetParent;
        }
        return null;
    }
    tui.relativePosition = relativePosition;
    ;
    function fixedPosition(target) {
        var $target = $(target);
        var offset = $target.offset();
        var $doc = $(document);
        return {
            x: offset.left - $doc.scrollLeft(),
            y: offset.top - $doc.scrollTop()
        };
    }
    tui.fixedPosition = fixedPosition;
    function debugElementPosition(target) {
        $(target).mousedown(function (e) {
            var pos = tui.fixedPosition(this);
            var anchor = document.createElement("span");
            anchor.style.backgroundColor = "#ccc";
            anchor.style.opacity = "0.5";
            anchor.style.display = "inline-block";
            anchor.style.position = "fixed";
            anchor.style.left = pos.x + "px";
            anchor.style.top = pos.y + "px";
            anchor.style.width = this.offsetWidth + "px";
            anchor.style.height = this.offsetHeight + "px";
            document.body.appendChild(anchor);
            $(anchor).mouseup(function (e) {
                document.body.removeChild(anchor);
            });
            // console.log(tui.format("x: {0}, y: {1}", pos.x, pos.y));
        });
    }
    tui.debugElementPosition = debugElementPosition;
    /**
     * Obtain hosted document's window size (exclude scrollbars if have)
     * NOTE: this function will spend much CPU time to run,
     * so you SHOULD NOT try to call this function repeatedly.
     */
    function windowSize() {
        var div = document.createElement("div");
        div.style.display = "block";
        div.style.position = "fixed";
        div.style.left = "0";
        div.style.top = "0";
        div.style.right = "0";
        div.style.bottom = "0";
        div.style.visibility = "hidden";
        var parent = document.body || document.documentElement;
        parent.appendChild(div);
        var size = { width: div.offsetWidth, height: div.offsetHeight };
        parent.removeChild(div);
        return size;
    }
    tui.windowSize = windowSize;
    ;
    /**
     * Get top window's body element
     */
    function getTopBody() {
        return top.document.body || top.document.getElementsByTagName("BODY")[0];
    }
    tui.getTopBody = getTopBody;
    /**
     * Get element's owner window
     */
    function getWindow(elem) {
        return elem.ownerDocument.defaultView || elem.ownerDocument.parentWindow;
    }
    tui.getWindow = getWindow;
    function cloneInternal(obj, excludeProperties) {
        if (obj === null)
            return null;
        else if (typeof obj === tui.undef)
            return undefined;
        else if (obj instanceof Array) {
            var newArray = [];
            for (var idx in obj) {
                if (obj.hasOwnProperty(idx) && excludeProperties.indexOf(idx) < 0) {
                    newArray.push(cloneInternal(obj[idx], excludeProperties));
                }
            }
            return newArray;
        }
        else if (typeof obj === "number")
            return obj;
        else if (typeof obj === "string")
            return obj;
        else if (typeof obj === "boolean")
            return obj;
        else if (typeof obj === "function")
            return obj;
        else {
            var newObj = {};
            for (var idx in obj) {
                if (obj.hasOwnProperty(idx) && excludeProperties.indexOf(idx) < 0) {
                    newObj[idx] = cloneInternal(obj[idx], excludeProperties);
                }
            }
            return newObj;
        }
    }
    /**
     * Deeply copy an object to an other object, but only contain properties without methods
     */
    function clone(obj, excludeProperties) {
        if (typeof excludeProperties === "string" && $.trim(excludeProperties).length > 0) {
            return cloneInternal(obj, [excludeProperties]);
        }
        else if (excludeProperties instanceof Array) {
            return cloneInternal(obj, excludeProperties);
        }
        else
            return JSON.parse(JSON.stringify(obj));
    }
    tui.clone = clone;
    /**
     * Test whether the button code is indecated that the event is triggered by a left mouse button.
     */
    function isLButton(e) {
        var button = (typeof e.which !== "undefined") ? e.which : e.button;
        if (button == 1) {
            return true;
        }
        else
            return false;
    }
    tui.isLButton = isLButton;
    /**
     * Prevent user press backspace key to go back to previous page
     */
    function banBackspace() {
        function ban(e) {
            var ev = e || window.event;
            var obj = ev.target || ev.srcElement;
            var t = obj.type || obj.getAttribute('type');
            var vReadOnly = obj.readOnly;
            var vDisabled = obj.disabled;
            vReadOnly = (typeof vReadOnly === tui.undef) ? false : vReadOnly;
            vDisabled = (typeof vDisabled === tui.undef) ? true : vDisabled;
            var flag1 = ev.keyCode === 8 && (t === "password" || t === "text" || t === "textarea") && (vReadOnly || vDisabled);
            var flag2 = ev.keyCode === 8 && t !== "password" && t !== "text" && t !== "textarea";
            if (flag2 || flag1)
                return false;
        }
        $(document).bind("keypress", ban);
        $(document).bind("keydown", ban);
    }
    tui.banBackspace = banBackspace;
    function cancelDefault(event) {
        if (event.preventDefault) {
            event.preventDefault();
        }
        else {
            event.returnValue = false;
        }
        return false;
    }
    tui.cancelDefault = cancelDefault;
    function cancelBubble(event) {
        if (event && event.stopPropagation)
            event.stopPropagation();
        else
            window.event.cancelBubble = true;
        return false;
    }
    tui.cancelBubble = cancelBubble;
    /**
     * Detect whether the given parent element is the real ancestry element
     * @param elem
     * @param parent
     */
    function isAncestry(elem, parent) {
        while (elem) {
            if (elem === parent)
                return true;
            else
                elem = elem.parentNode;
        }
        return false;
    }
    tui.isAncestry = isAncestry;
    /**
     * Detect whether the given child element is the real posterity element
     * @param elem
     * @param child
     */
    function isPosterity(elem, child) {
        return isAncestry(child, elem);
    }
    tui.isPosterity = isPosterity;
    function isFireInside(elem, event) {
        var target = event.target || event.srcElement;
        return isPosterity(elem, target);
    }
    tui.isFireInside = isFireInside;
    /**
     * Detect whether the element is inside the document
     * @param {type} elem
     */
    function isInDoc(elem) {
        var obj = elem;
        while (obj) {
            if (obj.nodeName.toUpperCase() === "HTML")
                return true;
            obj = obj.parentElement;
        }
        return false;
    }
    tui.isInDoc = isInDoc;
    /**
     * Format a string use a set of parameters
     */
    function format(token) {
        var params = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            params[_i - 1] = arguments[_i];
        }
        var formatrg = /\{(\d+)\}/g;
        token && (typeof token === "string") && params.length && (token = token.replace(formatrg, function (str, i) {
            return params[i] === null ? "" : params[i];
        }));
        return token ? token : "";
    }
    tui.format = format;
    /**
     * Format a number that padding it with '0'
     */
    function paddingNumber(v, min, max, alignLeft) {
        if (alignLeft === void 0) { alignLeft = false; }
        var result = Math.abs(v) + "";
        while (result.length < min) {
            result = "0" + result;
        }
        if (typeof max === "number" && result.length > max) {
            if (alignLeft)
                result = result.substr(0, max);
            else
                result = result.substr(result.length - max, max);
        }
        if (v < 0)
            result = "-" + result;
        return result;
    }
    tui.paddingNumber = paddingNumber;
    /**
     * Get the parameter of the URL query string.
     * @param {String} url
     * @param {String} key Parameter name
     */
    function getParam(url, key) {
        key = key.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regex = new RegExp("[\\?&]" + key + "=([^&#]*)"), results = regex.exec(url);
        return results === null ? null : decodeURIComponent(results[1].replace(/\+/g, " "));
    }
    tui.getParam = getParam;
    /**
     * Get the anchor of the URL query string.
     * @param {String} url
     */
    function getAnchor(url) {
        var anchor = location.href.match("(#.+)(?:\\?.*)?");
        if (anchor)
            anchor = anchor[1];
        return anchor;
    }
    tui.getAnchor = getAnchor;
    var BackupedScrollPosition = (function () {
        function BackupedScrollPosition(target) {
            this.backupInfo = [];
            var obj = target;
            while (obj && obj !== document.body) {
                obj = obj.parentElement;
                if (obj)
                    this.backupInfo.push({ obj: obj, left: obj.scrollLeft, top: obj.scrollTop });
            }
        }
        BackupedScrollPosition.prototype.restore = function () {
            for (var i = 0; i < this.backupInfo.length; i++) {
                var item = this.backupInfo[i];
                item.obj.scrollLeft = item.left;
                item.obj.scrollTop = item.top;
            }
        };
        return BackupedScrollPosition;
    })();
    tui.BackupedScrollPosition = BackupedScrollPosition;
    function backupScrollPosition(target) {
        return new BackupedScrollPosition(target);
    }
    tui.backupScrollPosition = backupScrollPosition;
    function focusWithoutScroll(target) {
        setTimeout(function () {
            if (tui.ieVer > 0) {
                //if (tui.ieVer > 8)
                //	target.setActive();
                //else {
                //	if (target !== document.activeElement)
                target.setActive();
            }
            else if (tui.ffVer > 0)
                target.focus();
            else {
                var backup = tui.backupScrollPosition(target);
                target.focus();
                backup.restore();
            }
        }, 0);
    }
    tui.focusWithoutScroll = focusWithoutScroll;
    function scrollToElement(elem) {
        var obj = elem;
        while (obj) {
            var parent = obj.offsetParent;
            $(parent).animate({ scrollTop: $(obj).offset().top }, 200);
            obj = parent;
        }
    }
    tui.scrollToElement = scrollToElement;
    /**
     * Get IE version
     * @return {Number}
     */
    tui.ieVer = (function () {
        var rv = -1; // Return value assumes failure.
        if (navigator.appName === "Microsoft Internet Explorer" || navigator.appName === "Netscape") {
            var ua = navigator.userAgent;
            var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
            if (re.exec(ua) !== null)
                rv = parseFloat(RegExp.$1);
        }
        if (rv === -1 && navigator.appName === "Netscape") {
            var ua = navigator.userAgent;
            var re = new RegExp("Trident/([0-9]{1,}[\.0-9]{0,})");
            if (re.exec(ua) !== null)
                rv = parseFloat(RegExp.$1);
            if (rv >= 7.0)
                rv = 11.0;
        }
        return rv;
    })();
    /**
     * Get Firefox version
     * @return {Number}
     */
    tui.ffVer = (function () {
        var rv = -1; // Return value assumes failure.
        if (navigator.appName === "Netscape") {
            var ua = navigator.userAgent;
            var re = new RegExp("Firefox/([0-9]{1,}[\.0-9]{0,})");
            if (re.exec(ua) !== null)
                rv = parseFloat(RegExp.$1);
        }
        return rv;
    })();
    /**
     * Set cookie value
     * @param name
     * @param value
     * @param days valid days
     */
    function saveCookie(name, value, expires, path, domain, secure) {
        if (secure === void 0) { secure = false; }
        // set time, it's in milliseconds
        var today = new Date();
        today.setTime(today.getTime());
        /*
        if the expires variable is set, make the correct
        expires time, the current script below will set
        it for x number of days, to make it for hours,
        delete * 24, for minutes, delete * 60 * 24
        */
        if (expires) {
            expires = expires * 1000 * 60 * 60 * 24;
        }
        var expires_date = new Date(today.getTime() + (expires));
        document.cookie = name + "=" + encodeURIComponent(JSON.stringify(value)) + ((expires) ? ";expires=" + expires_date.toUTCString() : "") + ((path) ? ";path=" + path : "") + ((domain) ? ";domain=" + domain : "") + ((secure) ? ";secure" : "");
    }
    tui.saveCookie = saveCookie;
    /**
     * Get cookie value
     * @param name
     */
    function loadCookie(name) {
        var arr = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));
        if (arr !== null)
            return JSON.parse(decodeURIComponent(arr[2]));
        else
            return null;
    }
    tui.loadCookie = loadCookie;
    /**
     * Delete cookie
     * @param name
     */
    function deleteCookie(name, path, domain) {
        if (loadCookie(name))
            document.cookie = name + "=" + ((path) ? ";path=" + path : "") + ((domain) ? ";domain=" + domain : "") + ";expires=Thu, 01-Jan-1970 00:00:01 GMT";
    }
    tui.deleteCookie = deleteCookie;
    /**
     * Save key value into local storage, if local storage doesn't usable then use local cookie instead.
     * @param {String} key
     * @param {String} value
     * @param {Boolean} sessionOnly If true data only be keeped in this session
     */
    function saveData(key, value, sessionOnly) {
        if (sessionOnly === void 0) { sessionOnly = false; }
        try {
            var storage = (sessionOnly === true ? window.sessionStorage : window.localStorage);
            if (storage) {
                storage.setItem(key, JSON.stringify(value));
            }
            else
                saveCookie(key, value, 365);
        }
        catch (e) {
        }
    }
    tui.saveData = saveData;
    /**
     * Load value from local storage, if local storage doesn't usable then use local cookie instead.
     * @param {String} key
     * @param {Boolean} sessionOnly If true data only be keeped in this session
     */
    function loadData(key, sessionOnly) {
        if (sessionOnly === void 0) { sessionOnly = false; }
        try {
            var storage = (sessionOnly === true ? window.sessionStorage : window.localStorage);
            if (storage)
                return JSON.parse(storage.getItem(key));
            else
                return loadCookie(key);
        }
        catch (e) {
            return null;
        }
    }
    tui.loadData = loadData;
    /**
     * Remove value from local storage, if local storage doesn't usable then use local cookie instead.
     * @param key
     * @param {Boolean} sessionOnly If true data only be keeped in this session
     */
    function deleteData(key, sessionOnly) {
        if (sessionOnly === void 0) { sessionOnly = false; }
        try {
            var storage = (sessionOnly === true ? window.sessionStorage : window.localStorage);
            if (storage)
                storage.removeItem(key);
            else
                deleteCookie(key);
        }
        catch (e) {
        }
    }
    tui.deleteData = deleteData;
    function windowScrollElement() {
        if (tui.ieVer > 0 || tui.ffVer > 0) {
            return window.document.documentElement;
        }
        else {
            return window.document.body;
        }
    }
    tui.windowScrollElement = windowScrollElement;
    /**
     * Load URL via AJAX request, It's a simplified version of jQuery.ajax method.
     *
     */
    function loadURL(url, completeCallback, async, method, data) {
        if (async === void 0) { async = true; }
        method = method ? method : "GET";
        $.ajax({
            "type": method,
            "url": url,
            "async": async,
            "contentType": "application/json",
            "data": (method === "GET" ? data : JSON.stringify(data)),
            "complete": function (jqXHR, status) {
                if (typeof completeCallback === "function" && completeCallback(status, jqXHR) === false) {
                    return;
                }
            },
            "processData": (method === "GET" ? true : false)
        });
    }
    tui.loadURL = loadURL;
    var _accMap = {};
    var _keyMap = {
        8: "Back",
        9: "Tab",
        13: "Enter",
        19: "Pause",
        20: "Caps",
        27: "Escape",
        32: "Space",
        33: "Prior",
        34: "Next",
        35: "End",
        36: "Home",
        37: "Left",
        38: "Up",
        39: "Right",
        40: "Down",
        45: "Insert",
        46: "Delete",
        48: "0",
        49: "1",
        50: "2",
        51: "3",
        52: "4",
        53: "5",
        54: "6",
        55: "7",
        56: "8",
        57: "9",
        65: "A",
        66: "B",
        67: "C",
        68: "D",
        69: "E",
        70: "F",
        71: "G",
        72: "H",
        73: "I",
        74: "J",
        75: "K",
        76: "L",
        77: "M",
        78: "N",
        79: "O",
        80: "P",
        81: "Q",
        82: "R",
        83: "S",
        84: "T",
        85: "U",
        86: "V",
        87: "W",
        88: "X",
        89: "Y",
        90: "Z",
        112: "F1",
        113: "F2",
        114: "F3",
        115: "F4",
        116: "F5",
        117: "F6",
        118: "F7",
        119: "F8",
        120: "F9",
        121: "F10",
        122: "F11",
        123: "F12",
        186: ";",
        187: "=",
        188: ",",
        189: "-",
        190: ".",
        191: "/",
        192: "~",
        219: "[",
        220: "\\",
        221: "]",
        222: "'"
    };
    function accelerate(e) {
        var k = _keyMap[e.keyCode];
        if (!k) {
            return;
        }
        k = k.toUpperCase();
        var key = (e.ctrlKey ? "CTRL" : "");
        if (e.altKey) {
            if (key.length > 0)
                key += "+";
            key += "ALT";
        }
        if (e.shiftKey) {
            if (key.length > 0)
                key += "+";
            key += "SHIFT";
        }
        if (e.metaKey) {
            if (key.length > 0)
                key += "+";
            key += "META";
        }
        if (key.length > 0)
            key += "+";
        key += k;
        var l = _accMap[key];
        if (l) {
            for (var i = 0; i < l.length; i++) {
                if (tui.fire(l[i], { name: l[i], event: e }) === false)
                    return;
            }
        }
    }
    function addAccelerate(key, actionId) {
        key = key.toUpperCase();
        var l = null;
        if (_accMap.hasOwnProperty(key))
            l = _accMap[key];
        else {
            l = [];
            _accMap[key] = l;
        }
        if (l.indexOf(actionId) < 0)
            l.push(actionId);
    }
    tui.addAccelerate = addAccelerate;
    function deleteAccelerate(key, actionId) {
        key = key.toUpperCase();
        if (!_accMap.hasOwnProperty(key))
            return;
        var l = _accMap[key];
        var pos = l.indexOf(actionId);
        if (pos >= 0) {
            l.splice(pos, 1);
            if (l.length <= 0)
                delete _accMap[key];
        }
    }
    tui.deleteAccelerate = deleteAccelerate;
    $(document).keydown(accelerate);
})(tui || (tui = {}));
/// <reference path="tui.core.ts" />
var tui;
(function (tui) {
    var shortWeeks = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    var weeks = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
    var shortMonths = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    var months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    /**
     * Get today
     */
    function today() {
        return new Date();
    }
    tui.today = today;
    /**
     * Input seconds and get a time description
     * @param seconds Tims distance of seconds
     * @param lang Display language
     */
    function timespan(seconds, lang) {
        var desc = ["day", "hour", "minute", "second"];
        var val = [];
        var beg = "", end = "";
        var d = Math.floor(seconds / 86400);
        val.push(d);
        seconds = seconds % 86400;
        var h = Math.floor(seconds / 3600);
        val.push(h);
        seconds = seconds % 3600;
        var m = Math.floor(seconds / 60);
        val.push(m);
        val.push(seconds % 60);
        var i = 0, j = 3;
        while (i < 4) {
            if (val[i] > 0) {
                beg.length && (beg += " ");
                beg += val[i] + " " + tui.str(val[i] > 1 ? desc[i] + "s" : desc[i], lang);
                break;
            }
            i++;
        }
        while (i < j) {
            if (val[j] > 0) {
                end.length && (end += " ");
                end += val[j] + " " + tui.str(val[j] > 1 ? desc[j] + "s" : desc[j], lang);
                break;
            }
            j--;
        }
        i++;
        while (i < j) {
            beg.length && (beg += " ");
            beg += val[i] + " " + tui.str(val[i] > 1 ? desc[i] + "s" : desc[i], lang);
            i++;
        }
        return beg + (beg.length ? " " : "") + end;
    }
    tui.timespan = timespan;
    /**
     * Get the distance of dt2 compare to dt1 (dt2 - dt1) return in specified unit (d: day, h: hours, m: minutes, s: seconds, ms: milliseconds)
     * @param dt1
     * @param dt2
     * @param unit "d", "h", "m", "s" or "ms"
     */
    function dateDiff(dt1, dt2, unit) {
        if (unit === void 0) { unit = "d"; }
        var d1 = dt1.getTime();
        var d2 = dt2.getTime();
        var diff = d2 - d1;
        var symbol = diff < 0 ? -1 : 1;
        diff = Math.abs(diff);
        unit = unit.toLocaleLowerCase();
        if (unit === "d") {
            return Math.floor(diff / 86400000) * symbol;
        }
        else if (unit === "h") {
            return Math.floor(diff / 3600000) * symbol;
        }
        else if (unit === "m") {
            return Math.floor(diff / 60000) * symbol;
        }
        else if (unit === "s") {
            return Math.floor(diff / 1000) * symbol;
        }
        else if (unit === "ms") {
            return diff * symbol;
        }
        else
            return NaN;
    }
    tui.dateDiff = dateDiff;
    /**
     * Get new date of dt add specified unit of values.
     * @param dt The day of the target
     * @param val Increased value
     * @param unit "d", "h", "m", "s" or "ms"
     */
    function dateAdd(dt, val, unit) {
        if (unit === void 0) { unit = "d"; }
        var d = dt.getTime();
        if (unit === "d") {
            return new Date(d + val * 86400000);
        }
        else if (unit === "h") {
            return new Date(d + val * 3600000);
        }
        else if (unit === "m") {
            return new Date(d + val * 60000);
        }
        else if (unit === "s") {
            return new Date(d + val * 1000);
        }
        else if (unit === "ms") {
            return new Date(d + val);
        }
        else
            return null;
    }
    tui.dateAdd = dateAdd;
    /**
     * Get day in year
     * @param dt The day of the target
     */
    function dayOfYear(dt) {
        var y = dt.getFullYear();
        var d1 = new Date(y, 0, 1);
        return dateDiff(d1, dt, "d");
    }
    tui.dayOfYear = dayOfYear;
    /**
     * Get total days of month
     * @param dt The day of the target
     */
    function totalDaysOfMonth(dt) {
        var y = dt.getFullYear();
        var m = dt.getMonth();
        var d1 = new Date(y, m, 1);
        if (m === 11) {
            y++;
            m = 0;
        }
        else {
            m++;
        }
        var d2 = new Date(y, m, 1);
        return dateDiff(d1, d2, "d");
    }
    tui.totalDaysOfMonth = totalDaysOfMonth;
    function parseDateInternal(dtStr, format) {
        if (!dtStr || !format)
            return null;
        var mapping = {};
        var gcount = 0;
        var isUTC = false;
        var values = {};
        function matchEnum(v, key, enumArray) {
            var m = dtStr.match(new RegExp("^" + enumArray.join("|"), "i"));
            if (m === null)
                return false;
            v = m[0].toLowerCase();
            v = v.substr(0, 1).toUpperCase() + v.substr(1);
            values[key] = enumArray.indexOf(v);
            dtStr = dtStr.substr(v.length);
            return true;
        }
        function matchNumber(v, key, min, max) {
            var len = v.length;
            var m = dtStr.match("^[0-9]{1," + len + "}");
            if (m === null)
                return false;
            v = m[0];
            var num = parseInt(v);
            if (num < min || num > max)
                return false;
            key && (values[key] = num);
            dtStr = dtStr.substr(v.length);
            return true;
        }
        var rule = {
            "y+": function (v) {
                if (!matchNumber(v, "year"))
                    return false;
                if (values["year"] < 100)
                    values["year"] += 1900;
                return true;
            },
            "M+": function (v) {
                var len = v.length;
                if (len < 3) {
                    if (!matchNumber(v, "month", 1, 12))
                        return false;
                    values["month"] -= 1;
                    return true;
                }
                else if (len === 3) {
                    return matchEnum(v, "month", shortMonths);
                }
                else {
                    return matchEnum(v, "month", months);
                }
            },
            "d+": function (v) {
                return matchNumber(v, "date", 1, 31);
            },
            "D+": matchNumber,
            "h+": function (v) {
                return matchNumber(v, "12hour", 1, 12);
            },
            "H+": function (v) {
                return matchNumber(v, "hour", 0, 24);
            },
            "m+": function (v) {
                return matchNumber(v, "minute", 0, 59);
            },
            "s+": function (v) {
                return matchNumber(v, "second", 0, 59);
            },
            "[qQ]+": function (v) {
                return matchNumber(v, null, 1, 4);
            },
            "S+": function (v) {
                return matchNumber(v, "millisecond", 0, 999);
            },
            "E+": function (v) {
                var len = v.length;
                if (len < 3) {
                    if (!matchNumber(v, null, 0, 6))
                        return false;
                    return true;
                }
                else if (len === 3) {
                    return matchEnum(v, null, shortWeeks);
                }
                else {
                    return matchEnum(v, null, weeks);
                }
            },
            "a|A": function matchNumber(v) {
                var len = v.length;
                var m = dtStr.match(/^(am|pm)/i);
                if (m === null)
                    return false;
                v = m[0];
                values["ampm"] = v.toLowerCase();
                dtStr = dtStr.substr(v.length);
                return true;
            },
            "z+": function (v) {
                var len = v.length;
                var m;
                if (len <= 2)
                    m = dtStr.match(/^([\-+][0-9]{2})/i);
                else if (len === 3)
                    m = dtStr.match(/^([\-+][0-9]{2})([0-9]{2})/i);
                else
                    m = dtStr.match(/^([\-+][0-9]{2}):([0-9]{2})/i);
                if (m === null)
                    return false;
                v = m[0];
                var tz = parseInt(m[1]);
                if (Math.abs(tz) < -11 || Math.abs(tz) > 11)
                    return false;
                tz *= 60;
                if (typeof m[2] !== tui.undef) {
                    if (tz > 0)
                        tz += parseInt(m[2]);
                    else
                        tz -= parseInt(m[2]);
                }
                values["tz"] = -tz;
                dtStr = dtStr.substr(v.length);
                return true;
            },
            "Z": function (v) {
                if (dtStr.substr(0, 1) !== "Z")
                    return false;
                isUTC = true;
                dtStr = dtStr.substr(1);
                return true;
            },
            "\"[^\"]*\"|'[^']*'": function (v) {
                v = v.substr(1, v.length - 2);
                if (dtStr.substr(0, v.length).toLowerCase() !== v.toLowerCase())
                    return false;
                dtStr = dtStr.substr(v.length);
                return true;
            },
            "[^yMmdDhHsSqEaAzZ'\"]+": function (v) {
                v = v.replace(/(.)/g, '\\$1');
                var m = dtStr.match(new RegExp("^" + v));
                if (m === null)
                    return false;
                v = m[0];
                dtStr = dtStr.substr(v.length);
                return true;
            }
        };
        var regex = "";
        for (var k in rule) {
            if (!rule.hasOwnProperty(k))
                continue;
            if (regex.length > 0)
                regex += "|";
            regex += "(^" + k + ")";
            mapping[k] = ++gcount;
        }
        var result;
        while ((result = format.match(regex)) !== null) {
            for (var k in mapping) {
                var v = result[mapping[k]];
                if (typeof v !== tui.undef) {
                    if (rule[k](v) === false)
                        return null;
                    break;
                }
            }
            format = format.substr(result[0].length);
        }
        if (format.length > 0 || dtStr.length > 0)
            return null;
        var parseCount = 0;
        for (var k in values) {
            if (!values.hasOwnProperty(k))
                continue;
            parseCount++;
        }
        if (parseCount <= 0)
            return null;
        var now = new Date();
        var year = values.hasOwnProperty("year") ? values["year"] : (isUTC ? now.getUTCFullYear() : now.getFullYear());
        var month = values.hasOwnProperty("month") ? values["month"] : (isUTC ? now.getUTCMonth() : now.getMonth());
        var date = values.hasOwnProperty("date") ? values["date"] : (isUTC ? now.getUTCDate() : now.getDate());
        var ampm = values.hasOwnProperty("ampm") ? values["ampm"] : "am";
        var hour;
        if (values.hasOwnProperty("hour"))
            hour = values["hour"];
        else if (values.hasOwnProperty("12hour")) {
            var h12 = values["12hour"];
            if (ampm === "am") {
                if (h12 >= 1 && h12 <= 11) {
                    hour = h12;
                }
                else if (h12 === 12) {
                    hour = h12 - 12;
                }
                else
                    return null;
            }
            else {
                if (h12 === 12)
                    hour = h12;
                else if (h12 >= 1 && h12 <= 11)
                    hour = h12 + 12;
                else
                    return null;
            }
        }
        else
            hour = 0;
        var minute = values.hasOwnProperty("minute") ? values["minute"] : 0;
        var second = values.hasOwnProperty("second") ? values["second"] : 0;
        var millisecond = values.hasOwnProperty("millisecond") ? values["millisecond"] : 0;
        var tz = values.hasOwnProperty("tz") ? values["tz"] : now.getTimezoneOffset();
        now.setUTCFullYear(year);
        now.setUTCMonth(month);
        now.setUTCDate(date);
        now.setUTCHours(hour);
        now.setUTCMinutes(minute);
        now.setUTCSeconds(second);
        now.setUTCMilliseconds(millisecond);
        if (!isUTC) {
            now.setTime(now.getTime() + tz * 60 * 1000);
        }
        return now;
    }
    /**
     * Parse string get date instance (
     * try to parse format:
     *		yyyy-MM-dd HH:mm:ss，
     *		yyyy-MM-dd,
     *		dd MMM yyyy,
     *		MMM dd, yyyy,
     *		ISO8601 format)
     * @param {String} dtStr Data string
     */
    function parseDate(dtStr, format) {
        if (typeof format === "string")
            return parseDateInternal(dtStr, format);
        else if (typeof format === tui.undef) {
            var dt = new Date(dtStr);
            if (!isNaN(dt.getTime()))
                return dt;
            dt = parseDateInternal(dtStr, "yyyy-MM-dd");
            if (dt !== null)
                return dt;
            dt = parseDateInternal(dtStr, "yyyy-MM-dd HH:mm:ss");
            if (dt !== null)
                return dt;
            dt = parseDateInternal(dtStr, "MMM dd, yyyy HH:mm:ss");
            if (dt !== null)
                return dt;
            dt = parseDateInternal(dtStr, "MMM dd, yyyy");
            if (dt !== null)
                return dt;
            dt = parseDateInternal(dtStr, "dd MMM yyyy HH:mm:ss");
            if (dt !== null)
                return dt;
            dt = parseDateInternal(dtStr, "dd MMM yyyy");
            if (dt !== null)
                return dt;
        }
        return null;
    }
    tui.parseDate = parseDate;
    /**
     * Convert date to string and output can be formated to ISO8601, RFC2822, RFC3339 or other customized format
     * @param dt {Date} Date object to be convert
     * @param dateFmt {String} which format should be apply, default use ISO8601 standard format
     */
    function formatDate(dt, dateFmt) {
        if (dateFmt === void 0) { dateFmt = "yyyy-MM-ddTHH:mm:sszzz"; }
        var isUTC = (dateFmt.indexOf("Z") >= 0 ? true : false);
        var fullYear = isUTC ? dt.getUTCFullYear() : dt.getFullYear();
        var month = isUTC ? dt.getUTCMonth() : dt.getMonth();
        var date = isUTC ? dt.getUTCDate() : dt.getDate();
        var hours = isUTC ? dt.getUTCHours() : dt.getHours();
        var minutes = isUTC ? dt.getUTCMinutes() : dt.getMinutes();
        var seconds = isUTC ? dt.getUTCSeconds() : dt.getSeconds();
        var milliseconds = isUTC ? dt.getUTCMilliseconds() : dt.getMilliseconds();
        var day = isUTC ? dt.getUTCDay() : dt.getDay();
        var rule = {
            "y+": fullYear,
            "M+": month + 1,
            "d+": date,
            "D+": dayOfYear(dt) + 1,
            "h+": (function (h) {
                if (h === 0)
                    return h + 12;
                else if (h >= 1 && h <= 12)
                    return h;
                else if (h >= 13 && h <= 23)
                    return h - 12;
            })(hours),
            "H+": hours,
            "m+": minutes,
            "s+": seconds,
            "q+": Math.floor((month + 3) / 3),
            "S+": milliseconds,
            "E+": day,
            "a": (function (h) {
                if (h >= 0 && h <= 11)
                    return "am";
                else
                    return "pm";
            })(isUTC ? dt.getUTCHours() : dt.getHours()),
            "A": (function (h) {
                if (h >= 0 && h <= 11)
                    return "AM";
                else
                    return "PM";
            })(hours),
            "z+": dt.getTimezoneOffset()
        };
        var regex = "";
        for (var k in rule) {
            if (!rule.hasOwnProperty(k))
                continue;
            if (regex.length > 0)
                regex += "|";
            regex += k;
        }
        var regexp = new RegExp(regex, "g");
        return dateFmt.replace(regexp, function (str, pos, source) {
            for (var k in rule) {
                if (str.match(k) !== null) {
                    if (k === "y+") {
                        return tui.paddingNumber(rule[k], str.length, str.length);
                    }
                    else if (k === "a" || k === "A") {
                        return rule[k];
                    }
                    else if (k === "z+") {
                        var z = "";
                        if (rule[k] >= 0) {
                            z += "-";
                        }
                        else {
                            z += "+";
                        }
                        if (str.length < 2)
                            z += Math.abs(Math.floor(rule[k] / 60));
                        else
                            z += tui.paddingNumber(Math.abs(Math.floor(rule[k] / 60)), 2);
                        if (str.length === 3)
                            z += tui.paddingNumber(Math.abs(Math.floor(rule[k] % 60)), 2);
                        else if (str.length > 3)
                            z += (":" + tui.paddingNumber(Math.abs(Math.floor(rule[k] % 60)), 2));
                        return z;
                    }
                    else if (k === "E+") {
                        if (str.length < 3)
                            return tui.paddingNumber(rule[k], str.length);
                        else if (str.length === 3)
                            return shortWeeks[rule[k]];
                        else
                            return weeks[rule[k]];
                    }
                    else if (k === "M+") {
                        if (str.length < 3)
                            return tui.paddingNumber(rule[k], str.length);
                        else if (str.length === 3)
                            return shortMonths[rule[k] - 1];
                        else
                            return months[rule[k] - 1];
                    }
                    else if (k === "S+") {
                        return tui.paddingNumber(rule[k], str.length, str.length, true);
                    }
                    else {
                        return tui.paddingNumber(rule[k], str.length);
                    }
                }
            }
            return str;
        });
    }
    tui.formatDate = formatDate;
})(tui || (tui = {}));
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
/// <reference path="tui.core.ts" />
var tui;
(function (tui) {
    var mimeTypeMap = {
        "application/java-archive": ["jar"],
        "application/msword": ["doc"],
        "application/pdf": ["pdf"],
        "application/pkcs10": ["p10"],
        "application/pkcs7-mime": ["p7m"],
        "application/pkcs7-signature": ["p7s"],
        "application/postscript": ["ai"],
        "application/vnd.ms-excel": ["xls"],
        "application/vnd.ms-powerpoint": ["ppt"],
        "application/vnd.ms-project": ["mpp"],
        "application/vnd.ms-visio.viewer": ["vsd"],
        "application/vnd.ms-xpsdocument": ["xps"],
        "application/vnd.oasis.opendocument.presentation": ["odp"],
        "application/vnd.oasis.opendocument.spreadsheet": ["ods"],
        "application/vnd.oasis.opendocument.text": ["odt"],
        "application/vnd.openxmlformats-officedocument.presentationml.presentation": ["pptx"],
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": ["xlsx"],
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document": ["docx"],
        "application/x-7z-compressed": ["7z"],
        "application/x-bzip2": ["bz2"],
        "application/x-gzip": ["gz"],
        "application/x-javascript": ["js"],
        "application/x-msdownload": ["exe"],
        "application/x-msmetafile": ["wmf"],
        "application/x-pkcs12": ["p12", "pfx"],
        "application/x-pkcs7-certificates": ["p7b"],
        "application/x-pkcs7-certreqresp": ["p7r"],
        "application/x-rar-compressed": ["rar"],
        "application/x-shockwave-flash": ["swf"],
        "application/x-tar": ["tar"],
        "application/x-x509-ca-cert": ["cer"],
        "application/xhtml+xml": ["xhtml"],
        "application/xml": ["xml"],
        "application/zip": ["zip"],
        "audio/mp4": ["m4a"],
        "audio/mpeg": ["mp3"],
        "audio/x-ms-wma": ["wma"],
        "audio/x-pn-realaudio": ["rm"],
        "image/bmp": ["bmp"],
        "image/gif": ["gif"],
        "image/jpeg": ["jpeg"],
        "image/nbmp": ["nbmp"],
        "image/png": ["png"],
        "image/svg-xml": ["svg"],
        "image/tiff": ["tiff"],
        "image/x-icon": ["ico"],
        "message/rfc822": ["eml"],
        "text/css": ["css"],
        "text/html": ["html"],
        "text/plain": ["txt"],
        "text/xml": ["xml"],
        "video/3gpp": ["3gp"],
        "video/3gpp2": ["3gp2"],
        "video/avi": ["avi"],
        "video/mp4": ["mp4"],
        "video/mpeg": ["mpeg"],
        "video/quicktime": ["mov"]
    };
    function checkExtWithMimeType(ext, mimeType) {
        var exts = mimeTypeMap[mimeType];
        if (typeof exts === tui.undef) {
            return true;
        }
        if (exts.indexOf(ext.toLowerCase()) >= 0)
            return true;
        else
            return false;
    }
    tui.checkExtWithMimeType = checkExtWithMimeType;
    function getBox(el) {
        var left, right, top, bottom;
        var offset = $(el).position();
        left = offset.left;
        top = offset.top;
        right = left + el.offsetWidth;
        bottom = top + el.offsetHeight;
        return {
            left: left,
            right: right,
            top: top,
            bottom: bottom
        };
    }
    function copyLayout(from, to) {
        var box = getBox(from);
        $(to).css({
            position: 'absolute',
            left: box.left + 'px',
            top: box.top + 'px',
            width: from.offsetWidth + 'px',
            height: from.offsetHeight + 'px'
        });
    }
    function fileFromPath(file) {
        return file.replace(/.*(\/|\\)/, "");
    }
    function getExt(file) {
        return (-1 !== file.indexOf('.')) ? file.replace(/.*[.]/, '') : '';
    }
    function preventDefault(e) {
        return e.preventDefault();
    }
    var UploadBinding = (function (_super) {
        __extends(UploadBinding, _super);
        function UploadBinding(button, options) {
            _super.call(this);
            this._settings = {
                action: "upload",
                name: "userfile",
                multiple: false,
                autoSubmit: true,
                responseType: "auto",
                hoverClass: "tui-input-btn-hover",
                focusClass: "tui-input-btn-active",
                disabledClass: "tui-input-btn-disabled"
            };
            this._button = null;
            this._input = null;
            this._disabled = false;
            if (options) {
                for (var i in options) {
                    if (options.hasOwnProperty(i)) {
                        this._settings[i] = options[i];
                    }
                }
            }
            if (typeof button === "string") {
                if (/^#.*/.test(button)) {
                    // If jQuery user passes #elementId don't break it
                    button = button.slice(1);
                }
                button = document.getElementById(button);
            }
            if (!button || button.nodeType !== 1) {
                throw new Error("Please make sure that you're passing a valid element");
            }
            if (button.nodeName.toLowerCase() === 'a') {
                // disable link
                $(button).on('click', preventDefault);
            }
            // DOM element
            this._button = button;
            // DOM element                 
            this._input = null;
            this._disabled = false;
            this.installBind();
        }
        UploadBinding.prototype.createIframe = function () {
            var id = tui.uuid();
            var iframe = tui.toElement('<iframe src="javascript:false;" name="' + id + '" />');
            iframe.setAttribute('id', id);
            iframe.style.display = 'none';
            document.body.appendChild(iframe);
            var doc = iframe.contentDocument ? iframe.contentDocument : window.frames[iframe.id].document;
            doc.charset = "utf-8";
            return iframe;
        };
        UploadBinding.prototype.createForm = function (iframe) {
            var settings = this._settings;
            var form = tui.toElement('<form method="post" enctype="multipart/form-data" accept-charset="UTF-8"></form>');
            form.setAttribute('accept-charset', 'UTF-8');
            if (settings.action)
                form.setAttribute('action', settings.action);
            form.setAttribute('target', iframe.name);
            form.style.display = 'none';
            document.body.appendChild(form);
            for (var prop in settings.data) {
                if (settings.data.hasOwnProperty(prop)) {
                    var el = document.createElement("input");
                    el.setAttribute('type', 'hidden');
                    el.setAttribute('name', prop);
                    el.setAttribute('value', settings.data[prop]);
                    form.appendChild(el);
                }
            }
            return form;
        };
        UploadBinding.prototype.createInput = function () {
            var _this = this;
            var input = document.createElement("input");
            input.setAttribute('type', 'file');
            if (this._settings.accept)
                input.setAttribute('accept', this._settings.accept);
            input.setAttribute('name', this._settings.name);
            if (this._settings.multiple)
                input.setAttribute('multiple', 'multiple');
            if (tui.ieVer > 0)
                input.title = "";
            else
                input.title = " ";
            $(input).css({
                'position': 'absolute',
                'right': 0,
                'margin': 0,
                'padding': 0,
                'fontSize': '480px',
                'fontFamily': 'sans-serif',
                'cursor': 'pointer'
            });
            var div = document.createElement("div");
            $(div).css({
                'display': 'block',
                'position': 'absolute',
                'overflow': 'hidden',
                'margin': 0,
                'padding': 0,
                'opacity': 0,
                'direction': 'ltr',
                //Max zIndex supported by Opera 9.0-9.2
                'zIndex': 2147483583
            });
            // Make sure that element opacity exists.
            // Otherwise use IE filter
            if (div.style.opacity !== "0") {
                if (typeof (div.filters) === 'undefined') {
                    throw new Error('Opacity not supported by the browser');
                }
                div.style.filter = "alpha(opacity=0)";
            }
            $(input).on('change', function () {
                if (!input || input.value === '') {
                    return;
                }
                // Get filename from input, required                
                // as some browsers have path instead of it
                var file = fileFromPath(input.value);
                var fileExt = getExt(file);
                // Check accept mimetype, now we only check by submit event.
                //if (this._settings.accept) {
                //	if (!checkExtWithMimeType(fileExt, this._settings.accept)) {
                //		this.clearInput();
                //		this.fire("invalid", { "file": file, "ext": fileExt });
                //		return;
                //	}
                //}
                if (_this.fire("change", { "file": file, "ext": fileExt }) === false) {
                    _this.clearInput();
                    return;
                }
                // Submit form when value is changed
                if (_this._settings.autoSubmit) {
                    _this.submit();
                }
            });
            $(input).on('mouseover', function () {
                $(_this._button).addClass(_this._settings.hoverClass);
            });
            $(input).on('mouseout', function () {
                $(_this._button).removeClass(_this._settings.hoverClass);
                $(_this._button).removeClass(_this._settings.focusClass);
                if (input.parentNode) {
                    // We use visibility instead of display to fix problem with Safari 4
                    // The problem is that the value of input doesn't change if it 
                    // has display none when user selects a file
                    input.parentNode.style.visibility = 'hidden';
                }
            });
            $(input).on('focus', function () {
                $(_this._button).addClass(_this._settings.focusClass);
            });
            $(input).on('blur', function () {
                $(_this._button).removeClass(_this._settings.focusClass);
            });
            div.appendChild(input);
            this._button.offsetParent.appendChild(div);
            this._input = input;
        };
        UploadBinding.prototype.deleteInput = function () {
            if (!this._input) {
                return;
            }
            tui.removeNode(this._input.parentNode);
            this._input = null;
            $(this._button).removeClass(this._settings.hoverClass);
            $(this._button).removeClass(this._settings.focusClass);
        };
        UploadBinding.prototype.clearInput = function () {
            this.deleteInput();
            this.createInput();
        };
        /**
        * Gets response from iframe and fires onComplete event when ready
        * @param iframe
        * @param file Filename to use in onComplete callback
        */
        UploadBinding.prototype.processResponse = function (iframe, file) {
            var _this = this;
            // getting response
            var toDeleteFlag = false, settings = this._settings;
            $(iframe).on('load', function () {
                if (iframe.src === "javascript:'%3Chtml%3E%3C/html%3E';" || iframe.src === "javascript:'<html></html>';") {
                    // First time around, do not delete.
                    // We reload to blank page, so that reloading main page
                    // does not re-submit the post.
                    if (toDeleteFlag) {
                        // Fix busy state in FF3
                        setTimeout(function () {
                            tui.removeNode(iframe);
                        }, 0);
                    }
                    return;
                }
                var doc = iframe.contentDocument ? iframe.contentDocument : window.frames[iframe.id].document;
                // fixing Opera 9.26,10.00
                if (doc.readyState && doc.readyState !== 'complete') {
                    return;
                }
                // fixing Opera 9.64
                if (doc.body && doc.body.innerHTML === "false") {
                    return;
                }
                var response;
                if (doc.XMLDocument) {
                    // response is a xml document Internet Explorer property
                    response = doc.XMLDocument;
                }
                else if (doc.body) {
                    // response is html document or plain text
                    response = doc.body.innerHTML;
                    if (settings.responseType && settings.responseType.toLowerCase() === 'json') {
                        if (doc.body.firstChild && doc.body.firstChild.nodeName.toUpperCase() === 'PRE') {
                            doc.normalize && doc.normalize();
                            response = doc.body.firstChild.firstChild.nodeValue;
                        }
                        if (response) {
                            try {
                                response = eval("(" + response + ")");
                            }
                            catch (e) {
                                response = null;
                            }
                        }
                        else {
                            response = null;
                        }
                    }
                }
                else {
                    // response is a xml document
                    response = doc;
                }
                _this.fire("complete", { "file": file, "ext": getExt(file), "response": response });
                // Reload blank page, so that reloading main page
                // does not re-submit the post. Also, remember to
                // delete the frame
                toDeleteFlag = true;
                // Fix IE mixed content issue
                iframe.src = "javascript:'<html></html>';";
                tui.removeNode(iframe);
            });
        };
        UploadBinding.prototype.submit = function (exparams) {
            if (!this._input || this._input.value === '') {
                return;
            }
            var file = fileFromPath(this._input.value);
            // user returned false to cancel upload
            if (this.fire("submit", { "file": file, "ext": getExt(file) }) === false) {
                this.clearInput();
                return;
            }
            // sending request    
            var iframe = this.createIframe();
            var form = this.createForm(iframe);
            // assuming following structure
            // div -> input type='file'
            tui.removeNode(this._input.parentNode);
            $(this._button).removeClass(this._settings.hoverClass);
            $(this._button).removeClass(this._settings.focusClass);
            form.appendChild(this._input);
            var el = document.createElement("input");
            el.setAttribute('type', 'hidden');
            el.setAttribute('name', "exparams");
            el.setAttribute('value', exparams);
            form.appendChild(el);
            form.submit();
            // request set, clean up
            tui.removeNode(form);
            form = null;
            this.deleteInput();
            // Get response from iframe and fire onComplete event when ready
            this.processResponse(iframe, file);
            // get ready for next request
            this.createInput();
        };
        UploadBinding.prototype.disabled = function (val) {
            if (typeof val === "boolean") {
                this._disabled = val;
                return this;
            }
            else
                return this._disabled;
        };
        UploadBinding.prototype.installBind = function () {
            $(this._button).on('mouseover', { self: this }, UploadBinding.makeBind);
        };
        UploadBinding.prototype.uninstallBind = function () {
            this.deleteInput();
            $(this._button).off('mouseover', UploadBinding.makeBind);
        };
        UploadBinding.makeBind = (function (e) {
            var self = e.data.self;
            if (self._disabled) {
                return;
            }
            if (!self._input) {
                self.createInput();
            }
            var div = self._input.parentNode;
            copyLayout(self._button, div);
            div.style.visibility = 'visible';
        });
        return UploadBinding;
    })(tui.EventObject);
    tui.UploadBinding = UploadBinding;
    function bindUpload(button, options) {
        return new UploadBinding(button, options);
    }
    tui.bindUpload = bindUpload;
})(tui || (tui = {}));
/// <reference path="tui.core.ts" />
var tui;
(function (tui) {
    var ArrayProvider = (function () {
        function ArrayProvider(data) {
            this._src = null;
            this._data = null;
            this._head = null;
            this._headCache = {};
            this._mapping = {};
            this._realKeyMap = null;
            if (data && data instanceof Array) {
                this._src = this._data = data;
            }
            else if (data && data.data) {
                this._src = this._data = data.data;
                if (data.head)
                    this._head = data.head;
                else
                    this._head = null;
            }
            else
                throw new Error("TUI Grid: Unsupported data format!");
        }
        ArrayProvider.prototype.length = function () {
            if (this._data)
                return this._data.length;
            else
                return 0;
        };
        ArrayProvider.prototype.at = function (index) {
            if (this._data)
                return this._data[index];
            else
                return null;
        };
        ArrayProvider.prototype.cell = function (index, key) {
            var row = this.at(index);
            if (!row)
                return null;
            var map = this.columnKeyMap();
            var realKey = map[key];
            if (realKey != null) {
                return row[realKey];
            }
            else {
                return row[key];
            }
        };
        ArrayProvider.prototype.columnKeyMap = function () {
            if (this._realKeyMap !== null)
                return this._realKeyMap;
            if (this._head) {
                var map = {};
                for (var i = 0; i < this._head.length; i++) {
                    map[this._head[i]] = i;
                }
                for (var k in this._mapping) {
                    if (!this._mapping.hasOwnProperty(k))
                        continue;
                    var mapTo = this._mapping[k];
                    if (map.hasOwnProperty(mapTo)) {
                        map[k] = map[mapTo];
                    }
                    else {
                        map[k] = mapTo;
                    }
                }
                this._realKeyMap = map;
                return map;
            }
            else {
                this._realKeyMap = this._mapping;
                return this._mapping;
            }
        };
        ArrayProvider.prototype.mapKey = function (key) {
            var map = this.columnKeyMap();
            var realKey = map[key];
            if (realKey != null) {
                return realKey;
            }
            else {
                return key;
            }
        };
        ArrayProvider.prototype.addKeyMap = function (key, mapTo) {
            this._mapping[key] = mapTo;
            this._realKeyMap = null;
        };
        ArrayProvider.prototype.removeKeyMap = function (key) {
            delete this._mapping[key];
            this._realKeyMap = null;
        };
        ArrayProvider.prototype.sort = function (key, desc, func) {
            if (func === void 0) { func = null; }
            if (this._src) {
                if (typeof func === "function") {
                    this._data = this._src.concat();
                    this._data.sort(func);
                }
                else if (key === null && func === null) {
                    this._data = this._src;
                    return this;
                }
                else {
                    if (this._head && typeof key === "string") {
                        key = this._head.indexOf(key);
                    }
                    this._data = this._src.concat();
                    this._data.sort(function (a, b) {
                        if (a[key] > b[key]) {
                            return desc ? -1 : 1;
                        }
                        else if (a[key] < b[key]) {
                            return desc ? 1 : -1;
                        }
                        else {
                            return 0;
                        }
                    });
                }
            }
            else {
                this._data = null;
            }
            return this;
        };
        ArrayProvider.prototype.data = function (data) {
            if (data) {
                if (data instanceof Array) {
                    this._src = this._data = data;
                    return this;
                }
                else if (data.data) {
                    this._src = this._data = data.data;
                    if (data.head)
                        this._head = data.head;
                    else
                        this._head = null;
                    return this;
                }
                else
                    throw new Error("TUI Grid: Unsupported data format!");
            }
            else {
                return this._data;
            }
        };
        /**
         * ArrayDataProvider peculiar, get source data set
         */
        ArrayProvider.prototype.src = function () {
            return this._src;
        };
        ArrayProvider.prototype.process = function (func) {
            this._data = func(this._src);
        };
        return ArrayProvider;
    })();
    tui.ArrayProvider = ArrayProvider;
    var RemoteCursorProvider = (function () {
        function RemoteCursorProvider(cacheSize) {
            if (cacheSize === void 0) { cacheSize = 100; }
            this._queryTimer = null;
            this._mapping = {};
            this._realKeyMap = null;
            this._firstQuery = true;
            this._cacheSize = cacheSize;
            this._invalid = true;
            this._data = [];
            this._begin = 0;
            this._length = 0;
            this._sortKey = null;
        }
        RemoteCursorProvider.prototype.length = function () {
            if (this._invalid) {
                this.doQuery(0);
            }
            return this._length;
        };
        RemoteCursorProvider.prototype.at = function (index) {
            if (index < 0 || index >= this.length()) {
                return null;
            }
            else if (this._invalid || index < this._begin || index >= this._begin + this._data.length) {
                this.doQuery(index);
            }
            if (index >= this._begin || index < this._begin + this._data.length)
                return this._data[index - this._begin];
            else
                return null;
        };
        RemoteCursorProvider.prototype.cell = function (index, key) {
            var row = this.at(index);
            if (!row)
                return null;
            var map = this.columnKeyMap();
            var realKey = map[key];
            if (realKey != null) {
                return row[realKey];
            }
            else {
                return row[key];
            }
        };
        RemoteCursorProvider.prototype.addKeyMap = function (key, mapTo) {
            this._mapping[key] = mapTo;
            this._realKeyMap = null;
        };
        RemoteCursorProvider.prototype.removeKeyMap = function (key) {
            delete this._mapping[key];
            this._realKeyMap = null;
        };
        RemoteCursorProvider.prototype.columnKeyMap = function () {
            if (this._realKeyMap !== null)
                return this._realKeyMap;
            if (this._head) {
                var map = {};
                for (var i = 0; i < this._head.length; i++) {
                    map[this._head[i]] = i;
                }
                for (var k in this._mapping) {
                    if (!this._mapping.hasOwnProperty(k))
                        continue;
                    var mapTo = this._mapping[k];
                    if (map.hasOwnProperty(mapTo)) {
                        map[k] = map[mapTo];
                    }
                    else {
                        map[k] = mapTo;
                    }
                }
                this._realKeyMap = map;
                return map;
            }
            else {
                this._realKeyMap = this._mapping;
                return this._mapping;
            }
        };
        RemoteCursorProvider.prototype.mapKey = function (key) {
            var map = this.columnKeyMap();
            var realKey = map[key];
            if (realKey != null) {
                return realKey;
            }
            else {
                return key;
            }
        };
        RemoteCursorProvider.prototype.sort = function (key, desc, func) {
            if (func === void 0) { func = null; }
            this._sortKey = key;
            this._desc = desc;
            this._invalid = true;
            return this;
        };
        RemoteCursorProvider.prototype.doQuery = function (begin) {
            var _this = this;
            if (typeof this._queryCallback !== "function") {
                return;
            }
            if (this._queryTimer !== null)
                clearTimeout(this._queryTimer);
            var self = this;
            var cacheBegin = begin - Math.round(this._cacheSize / 2);
            if (cacheBegin < 0)
                cacheBegin = 0;
            var queryInfo = {
                begin: cacheBegin,
                cacheSize: this._cacheSize,
                sortKey: this._sortKey,
                sortDesc: this._desc,
                update: function (info) {
                    self._data = info.data;
                    self._length = info.length;
                    self._begin = info.begin;
                    self._invalid = false;
                    if (typeof info.head !== tui.undef) {
                        self._head = info.head;
                    }
                    if (typeof self._updateCallback === "function") {
                        self._updateCallback({
                            length: self._length,
                            begin: self._begin,
                            data: self._data
                        });
                    }
                }
            };
            if (this._firstQuery) {
                this._firstQuery = false;
                this._queryCallback(queryInfo);
            }
            else {
                this._queryTimer = setTimeout(function () {
                    _this._firstQuery = true;
                    _this._queryTimer = null;
                    _this._queryCallback(queryInfo);
                }, 100);
            }
        };
        RemoteCursorProvider.prototype.onupdate = function (callback) {
            this._updateCallback = callback;
        };
        // Cursor own functions
        RemoteCursorProvider.prototype.onquery = function (callback) {
            this._queryCallback = callback;
        };
        return RemoteCursorProvider;
    })();
    tui.RemoteCursorProvider = RemoteCursorProvider;
})(tui || (tui = {}));
/// <reference path="tui.core.ts" />
var tui;
(function (tui) {
    var _maskDiv = document.createElement("div");
    _maskDiv.className = "tui-mask";
    _maskDiv.setAttribute("unselectable", "on");
    var mousewheelevt = (/Firefox/i.test(navigator.userAgent)) ? "DOMMouseScroll" : "mousewheel";
    $(_maskDiv).on(mousewheelevt, function (ev) {
        ev.stopPropagation();
        ev.preventDefault();
    });
    var _tooltip = document.createElement("span");
    _tooltip.className = "tui-tooltip";
    _tooltip.setAttribute("unselectable", "on");
    var _tooltipTarget = null;
    /**
     * Show a mask layer to prevent user drag or select document elements which don't want to be affected.
     * It's very useful when user perform a dragging operation.
     */
    function mask() {
        document.body.appendChild(_maskDiv);
        return _maskDiv;
    }
    tui.mask = mask;
    /**
     * Close a mask layer
     */
    function unmask() {
        if (_maskDiv.parentNode)
            _maskDiv.parentNode.removeChild(_maskDiv);
        _maskDiv.innerHTML = "";
        _maskDiv.style.cursor = "";
        _maskDiv.removeAttribute("tabIndex");
        _maskDiv.removeAttribute("data-tooltip");
        _maskDiv.removeAttribute("data-cursor-tooltip");
        return _maskDiv;
    }
    tui.unmask = unmask;
    function showTooltipAtCursor(target, tooltip, x, y) {
        if (target === _tooltipTarget || target === _tooltip) {
            _tooltip.style.left = x - 17 + "px";
            _tooltip.style.top = y + 20 + "px";
            _tooltip.innerHTML = tooltip;
            return;
        }
        document.body.appendChild(_tooltip);
        _tooltip.innerHTML = tooltip;
        _tooltipTarget = target;
        _tooltip.style.left = x - 17 + "px";
        _tooltip.style.top = y + 20 + "px";
    }
    tui.showTooltipAtCursor = showTooltipAtCursor;
    function showTooltip(target, tooltip) {
        if (target === _tooltipTarget || target === _tooltip) {
            return;
        }
        document.body.appendChild(_tooltip);
        _tooltip.innerHTML = tooltip;
        _tooltipTarget = target;
        var pos = tui.fixedPosition(target);
        if (target.offsetWidth < 20)
            _tooltip.style.left = (pos.x + target.offsetWidth / 2 - 17) + "px";
        else
            _tooltip.style.left = pos.x + "px";
        _tooltip.style.top = pos.y + 8 + target.offsetHeight + "px";
    }
    tui.showTooltip = showTooltip;
    function closeTooltip() {
        if (_tooltip.parentNode)
            _tooltip.parentNode.removeChild(_tooltip);
        _tooltip.innerHTML = "";
        _tooltipTarget = null;
    }
    tui.closeTooltip = closeTooltip;
    function whetherShowTooltip(target, e) {
        if (tui.isAncestry(target, _tooltip))
            return;
        var obj = target;
        while (obj) {
            var tooltip = obj.getAttribute("data-tooltip");
            if (tooltip) {
                if (obj.getAttribute("data-cursor-tooltip") === "true")
                    showTooltipAtCursor(obj, tooltip, e.clientX, e.clientY);
                else
                    showTooltip(obj, tooltip);
                return;
            }
            else {
                obj = obj.parentElement;
            }
        }
        if (!obj)
            closeTooltip();
    }
    tui.whetherShowTooltip = whetherShowTooltip;
    function whetherCloseTooltip(target) {
        if (target !== _tooltipTarget && target !== _tooltip) {
            closeTooltip();
        }
    }
    tui.whetherCloseTooltip = whetherCloseTooltip;
})(tui || (tui = {}));
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Control = (function (_super) {
            __extends(Control, _super);
            function Control(tagName, className, el) {
                _super.call(this);
                this._exposedEvents = {};
                if (typeof el === "object")
                    this.elem(el);
                else
                    this.elem(tagName, className);
                if (this[0])
                    this[0]._ctrl = this;
            }
            /**
             * Construct a component
             */
            Control.prototype.elem = function (el, clsName) {
                if (el && el.nodeName || el === null) {
                    this[0] = el;
                    this.bindMainElementEvent();
                }
                else if (typeof el === "string" && typeof clsName === "string") {
                    this[0] = document.createElement(el);
                    this[0].className = clsName;
                    this.bindMainElementEvent();
                }
                return this[0];
            };
            Control.prototype.bindMainElementEvent = function () {
                if (!this[0]) {
                    return;
                }
                var self = this;
                $(this[0]).focus(function () {
                    self.addClass("tui-focus");
                });
                $(this[0]).blur(function () {
                    self.removeClass("tui-focus");
                });
            };
            Control.prototype.exposeEvents = function (eventNames) {
                if (this[0]) {
                    if (typeof eventNames === "string")
                        eventNames = eventNames.split(/\s+/);
                    for (var i = 0; i < eventNames.length; i++) {
                        this._exposedEvents[eventNames[i]] = true;
                    }
                }
            };
            Control.prototype.bind = function (eventName, handler, priority) {
                if (this._exposedEvents[eventName]) {
                    $(this[0]).on(eventName, handler);
                }
                else
                    _super.prototype.bind.call(this, eventName, handler, priority);
            };
            Control.prototype.unbind = function (eventName, handler) {
                if (this._exposedEvents[eventName]) {
                    $(this[0]).off(eventName, handler);
                }
                else
                    _super.prototype.unbind.call(this, eventName, handler);
            };
            Control.prototype.id = function (val) {
                if (typeof val === "string") {
                    if (this[0])
                        this[0].id = val;
                    return this;
                }
                else {
                    if (this[0] && this[0].id)
                        return this[0].id;
                    else
                        return null;
                }
            };
            Control.prototype.hasAttr = function (attributeName) {
                if (this[0])
                    return typeof $(this[0]).attr(attributeName) === "string";
                else
                    return false;
            };
            Control.prototype.isAttrTrue = function (attributeName) {
                if (this.hasAttr(attributeName)) {
                    var attr = this.attr(attributeName).toLowerCase();
                    return attr === "" || attr === "true" || attr === "on";
                }
                else
                    return false;
            };
            Control.prototype.attr = function (p1, p2) {
                if (typeof p1 === "string" && typeof p2 === tui.undef) {
                    if (!this[0])
                        return null;
                    else {
                        var val = $(this[0]).attr(p1);
                        if (val === null || typeof val === tui.undef)
                            return null;
                        else
                            return val;
                    }
                }
                else {
                    if (this[0])
                        $(this[0]).attr(p1, p2);
                    return this;
                }
            };
            Control.prototype.removeAttr = function (attributeName) {
                if (this[0])
                    $(this[0]).removeAttr(attributeName);
                return this;
            };
            Control.prototype.css = function (p1, p2) {
                if (typeof p1 === "string" && typeof p2 === tui.undef) {
                    if (!this[0])
                        return null;
                    else
                        return $(this[0]).css(p1);
                }
                else {
                    if (this[0])
                        $(this[0]).css(p1, p2);
                    return this;
                }
            };
            Control.prototype.hasClass = function (className) {
                if (this[0])
                    return $(this[0]).hasClass(className);
                else
                    return false;
            };
            Control.prototype.addClass = function (param) {
                if (this[0])
                    $(this[0]).addClass(param);
                return this;
            };
            Control.prototype.removeClass = function (param) {
                if (this[0])
                    $(this[0]).removeClass(param);
                return this;
            };
            Control.prototype.refresh = function () {
            };
            Control.prototype.is = function (attrName, val) {
                if (typeof val === "boolean") {
                    if (val)
                        this.attr(attrName, "true");
                    else
                        this.removeAttr(attrName);
                    if (this[0] && tui.ieVer > 0 && tui.ieVer <= 8) {
                        this[0].className = this[0].className;
                    }
                    return this;
                }
                else {
                    return this.isAttrTrue(attrName);
                }
            };
            Control.prototype.hidden = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-hidden", val);
                    if (val) {
                        this.addClass("tui-hidden");
                    }
                    else
                        this.removeClass("tui-hidden");
                    return this;
                }
                else
                    return this.is("data-hidden");
            };
            Control.prototype.checked = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-checked", val);
                    this.fire("check", { ctrl: this, checked: val });
                    return this;
                }
                else
                    return this.is("data-checked");
            };
            Control.prototype.actived = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-actived", val);
                    if (val) {
                        this.addClass("tui-actived");
                    }
                    else
                        this.removeClass("tui-actived");
                    return this;
                }
                else
                    return this.is("data-actived");
            };
            Control.prototype.disabled = function (val) {
                return this.is("data-disabled", val);
            };
            Control.prototype.marked = function (val) {
                return this.is("data-marked", val);
            };
            Control.prototype.selectable = function (val) {
                if (typeof val === "boolean") {
                    if (!val)
                        this.attr("unselectable", "on");
                    else
                        this.removeAttr("unselectable");
                    return this;
                }
                else {
                    return !this.isAttrTrue("unselectable");
                }
            };
            Control.prototype.form = function (txt) {
                if (typeof txt === "string") {
                    this.attr("data-form", txt);
                    return this;
                }
                else
                    return this.attr("data-form");
            };
            Control.prototype.field = function (txt) {
                if (typeof txt === "string") {
                    this.attr("data-field", txt);
                    return this;
                }
                else
                    return this.attr("data-field");
            };
            Control.prototype.blur = function () {
                if (this[0]) {
                    this[0].blur();
                }
            };
            Control.prototype.focus = function () {
                var _this = this;
                if (this[0]) {
                    setTimeout(function () {
                        _this[0].focus();
                    }, 0);
                }
            };
            Control.prototype.focusWithoutScroll = function () {
                var _this = this;
                if (this[0]) {
                    setTimeout(function () {
                        tui.focusWithoutScroll(_this[0]);
                    }, 0);
                }
            };
            Control.prototype.isHover = function () {
                if (this[0]) {
                    return tui.isAncestry(_hoverElement, this[0]);
                }
                else
                    return false;
            };
            Control.prototype.isFocused = function () {
                if (this[0]) {
                    return tui.isAncestry(document.activeElement, this[0]);
                }
                else
                    return false;
            };
            Control.prototype.isAncestry = function (ancestry) {
                return tui.isAncestry(this[0], ancestry);
            };
            Control.prototype.isPosterity = function (posterity) {
                return tui.isPosterity(this[0], posterity);
            };
            Control.prototype.autoRefresh = function () {
                return true;
            };
            return Control;
        })(tui.EventObject);
        ctrl.Control = Control;
        function control(param, constructor, constructParam) {
            var elem = null;
            if (typeof param === "string" && param) {
                elem = document.getElementById(param);
                if (!elem)
                    return null;
                if (elem._ctrl) {
                    elem._ctrl.autoRefresh() && elem._ctrl.refresh();
                    return elem._ctrl;
                }
                else if (typeof constructParam !== tui.undef) {
                    return new constructor(elem, constructParam);
                }
                else
                    return new constructor(elem);
            }
            else if (param && param.nodeName) {
                elem = param;
                if (elem._ctrl) {
                    elem._ctrl.autoRefresh() && elem._ctrl.refresh();
                    return elem._ctrl;
                }
                else if (typeof constructParam !== tui.undef) {
                    return new constructor(elem, constructParam);
                }
                else
                    return new constructor(elem);
            }
            else if ((typeof param === tui.undef || param === null) && constructor) {
                if (typeof constructParam !== tui.undef) {
                    return new constructor(null, constructParam);
                }
                else
                    return new constructor();
            }
            else
                return null;
        }
        ctrl.control = control;
        var initializers = {};
        function registerInitCallback(clsName, constructFunc) {
            if (!initializers[clsName]) {
                initializers[clsName] = constructFunc;
            }
        }
        ctrl.registerInitCallback = registerInitCallback;
        function initCtrls(parent) {
            for (var clsName in initializers) {
                if (clsName) {
                    var func = initializers[clsName];
                    $(parent).find("." + clsName).each(function (idx, elem) {
                        func(elem);
                    });
                }
            }
        }
        ctrl.initCtrls = initCtrls;
        //var checkTooltipTimeout = null;
        var _hoverElement;
        $(window.document).mousemove(function (e) {
            _hoverElement = e.target || e.toElement;
            if (e.button === 0 && (e.which === 1 || e.which === 0)) {
                //if (checkTooltipTimeout)
                //	clearTimeout(checkTooltipTimeout);
                //checkTooltipTimeout = setTimeout(function () {
                tui.whetherShowTooltip(_hoverElement, e);
            }
        });
        $(window).scroll(function () {
            tui.closeTooltip();
        });
        $(window.document).ready(function () {
            initCtrls(document);
            tui.fire("initialized", null);
        });
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (_ctrl) {
        var FormAgent = (function (_super) {
            __extends(FormAgent, _super);
            function FormAgent(el) {
                _super.call(this, "span", FormAgent.CLASS, el);
                var parent = this[0].parentElement;
                while (parent) {
                    if ($(parent).hasClass("tui-form")) {
                        this.form($(parent).attr("id"));
                        break;
                    }
                    else
                        parent = parent.parentElement;
                }
                if (!this.hasAttr("data-target-property")) {
                    this.targetProperty("value");
                }
            }
            FormAgent.prototype.validate = function () {
                var param = { valid: true };
                if (this.fire("validate", param) === false)
                    return param.valid;
                var target = this.target();
                var isGroup = this.isGroup();
                if (!target)
                    return true;
                if (isGroup) {
                    var validator = this.groupValidator();
                    if (!validator)
                        return true;
                    var controls = $("." + _ctrl.Radiobox.CLASS + "[data-group='" + target + "'],." + _ctrl.Checkbox.CLASS + "[data-group='" + target + "']");
                    var values = [];
                    controls.each(function (index, elem) {
                        if (tui.parseBoolean($(elem).attr("data-checked")))
                            values.push($(elem).attr("data-value"));
                    });
                    var valid = true;
                    for (var k in validator) {
                        if (k && validator.hasOwnProperty(k)) {
                            if (k.substr(0, 5) === "*max:") {
                                var imax = parseFloat(k.substr(5));
                                if (isNaN(imax))
                                    throw new Error("Invalid validator: '*max:...' must follow a number");
                                var ival = values.length;
                                if (ival > imax) {
                                    valid = false;
                                }
                            }
                            else if (k.substr(0, 5) === "*min:") {
                                var imin = parseFloat(k.substr(5));
                                if (isNaN(imin))
                                    throw new Error("Invalid validator: '*min:...' must follow a number");
                                var ival = values.length;
                                if (ival < imin) {
                                    valid = false;
                                }
                            }
                            else {
                                valid = values.indexOf(k) >= 0;
                            }
                            if (!valid) {
                                controls.each(function (index, elem) {
                                    var ctrl = elem["_ctrl"];
                                    if (ctrl && typeof ctrl.notify === "function")
                                        ctrl.notify(validator[k]);
                                });
                                break;
                            }
                        }
                    }
                    return valid;
                }
                else {
                    var elem = document.getElementById(target);
                    if (elem && elem["_ctrl"]) {
                        var ctrl = elem["_ctrl"];
                        if (typeof ctrl.validate === "function") {
                            return ctrl.validate();
                        }
                    }
                    return true;
                }
            };
            FormAgent.prototype.target = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-target", val);
                    return this;
                }
                else
                    return this.attr("data-target");
            };
            FormAgent.prototype.targetProperty = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-target-property", val);
                    return this;
                }
                else
                    return this.attr("data-target-property");
            };
            FormAgent.prototype.groupValidator = function (val) {
                if (typeof val === "object" && val) {
                    this.attr("data-group-validator", JSON.stringify(val));
                    return this;
                }
                else if (val === null) {
                    this.removeAttr("data-group-validator");
                    return this;
                }
                else {
                    var strval = this.attr("data-group-validator");
                    if (strval === null) {
                        return null;
                    }
                    else {
                        try {
                            val = eval("(" + strval + ")");
                            if (typeof val !== "object")
                                return null;
                            else
                                return val;
                        }
                        catch (err) {
                            return null;
                        }
                    }
                }
            };
            FormAgent.prototype.isGroup = function (val) {
                if (typeof val !== tui.undef) {
                    this.is("data-is-group", !!val);
                    return this;
                }
                else
                    return this.is("data-is-group");
            };
            FormAgent.prototype.value = function (val) {
                var property = this.targetProperty();
                var target = this.target();
                var isGroup = this.isGroup();
                if (typeof val !== tui.undef) {
                    var param = { value: val };
                    if (this.fire("setvalue", param) === false)
                        return this;
                    val = param.value;
                    if (!target) {
                        this.attr("data-value", JSON.stringify(val));
                        return this;
                    }
                    if (isGroup) {
                        var controls = $("." + _ctrl.Radiobox.CLASS + "[data-group='" + target + "'],." + _ctrl.Checkbox.CLASS + "[data-group='" + target + "']");
                        var values;
                        if (val && typeof val.length === "number")
                            values = val;
                        else if (val === null)
                            values = [];
                        else
                            values = [val];
                        controls.each(function (index, elem) {
                            var ctrl = elem["_ctrl"];
                            if (typeof ctrl[property] === "function") {
                                if (values.indexOf(ctrl[property]()) >= 0) {
                                    ctrl.checked(true);
                                }
                                else
                                    ctrl.checked(false);
                            }
                        });
                    }
                    else {
                        var elem = document.getElementById(target);
                        if (elem && elem["_ctrl"]) {
                            var ctrl = elem["_ctrl"];
                            if (typeof ctrl[property] === "function") {
                                ctrl[property](val);
                            }
                        }
                        else if (elem) {
                            if (typeof elem[property] === "function") {
                                elem[property](val);
                            }
                            else {
                                elem[property] = val;
                            }
                        }
                    }
                    return this;
                }
                else {
                    var val = null;
                    if (!target) {
                        var strval = this.attr("data-value");
                        if (strval === null) {
                            val = null;
                        }
                        else {
                            try {
                                val = eval("(" + strval + ")");
                            }
                            catch (err) {
                                val = null;
                            }
                        }
                    }
                    else if (isGroup) {
                        var controls = $("." + _ctrl.Radiobox.CLASS + "[data-group='" + target + "']");
                        var values = [];
                        if (controls.length > 0) {
                            controls.each(function (index, elem) {
                                var ctrl = elem["_ctrl"];
                                if (ctrl) {
                                    if (typeof ctrl.checked === "function" && ctrl.checked() && typeof ctrl[property] === "function") {
                                        values.push(ctrl[property]());
                                    }
                                }
                            });
                            if (values.length > 0)
                                val = values[0];
                            else
                                val = null;
                        }
                        else {
                            controls = $("." + _ctrl.Checkbox.CLASS + "[data-group='" + target + "']");
                            controls.each(function (index, elem) {
                                var ctrl = elem["_ctrl"];
                                if (ctrl) {
                                    if (typeof ctrl.checked === "function" && ctrl.checked() && typeof ctrl[property] === "function") {
                                        values.push(ctrl[property]());
                                    }
                                }
                            });
                            val = values;
                        }
                    }
                    else {
                        var elem = document.getElementById(target);
                        if (elem && elem["_ctrl"]) {
                            var ctrl = elem["_ctrl"];
                            if (typeof ctrl[property] === "function") {
                                val = ctrl[property]();
                            }
                        }
                        else if (elem) {
                            if (typeof elem[property] === "function") {
                                val = elem[property]();
                            }
                            else {
                                val = elem[property];
                            }
                        }
                    }
                    var param = { value: val };
                    this.fire("getvalue", param);
                    return param.value;
                }
            };
            FormAgent.CLASS = "tui-form-agent";
            return FormAgent;
        })(_ctrl.Control);
        _ctrl.FormAgent = FormAgent;
        function formAgent(param) {
            return tui.ctrl.control(param, FormAgent);
        }
        _ctrl.formAgent = formAgent;
        tui.ctrl.registerInitCallback(FormAgent.CLASS, formAgent);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.formagent.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (_ctrl) {
        var Form = (function (_super) {
            __extends(Form, _super);
            function Form(el) {
                _super.call(this, "span", Form.CLASS, el);
                if (!this.hasAttr("data-method")) {
                    this.method("POST");
                }
                if (!this.hasAttr("data-timeout")) {
                    this.timeout(60000);
                }
                if (!this.hasAttr("data-target-property")) {
                    this.targetProperty("value");
                }
                if (!this.hasAttr("data-show-error")) {
                    this.isShowError(true);
                }
                if (this.id() === null)
                    this.id(tui.uuid());
                for (var i = 0; i < this[0].childNodes.length; i++) {
                    if (this[0].childNodes[i].nodeName.toLowerCase() === "span") {
                        var agent = tui.ctrl.formAgent(this[0].childNodes[i]);
                        agent.form(this.id());
                    }
                }
                var self = this;
                if (this.isAutoSubmit()) {
                    tui.on("initialized", function () {
                        self.submit();
                    });
                }
            }
            Form.prototype.isAutoSubmit = function (val) {
                if (typeof val !== tui.undef) {
                    this.is("data-auto-submit", !!val);
                    return this;
                }
                else
                    return this.is("data-auto-submit");
            };
            Form.prototype.isShowError = function (val) {
                if (typeof val !== tui.undef) {
                    this.is("data-show-error", !!val);
                    return this;
                }
                else
                    return this.is("data-show-error");
            };
            Form.prototype.waiting = function (msg) {
                if (typeof msg === "string") {
                    this.attr("data-waiting", msg);
                    return this;
                }
                else
                    return this.attr("data-waiting");
            };
            Form.prototype.action = function (url) {
                if (typeof url === "string") {
                    this.attr("data-action", url);
                    return this;
                }
                else
                    return this.attr("data-action");
            };
            Form.prototype.method = function (val) {
                if (typeof val === "string" && Form.METHODS.indexOf(val.toUpperCase()) >= 0) {
                    this.attr("data-method", val.toUpperCase());
                    return this;
                }
                else
                    return this.attr("data-method").toUpperCase();
            };
            Form.prototype.timeout = function (val) {
                if (typeof val === "number") {
                    this.attr("data-timeout", Math.round(val) + "");
                    return this;
                }
                else
                    return parseInt(this.attr("data-timeout"), 10);
            };
            Form.prototype.target = function (val) {
                if (typeof val === "string") {
                    this.attr("data-target", val);
                    return this;
                }
                else
                    return this.attr("data-target");
            };
            Form.prototype.targetProperty = function (val) {
                if (typeof val === "string") {
                    this.attr("data-target-property", val);
                    return this;
                }
                else
                    return this.attr("data-target-property");
            };
            Form.prototype.targetRedirect = function (val) {
                if (typeof val === "string") {
                    this.attr("data-target-redirect", val);
                    return this;
                }
                else
                    return this.attr("data-target-redirect");
            };
            Form.prototype.submitForm = function (val) {
                if (typeof val === "string") {
                    this.attr("data-submit-form", val);
                    return this;
                }
                else
                    return this.attr("data-submit-form");
            };
            Form.prototype.validate = function () {
                var id = this.id();
                if (!id) {
                    return true;
                }
                var valid = true;
                $("[data-form='" + id + "']").each(function (index, elem) {
                    if (typeof this._ctrl.validate === "function")
                        if (!this._ctrl.validate())
                            valid = false;
                });
                return valid;
            };
            Form.prototype.immediateValue = function (val) {
                if (typeof val !== tui.undef) {
                    this._immediateValue = val;
                    return this;
                }
                else
                    return this._immediateValue;
            };
            Form.prototype.value = function (val) {
                if (typeof val !== tui.undef) {
                    // Dispatch data to other controls
                    var id = this.id();
                    id && $("[data-form='" + id + "']").each(function (index, elem) {
                        var field;
                        if (this._ctrl) {
                            field = this._ctrl.field();
                            if (!field) {
                                return;
                            }
                            else if (field === "*") {
                                if (typeof this._ctrl.value === "function")
                                    this._ctrl.value(val);
                            }
                            else {
                                if (typeof this._ctrl.value === "function" && typeof val[field] !== tui.undef) {
                                    this._ctrl.value(val[field]);
                                }
                            }
                        }
                        else {
                            field = $(elem).attr("data-field");
                            if (!field) {
                                return;
                            }
                            else if (field === "*") {
                                $(elem).attr("data-value", JSON.stringify(val));
                            }
                            else {
                                if (typeof val[field] !== tui.undef) {
                                    $(elem).attr("data-value", JSON.stringify(val[field]));
                                }
                            }
                        }
                    });
                    return this;
                }
                else {
                    var result = {};
                    // Collect all fields from other controls
                    var id = this.id();
                    id && $("[data-form='" + id + "']").each(function (index, elem) {
                        var field;
                        var val;
                        if (this._ctrl) {
                            field = this._ctrl.field();
                            if (!field)
                                return;
                            if (this._ctrl.value)
                                val = this._ctrl.value();
                            else
                                return;
                        }
                        else {
                            field = $(elem).attr("data-field");
                            if (typeof field !== "string")
                                return;
                            val = $(elem).attr("data-value");
                            if (typeof val !== "string")
                                return;
                            try {
                                val = JSON.parse(val);
                            }
                            catch (e) {
                            }
                        }
                        if (field === "*")
                            result = val;
                        else if (result)
                            result[field] = val;
                    });
                    return result;
                }
            };
            Form.prototype.clear = function () {
                this._immediateValue = tui.undefVal;
                var id = this.id();
                id && $("[data-form='" + id + "']").each(function (index, elem) {
                    if (elem._ctrl) {
                        if (typeof elem._ctrl.value === "function")
                            elem._ctrl.value(null);
                    }
                    else {
                        $(elem).attr("data-value", "");
                        $(elem).removeAttr("data-value");
                    }
                });
            };
            Form.prototype.submit = function () {
                if (!this.validate())
                    return;
                var action = this.action();
                if (!action)
                    return;
                var id = this.id();
                if (!id)
                    return;
                var data = this.immediateValue();
                if (typeof data === tui.undef)
                    data = this.value();
                if (this.fire("submit", { id: this.id(), data: data }) === false)
                    return;
                var self = this;
                var waitDlg = null;
                if (this.waiting()) {
                    waitDlg = tui.waitbox(tui.str(this.waiting()));
                }
                $.ajax({
                    "type": this.method(),
                    "timeout": this.timeout(),
                    "url": action,
                    "contentType": "application/json",
                    "data": (this.method() === "GET" ? data : JSON.stringify(data)),
                    "complete": function (jqXHR, status) {
                        waitDlg && waitDlg.close();
                        if (status === "success") {
                            if (self.fire("success", { jqXHR: jqXHR, status: status }) === false) {
                                return;
                            }
                        }
                        else {
                            if (self.fire("error", { jqXHR: jqXHR, status: status }) === false) {
                                return;
                            }
                        }
                        if (self.fire("complete", { jqXHR: jqXHR, status: status }) === false) {
                            return;
                        }
                        if (status === "success") {
                            var targetRedirect = self.targetRedirect();
                            if (targetRedirect) {
                                window.location.assign(targetRedirect);
                                return;
                            }
                            var target = self.target();
                            var property = self.targetProperty();
                            if (target) {
                                var respJson = /^\s*application\/json\s*(;.+)?/i.test(jqXHR.getResponseHeader("content-type"));
                                var respVal = (respJson ? jqXHR["responseJSON"] : jqXHR.responseText);
                                target = document.getElementById(target);
                                if (target && target["_ctrl"]) {
                                    var ctrl = target["_ctrl"];
                                    if (typeof ctrl[property] === "function") {
                                        ctrl[property](respVal);
                                    }
                                }
                                else if (target) {
                                    if (typeof target[property] === "function") {
                                        target[property](respVal);
                                    }
                                    else {
                                        target[property] = respVal;
                                    }
                                }
                            }
                            var targetSubmitForm = self.submitForm();
                            if (targetSubmitForm) {
                                var form = tui.ctrl.form(targetSubmitForm);
                                form && form.submit();
                            }
                        }
                        else {
                            if (typeof Form.defaultErrorProc === "function") {
                                if (Form.defaultErrorProc({ jqXHR: jqXHR, status: status }) === false)
                                    return;
                            }
                            if (self.isShowError() && !(Form.ignoreErrors && Form.ignoreErrors.indexOf(jqXHR.status) >= 0)) {
                                var respText = /^\s*text\/plain\s*(;.+)?/i.test(jqXHR.getResponseHeader("content-type"));
                                if (respText && jqXHR.responseText)
                                    tui.errbox(tui.str(jqXHR.responseText), tui.str("Failed"));
                                else
                                    tui.errbox(tui.str(status) + " (" + jqXHR.status + ")", tui.str("Failed"));
                            }
                        }
                    },
                    "processData": (this.method() === "GET" ? true : false)
                });
            };
            Form.ignoreError = function (errorCodeList) {
                Form.ignoreErrors = errorCodeList;
            };
            Form.defaultError = function (proc) {
                Form.defaultErrorProc = proc;
            };
            Form.CLASS = "tui-form";
            Form.METHODS = ["GET", "POST", "PUT", "DELETE"];
            Form.STATUS = [
                "success",
                "notmodified",
                "error",
                "timeout",
                "abort",
                "parsererror"
            ];
            Form.ignoreErrors = null;
            Form.defaultErrorProc = null;
            return Form;
        })(_ctrl.Control);
        _ctrl.Form = Form;
        function form(param) {
            return tui.ctrl.control(param, Form);
        }
        _ctrl.form = form;
        tui.ctrl.registerInitCallback(Form.CLASS, form);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
/// <reference path="tui.ctrl.form.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Button = (function (_super) {
            __extends(Button, _super);
            function Button(el) {
                var _this = this;
                _super.call(this, "a", Button.CLASS, el);
                this._data = null;
                this._columnKeyMap = null;
                this._isMenu = false;
                this.disabled(this.disabled());
                this.selectable(false);
                this.exposeEvents("mousedown mouseup mousemove mouseenter mouseleave keydown keyup");
                var self = this;
                function openMenu() {
                    var pos = self.menuPos();
                    if (self.isMenu()) {
                        var menu = tui.ctrl.menu(self._data);
                        menu.show(self[0], pos || "Lb");
                        menu.on("select", function (data) {
                            self.fire("select", data);
                        });
                        menu.on("close", function () {
                            self.actived(false);
                        });
                        return;
                    }
                    var pop = tui.ctrl.popup();
                    var list = tui.ctrl.list();
                    list.consumeMouseWheelEvent(true);
                    list.rowcheckable(false);
                    pop.on("close", function () {
                        self.actived(false);
                    });
                    function doSelectItem(data) {
                        var item = list.activeItem();
                        var link = item[self._linkColumnKey];
                        if (link) {
                            pop.close();
                            window.location.href = link;
                            return;
                        }
                        var action = item["action"];
                        if (typeof action !== tui.undef) {
                            if (typeof action === "function") {
                                action();
                            }
                            pop.close();
                            self.focus();
                            self.fireClick(data["event"]);
                            return;
                        }
                        self.value(item[self._keyColumnKey]);
                        var targetElem = self.menuBind();
                        if (targetElem === null)
                            self.text(item[self._valueColumnKey]);
                        else {
                            targetElem = document.getElementById(targetElem);
                            if (targetElem) {
                                if (targetElem._ctrl) {
                                    if (typeof targetElem._ctrl.text === "function")
                                        targetElem._ctrl.text(item[self._valueColumnKey]);
                                }
                                else
                                    targetElem.innerHTML = item[self._valueColumnKey];
                            }
                        }
                        pop.close();
                        self.focus();
                        self.fireClick(data["event"]);
                    }
                    list.on("rowclick", function (data) {
                        doSelectItem(data);
                    });
                    list.on("keydown", function (data) {
                        if (data["event"].keyCode === 13) {
                            doSelectItem(data);
                        }
                    });
                    var testDiv = document.createElement("span");
                    testDiv.className = "tui-list-test-width-cell";
                    document.body.appendChild(testDiv);
                    var listWidth = self[0].offsetWidth;
                    for (var i = 0; i < self._data.length(); i++) {
                        var item = self._data.at(i);
                        testDiv.innerHTML = item[self._valueColumnKey];
                        if (testDiv.offsetWidth + 40 > listWidth) {
                            listWidth = testDiv.offsetWidth + 40;
                        }
                    }
                    document.body.removeChild(testDiv);
                    list[0].style.width = listWidth + "px";
                    list.data(self._data);
                    pop.show(list[0], self[0], pos || "Rb");
                    var items = self._data ? self._data.length() : 0;
                    if (items < 1)
                        items = 1;
                    else if (items > 15)
                        items = 15;
                    list[0].style.height = items * list.lineHeight() + 4 + "px";
                    list.refresh();
                    pop.refresh();
                    var val = self.value();
                    if (val && val.length > 0) {
                        list.activeRowByKey(val);
                        list.scrollTo(list.activerow());
                    }
                    list.focus();
                }
                $(this[0]).on("mousedown", function (e) {
                    if (_this.disabled())
                        return;
                    _this.actived(true);
                    var self = _this;
                    if (_this.data()) {
                        setTimeout(openMenu, 50);
                    }
                    else {
                        function releaseMouse(e) {
                            self.actived(false);
                            if (tui.isFireInside(self[0], e))
                                self.fireClick(e);
                            $(document).off("mouseup", releaseMouse);
                        }
                        $(document).on("mouseup", releaseMouse);
                    }
                });
                $(this[0]).on("keydown", function (e) {
                    if (_this.disabled())
                        return;
                    if (e.keyCode === 32) {
                        _this.actived(true);
                        e.preventDefault();
                    }
                    if (e.keyCode === 13) {
                        e.preventDefault();
                        if (_this.data()) {
                            _this.actived(true);
                            openMenu();
                        }
                        else {
                            e.type = "click";
                            setTimeout(function () {
                                _this.fireClick(e);
                            }, 100);
                        }
                    }
                });
                $(this[0]).on("keyup", function (e) {
                    if (_this.disabled())
                        return;
                    if (e.keyCode === 32) {
                        if (_this.data()) {
                            openMenu();
                        }
                        else {
                            _this.actived(false);
                            e.type = "click";
                            setTimeout(function () {
                                _this.fireClick(e);
                            }, 50);
                        }
                    }
                });
                var predefined = this.attr("data-data");
                if (predefined) {
                    predefined = eval("(" + predefined + ")");
                    this.data(predefined);
                }
                predefined = this.attr("data-menu");
                if (predefined) {
                    predefined = eval("(" + predefined + ")");
                    this.menu(predefined);
                }
            }
            Button.prototype.fireClick = function (e) {
                if (this.fire("click", { "ctrl": this[0], "event": e }) === false)
                    return;
                if (tui.fire(this.id(), { "ctrl": this[0], "event": e }) === false)
                    return;
                var formId = this.submitForm();
                if (formId) {
                    var form = tui.ctrl.form(formId);
                    form && form.submit();
                }
            };
            Button.prototype.submitForm = function (formId) {
                if (typeof formId === "string") {
                    this.attr("data-submit-form", formId);
                    return this;
                }
                else
                    return this.attr("data-submit-form");
            };
            Button.prototype.value = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-value", JSON.stringify(val));
                    return this;
                }
                else {
                    val = this.attr("data-value");
                    if (val === null) {
                        return null;
                    }
                    else {
                        try {
                            return eval("(" + val + ")");
                        }
                        catch (err) {
                            return null;
                        }
                    }
                }
            };
            Button.prototype.menuBind = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-menu-bind", val);
                    return this;
                }
                else
                    return this.attr("data-menu-bind");
            };
            Button.prototype.menuPos = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-menu-pos", val);
                    return this;
                }
                else
                    return this.attr("data-menu-pos");
            };
            Button.prototype.columnKey = function (key) {
                var val = this._columnKeyMap[key];
                if (typeof val === "number" && val >= 0)
                    return val;
                else
                    return key;
            };
            Button.prototype.isMenu = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-is-menu", val);
                    return this;
                }
                else
                    return this.is("data-is-menu");
            };
            Button.prototype.menu = function (data) {
                if (data) {
                    this.isMenu(true);
                    this.data(data);
                }
                else {
                    if (this.isMenu())
                        return this._data;
                    else
                        return null;
                }
            };
            Button.prototype.data = function (data) {
                if (data) {
                    var self = this;
                    if (data instanceof Array || data.data && data.data instanceof Array) {
                        data = new tui.ArrayProvider(data);
                    }
                    if (typeof data.length !== "function" || typeof data.sort !== "function" || typeof data.at !== "function" || typeof data.columnKeyMap !== "function") {
                        throw new Error("TUI Button: need a data provider.");
                    }
                    this._data = data;
                    if (data)
                        this._columnKeyMap = data.columnKeyMap();
                    else
                        this._columnKeyMap = {};
                    this._keyColumnKey = this.columnKey("key");
                    this._valueColumnKey = this.columnKey("value");
                    this._childrenColumnKey = this.columnKey("children");
                    this._linkColumnKey = this.columnKey("link");
                    return this;
                }
                else
                    return this._data;
            };
            Button.prototype.text = function (val) {
                if (typeof val !== tui.undef) {
                    $(this[0]).html(val);
                    return this;
                }
                else
                    return $(this[0]).html();
            };
            Button.prototype.html = function (val) {
                return this.text(val);
            };
            Button.prototype.disabled = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-disabled", val);
                    if (val)
                        this.removeAttr("tabIndex");
                    else
                        this.attr("tabIndex", "0");
                    return this;
                }
                else
                    return this.is("data-disabled");
            };
            Button.CLASS = "tui-button";
            return Button;
        })(ctrl.Control);
        ctrl.Button = Button;
        /**
         * Construct a button.
         * @param el {HTMLElement or element id or construct info}
         */
        function button(param) {
            return tui.ctrl.control(param, Button);
        }
        ctrl.button = button;
        tui.ctrl.registerInitCallback(Button.CLASS, button);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.button.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (_ctrl) {
        var Checkbox = (function (_super) {
            __extends(Checkbox, _super);
            function Checkbox(el) {
                var _this = this;
                _super.call(this, "a", Checkbox.CLASS, el);
                this.disabled(this.disabled());
                this.selectable(false);
                this.exposeEvents("mouseup mousedown mousemove mouseenter mouseleave keyup keydown");
                $(this[0]).on("mousedown", function (e) {
                    if (_this.disabled())
                        return;
                    _this[0].focus();
                    _this.actived(true);
                    var self = _this;
                    function releaseMouse(e) {
                        self.actived(false);
                        if (tui.isFireInside(self[0], e)) {
                            self.checked(!self.checked());
                            e.type = "click";
                            self.fireClick(e);
                        }
                        $(document).off("mouseup", releaseMouse);
                    }
                    $(document).on("mouseup", releaseMouse);
                });
                $(this[0]).on("keydown", function (e) {
                    if (_this.disabled())
                        return;
                    if (e.keyCode === 32) {
                        _this.actived(true);
                        e.preventDefault();
                    }
                    if (e.keyCode === 13) {
                        e.preventDefault();
                        e.type = "click";
                        _this.checked(!_this.checked());
                        setTimeout(function () {
                            _this.fireClick(e);
                        }, 100);
                    }
                });
                $(this[0]).on("keyup", function (e) {
                    if (_this.disabled())
                        return;
                    if (e.keyCode === 32) {
                        _this.actived(false);
                        _this.checked(!_this.checked());
                        e.type = "click";
                        setTimeout(function () {
                            _this.fireClick(e);
                        }, 50);
                    }
                });
            }
            Checkbox.prototype.fireClick = function (e) {
                if (this.fire("click", { "ctrl": this[0], "event": e }) === false)
                    return;
                if (tui.fire(this.id(), { "ctrl": this[0], "event": e }) === false)
                    return;
                var formId = this.submitForm();
                if (formId) {
                    var form = tui.ctrl.form(formId);
                    form && form.submit();
                }
            };
            Checkbox.prototype.checked = function (val) {
                if (typeof val === tui.undef) {
                    return _super.prototype.checked.call(this);
                }
                else {
                    _super.prototype.checked.call(this, !!val);
                    this.unNotifyGroup();
                    return this;
                }
            };
            Checkbox.prototype.triState = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-tri-state", val);
                }
                else
                    return this.is("data-tri-state");
            };
            Checkbox.prototype.text = function (val) {
                if (typeof val !== tui.undef) {
                    $(this[0]).html(val);
                    return this;
                }
                else
                    return $(this[0]).html();
            };
            Checkbox.prototype.group = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-group", val);
                    return this;
                }
                else
                    return this.attr("data-group");
            };
            Checkbox.prototype.value = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-value", JSON.stringify(val));
                    return this;
                }
                else {
                    val = this.attr("data-value");
                    if (val === null) {
                        return null;
                    }
                    else {
                        try {
                            return eval("(" + val + ")");
                        }
                        catch (err) {
                            return null;
                        }
                    }
                }
            };
            Checkbox.prototype.unNotifyGroup = function () {
                var groupName = this.group();
                if (groupName) {
                    $("." + Checkbox.CLASS + "[data-group='" + groupName + "']").each(function (index, elem) {
                        var ctrl = elem["_ctrl"];
                        if (ctrl && typeof ctrl.notify === "function") {
                            ctrl.notify(null);
                        }
                    });
                }
            };
            Checkbox.prototype.notify = function (message) {
                if (typeof message === "string") {
                    this.attr("data-tooltip", message);
                    this.addClass("tui-notify");
                }
                else if (message === null) {
                    this.attr("data-tooltip", "");
                    this.removeAttr("data-tooltip");
                    this.removeClass("tui-notify");
                }
            };
            Checkbox.prototype.disabled = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-disabled", val);
                    if (val)
                        this.removeAttr("tabIndex");
                    else
                        this.attr("tabIndex", "0");
                    return this;
                }
                else
                    return this.is("data-disabled");
            };
            Checkbox.prototype.submitForm = function (formId) {
                if (typeof formId === "string") {
                    this.attr("data-submit-form", formId);
                    return this;
                }
                else
                    return this.attr("data-submit-form");
            };
            Checkbox.CLASS = "tui-checkbox";
            return Checkbox;
        })(_ctrl.Control);
        _ctrl.Checkbox = Checkbox;
        /**
         * Construct a button.
         * @param el {HTMLElement or element id or construct info}
         */
        function checkbox(param) {
            return tui.ctrl.control(param, Checkbox);
        }
        _ctrl.checkbox = checkbox;
        tui.ctrl.registerInitCallback(Checkbox.CLASS, checkbox);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.button.ts" />
/// <reference path="tui.ctrl.checkbox.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (_ctrl) {
        var Radiobox = (function (_super) {
            __extends(Radiobox, _super);
            function Radiobox(el) {
                var _this = this;
                _super.call(this, "a", Radiobox.CLASS, el);
                this.disabled(this.disabled());
                this.selectable(false);
                this.exposeEvents("mouseup mousedown mousemove mouseenter mouseleave keyup keydown");
                $(this[0]).on("mousedown", function (e) {
                    if (_this.disabled())
                        return;
                    _this[0].focus();
                    _this.actived(true);
                    var self = _this;
                    function releaseMouse(e) {
                        self.actived(false);
                        if (tui.isFireInside(self[0], e)) {
                            self.checked(true);
                            e.type = "click";
                            self.fireClick(e);
                        }
                        $(document).off("mouseup", releaseMouse);
                    }
                    $(document).on("mouseup", releaseMouse);
                });
                $(this[0]).on("keydown", function (e) {
                    if (_this.disabled())
                        return;
                    if (e.keyCode === 32) {
                        _this.actived(true);
                        e.preventDefault();
                    }
                    if (e.keyCode === 13) {
                        e.preventDefault();
                        e.type = "click";
                        _this.checked(true);
                        setTimeout(function () {
                            _this.fireClick(e);
                        }, 100);
                    }
                });
                $(this[0]).on("keyup", function (e) {
                    if (_this.disabled())
                        return;
                    if (e.keyCode === 32) {
                        _this.actived(false);
                        _this.checked(true);
                        e.type = "click";
                        setTimeout(function () {
                            _this.fireClick(e);
                        }, 50);
                    }
                });
            }
            Radiobox.prototype.fireClick = function (e) {
                if (this.fire("click", { "ctrl": this[0], "event": e }) === false)
                    return;
                if (tui.fire(this.id(), { "ctrl": this[0], "event": e }) === false)
                    return;
                var formId = this.submitForm();
                if (formId) {
                    var form = tui.ctrl.form(formId);
                    form && form.submit();
                }
            };
            Radiobox.prototype.text = function (val) {
                if (typeof val !== tui.undef) {
                    $(this[0]).html(val);
                    return this;
                }
                else
                    return $(this[0]).html();
            };
            Radiobox.prototype.checked = function (val) {
                if (typeof val === tui.undef) {
                    return _super.prototype.checked.call(this);
                }
                else {
                    val = (!!val);
                    if (val) {
                        var groupName = this.group();
                        if (groupName) {
                            $("." + Radiobox.CLASS + "[data-group='" + groupName + "']").each(function (index, elem) {
                                var ctrl = elem["_ctrl"];
                                if (ctrl && typeof ctrl.checked === "function") {
                                    ctrl.checked(false);
                                }
                            });
                        }
                    }
                    _super.prototype.checked.call(this, val);
                    this.unNotifyGroup();
                    return this;
                }
            };
            Radiobox.prototype.group = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-group", val);
                    return this;
                }
                else
                    return this.attr("data-group");
            };
            Radiobox.prototype.value = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-value", JSON.stringify(val));
                    return this;
                }
                else {
                    val = this.attr("data-value");
                    if (val === null) {
                        return null;
                    }
                    else {
                        try {
                            return eval("(" + val + ")");
                        }
                        catch (err) {
                            return null;
                        }
                    }
                }
            };
            Radiobox.prototype.unNotifyGroup = function () {
                var groupName = this.group();
                if (groupName) {
                    $("." + Radiobox.CLASS + "[data-group='" + groupName + "']").each(function (index, elem) {
                        var ctrl = elem["_ctrl"];
                        if (ctrl && typeof ctrl.notify === "function") {
                            ctrl.notify(null);
                        }
                    });
                }
            };
            Radiobox.prototype.notify = function (message) {
                if (typeof message === "string") {
                    this.attr("data-tooltip", message);
                    this.addClass("tui-notify");
                }
                else if (message === null) {
                    this.attr("data-tooltip", "");
                    this.removeAttr("data-tooltip");
                    this.removeClass("tui-notify");
                }
            };
            Radiobox.prototype.disabled = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-disabled", val);
                    if (val)
                        this.removeAttr("tabIndex");
                    else
                        this.attr("tabIndex", "0");
                    return this;
                }
                else
                    return this.is("data-disabled");
            };
            Radiobox.prototype.submitForm = function (formId) {
                if (typeof formId === "string") {
                    this.attr("data-submit-form", formId);
                    return this;
                }
                else
                    return this.attr("data-submit-form");
            };
            Radiobox.CLASS = "tui-radiobox";
            return Radiobox;
        })(_ctrl.Control);
        _ctrl.Radiobox = Radiobox;
        /**
         * Construct a button.
         * @param el {HTMLElement or element id or construct info}
         */
        function radiobox(param) {
            return tui.ctrl.control(param, Radiobox);
        }
        _ctrl.radiobox = radiobox;
        tui.ctrl.registerInitCallback(Radiobox.CLASS, radiobox);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
/// <reference path="tui.time.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        function formatNumber(v, maxValue) {
            if (v < 0)
                v = 0;
            if (v > maxValue)
                v = maxValue;
            if (v < 10)
                return "0" + v;
            else
                return v + "";
        }
        var Calendar = (function (_super) {
            __extends(Calendar, _super);
            function Calendar(el) {
                var _this = this;
                _super.call(this, "div", Calendar.CLASS, el);
                this._time = tui.today();
                var self = this;
                this.attr("tabIndex", "0");
                this.selectable(false);
                this[0].innerHTML = "";
                this._tb = this[0].appendChild(document.createElement("table"));
                this._tb.cellPadding = "0";
                this._tb.cellSpacing = "0";
                this._tb.border = "0";
                var yearLine = this._tb.insertRow(-1);
                this._prevMonth = yearLine.insertCell(-1);
                this._prevMonth.className = "tui-prev-month-btn";
                this._prevYear = yearLine.insertCell(-1);
                this._prevYear.className = "tui-prev-year-btn";
                this._yearCell = yearLine.insertCell(-1);
                this._yearCell.colSpan = 3;
                this._nextYear = yearLine.insertCell(-1);
                this._nextYear.className = "tui-next-year-btn";
                this._nextMonth = yearLine.insertCell(-1);
                this._nextMonth.className = "tui-next-month-btn";
                for (var i = 0; i < 7; i++) {
                    var line = this._tb.insertRow(-1);
                    for (var j = 0; j < 7; j++) {
                        var cell = line.insertCell(-1);
                        if (i === 0) {
                            cell.className = "tui-week";
                            this.setText(i + 1, j, tui.str(Calendar._week[j]));
                        }
                    }
                }
                function elem(n) {
                    return document.createElement(n);
                }
                var timeDiv = elem("div");
                this._timeDiv = timeDiv;
                timeDiv.className = "tui-calendar-timebar";
                timeDiv.style.display = "none";
                var hourBox = elem("input");
                this._hourBox = hourBox;
                hourBox.type = "text";
                hourBox.className = "tui-calendar-timebox";
                hourBox._maxValue = 23;
                hourBox.maxLength = 2;
                hourBox._type = "hour";
                var minuteBox = elem("input");
                this._minuteBox = minuteBox;
                minuteBox.type = "text";
                minuteBox.className = "tui-calendar-timebox";
                minuteBox._maxValue = 59;
                minuteBox.maxLength = 2;
                minuteBox._type = "minute";
                var secondBox = elem("input");
                this._secondBox = secondBox;
                secondBox.type = "text";
                secondBox.className = "tui-calendar-timebox";
                secondBox._maxValue = 59;
                secondBox.maxLength = 2;
                secondBox._type = "second";
                function timeDown(e) {
                    var o = e.srcElement || e.target;
                    tui.cancelBubble(e);
                    var maxValue = o._maxValue;
                    var type = o._type;
                    var k = e.keyCode;
                    if (k === 37) {
                        if (o === minuteBox)
                            hourBox.focus();
                        else if (o === secondBox)
                            minuteBox.focus();
                    }
                    else if (k === 39) {
                        if (o === minuteBox)
                            secondBox.focus();
                        else if (o === hourBox)
                            minuteBox.focus();
                    }
                    else if (k === 38) {
                        var v = parseInt(o.value);
                        v++;
                        if (v > maxValue)
                            v = 0;
                        o.value = formatNumber(v, maxValue);
                        if (type === "hour")
                            self.hours(parseInt(o.value));
                        else if (type === "minute")
                            self.minutes(parseInt(o.value));
                        else
                            self.seconds(parseInt(o.value));
                        o.select();
                    }
                    else if (k === 40) {
                        var v = parseInt(o.value);
                        v--;
                        if (v < 0)
                            v = maxValue;
                        o.value = formatNumber(v, maxValue);
                        o.select();
                        if (type === "hour")
                            self.hours(parseInt(o.value));
                        else if (type === "minute")
                            self.minutes(parseInt(o.value));
                        else
                            self.seconds(parseInt(o.value));
                        o.select();
                    }
                    else if (k >= 48 && k <= 57) {
                        var v = k - 48;
                        var now = tui.today().getTime();
                        if (o._lastInputTime && (now - o._lastInputTime) < 1000)
                            o.value = formatNumber(parseInt(o.value.substr(1, 1)) * 10 + v, maxValue);
                        else
                            o.value = formatNumber(v, maxValue);
                        o._lastInputTime = now;
                        o.select();
                        if (type === "hour")
                            self.hours(parseInt(o.value));
                        else if (type === "minute")
                            self.minutes(parseInt(o.value));
                        else
                            self.seconds(parseInt(o.value));
                        o.select();
                    }
                    else if (k == 13)
                        self.fire("picked", { "ctrl": self[0], "event": e, "time": self.time() });
                    if (k !== 9)
                        return tui.cancelDefault(e);
                }
                function selectText(e) {
                    var o = e.srcElement || e.target;
                    setTimeout(function () {
                        o.select();
                    }, 0);
                }
                $(hourBox).on("keydown", timeDown);
                $(minuteBox).on("keydown", timeDown);
                $(secondBox).on("keydown", timeDown);
                $(hourBox).on("focus mousedown mouseup", selectText);
                $(minuteBox).on("focus mousedown mouseup", selectText);
                $(secondBox).on("focus mousedown mouseup", selectText);
                $(hourBox).on("contextmenu", tui.cancelDefault);
                $(minuteBox).on("contextmenu", tui.cancelDefault);
                $(secondBox).on("contextmenu", tui.cancelDefault);
                function createText(t) {
                    var txt = elem("span");
                    txt.style.verticalAlign = "middle";
                    txt.style.margin = "2px";
                    txt.innerHTML = t;
                    return txt;
                }
                var label = createText(tui.str("Choose Time"));
                timeDiv.appendChild(label);
                timeDiv.appendChild(hourBox);
                timeDiv.appendChild(createText(":"));
                timeDiv.appendChild(minuteBox);
                timeDiv.appendChild(createText(":"));
                timeDiv.appendChild(secondBox);
                var u = createText("<a class='tui-calendar-update'></a>");
                $(u).mousedown(function (e) {
                    var now = tui.today();
                    var newTime = new Date(self.year(), self.month() - 1, self.day(), now.getHours(), now.getMinutes(), now.getSeconds());
                    self.time(newTime);
                    return tui.cancelBubble(e);
                });
                timeDiv.appendChild(u);
                this[0].appendChild(timeDiv);
                $(this[0]).on("mousedown", function (e) {
                    if (tui.ffVer > 0)
                        _this.focus();
                    if (e.target.nodeName.toLowerCase() !== "td")
                        return;
                    var cell = e.target;
                    if ($(cell).hasClass("tui-prev-month-btn")) {
                        _this.prevMonth();
                    }
                    else if ($(cell).hasClass("tui-prev-year-btn")) {
                        _this.prevYear();
                    }
                    else if ($(cell).hasClass("tui-next-year-btn")) {
                        _this.nextYear();
                    }
                    else if ($(cell).hasClass("tui-next-month-btn")) {
                        _this.nextMonth();
                    }
                    else if (typeof cell["offsetMonth"] === "number") {
                        var d = parseInt(cell.innerHTML, 10);
                        var offset = cell["offsetMonth"];
                        if (offset < 0) {
                            var y = _this.year();
                            var m = _this.month();
                            if (m === 1) {
                                y--;
                                m = 12;
                            }
                            else {
                                m--;
                            }
                            _this.onPicked(y, m, d);
                        }
                        else if (offset > 0) {
                            var y = _this.year();
                            var m = _this.month();
                            if (m === 12) {
                                y++;
                                m = 1;
                            }
                            else {
                                m++;
                            }
                            _this.onPicked(y, m, d);
                        }
                        else if (offset === 0) {
                            _this.onPicked(_this.year(), _this.month(), d);
                        }
                    }
                });
                $(this[0]).on("click", function (e) {
                    if (e.target.nodeName.toLowerCase() !== "td")
                        return;
                    var cell = e.target;
                    if (typeof cell["offsetMonth"] === "number")
                        self.fire("picked", { "ctrl": _this[0], "event": e, "time": _this.time() });
                });
                $(this[0]).on("dblclick", function (e) {
                    if (e.target.nodeName.toLowerCase() !== "td")
                        return;
                    var cell = e.target;
                    if (typeof cell["offsetMonth"] === "number")
                        self.fire("dblpicked", { "ctrl": _this[0], "event": e, "time": _this.time() });
                });
                $(this[0]).on("keydown", function (e) {
                    var k = e.keyCode;
                    if ([13, 33, 34, 37, 38, 39, 40].indexOf(k) >= 0) {
                        if (k === 37) {
                            var tm = tui.dateAdd(_this._time, -1);
                            self.time(tm);
                        }
                        else if (k === 38) {
                            var tm = tui.dateAdd(_this._time, -7);
                            self.time(tm);
                        }
                        else if (k === 39) {
                            var tm = tui.dateAdd(_this._time, 1);
                            self.time(tm);
                        }
                        else if (k === 40) {
                            var tm = tui.dateAdd(_this._time, 7);
                            self.time(tm);
                        }
                        else if (k === 33) {
                            _this.prevMonth();
                        }
                        else if (k === 34) {
                            _this.nextMonth();
                        }
                        self.fire("picked", { "ctrl": _this[0], "event": e, "time": _this.time() });
                        return e.preventDefault();
                    }
                });
                // Set initial value
                var val = this.attr("data-value");
                if (val === null)
                    this.update();
                else {
                    var dateVal = tui.parseDate(val);
                    if (dateVal == null)
                        this.update();
                    else
                        this.time(dateVal);
                }
            }
            Calendar.prototype.setText = function (line, column, content) {
                var cell = (this._tb.rows[line].cells[column]);
                if (tui.ieVer > 0 && tui.ieVer < 9) {
                    cell.innerText = content;
                }
                else
                    cell.innerHTML = content;
            };
            Calendar.prototype.year = function (val) {
                if (typeof val === "number") {
                    if (this._time.getFullYear() !== val) {
                        this._time.setFullYear(val);
                        this.update();
                        this.fire("change", { "ctrl": this[0], "time": this.time() });
                    }
                    return this;
                }
                else
                    return this._time.getFullYear();
            };
            Calendar.prototype.day = function (val) {
                if (typeof val === "number") {
                    if (this._time.getDate() !== val) {
                        this._time.setDate(val);
                        this.update();
                        this.fire("change", { "ctrl": this[0], "time": this.time() });
                    }
                    return this;
                }
                else
                    return this._time.getDate();
            };
            Calendar.prototype.month = function (val) {
                if (typeof val === "number") {
                    if (this._time.getMonth() !== val - 1) {
                        this._time.setMonth(val - 1);
                        this.update();
                        this.fire("change", { "ctrl": this[0], "time": this.time() });
                    }
                    return this;
                }
                else
                    return this._time.getMonth() + 1;
            };
            Calendar.prototype.hours = function (val) {
                if (typeof val === "number") {
                    if (this._time.getHours() !== val) {
                        this._time.setHours(val);
                        this.update();
                        this.fire("change", { "ctrl": this[0], "time": this.time() });
                    }
                    return this;
                }
                else
                    return this._time.getHours();
            };
            Calendar.prototype.minutes = function (val) {
                if (typeof val === "number") {
                    if (this._time.getMinutes() !== val) {
                        this._time.setMinutes(val);
                        this.update();
                        this.fire("change", { "ctrl": this[0], "time": this.time() });
                    }
                    return this;
                }
                else
                    return this._time.getMinutes();
            };
            Calendar.prototype.seconds = function (val) {
                if (typeof val === "number") {
                    if (this._time.getSeconds() !== val) {
                        this._time.setSeconds(val);
                        this.update();
                        this.fire("change", { "ctrl": this[0], "time": this.time() });
                    }
                    return this;
                }
                else
                    return this._time.getSeconds();
            };
            Calendar.prototype.time = function (t) {
                if (t instanceof Date && t) {
                    var changed = false;
                    if (Math.floor(this._time.getTime() / 1000) !== Math.floor(t.getTime() / 1000))
                        changed = true;
                    this._time = t;
                    this.update();
                    changed && this.fire("change", { "ctrl": this[0], "time": this.time() });
                    return this;
                }
                else {
                    if (this.timepart()) {
                        return new Date(this._time.getTime());
                    }
                    else
                        return new Date(this._time.getFullYear(), this._time.getMonth(), this._time.getDate());
                }
            };
            Calendar.prototype.value = function (t) {
                if (t === null) {
                    this.time(tui.today());
                    return this;
                }
                return this.time(t);
            };
            Calendar.prototype.prevMonth = function () {
                var y = this.year(), m = this.month(), d = this.day();
                if (m === 1) {
                    y--;
                    m = 12;
                }
                else {
                    m--;
                }
                var newDate = new Date(y, m - 1, 1);
                if (d > tui.totalDaysOfMonth(newDate))
                    d = tui.totalDaysOfMonth(newDate);
                this.onPicked(y, m, d);
            };
            Calendar.prototype.nextMonth = function () {
                var y = this.year(), m = this.month(), d = this.day();
                if (m === 12) {
                    y++;
                    m = 1;
                }
                else {
                    m++;
                }
                var newDate = new Date(y, m - 1, 1);
                if (d > tui.totalDaysOfMonth(newDate))
                    d = tui.totalDaysOfMonth(newDate);
                this.onPicked(y, m, d);
            };
            Calendar.prototype.prevYear = function () {
                var y = this.year(), m = this.month(), d = this.day();
                y--;
                var newDate = new Date(y, m - 1, 1);
                if (d > tui.totalDaysOfMonth(newDate))
                    d = tui.totalDaysOfMonth(newDate);
                this.onPicked(y, m, d);
            };
            Calendar.prototype.nextYear = function () {
                var y = this.year(), m = this.month(), d = this.day();
                y++;
                var newDate = new Date(y, m - 1, 1);
                if (d > tui.totalDaysOfMonth(newDate))
                    d = tui.totalDaysOfMonth(newDate);
                this.onPicked(y, m, d);
            };
            Calendar.prototype.timepart = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-timepart", val);
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-timepart");
            };
            Calendar.prototype.onPicked = function (y, m, d) {
                var newDate = new Date(y, m - 1, d, this.hours(), this.minutes(), this.seconds());
                this.time(newDate);
            };
            Calendar.prototype.firstDay = function (date) {
                var y = date.getFullYear();
                var m = date.getMonth();
                return new Date(y, m, 1);
            };
            Calendar.prototype.update = function () {
                var today = tui.today();
                var firstWeek = this.firstDay(this._time).getDay();
                var daysOfMonth = tui.totalDaysOfMonth(this._time);
                var day = 0;
                this._yearCell.innerHTML = this.year() + " - " + this.month();
                for (var i = 0; i < 6; i++) {
                    for (var j = 0; j < 7; j++) {
                        var cell = this._tb.rows[i + 2].cells[j];
                        cell.className = "";
                        if (day === 0) {
                            if (j === firstWeek) {
                                day = 1;
                                cell.innerHTML = day + "";
                                cell.offsetMonth = 0;
                            }
                            else {
                                var preMonthDay = new Date(this.firstDay(this._time).valueOf() - ((firstWeek - j) * 1000 * 24 * 60 * 60));
                                cell.innerHTML = preMonthDay.getDate() + "";
                                cell.offsetMonth = -1;
                                $(cell).addClass("tui-prev-month");
                            }
                        }
                        else {
                            day++;
                            if (day <= daysOfMonth) {
                                cell.innerHTML = day + "";
                                cell.offsetMonth = 0;
                            }
                            else {
                                cell.innerHTML = (day - daysOfMonth) + "";
                                cell.offsetMonth = 1;
                                $(cell).addClass("tui-next-month");
                            }
                        }
                        if (day === this.day())
                            $(cell).addClass("tui-actived");
                        if (j === 0 || j === 6)
                            $(cell).addClass("tui-weekend");
                        if (this.year() === today.getFullYear() && this.month() === (today.getMonth() + 1) && day === today.getDate()) {
                            $(cell).addClass("tui-today");
                        }
                    }
                }
                if (this.timepart()) {
                    this._timeDiv.style.display = "";
                    this._hourBox.value = formatNumber(this.hours(), 23);
                    this._minuteBox.value = formatNumber(this.minutes(), 59);
                    this._secondBox.value = formatNumber(this.seconds(), 59);
                }
                else {
                    this._timeDiv.style.display = "none";
                }
            };
            Calendar.prototype.refresh = function () {
                this.update();
            };
            Calendar.CLASS = "tui-calendar";
            Calendar._week = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
            return Calendar;
        })(ctrl.Control);
        ctrl.Calendar = Calendar;
        /**
         * Construct a calendar.
         * @param el {HTMLElement or element id or construct info}
         */
        function calendar(param) {
            return tui.ctrl.control(param, Calendar);
        }
        ctrl.calendar = calendar;
        tui.ctrl.registerInitCallback(Calendar.CLASS, calendar);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var _currentPopup = null;
        function closeAllPopup() {
            var pop = _currentPopup;
            while (pop) {
                if (pop.parent())
                    pop = pop.parent();
                else {
                    pop.close();
                    pop = _currentPopup;
                }
            }
        }
        var Popup = (function (_super) {
            __extends(Popup, _super);
            function Popup() {
                _super.call(this, "div", Popup.CLASS, null);
                this._position = null;
                this._bindElem = null;
                this._body = document.body || document.getElementsByTagName("BODY")[0];
                this._parent = null;
                this._parentPopup = null;
                this._childPopup = null;
                this._checkInterval = null;
                this._showing = false;
                this._owner = null;
            }
            Popup.prototype.getParentPopup = function (elem) {
                var pop = _currentPopup;
                while (pop) {
                    if (pop.isPosterity(elem))
                        return pop;
                    else
                        pop = pop.parent();
                }
                return pop;
            };
            Popup.prototype.isShowing = function () {
                return this._showing;
            };
            Popup.prototype.owner = function (elem) {
                if (elem) {
                    this._owner = elem;
                    return this;
                }
                else
                    return this._owner;
            };
            Popup.prototype.show = function (content, param, bindType) {
                var _this = this;
                if (this._showing)
                    return;
                this._showing = true;
                if (typeof param === "string")
                    param = document.getElementById(param);
                var elem = null;
                if (param && param.nodeName && typeof bindType === "string") {
                    elem = this.elem("div", Popup.CLASS);
                    this._bindElem = param;
                    this._bindType = bindType;
                }
                else if (param && typeof param.x === "number" && typeof param.y === "number") {
                    elem = this.elem("div", Popup.CLASS);
                    this._position = param;
                    this._bindType = "LT";
                }
                else if (typeof param === tui.undef) {
                    elem = this.elem("div", Popup.CLASS);
                    this._position = { x: 0, y: 0 };
                    this._bindType = "LT";
                }
                if (elem) {
                    if (this._bindElem) {
                        this._parentPopup = this.getParentPopup(this._bindElem);
                        if (this._parentPopup) {
                            this._parentPopup.closeChild();
                            this._parentPopup.child(this);
                            this.parent(this._parentPopup);
                            this._parent = this._parentPopup[0];
                        }
                        else {
                            closeAllPopup();
                            this._parent = this._body;
                        }
                    }
                    else {
                        closeAllPopup();
                        this._parent = this._body;
                    }
                    _currentPopup = this;
                    elem.setAttribute("tabIndex", "-1");
                    this._parent.appendChild(elem);
                    if (this.owner())
                        this.owner().focus();
                    else
                        elem.focus();
                    if (typeof content === "string") {
                        elem.innerHTML = content;
                    }
                    else if (content && content.nodeName) {
                        elem.appendChild(content);
                    }
                    tui.ctrl.initCtrls(elem);
                    this.refresh();
                    if (this._bindElem) {
                        var pos = tui.fixedPosition(this._bindElem);
                        this._checkInterval = setInterval(function () {
                            var currentPos = tui.fixedPosition(_this._bindElem);
                            if (currentPos.x !== pos.x || currentPos.y !== pos.y) {
                                _this.close();
                            }
                        }, 100);
                    }
                }
            };
            Popup.prototype.close = function () {
                if (!this._showing)
                    return;
                this.closeChild();
                if (this._checkInterval) {
                    clearInterval(this._checkInterval);
                    this._checkInterval = null;
                }
                try {
                    this._parent.removeChild(this[0]);
                }
                catch (e) {
                }
                _currentPopup = this.parent();
                this.parent(null);
                if (_currentPopup)
                    _currentPopup.child(null);
                this.fire("close", {});
                this._showing = false;
            };
            Popup.prototype.closeChild = function () {
                if (this._childPopup) {
                    this._childPopup.close();
                    this._childPopup = null;
                }
            };
            Popup.prototype.parent = function (pop) {
                if (typeof pop !== tui.undef) {
                    this._parentPopup = pop;
                }
                return this._parentPopup;
            };
            Popup.prototype.child = function (pop) {
                if (typeof pop !== tui.undef) {
                    this._childPopup = pop;
                }
                return this._childPopup;
            };
            Popup.prototype.refresh = function () {
                if (!this[0])
                    return;
                var elem = this[0];
                var cw = tui.windowSize().width;
                var ch = tui.windowSize().height;
                var sw = elem.offsetWidth;
                var sh = elem.offsetHeight;
                var box = { x: 0, y: 0, w: 0, h: 0 };
                var pos = { x: 0, y: 0 };
                if (this._position) {
                    box = this._position;
                    box.w = 0;
                    box.h = 0;
                }
                else if (this._bindElem) {
                    box = tui.fixedPosition(this._bindElem);
                    box.w = this._bindElem.offsetWidth;
                    box.h = this._bindElem.offsetHeight;
                }
                // lower case letter means 'next to', upper case letter means 'align to'
                var compute = {
                    "l": function () {
                        pos.x = box.x - sw;
                        if (pos.x < 2)
                            pos.x = box.x + box.w;
                    },
                    "r": function () {
                        pos.x = box.x + box.w;
                        if (pos.x + sw > cw - 2)
                            pos.x = box.x - sw;
                    },
                    "t": function () {
                        pos.y = box.y - sh;
                        if (pos.y < 2)
                            pos.y = box.y + box.h;
                    },
                    "b": function () {
                        pos.y = box.y + box.h;
                        if (pos.y + sh > ch - 2)
                            pos.y = box.y - sh;
                    },
                    "L": function () {
                        pos.x = box.x;
                        if (pos.x + sw > cw - 2)
                            pos.x = box.x + box.w - sw;
                    },
                    "R": function () {
                        pos.x = box.x + box.w - sw;
                        if (pos.x < 2)
                            pos.x = box.x;
                    },
                    "T": function () {
                        pos.y = box.y;
                        if (pos.y + sh > ch - 2)
                            pos.y = box.y + box.h - sh;
                    },
                    "B": function () {
                        pos.y = box.y + box.h - sh;
                        if (pos.y < 2)
                            pos.y = box.y;
                    }
                };
                compute[this._bindType.substring(0, 1)](); // parse x
                compute[this._bindType.substring(1, 2)](); // parse y
                if (pos.x > cw - 2)
                    pos.x = cw - 2;
                if (pos.x < 2)
                    pos.x = 2;
                if (pos.y > ch - 2)
                    pos.y = ch - 2;
                if (pos.y < 2)
                    pos.y = 2;
                elem.style.left = pos.x + "px";
                elem.style.top = pos.y + "px";
            };
            Popup.CLASS = "tui-popup";
            return Popup;
        })(ctrl.Control);
        ctrl.Popup = Popup;
        function checkPopup(e) {
            setTimeout(function () {
                var obj = document.activeElement;
                while (_currentPopup) {
                    if (_currentPopup.isPosterity(obj) || _currentPopup.owner() === obj)
                        return;
                    else
                        _currentPopup.close();
                }
            }, 30);
        }
        ctrl.checkPopup = checkPopup;
        $(document).on("focus mousedown keydown", checkPopup);
        tui.on("#tui.check.popup", checkPopup);
        $(window).scroll(function () {
            closeAllPopup();
        });
        /**
         * Construct a button.
         * @param el {HTMLElement or element id or construct info}
         */
        function popup() {
            return new Popup();
        }
        ctrl.popup = popup;
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var _dialogStack = [];
        var _mask = document.createElement("div");
        _mask.className = "tui-dialog-mask";
        _mask.setAttribute("unselectable", "on");
        var mousewheelevt = (/Firefox/i.test(navigator.userAgent)) ? "DOMMouseScroll" : "mousewheel";
        $(_mask).on(mousewheelevt + " selectstart", function (ev) {
            ev.stopPropagation();
            ev.preventDefault();
        });
        function reorder() {
            if (_mask.parentNode !== null) {
                _mask.parentNode.removeChild(_mask);
            }
            if (_dialogStack.length > 0) {
                document.body.insertBefore(_mask, _dialogStack[_dialogStack.length - 1].elem());
            }
            else {
            }
        }
        function push(dlg) {
            _dialogStack.push(dlg);
            document.body.appendChild(dlg.elem());
            reorder();
        }
        function remove(dlg) {
            var index = _dialogStack.indexOf(dlg);
            if (index >= 0) {
                _dialogStack.splice(index, 1);
            }
            document.body.removeChild(dlg.elem());
            reorder();
        }
        function getParent(dlg) {
            var index = _dialogStack.indexOf(dlg);
            if (index > 0) {
                _dialogStack[index - 1];
            }
            else
                return null;
        }
        var Dialog = (function (_super) {
            __extends(Dialog, _super);
            function Dialog() {
                _super.call(this, "div", Dialog.CLASS, null);
                this._resourceElement = null;
                this._placeHolder = null;
                this._hiddenOriginal = false;
                this._isMoved = false;
                this._isInitialize = true;
                this._titleText = null;
                this._noRefresh = false;
                this._useEsc = true;
                this._sizeTimer = null;
                this._originScrollHeight = null;
                this._originScrollWidth = null;
            }
            /**
             * Show HTML content
             */
            Dialog.prototype.showContent = function (content, title, buttons) {
                if (this[0])
                    return this;
                this._resourceElement = null;
                this._placeHolder = null;
                return this.showElement(tui.toElement(content, true), title, buttons);
            };
            /**
             * Show resource form <script type="text/html"> ... </script>
             */
            Dialog.prototype.showResource = function (resourceId, title, buttons) {
                if (this[0])
                    return this;
                var elem = document.getElementById(resourceId);
                if (!elem) {
                    throw new Error("Resource id not found: " + resourceId);
                }
                return this.showContent(elem.innerHTML, title, buttons);
            };
            /**
             * Show a element from page, put the element into the dialog,
             * when dialog closed the specify element will be put back to its original place.
             */
            Dialog.prototype.showElement = function (elem, title, buttons) {
                var _this = this;
                if (this[0])
                    return this;
                if (typeof elem === "string") {
                    var elemId = elem;
                    elem = document.getElementById(elem);
                }
                if (!elem) {
                    throw new Error("Invalid element!");
                }
                this._resourceElement = elem;
                if ($(elem).hasClass("tui-hidden"))
                    this._hiddenOriginal = true;
                else
                    this._hiddenOriginal = false;
                if (elem.parentNode !== null) {
                    this._placeHolder = document.createElement("span");
                    this._placeHolder.className = "tui-hidden";
                    elem.parentNode.insertBefore(this._placeHolder, elem);
                }
                else
                    this._placeHolder = null;
                // Temporary inhibit refresh to prevent unexpected calculation
                this._noRefresh = true;
                this.elem("div", Dialog.CLASS);
                this.attr("tabIndex", "-1");
                this._titleDiv = document.createElement("div");
                this._titleDiv.className = "tui-dlg-title-bar";
                this._titleDiv.setAttribute("unselectable", "on");
                this._titleDiv.onselectstart = function () {
                    return false;
                };
                this[0].appendChild(this._titleDiv);
                this._closeIcon = document.createElement("span");
                this._closeIcon.className = "tui-dlg-close";
                this._titleDiv.appendChild(this._closeIcon);
                this._contentDiv = document.createElement("div");
                this[0].appendChild(this._contentDiv);
                this._buttonDiv = document.createElement("div");
                this._buttonDiv.className = "tui-dlg-btn-bar";
                this[0].appendChild(this._buttonDiv);
                var tt = "";
                if (typeof title === "string") {
                    tt = title;
                }
                else {
                    if (elem.title) {
                        tt = elem.title;
                    }
                }
                this.title(tt);
                this._contentDiv.appendChild(elem);
                $(elem).removeClass("tui-hidden");
                var self = this;
                if (buttons && typeof buttons.length === "number") {
                    for (var i = 0; i < buttons.length; i++) {
                        this.insertButton(buttons[i]);
                    }
                }
                else {
                    this.insertButton({
                        name: tui.str("Ok"),
                        func: function (data) {
                            self.close();
                        }
                    });
                }
                // Add to document
                push(this);
                // Convert all child elements into tui controls
                tui.ctrl.initCtrls(elem);
                this._isInitialize = true;
                this._isMoved = false;
                $(this._closeIcon).on("click", function () {
                    _this.close();
                });
                $(this._titleDiv).on("mousedown", function (e) {
                    if (e.target === _this._closeIcon)
                        return;
                    var dialogX = _this[0].offsetLeft;
                    var dialogY = _this[0].offsetTop;
                    var beginX = e.clientX;
                    var beginY = e.clientY;
                    var winSize = { width: _mask.offsetWidth, height: _mask.offsetHeight };
                    tui.mask();
                    function onMoveEnd(e) {
                        tui.unmask();
                        $(document).off("mousemove", onMove);
                        $(document).off("mouseup", onMoveEnd);
                    }
                    function onMove(e) {
                        var l = dialogX + e.clientX - beginX;
                        var t = dialogY + e.clientY - beginY;
                        if (l > winSize.width - self[0].offsetWidth)
                            l = winSize.width - self[0].offsetWidth;
                        if (l < 0)
                            l = 0;
                        if (t > winSize.height - self[0].offsetHeight)
                            t = winSize.height - self[0].offsetHeight;
                        if (t < 0)
                            t = 0;
                        self[0].style.left = l + "px";
                        self[0].style.top = t + "px";
                        self._isMoved = true;
                    }
                    $(document).on("mousemove", onMove);
                    $(document).on("mouseup", onMoveEnd);
                });
                $(this[0]).on(mousewheelevt, function (ev) {
                    ev.stopPropagation();
                    ev.preventDefault();
                });
                // After initialization finished preform refresh now.
                this._noRefresh = false;
                this[0].style.left = "0px";
                this[0].style.top = "0px";
                this.limitSize();
                this.refresh();
                this[0].focus();
                this.fire("open");
                this._sizeTimer = setInterval(function () {
                    if (self._contentDiv.scrollHeight !== self._originScrollHeight || self._contentDiv.scrollWidth !== self._originScrollWidth) {
                        self.refresh();
                        self._originScrollHeight = self._contentDiv.scrollHeight;
                        self._originScrollWidth = self._contentDiv.scrollWidth;
                    }
                }, 100);
                return this;
            };
            Dialog.prototype.limitSize = function () {
                var _this = this;
                setTimeout(function () {
                    _this._contentDiv.style.maxHeight = "";
                    _this[0].style.maxWidth = _mask.offsetWidth + "px";
                    _this[0].style.maxHeight = _mask.offsetHeight + "px";
                    _this._contentDiv.style.maxHeight = _mask.offsetHeight - _this._titleDiv.offsetHeight - _this._buttonDiv.offsetHeight - $(_this._contentDiv).outerHeight() + $(_this._contentDiv).height() + "px";
                    _this._contentDiv.style.maxWidth = _mask.offsetWidth - $(_this._contentDiv).outerWidth() + $(_this._contentDiv).width() + "px";
                    _this.refresh();
                }, 0);
            };
            Dialog.prototype.insertButton = function (btn, index) {
                if (!this[0])
                    return null;
                var button = tui.ctrl.button();
                button.text(btn.name);
                btn.id && button.id(btn.id);
                btn.cls && button.addClass(btn.cls);
                btn.func && button.on("click", btn.func);
                if (typeof index === "number" && !isNaN(index)) {
                    var refButton = this._buttonDiv.childNodes[index];
                    if (refButton)
                        this._buttonDiv.insertBefore(button.elem(), refButton);
                    else
                        this._buttonDiv.appendChild(button.elem());
                }
                else {
                    this._buttonDiv.appendChild(button.elem());
                }
                this.refresh();
                return button;
            };
            Dialog.prototype.removeButton = function (btn) {
                if (!this[0])
                    return;
                var refButton;
                if (typeof btn === "number") {
                    refButton = this._buttonDiv.childNodes[btn];
                }
                else if (btn instanceof ctrl.Button) {
                    refButton = btn.elem();
                }
                this._buttonDiv.removeChild(refButton);
            };
            Dialog.prototype.button = function (index) {
                if (!this[0])
                    return null;
                var refButton = this._buttonDiv.childNodes[index];
                if (refButton) {
                    return tui.ctrl.button(refButton);
                }
                else
                    return null;
            };
            Dialog.prototype.removeAllButtons = function () {
                if (!this[0])
                    return;
                this._buttonDiv.innerHTML = "";
            };
            Dialog.prototype.useesc = function (val) {
                if (typeof val === "boolean") {
                    this._useEsc = val;
                    this.title(this.title());
                }
                else {
                    return this._useEsc;
                }
            };
            Dialog.prototype.title = function (t) {
                if (typeof t === "string") {
                    if (!this[0])
                        return this;
                    if (this._closeIcon.parentNode)
                        this._closeIcon.parentNode.removeChild(this._closeIcon);
                    this._titleDiv.innerHTML = t;
                    if (this._useEsc)
                        this._titleDiv.appendChild(this._closeIcon);
                    this._titleText = t;
                    this.refresh();
                    return this;
                }
                else {
                    if (!this[0])
                        return null;
                    return this._titleText;
                }
            };
            Dialog.prototype.close = function () {
                if (!this[0])
                    return;
                clearInterval(this._sizeTimer);
                remove(this);
                this.elem(null);
                this._titleDiv = null;
                this._contentDiv = null;
                this._buttonDiv = null;
                this._closeIcon = null;
                this._titleText = null;
                if (this._placeHolder) {
                    this._placeHolder.parentNode && this._placeHolder.parentNode.insertBefore(this._resourceElement, this._placeHolder);
                    tui.removeNode(this._placeHolder);
                    this._placeHolder = null;
                    if (this._hiddenOriginal)
                        $(this._resourceElement).addClass("tui-hidden");
                    this._resourceElement = null;
                }
                this.fire("close");
            };
            Dialog.prototype.refresh = function () {
                if (!this[0])
                    return;
                if (this._noRefresh)
                    return;
                // Change position
                var winSize = { width: _mask.offsetWidth, height: _mask.offsetHeight };
                var box = {
                    left: this[0].offsetLeft,
                    top: this[0].offsetTop,
                    width: this[0].offsetWidth,
                    height: this[0].offsetHeight
                };
                if (this._isInitialize) {
                    var parent = getParent(this);
                    var centX, centY;
                    if (parent) {
                        var e = parent.elem();
                        centX = e.offsetLeft + e.offsetWidth / 2;
                        centY = e.offsetTop + e.offsetHeight / 2;
                        this._isMoved = true;
                    }
                    else {
                        centX = winSize.width / 2;
                        centY = winSize.height / 2;
                        this._isMoved = false;
                    }
                    box.left = centX - box.width / 2;
                    box.top = centY - box.height / 2;
                    this._isInitialize = false;
                }
                else {
                    if (!this._isMoved) {
                        box.left = (winSize.width - box.width) / 2;
                        box.top = (winSize.height - box.height) / 2;
                    }
                }
                if (box.left + box.width > winSize.width)
                    box.left = winSize.width - box.width;
                if (box.top + box.height > winSize.height)
                    box.top = winSize.height - box.height;
                if (box.left < 0)
                    box.left = 0;
                if (box.top < 0)
                    box.top = 0;
                this[0].style.left = box.left + "px";
                this[0].style.top = box.top + "px";
            };
            Dialog.CLASS = "tui-dialog";
            return Dialog;
        })(ctrl.Control);
        ctrl.Dialog = Dialog;
        /**
         * Construct a button.
         * @param el {HTMLElement or element id or construct info}
         */
        function dialog() {
            return tui.ctrl.control(null, Dialog);
        }
        ctrl.dialog = dialog;
        $(document).on("keydown", function (e) {
            var k = e.keyCode;
            if (_dialogStack.length <= 0)
                return;
            var dlg = _dialogStack[_dialogStack.length - 1];
            if (k === 27) {
                dlg.useesc() && dlg.close();
            }
            else if (k === 9) {
                setTimeout(function () {
                    if (!dlg.isPosterity(document.activeElement)) {
                        dlg.focus();
                    }
                }, 0);
            }
        });
        $(window).resize(function () {
            for (var i = 0; i < _dialogStack.length; i++) {
                _dialogStack[i].limitSize();
                _dialogStack[i].refresh();
            }
        });
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
    function msgbox(message, title) {
        var dlg = tui.ctrl.dialog();
        var wrap = document.createElement("div");
        wrap.className = "tui-dlg-msg";
        wrap.innerHTML = message;
        dlg.showElement(wrap, title);
        return dlg;
    }
    tui.msgbox = msgbox;
    function infobox(message, title) {
        var dlg = tui.ctrl.dialog();
        var wrap = document.createElement("div");
        wrap.className = "tui-dlg-warp tui-dlg-info";
        wrap.innerHTML = message;
        dlg.showElement(wrap, title);
        return dlg;
    }
    tui.infobox = infobox;
    function okbox(message, title) {
        var dlg = tui.ctrl.dialog();
        var wrap = document.createElement("div");
        wrap.className = "tui-dlg-warp tui-dlg-ok";
        wrap.innerHTML = message;
        dlg.showElement(wrap, title);
        return dlg;
    }
    tui.okbox = okbox;
    function errbox(message, title) {
        var dlg = tui.ctrl.dialog();
        var wrap = document.createElement("div");
        wrap.className = "tui-dlg-warp tui-dlg-err";
        wrap.innerHTML = message;
        dlg.showElement(wrap, title);
        return dlg;
    }
    tui.errbox = errbox;
    function warnbox(message, title) {
        var dlg = tui.ctrl.dialog();
        var wrap = document.createElement("div");
        wrap.className = "tui-dlg-warp tui-dlg-warn";
        wrap.innerHTML = message;
        dlg.showElement(wrap, title);
        return dlg;
    }
    tui.warnbox = warnbox;
    function askbox(message, title, callback) {
        var dlg = tui.ctrl.dialog();
        var wrap = document.createElement("div");
        wrap.className = "tui-dlg-warp tui-dlg-ask";
        wrap.innerHTML = message;
        var result = false;
        dlg.showElement(wrap, title, [
            {
                name: tui.str("Ok"),
                func: function () {
                    result = true;
                    dlg.close();
                }
            },
            {
                name: tui.str("Cancel"),
                func: function () {
                    dlg.close();
                }
            }
        ]);
        dlg.on("close", function () {
            if (typeof callback === "function")
                callback(result);
        });
        return dlg;
    }
    tui.askbox = askbox;
    function waitbox(message, cancelProc) {
        if (cancelProc === void 0) { cancelProc = null; }
        var dlg = tui.ctrl.dialog();
        var wrap = document.createElement("div");
        wrap.className = "tui-dlg-warp tui-dlg-wait";
        wrap.innerHTML = message;
        if (typeof cancelProc === "function")
            dlg.showElement(wrap, null, [{
                name: tui.str("Cancel"),
                func: function () {
                    dlg.close();
                    cancelProc();
                }
            }]);
        else {
            dlg.showElement(wrap, null, []);
        }
        dlg.useesc(false);
        return dlg;
    }
    tui.waitbox = waitbox;
    function loadHTML(url, elem, completeCallback, async, method, data) {
        if (async === void 0) { async = true; }
        tui.loadURL(url, function (status, jqXHR) {
            if (typeof completeCallback === "function" && completeCallback(status, jqXHR) === false) {
                return;
            }
            if (status === "success") {
                var matched = /<body[^>]*>((?:.|[\r\n])*)<\/body>/gim.exec(jqXHR.responseText);
                if (matched != null)
                    elem.innerHTML = matched[1];
                else
                    elem.innerHTML = jqXHR.responseText;
                tui.ctrl.initCtrls(elem);
            }
            else {
                tui.errbox(tui.str(status) + " (" + jqXHR.status + ")", tui.str("Failed"));
            }
        }, async, method, data);
    }
    tui.loadHTML = loadHTML;
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Scrollbar = (function (_super) {
            __extends(Scrollbar, _super);
            function Scrollbar(el) {
                var _this = this;
                _super.call(this, "span", Scrollbar.CLASS, el);
                this._btnThumb = null;
                this._btnHead = null;
                this._btnFoot = null;
                var self = this;
                this.attr("unselectable", "on");
                this[0].innerHTML = "";
                this._btnHead = document.createElement("span");
                this._btnHead.className = "tui-scroll-head";
                this[0].appendChild(this._btnHead);
                this._btnThumb = document.createElement("span");
                this._btnThumb.className = "tui-scroll-thumb";
                $(this._btnThumb).attr("unselectable", "on");
                this[0].appendChild(this._btnThumb);
                this._btnFoot = document.createElement("span");
                this._btnFoot.className = "tui-scroll-foot";
                this[0].appendChild(this._btnFoot);
                var scrollTimer = null;
                var scrollInterval = null;
                var moveParam = null;
                function stopMove() {
                    if (scrollTimer) {
                        clearTimeout(scrollTimer);
                        scrollTimer = null;
                    }
                    if (scrollInterval) {
                        clearInterval(scrollInterval);
                        scrollInterval = null;
                    }
                }
                function moveThumb() {
                    var val = self.value();
                    var total = self.total();
                    var achieve = false;
                    moveParam.pos = Math.round(moveParam.pos);
                    moveParam.step = Math.round(moveParam.step);
                    if (val === moveParam.pos)
                        return;
                    if (!moveParam.isIncrease) {
                        val -= moveParam.step;
                        if (val - (moveParam.isPage ? moveParam.step / 2 : 0) <= moveParam.pos || val <= 0) {
                            achieve = true;
                            if (val < 0)
                                val = 0;
                            stopMove();
                        }
                        self.value(val);
                    }
                    else {
                        val += moveParam.step;
                        if (val + (moveParam.isPage ? moveParam.step / 2 : 0) >= moveParam.pos || val >= total) {
                            achieve = true;
                            if (val > total)
                                val = total;
                            stopMove();
                        }
                        self.value(val);
                    }
                    self.fire("scroll", { value: self.value(), type: "mousedown" });
                    return achieve;
                }
                function releaseButton(e) {
                    stopMove();
                    $(self._btnHead).removeClass("tui-actived");
                    $(self._btnFoot).removeClass("tui-actived");
                    $(tui.unmask()).off("mouseup", releaseButton);
                    $(document).off("mouseup", releaseButton);
                }
                ;
                $(this[0]).mousedown(function (e) {
                    tui.fire("#tui.check.popup");
                    // Should check which target object was triggered.
                    if (!tui.isLButton(e)) {
                        return;
                    }
                    var obj = e.target;
                    if (obj !== self[0]) {
                        e.stopPropagation();
                        e.preventDefault();
                        return;
                    }
                    if (_this.total() <= 0)
                        return;
                    var dir = self.direction();
                    var pos, thumbLen;
                    if (dir === "vertical") {
                        pos = (typeof e.offsetY === "number" ? e.offsetY : e["originalEvent"].layerY);
                        thumbLen = _this._btnThumb.offsetHeight;
                    }
                    else {
                        pos = (typeof e.offsetX === "number" ? e.offsetX : e["originalEvent"].layerX);
                        thumbLen = _this._btnThumb.offsetWidth;
                    }
                    var v = _this.posToValue(pos - thumbLen / 2);
                    moveParam = { pos: v, step: self.page(), isIncrease: v > self.value(), isPage: true };
                    if (!moveThumb()) {
                        scrollTimer = setTimeout(function () {
                            scrollTimer = null;
                            scrollInterval = setInterval(moveThumb, 20);
                        }, 300);
                        $(tui.mask()).on("mouseup", releaseButton);
                        $(document).on("mouseup", releaseButton);
                    }
                    e.stopPropagation();
                    e.preventDefault();
                    return false;
                });
                $(this._btnHead).mousedown(function (e) {
                    if (!tui.isLButton(e))
                        return;
                    if (self.total() <= 0)
                        return;
                    $(self._btnHead).addClass("tui-actived");
                    moveParam = { pos: 0, step: self.step(), isIncrease: false, isPage: false };
                    if (!moveThumb()) {
                        scrollTimer = setTimeout(function () {
                            scrollTimer = null;
                            scrollInterval = setInterval(moveThumb, 20);
                        }, 300);
                        $(tui.mask()).on("mouseup", releaseButton);
                        $(document).on("mouseup", releaseButton);
                    }
                });
                $(this._btnFoot).mousedown(function (e) {
                    if (!tui.isLButton(e))
                        return;
                    if (self.total() <= 0)
                        return;
                    $(self._btnFoot).addClass("tui-actived");
                    moveParam = { pos: self.total(), step: self.step(), isIncrease: true, isPage: false };
                    if (!moveThumb()) {
                        scrollTimer = setTimeout(function () {
                            scrollTimer = null;
                            scrollInterval = setInterval(moveThumb, 20);
                        }, 300);
                        $(tui.mask()).on("mouseup", releaseButton);
                        $(document).on("mouseup", releaseButton);
                    }
                });
                var mousewheelevt = (/Firefox/i.test(navigator.userAgent)) ? "DOMMouseScroll" : "mousewheel";
                $(this[0]).on(mousewheelevt, function (e) {
                    var ev = e.originalEvent;
                    var delta = ev.detail ? ev.detail * (-120) : ev.wheelDelta;
                    //delta returns +120 when wheel is scrolled up, -120 when scrolled down
                    var scrollSize = (Math.round(self.page() / 2) > self.step() ? Math.round(self.page() / 2) : self.step());
                    var oldValue = self.value();
                    if (delta <= -120) {
                        self.value(self.value() + scrollSize);
                    }
                    else {
                        self.value(self.value() - scrollSize);
                    }
                    if (oldValue !== self.value())
                        self.fire("scroll", { value: self.value(), type: "mousewheel" });
                    e.stopPropagation();
                    e.preventDefault();
                });
                var beginX = 0, beginY = 0, beginLeft = 0, beginTop = 0;
                function dragThumb(e) {
                    var diff = 0;
                    var oldValue = self.value();
                    var pos;
                    if (self.direction() === "vertical") {
                        diff = e.clientY - beginY;
                        pos = beginTop + diff;
                    }
                    else {
                        diff = e.clientX - beginX;
                        pos = beginLeft + diff;
                    }
                    self.value(self.posToValue(pos));
                    if (oldValue !== self.value()) {
                        self.fire("scroll", { value: self.value(), type: "drag" });
                    }
                }
                function dragEnd(e) {
                    $(tui.unmask()).off("mousemove", dragThumb);
                    $(document).off("mouseup", dragEnd);
                    $(self._btnThumb).removeClass("tui-actived");
                    self.fire("dragend", { value: self.value() });
                }
                $(this._btnThumb).mousedown(function (e) {
                    if (!tui.isLButton(e))
                        return;
                    beginX = e.clientX;
                    beginY = e.clientY;
                    beginLeft = self._btnThumb.offsetLeft;
                    beginTop = self._btnThumb.offsetTop;
                    $(self._btnThumb).addClass("tui-actived");
                    $(tui.mask()).on("mousemove", dragThumb);
                    $(document).on("mouseup", dragEnd);
                    self.fire("dragbegin", { value: self.value() });
                });
                this.refresh();
            }
            Scrollbar.prototype.total = function (val) {
                if (typeof val === "number") {
                    if (val < 0)
                        val = 0;
                    val = Math.round(val);
                    this.attr("data-total", val);
                    if (this.value() > val)
                        this.value(val);
                    else
                        this.refresh();
                    return this;
                }
                else {
                    var val = parseInt(this.attr("data-total"), 10);
                    if (val === null || isNaN(val))
                        return 0;
                    else
                        return val;
                }
            };
            Scrollbar.prototype.value = function (val) {
                if (typeof val === "number") {
                    val = Math.round(val);
                    if (val < 0)
                        val = 0;
                    if (val > this.total())
                        val = this.total();
                    this.attr("data-value", val);
                    this.refresh();
                    return this;
                }
                else {
                    var val = parseInt(this.attr("data-value"), 10);
                    if (val === null || isNaN(val))
                        return 0;
                    else
                        return val;
                }
            };
            Scrollbar.prototype.step = function (val) {
                if (typeof val === "number") {
                    val = Math.round(val);
                    if (val < 1)
                        val = 1;
                    if (val > this.total())
                        val = this.total();
                    this.attr("data-step", val);
                    if (val > this.page())
                        this.page(val);
                    else
                        this.refresh();
                    return this;
                }
                else {
                    var val = parseInt(this.attr("data-step"), 10);
                    if (val === null || isNaN(val))
                        return this.total() > 0 ? 1 : 0;
                    else
                        return val;
                }
            };
            Scrollbar.prototype.page = function (val) {
                if (typeof val === "number") {
                    val = Math.round(val);
                    if (val < 1)
                        val = 1;
                    if (val > this.total())
                        val = this.total();
                    this.attr("data-page", val);
                    if (val < this.step())
                        this.step(val);
                    else
                        this.refresh();
                    return this;
                }
                else {
                    var val = parseInt(this.attr("data-page"), 10);
                    if (val === null || isNaN(val))
                        return this.total() > 0 ? 1 : 0;
                    else
                        return val;
                }
            };
            Scrollbar.prototype.direction = function (val) {
                if (typeof val === "string") {
                    if (["horizontal", "vertical"].indexOf(val) >= 0) {
                        this.attr("data-direction", val);
                        this.refresh();
                    }
                    return this;
                }
                else {
                    var dir = this.attr("data-direction");
                    if (dir === null)
                        return "vertical";
                    else
                        return dir;
                }
            };
            Scrollbar.prototype.logicLenToRealLen = function (logicLen) {
                var len = 0;
                var total = this.total();
                if (total <= 0)
                    return 0;
                if (this.direction() === "vertical") {
                    len = this[0].clientHeight - this._btnHead.offsetHeight - this._btnFoot.offsetHeight - this._btnThumb.offsetHeight;
                }
                else {
                    len = this[0].clientWidth - this._btnHead.offsetWidth - this._btnFoot.offsetWidth - this._btnThumb.offsetWidth;
                }
                return logicLen / total * len;
            };
            Scrollbar.prototype.posToValue = function (pos) {
                var total = this.total();
                if (total <= 0) {
                    return 0;
                }
                var len = 0;
                var val = 0;
                if (this.direction() === "vertical") {
                    len = this[0].clientHeight - this._btnHead.offsetHeight - this._btnFoot.offsetHeight - this._btnThumb.offsetHeight;
                    val = (pos - this._btnHead.offsetHeight) / len * total;
                }
                else {
                    len = this[0].clientWidth - this._btnHead.offsetWidth - this._btnFoot.offsetWidth - this._btnThumb.offsetWidth;
                    val = (pos - this._btnHead.offsetWidth) / len * total;
                }
                val = Math.round(val);
                return val;
            };
            Scrollbar.prototype.valueToPos = function (value) {
                var total = this.total();
                var step = this.step();
                var page = this.page();
                var vertical = (this.direction() === "vertical");
                var minSize = (vertical ? this._btnHead.offsetHeight : this._btnHead.offsetWidth);
                if (total <= 0) {
                    return { pos: 0, thumbLen: 0 };
                }
                var len = (vertical ? this[0].clientHeight - this._btnHead.offsetHeight - this._btnFoot.offsetHeight : this[0].clientWidth - this._btnHead.offsetWidth - this._btnFoot.offsetWidth);
                var thumbLen = Math.round(page / total * len);
                if (thumbLen < minSize)
                    thumbLen = minSize;
                if (thumbLen > len - 10)
                    thumbLen = len - 10;
                var scale = (value / total);
                if (scale < 0)
                    scale = 0;
                if (scale > 1)
                    scale = 1;
                var pos = minSize + Math.round(scale * (len - thumbLen)) - 1;
                return {
                    "pos": pos,
                    "thumbLen": thumbLen
                };
            };
            Scrollbar.prototype.refresh = function () {
                var pos = this.valueToPos(this.value());
                var vertical = (this.direction() === "vertical");
                if (vertical) {
                    this._btnThumb.style.height = (pos.thumbLen > 0 ? pos.thumbLen : 0) + "px";
                    this._btnThumb.style.top = pos.pos + "px";
                    this._btnThumb.style.left = "";
                    this._btnThumb.style.width = "";
                }
                else {
                    this._btnThumb.style.width = (pos.thumbLen > 0 ? pos.thumbLen : 0) + "px";
                    this._btnThumb.style.left = pos.pos + "px";
                    this._btnThumb.style.top = "";
                    this._btnThumb.style.height = "";
                }
            };
            Scrollbar.CLASS = "tui-scrollbar";
            return Scrollbar;
        })(ctrl.Control);
        ctrl.Scrollbar = Scrollbar;
        /**
         * Construct a scrollbar.
         * @param el {HTMLElement or element id or construct info}
         */
        function scrollbar(param) {
            return tui.ctrl.control(param, Scrollbar);
        }
        ctrl.scrollbar = scrollbar;
        tui.ctrl.registerInitCallback(Scrollbar.CLASS, scrollbar);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Table = (function (_super) {
            __extends(Table, _super);
            function Table(el) {
                _super.call(this, "table", ctrl.Grid.CLASS, el);
                this._splitters = [];
                this._columns = [];
                this._data = null;
                var self = this;
                this.addClass(Table.CLASS);
                this._columns = [];
                var noHead = this.noHead();
                var headLine = this.headLine();
                var headKeys = [];
                if (headLine) {
                    for (var i = 0; i < headLine.cells.length; i++) {
                        var cell = headLine.cells[i];
                        var colKey = $(cell).attr("data-key");
                        if (!noHead) {
                            var col = {
                                name: cell.innerHTML,
                                key: colKey ? colKey : i
                            };
                            headKeys.push(colKey ? colKey : i);
                            this._columns.push(col);
                        }
                        else {
                            headKeys.push(i);
                            this._columns.push({ name: "", key: i });
                        }
                    }
                }
                else {
                    if (!this.hasAttr("data-columns")) {
                        this._columns = [];
                    }
                }
                var data = {
                    head: headKeys,
                    data: []
                };
                for (var i = noHead ? 0 : 1; i < this[0].rows.length; i++) {
                    var row = this[0].rows[i];
                    var rowData = [];
                    for (var j = 0; j < this._columns.length; j++) {
                        rowData.push(row.cells[j].innerHTML);
                    }
                    data.data.push(rowData);
                }
                this.data(data);
            }
            Table.prototype.headLine = function () {
                var tb = this[0];
                if (!tb)
                    return null;
                return tb.rows[0];
            };
            Table.prototype.createSplitters = function () {
                var self = this;
                this._splitters.length = 0;
                var tb = this[0];
                if (!tb)
                    return;
                var headLine = this.headLine();
                if (!headLine)
                    return;
                if (this.noHead())
                    return;
                for (var i = 0; i < this._splitters.length; i++) {
                    tui.removeNode(this._splitters[i]);
                }
                if (this.resizable()) {
                    for (var i = 0; i < headLine.cells.length; i++) {
                        var cell = headLine.cells[i];
                        var splitter = document.createElement("span");
                        splitter["colIndex"] = i;
                        splitter.className = "tui-table-splitter";
                        if (typeof this._columns[i].width !== "number")
                            this._columns[i].width = $(cell).width();
                        $(splitter).attr("unselectable", "on");
                        headLine.cells[i].appendChild(splitter);
                        this._splitters.push(splitter);
                        $(splitter).mousedown(function (e) {
                            var target = e.target;
                            var l = target.offsetLeft;
                            var srcX = e.clientX;
                            target.style.height = self[0].clientHeight + "px";
                            target.style.bottom = "";
                            $(target).addClass("tui-splitter-move");
                            var mask = tui.mask();
                            mask.style.cursor = "col-resize";
                            function onDragEnd(e) {
                                $(document).off("mousemove", onDrag);
                                $(document).off("mouseup", onDragEnd);
                                $(target).removeClass("tui-splitter-move");
                                tui.unmask();
                                var colIndex = target["colIndex"];
                                var tmpWidth = self._columns[colIndex].width + e.clientX - srcX;
                                if (tmpWidth < 0)
                                    tmpWidth = 0;
                                self._columns[colIndex].width = tmpWidth;
                                self._columns[colIndex].important = true;
                                self.refresh();
                                self.fire("resizecolumn", colIndex);
                            }
                            function onDrag(e) {
                                target.style.left = l + e.clientX - srcX + "px";
                            }
                            $(document).mousemove(onDrag);
                            $(document).mouseup(onDragEnd);
                        });
                    }
                }
            };
            Table.prototype.noHead = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-no-head", val);
                    this.data(this.data());
                    return this;
                }
                else
                    return this.is("data-no-head");
            };
            Table.prototype.columns = function (val) {
                if (val) {
                    this._columns = val;
                    this.data(this.data());
                    return this;
                }
                else {
                    if (!this._columns) {
                        var valstr = this.attr("data-columns");
                        this._columns = eval("(" + valstr + ")");
                    }
                    return this._columns;
                }
            };
            Table.prototype.value = function (data) {
                if (data === null) {
                    return this.data([]);
                }
                else if (data) {
                    return this.data(data);
                }
                else {
                    var result = [];
                    var dt = this.data();
                    for (var i = 0; i < dt.length(); i++) {
                        result.push(dt.at(i));
                    }
                    return result;
                }
            };
            Table.prototype.data = function (data) {
                if (data) {
                    var self = this;
                    if (data instanceof Array || data.data && data.data instanceof Array) {
                        data = new tui.ArrayProvider(data);
                    }
                    if (typeof data.length !== "function" || typeof data.sort !== "function" || typeof data.at !== "function" || typeof data.columnKeyMap !== "function") {
                        throw new Error("TUI Table: need a data provider.");
                    }
                    var tb = this[0];
                    while (tb.rows.length > 0) {
                        tb.deleteRow(0);
                    }
                    if (!this.noHead()) {
                        var row = tb.insertRow(-1);
                        for (var j = 0; j < this._columns.length; j++) {
                            var cell = row.insertCell(-1);
                            cell.className = "tui-table-head";
                            if (["center", "left", "right"].indexOf(this._columns[j].headAlign) >= 0)
                                cell.style.textAlign = this._columns[j].headAlign;
                            var contentDiv = cell.appendChild(document.createElement("div"));
                            contentDiv.innerHTML = this._columns[j].name;
                        }
                    }
                    for (var i = 0; i < data.length(); i++) {
                        var rowData = data.at(i);
                        var row = tb.insertRow(-1);
                        for (var j = 0; j < this._columns.length; j++) {
                            var cell = row.insertCell(-1);
                            if (["center", "left", "right"].indexOf(this._columns[j].align) >= 0)
                                cell.style.textAlign = this._columns[j].align;
                            var contentDiv = cell.appendChild(document.createElement("div"));
                            var key;
                            if (this._columns[j].key) {
                                key = this._columns[j].key;
                            }
                            else {
                                key = j;
                            }
                            contentDiv.innerHTML = rowData[key];
                        }
                    }
                    this.createSplitters();
                    this.refresh();
                    return this;
                }
                else {
                    return this._data;
                }
            };
            Table.prototype.resizable = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-resizable", val);
                    this.createSplitters();
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-resizable");
            };
            Table.prototype.refresh = function () {
                if (!this.resizable())
                    return;
                var tb = this[0];
                if (!tb)
                    return;
                var headLine = tb.rows[0];
                if (!headLine)
                    return;
                var cellPadding = headLine.cells.length > 0 ? $(headLine.cells[0]).outerWidth() - $(headLine.cells[0]).width() : 0;
                var defaultWidth = Math.floor(tb.offsetWidth / (headLine.cells.length > 0 ? headLine.cells.length : 1) - cellPadding);
                var totalWidth = 0;
                var computeWidth = tb.offsetWidth - cellPadding * (headLine.cells.length > 0 ? headLine.cells.length : 1);
                for (var i = 0; i < this._columns.length; i++) {
                    if (typeof this._columns[i].width !== "number") {
                        this._columns[i].width = defaultWidth;
                        totalWidth += defaultWidth;
                    }
                    else if (!this._columns[i].important) {
                        totalWidth += this._columns[i].width;
                    }
                    else {
                        if (this._columns[i].width > computeWidth)
                            this._columns[i].width = computeWidth;
                        if (this._columns[i].width < 1)
                            this._columns[i].width = 1;
                        computeWidth -= this._columns[i].width;
                    }
                }
                for (var i = 0; i < this._columns.length; i++) {
                    if (!this._columns[i].important) {
                        if (totalWidth === 0)
                            this._columns[i].width = 0;
                        else
                            this._columns[i].width = Math.floor(this._columns[i].width / totalWidth * computeWidth);
                        if (this._columns[i].width < 1)
                            this._columns[i].width = 1;
                    }
                    else {
                        this._columns[i].important = false;
                    }
                    if (tb.rows.length > 0) {
                        var row = tb.rows[0];
                        $(row.cells[i]).css("width", this._columns[i].width + "px");
                    }
                }
                var headLine = this.headLine();
                for (var i = 0; i < this._splitters.length; i++) {
                    var splitter = this._splitters[i];
                    var cell = headLine.cells[i];
                    splitter.style.left = cell.offsetLeft + cell.offsetWidth - Math.round(splitter.offsetWidth / 2) + "px";
                    splitter.style.height = headLine.offsetHeight + "px";
                }
            };
            Table.CLASS = "tui-table";
            return Table;
        })(ctrl.Control);
        ctrl.Table = Table;
        /**
         * Construct a table control.
         * @param el {HTMLElement or element id or construct info}
         */
        function table(param) {
            return tui.ctrl.control(param, Table);
        }
        ctrl.table = table;
        tui.ctrl.registerInitCallback(Table.CLASS, table);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Grid = (function (_super) {
            __extends(Grid, _super);
            function Grid(el) {
                _super.call(this, "div", Grid.CLASS, el);
                this._tableId = tui.uuid();
                this._gridStyle = null;
                // Grid data related
                this._columns = null;
                this._emptyColumns = [];
                this._data = null;
                this._emptyData = new tui.ArrayProvider([]);
                this._splitters = [];
                // Scrolling related
                this._scrollTop = 0;
                this._scrollLeft = 0;
                this._bufferedLines = [];
                this._bufferedBegin = 0;
                this._bufferedEnd = 0; // _bufferedEnd = _bufferedBegin + buffered count
                this._dispLines = 0; // How many lines can be displayed in grid viewable area
                // Drawing related flags
                this._selectrows = [];
                this._activerow = null;
                //private _columnKeyMap: {} = null;
                this._noRefresh = false;
                this._initialized = false;
                //private _initInterval = null;
                // Following variables are very useful when grid switch to edit mode 
                // because grid cell need spend more time to draw.
                this._drawingTimer = null;
                this._delayDrawing = true;
                var self = this;
                this.attr("tabIndex", "0");
                this[0].innerHTML = "";
                if (document.createStyleSheet) {
                    this._gridStyle = document.createStyleSheet();
                }
                else {
                    this._gridStyle = document.createElement("style");
                    document.head.appendChild(this._gridStyle);
                }
                this._headline = document.createElement("div");
                this._headline.className = "tui-grid-head";
                this[0].appendChild(this._headline);
                this._hscroll = tui.ctrl.scrollbar();
                this._hscroll.direction("horizontal");
                this[0].appendChild(this._hscroll[0]);
                this._vscroll = tui.ctrl.scrollbar();
                this._vscroll.direction("vertical");
                this[0].appendChild(this._vscroll[0]);
                this._space = document.createElement("span");
                this._space.className = "tui-scroll-space";
                this[0].appendChild(this._space);
                var scrollTimeDelay = (tui.ieVer > 8 ? 100 : 50);
                this._vscroll.on("scroll", function (data) {
                    if (!self._delayDrawing) {
                        self._scrollTop = data["value"];
                        self.drawLines();
                    }
                    else {
                        var diff = Math.abs(data["value"] - self._scrollTop);
                        self._scrollTop = data["value"];
                        if (diff < 3 * self._lineHeight && self._drawingTimer === null) {
                            self.drawLines();
                        }
                        else {
                            self.drawLines(true);
                            clearTimeout(self._drawingTimer);
                            self._drawingTimer = setTimeout(function () {
                                self.clearBufferLines();
                                self.drawLines();
                                self._drawingTimer = null;
                            }, scrollTimeDelay);
                        }
                    }
                });
                this._hscroll.on("scroll", function (data) {
                    self._scrollLeft = data["value"];
                    self.drawLines();
                });
                var mousewheelevt = (/Firefox/i.test(navigator.userAgent)) ? "DOMMouseScroll" : "mousewheel";
                $(this[0]).on(mousewheelevt, function (ev) {
                    var e = ev.originalEvent;
                    var delta = e.detail ? e.detail * (-120) : e.wheelDelta;
                    var step = Math.round(self._vscroll.page() / 2);
                    //delta returns +120 when wheel is scrolled up, -120 when scrolled down
                    var scrollSize = step > self._vscroll.step() ? step : self._vscroll.step();
                    if (delta <= -120) {
                        if (self._vscroll.value() < self._vscroll.total()) {
                            self._vscroll.value(self._vscroll.value() + scrollSize);
                            self._scrollTop = self._vscroll.value();
                            self.drawLines();
                            ev.stopPropagation();
                            ev.preventDefault();
                        }
                        else if (self.consumeMouseWheelEvent()) {
                            ev.stopPropagation();
                            ev.preventDefault();
                        }
                    }
                    else {
                        if (self._vscroll.value() > 0) {
                            self._vscroll.value(self._vscroll.value() - scrollSize);
                            self._scrollTop = self._vscroll.value();
                            self.drawLines();
                            ev.stopPropagation();
                            ev.preventDefault();
                        }
                        else if (self.consumeMouseWheelEvent()) {
                            ev.stopPropagation();
                            ev.preventDefault();
                        }
                    }
                });
                $(this[0]).mousedown(function (e) {
                    tui.focusWithoutScroll(self[0]);
                    e.preventDefault();
                });
                $(this[0]).keyup(function (e) {
                    self.fire("keyup", { event: e });
                });
                $(this[0]).on("contextmenu", function (e) {
                    return self.fire("contextmenu", { event: e });
                });
                $(this[0]).keydown(function (e) {
                    if (self.fire("keydown", { event: e }) === false)
                        return;
                    var data = self.myData();
                    var k = e.keyCode;
                    // 37:left 38:up 39:right 40:down
                    if ([33, 34, 37, 38, 39, 40].indexOf(k) >= 0) {
                        if (k === 37) {
                            !self._hscroll.hidden() && self._hscroll.value(self._hscroll.value() - self._hscroll.step());
                            self._scrollLeft = self._hscroll.value();
                            self.drawLines();
                        }
                        else if (k === 38) {
                            if (!self.rowselectable() || data.length() <= 0) {
                                !self._vscroll.hidden() && self._vscroll.value(self._vscroll.value() - self._vscroll.step());
                                self._scrollTop = self._vscroll.value();
                                self.drawLines();
                            }
                            else {
                                if (self._activerow === null) {
                                    self.activerow(0);
                                    self.scrollTo(self._activerow);
                                }
                                else {
                                    if (self._activerow > 0)
                                        self.activerow(self._activerow - 1);
                                    self.scrollTo(self._activerow);
                                }
                            }
                        }
                        else if (k === 39) {
                            !self._hscroll.hidden() && self._hscroll.value(self._hscroll.value() + self._hscroll.step());
                            self._scrollLeft = self._hscroll.value();
                            self.drawLines();
                        }
                        else if (k === 40) {
                            if (!self.rowselectable() || data.length() <= 0) {
                                !self._vscroll.hidden() && self._vscroll.value(self._vscroll.value() + self._vscroll.step());
                                self._scrollTop = self._vscroll.value();
                                self.drawLines();
                            }
                            else {
                                if (self._activerow === null) {
                                    self.activerow(0);
                                    self.scrollTo(self._activerow);
                                }
                                else {
                                    if (self._activerow < data.length() - 1)
                                        self.activerow(self._activerow + 1);
                                    self.scrollTo(self._activerow);
                                }
                            }
                        }
                        else if (k === 33) {
                            if (!self.rowselectable() || data.length() <= 0) {
                                !self._vscroll.hidden() && self._vscroll.value(self._vscroll.value() - self._vscroll.page());
                                self._scrollTop = self._vscroll.value();
                                self.drawLines();
                            }
                            else {
                                if (self._activerow === null) {
                                    self.activerow(0);
                                    self.scrollTo(self._activerow);
                                }
                                else {
                                    if (self._activerow > 0)
                                        self.activerow(self._activerow - self._dispLines);
                                    self.scrollTo(self._activerow);
                                }
                            }
                        }
                        else if (k === 34) {
                            if (!self.rowselectable() || data.length() <= 0) {
                                !self._vscroll.hidden() && self._vscroll.value(self._vscroll.value() + self._vscroll.page());
                                self._scrollTop = self._vscroll.value();
                                self.drawLines();
                            }
                            else {
                                if (self._activerow === null) {
                                    self.activerow(self._dispLines);
                                    self.scrollTo(self._activerow);
                                }
                                else {
                                    if (self._activerow < data.length() - 1)
                                        self.activerow(self._activerow + self._dispLines);
                                    self.scrollTo(self._activerow);
                                }
                            }
                        }
                        e.preventDefault();
                        e.stopPropagation();
                        if (tui.ieVer > 0)
                            self[0].setActive();
                    }
                    else if (k === tui.KEY_TAB) {
                        if ((e.target || e.srcElement) === self[0]) {
                            var rowIndex;
                            if (self.rowselectable()) {
                                rowIndex = self.activerow();
                            }
                            else {
                                rowIndex = self._bufferedBegin;
                            }
                            if (self.editRow(rowIndex))
                                e.preventDefault();
                        }
                    }
                });
                if (this.hasAttr("data-delay-drawing"))
                    this._delayDrawing = this.is("data-delay-drawing");
                var predefined = this.attr("data-data");
                if (predefined)
                    predefined = eval("(" + predefined + ")");
                if (predefined)
                    this.data(predefined);
                else
                    this.refresh();
                //if (!this._initialized) {
                //	this._initInterval = setInterval(() => {
                //		self.refresh();
                //		if (self._initialized) {
                //			clearInterval(self._initInterval);
                //			self._initInterval = null;
                //		}
                //	}, 100);
                //}
            }
            //release() {
            //	if (this._initInterval)
            //		clearInterval(this._initInterval);
            //}
            // Make sure not access null object
            Grid.prototype.myData = function () {
                return this._data || this._emptyData;
            };
            Grid.prototype.myColumns = function () {
                return this.columns() || this._emptyColumns;
            };
            Grid.prototype.headHeight = function () {
                if (!this.noHead())
                    return this._headHeight;
                else
                    return 0;
            };
            Grid.colSize = function (size, def) {
                if (typeof size === "number" && !isNaN(size)) {
                    if (size < 0)
                        return 0;
                    else
                        return Math.round(size);
                }
                else
                    return def;
            };
            Grid.prototype.computeVScroll = function (mark) {
                var hScrollbarHeight = this._hscroll.hidden() ? 0 : this._hscroll[0].offsetHeight;
                var contentHeight = this._contentHeight;
                var innerHeight = this._boxHeight - hScrollbarHeight;
                var totalHeight = contentHeight + this.headHeight();
                this._dispLines = Math.ceil((innerHeight - this.headHeight()) / this._lineHeight);
                var vHidden = this._vscroll.hidden();
                if (totalHeight > innerHeight) {
                    this._vscroll.hidden(false);
                    this._vscroll[0].style.bottom = hScrollbarHeight + "px";
                    this._vscroll.total(totalHeight - innerHeight).value(this._scrollTop).step(this._lineHeight).page(innerHeight / totalHeight * (totalHeight - innerHeight));
                }
                else {
                    this._vscroll.hidden(true);
                    this._vscroll.total(0);
                }
                this._scrollTop = this._vscroll.value();
                if (vHidden !== this._vscroll.hidden()) {
                    this.computeHScroll(mark);
                    this.computeColumns();
                }
            };
            Grid.prototype.computeHScroll = function (mark) {
                mark.isHScrollComputed = true;
                var columns = this.myColumns();
                var vScrollbarWidth = this._vscroll.hidden() ? 0 : this._vscroll[0].offsetWidth;
                var innerWidth = this._boxWidth - vScrollbarWidth;
                var hHidden = this._hscroll.hidden();
                if (this.hasHScroll()) {
                    this._contentWidth = 0;
                    var cols = (columns.length < 1 ? 1 : columns.length);
                    var defaultWidth = Math.floor((innerWidth - this._borderWidth * cols) / cols);
                    if (defaultWidth < 100)
                        defaultWidth = 100;
                    for (var i = 0; i < columns.length; i++) {
                        this._contentWidth += Grid.colSize(columns[i].width, defaultWidth) + this._borderWidth;
                    }
                    if (this._contentWidth > innerWidth) {
                        this._hscroll.hidden(false);
                        this._hscroll[0].style.right = vScrollbarWidth + "px";
                        this._hscroll.total(this._contentWidth - innerWidth).value(this._scrollLeft).step(10).page(innerWidth / this._contentWidth * (this._contentWidth - innerWidth));
                    }
                    else {
                        this._hscroll.hidden(true);
                        this._hscroll.total(0);
                    }
                }
                else {
                    this._contentWidth = innerWidth;
                    this._hscroll.hidden(true);
                    this._hscroll.total(0);
                }
                this._scrollLeft = this._hscroll.value();
                if (hHidden !== this._hscroll.hidden())
                    this.computeVScroll(mark);
            };
            Grid.prototype.computeScroll = function () {
                this._boxWidth = this[0].clientWidth;
                this._boxHeight = this[0].clientHeight;
                var cell = document.createElement("span");
                cell.className = "tui-grid-cell";
                var line = document.createElement("span");
                line.className = "tui-grid-line";
                line.appendChild(cell);
                cell.innerHTML = "a";
                this[0].appendChild(line);
                this._lineHeight = $(line).outerHeight(); //line.offsetHeight;
                this._borderWidth = $(cell).outerWidth() - $(cell).width();
                cell.className = "tui-grid-head-cell";
                line.className = "tui-grid-head";
                this._headHeight = line.offsetHeight;
                this[0].removeChild(line);
                this._contentHeight = this._lineHeight * this.myData().length();
                var mark = { isHScrollComputed: false };
                this._hscroll.hidden(true);
                this._vscroll.hidden(true);
                this.computeVScroll(mark);
                if (!mark.isHScrollComputed) {
                    this.computeHScroll(mark);
                    this.computeColumns();
                }
                if (!this._hscroll.hidden() && !this._vscroll.hidden()) {
                    this._space.style.display = "";
                }
                else
                    this._space.style.display = "none";
            };
            // Do not need call this function standalone, 
            // it's always to be called by computeScroll function
            Grid.prototype.computeColumns = function () {
                var columns = this.myColumns();
                var vScrollbarWidth = this._vscroll.hidden() ? 0 : this._vscroll[0].offsetWidth;
                var innerWidth = this._boxWidth - vScrollbarWidth;
                var cols = (columns.length < 1 ? 1 : columns.length);
                var defaultWidth = Math.floor((innerWidth - this._borderWidth * cols) / cols);
                if (this.hasHScroll()) {
                    if (defaultWidth < 100)
                        defaultWidth = 100;
                    for (var i = 0; i < columns.length; i++) {
                        delete columns[i]["_important"];
                        columns[i].width = Grid.colSize(columns[i].width, defaultWidth);
                    }
                }
                else {
                    var totalNoBorderWidth = this._contentWidth - this._borderWidth * cols;
                    totalNoBorderWidth += (vScrollbarWidth === 0 ? 1 : 0);
                    var totalNoFixedWidth = totalNoBorderWidth;
                    var totalNeedComputed = 0;
                    var totalNeedComputedCount = 0;
                    var totalImportantWidth = 0;
                    var important = [];
                    for (var i = 0; i < columns.length; i++) {
                        if (columns[i]["fixed"]) {
                            if (typeof columns[i].width !== "number" || isNaN(columns[i].width))
                                columns[i].width = defaultWidth;
                            totalNoFixedWidth -= columns[i].width;
                        }
                    }
                    if (totalNoFixedWidth < 0)
                        totalNoFixedWidth = 0;
                    var totalNoImportantWidth = totalNoFixedWidth;
                    for (var i = 0; i < columns.length; i++) {
                        if (typeof columns[i].width !== "number" || isNaN(columns[i].width))
                            columns[i].width = defaultWidth;
                        else if (columns[i].width < 0)
                            columns[i].width = 0;
                        if (columns[i]["fixed"]) {
                        }
                        else if (columns[i]["_important"]) {
                            important.push(i);
                            delete columns[i]["_important"];
                            columns[i].width = Math.round(columns[i].width);
                            if (columns[i].width > totalNoFixedWidth) {
                                columns[i].width = totalNoFixedWidth;
                            }
                            totalImportantWidth += columns[i].width;
                            totalNoImportantWidth -= columns[i].width;
                        }
                        else {
                            totalNeedComputed += Math.round(columns[i].width);
                            totalNeedComputedCount++;
                        }
                    }
                    if (totalNeedComputedCount > 0 && totalNeedComputed === 0) {
                        for (var i = 0; i < columns.length; i++) {
                            if (important.indexOf(i) < 0 && !columns[i]["fixed"]) {
                                columns[i].width = Math.floor(totalNoImportantWidth / totalNeedComputedCount);
                            }
                        }
                    }
                    else {
                        for (var i = 0; i < columns.length; i++) {
                            if (important.indexOf(i) < 0 && !columns[i]["fixed"]) {
                                if (totalNeedComputed === 0)
                                    columns[i].width = 0; // To avoid divide by zero
                                else
                                    columns[i].width = Math.floor(Math.round(columns[i].width) / totalNeedComputed * totalNoImportantWidth);
                            }
                        }
                    }
                    var total = 0;
                    for (var i = 0; i < columns.length; i++) {
                        total += columns[i].width;
                    }
                    if (total < totalNoBorderWidth && columns.length > 0) {
                        for (var i = 0; i < columns.length; i++) {
                            if (!columns[i].fixed) {
                                columns[i].width += totalNoBorderWidth - total;
                                break;
                            }
                        }
                    }
                }
                var cssText = "";
                for (var i = 0; i < columns.length; i++) {
                    var wd = columns[i].width;
                    cssText += (".tui-grid-" + this._tableId + "-" + i + "{width:" + wd + "px}");
                }
                if (document.createStyleSheet)
                    this._gridStyle.cssText = cssText;
                else
                    this._gridStyle.innerHTML = cssText;
            };
            Grid.prototype.bindSplitter = function (cell, col, colIndex) {
                var self = this;
                var splitter = document.createElement("span");
                splitter.className = "tui-grid-splitter";
                splitter.setAttribute("unselectable", "on");
                $(splitter).mousedown(function (e) {
                    var l = splitter.offsetLeft;
                    var srcX = e.clientX;
                    splitter.style.height = self[0].clientHeight + "px";
                    splitter.style.bottom = "";
                    $(splitter).addClass("tui-splitter-move");
                    var mask = tui.mask();
                    mask.style.cursor = "col-resize";
                    function onDragEnd(e) {
                        $(document).off("mousemove", onDrag);
                        $(document).off("mouseup", onDragEnd);
                        tui.unmask();
                        splitter.style.bottom = "0";
                        splitter.style.height = "";
                        $(splitter).removeClass("tui-splitter-move");
                        col.width = col.width + e.clientX - srcX;
                        col["_important"] = true;
                        var currentTime = tui.today().getTime();
                        if (col["_lastClickTime"]) {
                            if (currentTime - col["_lastClickTime"] < 500) {
                                self.autofitColumn(colIndex, false, true);
                                self.fire("resizecolumn", { col: colIndex });
                                return;
                            }
                        }
                        col["_lastClickTime"] = currentTime;
                        self.refresh();
                        self.fire("resizecolumn", { col: colIndex });
                    }
                    function onDrag(e) {
                        splitter.style.left = l + e.clientX - srcX + "px";
                    }
                    $(document).on("mousemove", onDrag);
                    $(document).on("mouseup", onDragEnd);
                });
                this._splitters.push(splitter);
                return splitter;
            };
            Grid.prototype.bindSort = function (cell, col, colIndex) {
                var self = this;
                if (col.sort) {
                    $(cell).addClass("tui-grid-sortable");
                    $(cell).mousedown(function (event) {
                        if (!tui.isLButton(event))
                            return;
                        if (self._sortColumn !== colIndex)
                            self.sort(colIndex);
                        else if (!self._sortDesc)
                            self.sort(colIndex, true);
                        else
                            self.sort(null);
                    });
                }
                if (self._sortColumn === colIndex) {
                    if (self._sortDesc)
                        $(cell).addClass("tui-grid-cell-sort-desc");
                    else
                        $(cell).addClass("tui-grid-cell-sort-asc");
                }
            };
            Grid.prototype.moveSplitter = function () {
                for (var i = 0; i < this._splitters.length; i++) {
                    var splitter = this._splitters[i];
                    var cell = this._headline.childNodes[i]; //*2];
                    splitter.style.left = cell.offsetLeft + cell.offsetWidth - Math.round(splitter.offsetWidth / 2) + "px";
                }
            };
            Grid.prototype.drawCell = function (cell, contentSpan, col, colKey, value, row, rowIndex, colIndex) {
                if (rowIndex >= 0) {
                    if (["center", "left", "right"].indexOf(col.align) >= 0)
                        cell.style.textAlign = col.align;
                }
                else {
                    if (["center", "left", "right"].indexOf(col.headAlign) >= 0)
                        cell.style.textAlign = col.headAlign;
                }
                if (value === null || typeof value === tui.undef) {
                    contentSpan.innerHTML = "";
                }
                else if (typeof value === "object" && value.nodeName) {
                    contentSpan.innerHTML = "";
                    contentSpan.appendChild(value);
                }
                else {
                    contentSpan.innerHTML = value;
                }
                if (typeof col.format === "function") {
                    col.format.call(this, {
                        cell: cell,
                        value: value,
                        row: row,
                        col: col,
                        colKey: colKey,
                        rowIndex: rowIndex,
                        colIndex: colIndex,
                        isRowActived: rowIndex === this._activerow,
                        grid: this
                    });
                }
                if (this._sortColumn === colIndex)
                    $(cell).addClass("tui-grid-sort-cell");
                else
                    $(cell).removeClass("tui-grid-sort-cell");
            };
            Grid.prototype.drawHead = function () {
                if (this.noHead()) {
                    $(this._headline).addClass("tui-hidden");
                    return;
                }
                $(this._headline).removeClass("tui-hidden");
                var columns = this.myColumns();
                this._headline.innerHTML = "";
                this._splitters.length = 0;
                for (var i = 0; i < columns.length; i++) {
                    var col = columns[i];
                    var key = null;
                    if (typeof col.key !== tui.undef && col.key !== null) {
                        key = this.myData().mapKey(col.key);
                        if (typeof key === tui.undef)
                            key = col.key;
                    }
                    var cell = document.createElement("span");
                    cell.setAttribute("unselectable", "on");
                    cell.className = "tui-grid-head-cell tui-grid-" + this._tableId + "-" + i;
                    this._headline.appendChild(cell);
                    var contentSpan = document.createElement("span");
                    contentSpan.className = "tui-grid-cell-content";
                    cell.appendChild(contentSpan);
                    this.drawCell(cell, contentSpan, col, key, col.name, null, -1, i);
                    this.bindSort(cell, col, i);
                    if (this.resizable()) {
                        var splitter = this.bindSplitter(cell, col, i);
                        if (typeof columns[i].fixed === "boolean" && columns[i].fixed)
                            $(splitter).addClass("tui-hidden");
                    }
                }
                for (var i = 0; i < this._splitters.length; i++) {
                    var splitter = this._splitters[i];
                    this._headline.appendChild(splitter);
                }
                this.moveSplitter();
            };
            Grid.prototype.isRowSelected = function (rowIndex) {
                return this._selectrows.indexOf(rowIndex) >= 0;
            };
            Grid.prototype.drawLine = function (line, index, empty) {
                var columns = this.myColumns();
                if (line.childNodes.length !== columns.length) {
                    line.innerHTML = "";
                    var rowSel = this.rowselectable();
                    for (var i = 0; i < columns.length; i++) {
                        var cell = document.createElement("span");
                        if (rowSel)
                            cell.setAttribute("unselectable", "on");
                        cell.className = "tui-grid-cell tui-grid-" + this._tableId + "-" + i;
                        line.appendChild(cell);
                    }
                }
                if (empty) {
                    return;
                }
                var self = this;
                var data = this.myData();
                var rowData = data.at(index);
                for (var i = 0; i < line.childNodes.length; i++) {
                    var cell = line.childNodes[i];
                    cell.innerHTML = "";
                    var contentSpan = document.createElement("span");
                    contentSpan.className = "tui-grid-cell-content";
                    cell.appendChild(contentSpan);
                    var col = columns[i];
                    var key = null;
                    if (typeof col.key !== tui.undef)
                        key = data.mapKey(col.key);
                    var value = (key !== null && rowData ? rowData[key] : "");
                    this.drawCell(cell, contentSpan, col, key, value, rowData, index, i);
                }
                var jqLine = $(line);
                jqLine.on("contextmenu", function (e) {
                    return self.fire("rowcontextmenu", { "event": e, "index": index, "row": line });
                });
                var dragIndex = null;
                var mouseDownPt = null;
                jqLine.mousedown(function (e) {
                    if (self.rowselectable()) {
                        self.activerow(index);
                        self.scrollTo(index);
                    }
                    if (self.fire("rowmousedown", { "event": e, "index": index, "row": line }) === false)
                        return;
                    if (self.rowdraggable() && tui.isLButton(e)) {
                        mouseDownPt = { x: e.clientX, y: e.clientY };
                        dragIndex = null;
                        function testdrag(e) {
                            if (mouseDownPt === null || dragIndex !== null) {
                                return;
                            }
                            if (Math.abs(e.clientX - mouseDownPt.x) < 5 && Math.abs(e.clientY - mouseDownPt.y) < 5) {
                                return;
                            }
                            if (self.fire("rowdragstart", { "event": e, "index": index, "row": line }) === false)
                                return;
                            jqLine.addClass("tui-grid-line-drag");
                            dragIndex = index;
                            var m = tui.mask();
                            m.setAttribute("data-cursor-tooltip", "true");
                            self.focus();
                            // DRAG A LINE
                            var upTimer = null;
                            var downTimer = null;
                            var firstScroll = true;
                            var lineHeight = self.lineHeight();
                            var headHeight = self.headHeight();
                            var targetBox = document.createElement("div");
                            var targetIndex = null;
                            var position = null;
                            targetBox.className = "tui-grid-line-drop";
                            function fireBefore(targetLine) {
                                if (self.fire("rowdragover", { "event": e, "index": dragIndex, "targetIndex": targetIndex, position: "before" }) !== false) {
                                    targetBox.style.top = targetLine.offsetTop - 2 + "px";
                                    targetBox.style.height = "3px";
                                    targetBox.className = "tui-grid-line-drop-before";
                                    m.setAttribute("data-tooltip", "<i class='fa fa-level-up'></i> Move before ...");
                                    position = "before";
                                    return true;
                                }
                                else
                                    return false;
                            }
                            function fireAfter(targetLine) {
                                if (self.fire("rowdragover", { "event": e, "index": dragIndex, "targetIndex": targetIndex, position: "after" }) !== false) {
                                    targetBox.style.top = targetLine.offsetTop + targetLine.offsetHeight - 2 + "px";
                                    targetBox.style.height = "3px";
                                    targetBox.className = "tui-grid-line-drop-after";
                                    m.setAttribute("data-tooltip", "<i class='fa fa-level-down'></i> Move after ...");
                                    position = "after";
                                    return true;
                                }
                                else
                                    return false;
                            }
                            function fireInside(targetLine) {
                                if (self.fire("rowdragover", { "event": e, "index": dragIndex, "targetIndex": targetIndex, position: "inside" }) !== false) {
                                    targetBox.style.top = targetLine.offsetTop - 2 + "px";
                                    targetBox.style.height = targetLine.clientHeight + "px";
                                    targetBox.className = "tui-grid-line-drop";
                                    m.setAttribute("data-tooltip", "<i class='fa fa-arrow-right'></i> Move into ...");
                                    position = "inside";
                                    return true;
                                }
                                else
                                    return false;
                            }
                            function move(e) {
                                if (!tui.isLButton(e)) {
                                    targetIndex = null;
                                    position = null;
                                    release(e, true);
                                    return;
                                }
                                var x = e.clientX;
                                var y = e.clientY;
                                var pos = tui.fixedPosition(self[0]);
                                if (x < pos.x || x > pos.x + self[0].offsetWidth || y < pos.y || y > pos.y + self[0].offsetHeight) {
                                    m.style.cursor = "not-allowed";
                                }
                                else
                                    m.style.cursor = "move";
                                if (y < pos.y + headHeight) {
                                    clearInterval(downTimer);
                                    downTimer = null;
                                    upTimer === null && (upTimer = setInterval(function () {
                                        if (firstScroll)
                                            self.scrollTo(self._bufferedBegin);
                                        else if (self._bufferedBegin > 0) {
                                            self.scrollTo(self._bufferedBegin - 1);
                                        }
                                        firstScroll = false;
                                    }, 80)) && (firstScroll = true);
                                    tui.removeNode(targetBox);
                                    targetIndex = null;
                                    position = null;
                                    m.removeAttribute("data-tooltip");
                                }
                                else if (y > pos.y + self[0].offsetHeight) {
                                    clearInterval(upTimer);
                                    upTimer = null;
                                    downTimer === null && (downTimer = setInterval(function () {
                                        if (firstScroll)
                                            self.scrollTo(self._bufferedEnd - 1);
                                        else if (self._bufferedEnd <= self._data.length()) {
                                            self.scrollTo(self._bufferedEnd - 1);
                                        }
                                        firstScroll = false;
                                    }, 80)) && (firstScroll = true);
                                    tui.removeNode(targetBox);
                                    targetIndex = null;
                                    position = null;
                                    m.removeAttribute("data-tooltip");
                                }
                                else {
                                    clearInterval(downTimer);
                                    clearInterval(upTimer);
                                    downTimer = null;
                                    upTimer = null;
                                    // Use mouse position to detect which row is currently moved to.
                                    var base = headHeight - self._scrollTop % lineHeight;
                                    targetIndex = Math.floor((y - (pos.y + base)) / lineHeight);
                                    var targetLine = self._bufferedLines[targetIndex];
                                    if (!targetLine) {
                                        targetIndex = self._bufferedEnd - 1;
                                        return;
                                    }
                                    else
                                        targetIndex += self._bufferedBegin;
                                    if (targetIndex === dragIndex) {
                                        tui.removeNode(targetBox);
                                        targetIndex = null;
                                        return;
                                    }
                                    pos = tui.fixedPosition(targetLine);
                                    targetBox.style.left = targetLine.offsetLeft - 2 + "px";
                                    targetBox.style.width = targetLine.offsetWidth + "px";
                                    if (y - pos.y <= 12) {
                                        if (!fireBefore(targetLine))
                                            fireInside(targetLine);
                                    }
                                    else if (pos.y + targetLine.offsetHeight - y <= 12) {
                                        if (!fireAfter(targetLine))
                                            fireInside(targetLine);
                                    }
                                    else {
                                        if (!fireInside(targetLine)) {
                                            if (y - pos.y <= lineHeight / 2)
                                                fireBefore(targetLine);
                                            else
                                                fireAfter(targetLine);
                                        }
                                    }
                                    self[0].appendChild(targetBox);
                                }
                            }
                            function release(env, canceled) {
                                jqLine.removeClass("tui-grid-line-drag");
                                console.debug("mouse release");
                                mouseDownPt = null;
                                clearInterval(downTimer);
                                clearInterval(upTimer);
                                downTimer = null;
                                upTimer = null;
                                tui.removeNode(targetBox);
                                $(m).off("mousemove", move);
                                $(m).off("mouseup", release);
                                $(document).off("mouseup", release);
                                $(self[0]).off("keydown", keydown);
                                tui.unmask();
                                tui.closeTooltip();
                                self.fire("rowdragend", { "event": env, "index": dragIndex, "targetIndex": targetIndex, position: position, canceled: !!canceled });
                                dragIndex = null;
                            }
                            function keydown(e) {
                                if (e.keyCode === tui.KEY_ESC) {
                                    targetIndex = null;
                                    position = null;
                                    release(e, true);
                                    e.stopPropagation();
                                }
                            }
                            $(m).on("mousemove", move);
                            $(m).on("mouseup", release);
                            $(document).on("mouseup", release);
                            $(self[0]).on("keydown", keydown);
                        }
                        function release() {
                            mouseDownPt = null;
                            $(document).off("mouseup", release);
                            $(document).off("mousemove", testdrag);
                        }
                        $(document).on("mouseup", release);
                        $(document).on("mousemove", testdrag);
                    }
                    //	e.stopPropagation();
                });
                jqLine.mouseenter(function (e) {
                    self.fire("rowmouseenter", { "event": e, "index": index, "row": line });
                });
                jqLine.mouseleave(function (e) {
                    self.fire("rowmouseleave", { "event": e, "index": index, "row": line });
                });
                jqLine.mouseup(function (e) {
                    self.fire("rowmouseup", { "event": e, "index": index, "row": line });
                });
                jqLine.on("click", function (e) {
                    self.fire("rowclick", { "event": e, "index": index, "row": line });
                });
                jqLine.on("dblclick", function (e) {
                    self.fire("rowdblclick", { "event": e, "index": index, "row": line });
                });
            };
            Grid.prototype.moveLine = function (line, index, base) {
                line.style.top = (base + index * this._lineHeight) + "px";
                line.style.left = -this._scrollLeft + "px";
            };
            Grid.prototype.drawLines = function (empty) {
                if (empty === void 0) { empty = false; }
                this._headline.style.left = -this._scrollLeft + "px";
                var base = this.headHeight() - this._scrollTop % this._lineHeight;
                var begin = Math.floor(this._scrollTop / this._lineHeight);
                var newBuffer = [];
                var data = this.myData();
                for (var i = begin; i < begin + this._dispLines + 1 && i < data.length(); i++) {
                    if (i >= this._bufferedBegin && i < this._bufferedEnd) {
                        // Is buffered.
                        var line = this._bufferedLines[i - this._bufferedBegin];
                        this.moveLine(line, i - begin, base);
                        newBuffer.push(line);
                    }
                    else {
                        var line = document.createElement("div");
                        line.className = "tui-grid-line";
                        this[0].appendChild(line);
                        newBuffer.push(line);
                        line["_rowIndex"] = i;
                        this.drawLine(line, i, empty);
                        this.moveLine(line, i - begin, base);
                    }
                    if (this.isRowSelected(i)) {
                        $(line).addClass("tui-grid-line-selected");
                    }
                    else
                        $(line).removeClass("tui-grid-line-selected");
                }
                var end = i;
                for (var i = this._bufferedBegin; i < this._bufferedEnd; i++) {
                    if (i < begin || i >= end)
                        this[0].removeChild(this._bufferedLines[i - this._bufferedBegin]);
                }
                this._bufferedLines = newBuffer;
                this._bufferedBegin = begin;
                this._bufferedEnd = end;
            };
            Grid.prototype.clearBufferLines = function () {
                if (!this[0])
                    return;
                for (var i = 0; i < this._bufferedLines.length; i++) {
                    var l = this._bufferedLines[i];
                    this[0].removeChild(l);
                }
                this._bufferedLines = [];
                this._bufferedEnd = this._bufferedBegin = 0;
            };
            Grid.prototype.lineHeight = function () {
                if (typeof this._lineHeight !== tui.undef)
                    return this._lineHeight;
                else {
                    var grid = document.createElement("div");
                    grid.className = this[0].className;
                    var line = document.createElement("div");
                    line.className = "tui-grid-line";
                    grid.appendChild(line);
                    document.body.appendChild(grid);
                    var lineHeight = line.offsetHeight;
                    document.body.removeChild(grid);
                    return lineHeight;
                }
            };
            Grid.prototype.select = function (rows) {
                if (rows && typeof rows.length === "number" && rows.length >= 0) {
                    this._selectrows.length = 0;
                    for (var i = 0; i < rows.length; i++) {
                        this._selectrows.push(rows[i]);
                    }
                    // Clear buffer cause row click event cannot be raised, 
                    // so never do this when we only want to change row selection status.
                    this.drawLines();
                }
                return this._selectrows;
            };
            Grid.prototype.activerow = function (rowIndex) {
                if (typeof rowIndex === "number" || rowIndex === null) {
                    if (rowIndex < 0)
                        rowIndex = 0;
                    if (rowIndex >= this.myData().length())
                        rowIndex = this.myData().length() - 1;
                    this._activerow = rowIndex;
                    if (rowIndex === null)
                        this.select([]);
                    else
                        this.select([rowIndex]);
                }
                return this._activerow;
            };
            Grid.prototype.activeItem = function (rowItem) {
                var data = this.myData();
                if (typeof rowItem !== tui.undef) {
                    if (rowItem === null) {
                        this.activerow(null);
                    }
                    else {
                        for (var i = 0; i < data.length(); i++) {
                            if (data.at(i) === rowItem) {
                                this.activerow(i);
                                break;
                            }
                        }
                    }
                }
                if (this._activerow !== null) {
                    return data.at(this._activerow);
                }
                else
                    return null;
            };
            /**
             * Sort by specifed column
             * @param {Number} colIndex
             * @param {Boolean} desc
             */
            Grid.prototype.sort = function (colIndex, desc) {
                if (desc === void 0) { desc = false; }
                var columns = this.myColumns();
                if (colIndex === null) {
                    this._sortColumn = null;
                    this.myData().sort(null, desc);
                    this._sortDesc = false;
                }
                else if (typeof colIndex === "number" && !isNaN(colIndex) && colIndex >= 0 && colIndex < columns.length && columns[colIndex].sort) {
                    this._sortColumn = colIndex;
                    this._sortDesc = desc;
                    if (typeof columns[colIndex].sort === "function")
                        this.myData().sort(columns[colIndex].key, this._sortDesc, columns[colIndex].sort);
                    else
                        this.myData().sort(columns[colIndex].key, this._sortDesc);
                }
                this._sortDesc = !!desc;
                this._scrollTop = 0;
                this.activerow(null);
                this._initialized = false;
                this.refresh();
                return { colIndex: this._sortColumn, desc: this._sortDesc };
            };
            /**
             * Adjust column width to adapt column content
             * @param {Number} columnIndex
             * @param {Boolean} expandOnly Only expand column width
             * @param {Boolean} displayedOnly Only compute displayed lines,
             *		if this parameter is false then grid will compute all lines
             *		regardless of whether it is visible
             */
            Grid.prototype.autofitColumn = function (columnIndex, expandOnly, displayedOnly) {
                if (expandOnly === void 0) { expandOnly = false; }
                if (displayedOnly === void 0) { displayedOnly = true; }
                if (typeof (columnIndex) !== "number")
                    return;
                var columns = this.myColumns();
                if (columnIndex < 0 && columnIndex >= columns.length)
                    return;
                var col = columns[columnIndex];
                var maxWidth = 0;
                if (expandOnly)
                    maxWidth = col.width || 0;
                var cell = document.createElement("span");
                cell.className = "tui-grid-cell";
                cell.style.position = "absolute";
                cell.style.visibility = "hidden";
                cell.style.width = "auto";
                document.body.appendChild(cell);
                var data = this.myData();
                var key = null;
                if (typeof col.key !== tui.undef && col.key !== null) {
                    key = data.mapKey(col.key);
                    if (typeof key === tui.undef)
                        key = col.key;
                }
                var begin = displayedOnly ? this._bufferedBegin : 0;
                var end = displayedOnly ? this._bufferedEnd : data.length();
                for (var i = begin; i < end; i++) {
                    var rowData = data.at(i);
                    var v = rowData[key];
                    if (typeof v === "object" && v.nodeName) {
                        cell.innerHTML = "";
                        cell.appendChild(v);
                    }
                    else {
                        cell.innerHTML = v;
                    }
                    if (typeof col.format === "function")
                        col.format({
                            cell: cell,
                            value: v,
                            row: rowData,
                            col: col,
                            colKey: key,
                            rowIndex: i,
                            colIndex: columnIndex,
                            isRowActived: i === this._activerow,
                            grid: this
                        });
                    if (maxWidth < cell.offsetWidth - this._borderWidth)
                        maxWidth = cell.offsetWidth - this._borderWidth;
                }
                document.body.removeChild(cell);
                col.width = maxWidth;
                col["_important"] = true;
                this._initialized = false;
                this.refresh();
            };
            Grid.prototype.delayDrawing = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-delay-drawing", val);
                    this._delayDrawing = val;
                    return this;
                }
                else
                    return this._delayDrawing;
            };
            Grid.prototype.hasHScroll = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-has-hscroll", val);
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-has-hscroll");
            };
            Grid.prototype.noHead = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-no-head", val);
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-no-head");
            };
            Grid.prototype.columns = function (val) {
                if (val) {
                    this._columns = val;
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else {
                    if (!this._columns) {
                        var valstr = this.attr("data-columns");
                        this._columns = eval("(" + valstr + ")");
                    }
                    return this._columns;
                }
            };
            Grid.prototype.rowselectable = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-rowselectable", val);
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-rowselectable");
            };
            Grid.prototype.rowdraggable = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-rowdraggable", val);
                    return this;
                }
                else
                    return this.is("data-rowdraggable");
            };
            Grid.prototype.scrollTo = function (rowIndex) {
                if (typeof rowIndex !== "number" || isNaN(rowIndex) || rowIndex < 0 || rowIndex >= this.myData().length())
                    return;
                var v = this._vscroll.value();
                if (v > rowIndex * this._lineHeight) {
                    this._vscroll.value(rowIndex * this._lineHeight);
                    this._scrollTop = this._vscroll.value();
                    this.drawLines();
                }
                else {
                    var h = (rowIndex - this._dispLines + 1) * this._lineHeight;
                    var diff = (this._boxHeight - this.headHeight() - this._hscroll[0].offsetHeight - this._dispLines * this._lineHeight);
                    if (v < h - diff) {
                        this._vscroll.value(h - diff);
                        this._scrollTop = this._vscroll.value();
                        this.drawLines();
                    }
                }
            };
            Grid.prototype.editCell = function (rowIndex, colIndex) {
                if (typeof rowIndex !== "number" || rowIndex < 0 || rowIndex >= this.myData().length())
                    return false;
                if (typeof colIndex !== "number" || colIndex < 0 || colIndex >= this.columns().length)
                    return false;
                if (this.rowselectable()) {
                    this.activerow(rowIndex);
                }
                this.scrollTo(rowIndex);
                var line = this._bufferedLines[rowIndex - this._bufferedBegin];
                var cell = line.childNodes[colIndex];
                if (cell.childNodes[1] && cell.childNodes[1]["_ctrl"]) {
                    cell.childNodes[1]["_ctrl"].focus();
                    return true;
                }
                else if (cell.childNodes[0] && cell.childNodes[0].childNodes[0] && cell.childNodes[0].childNodes[0]["_ctrl"]) {
                    cell.childNodes[0].childNodes[0]["_ctrl"].focus();
                    return true;
                }
                else
                    return false;
            };
            Grid.prototype.editRow = function (rowIndex) {
                if (typeof rowIndex !== "number" || rowIndex < 0 || rowIndex >= this.myData().length())
                    return false;
                for (var i = 0; i < this._columns.length; i++) {
                    if (this.editCell(rowIndex, i))
                        return true;
                }
                return false;
            };
            Grid.prototype.resizable = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-resizable", val);
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-resizable");
            };
            Grid.prototype.value = function (data) {
                if (data === null) {
                    return this.data([]);
                }
                else if (data) {
                    return this.data(data);
                }
                else {
                    var result = [];
                    var dt = this.data();
                    for (var i = 0; i < dt.length(); i++) {
                        result.push(dt.at(i));
                    }
                    return result;
                }
            };
            Grid.prototype.data = function (data) {
                var _this = this;
                if (data) {
                    var self = this;
                    if (data instanceof Array || data.data && data.data instanceof Array) {
                        data = new tui.ArrayProvider(data);
                    }
                    if (typeof data.length !== "function" || typeof data.sort !== "function" || typeof data.at !== "function" || typeof data.columnKeyMap !== "function") {
                        this._initialized = false;
                        this.refresh();
                        throw new Error("TUI Grid: need a data provider.");
                    }
                    this._data && this._data.onupdate && this._data.onupdate(null);
                    this._data = data;
                    typeof this._data.onupdate === "function" && this._data.onupdate(function (updateInfo) {
                        var b = updateInfo.begin;
                        var e = b + updateInfo.data.length;
                        _this._initialized = false;
                        self.refresh();
                    });
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else {
                    return this.myData();
                }
            };
            Grid.prototype.consumeMouseWheelEvent = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-consume-mwe", val);
                    return this;
                }
                else
                    return this.is("data-consume-mwe");
            };
            Grid.prototype.noRefresh = function (val) {
                if (typeof val === "boolean") {
                    this._noRefresh = val;
                    return this;
                }
                else
                    return this._noRefresh;
            };
            Grid.prototype.refreshHead = function () {
                this.drawHead();
            };
            Grid.prototype.refresh = function () {
                if (this._noRefresh)
                    return;
                if (!this[0] || this[0].parentElement === null)
                    return;
                if (this[0].offsetWidth === 0 || this[0].offsetHeight === 0)
                    return;
                this._initialized = true;
                this.computeScroll();
                this.clearBufferLines();
                //			this._columnKeyMap = this.myData().columnKeyMap();
                this.drawHead();
                this.drawLines();
            };
            Grid.prototype.autoRefresh = function () {
                return !this._initialized;
            };
            /// Following static methods are used for cell formatting.
            Grid.menu = function (itemMenu, func, menuPos) {
                if (menuPos === void 0) { menuPos = "Rb"; }
                return function (data) {
                    if (data.rowIndex < 0)
                        return;
                    var tb = data.grid;
                    var array = data.grid.data().src();
                    data.cell.firstChild.innerHTML = "";
                    var btnMenu = tui.ctrl.button();
                    btnMenu.addClass("tui-grid-menu-button");
                    btnMenu.text("<i class='fa fa-bars'></i>");
                    if (typeof itemMenu === "function")
                        btnMenu.menu(itemMenu(data));
                    else
                        btnMenu.menu(itemMenu);
                    btnMenu.menuPos(menuPos);
                    data.cell.firstChild.appendChild(btnMenu[0]);
                    btnMenu.on("select", function (d) {
                        func && func(d.item, data);
                    });
                    $(btnMenu[0]).mousedown(function (e) {
                        data.grid.editCell(data.rowIndex, data.colIndex);
                        e.stopPropagation();
                        tui.fire("#tui.check.popup");
                    });
                    $(btnMenu[0]).keydown(function (e) {
                        handleKeyDownEvent(e, data, "button");
                    });
                };
            };
            Grid.button = function (text, func) {
                return function (data) {
                    if (data.rowIndex < 0)
                        return;
                    var tb = data.grid;
                    var array = data.grid.data().src();
                    data.cell.firstChild.innerHTML = "";
                    var btnMenu = tui.ctrl.button();
                    btnMenu.text(text);
                    data.cell.firstChild.appendChild(btnMenu[0]);
                    btnMenu.on("click", function () {
                        func && func(data);
                    });
                    $(btnMenu[0]).mousedown(function (e) {
                        data.grid.editCell(data.rowIndex, data.colIndex);
                        e.stopPropagation();
                        tui.fire("#tui.check.popup");
                    });
                    $(btnMenu[0]).keydown(function (e) {
                        handleKeyDownEvent(e, data, "button");
                    });
                };
            };
            Grid.checkbox = function (withHeader) {
                if (withHeader === void 0) { withHeader = true; }
                return function (data) {
                    if (data.rowIndex < 0) {
                        if (withHeader) {
                            var headCheck = tui.ctrl.checkbox();
                            data.cell.firstChild.innerHTML = "";
                            data.cell.firstChild.appendChild(headCheck[0]);
                            data.cell.style.textAlign = "center";
                            var dataSet = data.grid.data();
                            var totalLen = dataSet.length();
                            var checkedCount = 0;
                            var uncheckCount = 0;
                            for (var i = 0; i < totalLen; i++) {
                                if (dataSet.at(i)[data.colKey])
                                    checkedCount++;
                                else
                                    uncheckCount++;
                            }
                            if (totalLen === uncheckCount) {
                                headCheck.checked(false);
                                headCheck.triState(false);
                            }
                            else if (totalLen === checkedCount) {
                                headCheck.checked(true);
                                headCheck.triState(false);
                            }
                            else
                                headCheck.triState(true);
                            headCheck.on("click", function () {
                                if (typeof data.colKey !== tui.undef) {
                                    for (var i = 0; i < totalLen; i++) {
                                        dataSet.at(i)[data.colKey] = headCheck.checked();
                                    }
                                }
                                data.value = headCheck.checked();
                                data.grid.refresh();
                            });
                        }
                        return;
                    }
                    else {
                        data.cell.firstChild.innerHTML = "";
                        var chk = tui.ctrl.checkbox();
                        data.cell.firstChild.appendChild(chk[0]);
                        data.cell.style.textAlign = "center";
                        chk.checked(data.value);
                        chk.on("click", function () {
                            if (typeof data.colKey !== tui.undef)
                                data.row[data.colKey] = chk.checked();
                            data.value = chk.checked();
                            data.grid.refreshHead();
                        });
                        $(chk[0]).keydown(function (e) {
                            handleKeyDownEvent(e, data, "checkbox");
                        });
                    }
                };
            }; // end of chechBox
            Grid.textEditor = function (listData) {
                return createInputFormatter("text", listData);
            };
            Grid.selector = function (listData) {
                return createInputFormatter("select", listData);
            }; // end of selector
            Grid.fileSelector = function (address, accept) {
                return createInputFormatter("file", address, accept);
            }; // end of fileSelector
            Grid.calendarSelector = function () {
                return createInputFormatter("calendar");
            }; // end of calendarSelector
            Grid.customSelector = function (func, icon) {
                if (icon === void 0) { icon = "fa-ellipsis-h"; }
                return createInputFormatter("custom-select", func, icon);
            }; // end of calendarSelector
            Grid.CLASS = "tui-grid";
            return Grid;
        })(ctrl.Control);
        ctrl.Grid = Grid;
        function handleKeyDownEvent(e, data, type) {
            var k = e.keyCode;
            var col, row;
            if (k === tui.KEY_DOWN) {
                if (data.rowIndex < data.grid.data().length() - 1)
                    data.grid.editCell(data.rowIndex + 1, data.colIndex);
                e.stopPropagation();
                e.preventDefault();
            }
            else if (k === tui.KEY_UP) {
                if (data.rowIndex > 0)
                    data.grid.editCell(data.rowIndex - 1, data.colIndex);
                e.stopPropagation();
                e.preventDefault();
            }
            else if (k === tui.KEY_LEFT) {
                if (type !== "text" || e.ctrlKey) {
                    col = data.colIndex - 1;
                    while (col >= 0) {
                        if (data.grid.editCell(data.rowIndex, col--))
                            break;
                    }
                }
                e.stopPropagation();
            }
            else if (k === tui.KEY_RIGHT) {
                if (type !== "text" || e.ctrlKey) {
                    col = data.colIndex + 1;
                    while (col < data.grid.columns().length) {
                        if (data.grid.editCell(data.rowIndex, col++))
                            break;
                    }
                }
                e.stopPropagation();
            }
            else if (k === tui.KEY_TAB && e.shiftKey) {
                col = data.colIndex;
                row = data.rowIndex;
                while (row >= 0 && col >= 0) {
                    col--;
                    if (col < 0) {
                        col = data.grid.columns().length - 1;
                        row--;
                    }
                    if (data.grid.editCell(row, col))
                        break;
                }
                e.preventDefault();
                e.stopPropagation();
            }
            else if (k === tui.KEY_TAB) {
                col = data.colIndex;
                row = data.rowIndex;
                while (row < data.grid.data().length() && col < data.grid.columns().length) {
                    col++;
                    if (col >= data.grid.columns().length) {
                        col = 0;
                        row++;
                    }
                    if (data.grid.editCell(row, col))
                        break;
                }
                e.preventDefault();
                e.stopPropagation();
            }
            else {
                if (type === "text") {
                    data.grid.editCell(data.rowIndex, data.colIndex);
                }
            }
        }
        function createInputFormatter(type, param1, param2) {
            return function (data) {
                if (data.rowIndex < 0) {
                    return;
                }
                var editor = tui.ctrl.input(null, type);
                editor.useLabelClick(false);
                editor.addClass("tui-grid-editor");
                editor.on("select change", function () {
                    if (typeof data.colKey !== tui.undef)
                        data.row[data.colKey] = editor.value();
                    data.value = editor.value();
                });
                if (type === "text") {
                    if (param1)
                        editor.data(param1);
                }
                else if (type === "select") {
                    editor.data(param1);
                }
                else if (type === "custom-select") {
                    editor.on("btnclick", param1);
                    editor.icon(param2);
                }
                else if (type === "calendar") {
                }
                else if (type === "file") {
                    editor.uploadUrl(param1);
                    editor.accept(param2);
                }
                $(editor[0]).mousedown(function (e) {
                    data.grid.editCell(data.rowIndex, data.colIndex);
                    e.stopPropagation();
                    tui.fire("#tui.check.popup");
                });
                $(editor[0]).keydown(function (e) {
                    handleKeyDownEvent(e, data, type);
                });
                editor[0].style.width = $(data.cell).innerWidth() - 1 + "px";
                editor[0].style.height = $(data.cell).innerHeight() - 1 + "px";
                data.cell.appendChild(editor[0]);
                editor.value(data.value);
            };
        }
        /**
         * Construct a grid.
         * @param el {HTMLElement or element id or construct info}
         */
        function grid(param) {
            return tui.ctrl.control(param, Grid);
        }
        ctrl.grid = grid;
        tui.ctrl.registerInitCallback(Grid.CLASS, grid);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var TriState;
        (function (TriState) {
            TriState[TriState["Unchecked"] = 0] = "Unchecked";
            TriState[TriState["Checked"] = 1] = "Checked";
            TriState[TriState["HalfChecked"] = 2] = "HalfChecked";
        })(TriState || (TriState = {}));
        var List = (function (_super) {
            __extends(List, _super);
            //private _columnKeyMap: {} = null;
            function List(el) {
                var _this = this;
                _super.call(this, "div", List.CLASS, null);
                var self = this;
                this._grid = ctrl.grid(el);
                this._grid.noRefresh(true);
                this[0] = this._grid[0];
                this[0]._ctrl = this;
                this.addClass(List.CLASS);
                var columns = this._grid.columns();
                if (columns === null) {
                    this._grid.columns([{
                        key: "value",
                        format: function (info) {
                            if (info.rowIndex < 0)
                                return;
                            var rowcheckable = _this.rowcheckable();
                            var cell = info.cell.firstChild;
                            var itemIcon = info.row[_this._iconColumnKey];
                            var isExpanded = !!info.row[_this._expandColumnKey];
                            var hasCheckbox = (typeof info.row[_this._checkedColumnKey] !== tui.undef);
                            var isChecked = !!info.row[_this._checkedColumnKey];
                            var hasChild = !!info.row[_this._childrenColumKey] && info.row[_this._childrenColumKey].length > 0;
                            var isHalfChecked = (_this.triState() && info.row[_this._checkedColumnKey] === 2 /* HalfChecked */);
                            var spaceSpan = document.createElement("span");
                            spaceSpan.className = "tui-list-space";
                            var foldIcon = document.createElement("span");
                            foldIcon.className = "tui-list-fold";
                            if (hasChild) {
                                if (isExpanded) {
                                    $(foldIcon).addClass("tui-list-fold-expand");
                                    $(foldIcon).mousedown(function (e) {
                                        _this.onFoldRow(info.row, info.rowIndex, e);
                                    });
                                }
                                else {
                                    $(foldIcon).addClass("tui-list-fold-unexpand");
                                    $(foldIcon).mousedown(function (e) {
                                        _this.onExpandRow(info.row, info.rowIndex, e);
                                    });
                                }
                            }
                            if (typeof itemIcon === "string") {
                                var icon = document.createElement("i");
                                icon.className = "fa " + itemIcon;
                                icon.style.marginRight = "4px";
                                cell.insertBefore(icon, cell.firstChild);
                            }
                            if (hasCheckbox && rowcheckable) {
                                var checkIcon = document.createElement("span");
                                checkIcon.className = "tui-list-checkbox";
                                if (isChecked) {
                                    if (isHalfChecked)
                                        $(checkIcon).addClass("tui-half-checked");
                                    else
                                        $(checkIcon).addClass("tui-checked");
                                }
                                cell.insertBefore(checkIcon, cell.firstChild);
                                $(checkIcon).mouseup(function (e) {
                                    _this.onCheckRow(info.row, info.rowIndex, e);
                                });
                            }
                            cell.insertBefore(foldIcon, cell.firstChild);
                            cell.insertBefore(spaceSpan, cell.firstChild);
                            var singleWidth = spaceSpan.offsetWidth;
                            var level = info.row[_this._levelColumnKey];
                            spaceSpan.style.width = singleWidth * (typeof level === "number" ? level : 0) + "px";
                        }
                    }]);
                }
                this._grid.on("rowclick", function (data) {
                    return _this.fire("rowclick", data);
                });
                this._grid.on("rowdblclick", function (data) {
                    return _this.fire("rowdblclick", data);
                });
                this._grid.on("rowmousedown", function (data) {
                    return _this.fire("rowmousedown", data);
                });
                this._grid.on("rowdragstart", function (data) {
                    return _this.fire("rowdragstart", data);
                });
                this._grid.on("rowdragover", function (data) {
                    return _this.fire("rowdragover", data);
                });
                this._grid.on("rowdragend", function (data) {
                    return _this.fire("rowdragend", data);
                });
                this._grid.on("rowmouseup", function (data) {
                    return _this.fire("rowmouseup", data);
                });
                this._grid.on("rowmouseenter", function (data) {
                    return _this.fire("rowmouseenter", data);
                });
                this._grid.on("rowmouseleave", function (data) {
                    return _this.fire("rowmouseleave", data);
                });
                this._grid.on("rowcontextmenu", function (data) {
                    return _this.fire("rowcontextmenu", data);
                });
                this._grid.on("resizecolumn", function (data) {
                    return _this.fire("resizecolumn", data);
                });
                this._grid.on("contextmenu", function (data) {
                    return _this.fire("contextmenu", data);
                });
                this._grid.on("keydown", function (data) {
                    if (_this.fire("keydown", data) === false)
                        return false;
                    var keyCode = data["event"].keyCode;
                    if (keyCode === 32) {
                        var activeRowIndex = self._grid.activerow();
                        if (activeRowIndex >= 0) {
                            data["event"].preventDefault();
                            data["event"].stopPropagation();
                        }
                    }
                    else if (keyCode === 37) {
                        var activeRowIndex = self._grid.activerow();
                        var item = self._grid.activeItem();
                        if (item) {
                            var children = item[self._childrenColumKey];
                            if (children && children.length > 0 && item[self._expandColumnKey]) {
                                _this.onFoldRow(item, activeRowIndex, data["event"]);
                            }
                            else {
                                if (item["__parent"]) {
                                    self.activeItem(item["__parent"]);
                                    self.scrollTo(self.activerow());
                                    self.refresh();
                                }
                            }
                            data["event"].preventDefault();
                            data["event"].stopPropagation();
                        }
                    }
                    else if (keyCode === 39) {
                        var item = self._grid.activeItem();
                        if (item) {
                            var children = item[self._childrenColumKey];
                            if (children && children.length > 0 && !item[self._expandColumnKey]) {
                                _this.onExpandRow(item, activeRowIndex, data["event"]);
                            }
                            data["event"].preventDefault();
                            data["event"].stopPropagation();
                        }
                    }
                });
                this._grid.on("keyup", function (data) {
                    if (data["event"].keyCode === 32) {
                        var activeRowIndex = self._grid.activerow();
                        if (activeRowIndex >= 0) {
                            var row = self._grid.data().at(activeRowIndex);
                            _this.onCheckRow(row, activeRowIndex, data["event"]);
                        }
                    }
                    return _this.fire("keyup", data);
                });
                if (!this.hasAttr("data-no-head"))
                    this.noHead(true);
                if (!this.hasAttr("data-delay-drawing"))
                    this.delayDrawing(false);
                if (!this.hasAttr("data-rowselectable"))
                    this.rowselectable(true);
                if (!this.hasAttr("data-rowcheckable"))
                    this.rowcheckable(true);
                if (this._grid.data()) {
                    this._grid.noRefresh(false);
                    this.data(this._grid.data());
                }
                else {
                    this._grid.noRefresh(false);
                    this.refresh();
                }
            }
            List.prototype.checkChildren = function (children, checkState) {
                for (var i = 0; i < children.length; i++) {
                    if (!children[i])
                        continue;
                    if (typeof children[i][this._checkedColumnKey] !== tui.undef)
                        children[i][this._checkedColumnKey] = checkState;
                    var myChildren = children[i][this._childrenColumKey];
                    myChildren && myChildren.length > 0 && this.checkChildren(myChildren, checkState);
                }
            };
            List.prototype.checkParent = function (parent) {
                var children = parent[this._childrenColumKey];
                var checkedCount = 0, uncheckedCount = 0;
                for (var i = 0; i < children.length; i++) {
                    var row = children[i];
                    if (!row)
                        continue;
                    if (typeof row[this._checkedColumnKey] === tui.undef)
                        continue;
                    else if (row[this._checkedColumnKey] === 2 /* HalfChecked */) {
                        uncheckedCount++;
                        checkedCount++;
                        break;
                    }
                    else if (!!row[this._checkedColumnKey])
                        checkedCount++;
                    else
                        uncheckedCount++;
                }
                if (typeof parent[this._checkedColumnKey] !== tui.undef) {
                    if (checkedCount === 0)
                        parent[this._checkedColumnKey] = 0 /* Unchecked */;
                    else if (uncheckedCount === 0)
                        parent[this._checkedColumnKey] = 1 /* Checked */;
                    else
                        parent[this._checkedColumnKey] = 2 /* HalfChecked */;
                }
                parent["__parent"] && this.checkParent(parent["__parent"]);
            };
            List.prototype.checkRow = function (row, checkState) {
                if (typeof row[this._checkedColumnKey] !== tui.undef)
                    row[this._checkedColumnKey] = checkState;
                if (this.triState()) {
                    var children = row[this._childrenColumKey];
                    children && children.length > 0 && this.checkChildren(children, checkState);
                    var parent = row["__parent"];
                    parent && this.checkParent(parent);
                }
            };
            List.prototype.onCheckRow = function (row, rowIndex, event) {
                var checkState;
                if (this.triState()) {
                    checkState = row[this._checkedColumnKey];
                    if (checkState === 2 /* HalfChecked */ || !checkState)
                        checkState = 1 /* Checked */;
                    else
                        checkState = 0 /* Unchecked */;
                }
                else
                    checkState = !row[this._checkedColumnKey];
                this.checkRow(row, checkState);
                this.fire("rowcheck", { event: event, checked: row[this._checkedColumnKey], row: row, index: rowIndex });
                this.refresh();
            };
            List.prototype.expandRow = function (rowIndex) {
                var row = this.data().at(rowIndex);
                row[this._expandColumnKey] = true;
                this.formatData();
                this.fire("rowexpand", { row: row, index: rowIndex });
            };
            List.prototype.onExpandRow = function (row, rowIndex, event) {
                row[this._expandColumnKey] = true;
                this.formatData();
                this.fire("rowexpand", { event: event, row: row, index: rowIndex });
            };
            List.prototype.foldRow = function (rowIndex) {
                var row = this.data().at(rowIndex);
                row[this._expandColumnKey] = false;
                this.formatData();
                this.fire("rowfold", { row: row, index: rowIndex });
            };
            List.prototype.onFoldRow = function (row, rowIndex, event) {
                row[this._expandColumnKey] = false;
                this.formatData();
                this.fire("rowfold", { event: event, row: row, index: rowIndex });
            };
            List.prototype.initData = function (useTriState) {
                if (useTriState === void 0) { useTriState = false; }
                var self = this;
                var data = this._grid.data();
                if (data && typeof data.process === "function") {
                    function checkChildren(input, parentRow) {
                        var checkedCount = 0, uncheckedCount = 0;
                        for (var i = 0; i < input.length; i++) {
                            var row = input[i];
                            if (!row)
                                continue;
                            if (useTriState) {
                                if (row[self._childrenColumKey] && row[self._childrenColumKey].length > 0) {
                                    var state = checkChildren(row[self._childrenColumKey], row);
                                    row[self._checkedColumnKey] = state;
                                }
                                if (row[self._checkedColumnKey] === 2 /* HalfChecked */) {
                                    uncheckedCount++;
                                    checkedCount++;
                                }
                                else if (!!row[self._checkedColumnKey])
                                    checkedCount++;
                                else
                                    uncheckedCount++;
                            }
                            else {
                                if (row[self._childrenColumKey] && row[self._childrenColumKey].length > 0) {
                                    checkChildren(row[self._childrenColumKey], row);
                                }
                            }
                            row["__parent"] = parentRow;
                        }
                        if (useTriState) {
                            if (checkedCount === 0)
                                return 0 /* Unchecked */;
                            else if (uncheckedCount === 0)
                                return 1 /* Checked */;
                            else
                                return 2 /* HalfChecked */;
                        }
                    }
                    function processTree(input) {
                        checkChildren(input, null);
                        return input;
                    }
                    data.process(processTree);
                }
            };
            List.prototype.initTriState = function () {
                this.initData(true);
            };
            List.prototype.formatData = function () {
                var self = this;
                var data = this._grid.data();
                if (data && typeof data.process === "function") {
                    function addChildren(input, output, level) {
                        for (var i = 0; i < input.length; i++) {
                            var row = input[i];
                            if (!row)
                                continue;
                            output.push(row);
                            row[self._levelColumnKey] = level;
                            if (!!row[self._expandColumnKey] && row[self._childrenColumKey] && row[self._childrenColumKey].length > 0) {
                                addChildren(row[self._childrenColumKey], output, level + 1);
                            }
                        }
                    }
                    function processTree(input) {
                        var output = [];
                        addChildren(input, output, 0);
                        return output;
                    }
                    data.process(processTree);
                }
                this.refresh();
            };
            List.prototype.select = function (rows) {
                return this._grid.select(rows);
            };
            List.prototype.activerow = function (rowIndex) {
                return this._grid.activerow(rowIndex);
            };
            List.prototype.activeItem = function (rowItem) {
                if (typeof rowItem !== tui.undef) {
                    if (rowItem) {
                        var parent = rowItem["__parent"];
                        while (parent) {
                            parent[this._expandColumnKey] = true;
                            parent = parent["__parent"];
                        }
                        this.formatData();
                    }
                }
                return this._grid.activeItem(rowItem);
            };
            List.prototype.activeRowByKey = function (key) {
                var self = this;
                var activeRow = null;
                function checkChildren(children) {
                    for (var i = 0; i < children.length; i++) {
                        if (!children[i])
                            continue;
                        if (children[i][self._keyColumKey] === key) {
                            activeRow = children[i];
                            return true;
                        }
                        var myChilren = children[i][self._childrenColumKey];
                        if (myChilren && myChilren.length > 0)
                            if (checkChildren(myChilren))
                                return true;
                    }
                }
                var data = this._grid.data();
                if (typeof data.src === "function") {
                    if (checkChildren(data.src())) {
                        return this.activeItem(activeRow);
                    }
                    else
                        return this.activeItem(null);
                }
                else
                    return null;
            };
            List.prototype.doCheck = function (keys, checkState) {
                var self = this;
                //var useTriState = this.triState();
                var map = {};
                if (keys) {
                    for (var i = 0; i < keys.length; i++) {
                        map[keys[i]] = true;
                    }
                }
                function checkChildren(keys, children) {
                    for (var i = 0; i < children.length; i++) {
                        if (!children[i])
                            continue;
                        if (keys === null || map[children[i][self._keyColumKey]]) {
                            //children[i][self._checkedColumnKey] = checkState;
                            self.checkRow(children[i], checkState);
                        }
                        var myChilren = children[i][self._childrenColumKey];
                        if (myChilren && myChilren.length > 0)
                            checkChildren(keys, myChilren);
                    }
                }
                var data = this._grid.data();
                if (data && typeof data.src === "function") {
                    checkChildren(keys, data.src());
                    //if (useTriState) {
                    //	this.initTriState();
                    //}
                    this.refresh();
                }
            };
            List.prototype.checkItems = function (keys) {
                this.doCheck(keys, 1 /* Checked */);
                return this;
            };
            List.prototype.checkAllItems = function () {
                this.doCheck(null, 1 /* Checked */);
                return this;
            };
            List.prototype.uncheckItems = function (keys) {
                this.doCheck(keys, 0 /* Unchecked */);
                return this;
            };
            List.prototype.uncheckAllItems = function () {
                this.doCheck(null, 0 /* Unchecked */);
                return this;
            };
            List.prototype.checkedItems = function () {
                var self = this;
                var checkedItems = [];
                function checkChildren(children) {
                    for (var i = 0; i < children.length; i++) {
                        if (!children[i])
                            continue;
                        if (!!children[i][self._checkedColumnKey])
                            checkedItems.push(children[i]);
                        var myChilren = children[i][self._childrenColumKey];
                        if (myChilren && myChilren.length > 0)
                            checkChildren(myChilren);
                    }
                }
                var data = this._grid.data();
                if (data && typeof data.src === "function") {
                    checkChildren(data.src());
                }
                return checkedItems;
            };
            List.prototype.allKeys = function (onlyCheckable) {
                var self = this;
                var keys = [];
                function checkChildren(children) {
                    for (var i = 0; i < children.length; i++) {
                        if (!children[i])
                            continue;
                        var k = children[i][self._keyColumKey];
                        if ((onlyCheckable && typeof children[i][self._checkedColumnKey] !== tui.undef || !onlyCheckable) && typeof k !== tui.undef && k !== null)
                            keys.push(k);
                        var myChilren = children[i][self._childrenColumKey];
                        if (myChilren && myChilren.length > 0)
                            checkChildren(myChilren);
                    }
                }
                var data = this._grid.data();
                if (data && typeof data.src === "function") {
                    checkChildren(data.src());
                }
                return keys;
            };
            List.prototype.enumerate = function (func) {
                var self = this;
                var checkedItems = [];
                function enumChildren(children) {
                    for (var i = 0; i < children.length; i++) {
                        func(children[i]);
                        if (!children[i])
                            continue;
                        var myChilren = children[i][self._childrenColumKey];
                        if (myChilren && myChilren.length > 0)
                            enumChildren(myChilren);
                    }
                }
                var data = this._grid.data();
                if (data && typeof data.src === "function") {
                    enumChildren(data.src());
                }
            };
            /**
             * Adjust column width to adapt column content
             * @param {Number} columnIndex
             * @param {Boolean} expandOnly Only expand column width
             * @param {Boolean} displayedOnly Only compute displayed lines,
             *		if this parameter is false then grid will compute all lines
             *		regardless of whether it is visible
             */
            List.prototype.autofitColumn = function (columnIndex, expandOnly, displayedOnly) {
                if (expandOnly === void 0) { expandOnly = false; }
                if (displayedOnly === void 0) { displayedOnly = true; }
                this._grid.autofitColumn(columnIndex, expandOnly, displayedOnly);
            };
            List.prototype.delayDrawing = function (val) {
                return this._grid.delayDrawing(val);
            };
            List.prototype.noHead = function (val) {
                return this._grid.noHead(val);
            };
            List.prototype.hasHScroll = function (val) {
                return this._grid.hasHScroll(val);
            };
            List.prototype.columns = function (val) {
                return this._grid.columns(val);
            };
            List.prototype.rowdraggable = function (val) {
                return this._grid.rowdraggable(val);
            };
            List.prototype.rowselectable = function (val) {
                return this._grid.rowselectable(val);
            };
            List.prototype.rowcheckable = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-rowcheckable", val);
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-rowcheckable");
            };
            List.prototype.consumeMouseWheelEvent = function (val) {
                return this._grid.consumeMouseWheelEvent(val);
            };
            List.prototype.triState = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-tri-state", val);
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-tri-state");
            };
            List.prototype.scrollTo = function (rowIndex) {
                this._grid.scrollTo(rowIndex);
            };
            List.prototype.value = function (keys) {
                if (typeof keys !== tui.undef) {
                    this.uncheckAllItems();
                    if (keys != null)
                        this.checkItems(keys);
                    return this;
                }
                else {
                    var items = this.checkedItems();
                    var result = [];
                    for (var i = 0; i < items.length; i++) {
                        if (typeof items[i][this._keyColumKey] !== tui.undef)
                            result.push(items[i][this._keyColumKey]);
                    }
                    return result;
                }
            };
            List.prototype.data = function (data) {
                if (typeof data !== tui.undef) {
                    var noRef = this._grid.noRefresh();
                    this._grid.noRefresh(true);
                    this._grid.data(data);
                    var finalData = this._grid.data();
                    this._keyColumKey = finalData.mapKey("key");
                    this._childrenColumKey = finalData.mapKey("children");
                    this._checkedColumnKey = finalData.mapKey("checked");
                    this._levelColumnKey = finalData.mapKey("level");
                    this._valueColumnKey = finalData.mapKey("value");
                    this._expandColumnKey = finalData.mapKey("expand");
                    this._iconColumnKey = finalData.mapKey("icon");
                    if (this.triState())
                        this.initTriState();
                    else
                        this.initData();
                    this._grid.noRefresh(noRef);
                    this.formatData();
                    return this;
                }
                else
                    return this._grid.data();
            };
            List.prototype.lineHeight = function () {
                if (!this._grid)
                    return 0;
                return this._grid.lineHeight();
            };
            List.prototype.refresh = function () {
                if (!this._grid)
                    return;
                this._grid.refresh();
            };
            List.CLASS = "tui-list";
            return List;
        })(ctrl.Control);
        ctrl.List = List;
        /**
         * Construct a grid.
         * @param el {HTMLElement or element id or construct info}
         */
        function list(param) {
            return tui.ctrl.control(param, List);
        }
        ctrl.list = list;
        tui.ctrl.registerInitCallback(List.CLASS, list);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.core.ts" />
/// <reference path="tui.upload.ts" />
/// <reference path="tui.ctrl.popup.ts" />
/// <reference path="tui.ctrl.calendar.ts" />
/// <reference path="tui.ctrl.list.ts" />
/// <reference path="tui.ctrl.form.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        function validText(t) {
            if (typeof t === tui.undef || t === null) {
                return "";
            }
            else {
                return t + "";
            }
        }
        function getKeys(items) {
            var keys = [];
            for (var i = 0; i < items.length; i++) {
                var key = items[i]["key"];
                if (typeof key !== tui.undef)
                    keys.push(key);
            }
            return keys;
        }
        var Input = (function (_super) {
            __extends(Input, _super);
            function Input(el, type) {
                var _this = this;
                _super.call(this, "span", Input.CLASS, el === null ? undefined : el);
                this._fileId = null;
                this._binding = null;
                this._invalid = false;
                this._message = "";
                this._data = null;
                this._columnKeyMap = null;
                // Whether has been initialized.
                this._initialized = false;
                // For text suggestion
                this._suggestionList = null;
                this._suggestionPopup = null;
                this._suggestionText = null;
                this.openPopup = (function () {
                    var self = _this;
                    return function (e) {
                        if (self.type() === "calendar") {
                            self.showCalendar();
                        }
                        else if (self.type() === "select") {
                            self.showSingleSelect();
                        }
                        else if (self.type() === "multi-select") {
                            self.showMultiSelect();
                        }
                        else if (self.type() === "file") {
                        }
                        else {
                            self.fire("btnclick", { "ctrl": self[0], "event": e });
                        }
                    };
                })();
                var self = this;
                this._button = document.createElement("span");
                this._label = document.createElement("label");
                this._notify = document.createElement("div");
                this[0].innerHTML = "";
                this[0].appendChild(this._label);
                this[0].appendChild(this._button);
                this[0].appendChild(this._notify);
                this[1] = this._label;
                this[2] = this._button;
                if (typeof type !== tui.undef)
                    this.type(type);
                else
                    this.type(this.type());
                $(this._button).on("click", this.openPopup);
                $(this._label).on("mousedown", function (e) {
                    if (!_this.useLabelClick())
                        return;
                    if (!_this.disabled() && (_this.type() === "text" || _this.type() === "password" || _this.type() === "custom-text"))
                        setTimeout(function () {
                            _this._textbox.focus();
                        }, 0);
                    else if (_this.type() === "select" || _this.type() === "multi-select" || _this.type() === "calendar") {
                        _this.openPopup(e);
                    }
                    else if (_this.type() === "file") {
                    }
                });
                $(this[0]).on("keydown", function (e) {
                    if (e.keyCode !== 32)
                        return;
                    if (_this.type() === "select" || _this.type() === "multi-select" || _this.type() === "calendar") {
                        e.preventDefault();
                        e.stopPropagation();
                    }
                });
                $(this[0]).on("keyup", function (e) {
                    if (e.keyCode !== 32)
                        return;
                    if (_this.type() === "select" || _this.type() === "multi-select" || _this.type() === "calendar") {
                        _this.openPopup(e);
                        e.preventDefault();
                        e.stopPropagation();
                    }
                });
                if (this.type() === "text" || this.type() === "custom-text" || this.type() === "select" || this.type() === "multi-select") {
                    var predefined = this.attr("data-data");
                    if (predefined)
                        predefined = eval("(" + predefined + ")");
                    if (predefined)
                        this.data(predefined);
                }
                if (!this.hasAttr("data-label-click"))
                    this.useLabelClick(true);
                if (!this.hasAttr("data-empty-suggestion"))
                    this.emptySuggestion(true);
                this.value(this.value());
            }
            Input.prototype.doSubmit = function () {
                var formId = this.submitForm();
                if (formId) {
                    var form = tui.ctrl.form(formId);
                    form && form.submit();
                }
            };
            Input.prototype.createTextbox = function () {
                var _this = this;
                var self = this;
                var type = this.type();
                if (this._textbox) {
                    this[0].removeChild(this._textbox);
                }
                this._textbox = document.createElement("input");
                this[3] = this._textbox;
                if (type === "password") {
                    this._textbox.type = "password";
                }
                else {
                    this._textbox.type = "text";
                }
                // Should put textbox before button
                this[0].insertBefore(this._textbox, this._button);
                // Bind events ...
                $(this._textbox).on("focus", function () {
                    $(_this[0]).addClass("tui-focus");
                });
                $(this._textbox).on("blur", function () {
                    $(_this[0]).removeClass("tui-focus");
                });
                $(this._textbox).on("propertychange", function (e) {
                    if (e.originalEvent.propertyName !== 'value')
                        return;
                    setTimeout(function () {
                        if (_this.text() !== _this._textbox.value) {
                            _this.text(_this._textbox.value);
                            self.openSuggestion(self._textbox.value);
                            self.fire("change", { "ctrl": _this[0], "event": e, "text": _this.text() });
                        }
                    }, 0);
                });
                $(this._textbox).on("change", function (e) {
                    if (_this.text() !== _this._textbox.value) {
                        _this.text(_this._textbox.value);
                        self.openSuggestion(self._textbox.value);
                        _this.fire("change", { "ctrl": _this[0], "event": e, "text": _this.text() });
                    }
                });
                $(this._textbox).on("input", function (e) {
                    setTimeout(function () {
                        if (_this.text() !== _this._textbox.value) {
                            _this.text(_this._textbox.value);
                            self.openSuggestion(self._textbox.value);
                            self.fire("change", { "ctrl": self[0], "event": e, "text": self.text() });
                        }
                    }, 0);
                });
                $(this._textbox).keydown(function (e) {
                    //if (!tui.CONTROL_KEYS[e.keyCode]) {
                    //	setTimeout(function () {
                    //		self.openSuggestion(self._textbox.value);
                    //	}, 0);
                    //}
                    if (self._suggestionList) {
                        var list = self._suggestionList;
                        if (e.keyCode === tui.KEY_DOWN) {
                            var r = list.activerow();
                            if (r === null || r >= list.data().length() - 1)
                                list.activerow(0);
                            else
                                list.activerow(r + 1);
                            if (list.activeItem()) {
                                list.scrollTo(list.activerow());
                                self.value(list.activeItem().key);
                            }
                            e.preventDefault();
                            e.stopPropagation();
                        }
                        else if (e.keyCode === tui.KEY_UP) {
                            var r = list.activerow();
                            if (r === null || r <= 0)
                                list.activerow(list.data().length() - 1);
                            else
                                list.activerow(r - 1);
                            if (list.activeItem()) {
                                list.scrollTo(list.activerow());
                                self.value(list.activeItem().key);
                            }
                            e.preventDefault();
                            e.stopPropagation();
                        }
                        else if (e.keyCode === tui.KEY_ENTER) {
                            if (self.readonly()) {
                                self._suggestionPopup.close();
                                self._textbox.focus();
                                return false;
                            }
                            if (list.activeItem()) {
                                self.value(list.activeItem().key);
                                self._suggestionPopup.close();
                                self._textbox.focus();
                                self.fire("change", { "ctrl": self[0], "event": e, "text": self.text() });
                            }
                        }
                    }
                    if (e.keyCode === tui.KEY_ENTER) {
                        self.fire("enter", { "ctrl": self[0], "event": e, "text": self.text() });
                        _this.doSubmit();
                    }
                });
            };
            Input.prototype.showCalendar = function () {
                var self = this;
                var pop = tui.ctrl.popup();
                var calendar = tui.ctrl.calendar();
                calendar.timepart(this.timepart());
                calendar.time(self.value());
                calendar.on("picked", function (e) {
                    if (self.readonly()) {
                        pop.close();
                        self.focus();
                        return false;
                    }
                    self.value(e["time"]);
                    pop.close();
                    self.focus();
                    if (self.fire("select", { ctrl: self[0], type: self.type(), time: e["time"] }) === false)
                        return;
                    self.doSubmit();
                });
                var calbox = document.createElement("div");
                calbox.appendChild(calendar[0]);
                var todayLink = document.createElement("a");
                todayLink.innerHTML = "<i class='fa fa-clock-o'></i> " + tui.str("Today") + ": " + tui.formatDate(tui.today(), "yyyy-MM-dd");
                todayLink.href = "javascript:void(0)";
                $(todayLink).click(function (e) {
                    if (self.readonly()) {
                        pop.close();
                        self.focus();
                        return false;
                    }
                    self.value(tui.today());
                    pop.close();
                    self.focus();
                    if (self.fire("select", { ctrl: self[0], type: self.type(), time: e["time"] }) === false)
                        return;
                    self.doSubmit();
                });
                var todayLine = document.createElement("div");
                todayLine.appendChild(todayLink);
                todayLine.className = "tui-input-select-bar";
                if (self.clearable()) {
                    var space = document.createElement("span");
                    space.className = "bar-space";
                    var clearLink = document.createElement("a");
                    clearLink.innerHTML = "<i class='fa fa-trash-o'></i>";
                    clearLink.href = "javascript:void(0)";
                    $(clearLink).click(function (e) {
                        if (self.readonly()) {
                            pop.close();
                            self.focus();
                            return false;
                        }
                        self.value(null);
                        pop.close();
                        self.focus();
                        if (self.fire("select", { ctrl: self[0], type: self.type(), time: e["time"] }) === false)
                            return;
                        self.doSubmit();
                    });
                    todayLine.appendChild(space);
                    todayLine.appendChild(clearLink);
                }
                calbox.appendChild(todayLine);
                pop.show(calbox, self[0], "Rb");
                calendar.focus();
            };
            Input.prototype.showSingleSelect = function () {
                var self = this;
                var pop = tui.ctrl.popup();
                var list = tui.ctrl.list();
                list.consumeMouseWheelEvent(true);
                list.rowcheckable(false);
                list.on("rowclick", function (data) {
                    if (self.readonly()) {
                        pop.close();
                        self.focus();
                        return false;
                    }
                    self.selectValue(self.getKeyValue([list.activeItem()]));
                    pop.close();
                    self.focus();
                    if (self.fire("select", { ctrl: self[0], type: self.type(), item: list.activeItem() }) === false)
                        return;
                    self.doSubmit();
                });
                list.on("keydown", function (data) {
                    if (data["event"].keyCode === 13) {
                        if (self.readonly()) {
                            pop.close();
                            self.focus();
                            return false;
                        }
                        if (list.activeItem())
                            self.selectValue(self.getKeyValue([list.activeItem()]));
                        else
                            self.selectValue(null);
                        pop.close();
                        self.focus();
                        if (self.fire("select", { ctrl: self[0], type: self.type(), item: list.activeItem() }) === false)
                            return;
                        self.doSubmit();
                    }
                });
                list[0].style.width = self[0].offsetWidth - 2 + "px";
                list.data(self._data);
                var calbox = document.createElement("div");
                calbox.appendChild(list[0]);
                if (self.clearable()) {
                    var bar = document.createElement("div");
                    bar.className = "tui-input-select-bar";
                    var clearLink = document.createElement("a");
                    clearLink.innerHTML = "<i class='fa fa-trash-o'></i> " + tui.str("Clear");
                    clearLink.href = "javascript:void(0)";
                    $(clearLink).click(function (e) {
                        if (self.readonly()) {
                            pop.close();
                            self.focus();
                            return false;
                        }
                        self.selectValue(null);
                        pop.close();
                        self.focus();
                        if (self.fire("select", { ctrl: self[0], type: self.type(), item: list.activeItem() }) === false)
                            return;
                        self.doSubmit();
                    });
                    bar.appendChild(clearLink);
                    calbox.appendChild(bar);
                }
                pop.show(calbox, self[0], "Rb");
                var items = self._data ? self._data.length() : 0;
                if (items < 1)
                    items = 1;
                else if (items > 8)
                    items = 8;
                list[0].style.height = items * list.lineHeight() + 4 + "px";
                list.refresh();
                pop.refresh();
                var val = this.selectValue();
                if (val && val.length > 0) {
                    list.activeRowByKey(val[0].key);
                    list.scrollTo(list.activerow());
                }
                list.focus();
            };
            Input.prototype.showMultiSelect = function () {
                var self = this;
                var pop = tui.ctrl.popup();
                var list = tui.ctrl.list();
                list.consumeMouseWheelEvent(true);
                var calbox = document.createElement("div");
                calbox.appendChild(list[0]);
                list[0].style.width = self[0].offsetWidth - 2 + "px";
                list.data(self._data);
                var allKeys = list.allKeys(true);
                list.uncheckAllItems();
                var keys = getKeys(this.selectValue());
                list.checkItems(keys);
                calbox.appendChild(list[0]);
                var bar = document.createElement("div");
                bar.className = "tui-input-select-bar";
                calbox.appendChild(bar);
                var selAll = tui.ctrl.checkbox();
                selAll.text("All");
                selAll.on("click", function () {
                    if (selAll.checked())
                        list.checkAllItems();
                    else
                        list.uncheckAllItems();
                });
                var okLink = document.createElement("a");
                okLink.innerHTML = "<i class='fa fa-check'></i> " + tui.str("Accept");
                okLink.href = "javascript:void(0)";
                $(okLink).click(function (e) {
                    if (self.readonly()) {
                        pop.close();
                        self.focus();
                        return false;
                    }
                    self.selectValue(self.getKeyValue(list.checkedItems()));
                    pop.close();
                    self.focus();
                    if (self.fire("select", { ctrl: self[0], type: self.type(), checkedItems: list.checkedItems() }) === false)
                        return;
                    self.doSubmit();
                });
                function isAllChecked() {
                    if (list.checkedItems().length >= allKeys.length)
                        selAll.checked(true);
                    else
                        selAll.checked(false);
                }
                isAllChecked();
                list.on("rowcheck", function (data) {
                    isAllChecked();
                });
                list.on("keydown", function (data) {
                    if (data["event"].keyCode === 13) {
                        if (self.readonly()) {
                            pop.close();
                            self.focus();
                            return false;
                        }
                        self.selectValue(self.getKeyValue(list.checkedItems()));
                        pop.close();
                        self.focus();
                        if (self.fire("select", { ctrl: self[0], type: self.type(), checkedItems: list.checkedItems() }) === false)
                            return;
                        self.doSubmit();
                    }
                });
                bar.appendChild(selAll[0]);
                var space = document.createElement("span");
                space.className = "bar-space";
                // bar.appendChild(document.createTextNode(" | "));
                bar.appendChild(space);
                bar.appendChild(okLink);
                pop.show(calbox, self[0], "Rb");
                var items = self._data ? self._data.length() : 0;
                if (items < 1)
                    items = 1;
                else if (items > 8)
                    items = 8;
                list[0].style.height = items * list.lineHeight() + 4 + "px";
                list.refresh();
                pop.refresh();
                list.focus();
            };
            Input.prototype.makeFileUpload = function () {
                var _this = this;
                if (this._binding)
                    return;
                this._binding = tui.bindUpload(this._button, {
                    action: this.uploadUrl(),
                    name: 'file',
                    autoSubmit: true,
                    accept: this.accept(),
                    responseType: "json"
                });
                this._binding.on("change", function (data) {
                    _this.focus();
                    var result = _this.validate(data["file"]);
                    if (!result) {
                        _this.value(null);
                        _this._invalid = true;
                        _this.refresh();
                    }
                    return result;
                });
                this._binding.on("complete", function (data) {
                    data["ctrl"] = _this[0];
                    if (_this.fire("complete", data) === false)
                        return;
                    var response = data["response"];
                    if (response) {
                        response.file = data["file"];
                        _this.value(response);
                        if (_this.fire("select", { ctrl: _this[0], type: _this.type(), file: response }) === false)
                            return;
                    }
                    else {
                        tui.errbox(tui.str("Upload failed, please check file type!"), tui.str("Error"));
                    }
                });
            };
            Input.prototype.unmakeFileUpload = function () {
                if (this._binding) {
                    this._binding.uninstallBind();
                    this._binding = null;
                }
            };
            Input.prototype.formatSelectText = function (val) {
                var text = "";
                for (var i = 0; i < val.length; i++) {
                    if (text.length > 0)
                        text += "; ";
                    text += validText(val[this._valueColumnKey]);
                }
                return text;
            };
            Input.prototype.formatSelectTextByData = function (val) {
                var self = this;
                var map = {};
                function buildMap(children) {
                    for (var i = 0; i < children.length; i++) {
                        if (!children[i])
                            continue;
                        var k = children[i][self._keyColumKey];
                        map[k] = children[i][self._valueColumnKey];
                        var myChildren = children[i][self._childrenColumKey];
                        if (myChildren && myChildren.length > 0) {
                            buildMap(myChildren);
                        }
                    }
                }
                var data = this._data;
                data && typeof data.src === "function" && buildMap(data.src());
                var text = "";
                for (var i = 0; i < val.length; i++) {
                    if (text.length > 0)
                        text += ", ";
                    var t = map[val[i].key];
                    if (typeof t === tui.undef)
                        t = validText(val[i].value);
                    else
                        t = validText(t);
                    text += t;
                }
                return text;
            };
            Input.prototype.columnKey = function (key) {
                var val = this._columnKeyMap[key];
                if (typeof val === "number" && val >= 0)
                    return val;
                else if (typeof val === "string")
                    return val;
                else
                    return key;
            };
            Input.prototype.getKeyValue = function (value) {
                var result = [];
                for (var i = 0; i < value.length; i++) {
                    if (typeof value[i][this._keyColumKey] !== tui.undef) {
                        var item = { key: value[i][this._keyColumKey] };
                        if (typeof value[i][this._valueColumnKey] !== tui.undef)
                            item.value = value[i][this._valueColumnKey];
                        result.push(item);
                    }
                }
                return result;
            };
            Input.prototype.openSuggestion = function (text) {
                if (this.type() !== "text" && this.type() !== "custom-text") {
                    this._suggestionPopup && this._suggestionPopup.close();
                    return;
                }
                if (!this._data || (!this.emptySuggestion() && (!text || text.length === 0))) {
                    this._suggestionPopup && this._suggestionPopup.close();
                    return;
                }
                if (this._suggestionText === text) {
                    return;
                }
                var max = this.maxSuggestions();
                var suggestions = [];
                for (var i = 0; i < this._data.length(); i++) {
                    var val = this._data.cell(i, "value");
                    var m = -1;
                    if (typeof val !== tui.undef && val !== null)
                        m = val.toLowerCase().indexOf(text.toLowerCase());
                    var sug = "";
                    if (m >= 0) {
                        sug += val.substring(0, m);
                        sug += "<b style='color:#000'>" + val.substr(m, text.length) + "</b>";
                        sug += val.substring(m + text.length);
                        suggestions.push({ key: val, value: sug });
                    }
                    else {
                        var k = this._data.cell(i, "key");
                        if (typeof k !== tui.undef && k !== null) {
                            m = (k + "").toLowerCase().indexOf(text.toLowerCase());
                            if (m >= 0) {
                                suggestions.push({ key: val, value: val });
                            }
                        }
                    }
                    if (max && suggestions.length >= max)
                        break;
                }
                if (suggestions.length === 0) {
                    this._suggestionPopup && this._suggestionPopup.close();
                    return;
                }
                this._suggestionText = text;
                suggestions.sort(function (a, b) {
                    return a.key.localeCompare(b.key);
                });
                var self = this;
                var pop = this._suggestionPopup;
                var list = this._suggestionList;
                if (this._suggestionPopup === null) {
                    pop = this._suggestionPopup = tui.ctrl.popup();
                    list = this._suggestionList = tui.ctrl.list();
                    this._suggestionPopup.owner(self._textbox);
                    list.consumeMouseWheelEvent(true);
                    list.rowcheckable(false);
                    list.on("rowmousedown", function (data) {
                        if (self.readonly()) {
                            pop.close();
                            self._textbox.focus();
                            return false;
                        }
                        self.value(list.activeItem().key);
                        pop.close();
                        self._textbox.focus();
                        self.fire("change", { "ctrl": self[0], "event": data["event"], "text": self.text() });
                        return;
                        self.doSubmit();
                    });
                    pop.on("close", function () {
                        self._suggestionPopup = null;
                        self._suggestionList = null;
                        self._suggestionText = null;
                    });
                    pop.show(list[0], self[0], "Rb");
                }
                list[0].style.width = self[0].offsetWidth - 2 + "px";
                list.data(suggestions);
                list.activerow(null);
                var items = suggestions.length;
                if (items < 1)
                    items = 1;
                else if (items > 8)
                    items = 8;
                list[0].style.height = items * list.lineHeight() + 4 + "px";
                list.refresh();
                pop.refresh();
            };
            Input.prototype.useLabelClick = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-label-click", val);
                    return this;
                }
                else
                    return this.is("data-label-click");
            };
            Input.prototype.fileId = function () {
                return this._fileId;
            };
            Input.prototype.type = function (txt) {
                var type;
                if (typeof txt === "string") {
                    txt = txt.toLowerCase();
                    if (Input._supportType.indexOf(txt) >= 0) {
                        this.attr("data-type", txt);
                    }
                    else
                        this.attr("data-type", "text");
                    type = this.type();
                    if (type === "text" || type === "password" || type === "custom-text") {
                        this.removeAttr("tabIndex");
                    }
                    else {
                        this.attr("tabIndex", "0");
                    }
                    this.createTextbox();
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else {
                    type = this.attr("data-type");
                    if (!type)
                        return "text";
                    else
                        type = type.toLowerCase();
                    if (Input._supportType.indexOf(type) >= 0) {
                        return type;
                    }
                    else
                        return "text";
                }
            };
            Input.prototype.validator = function (val) {
                if (typeof val === "object" && val) {
                    this.attr("data-validator", JSON.stringify(val));
                    this._invalid = false;
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else if (val === null) {
                    this.removeAttr("data-validator");
                    this._invalid = false;
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else {
                    val = this.attr("data-validator");
                    if (val === null) {
                        return null;
                    }
                    else {
                        try {
                            val = eval("(" + val + ")");
                            if (typeof val !== "object")
                                return null;
                            else
                                return val;
                        }
                        catch (err) {
                            return null;
                        }
                    }
                }
            };
            Input.prototype.emptySuggestion = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-empty-suggestion", val);
                    return this;
                }
                else
                    return this.is("data-empty-suggestion");
            };
            Input.prototype.maxSuggestions = function (val) {
                if (typeof val === "number") {
                    this.attr("data-max-suggestions", Math.floor(val));
                    return this;
                }
                else {
                    val = parseInt(this.attr("data-max-suggestions"));
                    if (isNaN(val))
                        return null;
                    else
                        return val;
                }
            };
            Input.prototype.validate = function (txt) {
                var finalText = typeof txt === "string" ? txt : this.text();
                if (finalText === null)
                    finalText = "";
                this._invalid = false;
                var validator = this.validator();
                if (validator) {
                    for (var k in validator) {
                        if (k && validator.hasOwnProperty(k)) {
                            if (k === "*password") {
                                if (!/[a-z]/.test(finalText) || !/[A-Z]/.test(finalText) || !/[0-9]/.test(finalText) || !/[\~\`\!\@\#\$\%\^\&\*\(\)\_\-\+\=\\\]\[\{\}\:\;\"\'\/\?\,\.\<\>\|]/.test(finalText) || finalText.length < 6) {
                                    this._invalid = true;
                                }
                            }
                            else if (k.substr(0, 8) === "*maxlen:") {
                                var imaxLen = parseFloat(k.substr(8));
                                if (isNaN(imaxLen))
                                    throw new Error("Invalid validator: '*maxlen:...' must follow a number");
                                var ival = finalText.length;
                                if (ival > imaxLen) {
                                    this._invalid = true;
                                }
                            }
                            else if (k.substr(0, 8) === "*minlen:") {
                                var iminLen = parseFloat(k.substr(8));
                                if (isNaN(iminLen))
                                    throw new Error("Invalid validator: '*iminLen:...' must follow a number");
                                var ival = finalText.length;
                                if (ival < iminLen) {
                                    this._invalid = true;
                                }
                            }
                            else if (k.substr(0, 5) === "*max:") {
                                var imax = parseFloat(k.substr(5));
                                if (isNaN(imax))
                                    throw new Error("Invalid validator: '*max:...' must follow a number");
                                var ival = parseFloat(finalText);
                                if (isNaN(ival) || ival > imax) {
                                    this._invalid = true;
                                }
                            }
                            else if (k.substr(0, 5) === "*min:") {
                                var imin = parseFloat(k.substr(5));
                                if (isNaN(imin))
                                    throw new Error("Invalid validator: '*min:...' must follow a number");
                                var ival = parseFloat(finalText);
                                if (isNaN(ival) || ival < imin) {
                                    this._invalid = true;
                                }
                            }
                            else if (k.substr(0, 6) === "*same:") {
                                var other = k.substr(6);
                                other = input(other);
                                if (other) {
                                    var otherText = other.text();
                                    if (otherText === null)
                                        otherText = "";
                                    if (finalText !== otherText)
                                        this._invalid = true;
                                }
                                else {
                                    this._invalid = true;
                                }
                            }
                            else {
                                var regexp;
                                if (k.substr(0, 1) === "*") {
                                    var v = Input.VALIDATORS[k];
                                    if (v)
                                        regexp = new RegExp(v);
                                    else
                                        throw new Error("Invalid validator: " + k + " is not a valid validator");
                                }
                                else {
                                    regexp = new RegExp(k);
                                }
                                this._invalid = !regexp.test(finalText);
                            }
                            if (this._invalid) {
                                this._message = validator[k];
                                break;
                            }
                        }
                    }
                }
                if (this._invalid && !this._message) {
                    this._message = tui.str("Invalid input.");
                }
                this._initialized = false;
                this.refresh();
                return !this._invalid;
            };
            Input.prototype.uploadUrl = function (url) {
                if (typeof url === "string") {
                    this.attr("data-upload-url", url);
                    this.unmakeFileUpload();
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.attr("data-upload-url");
            };
            Input.prototype.text = function (txt) {
                var type = this.type();
                if (typeof txt === "string") {
                    if (type === "text" || type === "password" || type === "custom-text") {
                        this.attr("data-text", txt);
                        this.attr("data-value", txt);
                        this._invalid = false;
                        this._initialized = false;
                        this.refresh();
                    }
                    return this;
                }
                else
                    return this.attr("data-text");
            };
            Input.prototype.accept = function (txt) {
                var type = this.type();
                if (typeof txt === "string") {
                    this.attr("data-accept", txt);
                    this.unmakeFileUpload();
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.attr("data-accept");
            };
            Input.prototype.clearable = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-clearable", val);
                    return this;
                }
                else
                    return this.is("data-clearable");
            };
            Input.prototype.timepart = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-timepart", val);
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-timepart");
            };
            Input.prototype.data = function (data) {
                if (data) {
                    var self = this;
                    if (data instanceof Array || data.data && data.data instanceof Array) {
                        data = new tui.ArrayProvider(data);
                    }
                    if (typeof data.length !== "function" || typeof data.sort !== "function" || typeof data.at !== "function" || typeof data.columnKeyMap !== "function") {
                        throw new Error("TUI Input: need a data provider.");
                    }
                    this._data = data;
                    if (data)
                        this._columnKeyMap = data.columnKeyMap();
                    else
                        this._columnKeyMap = {};
                    this._keyColumKey = this.columnKey("key");
                    this._valueColumnKey = this.columnKey("value");
                    this._childrenColumKey = this.columnKey("children");
                    return this;
                }
                else
                    return this._data;
            };
            Input.prototype.valueHasText = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-value-has-text", val);
                    return this;
                }
                else
                    return this.is("data-value-has-text");
            };
            Input.prototype.valueToSelect = function (val) {
                if (this.valueHasText()) {
                    return val;
                }
                else {
                    if (this.type() === "select") {
                        val = [val];
                    }
                    var newval = [];
                    if (val && val.length > 0) {
                        for (var i = 0; i < val.length; i++) {
                            newval.push({ key: val[i] });
                        }
                    }
                    return newval;
                }
            };
            Input.prototype.selectToValue = function (val) {
                if (this.valueHasText()) {
                    return val;
                }
                else {
                    if (this.type() === "select") {
                        if (val && val.length > 0)
                            return val[0].key;
                        else
                            return null;
                    }
                    else {
                        var newval = [];
                        if (val && val.length > 0) {
                            for (var i = 0; i < val.length; i++) {
                                newval.push(val[i].key);
                            }
                        }
                        return newval;
                    }
                }
            };
            Input.prototype.selectValue = function (val) {
                var type = this.type();
                if (typeof val !== tui.undef) {
                    if (type === "select" || type === "multi-select") {
                        if (val && typeof val.length === "number") {
                            this.attr("data-value", JSON.stringify(val));
                            this.attr("data-text", this.formatSelectTextByData(val));
                        }
                        else if (val === null) {
                            this.attr("data-value", "[]");
                            this.attr("data-text", "");
                        }
                        this._invalid = false;
                        this._initialized = false;
                        this.refresh();
                    }
                    return this;
                }
                else {
                    val = this.attr("data-value");
                    if (type === "select" || type === "multi-select") {
                        if (val === null) {
                            return [];
                        }
                        return eval("(" + val + ")");
                    }
                    else
                        return null;
                }
            };
            Input.prototype.readonly = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-readonly", val);
                    this._initialized = false;
                    this._invalid = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-readonly");
            };
            Input.prototype.dateFormat = function (format) {
                if (typeof format === "string") {
                    this.attr("data-date-format", format);
                    return this;
                }
                else
                    return this.attr("data-date-format");
            };
            Input.prototype.textFormat = function (format) {
                if (typeof format === "string") {
                    this.attr("data-text-format", format);
                    return this;
                }
                else
                    return this.attr("data-text-format");
            };
            Input.prototype.allKeys = function () {
                var type = this.type();
                if (type === "select" || type === "multi-select") {
                    var list = tui.ctrl.list();
                    list.data(this._data);
                    return list.allKeys(type === "multi-select");
                }
                else
                    return null;
            };
            Input.prototype.value = function (val) {
                var type = this.type();
                if (typeof val !== tui.undef) {
                    if (val == null) {
                        this.removeAttr("data-value");
                        this.attr("data-text", "");
                        this._invalid = false;
                        this._initialized = false;
                        this.refresh();
                    }
                    else if (type === "calendar") {
                        if (typeof val === "string") {
                            try {
                                val = tui.parseDate(val);
                            }
                            catch (e) {
                                val = null;
                            }
                        }
                        if (val instanceof Date) {
                            this.attr("data-value", tui.formatDate(val, "yyyy-MM-ddTHH:mm:ss.SSSZ"));
                            var fmt = this.textFormat();
                            if (fmt === null) {
                                if (this.timepart())
                                    fmt = tui.str("yyyy-MM-dd HH:mm:ss");
                                else
                                    fmt = tui.str("yyyy-MM-dd");
                            }
                            this.attr("data-text", tui.formatDate(val, fmt));
                            this._invalid = false;
                        }
                        this._initialized = false;
                        this.refresh();
                    }
                    else if (type === "file") {
                        if (val === null) {
                            this.attr("data-value", JSON.stringify(val));
                            this.attr("data-text", "");
                        }
                        else if (val.file && val.fileId) {
                            this.attr("data-value", JSON.stringify(val));
                            this.attr("data-text", val.file);
                        }
                        this._invalid = false;
                        this._initialized = false;
                        this.refresh();
                    }
                    else if (type === "text" || type === "password" || type === "custom-text") {
                        this.attr("data-text", val);
                        this.attr("data-value", val);
                        this._invalid = false;
                        this._initialized = false;
                        this.refresh();
                    }
                    else if (type === "custom-select") {
                        if (val.key && val.value) {
                            this.attr("data-value", val.key);
                            this.attr("data-text", val.value);
                        }
                        else if (val.key) {
                            this.attr("data-value", val.key);
                            this.attr("data-text", val.key);
                        }
                        else if (val.value) {
                            this.attr("data-value", val.value);
                            this.attr("data-text", val.value);
                        }
                        else {
                            this.attr("data-value", val);
                            this.attr("data-text", val);
                        }
                        this._invalid = false;
                        this._initialized = false;
                        this.refresh();
                    }
                    else if (type === "select" || type === "multi-select") {
                        this.selectValue(this.valueToSelect(val));
                    }
                    return this;
                }
                else {
                    val = this.attr("data-value");
                    if (type === "calendar") {
                        if (val === null)
                            return null;
                        var dateVal = tui.parseDate(val);
                        if (this.dateFormat() !== null) {
                            return tui.formatDate(dateVal, this.dateFormat());
                        }
                        else
                            return dateVal;
                    }
                    else if (type === "file") {
                        if (val === null)
                            return null;
                        return eval("(" + val + ")");
                    }
                    else if (type === "select" || type === "multi-select") {
                        return this.selectToValue(this.selectValue());
                    }
                    else
                        return val;
                }
            };
            Input.prototype.textAlign = function (align) {
                if (typeof align === "string") {
                    if (align === "left" || align === "center" || align === "right") {
                        this.attr("data-text-algin", align);
                        this._initialized = false;
                        this.refresh();
                    }
                    return this;
                }
                else {
                    align = this.attr("data-text-algin");
                    if (align === null)
                        align = "left";
                    return align;
                }
            };
            Input.prototype.icon = function (txt) {
                if (typeof txt === "string") {
                    this.attr("data-icon", txt);
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.attr("data-icon");
            };
            Input.prototype.placeholder = function (txt) {
                if (typeof txt === "string") {
                    this.attr("data-placeholder", txt);
                    this._initialized = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.attr("data-placeholder");
            };
            Input.prototype.getSelectRow = function () {
                if (this.type() === "select") {
                    var list = tui.ctrl.list();
                    list.data(this._data);
                    list.activeRowByKey(this.value());
                    return list.activeItem();
                }
                else if (this.type() === "multi-select") {
                    var list = tui.ctrl.list();
                    list.data(this._data);
                    list.checkItems(this.value());
                    return list.checkedItems();
                }
                else
                    return null;
            };
            Input.prototype.getSelectRowColumn = function (columnKey) {
                if (this._data == null)
                    return null;
                var row = this.getSelectRow();
                if (this.type() === "select") {
                    return row[this._data.mapKey(columnKey)];
                }
                else if (this.type() === "multi-select") {
                    var result = [];
                    var k = this._data.mapKey(columnKey);
                    for (var i = 0; i < row.length; i++) {
                        result.push(row[i][k]);
                    }
                    return result;
                }
                else
                    return null;
            };
            Input.prototype.submitForm = function (formId) {
                if (typeof formId === "string") {
                    this.attr("data-submit-form", formId);
                    return this;
                }
                else
                    return this.attr("data-submit-form");
            };
            Input.prototype.autoRefresh = function () {
                return !this._initialized;
            };
            Input.prototype.focus = function () {
                var _this = this;
                if (this.type() === "text" || this.type() === "password" || this.type() === "custom-text") {
                    setTimeout(function () {
                        _this._textbox.focus();
                    }, 0);
                }
                else if (this[0]) {
                    setTimeout(function () {
                        _this[0].focus();
                    }, 0);
                }
            };
            Input.prototype.refresh = function () {
                if (!this[0] || this._initialized)
                    return;
                // IE8 hack (first get element width or height obtain zero)
                this[0].offsetWidth || this[0].offsetHeight;
                if (this[0].offsetWidth <= 0 || this[0].offsetHeight <= 0)
                    return;
                this._initialized = true;
                var type = this.type().toLowerCase();
                if (type === "file" && !this.readonly()) {
                    this.makeFileUpload();
                }
                else
                    this.unmakeFileUpload();
                var placeholder = this.placeholder();
                if (placeholder === null)
                    placeholder = "";
                var text = this.text();
                if (text === null)
                    text = "";
                // BUTTON
                var hasBtn = false;
                if (type !== "text" && type !== "password") {
                    if ($(this[0]).width() < this._button.offsetWidth) {
                        hasBtn = false;
                    }
                    else {
                        hasBtn = true;
                    }
                }
                else {
                    hasBtn = false;
                }
                if (hasBtn) {
                    this._button.style.height = "";
                    this._button.style.height = ($(this[0]).innerHeight() - ($(this._button).outerHeight() - $(this._button).height())) + "px";
                    this._button.style.lineHeight = this._button.style.height;
                    this._button.style.display = "";
                }
                else {
                    this._button.style.display = "none";
                }
                // BUTTON ICON
                if (this.icon()) {
                    $(this._button).addClass(this.icon());
                }
                else
                    this._button.className = "";
                var align = this.textAlign();
                // SHOW LABEL
                var hasLabel = false;
                var hasTextbox = false;
                if (type === "text" || type === "password" || type === "custom-text") {
                    hasTextbox = true;
                    if (!text) {
                        hasLabel = true;
                    }
                    else
                        hasLabel = false;
                }
                else {
                    hasLabel = true;
                    hasTextbox = false;
                }
                if (hasLabel) {
                    if (placeholder && !text) {
                        this._label.innerHTML = placeholder;
                        $(this._label).addClass("tui-placeholder");
                    }
                    else {
                        this._label.innerHTML = text;
                        $(this._label).removeClass("tui-placeholder");
                    }
                    this._label.style.textAlign = align;
                    this._label.style.display = "";
                    if (hasBtn) {
                        this._label.style.width = "";
                        this._label.style.width = ($(this[0]).innerWidth() - ($(this._label).outerWidth() - $(this._label).width()) - $(this._button).outerWidth()) + "px";
                    }
                    else {
                        this._label.style.width = "";
                        this._label.style.width = ($(this[0]).innerWidth() - ($(this._label).outerWidth() - $(this._label).width())) + "px";
                    }
                    this._label.style.height = "";
                    this._label.style.height = ($(this[0]).innerHeight() - ($(this._label).outerHeight() - $(this._label).height())) + "px";
                    this._label.style.lineHeight = this._label.style.height;
                }
                else {
                    this._label.style.display = "none";
                }
                // TEXTBOX
                if (hasTextbox) {
                    if (this.readonly())
                        this._textbox.readOnly = true;
                    else
                        this._textbox.readOnly = false;
                    if (this._textbox.value !== text)
                        this._textbox.value = text;
                    this.removeAttr("tabIndex");
                    this._textbox.style.textAlign = align;
                    this._textbox.style.display = "";
                    if (hasBtn) {
                        this._textbox.style.width = "";
                        this._textbox.style.width = ($(this[0]).innerWidth() - ($(this._textbox).outerWidth() - $(this._textbox).width()) - $(this._button).outerWidth()) + "px";
                    }
                    else {
                        this._textbox.style.width = "";
                        this._textbox.style.width = ($(this[0]).innerWidth() - ($(this._textbox).outerWidth() - $(this._textbox).width())) + "px";
                    }
                    this._textbox.style.height = "";
                    this._textbox.style.height = ($(this[0]).innerHeight() - ($(this._textbox).outerHeight() - $(this._textbox).height())) + "px";
                    this._textbox.style.lineHeight = this._textbox.style.height;
                }
                else {
                    this._textbox.style.display = "none";
                }
                /// INVALID NOTIFY MESSAGE
                if (this._invalid) {
                    $(this._notify).attr("data-tooltip", this._message);
                    $(this._notify).css({
                        "display": "",
                        "right": (hasBtn ? this._button.offsetWidth : 0) + "px"
                    });
                    $(this._notify).css({
                        "line-height": this._notify.offsetHeight + "px"
                    });
                }
                else {
                    $(this._notify).css("display", "none");
                }
            };
            Input.CLASS = "tui-input";
            Input.VALIDATORS = {
                "*email": "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$",
                "*chinese": "^[\\u4e00-\\u9fa5]+$",
                "*url": "^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$",
                "*digital": "^\\d+$",
                "*integer": "^[+\\-]?\\d+$",
                "*float": "^[+\\-]?\\d*\\.\\d+$",
                "*number": "^[+\\-]?\\d+|(\\d*\\.\\d+)$",
                "*currency": "^-?\\d{1,3}(,\\d{3})*\\.\\d{2,3}$",
                "*date": "^[0-9]{4}-1[0-2]|0?[1-9]-0?[1-9]|[12][0-9]|3[01]$",
                "*key": "^[_a-zA-Z][a-zA-Z0-9_]*$",
                "*any": "\\S+"
            };
            Input._supportType = [
                "text",
                "password",
                "select",
                "multi-select",
                "calendar",
                "file",
                "custom-select",
                "custom-text"
            ];
            return Input;
        })(ctrl.Control);
        ctrl.Input = Input;
        /**
         * Construct a button.
         * @param el {HTMLElement or element id or construct info}
         */
        function input(param, type) {
            return tui.ctrl.control(param, Input, type);
        }
        ctrl.input = input;
        tui.ctrl.registerInitCallback(Input.CLASS, input);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.input.ts" /> 
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var TextArea = (function (_super) {
            __extends(TextArea, _super);
            function TextArea(el) {
                var _this = this;
                _super.call(this, "div", TextArea.CLASS, el);
                this._invalid = false;
                this._message = "";
                var self = this;
                this._label = document.createElement("label");
                this._notify = document.createElement("div");
                this[0].innerHTML = "";
                this[0].appendChild(this._label);
                this[0].appendChild(this._notify);
                this.createTextbox();
                $(this._label).on("mousedown", function () {
                    if (!_this.disabled())
                        setTimeout(function () {
                            _this._textbox.focus();
                        }, 0);
                });
                this.value(this.value());
                //this.refresh();
            }
            TextArea.prototype.createTextbox = function () {
                var _this = this;
                var self = this;
                if (this._textbox) {
                    this[0].removeChild(this._textbox);
                }
                this._textbox = document.createElement("textarea");
                // Should put textbox before notify
                this[0].insertBefore(this._textbox, this._notify);
                // Bind events ...
                $(this._textbox).on("focus", function () {
                    $(_this[0]).addClass("tui-focus");
                });
                $(this._textbox).on("blur", function () {
                    $(_this[0]).removeClass("tui-focus");
                });
                $(this._textbox).on("propertychange", function (e) {
                    if (e.originalEvent.propertyName !== 'value')
                        return;
                    setTimeout(function () {
                        if (_this.text() !== _this._textbox.value) {
                            _this.text(_this._textbox.value);
                            self.fire("change", { "ctrl": _this[0], "event": e, "text": _this.text() });
                            _this.refresh();
                        }
                    }, 0);
                });
                $(this._textbox).on("change", function (e) {
                    if (_this.text() !== _this._textbox.value) {
                        _this.text(_this._textbox.value);
                        _this.fire("change", { "ctrl": _this[0], "event": e, "text": _this.text() });
                        _this.refresh();
                    }
                });
                $(this._textbox).on("input", function (e) {
                    setTimeout(function () {
                        if (_this.text() !== _this._textbox.value) {
                            _this.text(_this._textbox.value);
                            self.fire("change", { "ctrl": self[0], "event": e, "text": self.text() });
                            _this.refresh();
                        }
                    }, 0);
                });
            };
            TextArea.prototype.validator = function (val) {
                if (typeof val === "object" && val) {
                    this.attr("data-validator", JSON.stringify(val));
                    this._invalid = false;
                    this.refresh();
                    return this;
                }
                else if (val === null) {
                    this.removeAttr("data-validator");
                    this._invalid = false;
                    this.refresh();
                    return this;
                }
                else {
                    val = this.attr("data-validator");
                    if (val === null) {
                        return null;
                    }
                    else {
                        try {
                            val = eval("(" + val + ")");
                            if (typeof val !== "object")
                                return null;
                            else
                                return val;
                        }
                        catch (err) {
                            return null;
                        }
                    }
                }
            };
            TextArea.prototype.validate = function (txt) {
                var finalText = typeof txt === "string" ? txt : this.text();
                if (finalText === null)
                    finalText = "";
                this._invalid = false;
                var validator = this.validator();
                if (validator) {
                    for (var k in validator) {
                        if (k && validator.hasOwnProperty(k)) {
                            if (k === "*password") {
                                if (!/[a-z]/.test(finalText) || !/[A-Z]/.test(finalText) || !/[0-9]/.test(finalText) || !/[\~\`\!\@\#\$\%\^\&\*\(\)\_\-\+\=\\\]\[\{\}\:\;\"\'\/\?\,\.\<\>\|]/.test(finalText) || finalText.length < 6) {
                                    this._invalid = true;
                                }
                            }
                            else if (k.substr(0, 5) === "*max:") {
                                var imax = parseFloat(k.substr(5));
                                if (isNaN(imax))
                                    throw new Error("Invalid validator: '*max:...' must follow a number");
                                var ival = parseFloat(finalText);
                                if (isNaN(ival) || ival > imax) {
                                    this._invalid = true;
                                }
                            }
                            else if (k.substr(0, 4) === "*min:") {
                                var imin = parseFloat(k.substr(5));
                                if (isNaN(imin))
                                    throw new Error("Invalid validator: '*min:...' must follow a number");
                                var ival = parseFloat(finalText);
                                if (isNaN(ival) || ival < imin) {
                                    this._invalid = true;
                                }
                            }
                            else {
                                var regexp;
                                if (k.substr(0, 1) === "*") {
                                    var v = ctrl.Input.VALIDATORS[k];
                                    if (v)
                                        regexp = new RegExp(v);
                                    else
                                        throw new Error("Invalid validator: " + k + " is not a valid validator");
                                }
                                else {
                                    regexp = new RegExp(k);
                                }
                                this._invalid = !regexp.test(finalText);
                            }
                            if (this._invalid) {
                                this._message = validator[k];
                                break;
                            }
                        }
                    }
                }
                if (this._invalid && !this._message) {
                    this._message = tui.str("Invalid input.");
                }
                this.refresh();
                return !this._invalid;
            };
            TextArea.prototype.text = function (txt) {
                if (typeof txt !== tui.undef) {
                    this.attr("data-text", txt);
                    this.attr("data-value", txt);
                    this._invalid = false;
                    this.refresh();
                    return this;
                }
                else
                    return this.attr("data-text");
            };
            TextArea.prototype.readonly = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-readonly", val);
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-readonly");
            };
            TextArea.prototype.value = function (val) {
                if (typeof val !== tui.undef) {
                    if (val === null) {
                        this.attr("data-text", "");
                        this.attr("data-value", "");
                    }
                    else {
                        this.attr("data-text", val);
                        this.attr("data-value", val);
                    }
                    this._invalid = false;
                    this.refresh();
                    return this;
                }
                else {
                    val = this.attr("data-value");
                    return val;
                }
            };
            TextArea.prototype.autoResize = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-auto-resize", val);
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-auto-resize");
            };
            TextArea.prototype.placeholder = function (txt) {
                if (typeof txt === "string") {
                    this.attr("data-placeholder", txt);
                    this.refresh();
                    return this;
                }
                else
                    return this.attr("data-placeholder");
            };
            TextArea.prototype.refresh = function () {
                var placeholder = this.placeholder();
                if (placeholder === null)
                    placeholder = "";
                var text = this.text();
                if (text === null)
                    text = "";
                var withBtn = false;
                if (this._textbox.value !== text)
                    this._textbox.value = text;
                if (this.readonly())
                    this._textbox.readOnly = true;
                else
                    this._textbox.readOnly = false;
                this._textbox.style.display = "";
                this._label.style.display = "none";
                this._textbox.style.width = "";
                this._textbox.style.width = ($(this[0]).innerWidth() - ($(this._textbox).outerWidth() - $(this._textbox).width())) + "px";
                //this._textbox.scrollHeight
                this._textbox.style.height = "";
                var maxHeight = parseInt($(this[0]).css("max-height"), 10);
                if (this._textbox.scrollHeight < maxHeight || isNaN(maxHeight)) {
                    this._textbox.style.overflow = "hidden";
                    $(this[0]).css("height", this._textbox.scrollHeight + "px");
                }
                else {
                    this._textbox.style.overflow = "auto";
                    $(this[0]).css("height", maxHeight + "px");
                }
                this._textbox.style.height = ($(this[0]).innerHeight() - ($(this._textbox).outerHeight() - $(this._textbox).height())) + "px";
                //this._textbox.style.lineHeight = this._textbox.style.height;
                this._label.style.width = this._textbox.style.width;
                if (placeholder && !text) {
                    this._label.innerHTML = placeholder;
                    this._label.style.display = "";
                    $(this._label).addClass("tui-placeholder");
                    this._label.style.lineHeight = $(this._label).height() + "px";
                }
                else {
                    $(this._label).removeClass("tui-placeholder");
                }
                if (this._invalid) {
                    $(this._notify).attr("data-tooltip", this._message);
                    $(this._notify).css({
                        "display": "",
                        "right": "0px"
                    });
                    $(this._notify).css({
                        "line-height": this._notify.offsetHeight + "px"
                    });
                }
                else {
                    $(this._notify).css("display", "none");
                }
            };
            TextArea.CLASS = "tui-textarea";
            return TextArea;
        })(ctrl.Control);
        ctrl.TextArea = TextArea;
        /**
         * Construct a button.
         * @param el {HTMLElement or element id or construct info}
         */
        function textarea(param) {
            return tui.ctrl.control(param, TextArea);
        }
        ctrl.textarea = textarea;
        tui.ctrl.registerInitCallback(TextArea.CLASS, textarea);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Tab = (function (_super) {
            __extends(Tab, _super);
            function Tab(el) {
                _super.call(this, "div", Tab.CLASS, el);
                this._tabId = "tab-" + tui.uuid();
                this._buttons = [];
                var self = this;
                var removeList = [];
                var activeIndex = 0;
                for (var i = 0; i < this[0].childNodes.length; i++) {
                    var child = this[0].childNodes[i];
                    if (child.nodeName.toLowerCase() === "span" || child.nodeName.toLowerCase() === "a") {
                        $(child).addClass("tui-radiobox");
                        var button = tui.ctrl.radiobox(child);
                        button.group(this._tabId);
                        this._buttons.push(button);
                        if (button.checked())
                            activeIndex = this._buttons.length - 1;
                        button.on("check", function (data) {
                            self.checkPage(data.ctrl);
                        });
                    }
                    else
                        removeList.push(child);
                }
                //for (var i = 0; i < removeList.length; i++) {
                //	tui.removeNode(removeList[i]);
                //}
                this.at(activeIndex).checked(true);
            }
            Tab.prototype.checkPage = function (button) {
                var tabId = button.attr("data-tab");
                tabId = "#" + tabId;
                if (button.checked()) {
                    $(tabId).removeClass("tui-hidden");
                    tui.ctrl.initCtrls($(tabId)[0]);
                }
                else {
                    $(tabId).addClass("tui-hidden");
                }
                this.fire("active", { index: this._buttons.indexOf(button), text: button.text() });
            };
            Tab.prototype.at = function (index) {
                if (index >= 0 && index < this._buttons.length)
                    return this._buttons[index];
                else
                    return null;
            };
            Tab.prototype.count = function () {
                return this._buttons.length;
            };
            Tab.prototype.add = function (name, index) {
                var self = this;
                if (typeof index === "number") {
                    if (index >= this._buttons.length)
                        return null;
                }
                var button;
                if (typeof name === "string") {
                    button = tui.ctrl.radiobox();
                    button.text(name);
                }
                else if (typeof name === "object") {
                    button = name;
                }
                button.group(this._tabId);
                button.on("check", function (data) {
                    self.checkPage(data.ctrl);
                });
                if (typeof index === tui.undef) {
                    this[0].appendChild(button[0]);
                    this._buttons.push(button);
                }
                else {
                    this[0].insertBefore(button[0], this.at(index)[0]);
                    this._buttons.splice(index, 0, button);
                }
                return button;
            };
            Tab.prototype.remove = function (index) {
                if (typeof index === "object")
                    index = this._buttons.indexOf(index);
                var button = this.at(index);
                if (button) {
                    var activeIndex = -1;
                    if (button.checked()) {
                        activeIndex = index;
                    }
                    this._buttons.splice(index, 1);
                    tui.removeNode(button[0]);
                    if (activeIndex >= 0) {
                        if (activeIndex < this._buttons.length)
                            this.active(activeIndex);
                        else if (this._buttons.length > 0)
                            this.active(this._buttons.length - 1);
                    }
                    return button;
                }
                return null;
            };
            Tab.prototype.active = function (index) {
                if (typeof index !== tui.undef) {
                    if (typeof index === "object")
                        index = this._buttons.indexOf(index);
                    var button = this.at(index);
                    if (button) {
                        button.checked(true);
                        this.checkPage(button);
                    }
                    return this;
                }
                else {
                    for (var i = 0; i < this._buttons.length; i++) {
                        if (this._buttons[i].checked())
                            return i;
                    }
                    return -1;
                }
            };
            Tab.CLASS = "tui-tab";
            return Tab;
        })(ctrl.Control);
        ctrl.Tab = Tab;
        function tab(param) {
            return tui.ctrl.control(param, Tab);
        }
        ctrl.tab = tab;
        tui.ctrl.registerInitCallback(Tab.CLASS, tab);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Paginator = (function (_super) {
            __extends(Paginator, _super);
            function Paginator(el) {
                _super.call(this, "div", Paginator.CLASS, el);
                if (!this.hasAttr("data-max-buttons"))
                    this.maxButtons(3);
                if (!this.hasAttr("data-value"))
                    this.value(1);
                if (!this.hasAttr("data-page-size"))
                    this.pageSize(10);
                if (!this.hasAttr("data-total-size"))
                    this.totalSize(0);
                this.refresh();
            }
            Paginator.prototype.value = function (val) {
                if (typeof val !== tui.undef) {
                    if (typeof val === "number") {
                        if (val > this.totalPages())
                            val = this.totalPages();
                        if (val < 1)
                            val = 1;
                        this.attr("data-value", val);
                        this.refresh();
                    }
                    return this;
                }
                else
                    return Math.round(parseInt(this.attr("data-value")));
            };
            Paginator.prototype.totalPages = function () {
                var total = Math.ceil(this.totalSize() / this.pageSize());
                if (total < 1)
                    total = 1;
                return total;
            };
            Paginator.prototype.pageSize = function (val) {
                if (typeof val !== tui.undef) {
                    if (typeof val === "number") {
                        if (val <= 0)
                            val = 1;
                        this.attr("data-page-size", val);
                        this.refresh();
                    }
                    return this;
                }
                else
                    return Math.round(parseInt(this.attr("data-page-size")));
            };
            Paginator.prototype.totalSize = function (val) {
                if (typeof val !== tui.undef) {
                    if (typeof val === "number") {
                        this.attr("data-total-size", val);
                        this.refresh();
                    }
                    return this;
                }
                else
                    return Math.round(parseInt(this.attr("data-total-size")));
            };
            Paginator.prototype.submitForm = function (formId) {
                if (typeof formId === "string") {
                    this.attr("data-submit-form", formId);
                    return this;
                }
                else
                    return this.attr("data-submit-form");
            };
            Paginator.prototype.maxButtons = function (val) {
                if (typeof val !== tui.undef) {
                    if (typeof val === "number") {
                        if (val <= 0)
                            val = 1;
                        this.attr("data-max-buttons", val);
                        this.refresh();
                    }
                    return this;
                }
                else
                    return Math.round(parseInt(this.attr("data-max-buttons")));
            };
            Paginator.prototype.changeValue = function (val) {
                this.value(val);
                //this.refresh();
                if (this.fire("change", { ctrl: this[0], value: this.value() }) === false)
                    return;
                var formId = this.submitForm();
                if (formId) {
                    var form = tui.ctrl.form(formId);
                    form && form.submit();
                }
            };
            Paginator.prototype.refresh = function () {
                if (!this[0])
                    return;
                var self = this;
                this[0].innerHTML = "";
                // Add Previous Button
                var previous = ctrl.button();
                previous.text(tui.str("Previous"));
                this[0].appendChild(previous[0]);
                if (this.value() === 1) {
                    previous.disabled(true);
                }
                else {
                    previous.on("click", function () {
                        self.changeValue(self.value() - 1);
                    });
                }
                var maxButtons = this.maxButtons();
                var totalPages = this.totalPages();
                var fromIndex = this.value() - Math.floor(maxButtons / 2) + (maxButtons % 2 === 0 ? 1 : 0);
                if (fromIndex <= 1) {
                    fromIndex = 1;
                }
                var toIndex = (fromIndex === 1 ? fromIndex + maxButtons : fromIndex + maxButtons - 1);
                if (toIndex >= totalPages) {
                    toIndex = totalPages;
                    fromIndex = toIndex - maxButtons;
                    if (fromIndex < 1) {
                        fromIndex = 1;
                    }
                }
                if (fromIndex > 1) {
                    var btn = ctrl.button();
                    btn.html(1 + (fromIndex > 2 ? " <i class='fa fa-ellipsis-h'></i>" : ""));
                    this[0].appendChild(btn[0]);
                    btn.on("click", function () {
                        self.changeValue(1);
                    });
                }
                for (var i = fromIndex; i <= toIndex; i++) {
                    var btn = ctrl.button();
                    btn.text(i + "");
                    btn.on("click", function () {
                        self.changeValue(parseInt(this.text()));
                    });
                    this[0].appendChild(btn[0]);
                    if (i === this.value())
                        btn.addClass("tui-primary");
                }
                if (toIndex < totalPages) {
                    var btn = ctrl.button();
                    btn.html((toIndex < totalPages - 1 ? "<i class='fa fa-ellipsis-h'></i> " : "") + totalPages);
                    this[0].appendChild(btn[0]);
                    btn.on("click", function () {
                        self.changeValue(totalPages);
                    });
                }
                // Add Next Button
                var next = ctrl.button();
                next.text(tui.str("Next"));
                this[0].appendChild(next[0]);
                if (this.value() === this.totalPages()) {
                    next.disabled(true);
                }
                else {
                    next.on("click", function () {
                        self.changeValue(self.value() + 1);
                    });
                }
            };
            Paginator.CLASS = "tui-paginator";
            return Paginator;
        })(ctrl.Control);
        ctrl.Paginator = Paginator;
        function paginator(param) {
            return tui.ctrl.control(param, Paginator);
        }
        ctrl.paginator = paginator;
        tui.ctrl.registerInitCallback(Paginator.CLASS, paginator);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Tips = (function (_super) {
            __extends(Tips, _super);
            function Tips(el) {
                var _this = this;
                _super.call(this, "div", Tips.CLASS, el);
                this._closeButton = null;
                var btn = document.createElement("span");
                this._closeButton = btn;
                btn.className = "tui-tips-close";
                this[0].appendChild(btn);
                $(btn).click(function (e) {
                    _this.close();
                });
            }
            Tips.prototype.useVisible = function (val) {
                if (typeof val !== tui.undef) {
                    this.is("data-use-visible", !!val);
                    return this;
                }
                else
                    return this.is("data-use-visible");
            };
            Tips.prototype.autoCloseTime = function (val) {
                if (typeof val === "number") {
                    if (isNaN(val) || val <= 0)
                        this.removeAttr("data-auto-close-time");
                    else
                        this.attr("data-auto-close-time", Math.floor(val));
                    return this;
                }
                else {
                    val = parseInt(this.attr("data-auto-close-time"));
                    if (isNaN(val))
                        return null;
                    else if (val <= 0)
                        return null;
                    else
                        return val;
                }
            };
            Tips.prototype.show = function (msg) {
                var _this = this;
                if (typeof msg !== tui.undef) {
                    tui.removeNode(this._closeButton);
                    this[0].innerHTML = msg;
                    this[0].appendChild(this._closeButton);
                }
                this.removeClass("tui-invisible");
                this.removeClass("tui-hidden");
                this[0].style.opacity = "0";
                $(this[0]).animate({ opacity: 1 }, 100, function () {
                    var autoClose = _this.autoCloseTime();
                    if (autoClose !== null) {
                        setTimeout(function () {
                            _this.close();
                        }, autoClose);
                    }
                });
            };
            Tips.prototype.close = function () {
                var _this = this;
                this.fire("close", { ctrl: this[0] });
                $(this[0]).animate({ opacity: 0 }, 300, function () {
                    if (_this.useVisible())
                        _this.addClass("tui-invisible");
                    else
                        _this.addClass("tui-hidden");
                });
            };
            Tips.CLASS = "tui-tips";
            return Tips;
        })(ctrl.Control);
        ctrl.Tips = Tips;
        /**
         * Construct a tips.
         * @param el {HTMLElement or element id or construct info}
         */
        function tips(param) {
            return tui.ctrl.control(param, Tips);
        }
        ctrl.tips = tips;
        tui.ctrl.registerInitCallback(Tips.CLASS, tips);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        function getRealParent(monitoredParent) {
            if (monitoredParent && monitoredParent.window && monitoredParent.document || monitoredParent.nodeName.toLowerCase() === "#document" || monitoredParent.nodeName.toLowerCase() === "body" || monitoredParent.nodeName.toLowerCase() === "html") {
                // Bind to a window
                if (monitoredParent.window && monitoredParent.document) {
                    return monitoredParent;
                }
                else if (monitoredParent.nodeName.toLowerCase() === "#document") {
                    return monitoredParent.defaultView || monitoredParent.parentWindow;
                }
                else {
                    return tui.getWindow(monitoredParent);
                }
            }
            else {
                return monitoredParent;
            }
        }
        function getRealTagetScrollElement(monitoredParent) {
            if (monitoredParent && monitoredParent.document) {
                if (tui.ieVer > 0 || tui.ffVer > 0) {
                    return monitoredParent.document.documentElement;
                }
                else {
                    return monitoredParent.document.body;
                }
            }
            else {
                return monitoredParent;
            }
        }
        /**
         * AccordionGroup class used to display a foldable panel
         * to show a group items in this panel.
         */
        var AccordionGroup = (function (_super) {
            __extends(AccordionGroup, _super);
            function AccordionGroup(el) {
                var _this = this;
                _super.call(this, "div", AccordionGroup.CLASS, el);
                this._accordions = [];
                this._anchors = [];
                this._monitoredParent = null;
                this._inScrolling = false;
                this._onScroll = (function () {
                    var self = _this;
                    var scrollTimer = null;
                    return function (e) {
                        if (self._monitoredParent === null)
                            return;
                        if (self._inScrolling)
                            return;
                        var parent = getRealTagetScrollElement(self._monitoredParent);
                        for (var i = 0; i < self._anchors.length; i++) {
                            var elemId = self._anchors[i];
                            var elem = document.getElementById(elemId);
                            if (!elem)
                                continue;
                            var pos = tui.relativePosition(elem, parent);
                            if (Math.abs(pos.y - parent.scrollTop - self.distance()) <= 100) {
                                if (scrollTimer != null) {
                                    clearTimeout(scrollTimer);
                                    scrollTimer = null;
                                }
                                scrollTimer = setTimeout(function () {
                                    self.value("#" + elem.id);
                                }, 50);
                                break;
                            }
                        }
                    };
                })();
                this._onSelect = (function () {
                    var self = _this;
                    return function (data) {
                        if (self.fire("select", data) !== false) {
                            if (self.keyIsLink()) {
                                if (data.key && data.key.slice(0, 1) === "#") {
                                    var elemId = data.key.substr(1);
                                    var elem = document.getElementById(elemId);
                                    if (elem) {
                                        var parent = getRealTagetScrollElement(self._monitoredParent);
                                        var pos = tui.relativePosition(elem, parent);
                                        self._inScrolling = true;
                                        $(parent).stop().animate({ "scrollTop": pos.y - self.distance() }, 200, function () {
                                            window.location.href = data.key;
                                            parent.scrollTop = pos.y - self.distance();
                                            self._inScrolling = false;
                                        });
                                    }
                                    else {
                                        window.location.href = data.key;
                                    }
                                }
                                else {
                                    window.location.href = data.key;
                                }
                            }
                        }
                    };
                })();
                if (this.hasAttr("data-max-height"))
                    this.maxHeight(this.maxHeight());
                this.bindChildEvents();
            }
            AccordionGroup.prototype.distance = function (tolerance) {
                if (typeof tolerance === "number") {
                    this.attr("data-distance", tolerance);
                    return this;
                }
                else {
                    var v = this.attr("data-distance");
                    v = parseInt(this.attr("data-distance"));
                    if (isNaN(v))
                        return 50;
                    else
                        return v;
                }
            };
            AccordionGroup.prototype.maxHeight = function (maxHeight) {
                if (typeof maxHeight === "number") {
                    this.attr("data-max-height", maxHeight);
                    var allCaptionHeight = 0;
                    for (var i = 0; i < this[0].childNodes.length; i++) {
                        var elem = this[0].childNodes[i];
                        if ($(elem).hasClass("tui-accordion")) {
                            var acc = ctrl.accordion(elem);
                            allCaptionHeight += acc.captionHeight();
                        }
                        else if (elem.tagName) {
                            allCaptionHeight += $(elem).outerHeight();
                        }
                    }
                    $(this[0]).find(".tui-accordion").each(function (idx, elem) {
                        if (elem._ctrl) {
                            elem._ctrl.maxHeight(maxHeight - allCaptionHeight + elem._ctrl.captionHeight());
                            elem._ctrl.refresh();
                        }
                    });
                    return this;
                }
                else
                    return parseInt(this.attr("data-max-height"));
            };
            AccordionGroup.prototype.value = function (key) {
                if (typeof key !== tui.undef) {
                    $(this[0]).find(".tui-accordion").each(function (idx, elem) {
                        if (elem._ctrl) {
                            elem._ctrl.value(key);
                            if (elem._ctrl.value() !== null)
                                return false;
                        }
                    });
                }
                else {
                    var val = null;
                    $(this[0]).find(".tui-accordion").each(function (idx, elem) {
                        if (elem._ctrl) {
                            val = elem._ctrl.value();
                            if (val !== null)
                                return false;
                        }
                    });
                    return val;
                }
            };
            AccordionGroup.prototype.installMonitor = function (monitoredParent) {
                if (typeof monitoredParent === "string")
                    monitoredParent = document.getElementById(monitoredParent);
                this._monitoredParent = getRealParent(monitoredParent);
                if (this._monitoredParent)
                    $(this._monitoredParent).scroll(this._onScroll);
            };
            AccordionGroup.prototype.uninstallMonitor = function () {
                if (this._monitoredParent)
                    $(this._monitoredParent).off("scroll", this._onScroll);
                this._monitoredParent = null;
            };
            AccordionGroup.prototype.keyIsLink = function (val) {
                if (typeof val !== tui.undef) {
                    this.is("data-key-is-link", !!val);
                    return this;
                }
                else
                    return this.is("data-key-is-link");
            };
            AccordionGroup.prototype.addAccordion = function (acc) {
                this[0].appendChild(acc[0]);
                this.bindChildEvents();
            };
            AccordionGroup.prototype.clear = function () {
                this[0].innerHTML = "";
                this.bindChildEvents();
            };
            AccordionGroup.prototype.bindChildEvents = function () {
                for (var acc in this._accordions) {
                    if (this._accordions.hasOwnProperty(acc))
                        this._accordions[acc].off("select", this._onSelect);
                }
                this._accordions = [];
                this._anchors = [];
                var self = this;
                $(this[0]).find(".tui-accordion").each(function (idx, elem) {
                    if (typeof elem._ctrl === tui.undef)
                        ctrl.accordion(elem);
                    if (elem._ctrl) {
                        self._accordions.push(elem._ctrl);
                        elem._ctrl.on("select", self._onSelect);
                        if (self.keyIsLink()) {
                            elem._ctrl.enumerate(function (item) {
                                if (item.key && item.key.slice(0, 1) === "#") {
                                    self._anchors.push(item.key.substr(1));
                                }
                            });
                        }
                    }
                });
            };
            AccordionGroup.prototype.refresh = function () {
                for (var acc in this._accordions) {
                    if (this._accordions.hasOwnProperty(acc))
                        this._accordions[acc].refresh();
                }
            };
            AccordionGroup.CLASS = "tui-accordion-group";
            return AccordionGroup;
        })(ctrl.Control);
        ctrl.AccordionGroup = AccordionGroup;
        function accordionGroup(param) {
            return tui.ctrl.control(param, AccordionGroup);
        }
        ctrl.accordionGroup = accordionGroup;
        tui.ctrl.registerInitCallback(AccordionGroup.CLASS, accordionGroup);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.accordiongroup.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (_ctrl) {
        /**
         * Accordion class used to display a navigation sidebar to
         * let user easy jump to a particular page section to read.
         */
        var Accordion = (function (_super) {
            __extends(Accordion, _super);
            function Accordion(el) {
                var _this = this;
                _super.call(this, "div", Accordion.CLASS, el);
                this._caption = null;
                this._list = null;
                this._initialized = false;
                this[0].innerHTML = "";
                this._caption = document.createElement("div");
                this._caption.className = "tui-accordion-caption";
                this._caption.setAttribute("unselectable", "on");
                this[0].appendChild(this._caption);
                this._list = tui.ctrl.list();
                this[0].appendChild(this._list[0]);
                var predefined = this.attr("data-data");
                if (predefined)
                    predefined = eval("(" + predefined + ")");
                $(this._caption).click(function () {
                    _this.expanded(!_this.expanded());
                });
                $(this._caption).keydown(function (e) {
                    if (e.keyCode === 13) {
                        _this.expanded(!_this.expanded());
                    }
                });
                var self = this;
                this._list.on("rowclick keydown", function (data) {
                    if (data["name"] === "rowclick") {
                        self.value(self._list.data().at(data["index"])["key"]);
                        var k = self.value();
                        if (k)
                            self.fire("select", { "ctrl": self[0], "key": k, "caption": self.caption() });
                    }
                    else if (data["event"].keyCode === 13) {
                        self.value(self._list.activeItem().key);
                        var k = self.value();
                        if (k)
                            self.fire("select", { "ctrl": self[0], "key": k, "caption": self.caption() });
                    }
                });
                this._list.on("rowexpand rowfold", function (data) {
                    self._list.activerow(null);
                    data["event"].stopPropagation();
                    self.refresh();
                });
                var originFormator = this._list.columns()[0].format;
                this._list.columns()[0].format = function (data) {
                    originFormator(data);
                    if (typeof data.row.checked === tui.undef)
                        return;
                    var contentSpan = data.cell.firstChild;
                    var checkSpan = contentSpan.childNodes[2];
                    contentSpan.removeChild(checkSpan);
                    if (data.row.checked) {
                        $(data.cell.parentElement).addClass("tui-accordion-row-checked");
                    }
                    else
                        $(data.cell.parentElement).removeClass("tui-accordion-row-checked");
                };
                var animation = this.useAnimation();
                this.useAnimation(false);
                if (predefined) {
                    this.data(predefined);
                    var checkedItems = this._list.checkedItems();
                    if (checkedItems && checkedItems.length > 0) {
                        var k = this._list.data().mapKey("key");
                        this.value(checkedItems[0][k]);
                        k = self.value();
                        if (k)
                            self.fire("select", { "ctrl": self[0], "key": k, "caption": self.caption() });
                    }
                    else {
                        this.expanded(this.expanded());
                    }
                }
                else
                    this.expanded(this.expanded());
                this.useAnimation(animation);
            }
            Accordion.prototype.data = function (data) {
                if (typeof data !== tui.undef) {
                    this._list.data(data);
                    this.refresh();
                    return this;
                }
                else
                    return this._list.data(data);
            };
            Accordion.prototype.caption = function (val) {
                if (typeof val === "string") {
                    this.attr("data-caption", val);
                    this._caption.innerHTML = val || "";
                    this.refresh();
                    return this;
                }
                else
                    return this.attr("data-caption");
            };
            Accordion.prototype.captionHeight = function () {
                return $(this._caption).outerHeight();
            };
            Accordion.prototype.expanded = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-expanded", val);
                    if (this.expanded()) {
                        var groupName = this.group();
                        if (groupName) {
                            var self = this;
                            $("." + Accordion.CLASS + "[data-group='" + groupName + "']").each(function (index, elem) {
                                var ctrl = elem["_ctrl"];
                                if (ctrl && ctrl !== self && typeof ctrl.expanded === "function") {
                                    ctrl.expanded(false);
                                }
                            });
                        }
                    }
                    this.refresh();
                    return this;
                }
                else
                    return this.is("data-expanded");
            };
            Accordion.prototype.maxHeight = function (maxHeight) {
                if (typeof maxHeight === "number") {
                    this.attr("data-max-height", maxHeight);
                    return this;
                }
                else
                    return parseInt(this.attr("data-max-height"));
            };
            Accordion.prototype.group = function (val) {
                if (typeof val !== tui.undef) {
                    this.attr("data-group", val);
                    return this;
                }
                else
                    return this.attr("data-group");
            };
            Accordion.prototype.value = function (key) {
                if (typeof key !== tui.undef) {
                    this._list.value([key]);
                    this._list.activerow(null);
                    if (this._list.value().length > 0) {
                        this._list.activeRowByKey(key);
                        this.expanded(true);
                        this._list.scrollTo(this._list.activerow());
                        var groupName = this.group();
                        if (groupName) {
                            var self = this;
                            $("." + Accordion.CLASS + "[data-group='" + groupName + "']").each(function (index, elem) {
                                var ctrl = elem["_ctrl"];
                                if (ctrl && ctrl !== self && typeof ctrl.value === "function") {
                                    ctrl.value(null);
                                }
                            });
                        }
                    }
                    return this;
                }
                else {
                    var val = this._list.value();
                    if (val === null)
                        return val;
                    else if (val.length > 0)
                        return val[0];
                    else
                        return null;
                }
            };
            Accordion.prototype.autoRefresh = function () {
                return !this._initialized;
            };
            Accordion.prototype.enumerate = function (func) {
                if (this._list)
                    this._list.enumerate(func);
            };
            Accordion.prototype.consumeMouseWheelEvent = function (val) {
                return this._list.consumeMouseWheelEvent(val);
            };
            Accordion.prototype.useAnimation = function (val) {
                if (typeof val === "boolean") {
                    this.is("data-anmimation", val);
                    return this;
                }
                else
                    return this.is("data-anmimation");
            };
            Accordion.prototype.refresh = function () {
                var _this = this;
                if (!this[0] || this[0].offsetWidth === 0 || this[0].offsetHeight === 0)
                    return;
                this._initialized = true;
                var captionHeight;
                if (!this.expanded()) {
                    this._caption.setAttribute("tabIndex", "0");
                    $(this._caption).removeClass("tui-expanded");
                    captionHeight = this._caption.offsetHeight;
                    $(this._caption).addClass("tui-expanded");
                    if (this.useAnimation()) {
                        $(this[0]).stop().animate({ "height": captionHeight + "px" }, Accordion.ANIMATION_DURATION, "linear", function () {
                            $(_this._caption).removeClass("tui-expanded");
                            _this._list[0].style.display = "none";
                        });
                    }
                    else {
                        $(this[0]).stop();
                        this[0].style.height = captionHeight + "px";
                        $(this._caption).removeClass("tui-expanded");
                        this._list[0].style.display = "none";
                    }
                }
                else {
                    this._caption.removeAttribute("tabIndex");
                    $(this._caption).addClass("tui-expanded");
                    this._list[0].style.display = "";
                    captionHeight = this._caption.offsetHeight;
                    var maxHeight = this.maxHeight();
                    var lines = 1;
                    if (this._list.data())
                        lines = this._list.data().length();
                    if (lines < 1)
                        lines = 1;
                    var height = this._list.lineHeight() * lines + 4;
                    if (!isNaN(maxHeight) && height > maxHeight - captionHeight) {
                        height = maxHeight - captionHeight;
                    }
                    this._list[0].style.height = height + "px";
                    this._list[0].style.width = $(this[0]).width() + "px";
                    this._list.refresh();
                    if (this.useAnimation()) {
                        $(this[0]).stop().animate({ "height": height + captionHeight + "px" }, Accordion.ANIMATION_DURATION, "linear", function () {
                            $(_this._caption).addClass("tui-expanded");
                            _this._list[0].style.display = "";
                            _this._list.focus();
                        });
                    }
                    else {
                        $(this[0]).stop();
                        this[0].style.height = height + captionHeight + "px";
                        $(this._caption).addClass("tui-expanded");
                        this._list[0].style.display = "";
                        this._list.focus();
                    }
                }
            };
            Accordion.CLASS = "tui-accordion";
            Accordion.ANIMATION_DURATION = 200;
            return Accordion;
        })(_ctrl.Control);
        _ctrl.Accordion = Accordion;
        function accordion(param) {
            return tui.ctrl.control(param, Accordion);
        }
        _ctrl.accordion = accordion;
        tui.ctrl.registerInitCallback(Accordion.CLASS, accordion);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.popup.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        var Menu = (function (_super) {
            __extends(Menu, _super);
            function Menu(data) {
                _super.call(this);
                this._items = [];
                this._menuDiv = null;
                this._parentMenu = null;
                if (data instanceof Array || data.data && data.data instanceof Array) {
                    data = new tui.ArrayProvider(data);
                }
                else if (typeof data === "function" || typeof data.length !== "function" || typeof data.sort !== "function" || typeof data.at !== "function" || typeof data.columnKeyMap !== "function") {
                    throw new Error("TUI Menu: need a data provider.");
                }
                this._data = data;
                var self = this;
                this.on("close", function () {
                    self._items = [];
                    delete self._activedItem;
                });
            }
            Menu.prototype.fireClick = function (item, row) {
                if (item._showChildTimer)
                    clearTimeout(item._showChildTimer);
                if (item._childMenu && (typeof row.key === tui.undef || row.key === null)) {
                    item._childMenu.show(item.firstChild, "rT");
                }
                else {
                    this.closeAll();
                    if (this.fire("select", { "ctrl": self[0], "item": row }) === false) {
                        return;
                    }
                    if (typeof row.key !== tui.undef && row.key !== null) {
                        if (tui.fire(row.key, row) === false)
                            return;
                    }
                    if (row.link) {
                        window.location.href = row.link;
                    }
                }
            };
            Menu.prototype.bindMouseEvent = function (item, row) {
                var self = this;
                $(item).mousemove(function (e) {
                    if (item["_menuIndex"] !== self._activedItem) {
                        self.deactiveItem(self._activedItem);
                        $(item).addClass("tui-actived");
                        self._activedItem = item["_menuIndex"];
                        if (item._childMenu) {
                            item._showChildTimer = setTimeout(function () {
                                item._childMenu.show(item.firstChild, "rT");
                            }, 400);
                        }
                    }
                });
                $(item).mouseleave(function (e) {
                    if (!tui.isAncestry(document.activeElement, self._menuDiv))
                        return;
                    if (item._showChildTimer)
                        clearTimeout(item._showChildTimer);
                    $(item).removeClass("tui-actived");
                    delete self._activedItem;
                });
                $(item).click(function (e) {
                    self.fireClick(item, row);
                });
            };
            Menu.prototype.deactiveItem = function (itemIndex) {
                if (itemIndex >= 0 && itemIndex < this._items.length) {
                    var item = this._items[itemIndex];
                    $(item).removeClass("tui-actived");
                    if (item._childMenu)
                        item._childMenu.close();
                    if (item._showChildTimer)
                        clearTimeout(item._showChildTimer);
                }
            };
            Menu.prototype.setParentMenu = function (parent) {
                this._parentMenu = parent;
            };
            Menu.prototype.closeAll = function () {
                if (this._parentMenu) {
                    this._parentMenu.closeAll();
                }
                else
                    this.close();
            };
            Menu.prototype.focus = function () {
                this._menuDiv.focus();
            };
            Menu.prototype.show = function (param, bindType) {
                if (this.isShowing())
                    return;
                if (!this._data)
                    return;
                var self = this;
                var data;
                if (typeof this._data === "function") {
                    data = this._data();
                    if (typeof data.length !== "function" || typeof data.sort !== "function" || typeof data.at !== "function" || typeof data.columnKeyMap !== "function") {
                        return;
                    }
                }
                else
                    data = this._data;
                var div = document.createElement("div");
                this._menuDiv = div;
                div.className = Menu.CLASS;
                $(div).attr("unselectable", "on");
                $(div).attr("tabIndex", "-1");
                for (var i = 0; i < data.length(); i++) {
                    var row = data.at(i);
                    var item = document.createElement("div");
                    if (row.value === "-") {
                        item.className = "tui-menu-line";
                    }
                    else {
                        var innerBox = document.createElement("div");
                        item.appendChild(innerBox);
                        innerBox.innerHTML = row.value;
                        var icon = document.createElement("i");
                        icon.className = "tui-menu-icon";
                        innerBox.insertBefore(icon, innerBox.firstChild);
                        if (row.checked) {
                            $(icon).addClass("tui-menu-icon-checked");
                        }
                        else if (row.icon) {
                            $(icon).addClass(row.icon);
                        }
                        if (row.children) {
                            $(item).addClass("tui-menu-has-children");
                            var childMenu = menu(row.children);
                            childMenu.on("select", function (data) {
                                self.fire("select", data);
                            });
                            childMenu.setParentMenu(this);
                            item["_childMenu"] = childMenu;
                        }
                        this.bindMouseEvent(item, row);
                        item["_menuIndex"] = this._items.length;
                        this._items.push(item);
                    }
                    div.appendChild(item);
                }
                $(div).keydown(function (e) {
                    var c = e.keyCode;
                    if (c === tui.KEY_DOWN || c === tui.KEY_TAB) {
                        if (typeof self._activedItem !== "number" && self._items.length > 0) {
                            self._activedItem = 0;
                        }
                        else {
                            $(self._items[self._activedItem]).removeClass("tui-actived");
                            self._activedItem++;
                            if (self._activedItem > self._items.length - 1)
                                self._activedItem = 0;
                        }
                        if (typeof self._activedItem === "number" && self._activedItem >= 0 && self._activedItem < self._items.length)
                            $(self._items[self._activedItem]).addClass("tui-actived");
                        e.stopPropagation();
                        e.preventDefault();
                    }
                    else if (c === tui.KEY_UP) {
                        if (typeof self._activedItem !== "number" && self._items.length > 0) {
                            self._activedItem = self._items.length - 1;
                        }
                        else {
                            $(self._items[self._activedItem]).removeClass("tui-actived");
                            self._activedItem--;
                            if (self._activedItem < 0)
                                self._activedItem = self._items.length - 1;
                        }
                        if (typeof self._activedItem === "number" && self._activedItem >= 0 && self._activedItem < self._items.length)
                            $(self._items[self._activedItem]).addClass("tui-actived");
                        e.stopPropagation();
                        e.preventDefault();
                    }
                    else if (c === tui.KEY_ESC) {
                        self.close();
                        e.stopPropagation();
                        e.preventDefault();
                    }
                    else if (c === tui.KEY_LEFT) {
                        if (self._parentMenu) {
                            self.close();
                            self._parentMenu.focus();
                        }
                    }
                    else if (c === tui.KEY_RIGHT) {
                        if (typeof self._activedItem === "number" && self._activedItem >= 0 && self._activedItem < self._items.length) {
                            var item = self._items[self._activedItem];
                            if (item._childMenu) {
                                item._childMenu.show(item.firstChild, "rT");
                            }
                        }
                    }
                    else if (c === tui.KEY_ENTER) {
                        if (typeof self._activedItem === "number" && self._activedItem >= 0 && self._activedItem < self._items.length) {
                            var item = self._items[self._activedItem];
                            var row = data.at(self._activedItem);
                            self.fireClick(item, row);
                        }
                    }
                });
                _super.prototype.show.call(this, div, param, bindType);
                div.focus();
            };
            Menu.CLASS = "tui-menu";
            return Menu;
        })(ctrl.Popup);
        ctrl.Menu = Menu;
        function menu(data) {
            return new Menu(data);
        }
        ctrl.menu = menu;
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
/// <reference path="tui.ctrl.control.ts" />
var tui;
(function (tui) {
    var ctrl;
    (function (ctrl) {
        /**
         * Navbar class used to display a navigation head bar to
         * let user easy jump to a particular functional area in the website.
         */
        var Navbar = (function (_super) {
            __extends(Navbar, _super);
            function Navbar(el) {
                var _this = this;
                _super.call(this, "div", Navbar.CLASS, el);
                this._float = false;
                this._onScroll = (function () {
                    var self = _this;
                    return function (e) {
                        var pos;
                        if (self._float === false) {
                            pos = tui.fixedPosition(self[0]);
                            self[0].style.left = "";
                        }
                        else {
                            pos = tui.fixedPosition(self[1]);
                            self[0].style.left = (-tui.windowScrollElement().scrollLeft) + "px";
                        }
                        if (pos.y < self.top() && self._float === false) {
                            self._float = true;
                            self[1].style.height = self[0].offsetHeight + "px";
                            self[0].style.position = "fixed";
                            self[0].style.left = (-tui.windowScrollElement().scrollLeft) + "px";
                            self[0].style.right = "0px";
                            self[0].style.top = self.top() + "px";
                            document.body.insertBefore(self[1], self[0]);
                        }
                        else if (pos.y >= self.top() && self._float === true) {
                            self._float = false;
                            self[0].style.position = "";
                            self[0].style.top = "";
                            self[0].style.left = "";
                            self[1].style.height = "";
                            tui.removeNode(self[1]);
                        }
                    };
                })();
                this[1] = document.createElement("div");
                this.installMonitor();
                this._onScroll(null);
            }
            Navbar.prototype.installMonitor = function () {
                $(window).scroll(this._onScroll);
            };
            Navbar.prototype.uninstallMonitor = function () {
                $(window).off("scroll", this._onScroll);
            };
            Navbar.prototype.top = function (val) {
                if (typeof val === "string") {
                    this.attr("data-top", val);
                    return this;
                }
                else {
                    val = parseInt(this.attr("data-top"));
                    if (isNaN(val))
                        return 0;
                    else
                        return val;
                }
            };
            Navbar.CLASS = "tui-navbar";
            return Navbar;
        })(ctrl.Control);
        ctrl.Navbar = Navbar;
        function navbar(param) {
            return tui.ctrl.control(param, Navbar);
        }
        ctrl.navbar = navbar;
        tui.ctrl.registerInitCallback(Navbar.CLASS, navbar);
    })(ctrl = tui.ctrl || (tui.ctrl = {}));
})(tui || (tui = {}));
