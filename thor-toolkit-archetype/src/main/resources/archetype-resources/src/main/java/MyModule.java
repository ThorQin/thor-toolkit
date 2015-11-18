#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.thorqin.toolkit.validation.annotation.*;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import javax.servlet.http.HttpServletRequest;
import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.web.session.WebSession;

@WebModule
public class MyModule {
	/*
    @Service("db")
    DBService db;

	@Service("application")
	Application app;

	@Service("logger")
	Logger logger;

	@Service("config")
	AppConfigManager config;
	*/

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

