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

    // 创建文件夹
    val libDir = File(dir, "libs")
    if (!libDir.exists()) libDir.mkdirs()

    // 创建文件夹
    val luaDir = File(dir, "project")
    if (!luaDir.exists()) luaDir.mkdirs()

    // 创建lua文件
    val luaFile = File(dir, "run.lua")
    if (!luaFile.exists()) {
        luaFile.createNewFile()
    }

}






