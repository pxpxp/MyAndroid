package com.pxp.http.cb

import com.pxp.http.model.ApiError
import com.pxp.http.model.ApiTag

/**
 * 给java版本用的，kt接口的空实现 java代码调用没效果
 */
open class SimpleBasicListener : ApiBasicListener {
    /**
     * 请求开始
     */
    override fun onStart(tag: ApiTag?) {

    }

    /**
     * 请求完成
     */
    override fun onCompleted(tag: ApiTag?) {

    }

    /**
     * 请求出错
     */
    override fun onError(e: ApiError, tag: ApiTag?) {

    }
}