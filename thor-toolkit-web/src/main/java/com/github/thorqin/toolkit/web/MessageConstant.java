package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.utility.Localization;

import java.text.MessageFormat;

/**
 * Created by thor on 11/18/15.
 */
public enum MessageConstant {
    INVALID_HTTP_METHOD("message.invalid.http.method", "Invalid HTTP method"),
    NOT_FOUND("message.not.found", "Not found"),
    INVALID_REQUEST_CONTENT("message.invalid.request.content", "Invalid request content"),
    UNEXPECTED_SERVER_ERROR("message.unexpected.server.error", "Unexpected server error");

    private String key;
    private String message;

    MessageConstant(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public String getMessage(Localization loc, Object... params) {
        String msg;
        if (loc != null) {
            msg = loc.get(key, message);
        } else
            msg = message;
        return MessageFormat.format(msg, params);
    }
}
