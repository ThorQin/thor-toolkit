package com.github.thorqin.toolkit.web.router;

import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.web.MessageConstant;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.utility.ServletUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by nuo.qin on 1/28/2015.
 */
public abstract class WebRouterBase extends HttpServlet implements ConfigManager.ChangeListener {
    protected final WebApplication application;

    public WebRouterBase(WebApplication application) {
        this.application = application;
    }

    public WebApplication getApplication() {
        return application;
    }

    @Override
    public void onConfigChanged(ConfigManager configManager) {
    }

    public void sendError(HttpServletRequest req, HttpServletResponse resp, int status) {
        Localization loc = Localization.getInstance();
        String message;
        String accept = req.getHeader("accept");
        if (accept != null) {
            accept = accept.toLowerCase();
            int pos = accept.indexOf(";");
            if (pos >= 0) {
                accept = accept.substring(0, pos);
            }
            String[] acceptArray = accept.split(",");
            accept = "plain";
            for (int i = 0; i < acceptArray.length; i++) {
                if (acceptArray[i].trim().equals("text/html")) {
                    accept = "html";
                    break;
                } else if (acceptArray[i].trim().equals("application/json")) {
                    accept = "json";
                    break;
                }
            }
        } else {
            accept = "plain";
        }
        if (status == 404) {
            message = MessageConstant.NOT_FOUND.getMessage(loc);
        } else {
            message = MessageConstant.INVALID_HTTP_METHOD.getMessage(loc);
        }
        if (accept.equals("html")) {
            String content = null;
            try {
                content = application.readAppTextFile("html/" + status + ".html", true);
            } catch (Exception e) {
            }
            if (content == null) {
                content = "<html><head><title>" + status + "</title><body><h3>" + message +"</h3></body></html>";
            }
            ServletUtils.sendHtml(resp, status, content);
        } else if (accept.equals("json")) {
            ServletUtils.sendJsonString(resp, status, "{\"error\":\"" + message + "\"}");
        } else {
            ServletUtils.sendText(resp, status, message);
        }
    }
}
