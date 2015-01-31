package com.github.thorqin.toolkit.testweb;

import com.github.thorqin.toolkit.validation.annotation.ValidateNumber;
import com.github.thorqin.toolkit.validation.annotation.ValidateString;
import com.github.thorqin.toolkit.web.WebContent;
import com.github.thorqin.toolkit.web.annotation.Param;
import com.github.thorqin.toolkit.web.annotation.Query;
import com.github.thorqin.toolkit.web.annotation.WebEntry;
import com.github.thorqin.toolkit.web.annotation.WebModule;
import static com.github.thorqin.toolkit.web.annotation.WebEntry.HttpMethod.GET;
/**
 * Created by nuo.qin on 1/30/2015.
 */
@WebModule
public class MyModule {

    @WebEntry(value = "{abc}.do", method=GET, useCache = false)
    public WebContent hello(@Param("abc") @ValidateString(minLength=10) String param,
                        @Query("value") Integer value) {
        return WebContent.view("/test.jsp");
    }

}
