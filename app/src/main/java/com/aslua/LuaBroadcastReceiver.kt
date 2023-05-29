package com.aslua

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// 定义 LuaBroadcastReceiver 类，实现 BroadcastReceiver 接口
class LuaBroadcastReceiver(private val mRlt: OnReceiveListener) : BroadcastReceiver() {
    // 当接收到广播时触发的回调函数
    override fun onReceive(context: Context, intent: Intent) {
        // 调用 OnReceiveListener 接口的 onReceive 方法
        mRlt.onReceive(context, intent)
    }

    // 定义 OnReceiveListener 接口
    interface OnReceiveListener {
        // 当接收到广播时的回调函数
        fun onReceive(context: Context?, intent: Intent?)
    }
}
