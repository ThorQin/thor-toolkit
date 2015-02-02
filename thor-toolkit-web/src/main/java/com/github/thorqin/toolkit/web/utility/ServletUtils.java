package com.github.thorqin.toolkit.web.utility;

import com.github.thorqin.toolkit.utility.MimeUtils;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.utility.UserAgentUtils;
import com.github.thorqin.toolkit.web.HttpException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nuo.qin on 12/25/2014.
 */
public class ServletUtils {
    private static Logger logger = Logger.getLogger(ServletUtils.class.getName());

    public static String getURL(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(request.getRequestURL());
        String queryString = request.getQueryString();
        if (queryString != null)
            sb.append("?").append(queryString);
        return sb.toString();
    }

    public static void sendText(HttpServletResponse response, Integer status, String message) {
        response.setStatus(status);
        sendText(response, message);
    }

    public static void sendText(HttpServletResponse response, String message, String contentType) {
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        try (Writer w = response.getWriter()) {
            if (message != null)
                w.write(message);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Send message to client failed!", ex);
        }
    }

    public static void sendText(HttpServletResponse response, String message) {
        sendText(response, message, "text/plain");
    }

    public static void sendHtml(HttpServletResponse response, String html) {
        sendText(response, html, "text/html");
    }

    public static void sendJsonString(HttpServletResponse response, Integer status, String jsonString) {
        response.setStatus(status);
        sendJsonString(response, jsonString);
    }

    public static void sendJsonString(HttpServletResponse response, String jsonString) {
        sendText(response, jsonString, "application/json");
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
        try (Writer w = response.getWriter()){
            Serializer.toJson(obj, w);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Send message to client failed!", ex);
        }
    }

    public static void download(HttpServletRequest req,
                                HttpServletResponse resp,
                                File file,
                                String fileName) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            download(req, resp, inputStream, fileName);
        }
    }

    public static void download(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    InputStream inputStream,
                                    String fileName) throws IOException {
        try {
            if (fileName != null) {
                fileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
            } else {
                fileName = "download.dat";
            }
        } catch (UnsupportedEncodingException e1) {
            fileName = "download.dat";
            e1.printStackTrace();
        }
        String extName = "";
        if (fileName.lastIndexOf(".") > 0)
            extName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String mimeType = MimeUtils.getFileMime(extName);
        if (mimeType == null)
            mimeType = "application/octet-stream";
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setContentType(mimeType);

        String userAgent = req.getHeader("User-Agent").toLowerCase();
        UserAgentUtils.UserAgentInfo uaInfo = UserAgentUtils.parse(userAgent);
        if (uaInfo.browser == UserAgentUtils.BrowserType.FIREFOX) {
            resp.addHeader("Content-Disposition", "attachment; filename*=\"utf-8''"
                    + fileName + "\"");
        } else {
            resp.addHeader("Content-Disposition", "attachment; filename=\""
                    + fileName + "\"");
        }
        try (OutputStream outputStream = resp.getOutputStream()) {
            int length;
            byte[] buffer = new byte[4096];
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    public static void error(Exception ex, Integer status) throws HttpException {
        Throwable e = ex;
        Throwable cause;
        while ((cause = e.getCause()) != null)
            e = cause;
        throw new HttpException(status, e.getMessage(), ex);
    }

    public static void error(Exception ex) throws HttpException {
        error(ex, 400);
    }
}
