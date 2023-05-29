package com.yongle.aslua.ui.tianjia

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityChuangjianxiangmuBinding

class Chuangjianxiangmu : AppCompatActivity() {

    // 声明变量
    private lateinit var binding: ActivityChuangjianxiangmuBinding

    private lateinit var adapter: GridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.title_chuangjianxiangmu)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityChuangjianxiangmuBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT


        // 初始化 ViewModel
        val viewModel = ViewModelProvider(this)[TransformViewModel::class.java]

        // 设置适配器
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = GridAdapter()

        recyclerView.adapter = adapter

        // 设置适配器的点击事件监听器
        adapter.setOnRadioButtonClickedListener { position ->

            viewModel.selectedTab = position

        }


        // 设置数据项
        val gridItems = listOf(
            GridItem(R.drawable.ic_uivt_view_empty, getString(R.string.cjtext1)),
            GridItem(R.drawable.ic_uivt_view_basic, getString(R.string.cjtext2)),
            GridItem(R.drawable.ic_uivt_view_custom, getString(R.string.cjtext3)),
            GridItem(R.drawable.ic_uivt_view_drawer, getString(R.string.cjtext4)),
            GridItem(R.drawable.ic_uivt_view_bottombar,getString(R.string.cjtext5)),
            GridItem(R.drawable.ic_uivt_view_tabbar, getString(R.string.cjtext6)),
        )


        gridItems[viewModel.selectedTab].isSelected = true  // 更新选中项的状态

        // 设置适配器的数据项
        adapter.setItems(gridItems)


        binding.button?.setOnClickListener {

            Snackbar.make(it, "暂未开放", Snackbar.LENGTH_SHORT).show()
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