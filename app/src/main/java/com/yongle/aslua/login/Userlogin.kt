package com.yongle.aslua.login

import com.google.gson.GsonBuilder
import com.tencent.mmkv.MMKV
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import okhttp3.Headers

fun userlogin(uid: String, name: String, picture: String) {

    val kv = MMKV.defaultMMKV()!!

    // 发送 POST 请求示例
    val post = mapOf("uid" to uid, "user_name" to name, "user_picture" to picture)
    HttpClient().okhttp(GetApi.SEARCH_HOT_DETTS, post, null,
        object : HttpClient.HttpCallback {

            // 处理响应结果
            override fun onSuccess(code: Int, body: String?, headers: Headers) {

                // 处理请求成功
                HttpClient().okhttp(GetApi.SEARCH_HOT_DETTS + "/" + uid, null, null,
                    object : HttpClient.HttpCallback {

                        // 处理响应结果
                        override fun onSuccess(code: Int, body: String?, headers: Headers) {

                            val gson = GsonBuilder().create()
                            val username = gson.fromJson(body, UserLogin::class.java).user_name
                            val userPicture = gson.fromJson(body, UserLogin::class.java).user_picture

                            if (username != name || userPicture != picture) {

                                val put = mapOf("user_name" to name, "user_picture" to picture)
                                HttpClient().put(GetApi.SEARCH_HOT_DETTS + "/$uid", put,
                                    object : HttpClient.HttpCallback {

                                        // 处理响应结果
                                        override fun onSuccess(code: Int, body: String?, headers: Headers) {
                                            if (code == 200) {
                                                // 处理请求成功
                                                val editor = kv.edit()
                                                editor.putString("user_login", body)
                                                editor.apply()
                                            }
                                        }

                                    })

                            } else {
                                // 处理请求成功
                                val editor = kv.edit()
                                editor.putString("user_login", body)
                                editor.apply()
                            }

                        }

                    })
            }
        })
}