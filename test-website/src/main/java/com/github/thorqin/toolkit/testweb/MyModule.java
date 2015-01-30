package com.github.thorqin.toolkit.testweb;

import com.github.thorqin.toolkit.web.annotation.WebEntry;
import com.github.thorqin.toolkit.web.annotation.WebModule;
import static com.github.thorqin.toolkit.web.annotation.WebEntry.HttpMethod.GET;
/**
 * Created by nuo.qin on 1/30/2015.
 */
@WebModule
public class MyModule {

    @WebEntry(method=GET)
    public String hello() {
        return "Hello World!";
    }

}
