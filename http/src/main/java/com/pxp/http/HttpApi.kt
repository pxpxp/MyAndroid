package com.pxp.http

import android.net.Uri
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.pxp.http.cb.ApiListener
import com.pxp.http.model.ApiData
import com.pxp.http.model.ApiError
import com.pxp.http.model.ApiInfo
import com.pxp.http.model.ApiResponse
import com.pxp.http.model.ApiTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


/**
 *  @author pxp
 */
class HttpApi private constructor(
    //基础请求host,"https://overseas.lavatest.com"  最后不需要/ 最后不需要/ 最后不需要/
    private var baseHost: String,
    //解析的状态码字段名称
    private var codeKey: String,
    //解析的数据字段名称
    private var dataKey: String,
    //解析的消息字段名称
    private var msgKey: String,
    //解析的时间戳字段名称
    private var timeKey: String,
    //状态码成功值
    private var successCode: Int,
    //连接超时,单位秒
    private var connectTimeout: Long,
    //写超时,单位秒
    private var writeTimeout: Long,
    //读超时,单位秒
    private var readTimeout: Long,
    //log日志输出
    private var logInterceptor: HttpLoggingInterceptor.Logger? = null,
    //请求头数据配置
    private var headerInfo: () -> Map<String, String>,
    //请求返回的消息分发数据,json格式
    private var mediator: (mediator: String) -> Unit
) {
    companion object {
        lateinit var instance: HttpApi

        /**
         * 初始化网络相关配置
         * @param baseHost 网络请求基础URL
         * @param codeKey  解析的状态码字段名称
         * @param dataKey  解析的数据字段名称
         * @param msgKey   解析的消息字段名称
         * @param timeKey  解析的时间戳字段名称
         * @param successCode 状态码成功值
         * @param logInterceptor 请求日志输出
         * @param headerInfo 请求头数据
         */
        fun init(
            baseHost: String,
            codeKey: String = "code",
            dataKey: String = "data",
            msgKey: String = "mediator",
            timeKey: String = "timestamp",
            successCode: Int = 1000,
            connectTimeout: Long = 5,
            writeTimeout: Long = 30,
            readTimeout: Long = 30,
            logInterceptor: HttpLoggingInterceptor.Logger? = null,
            headerInfo: () -> Map<String, String>,
            mediator: (mediator: String) -> Unit
        ) {
            instance = HttpApi(
                baseHost, codeKey, dataKey, msgKey, timeKey,
                successCode, connectTimeout, writeTimeout, readTimeout,
                logInterceptor, headerInfo, mediator
            )
        }
    }

    /**
     * 请求类型
     */
    private enum class RequestType {
        GET,
        POST,
        PUT
    }

    /**
     * 接口请求服务
     */
    private var apiInfo: ApiInfo

    /**
     * io 线程调度
     */
    private val ioCoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    /**
     * 主线程调度
     */
    private val mainCoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    /**
     * 请求头信息
     */
    private var hostHeader: Map<String, String> = mutableMapOf()

    /**
     * 请求地址
     */
    private var hostUrl = baseHost

    init {
        //配置retrofit
        val retrofit = Retrofit.Builder()
            .client(createOkHttpClient())
            .baseUrl(baseHost)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(ConverterFactory())
            .build()
        apiInfo = retrofit.create(ApiInfo::class.java)
    }

    /**
     * 创建一个okHttp客户端对象
     */
    private fun createOkHttpClient(): OkHttpClient {
        //设置log拦截器
        val logging = logInterceptor?.let {
            HttpLoggingInterceptor(it)
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                //设定请求头信息
                val request = chain.request()
                val requestBuilder = request.newBuilder()
                val header = hostHeader.ifEmpty {
                    //hostHeader为空说明本次请求没有设定自定义请求头数据,所以使用默认的请求头数据
                    headerInfo.invoke()
                }

                //添加请求头数据
                for ((k, v) in header) {
                    requestBuilder.addHeader(k, v)
                }
                //自定义
                Uri.parse(hostUrl).host?.let {
                    requestBuilder.url(request.url.newBuilder().host(it).build())
                }
                chain.proceed(requestBuilder.build())
            })
            //https
            .hostnameVerifier(TrustAll.hostnameVerifier())
            //连接超时
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            //写数据超时
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            //读数据超时
            .readTimeout(readTimeout, TimeUnit.SECONDS)
        logging?.let {
            //log输出
            it.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(it)
        }

        TrustAll.socketFactory()?.let {
            builder.sslSocketFactory(it, TrustAll.trustManager())
        }
        return builder.build()
    }

    /**
     * 异步get请求
     * @param url 请求路径
     * @param dataListener 请求成功回调,根据接口数据设定类型,实现其中之一即可
     * @param data 请求参数封装
     * @param cls 请求数据类型
     */
    fun <T> get(
        lifeCoroutineScope: LifecycleCoroutineScope? = null,
        url: String,
        dataListener: ApiListener<T>? = null,
        data: ApiData,
        cls: Class<T>
    ) {
        lifeCoroutineScope?.let {
            it.launch {
                withContext(ioCoroutineScope.coroutineContext) {
                    toNext(url, dataListener, data, cls, requestType = RequestType.GET)
                }
            }
        } ?: run {
            ioCoroutineScope.launch {
                toNext(url, dataListener, data, cls, requestType = RequestType.GET)
            }
        }
    }

    fun <T> get(
        url: String,
        dataListener: ApiListener<T>? = null,
        data: ApiData,
        type: Type
    ) {
        ioCoroutineScope.launch {
            toNext(url, dataListener, data, type = type, requestType = RequestType.GET)
        }
    }

    /**
     * 异步post请求
     * @param url 请求路径
     * @param dataListener 请求成功回调,根据接口数据设定类型,实现其中之一即可
     * @param data 请求参数封装
     * @param cls 请求数据类型
     */
    fun <T> post(
        lifeCoroutineScope: LifecycleCoroutineScope? = null,
        url: String,
        dataListener: ApiListener<T>? = null,
        data: ApiData,
        cls: Class<T>
    ) {
        lifeCoroutineScope?.let {
            it.launch {
                withContext(ioCoroutineScope.coroutineContext) {
                    toNext(url, dataListener, data, cls, requestType = RequestType.POST)
                }
            }
        } ?: run {
            ioCoroutineScope.launch {
                toNext(url, dataListener, data, cls, requestType = RequestType.POST)
            }
        }
    }

    fun <T> post(
        url: String,
        dataListener: ApiListener<T>? = null,
        data: ApiData,
        type: Type
    ) {
        ioCoroutineScope.launch {
            toNext(url, dataListener, data, type = type, requestType = RequestType.POST)
        }
    }


    /**
     * 异步put请求
     * @param url 请求路径
     * @param dataListener 请求成功回调,根据接口数据设定类型,实现其中之一即可
     * @param data 请求参数封装
     * @param cls 请求数据类型
     */
    fun <T> put(
        lifeCoroutineScope: LifecycleCoroutineScope? = null,
        url: String,
        dataListener: ApiListener<T>? = null,
        data: ApiData,
        cls: Class<T>
    ) {
        lifeCoroutineScope?.let {
            it.launch {
                withContext(ioCoroutineScope.coroutineContext) {
                    toNext(url, dataListener, data, cls, requestType = RequestType.PUT)
                }
            }
        } ?: run {
            ioCoroutineScope.launch {
                toNext(url, dataListener, data, cls, requestType = RequestType.PUT)
            }
        }
    }

    fun <T> put(
        url: String,
        dataListener: ApiListener<T>? = null,
        data: ApiData,
        type: Type
    ) {
        ioCoroutineScope.launch {
            toNext(url, dataListener, data, type = type, requestType = RequestType.PUT)
        }
    }


    private suspend fun <T> toNext(
        url: String,
        dataListener: ApiListener<T>? = null,
        data: ApiData,
        cls: Class<T>? = null,
        type: Type? = null,
        requestType: RequestType
    ) {
        data.basicListener?.onStart(data.tag)
        val response = when (requestType) {
            RequestType.GET -> {
                if (cls != null) getSync(url, data, cls)
                else getSync(url, data, type!!)
            }

            RequestType.POST -> {
                if (cls != null) postSync(url, data, cls)
                else postSync(url, data, type!!)
            }

            RequestType.PUT -> {
                if (cls != null) putSync(url, data, cls)
                else putSync(url, data, type!!)
            }
        }
        data.basicListener?.onCompleted(data.tag)
        if (response.error != null) {
            data.basicListener?.onError(response.error!!, response.tag)
        } else {
            if (data.isList) {
                dataListener?.onSuccessList(response.dataList!!, response.tag)
            } else {
                dataListener?.onSuccess(response.data!!, response.tag)
            }
        }
    }

    suspend fun <T> getSync(
        url: String,
        data: ApiData,
        cls: Class<T>
    ): ApiResponse<T> {
        return execute(
            requestType = RequestType.GET,
            url = url,
            tag = data.tag,
            isList = data.isList,
            params = data.params,
            body = data.body,
            header = data.header,
            isNoKey = data.isNoKey,
            replaceHost = data.replaceHost,
            cls = cls
        )
    }

    suspend fun <T> getSync(
        url: String,
        data: ApiData,
        type: Type,
    ): ApiResponse<T> {
        return execute(
            requestType = RequestType.GET,
            url = url,
            tag = data.tag,
            isList = data.isList,
            params = data.params,
            body = data.body,
            header = data.header,
            isNoKey = data.isNoKey,
            replaceHost = data.replaceHost,
            type = type
        )
    }

    suspend fun <T> postSync(
        url: String,
        data: ApiData,
        cls: Class<T>
    ): ApiResponse<T> {
        return execute(
            requestType = RequestType.POST,
            url = url,
            tag = data.tag,
            isList = data.isList,
            params = data.params,
            body = data.body,
            header = data.header,
            isNoKey = data.isNoKey,
            replaceHost = data.replaceHost,
            cls = cls
        )
    }

    suspend fun <T> postSync(
        url: String,
        data: ApiData,
        type: Type,
    ): ApiResponse<T> {
        return execute(
            requestType = RequestType.POST,
            url = url,
            tag = data.tag,
            isList = data.isList,
            params = data.params,
            body = data.body,
            header = data.header,
            isNoKey = data.isNoKey,
            replaceHost = data.replaceHost,
            type = type
        )
    }

    suspend fun <T> putSync(
        url: String,
        data: ApiData,
        cls: Class<T>
    ): ApiResponse<T> {
        return execute(
            requestType = RequestType.PUT,
            url = url,
            tag = data.tag,
            isList = data.isList,
            params = data.params,
            body = data.body,
            header = data.header,
            isNoKey = data.isNoKey,
            replaceHost = data.replaceHost,
            cls = cls
        )
    }

    suspend fun <T> putSync(
        url: String,
        data: ApiData,
        type: Type,
    ): ApiResponse<T> {
        return execute(
            requestType = RequestType.PUT,
            url = url,
            tag = data.tag,
            isList = data.isList,
            params = data.params,
            body = data.body,
            header = data.header,
            isNoKey = data.isNoKey,
            replaceHost = data.replaceHost,
            type = type
        )
    }

    /**
     * 处理请求
     * @param requestType 请求类型
     * @param replaceHost 该请求替换的基础路径,为空表示不替换
     * @param url 请求路径
     * @param tag 请求标识
     * @param isList 返回数据是否是数组类型
     * @param header 请求头
     * @param params 请求参数-封装在url之后, key=value & key=value方式
     * @param body  请求参数-封装在post请求体之内
     * @param isNoKey  true该请求返回的数据直接解析为指定的数据类型
     * @param cls 返回数据的类型
     */
    private suspend fun <T> execute(
        requestType: RequestType,
        replaceHost: String?,
        url: String,
        tag: ApiTag? = null,
        isList: Boolean = false,
        header: Map<String, String>? = null,
        params: Map<String, Any>? = null,
        body: String? = null,
        isNoKey: Boolean = false,
        cls: Class<T>? = null,
        type: Type? = null
    ): ApiResponse<T> {
        val response = ApiResponse<T>()
        response.tag = tag
        try {
            hostUrl = replaceHost ?: baseHost
            hostHeader = header ?: mutableMapOf()
            //请求开始
            val r: Response<JsonObject> = when (requestType) {
                RequestType.GET -> {
                    executeGet(url, params)
                }

                RequestType.POST -> {
                    executePost(url, params, body)
                }

                RequestType.PUT -> {
                    executePut(url, params, body)
                }
            }
            //请求完成
            //检测请求是否成功
            if (!r.isSuccessful) {
                response.error = ApiError(r.code())
                return response
            }
            //获取返回数据
            val jsonObject = r.body()
            if (jsonObject == null || jsonObject.toString().isEmpty()) {
                response.error = ApiError(-0x01, throwable = Throwable("没有解析到任何数据"))
                return response
            }

            //不包含要解析的字段
            if (!checkSuccess(jsonObject)) {
                //判断是否设置了不需要这些字段
                if (!isNoKey) {
                    val apiError = ApiError()
                    if (jsonObject.has(msgKey))
                        toMediator(jsonObject.getAsJsonObject(msgKey).toString())
                    if (jsonObject.has(codeKey)) {
                        apiError.errorCode = jsonObject.get(codeKey).asInt
                    }
                    if (jsonObject.has(timeKey)) {
                        apiError.timestamp = jsonObject.get(timeKey).asLong
                    }
                }
                //直接解析并返回数据-针对第三方的接口返回
                val gson = Gson()
                if (isList) {
                    val jsonArray = jsonObject.asJsonArray
                    val dataList = arrayListOf<T>()
                    for (j in jsonArray) {
                        val d = if (cls != null) gson.fromJson(j, cls) else gson.fromJson(j, type)
                        dataList.add(d)
                    }
                    response.dataList = dataList
                } else {
                    val d = if (cls != null) gson.fromJson(jsonObject, cls) else gson.fromJson(
                        jsonObject,
                        type
                    )
                    response.data = d
                }
                return response
            }
            val code = jsonObject.get(codeKey).asInt
            if (code != successCode) {
                //成功码对应不上- 获取mediator
                if (jsonObject.has(msgKey))
                    toMediator(jsonObject.getAsJsonObject(msgKey).toString())
                if (jsonObject.has(timeKey))
                    response.error =
                        ApiError(code, timestamp = jsonObject.get(timeKey).asLong)
                return response
            }
            //请求成功,获取参数
            val gson = Gson()
            if (isList) {
                val jsonArray = jsonObject.getAsJsonArray(dataKey)
                val dataList = arrayListOf<T>()
                for (j in jsonArray) {
                    val d = if (cls != null) gson.fromJson(j, cls) else gson.fromJson(j, type)
                    dataList.add(d)
                }
                response.dataList = dataList
            } else {
                val jsonData = jsonObject.getAsJsonObject(dataKey)
                val d =
                    if (cls != null) gson.fromJson(jsonData, cls) else gson.fromJson(jsonData, type)
                response.data = d
            }

            if (jsonObject.has(msgKey))
                toMediator(jsonObject.getAsJsonObject(msgKey).toString())
            return response
        } catch (e: Throwable) {
            response.error = ApiError(-0x11, throwable = e)
            return response
        }
    }


    /**
     * 校验是否包含需要解析的字段
     */
    private fun checkSuccess(jsonObject: JsonObject): Boolean {
        if (!jsonObject.has(codeKey))
            return false
        val code = jsonObject.get(codeKey).asInt
        return if (code == successCode) {
            jsonObject.has(codeKey) && jsonObject.has(timeKey) && jsonObject.has(dataKey)
        } else {
            jsonObject.has(codeKey) && jsonObject.has(timeKey) && jsonObject.has(msgKey)
        }
    }

    private fun toMediator(data: String) {
        mainCoroutineScope.launch {
            mediator.invoke(data)
        }
    }

    /**
     * 处理put请求
     */
    private suspend fun executePut(
        url: String,
        params: Map<String, Any>?,
        body: String?
    ): Response<JsonObject> {
        return if (body != null && params != null) {
            val b = body.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            apiInfo.executePut(url, params2String(params), b)
        } else if (body != null) {
            val b = body.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            apiInfo.executePut(url, b)
        } else if (params != null) {
            apiInfo.executePut(url, params2String(params))
        } else {
            apiInfo.executePut(url)
        }
    }

    /**
     * 处理post请求
     */
    private suspend fun executePost(
        url: String,
        params: Map<String, Any>?,
        body: String?
    ): Response<JsonObject> {
        return if (body != null && params != null) {
            val b = body.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            apiInfo.executePost(url, params2String(params), b)
        } else if (body != null) {
            val b = body.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            apiInfo.executePost(url, b)
        } else if (params != null) {
            apiInfo.executePost(url, params2String(params))
        } else {
            apiInfo.executePost(url)
        }
    }


    /**
     * 处理get请求
     */
    private suspend fun executeGet(
        url: String,
        params: Map<String, Any>?
    ): Response<JsonObject> {
        return if (params != null) {
            apiInfo.executeGet(url, params2String(params))
        } else {
            apiInfo.executeGet(url)
        }
    }

    /**
     * 将params的v值全部转变为string
     */
    private fun params2String(params: Map<String, Any>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((k, v) in params) {
            v.let {
                result[k] = v.toString()
            }
        }
        return result
    }
}