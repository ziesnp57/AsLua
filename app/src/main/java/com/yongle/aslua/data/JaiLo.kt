package com.yongle.aslua.data

import com.yongle.aslua.MainActivity
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.room.datacache
import okhttp3.Headers


// 管理初始化
fun init() {


    //加载网络数据
  getnn()
  //  getnu()
}



fun getnn() {
    // 发送 GET 请求示例

    val header = mapOf(
        "If-None-Match" to read("listetag")!!.toString() // 添加 ETag 请求头
    )

    HttpClient().okhttp(GetApi.SEARCH_DEFAULT, null, header, object : HttpClient.HttpCallback {

        // 处理响应结果
        override fun onSuccess(code: Int, body: String?, headers: Headers) {

        if (code == 200) {
            // 获取 ETag 响应头
            val etag = headers["ETag"]
            // 保存 ETag 响应头
            if (etag != null) {
                stored("listetag", etag)
            }

        //更新本地缓存
        datacache(MainActivity.Companion.GsonFactory.instance.fromJson(body, ResponseData::class.java).data)
            }
        }
    })
}

fun getnu() {
    // 发送 GET 请求示例

    val header = mapOf(
        "If-None-Match" to read("dataetag")!!.toString() // 添加 ETag 请求头
    )

    HttpClient().okhttp(GetApi.SEARCH_HOT_DETAIL, null, header, object : HttpClient.HttpCallback {

        // 处理响应结果
        override fun onSuccess(code: Int, body: String?, headers: Headers) {
            if (code == 200) {
                // 保存ETag值
              //  stored("dataetag", headers["ETag"]!!.toString())

                println(body)
                //更新本地缓存
             //    datacache(Code.Companion.GsonFactory.instance.fromJson(body?.string(), Datalist::class.java).data)
            }
        }
    })
}