#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebRouter;

@WebApp(name = "my-website",
    routers = {@WebRouter("*.do")/*, @WebRouter(value = "/db/*", type = MyDBRouter.class) */ }
)
public class MyApplication extends WebApplication {

}
