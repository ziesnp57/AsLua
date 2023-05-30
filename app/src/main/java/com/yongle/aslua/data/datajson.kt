package com.yongle.aslua.data

import com.google.gson.annotations.SerializedName

data class ResponseDatas(
    val code: Int,
    val msg: String,
)

// 创建数据类来存储您的数据
data class ResponseData(
    val code: Int,
    val msg: String,
    val data: List<ContentTypes>
)

data class ContentTypes(
    @SerializedName("id") val typeId: Int,
    @SerializedName("name") val typeName: String,
)

