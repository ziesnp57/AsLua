package com.yongle.aslua.ui.slideshow.guanyu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityFankuiBinding


class Fankui : AppCompatActivity() {
    // 声明变量
    private lateinit var binding: ActivityFankuiBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.fankuiwenti)


        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityFankuiBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 设置webview
        val  webView = binding.webview

        // 设置支持js
        webView.settings.javaScriptEnabled = true

        // 开启 DOM storage API 功能
        webView.settings.domStorageEnabled = true

        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        // 设置webview的客户端
        val webViewClient: WebViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }
        webView.webViewClient = webViewClient

        webView.loadUrl("https://support.qq.com/product/593222")

    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        // 判断是否可以返回上一页
                if (binding.webview.canGoBack()) {
                    binding.webview.goBack()
                   if (!binding.webview.canGoBack()) {
                       finish()
                   }
                }
        return true
    }
    // 设置返回按钮的点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}