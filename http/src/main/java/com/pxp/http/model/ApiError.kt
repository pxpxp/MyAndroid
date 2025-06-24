package com.pxp.http.model

/**
 * 请求错误信息包装
 */
class ApiError(
    //错误码
    // -0X01没有数据,返回的json为空
    // -0X11 表示解析出错
    var errorCode: Int = -10086,
    //时间戳
    var timestamp: Long = 0,
    //错误异常
    var throwable: Throwable? = null
) {
    override fun toString(): String {
        return "ApiError(errorCode=$errorCode, timestamp=$timestamp, throwable=$throwable)"
    }
}

//class Mediator(
//    //目标,例如 Notice
//    var target: String = "",
//    //动作,例如 Toast、Dialog
//    var action: String = "",
//    //参数-格式不定,业务自行处理
//    var params: JsonObject = JsonObject(),
//) {
//    override fun toString(): String {
//        return "Mediator(target='$target', action='$action', params=$params)"
//    }
//}