package com.yongle.aslua.data

import com.google.gson.annotations.SerializedName


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



data class ResponseDatu(
    val code: Int,
    val msg: String,
    val data: List<ContentTypeu>
)

data class ContentTypeu(
    @SerializedName("manual_id") val manualId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_portrait") val userPortrait: String,
    @SerializedName("type_id") val typeId: Int,
    @SerializedName("type_name") val typeName: String,
    @SerializedName("manual_name") val manualName: String,
    @SerializedName("manual_content") val manualContent: String,
    @SerializedName("manual_source") val manualSource: String,
    @SerializedName("manual_platform") val manualPlatform: String,
    @SerializedName("manual_time") val manualTime: String,
    @SerializedName("manual_time_add") val manualTimeAdd: String,
    @SerializedName("manual_time_update") val manualTimeUpdate: String,
    @SerializedName("comment_time") val commentTime: String,
    @SerializedName("manual_down") val manualDown: Int,
    @SerializedName("manual_hits") val manualHits: Int,
    @SerializedName("manual_comment") val manualComment: Int,
    @SerializedName("manual_up") val manualUp: Int,
    @SerializedName("manual_fav") val manualFav: Int
)