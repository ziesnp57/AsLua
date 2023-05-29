package com.yongle.aslua.ui.slideshow.liulan

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.yongle.aslua.databinding.ActivityLiulanBinding
import com.yongle.aslua.ui.slideshow.liulan.ui.main.SectionsPagerAdapter

class Liulan : AppCompatActivity() {

    private lateinit var binding: ActivityLiulanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityLiulanBinding.inflate(layoutInflater)

        // 设置 Toolbar
        setSupportActionBar(binding.toolbar)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置布局
        setContentView(binding.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT

        val sectionsPagerAdapter = SectionsPagerAdapter(
            this,
            supportFragmentManager
        )
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

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