package com.yongle.aslua.login

import android.widget.Toast
import com.yongle.aslua.MainActivity

//对于Toast的一个简单封装，也用到了扩展函数
fun String.showToast(duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(MainActivity.context,this,duration).show()
}


