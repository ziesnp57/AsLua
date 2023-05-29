package com.yongle.aslua.api

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog

//检查更新
fun updateDialog(context: android.content.Context) {

    //
   val version = context.packageManager.getPackageInfo(context.packageName, 0).versionCode



val dialog = AlertDialog.Builder(context)
        .setTitle("检查更新")
        .setMessage("当前版本：$version\n最新版本：$version")
        .setPositiveButton("更新") { _, which ->
            //点击更新
            val uri = Uri.parse("https://www.baidu.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
        .setNegativeButton("取消") { dialog, which ->
            //点击取消
        }
        .create()
    dialog.show()













}