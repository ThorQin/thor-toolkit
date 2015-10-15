#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import javax.servlet.http.HttpServletRequest;

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


    @WebEntry(method = HttpMethod.POST)
    public WebContent getServerInfo(HttpServletRequest request) {
        String serverTime = new DateTime().toString(DateTimeFormat.mediumDateTime());
        String serverName = request.getServletContext().getServerInfo();
        return WebContent.json(serverName + "<br>" + serverTime);
    }

}

