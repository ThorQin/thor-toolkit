package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.web.WebApplication;
import com.google.javascript.jscomp.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thor on 6/12/15.
 */

public class ScriptTag extends SimpleTagSupport {

    public String id = null;
    private static Map<String, String> cache = new HashMap<>();
    private String appName = null;

    @Override
    public void doTag() throws JspException, IOException {

        boolean compressJs = false;
        WebApplication app = WebApplication.get(appName);
        if (app != null)
            compressJs = app.getSetting().compressJs;
        if (!compressJs) {
            getJspBody().invoke(getJspContext().getOut());
            return;
        }

        StringWriter stringWriter = new StringWriter();
        getJspBody().invoke(stringWriter);
        String jsContent = stringWriter.toString().trim();

        String compressed;
        boolean found;
        synchronized (cache) {
            found = cache.containsKey(jsContent);
        }
        if (found) {
            compressed = cache.get(jsContent);
        } else {
            HttpServletRequest request = (HttpServletRequest)this.getJspContext()
                    .getAttribute("javax.servlet.jsp.jspRequest");
            String scriptName;
            if (id != null) {
                scriptName = request.getRequestURI() + "#" + id;
            } else {
                scriptName = request.getRequestURI() + "#" + (long)(Math.random() * 10000000);
            }
            SourceFile sourceFile = SourceFile.builder().buildFromCode(scriptName , jsContent);
            SourceFile externalSource = SourceFile.builder().buildFromCode("tmp", "");
            com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
            CompilerOptions options = new CompilerOptions();
            CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
            WarningLevel.DEFAULT.setOptionsForWarningLevel(options);
            Result result = compiler.compile(externalSource, sourceFile, options);
            if (result.success) {
                compressed = compiler.toSource();
                synchronized (cache) {
                    cache.put(jsContent, compressed);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (JSError err: result.errors) {
                    sb.append(err.toString()).append("\n");
                }
                throw new JspException(sb.toString());
            }
        }

        JspWriter out = getJspContext().getOut();
        if (id != null) {
            out.print("<script id=\"");
            out.print(id);
            out.println("\">");
        } else
            out.println("<script>");
        out.println(compressed);
        out.println("</script>");
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
