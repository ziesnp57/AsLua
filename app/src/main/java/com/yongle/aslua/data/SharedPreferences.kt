package com.yongle.aslua.data

import android.content.Context
import com.yongle.aslua.MainActivity.Companion.context



val sp = context.getSharedPreferences("moin", Context.MODE_PRIVATE)

// 获取 SharedPreferences
fun stored(surface: String, key: String) {

    val editor = sp.edit()
    editor.putString(surface, key)
    editor.apply()

}

fun read(surface: String): String? {

    return sp.getString(surface, "")

}
