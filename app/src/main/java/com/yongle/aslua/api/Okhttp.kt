package com.yongle.aslua.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.yongle.aslua.MainActivity.Companion.context
import com.yongle.aslua.data.read
import com.yongle.aslua.data.stored
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


//封装okhttp
class HttpClient {

    // 创建 OkHttpClient 对象，在其中添加拦截器和缓存等选项。
    private val httpClient = OkHttpClient.Builder()
        // 添加缓存选项
        .cache(Cache(File(context.cacheDir, "http-cache"), 20 * 1024 * 1024.toLong()))
        .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间
        .readTimeout(5, TimeUnit.SECONDS) // 设置读取超时时间
        .writeTimeout(5, TimeUnit.SECONDS) // 设置写入超时时间

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

    // 封装 GET 请求方法，传入 url 和回调接口参数
    fun getlist(url: String,  callback: HttpCallback) {

        val request = Request.Builder() // 创建请求构建器
            .url(url) // 设置 URL 地址
            .addHeader("If-None-Match", read("listetag").toString()) // 添加 ETag 请求头
            .build() // 构建请求对象

        // 执行请求，并获取响应数据流。
        httpClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) { // 处理响应结果

                if (response.code == 200) {

                    stored("listetag", response.header("ETag")!!)
                    callback.onSuccess(response.body!!.string()) // 成功回调处理方法，返回响应结果
                }

            }
        })
    }

    // 封装 POST 请求方法，传入 url 和回调接口参数
    fun postdata(url: String, id: String, tab: String, nam:String, text:String, decode:String, callback: HttpCallback) {

        // 创建请求体对象
        val requestBody = FormBody.Builder()
            .add("user_id", id)
            .add("type_id", tab)
            .add("datalist_name", nam)
            .add("datalist_data", text)
            .add("data", decode)
            .build()


        val request = Request.Builder() // 创建请求构建器
            .url(url) // 设置 URL 地址
            .post(requestBody) // 设置请求体
            .build() // 构建请求对象

        // 执行请求，并获取响应数据流。
        httpClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) { // 处理响应结果
                println(request)
                if (response.code == 200) {
                    callback.onSuccess(response.body!!.string()) // 成功回调处理方法，返回响应结果
                }

            }
        })

    }


    // 定义回调接口，用于异步获取响应数据
    interface HttpCallback {
        fun onSuccess(response: String)
        fun onFailure(message: String?)
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