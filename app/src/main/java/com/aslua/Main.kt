package com.aslua

import android.content.Intent
import android.os.Bundle

class Main : LuaActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 调用父类的 onCreate 方法
        super.onCreate(savedInstanceState)
        // 如果 savedInstanceState 为空并且传递了 data，那么调用 onNewIntent 方法
        if (savedInstanceState == null && intent.data != null) runFunc("onNewIntent", intent)
        // 如果传递了 isVersionChanged 参数并且 savedInstanceState 为空，那么调用 onVersionChanged 方法
        if (intent.getBooleanExtra("isVersionChanged", false) && savedInstanceState == null) {
            onVersionChanged(
                intent.getStringExtra("newVersionName"),
                intent.getStringExtra("oldVersionName")
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        // 调用父类的 onNewIntent 方法
        super.onNewIntent(intent)
        // 调用 Lua 函数 onNewIntent
        runFunc("onNewIntent", intent)
    }

    override fun getLuaDir(): String {
        // 返回本地目录，用于 Lua 脚本的存储
        return localDir
    }

    override fun getLuaPath(): String {
        // 调用 initMain 方法，用于初始化 main.lua 的内容
        initMain()
        // 返回 Lua 脚本的路径
        return "$localDir/main.lua"
    }

    /**
     * 当应用版本号发生变化时调用该方法
     * @param newVersionName 新版本号
     * @param oldVersionName 旧版本号
     */
    private fun onVersionChanged(newVersionName: String?, oldVersionName: String?) {
        // 调用 Lua 函数 onVersionChanged
        runFunc("onVersionChanged", newVersionName, oldVersionName)
    }
}
