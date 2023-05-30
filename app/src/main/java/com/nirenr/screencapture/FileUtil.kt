package com.nirenr.screencapture

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by ryze on 2016-5-26.
 */
object FileUtil {
    //系统保存截图的路径
    private val SCREENCAPTURE_PATH = "ScreenCapture" + File.separator + "Screenshots" + File.separator

    private const val SCREENSHOT_NAME = "Screenshot"
    private fun getAppPath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            Environment.getExternalStorageDirectory().toString()
        } else {
            context.filesDir.toString()
        }
    }

    private fun getScreenShots(context: Context): String {
        val stringBuffer = StringBuffer(getAppPath(context))
        stringBuffer.append(File.separator)
        stringBuffer.append(SCREENCAPTURE_PATH)
        val file = File(stringBuffer.toString())
        if (!file.exists()) {
            file.mkdirs()
        }
        return stringBuffer.toString()
    }

    fun getScreenShotsName(context: Context): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", Locale.CHINESE)
        val date = simpleDateFormat.format(Date())
        val stringBuffer = StringBuffer(getScreenShots(context))
        stringBuffer.append(SCREENSHOT_NAME)
        stringBuffer.append("_")
        stringBuffer.append(date)
        stringBuffer.append(".png")
        return stringBuffer.toString()
    }
}
