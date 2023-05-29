package com.yongle.aslua.ui.slideshowl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SlideshowlViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "市场 开发中"
    }
    val text: LiveData<String> = _text
}