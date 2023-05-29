package com.yongle.aslua.lua

import android.content.res.AssetManager
import android.widget.Toast
import com.yongle.aslua.MainActivity.Companion.context
import java.io.File
import java.io.FileOutputStream

fun copyResourcesToAppPath() {
    val assetManager: AssetManager = context.resources.assets
    val resourcesFolder = "/"

    // 获取resources文件夹下的所有资源文件的名称列表
    val resourceFiles: Array<String> = assetManager.list(resourcesFolder) ?: return

    for (fileName in resourceFiles) {
        val sourceFilePath = "$resourcesFolder/$fileName"
Toast.makeText(context,sourceFilePath,Toast.LENGTH_LONG).show()
        // 创建目标文件
        val destinationFile = File(context.filesDir, fileName)

        // 复制资源文件
        assetManager.open(sourceFilePath).use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }
        // 复制成功
    }
}





