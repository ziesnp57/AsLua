package com.yongle.aslua.ui.tianjia

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.yongle.aslua.MainActivity
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.data.Datalists
import com.yongle.aslua.databinding.ActivityJiaochengdaimaBinding
import com.yongle.aslua.room.ContentType
import com.yongle.aslua.ui.aeiun.switchThemeIfRequired
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.eclipse.tm4e.core.registry.IThemeSource

class Jiaochengdaima : AppCompatActivity() {

    // 声明变量
    private lateinit var binding: ActivityJiaochengdaimaBinding

    // 声明 MMKV
    private val kv = MMKV.defaultMMKV()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.title_jiaochengdaima)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityJiaochengdaimaBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 初始化 ViewModel
        val viewModel = ViewModelProvider(this)[TransformViewModel::class.java]

        // id是tds的TabLayout添加选项卡
        val tds = binding.tabTds

        // 异步IO线程 查询数据
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                MainActivity.Companion.Db.instance.contentTypeDao().getAll()
            }
            addTabs(tds, result)
            // 设置选中状态
            tds.post {
                val tab = tds.getTabAt(viewModel.selectedTab)
                tds.clearAnimation()
                tds.setScrollPosition(viewModel.selectedTab, 0f, true)
                tab?.select()
            }
        }


        // 添加分类选项卡的选择监听器
        tds.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 记录选中的tab
                viewModel.selectedTab = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        // 加载默认主题和语言
        loadDefaultThemes()
        loadDefaultLanguages()

        // 确定 TextMate 主题
        ensureTextmateTheme()


        // 设置编辑器语言为 Lua
        val editor = binding.codeEditor
        val language = TextMateLanguage.create("source.lua", true)
        editor.setEditorLanguage(language)

        // 设置滑行行号字体大小
        editor.lineInfoTextSize = 38F

        // 根据当前主题自动切换深色/浅色模式
        switchThemeIfRequired(this, binding.codeEditor)

    }

    // 加载默认的 TextMate 主题
    private fun loadDefaultThemes() {
        // 将资产文件系统提供程序添加到文件提供程序注册表中
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(
                applicationContext.assets
            )
        )

        // 加载预定义的主题列表
        val themes = arrayOf("darcula", "quietlight")
        val themeRegistry = ThemeRegistry.getInstance()
        themes.forEach { name ->
            val path = "textmate/$name.json"
            // 加载主题定义
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        // 从资产文件系统中加载主题定义
                        FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
                    ), name
                ).apply {
                    if (name != "quietlight") {
                        isDark = true
                    }
                }
            )
        }

        // 设置默认主题
        themeRegistry.setTheme("quietlight")



        binding.jctext1.setOnClickListener {

            // 清空编辑器
            binding.codeEditor.setText(null)
        }

        binding.jctext2.setOnClickListener {

            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // 检查粘贴板是否有内容
            if (clipboardManager.hasPrimaryClip()) {
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text
                    // 粘贴代码
                    binding.codeEditor.setText(text)
                }
            }
        }

        binding.jctext3.setOnClickListener {


        }

        val id = intent.extras?.getString("id")

        // 获取intent传递的数据
        val uid = intent.extras!!.getString("qqLogin").toString()

        if (id != null) {
            binding.textt.setText(intent.extras?.getString("name"))

            // 设置选中状态
            binding.tabTds.post {
                val typeid = intent.extras?.getString("typed")!!.toInt() - 1
                val tabTd = binding.tabTds

                val tab = tabTd.getTabAt(typeid)
                tabTd.clearAnimation()
                tabTd.setScrollPosition(typeid, 0f, true)
                tab?.select()
            }

            val header = mapOf(
                "If-None-Match" to kv.decodeString("dataetag$id").toString() // 添加 ETag 请求头
            )

            HttpClient().okhttp(
                GetApi.SEARCH_HOT_DETAIL + "?data_id=$id", null, header,
                object : HttpClient.HttpCallback {

                    // 处理响应结果
                    override fun onSuccess(code: Int, body: String?, headers: Headers) {

                        if (code == 200) {
                            val data = Gson().fromJson(body, Datalists::class.java).data

                            runOnUiThread {
                                binding.codeEditor.setText(data) // 在 UI 线程中设置编辑器的文本
                            }
                        }

                        if (code == 304) {
                            val data = MainActivity.Companion.Db.instance.networkDataDao()
                                .getContentType(id)

                            runOnUiThread {
                                binding.codeEditor.setText(data) // 在 UI 线程中设置编辑器的文本
                            }
                        }
                    }
                })
            binding.jctext4.setOnClickListener { it ->
                // 获取
                val nam = binding.textt.text?.trimStart().toString()

                //获取当前选中
                val tab =
                    binding.tabTds.getTabAt(binding.tabTds.selectedTabPosition)?.tag.toString()

                //获取前两行内容移除空换行
                val text = binding.codeEditor.text.trimStart().split("\n").map { it.trim() }
                    .filter { it.isNotEmpty() }.take(2).joinToString("\n")

                // 获取编辑器内容
                val texts = binding.codeEditor.text.trimStart().toString()

                if (nam == "" || texts == "" || tab == "") {
                    Snackbar.make(it, "请填写完整", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show()
                    return@setOnClickListener
                }

                // 发送 PUT 请求示例
                HttpClient().put(GetApi.SEARCH_HOT_DETAIL + "/$id",
                    mapOf(
                        "type_id" to tab,
                        "datalist_name" to nam,
                        "datalist_data" to text,
                        "data" to texts
                    ),
                    object : HttpClient.HttpCallback {

                        // 处理响应结果
                        override fun onSuccess(code: Int, body: String?, headers: Headers) {
                            if (body == "1") {
                                Snackbar.make(it, "修改成功", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null)
                                    .addCallback(object : Snackbar.Callback() {
                                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                            finish()
                                        }
                                    })
                                    .show()
                            } else {
                                Snackbar.make(it, "内容已存在", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                        }
                    })
            }

        } else {

            binding.jctext4.setOnClickListener { it ->
                // 获取
                val nam = binding.textt.text?.trimStart().toString()

                //获取当前选中
                val tab =
                    binding.tabTds.getTabAt(binding.tabTds.selectedTabPosition)?.tag.toString()

                //获取前两行内容移除空换行
                val text = binding.codeEditor.text.trimStart().split("\n").map { it.trim() }
                    .filter { it.isNotEmpty() }.take(2).joinToString("\n")

                // 获取编辑器内容
                val texts = binding.codeEditor.text.trimStart().toString()

                if (nam == "" || text == "" || tab == "") {
                    Snackbar.make(it, "请填写完整", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show()
                    return@setOnClickListener
                }

                // 发送 POST 请求示例
                HttpClient().okhttp(GetApi.SEARCH_HOT_DETAIL,
                    mapOf(
                        "user_id" to uid,
                        "type_id" to tab,
                        "datalist_name" to nam,
                        "datalist_data" to text,
                        "data" to texts
                    ),
                    null,
                    object : HttpClient.HttpCallback {

                        // 处理响应结果
                        override fun onSuccess(code: Int, body: String?, headers: Headers) {
                            if (code == 200) {
                                Snackbar.make(it, "发布成功", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null)
                                    .addCallback(object : Snackbar.Callback() {
                                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                            finish()
                                        }
                                    })
                                    .show()
                            } else {
                                Snackbar.make(it, "内容已存在", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                        }

                    })
            }

        }
    }

    // 加载默认的 TextMate 语言定义
    private fun loadDefaultLanguages() {
        // 加载语言定义列表
        GrammarRegistry.getInstance().loadGrammars(
            languages {
                language("lua") {
                    grammar = "textmate/lua/syntaxes/lua.tmLanguage.json"
                    scopeName = "source.lua"
                    languageConfiguration = "textmate/lua/language-configuration.json"
                }
            }
        )
    }

    // 确保编辑器控件使用 TextMate 主题
    private fun ensureTextmateTheme() {
        val editor = binding.codeEditor
        var editorColorScheme = editor.colorScheme
        if (editorColorScheme !is TextMateColorScheme) {
            // 创建 TextMate 颜色方案
            editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = editorColorScheme
        }
    }

    private fun addTabs(tabLayout: TabLayout, tabs: List<ContentType>) {
        for (tab in tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab.typeName).setTag(tab.typeId))
        }
    }


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