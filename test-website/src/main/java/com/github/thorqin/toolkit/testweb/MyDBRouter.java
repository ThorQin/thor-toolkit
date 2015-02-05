package com.github.thorqin.toolkit.testweb;

import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.DBRouter;
import com.github.thorqin.toolkit.web.router.WebDBRouter;

/**
 * Created by nuo.qin on 2/5/2015.
 */
@DBRouter
public class MyDBRouter extends WebDBRouter {
    public MyDBRouter(WebApplication application) throws ValidateException {
        super(application);
    }
}
