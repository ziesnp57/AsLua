package com.yongle.aslua.data

import com.yongle.aslua.MainActivity
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.room.datacache


// 管理初始化
fun init() {

    //加载网络数据
  getnn()

}



fun getnn() {
    // 发送 GET 请求示例
    HttpClient().getlist(GetApi.SEARCH_DEFAULT, object : HttpClient.HttpCallback {

        // 处理响应结果
        override fun onSuccess(response: String) {

        //更新本地缓存
        datacache(MainActivity.Companion.GsonFactory.instance.fromJson(response, ResponseData::class.java).data)

        }

        override fun onFailure(message: String?) {}
    })
}



