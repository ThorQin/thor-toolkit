package com.github.thorqin.toolkit.validation;

import com.github.thorqin.toolkit.utility.Localization;

import java.text.MessageFormat;

/**
 * Created by thor on 11/18/15.
 */
public enum ValidateMessageConstant {
    VERIFY_FAILED("validate.failed", "Validate failed: {0}: {1}"),
    CANNOT_BE_NULL("validate.cannot.be.null", "value must be provided"),
    CANNOT_BE_EMPTY("validate.cannot.be.empty", "value cannot be empty"),
    INVALID_TYPE("validate.invalid.type", "invalid type, expect ''{0}'' but found ''{1}''"),
    INVALID_VALUE("validate.invalid.value", "invalid value"),
    INVALID_FORMAT("validate.invalid.format", "invalid format"),
    INVALID_TIME_FORMAT("validate.invalid.time.format", "specified min/max date time format is invalid"),
    INVALID_VALIDATE_RULE("validate.invalid.rule", "invalid rule definition, specified rule cannot match object type: {0}"),
    VALUE_SHOULD_LESS_THAN("validate.value.should.less.than", "value should be less than or equal to {0}"),
    VALUE_SHOULD_GREAT_THAN("validate.value.should.great.than", "value should be greater than or equal to {0}"),
    VALUE_SHOULD_BETWEEN("validate.value.should.between", "value should between {0} and {1}"),
    COUNT_SHOULD_LESS_THAN("validate.count.should.less.than", "item count should be less than or equal to {0}"),
    COUNT_SHOULD_GREAT_THAN("validate.count.should.great.than", "item count should be greater than or equal to {0}"),
    COUNT_SHOULD_BETWEEN("validate.count.should.between", "item count should between {0} and {1}"),
    TIME_SHOULD_LESS_THAN("validate.time.should.less.than", "time should be earlier than or equal to {0}"),
    TIME_SHOULD_GREAT_THAN("validate.time.should.great.than", "time should be later than or equal to {0}"),
    TIME_SHOULD_BETWEEN("validate.time.should.between", "time should between {0} and {1}"),
    LENGTH_SHOULD_LESS_THAN("validate.length.should.less.than", "length should be less than or equal to {0}"),
    LENGTH_SHOULD_GREAT_THAN("validate.length.should.great.than", "length should be greater than or equal to {0}"),
    LENGTH_SHOULD_BETWEEN("validate.length.should.between", "length should between {0} and {1}");
    private String key;
    private String message;

    ValidateMessageConstant(String key, String message) {
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

    public void throwException(Localization loc, Object... params) throws ValidateException {
        throw new ValidateException(getMessage(loc, params));
    }
}
