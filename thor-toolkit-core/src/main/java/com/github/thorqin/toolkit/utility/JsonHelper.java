package com.github.thorqin.toolkit.utility;

import com.google.gson.JsonElement;

/**
 * Created by thor on 8/19/16.
 */
public class JsonHelper extends JsonAccessor {

    public JsonHelper(String jsonString) {
        rootObj = Serializer.fromJson(jsonString, JsonElement.class);
    }
    public JsonHelper(JsonElement jsonElement) {
        rootObj = jsonElement;
    }
}
