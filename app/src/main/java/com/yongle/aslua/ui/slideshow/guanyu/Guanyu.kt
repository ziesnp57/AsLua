package com.yongle.aslua.ui.slideshow.guanyu

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yongle.aslua.R
import com.yongle.aslua.data.ListItem
import com.yongle.aslua.databinding.ActivityGuanyuBinding

class Guanyu : AppCompatActivity() {

    // 声明变量
    private var binding: ActivityGuanyuBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.guanyu)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityGuanyuBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding!!.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT


        // 设置 用户协议 点击事件
        binding!!.yonghuxieyi.setOnClickListener {
            startActivity(Intent(this, Yonghuxieyi::class.java))
        }

        // 设置 隐私政策 点击事件
        binding!!.yinsizhengce.setOnClickListener {
            startActivity(Intent(this, Yinsizhengce::class.java))
        }


        // 合并图片资源和文本数据
        val listData = listOf(
            ListItem(R.drawable.jianchagengxin, getString(R.string.jianchagengxin)),
            ListItem(R.drawable.fankuiwenti, getString(R.string.fankuiwenti)),
            ListItem(R.drawable.guanyuzuozhe, getString(R.string.guanyuzuozhe)),
            ListItem(R.drawable.kaiyuanxiangmu, getString(R.string.kaiyuanxiangmu)),
        )

        // 自定义 Adapter，同时显示图片和文本
        class CustomAdapter(context: Context, private val resourceId: Int, private val data: List<ListItem>) :
            ArrayAdapter<ListItem>(context, resourceId, data) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(resourceId, parent, false)
                val imageView = view.findViewById<ImageView>(R.id.imageView5)
                val textView = view.findViewById<TextView>(R.id.textView2)

                // 设置图片和文本
                val item = data[position]
                imageView.setImageResource(item.imageId)
                textView.text = item.text

                return view
            }
        }

// 设置 listView 的 adapter 和点击事件
        binding!!.listView.adapter = CustomAdapter(this, R.layout.item_slideshow, listData)
        binding!!.listView.setOnItemClickListener { _, _, position, _ ->

            // 点击事件
            when (position) {
                0 -> {
                    // 检查更新

                }
                1 -> {
                    // 反馈问题
                    startActivity(Intent(this, Fankui::class.java))
                }
                2 -> {
                    // 关于作者
                    startActivity(Intent(this, Guangyuzuozhe::class.java))
                }
                3 -> {
                    // 开源项目
                    startActivity(Intent(this, Kaiyuanxuke::class.java))
                }
            }


        }

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