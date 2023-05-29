package com.yongle.aslua.ui.transform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yongle.aslua.MainActivity
import com.yongle.aslua.room.ContentType

// 定义一个名为 TransformViewModel 的 ViewModel 类
class TransformViewModel : ViewModel() {

    // 定义一个私有的 变量
    fun getdata(): List<ContentType> {
        return MainActivity.Companion.Db.instance.contentTypeDao().getAll()
    }

    // 定义一个私有的 MutableLiveData 变量 _texts，并初始化为一个包含 16 个字符串的列表
    private val _texts = MutableLiveData<List<String>>().apply {
        value = (1..16).mapIndexed { _, i ->
            "# $i"
        }
    }

    // 定义一个公有的 LiveData 变量 texts，其值为 _texts 变量
    val texts: LiveData<List<String>> = _texts


    // 定义一个公有的 tabTm 变量
    var selectedTab: Int = 0

    // 定义一个公有的 tabTd 变量
    var selectedTabs: Int = 0

}


