package com.yongle.aslua.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.yongle.aslua.MainActivity
import com.yongle.aslua.R
import com.yongle.aslua.data.ListItemuser
import com.yongle.aslua.databinding.ActivityUserBinding
import com.yongle.aslua.ui.slideshow.SlideshowFragment

class User : AppCompatActivity() {

    // 声明变量
    private lateinit var binding: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.yonghuxinxi)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityUserBinding.inflate(layoutInflater)

        // 初始化
        val  kv = MMKV.defaultMMKV()

        // 设置布局
        setContentView(binding.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT


        // 合并qq_info  qq_login图片资源和文本数据
        val listData = mutableListOf<ListItemuser>()

        kv.decodeString("qq_login")?.let { it ->
            val gson = Gson()
            val qqLogin = gson.fromJson(it, QQLogin::class.java)

            kv.decodeString("qq_info")?.let {
                val qqInfo = gson.fromJson(it, QQInfo::class.java)
                listData.add(ListItemuser(qqInfo.figureurl_qq_2, getString(R.string.touxang), null))
                listData.add(ListItemuser(null, getString(R.string.nicheng), qqInfo.nickname))
                listData.add(ListItemuser(null, getString(R.string.id), qqLogin.openid))

                    val userLogin = gson.fromJson(kv.decodeString("user_login"), UserLogin::class.java).data[0]

                   if (userLogin.user_admin == 2) {
                            listData.add(ListItemuser(null, getString(R.string.quanxan),
                                "开发者"
                            ))
                        }
                    if (userLogin.user_admin == 1) {
                            listData.add(ListItemuser(null, getString(R.string.quanxan),
                                "管理员"
                            ))
                        }
                    if (userLogin.user_admin == 0) {
                            listData.add(ListItemuser(null, getString(R.string.quanxan),
                                "普通用户"
                            ))
                        }


                    listData.add(ListItemuser(null, getString(R.string.zhuceshijan),
                        userLogin.creation_time
                    ))

            }

        }



        // 自定义 Adapter，同时显示图片和文本
        class CustomAdapter(context: Context, private val resourceId: Int, private val data: List<ListItemuser>) :
            ArrayAdapter<ListItemuser>(context, resourceId, data) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(resourceId, parent, false)
                val imageView = view.findViewById<ImageView>(R.id.imageView5)
                val textView = view.findViewById<TextView>(R.id.textView2)
                val textView1 = view.findViewById<TextView>(R.id.textView4)

                // 设置图片和文本
                val item = data[position]
                item.imageId?.let {

                    //设置头像
                    Glide.with(this@User).load(item.imageId).into(imageView)
                     }
                textView.text = item.text
                textView1.text = item.text1

                return view
            }
        }
        binding.listView.adapter = CustomAdapter(this, R.layout.item_user, listData)


        // 设置退出登录按钮的点击事件
binding.button3.setOnClickListener {

    MainActivity.mTencent.logout(this)
    kv.remove("qq_login")
    kv.remove("qq_info")
    kv.remove("user_login")

    val intent = Intent(this@User,SlideshowFragment::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)

}
    }


    // 设置返回按钮的点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                @Suppress("DEPRECATION")
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}