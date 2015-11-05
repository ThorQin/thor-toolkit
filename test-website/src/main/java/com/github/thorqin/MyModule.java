package com.github.thorqin;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.validation.annotation.ValidateMap;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import com.github.thorqin.toolkit.web.session.WebSession;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@WebModule
public class MyModule {
	/*
    @Service("db")
    DBService db;
	*/

    @Service("myService")
    MyService myService;

    @WebEntry(method = HttpMethod.POST)
    public WebContent getServerInfo(HttpServletRequest request, WebSession session) {
        String serverName = request.getServletContext().getServerInfo();
        session.set("lang", "zh-cn");
        return WebContent.json(serverName + " " + myService.getServerTime());
    }

    @WebEntry(method = HttpMethod.POST)
    public Map<String, Object> echo(@Entity @ValidateMap(type = Object.class) Map<String, Object> body) {
        return body;
    }

}

