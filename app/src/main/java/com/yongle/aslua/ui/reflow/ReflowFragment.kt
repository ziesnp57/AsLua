package com.yongle.aslua.ui.reflow

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.yongle.aslua.MainActivity.Companion.sdDir
import com.yongle.aslua.R
import com.yongle.aslua.data.App
import com.yongle.aslua.data.AppList
import com.yongle.aslua.data.ZipFolder
import com.yongle.aslua.databinding.FragmentReflowBinding
import com.yongle.aslua.databinding.ItemReflowBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ReflowFragment : Fragment() {

    // 该属性用于绑定视图，仅在 onCreateView 和 onDestroyView 方法之间有效
    private var _binding: FragmentReflowBinding? = null

    private val binding get() = _binding!! // 获取绑定视图


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle? // 用于保存和恢复状态的数据
    ): View {
        val reflowViewModel = ViewModelProvider(this)[ReflowViewModel::class.java]

        _binding = FragmentReflowBinding.inflate(inflater, container, false) // 将布局文件绑定到视图
        val root: View = binding.root // 获取根视图

        // 显示fab
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        if (!fab.isVisible) {
            fab.show()
        }


        // 初始化RecyclerView
        val recyclerView = binding.recyclerviewReflow


        if (!reflowViewModel.isLoaSig) {
            reflowViewModel.isLoaSig = true
            intnrecyclerView()
            recyclerView.adapter = MyAdapter(reflowViewModel.appList)
        } else {
            recyclerView.adapter = MyAdapter(reflowViewModel.appList)
            if (reflowViewModel.appList.size != 0) {
                binding.materialCardView7.visibility = View.GONE
                binding.textView8.visibility = View.GONE
                binding.imageView7.visibility = View.GONE
            } else {
                binding.materialCardView7.visibility = View.VISIBLE
                binding.textView8.visibility = View.VISIBLE
                binding.imageView7.visibility = View.VISIBLE
            }
        }

        // 下拉刷新
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // 刷新时的操作
            reflowViewModel.appList.clear()
            intnrecyclerView()
            swipeRefreshLayout.isRefreshing = false
        }

        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500
        )



        return root // 返回根视图
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null // 在视图销毁时解除视图绑定
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun intnrecyclerView() {

        val reflowViewModel = ViewModelProvider(this)[ReflowViewModel::class.java]

        val recyclerView = binding.recyclerviewReflow
        val pluginDir = "$sdDir/AsLua/project"

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {

                File(pluginDir).list()?.forEach {

                    val appFile = File("$pluginDir/$it/app.json")
                    val iconFile = File("$pluginDir/$it/icon.png")

                    if (appFile.exists()) {
                        try {
                            val gson = GsonBuilder().create()
                            val app = gson.fromJson(appFile.readText(), App::class.java)
                            val iconPath = if (iconFile.exists()) iconFile.absolutePath else null
                            reflowViewModel.appList.add(
                                AppList(iconPath, app.appName, app.appVer, app.appPackageName, it)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                recyclerView.adapter?.notifyDataSetChanged()

                if (reflowViewModel.appList.size != 0) {
                    binding.materialCardView7.visibility = View.GONE
                    binding.textView8.visibility = View.GONE
                    binding.imageView7.visibility = View.GONE
                } else {
                    binding.materialCardView7.visibility = View.VISIBLE
                    binding.textView8.visibility = View.VISIBLE
                    binding.imageView7.visibility = View.VISIBLE
                }
            }
        }
    }


    class MyAdapter(private val dataList: MutableList<AppList>) :
        RecyclerView.Adapter<MyAdapter.ReflowViewHolder>() {


        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }


        @SuppressLint("NotifyDataSetChanged")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReflowViewHolder {
            val binding = ItemReflowBinding.inflate(LayoutInflater.from(parent.context))

            // 为itemView设置点击事件监听器
            return ReflowViewHolder(binding).apply {
                binding.materialCardView3.setOnClickListener {
                    // 获取当前点击的item的位置
                    val position = bindingAdapterPosition
                    // 判断当前位置是否有效
                    if (position != RecyclerView.NO_POSITION) {

                        // 跳转页面
                        val intent = Intent(parent.context, Editpage::class.java)

                        // 传递数据
                        intent.putExtra("nameDri", dataList[position].name)
                        intent.putExtra("name", dataList[position].appName)
                        parent.context.startActivity(intent)

                    }
                }
                // 为itemView设置长按事件监听器
                binding.materialCardView3.setOnLongClickListener {

                    val items = arrayOf("删除项目", "导出项目")
                    MaterialAlertDialogBuilder(parent.context, R.style.MyAlertDialogStyle)
                        .setTitle("菜单")
                        .setItems(items) { _, which ->
                            val directory =
                                File("$sdDir/AsLua/project/${dataList[bindingAdapterPosition].name}")

                            when (which) {
                                0 -> {
                                    MaterialAlertDialogBuilder(
                                        parent.context,
                                        R.style.MyAlertDialogStyle
                                    )
                                        .setTitle("提示")
                                        .setMessage("确定要删除项目吗？")
                                        .setPositiveButton("确定") { _, _ ->
                                            CoroutineScope(Dispatchers.IO).launch {
                                                withContext(Dispatchers.IO) {
                                                    if (directory.exists() && directory.isDirectory) {
                                                        directory.deleteRecursively()
                                                    }
                                                }
                                            }

                                            CoroutineScope(Dispatchers.Main).launch {
                                                dataList.remove(dataList[bindingAdapterPosition])
                                                notifyDataSetChanged()
                                                Snackbar.make(it, "删除成功", Snackbar.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }
                                        .setNegativeButton("取消") { _, _ ->
                                        }
                                        .show()
                                }

                                1 -> {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        withContext(Dispatchers.IO) {

                                            if (directory.exists() && directory.isDirectory) {
                                                val zipFile = File(
                                                    "$sdDir/AsLua/project/${dataList[bindingAdapterPosition].name}.als"
                                                )
                                                if (zipFile.exists()) {
                                                    zipFile.delete()
                                                }
                                                ZipFolder().zip(
                                                    directory.absolutePath,
                                                    zipFile.absolutePath
                                                )
                                            }
                                        }

                                        Snackbar.make(
                                            binding.root,
                                            "导出成功",
                                            Snackbar.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            }
                        }
                        .show()



                    true
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ReflowViewHolder, position: Int) {

            holder.textView.text = dataList[position].appName
            holder.textView1.text = "版本 " + dataList[position].appVer
            holder.textView2.text = "包名 " + dataList[position].appPackageName
            //设置icon
            if (dataList[position].icon != null)
                Glide.with(holder.itemView.context)
                    .load(dataList[position].icon)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.imageView)
        }

        inner class ReflowViewHolder(
            binding: ItemReflowBinding
        ) :
            RecyclerView.ViewHolder(binding.root) {

            val imageView: ImageView = binding.imageView9

            val textView: TextView = binding.textView18

            val textView1: TextView = binding.textView19

            val textView2: TextView = binding.textView20

        }

    }
}