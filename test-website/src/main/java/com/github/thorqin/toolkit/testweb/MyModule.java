package com.github.thorqin.toolkit.testweb;

import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;

import static com.github.thorqin.toolkit.web.annotation.WebEntry.HttpMethod.GET;

@WebModule
public class MyModule {
	/*
    @DBInstance
    DBService db;
	*/
	
    @WebEntry(method = GET)
    public WebContent hello() {
        return WebContent.html("<b>Hello World!</b>");
    }

}

