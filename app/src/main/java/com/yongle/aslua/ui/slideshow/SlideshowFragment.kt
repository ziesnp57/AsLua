package com.yongle.aslua.ui.slideshow


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.yongle.aslua.MainActivity.Companion.mTencent
import com.yongle.aslua.R
import com.yongle.aslua.admin.Eimu
import com.yongle.aslua.data.ListItem
import com.yongle.aslua.databinding.FragmentSlideshowBinding
import com.yongle.aslua.login.Login
import com.yongle.aslua.login.QQInfo
import com.yongle.aslua.login.User
import com.yongle.aslua.login.UserLogin
import com.yongle.aslua.ui.slideshow.Yunama.Yunama
import com.yongle.aslua.ui.slideshow.guanyu.Guanyu
import com.yongle.aslua.ui.slideshow.liulan.Liulan
import com.yongle.aslua.ui.slideshow.shenhe.Shenhe
import com.yongle.aslua.ui.slideshow.shoucang.Shoucang
import com.yongle.aslua.ui.slideshow.teizi.Wdteizi


class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel = ViewModelProvider(this)[SlideshowViewModel::class.java]

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 隐藏fab
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        if (fab.isVisible) {
            fab.hide()
        }


            val kv = MMKV.defaultMMKV()
            kv.decodeString("qq_info")?.let {
                val gson = Gson()
                val qqInfo = gson.fromJson(it, QQInfo::class.java)

                //设置头像
                Glide.with(requireContext()).load(qqInfo.figureurl_qq_2).into(binding.qquserimg)

                //设置昵称
                binding.textView2.text = qqInfo.nickname

                binding.textView3.text = getString(R.string.chakanxincxi)

            }

        kv.decodeString("user_login")?.let {
            val gson = Gson()
            val userLogin = gson.fromJson(it, UserLogin::class.java).data[0].user_admin

            //设置权限
            if (userLogin == 1) {
                binding.rootshenhe.isVisible = true
            }

            if (userLogin == 2) {
                binding.rootshenhe.isVisible = true
                binding.rootyonghu.isVisible = true
            }
        }

        // 设置 qquser 的点击事件
        binding.qquser.setOnClickListener {

                if (!mTencent.isSessionValid) {

                    //跳转页面
                    startActivity(Intent(requireContext(), Login::class.java))

                } else {

                    //跳转页面
                    startActivity(Intent(requireContext(), User::class.java))
                }

        }


        // 设置 我的帖子 的点击事件
        binding.teizi.setOnClickListener {
            if (mTencent.isSessionValid) {
                //跳转页面
                startActivity(Intent(requireContext(), Wdteizi::class.java))
            } else {
                Snackbar.make(requireView(), "请先登录", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }

        // 设置 我的源码 的点击事件
        binding.yuama.setOnClickListener {
            if (mTencent.isSessionValid) {
                //跳转页面
                startActivity(Intent(requireContext(), Yunama::class.java))
            } else {
                Snackbar.make(requireView(), "请先登录", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }

        // 设置 审核管理 的点击事件
        binding.rootshenhe.setOnClickListener {
            // 审核管理
            if (mTencent.isSessionValid) {
                //跳转页面
                startActivity(Intent(requireContext(), Shenhe::class.java))
            } else {
                Snackbar.make(requireView(), "请先登录", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }

        // 设置 用户管理 的点击事件
        binding.rootyonghu.setOnClickListener {

            //跳转页面
            startActivity(Intent(requireContext(), Eimu::class.java))
        }


        // 合并图片资源和文本数据
        val listData = listOf(
            ListItem(R.drawable.liulanjilu, getString(R.string.liulanjilu)),
            ListItem(R.drawable.shoucang, getString(R.string.shoucang)),
            ListItem(R.drawable.shezhi, getString(R.string.sheshi)),
            ListItem(R.drawable.guanyu, getString(R.string.guanyu)),
        )

        // 自定义 Adapter，同时显示图片和文本
        class CustomAdapter(context: Context, private val resourceId: Int, private val data: List<ListItem>) :
            ArrayAdapter<ListItem>(context, resourceId, data) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(resourceId, parent, false)
                val imageView = view.findViewById<ImageView>(R.id.imageView5)
                val textView = view.findViewById<TextView>(R.id.textView2)

                // 设置图片和文本
                val item = data[position]
                imageView.setImageResource(item.imageId)
                textView.text = item.text

                return view
            }
        }

// 设置 listView 的 adapter 和点击事件
        binding.listView.adapter = CustomAdapter(requireContext(), R.layout.item_slideshow, listData)
        binding.listView.setOnItemClickListener { _, _, position, _ ->

            // 点击事件
            when (position) {
                0 -> {
                    // 浏览记录
                    if (mTencent.isSessionValid) {
                        //跳转页面
                        startActivity(Intent(requireContext(), Liulan::class.java))
                    } else {
                        Snackbar.make(requireView(), "请先登录", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    }
                }
                1 -> {
                    // 收藏
                    // 浏览记录
                    if (mTencent.isSessionValid) {
                        //跳转页面
                        startActivity(Intent(requireContext(), Shoucang::class.java))
                    } else {
                        Snackbar.make(requireView(), "请先登录", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    }
                }
                2 -> {
                    // 设置
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                }
                3 -> {
                    // 关于
                    startActivity(Intent(requireContext(), Guanyu::class.java))
                }
            }


        }



        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()

        // 释放binding
        _binding = null
    }


}
