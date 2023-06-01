package com.yongle.aslua.ui.slideshow.guanyu

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityYinsizhengceBinding

class Yinsizhengce : AppCompatActivity() {

    // 声明变量
    private var binding: ActivityYinsizhengceBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.yinsizhengce)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityYinsizhengceBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding?.root)

        // 设置webview
        val  webView = binding?.webview

        val html = """
<html>
<body>
<p><strong>生效日期：2023-05-08</strong></p>

<p>欢迎使用 AsLua 应用（以下简称“本应用”）。本应用尊重并保护用户的隐私权。本隐私政策（以下简称“本政策”）将说明本应用收集、使用、保护、存储、共享和处理用户信息的方式。</p>

<h3>用户信息的收集</h3>

<p>本应用仅会收集与应用相关的信息，以提供更好的服务和体验。本应用收集的用户信息主要包括：</p>

<p>1. 设备信息：包括设备型号、操作系统版本、唯一设备标识符（如IMEI等）、MAC地址、IP地址等。</p>

<p>2. 日志信息：包括使用本应用的操作记录、崩溃日志、错误日志等信息。</p>

<p>3.位置信息：本应用不会收集、存储用户的地理位置信息。</p>

<h3>用户信息的使用</h3>

<p>本应用收集的用户信息仅用于以下用途：</p>

<p>1. 向用户提供更好的服务和体验；</p>

<p>2. 解决软件程序错误和程序性能问题。</p>

<h3>用户信息的保护</h3>

<p>本应用采取严格的信息安全措施保护用户信息，包括但不限于加密、访问控制等手段，防止用户信息被非法获取、泄露、篡改或者损毁。</p>

<h3>用户信息的存储</h3>

<p>本应用会采取必要措施，将用户信息存储在安全可靠的服务器中，确保用户信息的安全性和机密性。</p>

<h3>用户信息的共享和处理</h3>

<p>本应用承诺不会向任何第三方出售、出租、交换或者分享用户信息，除非得到用户明确的同意或法律法规规定。</p>

<h3>未成年人保护</h3>

<p>本应用非常重视未成年人的个人信息保护。如用户未满18周岁，请在监护人的指导下使用本应用。</p>

<h3>隐私政策的更新</h3>

<p>本政策的条款可能根据实际情况进行更新，我们鼓励用户在使用本应用时查阅本政策以及相关条款的最新版本。</p>

<h3>联系我们</h3>

<p>如您对本应用的隐私政策有任何疑问、意见或建议，或者您需要修改、删除或访问您的个人信息，请通过以下方式联系我们：</p>

<p>邮箱：2063809513@qq.com</p>

</body>
</html>
    """
        webView?.loadData(html, "text/html", "UTF-8")

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