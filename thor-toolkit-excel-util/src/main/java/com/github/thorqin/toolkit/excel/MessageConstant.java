package com.github.thorqin.toolkit.excel;

import com.github.thorqin.toolkit.utility.Localization;

import java.text.MessageFormat;

/**
 * Created by thor on 8/3/16.
 */
public enum MessageConstant {
    EMPTY_DATA_MODEL("message.excel.no.valid.data.model", "No valid data model specified"),
    SHEET_IS_REQUIRED("message.excel.sheet.is.required", "Sheet \"{0}\" is required"),
    SHEET_IS_REQUIRED_BY("message.excel.sheet.is.required.by", "Sheet \"{0}\" is required, because sheet \"{1}\" is provided"),
    INVALID_VALUE_FORMAT("message.excel.invalid.format", "Invalid format: {0}"),
    COLUMN_IS_REQUIRED("message.excel.column.is.required", "Column \"{0}\" is required"),
    COLUMN_IS_REQUIRED_BY("message.excel.column.is.required.by", "Column \"{0}\" is required, because column \"{1}\" is provided");


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
