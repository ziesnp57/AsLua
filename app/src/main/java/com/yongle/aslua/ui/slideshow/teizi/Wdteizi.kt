package com.yongle.aslua.ui.slideshow.teizi

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.data.ContentType
import com.yongle.aslua.data.Datalist
import com.yongle.aslua.databinding.ActivityWdteiziBinding
import com.yongle.aslua.databinding.ItemTransformBinding
import com.yongle.aslua.ui.aeiun.Code
import com.yongle.aslua.ui.tianjia.Jiaochengdaima
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Headers

class Wdteizi : AppCompatActivity() {

    private lateinit var binding: ActivityWdteiziBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置标题
        title = getString(R.string.teizi)

        // 从 ActivityMainBinding 中获取布局文件的根视图
        binding = ActivityWdteiziBinding.inflate(layoutInflater)

        // 设置布局
        setContentView(binding.root)

        // 创建 WdteziViewModel 对象
        val wdteziViewModel = ViewModelProvider(this)[WdteziViewModel::class.java]

        // 导航栏透明
        window.navigationBarColor = Color.TRANSPARENT

        val tabs: TabLayout = binding.tabs
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_1))
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_2))
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_3))

        // 设置选中状态
        tabs.getTabAt(wdteziViewModel.selectedTabs)?.select()

        // 添加选项卡的选择监听器
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 记录选中的tab
                wdteziViewModel.selectedTabs = tab.position

                // 加载数据
                intnrecyclerView("&id=" + tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        // 初始化RecyclerView
        val recyclerView = binding.recyclerviewWdteizi

        // 加载数据
        intnrecyclerView("&id=" + wdteziViewModel.selectedTabs.toString())


        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // 在这里执行刷新操作
            intnrecyclerView(wdteziViewModel.urls)
            swipeRefreshLayout.isRefreshing = false
        }

        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500
        )

        val uid = intent.extras?.getString("uid")

        fun loadMoreData() {
            // 发送网络请求，获取新的数据
            wdteziViewModel.start += 1

            HttpClient().okhttp(GetApi.SEARCH_HOT_DETTS + "?uid=$uid" + wdteziViewModel.urls + "&page=" + wdteziViewModel.start,
                null, null,
                object : HttpClient.HttpCallback {

                    // 处理响应结果
                    override fun onSuccess(code: Int, body: String?, headers: Headers) {

                        if (code == 200) {
                            val gson = GsonBuilder().create()
                            val hait = gson.fromJson(body, Datalist::class.java).data

                            if (hait.isNotEmpty()) {

                                runOnUiThread {

                                    val adapter = recyclerView.adapter as MyAdapter
                                    adapter.addAll(hait)

                                }
                                // 加载完成后将 isLoading 标志位设为 false
                                wdteziViewModel.isLoading = false
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
                    if (!wdteziViewModel.isLoading) {
                        wdteziViewModel.isLoading = true
                        loadMoreData()
                    }

                }
            }
        })

    }

    private fun intnrecyclerView(url: String) {
        // 创建 WdteziViewModel 对象
        val wdteziViewModel = ViewModelProvider(this)[WdteziViewModel::class.java]

        wdteziViewModel.urls = url

        wdteziViewModel.start = 1

        wdteziViewModel.isLoading = false

        // 获取 RecyclerView 对象并进行相关操作

        val uid = intent.extras?.getString("uid")

        HttpClient().okhttp(
            GetApi.SEARCH_HOT_DETTS + "?uid=$uid" + url, null, null,
            object : HttpClient.HttpCallback {
                // 处理响应结果
                override fun onSuccess(code: Int, body: String?, headers: Headers) {

                    if (code == 200) {
                        val gson = GsonBuilder().create()
                        val hotDetail = gson.fromJson(body, Datalist::class.java).data

                        runOnUiThread {
                            // 在这里执行 UI 操作

                            binding.recyclerviewWdteizi.adapter =
                                MyAdapter(hotDetail.toMutableList())
                        }

                    }
                }
            })
    }

    class MyAdapter(private val dataList: MutableList<ContentType>) :
        RecyclerView.Adapter<MyAdapter.WdteiziViewHolder>() {

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WdteiziViewHolder {
            val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context))

            // 为itemView设置点击事件监听器
            return WdteiziViewHolder(binding).apply {
                binding.materialCardView3.setOnClickListener {
                    // 获取当前点击的item的位置
                    val position = bindingAdapterPosition
                    // 判断当前位置是否有效
                    if (position != RecyclerView.NO_POSITION) {

                        // 跳转页面
                        val intent = Intent(parent.context, Code::class.java)

                        // 传递数据
                        intent.putExtra("id", dataList[position].id)
                        intent.putExtra("name", dataList[position].datalistName)
                        intent.putExtra("picture", dataList[position].userPicture)
                        intent.putExtra("username", dataList[position].userName)
                        intent.putExtra("time", dataList[position].timeAdd)
                        parent.context.startActivity(intent)

                    }
                }
                // 为itemView设置长按事件监听器
                binding.materialCardView3.setOnLongClickListener {


                    val items = arrayOf("复制名称", "修改帖子", "删除帖子")
                    MaterialAlertDialogBuilder(parent.context, R.style.MyAlertDialogStyle)
                        .setTitle("菜单")
                        .setItems(items) { _, which ->

                            when (which) {
                                0 -> {
                                    val clipboardManager =
                                        parent.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText(
                                        "text",
                                        dataList[bindingAdapterPosition].datalistName
                                    )
                                    clipboardManager.setPrimaryClip(clipData)

                                    Snackbar.make(it, "复制成功", Snackbar.LENGTH_SHORT).show()
                                }

                                1 -> {
                                    val intent = Intent(parent.context, Jiaochengdaima::class.java)
                                    intent.putExtra("id", dataList[bindingAdapterPosition].id)
                                    intent.putExtra(
                                        "typed",
                                        dataList[bindingAdapterPosition].typeId.toString()
                                    )
                                    intent.putExtra(
                                        "name",
                                        dataList[bindingAdapterPosition].datalistName
                                    )
                                    parent.context.startActivity(intent)

                                }

                                2 -> {
                                    MaterialAlertDialogBuilder(
                                        parent.context,
                                        R.style.MyAlertDialogStyle
                                    )
                                        .setTitle("提示")
                                        .setMessage("确定要删除帖子吗？")
                                        .setPositiveButton("确定") { _, _ ->
                                            HttpClient().okhttp(
                                                GetApi.SEARCH_HOT_DETTS + "?uid=" + dataList[bindingAdapterPosition].userId + "&id=3" + "&data_id=" + dataList[bindingAdapterPosition].id,
                                                null, null,
                                                object : HttpClient.HttpCallback {

                                                    // 处理响应结果
                                                    @SuppressLint("NotifyDataSetChanged")
                                                    override fun onSuccess(
                                                        code: Int, body: String?, headers: Headers
                                                    ) {
                                                        if (body == "1") {
                                                            it.post {

                                                                dataList.remove(dataList[bindingAdapterPosition])
                                                                notifyDataSetChanged()

                                                                Snackbar.make(
                                                                    it,
                                                                    "删除成功",
                                                                    Snackbar.LENGTH_SHORT
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

        private fun formatNumber(num: Int): String {
            return when {
                num < 1000 -> num.toString()
                num < 10000 -> String.format("%.1fk", num / 1000.0)
                else -> String.format("%.1fw", num / 10000.0)
            }
        }

        override fun onBindViewHolder(holder: WdteiziViewHolder, position: Int) {

            holder.textView.text = dataList[position].userName
            holder.textView1.text = dataList[position].datalistName
            holder.textView2.text = dataList[position].datalistData
            holder.textView3.text = dataList[position].timeAdd
            holder.textView4.text = formatNumber(dataList[position].datalistUp.toInt())
            holder.textView5.text = formatNumber(dataList[position].datalistDown.toInt())
            holder.textView6.text = formatNumber(dataList[position].datalistFav.toInt())

            //设置头像
            Glide.with(holder.itemView.context)
                .load(dataList[position].userPicture)
                .into(holder.imageView)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun addAll(list: List<ContentType>) {
            dataList.addAll(list)
            notifyDataSetChanged()
        }


        inner class WdteiziViewHolder(
            binding: ItemTransformBinding
        ) :
            RecyclerView.ViewHolder(binding.root) {

            val imageView: CircleImageView = binding.imageViewItemTransform

            val textView: TextView = binding.textView5

            val textView1: TextView = binding.textView9

            val textView2: TextView = binding.textView13

            val textView3: TextView = binding.textView7

            val textView4: TextView = binding.textView10

            val textView5: TextView = binding.textView11

            val textView6: TextView = binding.textView12
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