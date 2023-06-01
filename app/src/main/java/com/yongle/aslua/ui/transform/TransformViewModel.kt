package com.yongle.aslua.ui.transform

import androidx.lifecycle.ViewModel
import com.yongle.aslua.data.ContentType

// 定义一个名为 TransformViewModel 的 ViewModel 类
class TransformViewModel : ViewModel() {

    // 定义一个公有的 tabTm 变量
    var selectedTab: Int = 0

    // 定义一个公有的 tabTd 变量
    var selectedTabs: Int = 0

    // 定义一个私有的 dataLists 变量
    var typeList = mutableListOf<com.yongle.aslua.room.ContentType>()

    // 定义一个私有的 dataLists 变量
    var dataLists = mutableListOf<ContentType>()

    // 定义一个公有的 url 变量
    var urls: String = ""

    // 定义一个公有的 start 变量
    var start: Int = 1

    // 定义一个公有的 isLoading 变量
    var isLoading: Boolean = false

    // 定义一个公有的 isLoaSig 变量
    var isLoaSig: Boolean = false
}


