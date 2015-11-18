package com.github.thorqin;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.ValidateMessageConstant;
import com.github.thorqin.toolkit.validation.Validatable;
import com.github.thorqin.toolkit.validation.annotation.*;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import com.github.thorqin.toolkit.web.session.WebSession;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebModule
public class MyModule {
	/*
    @Service("db")
    DBService db;
	*/

    @Service("logger")
    Logger logger;

    @Service("myService")
    MyService myService;

    @WebEntry(method = HttpMethod.POST)
    public WebContent getServerInfo(HttpServletRequest request, Localization loc) {
        String serverName = request.getServletContext().getServerInfo();
        return WebContent.json("<span style='display:inline-block;vertical-align:middle'>"
                + serverName + "<br>" + myService.getServerTime(loc) + "</span>");
    }

    @WebEntry(method = HttpMethod.POST)
    public void setLanguage(@Entity @ValidateString("^(en-us|zh-cn)$") String language, WebSession session) {
        session.set("lang", language.toLowerCase());
    }

}

