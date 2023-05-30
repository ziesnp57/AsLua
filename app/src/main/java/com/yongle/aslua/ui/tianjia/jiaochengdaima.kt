package com.yongle.aslua.ui.tianjia

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.yongle.aslua.MainActivity
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.data.ResponseDatas
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
import org.eclipse.tm4e.core.registry.IThemeSource

class Jiaochengdaima : AppCompatActivity() {

    // 声明变量
    private lateinit var binding: ActivityJiaochengdaimaBinding

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
                tds.getTabAt(viewModel.selectedTab)?.select()
            }
        }


        // 添加分类选项卡的选择监听器
        tds.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 记录选中的tab
                viewModel.selectedTab = tab.position

                // 选项卡被选中时的回调
                println(tab.text.toString())
                println(tab.tag)
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

        // 获取intent传递的数据
        val id = intent.extras!!.getString("qqLogin")

        binding.jctext4.setOnClickListener {
            // 获取
            val nam = binding.textt.text.toString()

            //获取当前选中
            val tab = binding.tabTds.getTabAt(binding.tabTds.selectedTabPosition)?.tag

            //获取前两行内容
            val text = binding.codeEditor.text.toString().split("\n").take(3).joinToString("\n")

            // 获取编辑器内容
            val decode = binding.codeEditor.text.toString()

            if ( nam == "" || text == "" || tab == null) {
                Snackbar.make(it, "请填写完整", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                return@setOnClickListener
            }

            // 发送 POST 请求示例
                HttpClient().post(GetApi.SEARCH_HOT_DETAIL, "user_id=$id&type_id=$tab&datalist_name=$nam&datalist_data=$text&data=$decode",
                    object : HttpClient.HttpCallback {

                    // 处理响应结果
                    override fun onSuccess(response: String) {
                        if (MainActivity.Companion.GsonFactory.instance.fromJson(response, ResponseDatas::class.java).code == 200) {

                            println("发布成功")
                            // 跳转到主页
                            finish()
                        } else {
                            Snackbar.make(it, "内容已存在", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show()
                        }
                    }

                    override fun onFailure(message: String?) {
                        Log.e("TAG", message!!)
                    }
                })


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