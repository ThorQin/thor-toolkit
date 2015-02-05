package com.github.thorqin.toolkit.testweb;

import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebRouter;

/**
 * Created by nuo.qin on 1/30/2015.
 */
@WebApp(name = "test-website", routers = {
        @WebRouter("*.do"),
        @WebRouter(value = "/db/*", type = MyDBRouter.class)})
public class MyApplication extends WebApplication {

}
