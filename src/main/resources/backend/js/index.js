/* 自定义trim */
function trim(str) {  //删除左右两端的空格,自定义的trim()方法
    return str == undefined ? "" : str.replace(/(^\s*)|(\s*$)/g, "")
}

//获取url地址上面的参数
function requestUrlParam(argname) {
    var url = location.href //获取完整的请求url路径
    //将?问号后的路径提取出来，并根据&进行分组
    var arrStr = url.substring(url.indexOf("?") + 1).split("&")

    //遍历提取出传入的id
    for (var i = 0; i < arrStr.length; i++) {
        var loc = arrStr[i].indexOf(argname + "=")
        if (loc != -1) {
            return arrStr[i].replace(argname + "=", "").replace("?", "")
        }
    }
    return ""
}
