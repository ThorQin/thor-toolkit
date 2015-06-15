#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="/thor-toolkit" prefix="thor" %>
<!DOCTYPE html>
<html>
<head>
	<thor:toolkit bundle="message"/>
    <title>${loc.get("title")}</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" href="${root}/assets/script/tui/css/tui.min.css">
    <link href="${root}/assets/script/font-awesome-4.2.0/css/font-awesome.min.css" rel="stylesheet">
    <script src="${root}/assets/script/jquery-1.11.1.min.js"></script>
    <script src="${root}/assets/script/tui/tui.all.js"></script>
    <script src="${root}/assets/script/tui/lang/zh-cn.js"></script>
    <script src="${root}/assets/script/tui/lang/en-us.js"></script>
    <thor:script>
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
    </thor:script>
</head>
<body>
<h1>${loc.get("title")}</h1>
<br>
<a id="btnTest" class="tui-button tui-primary">Show server info ...</a>

</body>
</html>

