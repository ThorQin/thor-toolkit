#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import javax.servlet.http.HttpServletRequest;
import com.github.thorqin.toolkit.annotation.Service;

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
    public WebContent getServerInfo(HttpServletRequest request) {
        String serverName = request.getServletContext().getServerInfo();
        return WebContent.json(serverName + "<br>" + myService.getServerTime());
    }

}

