package com.yongle.aslua


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tencent.connect.common.Constants
import com.tencent.mmkv.MMKV
import com.tencent.tauth.DefaultUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.yongle.aslua.data.init
import com.yongle.aslua.databinding.ActivityMainBinding
import com.yongle.aslua.login.BaseUiListener
import com.yongle.aslua.login.QQLogin
import com.yongle.aslua.room.AppDatabase
import com.yongle.aslua.ui.tianjia.Jiaochengdaima
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // 声明变量
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    private lateinit var  iu: BaseUiListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全局 context
        context = applicationContext

        //检查更新
       // updateDialog(this)

        MMKV.initialize(context)

        val kv = MMKV.defaultMMKV()

        // 初始化
        init()

        // 初始化QQ登录配置
        Tencent.setIsPermissionGranted(true, Build.MODEL)
        Tencent.resetTargetAppInfoCache()
        Tencent.resetQQAppInfoCache()
        Tencent.resetTimAppInfoCache()

        iu = BaseUiListener(mTencent)
        kv.decodeString("qq_login")?.let{
            val gson = Gson()
            val qqLogin = gson.fromJson(it, QQLogin::class.java)
            mTencent.setAccessToken(qqLogin.access_token,qqLogin.expires_in.toString())
            mTencent.openId = qqLogin.openid
        }

            mTencent.checkLogin(object : DefaultUiListener() {
                override fun onComplete(response: Any) {
                    val jsonResp = response as JSONObject

                    if (jsonResp.optInt("ret", -1) == 0) {
                        val jsonObject: String? = kv.decodeString("qq_login")
                        if (jsonObject == null) {

                           // 登录失败
                        } else {


                        }
                    } else {

                        //登录已过期
                       mTencent.logout(context)
                       kv.remove("qq_login")
                        kv.remove("qq_info")
                        kv.remove("user_login")

                    }
                }

                override fun onError(e: UiError) {}
                override fun onCancel() {}
            })


        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 设置 Toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        // 导航栏沉浸式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        // 设置 FloatingActionButton 的点击事件
        binding.appBarMain.fab?.setOnClickListener {

            if (binding.appBarMain.toolbar.title.toString() == getString(R.string.menu_transform)){

               if (mTencent.isSessionValid) {
                   // 获取当前id
                   val gson = Gson()
                   val qqLogin = gson.fromJson(kv.decodeString("qq_login"), QQLogin::class.java).openid

                   // 跳转页面
                     val intent = Intent(this, Jiaochengdaima::class.java)

                   // 传递数据
                   intent.putExtra("qqLogin", qqLogin)
                   startActivity(intent)

               } else {
                   Snackbar.make(it, "请先登录", Snackbar.LENGTH_LONG)
                       .setAction("Action", null).show()
               }
            }
            if (binding.appBarMain.toolbar.title.toString() == getString(R.string.menu_reflow)){
                //跳转页面
                //startActivity(Intent(this, Chuangjianxiangmu::class.java))

               startActivity(Intent(this, com.aslua.Main::class.java))
            }
        }


        // 获取 NavigationController 和 NavHostFragment
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        // 设置 NavigationView
        binding.navView?.let {

            // 配置 AppBarConfiguration
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_reflow, R.id.nav_transform,
                    R.id.nav_slideshow, R.id.nav_slideshowl,
                    R.id.nav_settings
                ),
                binding.drawerLayout
            )

            // 设置 ActionBar 和 NavigationController 的绑定
            setupActionBarWithNavController(navController, appBarConfiguration)

            // 设置 NavigationView 和 NavigationController 的绑定
            it.setupWithNavController(navController)

        }

        // 设置 BottomNavigationView
        binding.appBarMain.contentMain.bottomNavView?.let {

            // 配置 AppBarConfiguration
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_reflow, R.id.nav_transform,
                    R.id.nav_slideshow, R.id.nav_slideshowl,
                )
            )

            // 设置 ActionBar 和 NavigationController 的绑定
            setupActionBarWithNavController(navController, appBarConfiguration)

            // 设置 BottomNavigationView 和 NavigationController 的绑定
            it.setupWithNavController(navController)

        }
    }

    // 创建 Overflow 菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)

        // 使用 findViewById 查找 NavigationView
        val navView: NavigationView? = findViewById(R.id.nav_view)

        // 如果 NavigationView 不存在，则创建 Overflow 菜单
        if (navView == null) {
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    // 处理 Overflow 菜单点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                // 打开消息界面
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 处理 ActionBar 返回按钮点击事件
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // 使用 AppBarConfiguration 配置 NavigationController 的返回操作
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    //这个回调改不了，只能等腾讯api更新了再改
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        //腾讯QQ回调
        Tencent.onActivityResultData(requestCode, resultCode, data,iu)
        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
                Tencent.handleResultData(data, iu)
            }
        }
    }

    // 全局 context
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        // SD 卡目录
        val sdcardDir: String = Environment.getExternalStorageDirectory().absolutePath

        // 定义一个 boolean 类型的变量来跟踪 FAB 的状态
        var fabds = true

        //QQ登录
        val mTencent: Tencent by lazy {
            Tencent.createInstance("102049686",context, "com.tencent.login.fileprovider")
        }

        //初始化Room数据库
        object Db {
            val instance: AppDatabase by lazy {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "data-name"
                ).build()
            }
        }

        //Gson
        object GsonFactory {
            val instance: Gson by lazy {
                GsonBuilder().create()
            }
        }


    }
}