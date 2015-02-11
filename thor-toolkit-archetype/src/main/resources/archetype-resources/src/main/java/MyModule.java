#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;

import static ${groupId}.toolkit.web.annotation.WebEntry.HttpMethod.GET;

@WebModule
public class MyModule {
	/*
    @DBInstance
    DBService db;
	*/
	
    @WebEntry(method = GET)
    public WebContent hello() {
        return WebContent.html("<b>Hello World!</b>");
    }

}

