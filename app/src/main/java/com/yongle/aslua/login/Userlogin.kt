package com.yongle.aslua.login

import com.tencent.mmkv.MMKV
import com.yongle.aslua.api.GetApi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

fun userlogin(uid: String, name: String, picture: String) {

    val kv = MMKV.defaultMMKV()

//  post 请求示例
    val requestBody = FormBody.Builder()
        .add("uid", uid)
        .add("user_name", name)
        .add("user_picture", picture)
        .build()

    val request = Request.Builder()
        .url(GetApi.SEARCH_HOT_DETTS)
        .post(requestBody)
        .build()

    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // 处理请求失败
        }

        override fun onResponse(call: Call, response: Response) {
            // 处理请求成功
            getuser(uid)
        }

        private fun getuser(uid: String) {

            val clients = OkHttpClient()

            val requests = Request.Builder()
                .url(GetApi.SEARCH_HOT_DETTS + "/" + uid)
                .build()

            clients.newCall(requests).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // 处理请求失败
                }

                override fun onResponse(call: Call, response: Response) {
                    // 处理请求成功
                    kv.encode("user_login", response.body?.string())
                }
            })
        }
    })

}