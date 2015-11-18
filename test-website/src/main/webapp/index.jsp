<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="/thor-toolkit" prefix="tt" %>
<!DOCTYPE html>
<html>
<head>
	<tt:env bundle="message"/>
	<tt:service service="myService"/>
    <title>${loc.get("title")}</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" href="${root}/assets/script/tui/css/tui.min.css">
    <link href="${root}/assets/script/font-awesome-4.2.0/css/font-awesome.min.css" rel="stylesheet">
    <script src="${root}/assets/script/jquery-1.11.1.min.js"></script>
    <script src="${root}/assets/script/tui/tui.all.js"></script>
    <script src="${root}/assets/script/tui/lang/zh-cn.js"></script>
    <script src="${root}/assets/script/tui/lang/en-us.js"></script>
    <tt:script>
        tui.lang = '${session.get("lang")}';
		if (tui.lang === "")
			tui.lang = "en-us";
        var languages = [
            {"key":"en-us", "value":"English"},
            {"key":"zh-cn", "value":"中文"}
        ];
        $(function(){
            tui.ctrl.button("btnTest").on("click", function(){
                var form = tui.ctrl.form();
                form.action("getServerInfo.do");
                form.on("receive", function(data){
                    tui.infobox(data);
                });
                form.submit();
            });

            tui.ctrl.input("langSelector").on("select", function(data){
                if (this.value() === tui.lang)
                    return;
                var form = tui.ctrl.form();
                form.action("setLanguage.do");
                form.immediateValue(this.value());
                form.targetRedirect(".");
                form.submit();
            });
			tui.ctrl.input("langSelector").value(tui.lang);
        });
    </tt:script>
</head>
<body>
<div style="background:#204060;padding:4px;position:fixed;left:0;top:0;right:0">
	<div style="float:right;">
		<span style="color:white">${loc.get("language")}</span>
		<span id="langSelector" class="tui-input" data-type="select" data-data="languages" ></span>
	</div>
	<div style="clear:both"></div>
</div>
<h1 style="margin-top:60px">${loc.get("title")}</h1>
<hr>
<br>
<a id="btnTest" class="tui-button tui-primary">${loc.get("show.server.info")}</a>
</body>
</html>

