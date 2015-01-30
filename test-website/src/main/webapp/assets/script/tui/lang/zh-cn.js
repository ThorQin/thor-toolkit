/// <reference path="../tui.core.ts" />
(function () {
    var dict = {
        "Day": "天",
        "Days": "天",
        "Hour": "小时",
        "Hours": "小时",
        "Minute": "分钟",
        "Minutes": "分钟",
        "Second": "秒",
        "Seconds": "秒",
		"Choose Time": "选择时间",
        "Failed": "失败",
        "Error": "错误",
        "Upload failed, please check file type!": "文件上传失败，请检查文件类型！",
        "success": "成功",
        "notmodified": "查询内容没有改变！",
        "error": "发生内部错误！",
        "timeout": "请求超时！",
        "abort": "操作被中止！",
        "parsererror": "服务器返回内容无效！",
        "Ok": "确定",
        "Cancel": "取消",
        "Apply": "应用",
        "Sun": "日",
        "Mon": "一",
        "Tue": "二",
        "Wed": "三",
        "Thu": "四",
        "Fri": "五",
        "Sat": "六",
        "Rerresh": "刷新",
        "Clear": "清除",
        "Select": "选择",
        "Today": "今天",
        "Invalid input.": "输入内容无效。",
        "Previous": "上一页",
        "Next": "下一页"
    };

    // Register this dictionary.
    tui.registerTranslator("zh-cn", dict);
    // Or you can register a custom translate function like this:
    // tui.registerTranslator("en-us", function(str) {
    //		if (str === 'xxx' && hasOtherConditions)
    //			return 'the string what you wish to displayed';
    //		else
    //			......
    // });
})();
