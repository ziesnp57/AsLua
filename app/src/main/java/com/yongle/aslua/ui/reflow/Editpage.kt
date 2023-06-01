package com.yongle.aslua.ui.reflow


import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.tabs.TabLayout
import com.yongle.aslua.R
import com.yongle.aslua.databinding.ActivityEditpageBinding
import com.yongle.aslua.ui.aeiun.switchThemeIfRequired
import com.yongle.aslua.ui.aeiun.utils.CrashHandler
import io.github.rosemoe.sora.widget.EditorSearcher


class Editpage : AppCompatActivity() {

    private lateinit var binding: ActivityEditpageBinding

    private lateinit var searchMenu: PopupMenu
    private var searchOptions = EditorSearcher.SearchOptions(false, false)

    // 声明撤销和重做菜单项
    private var undo: MenuItem? = null
    private var redo: MenuItem? = null


    private var screenWidth: Int = 0
    private var menuWidth: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置崩溃处理器
        CrashHandler.INSTANCE.init(this)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = intent.extras?.getString("name")

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityEditpageBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)


        val tabs: TabLayout = binding.tabs

        tabs.addTab(tabs.newTab().setText("main.lua"))

        //设置tabTm
        val tabsd: TabLayout = binding.tabsd
        tabsd.addTab(tabsd.newTab().setText("格式化"))
        tabsd.addTab(tabsd.newTab().setText("打包"))
        tabsd.addTab(tabsd.newTab().setText("备份"))
        tabsd.addTab(tabsd.newTab().setText("属性"))
        tabsd.addTab(tabsd.newTab().setText("GPT"))
        tabsd.addTab(tabsd.newTab().setText("搜索"))
        tabsd.addTab(tabsd.newTab().setText("日志"))
        tabsd.addTab(tabsd.newTab().setText("导入分析"))
        tabsd.addTab(tabsd.newTab().setText("素材仓库"))
        tabsd.addTab(tabsd.newTab().setText("配色参考"))


        //tabsd.setTabTextColors(Color.parseColor("#000000"), textColor)
        // 添加选项卡的选择监听器
        tabsd.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                when (tab.position) {
                    0 -> {
                        binding.codeEditor.setText("格式化")
                    }

                    1 -> {
                        //binding.codeEditor.setText("打包")
                    }

                    2 -> {
                        binding.codeEditor.setText("备份")
                    }

                    3 -> {
                        //binding.codeEditor.setText("属性")
                    }

                    4 -> {
                        //binding.codeEditor.setText("GPT")
                    }

                    5 -> {
                        //binding.codeEditor.setText("搜索")
                    }

                    6 -> {
                        //binding.codeEditor.setText("日志")
                    }

                    7 -> {
                        //binding.codeEditor.setText("导入分析")
                    }

                    8 -> {
                        //binding.codeEditor.setText("素材仓库")
                    }

                    9 -> {
                        //binding.codeEditor.setText("配色参考")
                    }

                }


            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        val drawerLayout = binding.drawerLayout
        val drawer = binding.drawer


        fun width(): Int {
            //始终获取屏幕宽度。不管横屏
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val heightPixels = Resources.getSystem().displayMetrics.heightPixels

            return if (screenWidth > heightPixels) {
                heightPixels
            } else {
                screenWidth
            }
        }

        //设置drawer的宽度始终为屏幕宽度的80%
        drawer.layoutParams.width = (width() * 0.8).toInt()



        @Suppress("DEPRECATION")
        drawerLayout.setDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val scaleFactor = 1 - (1 - slideOffset) * 0.3f
                val slideX = drawerView.width * slideOffset


            }
        })

        val editor = binding.codeEditor

        // 绑定输入框与编辑器
        val inputView = binding.symbolInputView
        inputView.bindEditor(editor)

        // 添加常用符号
        inputView.addSymbols(
            arrayOf(
                "(", ")", "[",
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


        /*     // 加载默认主题和语言
             loadDefaultThemes()
             loadDefaultLanguages()

             // 确定 TextMate 主题
             ensureTextmateTheme()*/


        // 设置编辑器语言为 Lua
        // val language = TextMateLanguage.create("source.lua", true)
        // editor.setEditorLanguage(language)
        // editor.setEditorLanguage(LuaLanguage())
        // 设置滑行行号字体大小
        editor.lineInfoTextSize = 38F


        /*       // 更新编辑器状态
               updatePositionText()
               updateBtnState()*/

        // 根据当前主题自动切换深色/浅色模式
        switchThemeIfRequired(this, editor)


    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val drawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // 退出时保存文件
            // saveFile()
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        undo = menu.findItem(R.id.text_undo)
        redo = menu.findItem(R.id.text_redo)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_dehaze_24)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            android.R.id.home -> {
                val drawerLayout = binding.drawerLayout
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}