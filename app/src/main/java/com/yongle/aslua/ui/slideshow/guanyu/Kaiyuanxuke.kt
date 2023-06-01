package com.yongle.aslua.ui.slideshow.guanyu

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yongle.aslua.R
import com.yongle.aslua.data.ListItems
import com.yongle.aslua.databinding.ActivityKaiyuanxukeBinding

class Kaiyuanxuke : AppCompatActivity() {

        // 声明变量
        private var binding: ActivityKaiyuanxukeBinding? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // 设置返回按钮
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // 设置标题
            title = getString(R.string.kaiyuanxiangmu)

            // 从 ActivityMainBinding 中获取布局文件的根视图
            binding = ActivityKaiyuanxukeBinding.inflate(layoutInflater)

            // 设置布局
            setContentView(binding?.root)

            // 导航栏透明
            window.navigationBarColor = Color.TRANSPARENT


            // 合并数据
            val listDatad = listOf(
                ListItems("Android Jetpack", "Jetpack is a suite of libraries to help developers follow best practices, reduce boilerplate code, and write code that works consistently across Android versions and devices so that developers can focus on the code they care about", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://developer.android.com/jetpack"),
                ListItems("AndroidX", "AndroidX is the open-source project that the Android team uses to develop, test, package, version and release libraries within Jetpack", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://developer.android.com/jetpack/androidx"),
                ListItems("Room", "The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://developer.android.com/jetpack/androidx/releases/room"),
                ListItems("Material Design Icons", "Material Design Icons' growing icon collection allows designers and developers targeting various platforms to download icons in the format, color and size they need for any project", "SIL Open Font License 1.1", "https://pictogrammers.com/docs/general/license/", "https://materialdesignicons.com/"),
                ListItems("AndroLua_pro", "Lua and LuaJava ported to Android", "License", "https://github.com/nirenr/AndroLua_pro/blob/master/LICENSE.txt", "https://github.com/nirenr/AndroLua_pro"),
                ListItems("Glide", "An image loading and caching library for Android focused on smooth scrolling", "License", "https://bumptech.github.io/glide/dev/open-source-licenses.html", "https://github.com/bumptech/glide"),
                ListItems("OkHttp", "An HTTP & HTTP/2 client for Android and Java applications", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://github.com/square/okhttp"),
                ListItems("Gson", "A Java serialization/deserialization library to convert Java Objects into JSON and back", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://github.com/Google/Gson"),
                ListItems("Sora-editor", "A simple and fast markdown editor for Android", "LGPL 2.1 License", "https://github.com/Rosemoe/sora-editor/blob/main/LICENSE", "https://github.com/Rosemoe/sora-editor"),
                ListItems("MMKV", "An efficient, small mobile key-value storage framework developed by WeChat", "License", "https://github.com/Tencent/MMKV/blob/master/LICENSE.TXT", "https://github.com/Tencent/MMKV"),
                ListItems("JsoupXpath", "JsoupXpath is an extension of Jsoup, which can use XPath to parse HTML", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://github.com/zhegexiaohuozi/JsoupXpath"),
                ListItems("Circleimageview", "A circular ImageView for Android", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://github.com/hdodenhof/CircleImageView"),
                ListItems("TinyPinyin", "A Chinese pinyin search library", "Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0", "https://github.com/promeG/TinyPinyin"),
                ListItems("Luajava", "LuaJava is a scripting tool for Java. It is based on Lua 5.4.4 (the same version used by PUC-Rio), but most of the library functions have been reimplemented in Java", "MIT License", "", ""),
                ListItems("Lua", "Lua is a powerful, efficient, lightweight, embeddable scripting language. It supports procedural programming, object-oriented programming, functional programming, data-driven programming, and data description", "MIT License", "", ""),
                ListItems("laqlite3","laqlite3 is a Lua binding to SQLite3", "MIT License", "", ""),
                ListItems("cjson", "cjson is a fast JSON encoding/parsing module for Lua", "MIT License", "", ""),
                ListItems("bson", "bson is a Lua binding to libbson", "MIT License", "", ""),
                ListItems("luasocket", "luasocket is a Lua binding to the socket library", "MIT License", "", ""),
                )

            // 自定义 Adapter，同时显示图片和文本
            class CustomAdapters(context: Context, private val resourceId: Int, private val data: List<ListItems>) :
                ArrayAdapter<ListItems>(context, resourceId, data) {

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: LayoutInflater.from(context).inflate(resourceId, parent, false)
                    val textView = view.findViewById<TextView>(R.id.textView14)
                    val textView1 = view.findViewById<TextView>(R.id.textView15)
                    val textView2 = view.findViewById<TextView>(R.id.textView16)
                    val imageButton = view.findViewById<ImageButton>(R.id.imageButton)

                    // 设置文本
                    val item = data[position]

                    textView.text = item.text
                    textView1.text = item.text1
                    textView2.text = item.text2

                    // 设置点击事件
                    textView2.setOnClickListener {
                        // 跳转到浏览器
                        val intent = android.content.Intent()
                        intent.action = "android.intent.action.VIEW"
                        val contenturl = android.net.Uri.parse(item.text3)
                        intent.data = contenturl
                        startActivity(intent)
                    }

                    imageButton.setOnClickListener {
                        // 跳转到浏览器
                       val intent = android.content.Intent()
                        intent.action = "android.intent.action.VIEW"
                        val contenturl = android.net.Uri.parse(item.text4)
                        intent.data = contenturl
                        startActivity(intent)
                    }

                    return view
                }
            }

// 设置 listView 的 adapter 和点击事件
            binding?.listView?.adapter = CustomAdapters(this, R.layout.item_likaiy, listDatad)




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