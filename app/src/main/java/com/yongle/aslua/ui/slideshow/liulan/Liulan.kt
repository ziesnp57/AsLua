package com.yongle.aslua.ui.slideshow.liulan


import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.databinding.ActivityLiulanBinding
import com.yongle.aslua.ui.slideshow.liulan.ui.main.SectionsPagerAdapter
import okhttp3.Headers


class Liulan : AppCompatActivity() {

    private lateinit var binding: ActivityLiulanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.liulanjilu)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityLiulanBinding.inflate(layoutInflater)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.liulan, menu)
        val clearItem = menu.findItem(R.id.action_clear)
        clearItem.setOnMenuItemClickListener {
            // 处理清空操作
            MaterialAlertDialogBuilder(this, R.style.MyAlertDialogStyle)
                .setTitle("提示")
                .setMessage("确定要清空浏览记录吗？")
                .setPositiveButton("确定") { _, _ ->
                    val uid = intent.extras?.getString("uid")

                    HttpClient().okhttp(
                        GetApi.SEARCH_HOT_DETAOU + "?uid=$uid&id=1", null, null,
                        object : HttpClient.HttpCallback {

                            // 处理响应结果
                            override fun onSuccess(code: Int, body: String?, headers: Headers) {
                                if (body != "0") {

                                    // 清空数据
                                    val recyclerView =
                                        findViewById<RecyclerView>(R.id.recyclerview_liulan)
                                    recyclerView.post {
                                        recyclerView.adapter = null
                                    }
                                    Snackbar.make(
                                        binding.root,
                                        "清空成功",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                }
                .setNegativeButton("取消") { _, _ ->
                }
                .show()
            true
        }
        return true
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