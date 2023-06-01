package com.yongle.aslua.ui.slideshow.doc

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.databinding.ActivityDocAddBinding
import com.yongle.aslua.login.UserLogin
import com.yongle.aslua.ui.aeiun.switchThemeIfRequired
import okhttp3.Headers

class DocAdd : AppCompatActivity() {

    private lateinit var binding: ActivityDocAddBinding

    val kv = MMKV.defaultMMKV()!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.doc)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityDocAddBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT

        binding.texts.setText(intent.extras?.getString("name"))

        val url = intent.extras?.getString("url")

        // 设置编辑器语言为 Lua
        val editor = binding.codeEditor

        // 设置滑行行号字体大小
        editor.lineInfoTextSize = 38F

        // 根据当前主题自动切换深色/浅色模式
        switchThemeIfRequired(this, editor)

        if (url != null) {
            HttpClient().okhttp(
                url, null, null,
                object : HttpClient.HttpCallback {

                    // 处理响应结果
                    override fun onSuccess(code: Int, body: String?, headers: Headers) {
                        runOnUiThread {
                            binding.codeEditor.setText(body)
                        }
                    }
                })
        }


        binding.button4.setOnClickListener {
            if (url != null) {
                val id = intent.extras?.getString("id")
                val name = binding.texts.text?.trimStart().toString()
                val data = binding.codeEditor.text.toString()

                if (data == "") {
                    Snackbar.make(it, "内容不能为空", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show()
                    return@setOnClickListener
                }

                // 发送 PUT 请求示例
                HttpClient().put(GetApi.SEARCH_HOT_DOC + "/$id",
                    mapOf(
                        "name" to name,
                        "data" to data
                    ),
                    object : HttpClient.HttpCallback {

                        // 处理响应结果
                        override fun onSuccess(code: Int, body: String?, headers: Headers) {
                            if (body == "1") {
                                Snackbar.make(it, "保存成功", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null)
                                    .addCallback(object : Snackbar.Callback() {
                                        override fun onDismissed(
                                            transientBottomBar: Snackbar?,
                                            event: Int
                                        ) {
                                            finish()
                                        }
                                    })
                                    .show()
                            }
                        }
                    })

            } else {

                val name = binding.texts.text?.trimStart().toString()
                val data = binding.codeEditor.text.toString()
                if (data == "") {
                    Snackbar.make(it, "内容不能为空", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show()
                    return@setOnClickListener
                }

                // 发送 POST 请求示例
                val uid = Gson().fromJson(kv.decodeString("user_login"), UserLogin::class.java).uid
                val post = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "data" to data
                )
                HttpClient().okhttp(
                    GetApi.SEARCH_HOT_DOC, post, null,
                    object : HttpClient.HttpCallback {

                        // 处理响应结果
                        override fun onSuccess(code: Int, body: String?, headers: Headers) {
                            if (body == "1") {
                                runOnUiThread {
                                    Snackbar.make(it, "保存成功", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null)
                                        .addCallback(object : Snackbar.Callback() {
                                            override fun onDismissed(
                                                transientBottomBar: Snackbar?,
                                                event: Int
                                            ) {
                                                finish()
                                            }
                                        })
                                        .show()
                                }
                            }
                        }
                    })
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