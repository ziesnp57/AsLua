package com.yongle.aslua.login

import android.util.Log
import com.tencent.mmkv.MMKV
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient

fun userlogin(uid: String, name: String, picture: String) {

    val kv = MMKV.defaultMMKV()

    // 发送 POST 请求示例
    HttpClient().post(GetApi.SEARCH_HOT_DETTS, "uid=$uid&user_name=$name&user_picture=$picture",
        object : HttpClient.HttpCallback {

            // 处理响应结果
            override fun onSuccess(response: String) {
                // 处理请求成功
                getuser(uid)
            }

            override fun onFailure(message: String?) {
                Log.e("TAG", message!!)
            }

        private fun getuser(uid: String) {

            // 发送 GET 请求示例
            HttpClient().get(GetApi.SEARCH_HOT_DETTS + "/" + uid, object : HttpClient.HttpCallback {

                // 处理响应结果
                override fun onSuccess(response: String) {
                    // 处理请求成功
                    kv.encode("user_login", response)
                }

                override fun onFailure(message: String?) {}
            })
        }
    })
}