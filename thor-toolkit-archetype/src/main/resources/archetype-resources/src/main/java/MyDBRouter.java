#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.DBRouter;
import com.github.thorqin.toolkit.web.router.WebDBRouter;

@DBRouter
public class MyDBRouter extends WebDBRouter {
    public MyDBRouter(WebApplication application) throws ValidateException {
        super(application);
    }
}
