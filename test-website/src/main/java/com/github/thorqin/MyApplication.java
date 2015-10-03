package com.github.thorqin;

import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebRouter;

@WebApp(name = "testtag",
        routers = {
                @WebRouter("*.do")
                // Uncomment following line to enable database router
                // , @WebRouter(value = "/db/*", type = MyApplication.MyDBRouter.class)
        }
        // , services = @WebAppService(name = "db", type = DBService.class)
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
