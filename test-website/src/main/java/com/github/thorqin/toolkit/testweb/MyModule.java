package com.github.thorqin.toolkit.testweb;

import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.router.WebContent;
import com.github.thorqin.toolkit.web.router.WebDBRouter;

import java.sql.SQLException;

import static com.github.thorqin.toolkit.web.annotation.WebEntry.HttpMethod.GET;
/**
 * Created by nuo.qin on 1/30/2015.
 */
@WebModule
public class MyModule {

    @DBInstance
    DBService db;

    @WebEntry(method = GET)
    public WebContent hello() throws SQLException {
        return WebContent.json(db.query("select * from tb_user"));
    }

}

