package com.yongle.aslua.ui.aeiun

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aslua.LuaActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.yongle.aslua.MainActivity.Companion.Db
import com.yongle.aslua.MainActivity.Companion.sdDir
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.data.Datalists
import com.yongle.aslua.databinding.ActivityFdhfBinding
import com.yongle.aslua.login.UserLogin
import com.yongle.aslua.room.datacaches
import com.yongle.aslua.ui.aeiun.utils.CrashHandler
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EditorKeyEvent
import io.github.rosemoe.sora.event.KeyBindingEvent
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.event.SideIconClickEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.text.LineSeparator
import io.github.rosemoe.sora.widget.EditorSearcher
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.getComponent
import io.github.rosemoe.sora.widget.subscribeEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.zip.ZipFile


class Code : AppCompatActivity() {

    // 声明变量
    private lateinit var binding: ActivityFdhfBinding

    // 声明 MMKV
    private val kv = MMKV.defaultMMKV()

    private lateinit var searchMenu: PopupMenu
    private var searchOptions = EditorSearcher.SearchOptions(false, false)

    // 声明撤销和重做菜单项
    private var undo: MenuItem? = null
    private var redo: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置崩溃处理器
        CrashHandler.INSTANCE.init(this)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = intent.extras?.getString("name")

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityFdhfBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        binding.textView.text = intent.extras?.getString("username")

        //设置头像
        Glide.with(this).load(intent.extras?.getString("picture")).into(binding.imageView)

        binding.textView3.text = intent.extras?.getString("time")

        val id = intent.extras?.getString("id")

        val header = mapOf(
            "If-None-Match" to kv.decodeString("dataetag$id").toString() // 添加 ETag 请求头
        )

        val uis =
            Gson().fromJson(kv.decodeString("user_login"), UserLogin::class.java)?.uid.toString()

        val uid = if (uis != "null") "&uid=$uis" else ""

        HttpClient().okhttp(
            GetApi.SEARCH_HOT_DETAIL + "?data_id=$id$uid", null, header,
            object : HttpClient.HttpCallback {

                // 处理响应结果
                override fun onSuccess(code: Int, body: String?, headers: Headers) {

                    if (code == 200) {
                        // 获取 ETag 响应头
                        val etag = headers["ETag"]
                        // 保存 ETag 响应头
                        if (etag != null) {
                            val editor = kv.edit()
                            editor.remove("dataetag$id")
                            editor.putString("dataetag$id", etag)
                            editor.apply()
                        }
                        val data = Gson().fromJson(body, Datalists::class.java).data

                        runOnUiThread {
                            binding.codeEditor.setText(data) // 在 UI 线程中设置编辑器的文本
                            updatePositionText() // 更新光标位置文本
                            updateBtnState() // 更新按钮状态
                        }
                        //更新本地缓存
                        datacaches(id!!, data)
                    }

                    if (code == 304) {
                        val data = Db.instance.networkDataDao().getContentType(id!!)

                        runOnUiThread {
                            binding.codeEditor.setText(data) // 在 UI 线程中设置编辑器的文本
                            updatePositionText() // 更新光标位置文本
                            updateBtnState() // 更新按钮状态

                        }
                    }
                }
            })


        // 绑定输入框与编辑器
        val inputView = binding.symbolInputView
        inputView.bindEditor(binding.codeEditor)

        // 添加常用符号
        inputView.addSymbols(
            arrayOf(
                "(",
                ")",
                "[",
                "]",
                "{",
                "}",
                "\"",
                "=",
                ":",
                ".",
                ",",
                ":",
                "_",
                "+",
                "-",
                "*",
                "/",
                "\\",
                "%",
                "#",
                "^",
                "$",
                "?",
                "&",
                "|",
                "<",
                ">",
                "~",
                "'"
            ),
            arrayOf(
                "(",
                ")",
                "[",
                "]",
                "{}",
                "}",
                "\"",
                "=",
                ":",
                ".",
                ",",
                ":",
                "_",
                "+",
                "-",
                "*",
                "/",
                "\\",
                "%",
                "#",
                "^",
                "$",
                "?",
                "&",
                "|",
                "<",
                ">",
                "~",
                "'"
            )
        )


        // 加载默认主题和语言
        loadDefaultThemes()
        loadDefaultLanguages()

        // 确定 TextMate 主题
        ensureTextmateTheme()


        // 设置编辑器语言为 Lua
        val editor = binding.codeEditor
        val language = TextMateLanguage.create("source.lua", true)
       editor.setEditorLanguage(language)
        // editor.setEditorLanguage(LuaLanguage())
        // 设置滑行行号字体大小
        editor.lineInfoTextSize = 38F


        // 更新编辑器状态
        updatePositionText()
        updateBtnState()

        // 根据当前主题自动切换深色/浅色模式
        switchThemeIfRequired(this, binding.codeEditor)



