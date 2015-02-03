package com.github.thorqin.toolkit.testweb;

import com.github.thorqin.toolkit.web.router.WebContent;
import com.github.thorqin.toolkit.web.annotation.Param;
import com.github.thorqin.toolkit.web.annotation.Query;
import com.github.thorqin.toolkit.web.annotation.WebEntry;
import com.github.thorqin.toolkit.web.annotation.WebModule;
import static com.github.thorqin.toolkit.web.annotation.WebEntry.HttpMethod.GET;
/**
 * Created by nuo.qin on 1/30/2015.
 */
@WebModule
public class MyModule {
    @WebEntry(method = GET, value = "{user}/hello.do")
    public WebContent hello(@Query("value") int value,
                            @Param("user") String name) {
        System.out.println("name: " + name);
        return WebContent.view("/test.jsp");
    }

    @WebEntry(method = GET, value = "api/{name}")
    public WebContent print(@Param("name") String name) {
        return WebContent.html("<b>" + name + "</b>");
    }

    @WebEntry(method = GET, value = "api")
    public WebContent showDefault() {
        return WebContent.html("<b>default</b>");
    }

    @WebEntry(method = GET, value = "api/")
    public WebContent showDefault1() {
        return WebContent.html("<b>default1</b>");
    }
}
