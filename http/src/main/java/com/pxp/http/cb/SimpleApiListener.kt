package com.pxp.http.cb

import com.pxp.http.model.ApiTag


/**
 * 给java版本用的，kt接口的空实现 java代码调用没效果
 */
open class SimpleApiListener<T> :ApiListener<T>{
    /**
     * 成功,数据为单独数据类型
     */
    override fun onSuccess(data: T, tag: ApiTag?) {

    }

    /**
     * 成功,数据为列表数据类型
     */
    override fun onSuccessList(data: List<T>, tag: ApiTag?) {

    }
}