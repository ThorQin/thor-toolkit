package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.Application;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.web.WebApplication;
import com.google.common.base.*;
import com.google.javascript.jscomp.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thor on 6/12/15.
 */

public class ScriptTag extends SimpleTagSupport {

    public String id = null;
    private static final Map<String, String> CACHE = new HashMap<>();
    private String appName = null;
    private String src = null;


    private String compress(String content) throws JspException {
        if (content == null || content.trim().isEmpty())
            return "";
        boolean found;
        synchronized (CACHE) {
            found = CACHE.containsKey(content);
        }
        if (found) {
            return CACHE.get(content);
        } else {
            HttpServletRequest request = (HttpServletRequest) this.getJspContext()
                    .getAttribute("javax.servlet.jsp.jspRequest");
            String scriptName;
            if (id != null) {
                scriptName = request.getRequestURI() + "#" + id;
            } else {
                scriptName = request.getRequestURI() + "#" + (long) (Math.random() * 10000000);
            }
            SourceFile sourceFile = SourceFile.builder().buildFromCode(scriptName, content);
            SourceFile externalSource = SourceFile.builder().buildFromCode("tmp", "");
            com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
            CompilerOptions options = new CompilerOptions();
            CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
            WarningLevel.DEFAULT.setOptionsForWarningLevel(options);
            Result result = compiler.compile(externalSource, sourceFile, options);
            if (result.success) {
                String compressed = compiler.toSource();
                synchronized (CACHE) {
                    CACHE.put(content, compressed);
                }
                return compressed;
            } else {
                StringBuilder sb = new StringBuilder();
                for (JSError err : result.errors) {
                    sb.append(err.toString()).append("\n");
                }
                throw new JspException(sb.toString());
            }
        }
    }

    @Override
    public void doTag() throws JspException, IOException {
        boolean compressJs = false;
        WebApplication app = (WebApplication)Application.get(appName);
        if (app != null)
            compressJs = app.getSetting().compressJs;

        boolean srcContent = false;
        StringBuilder result = new StringBuilder();
        JspWriter out = getJspContext().getOut();
        // compress src
        if (src != null) {
            HttpServletRequest request = (HttpServletRequest) this.getJspContext()
                    .getAttribute("javax.servlet.jsp.jspRequest");
            if (src.matches(request.getContextPath() + "/.+\\.(?i)js")) {
                // Reference to a local file
                File file = new File(request.getServletContext().getRealPath(src));
                if (file.isFile()) {
                    String content = Serializer.readTextFile(file);
                    result.append(compressJs ? compress(content) : content);
                    src = null;
                    srcContent = true;
                }
            }
        }

        StringWriter stringWriter = new StringWriter();
        JspFragment jsBody = getJspBody();
        if (jsBody != null)
            jsBody.invoke(stringWriter);
        String jsContent = stringWriter.toString().trim();
        if (srcContent && !jsContent.isEmpty())
            result.append("\n\n");
        result.append(compressJs ? compress(jsContent) : jsContent);

        out.print("<script");
        if (id != null) {
            out.print(" id=\"");
            out.print(id);
            out.print("\"");
        }
        if (src != null) {
            out.print(" src=\"");
            out.print(src);
            out.print("\"");
        }
        out.print(">");
        if (result.length() > 0) {
            out.print("\n");
            out.println(result.toString());
        }
        out.print("</script>");
    }

    public void setId(String id) {
        if (Strings.isNullOrEmpty(id))
            id = null;
        this.id = id;
    }

    public void setAppName(String appName) {
        if (Strings.isNullOrEmpty(appName))
            appName = null;
        this.appName = appName;
    }

    public void setSrc(String src) {
        if (Strings.isNullOrEmpty(src))
            src = null;
        this.src = src;
    }

}
