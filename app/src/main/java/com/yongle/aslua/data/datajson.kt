package com.yongle.aslua.data

import com.google.gson.annotations.SerializedName

// 创建数据类来存储您的数据
data class ResponseData(
    val msg: String,
    val data: List<ContentTypes>
)

data class ContentTypes(
    @SerializedName("id") val typeId: Int,
    @SerializedName("name") val typeName: String,
)

data class Datalist(
    val current_page: String,
    val last_page: String,
    val data: List<ContentType>
)

data class ContentType(
    @SerializedName("id") val id: String,
    @SerializedName("type_id") val typeId: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_picture") val userPicture: String,
    @SerializedName("datalist_name") val datalistName: String,
    @SerializedName("datalist_data") val datalistData: String,
    @SerializedName("time_add") val timeAdd: String,
    @SerializedName("time_update") val timeUpdate: String,
    @SerializedName("datalist_down") val datalistDown: String,
    @SerializedName("datalist_fav") val datalistFav: String,
    @SerializedName("datalist_up") val datalistUp: String,
    @SerializedName("sh") val sh: Int,
)

data class Doclist(
    val current_page: String,
    val last_page: String,
    val data: List<Content>
)

data class Content(
    @SerializedName("id") val id: Int,
    @SerializedName("uid") val uid: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
    @SerializedName("time_add") val time: String,
)

data class Datalists(
    val data: String,
    val id: Int,
)

data class App(
    val appName: String,
    val appVer: String,
    val appCode: String,
    val appPackageName: String,
    val debug: Boolean,
    val permission: ArrayList<String>
)

data class Permission(
    val data: String,
)

data class AppList(
    val icon: String?,
    val appName: String,
    val appVer: String,
    val appPackageName: String,
    val name: String,
)