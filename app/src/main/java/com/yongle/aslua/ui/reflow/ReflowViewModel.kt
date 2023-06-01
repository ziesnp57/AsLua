package com.yongle.aslua.ui.reflow

import androidx.lifecycle.ViewModel
import com.yongle.aslua.data.AppList

class ReflowViewModel : ViewModel() {

    // 定义一个私有的 dataLists 变量
    val appList = mutableListOf<AppList>()

    // 定义一个公有的 isLoaSig 变量
    var isLoaSig: Boolean = false

}