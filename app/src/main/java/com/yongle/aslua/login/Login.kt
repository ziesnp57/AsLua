package com.yongle.aslua.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent
import com.yongle.aslua.MainActivity
import com.yongle.aslua.MainActivity.Companion.mTencent
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    // 声明变量
    private lateinit var binding: ActivityLoginBinding

    private lateinit var iu: BaseUiListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.yonghudenglu)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // 初始化
        iu = BaseUiListener(mTencent)

        // 设置布局
        setContentView(binding.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT



        // QQ登录
        binding.qqlogin.setOnClickListener {
            Tencent.resetQQAppInfoCache()
            Tencent.resetTimAppInfoCache()
            Tencent.resetTargetAppInfoCache()

            when (mTencent.login(this, "get_user_info", iu, true)) {
                -1 -> {
                    "异常".showToast()
                    mTencent.logout(MainActivity.context)
                }
            }

        }

        // 微信登录
        binding.wxlogin.setOnClickListener {

            Snackbar.make(it, "暂未开放", Snackbar.LENGTH_SHORT).show()

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        //腾讯QQ回调
        Tencent.onActivityResultData(requestCode, resultCode, data,iu)
        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
                Tencent.handleResultData(data, iu)
            }
        }
    }


    // 设置返回按钮的点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                @Suppress("DEPRECATION")
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}