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
        $(function(){
            tui.ctrl.button("btnTest").on("click", function(){
                var form = tui.ctrl.form();
                form.action("getServerInfo.do");
                form.on("receive", function(data){
                    tui.errbox(data);
                });
                form.submit();
            });

            tui.ctrl.button("btnTest1").on("click", function(){
                var form = tui.ctrl.form();
                form.immediateValue({"name":"Thor", "age": 40});
                form.action("echo.do");
                form.on("receive", function(data){
                    tui.infobox(JSON.stringify(data));
                });
                form.submit();
            });
        });
    </tt:script>
</head>
<body>
<h1>${loc.get("title")}</h1>
<br>
<a id="btnTest" class="tui-button tui-primary">Show server info ...</a>
<a id="btnTest1" class="tui-button tui-success">Post some data to server ...</a>
<br>
${myService.getServerTime()}
<br>
session:${session.get("lang")}
</body>
</html>

