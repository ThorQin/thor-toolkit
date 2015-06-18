package com.github.thorqin;

import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import com.github.thorqin.toolkit.web.annotation.WebEntry.HttpMethod;
import com.github.thorqin.toolkit.web.session.WebSession;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import javax.servlet.http.HttpServletRequest;

@WebModule
public class MyModule {
	/*
    @Service("db")
    DBService db;
	*/

    @WebEntry(method = HttpMethod.POST)
    public WebContent getServerInfo(HttpServletRequest request, WebSession session) {
        String serverTime = new DateTime().toString(DateTimeFormat.mediumDateTime());
        String serverName = request.getServletContext().getServerInfo();
        session.set("lang", "zh-cn");
        return WebContent.json(serverName + "<br>" + serverTime);
    }

}

