package com.yongle.aslua.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.gson.Gson
import com.yongle.aslua.MainActivity.Companion.context
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.ConnectionPool
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


//封装okhttp
class HttpClient {

    // 创建 OkHttpClient 对象，在其中添加拦截器和缓存等选项。
    private val httpClient = OkHttpClient.Builder()
        // 添加缓存选项
        .cache(Cache(File(context.cacheDir, "http-cache"), 50 * 1024 * 1024.toLong()))
        .connectTimeout(5, TimeUnit.SECONDS) // 设置连接超时时间
        .readTimeout(5, TimeUnit.SECONDS) // 设置读取超时时间
        .writeTimeout(5, TimeUnit.SECONDS) // 设置写入超时时间
        .connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS)) // 设置连接池

        // 该拦截器会在请求发出前执行，用于判断是否有网络连接，并在没有网络连接时强制使用缓存
        .addInterceptor { chain ->
            var request = chain.request()
            request = if (!isNetworkAvailable()) { // 如果没有网络连接
                request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE) // 设置强制使用缓存选项
                    .build()
            } else { // 如果有网络连接
                request.newBuilder()
                    .cacheControl( // 设置缓存选项
                        CacheControl.Builder()
                            .maxAge(60, TimeUnit.SECONDS) // 设置最大缓存时间为 60 秒
                            .build()
                    )
                    .build()
            }
            chain.proceed(request) // 继续执行请求链
        }
        .build()


    // 封装请求方法，传入 url 和回调接口参数
    fun okhttp(url: String, post: Map<String, String>?, headers: Map<String, String>?, callback: HttpCallback) {

        // 构建请求体
        val requestBody = post?.let {
            MultipartBody.Builder().setType(MultipartBody.FORM).apply {
                for ((key, value) in it) {
                    addFormDataPart(key, value)
                }
            }.build()
        }

        // 创建请求构建器
        val request = Request.Builder().apply {
            url(url)// 设置 URL 地址

            headers?.let {
                for ((key, value) in it) {
                    addHeader(key, value)
                }
            }

            if (post != null) {
                // 设置请求方法和请求体
                post(requestBody!!)
            }
        }.build()// 构建请求对象

        // 执行请求，并获取响应数据流。
        httpClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", e.message!!)
            }

            override fun onResponse(call: Call, response: Response) { // 处理响应结果
                  callback.onSuccess(response.code, response.body?.string(), response.headers) // 成功回调处理方法，返回响应结果
            }
        })
    }

    // 封装请求方法，传入 url 和回调接口参数
    fun put(url: String, put: Map<String, String>?, callback: HttpCallback) {
        val gson = Gson()
        val json = gson.toJson(put)

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)

        val request = Request.Builder()
            .url(url)
            .header("Connection", "close")
            .put(body)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TAG", e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(response.code, response.body?.string(), response.headers)
            }
        })
    }


    // 定义回调接口
    interface HttpCallback {
        fun onSuccess(code: Int, body: String?, headers: Headers) // 成功回调方法，返回响应结果
    }

    // 判断当前网络是否可用
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ))
    }
}