        binding.button2.setOnClickListener {


            // 低部弹窗
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
            // 设置点击外部是否可以取消
            bottomSheetDialog.setCanceledOnTouchOutside(true)


            bottomSheetDialog.show()


        }


        // 编辑器设置
        binding.codeEditor.apply {
            setLineSpacing(2f, 1.1f)

            //  nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR or CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION

            // 动态更新
            subscribeEvent<SelectionChangeEvent> { _, _ -> updatePositionText() }
            subscribeEvent<PublishSearchResultEvent> { _, _ -> updatePositionText() }
            subscribeEvent<ContentChangeEvent> { _, _ ->
                postDelayedInLifecycle(
                    ::updateBtnState,
                    50
                )
            }
            subscribeEvent<SideIconClickEvent> { _, _ ->
                Toast.makeText(this@Code, "单击侧图标", Toast.LENGTH_SHORT).show()
            }

            subscribeEvent<KeyBindingEvent> { event, _ ->
                if (event.eventType != EditorKeyEvent.Type.DOWN) {
                    return@subscribeEvent
                }

            }

            getComponent<EditorAutoCompletion>()
                .setEnabledAnimation(true)
        }

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

    // 打开资源文件
    @OptIn(DelicateCoroutinesApi::class)
    private fun openAssetsFile(name: String) {

        GlobalScope.launch(Dispatchers.IO) {
            val file = File(name)
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val stringBuilder = StringBuilder()
                var text: String?
                while (run {
                        text = bufferedReader.readLine()
                        text
                    } != null) {
                    stringBuilder.append(text)
                    stringBuilder.append("\n") // 手动添加换行符
                }
                inputStream.close()

                binding.codeEditor.setText(stringBuilder.toString(), null) // 在 UI 线程中设置编辑器的文本
                updatePositionText() // 更新光标位置文本
                updateBtnState() // 更新按钮状态
            }
        }

    }

    // 更新按钮状态
    private fun updateBtnState() {
        undo?.isEnabled = binding.codeEditor.canUndo() // 撤销按钮是否可用
        redo?.isEnabled = binding.codeEditor.canRedo() // 重做按钮是否可用
    }

    // 更新光标位置文本
    private fun updatePositionText() {
        val cursor = binding.codeEditor.cursor // 获取编辑器的光标
        var text =
            (1 + cursor.leftLine).toString() + ":" + cursor.leftColumn + ";" + cursor.left + " " // 添加行列数和字符位置
        text += if (cursor.isSelected) { // 如果有选中文本
            "(" + (cursor.right - cursor.left) + " chars)" // 添加选中字符数
        } else {
            val content = binding.codeEditor.text
            if (content.getColumnCount(cursor.leftLine) == cursor.leftColumn) { // 如果在行末
                "(<" + content.getLine(cursor.leftLine).lineSeparator.let {
                    if (it == LineSeparator.NONE) {
                        "EOF"
                    } else {
                        it.name
                    }
                } + ">)" // 添加行尾标识符
            } else {
                val char = binding.codeEditor.text.charAt(
                    cursor.leftLine,
                    cursor.leftColumn
                ) // 获取当前字符
                if (char.isLowSurrogate() && cursor.leftColumn > 0) { // 如果当前字符是低代理项且前面有字符
                    "(" + String(
                        charArrayOf(
                            binding.codeEditor.text.charAt(
                                cursor.leftLine,
                                cursor.leftColumn - 1
                            ), char
                        )
                    ) + ")" // 添加高低代理项对
                } else if (char.isHighSurrogate() && cursor.leftColumn + 1 < binding.codeEditor.text.getColumnCount(
                        cursor.leftLine
                    )
                ) { // 如果当前字符是高代理项且后面有字符
                    "(" + String(
                        charArrayOf(
                            char, binding.codeEditor.text.charAt(
                                cursor.leftLine,
                                cursor.leftColumn + 1
                            )
                        )
                    ) + ")" // 添加高低代理项对
                } else {

                    "(" + escapeIfNecessary(
                        binding.codeEditor.text.charAt(
                            cursor.leftLine,
                            cursor.leftColumn
                        )
                    ) + ")"


                }
            }
        }
        val searcher = binding.codeEditor.searcher
        if (searcher.hasQuery()) {
            val idx = searcher.currentMatchedPositionIndex
            val matchText = when (val count = searcher.matchedPositionCount) {
                0 -> {
                    "no match"
                }

                1 -> {
                    "1 match"
                }

                else -> {
                    "$count matches"
                }
            }
            text += if (idx == -1) {
                "($matchText)"
            } else {
                "(${idx + 1}/$matchText)"
            }
        }
        binding.positionDisplay.text = text
    }

    private fun escapeIfNecessary(c: Char): String {
        return when (c) {
            '\n' -> "\\n"
            '\t' -> "\\t"
            '\r' -> "\\r"
            ' ' -> "<ws>"
            else -> c.toString()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        switchThemeIfRequired(this, binding.codeEditor)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        undo = menu.findItem(R.id.text_undo)
        redo = menu.findItem(R.id.text_redo)
        return super.onCreateOptionsMenu(menu)
    }


    fun gotoNext() {
        try {
            binding.codeEditor.searcher.gotoNext()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun gotoLast() {
        try {
            binding.codeEditor.searcher.gotoPrevious()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun replace() {
        try {
            //   binding.codeEditor.searcher.replaceThis(binding.replaceEditor.text.toString())
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun replaceAll() {
        try {
            // binding.codeEditor.searcher.replaceAll(binding.replaceEditor.text.toString())
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun showSearchOptions() {
        searchMenu.show()
    }

    /**
     * 从 assets 中拷贝文件到 externalCacheDir 中
     */
    private suspend fun unAssets() = withContext(Dispatchers.IO) {
        //externalCacheDir?.deleteRecursively()
        val zipFile = ZipFile(packageResourcePath)
        val zipEntries = zipFile.entries()
        while (zipEntries.hasMoreElements()) {
            val zipEntry = zipEntries.nextElement()
            val fileName = zipEntry.name
            if (fileName.startsWith("assets/testProject/")) {
                val inputStream = zipFile.getInputStream(zipEntry)
                // 使用外部缓存目录，避免 Android 10 以上无法访问 assets 目录
                val filePath = externalCacheDir?.resolve(fileName.substring("assets/".length))
                filePath?.parentFile?.mkdirs()
                val outputStream = FileOutputStream(filePath)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }
        }
        zipFile.close()
    }


    /**
     * 连接到语言服务器，并创建 LSP 编辑器
     */
    /* private suspend fun connectToLanguageServer() = withContext(Dispatchers.IO) {

         val port = randomPort() //获取随机端口

         val projectPath = externalCacheDir?.resolve("testProject")?.absolutePath ?: ""

         // 启动 LSP 服务器
         startService(
             Intent(this@Code, LspLanguageServerService::class.java)
                 .apply {
                     putExtra("port", port)
                 }
         )

         // 创建自定义 LSP 服务器定义
         val serverDefinition =
             object : CustomLanguageServerDefinition(".lua",
                 { SocketStreamConnectionProvider { port } }
             ) {

             }

         withContext(Dispatchers.Main) {

             // 创建 LSP 编辑器
             lspEditor = LspEditorManager
                 .getOrCreateEditorManager(projectPath)
                 .createEditor(
                     URIUtils.fileToURI("$projectPath/sample.lua").toString(),
                     serverDefinition
                 )
             val wrapperLanguage = createTextMateLanguage()
             lspEditor.setWrapperLanguage(wrapperLanguage)
             lspEditor.editor = binding.codeEditor
         }

         lifecycleScope.launch(Dispatchers.Main) {
             try {
                 withContext(Dispatchers.IO) {
                     lspEditor.connectWithTimeout() // 连接到 LSP 服务器
                     lspEditor.requestManager?.didChangeWorkspaceFolders(
                         DidChangeWorkspaceFoldersParams().apply {
                             this.event = WorkspaceFoldersChangeEvent().apply {
                                 added = listOf(WorkspaceFolder("file://$projectPath/std/Lua53"))
                             }
                         }
                     )
                 }


                 toast("连接语言服务器") // 显示 Toast
             } catch (e: Exception) {
                 toast("无法连接语言服务器") // 显示 Toast
                 e.printStackTrace()
             }
         }
     }*/

    override fun onDestroy() {
        super.onDestroy()
        binding.codeEditor.release() // 释放编辑器资源
        finish()
    }

    // 设置返回按钮的点击事件
    @OptIn(DelicateCoroutinesApi::class)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                binding.codeEditor.release() // 释放编辑器资源
                finish()
                return true
            }

            R.id.text_undo -> binding.codeEditor.undo()
            R.id.text_redo -> binding.codeEditor.redo()
            R.id.text_play -> {
                // 跳转页面


                val luaPath = "$sdDir/AsLua/run.lua"

                // 在 IO 线程中执行文件操作
                GlobalScope.launch(Dispatchers.IO) {
                    File(luaPath).writeText(binding.codeEditor.text.toString())
                }


                val intent = Intent(this, LuaActivity::class.java)
                intent.putExtra("luaPath", luaPath)
                intent.putExtra("name", getString(R.string.daimatiaoshi))
                intent.putExtra("checkUpdate", true)
                intent.putExtra("fileUri", getIntent().data)
                intent.setData(Uri.parse("file://$luaPath?documentId=0"))
                startActivity(intent)

            }

        }
        return super.onOptionsItemSelected(item)
    }
}



