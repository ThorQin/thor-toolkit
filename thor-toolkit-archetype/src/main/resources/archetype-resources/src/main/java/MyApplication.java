#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebRouter;
import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.schedule.ScheduleService;

@WebApp(name = "${artifactId}",
        configName = "config.json", // Or "config.yml" if you more like YAML syntax
        routers = {
                @WebRouter("*.do")
                // Uncomment following line to enable database router
                // , @WebRouter(value = "/db/*", type = MyApplication.MyDBRouter.class)
        },
        services = {
                @Service(value = "scheduler", type = ScheduleService.class)
                // , @Service(value = "db", type = DBService.class)
        }
)
public class MyApplication extends WebApplication {
    /* Uncomment following lines to enable database router
    @DBRouter
    public static class MyDBRouter extends WebDBRouter {
        public MyDBRouter(WebApplication application) throws ValidateException {
            super(application);
        }
    }
    */
}


