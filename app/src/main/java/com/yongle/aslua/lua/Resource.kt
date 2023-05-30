package com.yongle.aslua.lua

import com.yongle.aslua.MainActivity.Companion.sdDir
import java.io.File

/**
 * 资源管理
 */
fun copyResourcesToAppPath() {

    // 创建文件夹
    val dir = File(sdDir, "AsLua")
    if (!dir.exists()) dir.mkdirs()




}






