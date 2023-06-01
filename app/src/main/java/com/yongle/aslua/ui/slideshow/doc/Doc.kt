package com.yongle.aslua.ui.slideshow.doc

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.data.Content
import com.yongle.aslua.data.Doclist
import com.yongle.aslua.databinding.ActivityDocBinding
import com.yongle.aslua.databinding.ItemDocBinding
import okhttp3.Headers

class Doc : AppCompatActivity() {

    private lateinit var binding: ActivityDocBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.doc)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityDocBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT


        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // 在这里执行刷新操作
            intnrecyclerView()
            swipeRefreshLayout.isRefreshing = false
        }

        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500
        )

        // 初始化RecyclerView
        val recyclerView = binding.recyclerviewDoc

        intnrecyclerView()


        var isLoading = false
        var start = 1
        val uid = intent.extras?.getString("uid")

        fun loadMoreData() {
            // 发送网络请求，获取新的数据
            start += 1

            HttpClient().okhttp(
                GetApi.SEARCH_HOT_DOC + "?uid=$uid&page=$start",
                null, null,
                object : HttpClient.HttpCallback {
                    // 处理响应结果
                    override fun onSuccess(code: Int, body: String?, headers: Headers) {

                        if (code == 200) {
                            val gson = GsonBuilder().create()
                            val hait = gson.fromJson(body, Doclist::class.java).data

                            if (hait.isNotEmpty()) {

                                runOnUiThread {
                                    val adapter =
                                        recyclerView.adapter as MyAdapter
                                    adapter.addAll(hait)

                                }
                                // 加载完成后将 isLoading 标志位设为 false
                                isLoading = false
                            }

                        }
                    }
                })
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                if (lastVisibleItemPosition == recyclerView.adapter!!.itemCount - 2) {

                    // 滑动到最后一个item，触发加载更多数据的操作
                    if (!isLoading) {
                        isLoading = true
                        loadMoreData()
                    }

                }
            }
        })

    }


    private fun intnrecyclerView() {

        // pageViewModel.urls = url

        //  pageViewModel.start = 1

        //  pageViewModel.isLoading = false

        // 获取 RecyclerView 对象并进行相关操作
        val uid = intent.extras?.getString("uid")
        HttpClient().okhttp(
            GetApi.SEARCH_HOT_DOC + "?uid=$uid", null, null,
            object : HttpClient.HttpCallback {
                // 处理响应结果
                override fun onSuccess(code: Int, body: String?, headers: Headers) {

                    if (code == 200) {
                        val gson = GsonBuilder().create()
                        val hotDetail = gson.fromJson(body, Doclist::class.java).data

                        runOnUiThread {
                            // 在这里执行 UI 操作
                            binding.recyclerviewDoc.adapter =
                                MyAdapter(hotDetail.toMutableList())
                        }

                    }
                }
            })
    }

    class MyAdapter(private val dataList: MutableList<Content>) :
        RecyclerView.Adapter<MyAdapter.DocViewHolder>() {

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
            val binding = ItemDocBinding.inflate(LayoutInflater.from(parent.context))

            // 为itemView设置点击事件监听器
            return DocViewHolder(binding).apply {
                binding.materialCardView3.setOnClickListener {
                    // 获取当前点击的item的位置
                    val position = bindingAdapterPosition
                    // 判断当前位置是否有效
                    if (position != RecyclerView.NO_POSITION) {

                        // 跳转页面
                        val intent = Intent(parent.context, DocAdd::class.java)
                        // 传递数据
                        intent.putExtra("id", dataList[position].url)
                        intent.putExtra("name", dataList[position].name)
                        intent.putExtra("url", "${GetApi.SEARCH_HOT_DOC}/${dataList[position].url}")
                        parent.context.startActivity(intent)

                    }
                }
                // 为itemView设置长按事件监听器
                binding.materialCardView3.setOnLongClickListener {

                    val items = arrayOf("复制链接", "删除文档")
                    MaterialAlertDialogBuilder(parent.context, R.style.MyAlertDialogStyle)
                        .setTitle("菜单")
                        .setItems(items) { _, which ->
                            when (which) {
                                0 -> {
                                    val clipboard =
                                        parent.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText(
                                        "Label",
                                        "${GetApi.SEARCH_HOT_DOC}/${dataList[bindingAdapterPosition].url}"
                                    )
                                    clipboard.setPrimaryClip(clipData)
                                    Snackbar.make(
                                        binding.root,
                                        "已复制链接",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }

                                1 -> {
                                    // 处理删除操作
                                    MaterialAlertDialogBuilder(
                                        parent.context,
                                        R.style.MyAlertDialogStyle
                                    )
                                        .setTitle("提示")
                                        .setMessage("确定要删除文档吗？")
                                        .setPositiveButton("确定") { _, _ ->

                                            HttpClient().okhttp(
                                                GetApi.SEARCH_HOT_DOC + "?url=" + dataList[bindingAdapterPosition].url,
                                                null, null,
                                                object : HttpClient.HttpCallback {

                                                    // 处理响应结果
                                                    @SuppressLint("NotifyDataSetChanged")
                                                    override fun onSuccess(
                                                        code: Int,
                                                        body: String?,
                                                        headers: Headers
                                                    ) {
                                                        if (body != "0") {

                                                            dataList.remove(dataList[bindingAdapterPosition])
                                                            // 在主线程中刷新适配器
                                                            Handler(Looper.getMainLooper())
                                                                .post {
                                                                    notifyDataSetChanged()
                                                                    Snackbar.make(
                                                                        binding.root,
                                                                        "删除成功",
                                                                        Snackbar.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                        }
                                                    }
                                                })
                                        }
                                        .setNegativeButton("取消") { _, _ ->
                                        }
                                        .show()
                                }
                            }
                        }
                        .show()

                    true
                }
            }
        }

        override fun onBindViewHolder(holder: DocViewHolder, position: Int) {

            holder.textView.text = dataList[position].name
            holder.textView1.text = dataList[position].time

        }

        @SuppressLint("NotifyDataSetChanged")
        fun addAll(list: List<Content>) {
            dataList.addAll(list)
            notifyDataSetChanged()
        }

        inner class DocViewHolder(
            binding: ItemDocBinding
        ) :
            RecyclerView.ViewHolder(binding.root) {

            val textView: TextView = binding.textView17

            val textView1: TextView = binding.textView21

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.doc, menu)
        val clearItem = menu.findItem(R.id.action_clear)
        clearItem.setOnMenuItemClickListener {
            // 跳转页面
            startActivity(Intent(this, DocAdd::class.java))
            true
        }
        return true
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