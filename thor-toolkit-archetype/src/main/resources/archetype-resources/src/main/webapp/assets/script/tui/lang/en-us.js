/// <reference path="../tui.core.ts" />
(function () {
    var dict = {
        "success": "Success",
        "notmodified": "Request's content has not been modified!",
        "error": "Occurred an internal error!",
        "timeout": "Request timeout!",
        "abort": "Operating has been aborted!",
        "parsererror": "Server response invalid content!"
    };

    // Register this dictionary.
    tui.registerTranslator("en-us", dict);
    // Or you can register a custom translate function like this:
    // tui.registerTranslator("en-us", function(str) {
    //		if (str === 'xxx' && hasOtherConditions)
    //			return 'the string what you wish to displayed';
    //		else
    //			......
    // });
})();
