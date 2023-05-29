package com.yongle.aslua.ui.slideshow.guanyu

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityYonghuxieyiBinding

class Yonghuxieyi : AppCompatActivity() {

    // 声明变量
    private lateinit var binding: ActivityYonghuxieyiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.yonghuxieyi)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityYonghuxieyiBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 设置webview
        val  webView = binding.webview


val html = """
<html>
<body>
<p><strong>生效日期：2023-05-08</strong></p>

<p>本协议是您与 个人开发者 歪果桃 或 AsLua应用（下称“AsLua”或“本应用”） 的用户协议。请在使用本应用之前，仔细阅读以下协议内容，特别是协议中的重点条款。当您使用本应用时，即表示您同意接受以下协议中的条款。</p>

<h3>服务条款</h3>

<p>1. AsLua应用是一款提供Lua语言脚本编辑、运行、调试的应用，具有广泛的应用场景。</p>

<p>2. 本应用由开发者 歪果桃 提供，用户通过下载、安装、使用本应用即视为同意本协议。</p>

<p>3. 用户不得利用本应用从事违反法律法规、社会公德及侵犯他人合法权益的行为，如编写传播淫秽、赌博、暴力、恐怖或教唆犯罪的脚本等。</p>

<p>4. 用户不得利用本应用从事侵犯他人知识产权或其他合法权益的行为，如抄袭、盗用、篡改他人脚本等。</p>

<p>5. 用户不得利用本应用编写违反当地法律法规的代码</p>

<p>6. 如用户违反本协议，开发者有权停止提供服务，并保留追究用户法律责任的权利。</p>

<h3>隐私保护</h3>

<p>1. 本应用将严格保护用户的隐私信息，不会将用户的个人信息提供给第三方，除非得到用户明确授权或法律法规要求。</p>

<p>2. 用户使用本应用时，会涉及到用户的设备信息、应用使用记录等信息。这些信息将用于改进本应用的服务质量，但不会被用于其他用途。</p>

<p>3. 为了保护用户隐私，用户不应将个人敏感信息（如账号密码、银行卡号等）保存在本应用中。</p>

<h3>免责声明</h3>

<p>1. 用户在使用本应用时，应自行承担风险，开发者不对因使用本应用而导致的任何直接或间接损失承担责任。</p>

<p>2. 本应用的运行需要依赖于用户的设备和网络环境，如因用户设备或网络环境不稳定等原因导致本应用无法正常运行，开发者不承担责任。</p>

<p>3. 用户在使用本应用时，应遵守法律法规、社会公德，开发者不对用户的行为负责。</p>

<h3>其他条款</h3>

<p>1. 本协议的解释、效力及纠纷的解决均适用中华人民共和国法律。</p>

<p>2. 本协议所有条款的标题仅为阅读方便，本身并无实际涵义，不能作为本协议涵义解释的依据。</p>

<p>3. 本协议条款无论因何种原因部分无效或不可执行，其余条款仍有效，对双方具有约束力。</p>

<p>4. 本协议最终版权归开发者 歪果桃 所有</p>

</body>
</html>
    """
       webView.loadData(html, "text/html", "UTF-8")

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