package com.yongle.aslua.ui.slideshow.guanyu

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityKaiyuanxukeBinding

class Kaiyuanxuke : AppCompatActivity() {

        // 声明变量
        private lateinit var binding: ActivityKaiyuanxukeBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // 设置返回按钮
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // 设置标题
            title = getString(R.string.kaiyuanxiangmu)

            // 从 ActivityMainBinding 中获取布局文件的根视图
            binding = ActivityKaiyuanxukeBinding.inflate(layoutInflater)

            // 设置布局
            setContentView(binding.root)

            // 导航栏透明
            window.navigationBarColor = Color.TRANSPARENT


            // 设置



    }


    // 设置返回按钮的点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}