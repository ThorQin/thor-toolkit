package com.github.thorqin.toolkit.web.utility;

import com.github.thorqin.toolkit.utility.Serializer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nuo.qin on 12/25/2014.
 */
public class ServletUtils {
    private static Logger logger = Logger.getLogger(ServletUtils.class.getName());

    public static void sendText(HttpServletResponse response, Integer status, String message) {
        response.setStatus(status);
        sendText(response, message);
    }

    public static void sendText(HttpServletResponse response, String message) {
        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        try (Writer w = response.getWriter();) {
            if (message != null)
                w.write(message);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Send message to client failed!", ex);
        }
    }

    public static void sendJsonString(HttpServletResponse response, Integer status, String jsonString) {
        response.setStatus(status);
        sendJsonString(response, jsonString);
    }

    public static void sendJsonString(HttpServletResponse response, String jsonString) {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        try (Writer w = response.getWriter();) {
            w.write(jsonString);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Send message to client failed!", ex);
        }
    }

    public static void send(HttpServletResponse response, Integer status) {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setStatus(status);
    }

    public static void sendJsonObject(HttpServletResponse response, Integer status, Object obj) {
        response.setStatus(status);
        sendJsonObject(response, obj);
    }

    public static void sendJsonObject(HttpServletResponse response, Object obj) {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        try (Writer w = response.getWriter();){
            Serializer.toJson(obj, w);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Send message to client failed!", ex);
        }
    }
}
