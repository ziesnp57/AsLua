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
import com.yongle.aslua.MainActivity.Companion.context
import com.yongle.aslua.MainActivity.Companion.mTencent
import com.yongle.aslua.R
import com.yongle.aslua.data.ListItemuser
import com.yongle.aslua.databinding.ActivityUserBinding

class User : AppCompatActivity() {

    // 声明变量
    private var binding: ActivityUserBinding? = null

    val kv = MMKV.defaultMMKV()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.yonghuxinxi)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityUserBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding?.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT


        // 合并qq_info  qq_login图片资源和文本数据
        val listData = mutableListOf<ListItemuser>()

        kv.decodeString("user_login")?.let {
            val userLogin = Gson().fromJson(it, UserLogin::class.java)

            listData.add(ListItemuser(userLogin.user_picture, getString(R.string.touxang), null))
            listData.add(ListItemuser(null, getString(R.string.nicheng), userLogin.user_name))
            listData.add(ListItemuser(null, getString(R.string.id), userLogin.uid))

            val userType = when (userLogin.user_admin) {
                2 -> "开发者"
                1 -> "管理员"
                else -> "普通用户"
            }
            listData.add(ListItemuser(null, getString(R.string.quanxan), userType))

            listData.add(
                ListItemuser(
                    null, getString(R.string.zhuceshijan),
                    userLogin.creation_time
                )
            )

        }


        // 自定义 Adapter，同时显示图片和文本
        class CustomAdapter(
            context: Context,
            private val resourceId: Int,
            private val data: List<ListItemuser>
        ) :
            ArrayAdapter<ListItemuser>(context, resourceId, data) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view =
                    convertView ?: LayoutInflater.from(context).inflate(resourceId, parent, false)
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
        binding!!.listView.adapter = CustomAdapter(this, R.layout.item_user, listData)



// 设置退出登录按钮的点击事件
        binding!!.button3.setOnClickListener {

            // 退出QQ登录
            mTencent.logout(context)

            // 退出登录成功
            kv.remove("qq_login")
            kv.remove("user_login")

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

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