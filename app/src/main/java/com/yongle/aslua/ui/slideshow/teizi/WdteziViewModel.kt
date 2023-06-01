package com.yongle.aslua.ui.slideshow.teizi

import androidx.lifecycle.ViewModel

class WdteziViewModel : ViewModel() {

    // 定义一个公有的 tabs 变量
    var selectedTabs: Int = 0

    // 定义一个公有的 url 变量
    var urls: String = ""

    // 定义一个公有的 start 变量
    var start: Int = 1

    // 定义一个公有的 isLoading 变量
    var isLoading: Boolean = false

}