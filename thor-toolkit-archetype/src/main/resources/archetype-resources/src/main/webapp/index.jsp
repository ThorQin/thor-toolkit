#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Hello World</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" href="${request.contextPath}/assets/script/tui/css/tui.min.css">
    <link href="${request.contextPath}/assets/script/font-awesome-4.2.0/css/font-awesome.min.css" rel="stylesheet">
    <script src="${request.contextPath}/assets/script/jquery-1.11.1.min.js"></script>
    <script src="${request.contextPath}/assets/script/tui/tui.all.js"></script>
    <script src="${request.contextPath}/assets/script/tui/lang/zh-cn.js"></script>
    <script src="${request.contextPath}/assets/script/tui/lang/en-us.js"></script>
    <script>
        $(function(){
            tui.ctrl.button("btnTest").on("click", function(){
                tui.infobox("<span id='serverInfo' style='display:inline-block;text-align:left;vertical-align:middle;width:180px;height:40px'></span>");
                var form = tui.ctrl.form();
                form.action("getServerInfo.do");
                form.target("serverInfo");
                form.field("*");
                form.targetProperty("innerHTML");
                form.submit();
            });
        });
    </script>
</head>
<body>
<h1>Hello World!</h1>
<br>
<a id="btnTest" class="tui-button tui-primary">Show server info ...</a>

</body>
</html>

