package com.yongle.aslua.ui.reflow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReflowViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "开发中"
    }

    val text: LiveData<String> = _text
}