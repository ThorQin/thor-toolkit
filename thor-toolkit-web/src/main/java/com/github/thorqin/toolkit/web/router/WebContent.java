package com.github.thorqin.toolkit.web.router;

import com.github.thorqin.toolkit.utility.Serializer;

/**
 * Created by thor on 1/31/2015.
 */
public class WebContent {

    public enum Type {
        PLAIN,
        JSON,
        HTML,
        REDIRECTION
    }

    protected String content;
    protected Type type;

    private WebContent() {}

    @Override
    public String toString() {
        return content;
    }

    public Type getType() {
        return type;
    }


    public static WebContent json(Object object) {
        WebContent content = new WebContent();
        content.content = Serializer.toJsonString(object);
        content.type = Type.JSON;
        return content;
    }

    public static WebContent jsonString(String jsonString) {
        WebContent content = new WebContent();
        content.content = jsonString;
        content.type = Type.JSON;
        return content;
    }

    public static WebContent text(String plain) {
        WebContent content = new WebContent();
        content.content = plain;
        content.type = Type.PLAIN;
        return content;
    }

    public static WebContent html(String html) {
        WebContent content = new WebContent();
        content.content = html;
        content.type = Type.HTML;
        return content;
    }

    public static WebContent view(String path) {
        WebContent content = new WebContent();
        content.content = path;
        content.type = Type.REDIRECTION;
        return content;
    }
}
