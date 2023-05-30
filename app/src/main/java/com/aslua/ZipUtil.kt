package com.aslua

import java.io.IOException

object ZipUtil {
    fun zip(sourceFilePath: String?, zipFilePath: String?): Boolean {
        return LuaUtil.zip(sourceFilePath, zipFilePath)
    }

    fun unzip(zipPath: String?, destPath: String?): Boolean {
        return try {
            LuaUtil.unZip(zipPath, destPath)
            true
        } catch (e: IOException) {
            false
        }
    }
}
