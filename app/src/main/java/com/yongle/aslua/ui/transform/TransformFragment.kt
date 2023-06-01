package com.yongle.aslua.ui.transform


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tencent.mmkv.MMKV
import com.yongle.aslua.MainActivity.Companion.Db
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.data.ContentType
import com.yongle.aslua.data.Datalist
import com.yongle.aslua.databinding.FragmentTransformBinding
import com.yongle.aslua.databinding.ItemTransformBinding
import com.yongle.aslua.login.UserLogin
import com.yongle.aslua.ui.aeiun.Code
import com.yongle.aslua.ui.tianjia.Jiaochengdaima
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers


class TransformFragment : Fragment() {

    private val transformViewModel: TransformViewModel by lazy {
        ViewModelProvider(this)[TransformViewModel::class.java]
    }

    // 该属性用于绑定视图，仅在 onCreateView 和 onDestroyView 方法之间有效
    private var _binding: FragmentTransformBinding? = null

    // 用于获取绑定的视图
    private val binding get() = _binding!!

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 创建 TransformViewModel 对象
        val transformViewModel = ViewModelProvider(this)[TransformViewModel::class.java]
        // 绑定视图
        _binding = FragmentTransformBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)!!
        if (!fab.isVisible) {
            fab.show()
        }

        // 初始化RecyclerView
        val recyclerView = binding.recyclerviewTransform

        // 获取 SearchView 对象并进行相关操作
        val searchView = binding.searchViewJk
        searchView.onActionViewExpanded()

        // 搜索框文字变化监听
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            // 搜索框文字变化监听
            override fun onQueryTextChange(newText: String?): Boolean {

                // 去除空格
                if (newText != null && newText.startsWith(" ")) {
                    searchView.setQuery(newText.trimStart(), false)
                } else {
                    // 显示搜索结果
                    intnrecyclerView("?id=" + transformViewModel.selectedTab + "&wd=$newText&fi=" + transformViewModel.selectedTabs)
                }

                return true
            }
        })


        // 获取 tabTd 对象并添加选项卡
        val tabTd: TabLayout = binding.tabTd
        tabTd.addTab(tabTd.newTab().setText("所有分类").setTag(0))

        if (transformViewModel.typeList.size != 0) {
            for (tab in transformViewModel.typeList) {
                tabTd.addTab(tabTd.newTab().setText(tab.typeName).setTag(tab.typeId))
            }
            // 设置选中状态
            tabTd.post {
                val tab = tabTd.getTabAt(transformViewModel.selectedTab)
                tabTd.clearAnimation()
                tabTd.setScrollPosition(transformViewModel.selectedTab, 0f, true)
                tab?.select()
            }
        } else {
            // 异步IO线程 查询数据
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    Db.instance.contentTypeDao().getAll()
                }

                for (tab in result) {
                    tabTd.addTab(tabTd.newTab().setText(tab.typeName).setTag(tab.typeId))
                }

                // 设置选中状态
                tabTd.post {
                    tabTd.getTabAt(transformViewModel.selectedTab)?.select()
                }

                transformViewModel.typeList = result.toMutableList()
            }
        }

        // 添加分类选项卡的选择监听器
        tabTd.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 记录选中的tab
                transformViewModel.selectedTab = tab.position

                // 获取搜索框内容
                val searchText = searchView.query.toString()

                // 加载数据
                intnrecyclerView("?id=" + tab.tag + "&wd=$searchText&fi=" + transformViewModel.selectedTabs)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        //设置tabTm
        val tabTm: TabLayout = binding.tabTm
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_1))
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_2))
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_3))
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_4))

        // 设置选中状态
        tabTm.getTabAt(transformViewModel.selectedTabs)?.select()

        // 添加选项卡的选择监听器
        tabTm.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 记录选中的tab
                transformViewModel.selectedTabs = tab.position

                // 获取搜索框内容
                val searchText = searchView.query.toString()

                // 加载数据
                intnrecyclerView("?id=" + transformViewModel.selectedTab + "&wd=$searchText&fi=" + tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        // 加载数据
        if (transformViewModel.selectedTab == 0) {
            intnrecyclerView("")
        }

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout?.setOnRefreshListener {
            // 在这里执行刷新操作
            intnrecyclerView(transformViewModel.urls)
            swipeRefreshLayout.isRefreshing = false
        }

        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout?.setColorSchemeResources(
            R.color.purple_500
        )



        fun loadMoreData() {
            // 发送网络请求，获取新的数据

            transformViewModel.start += 1

            HttpClient().okhttp(GetApi.SEARCH_HOT_DETAIL + transformViewModel.urls + "&page=" + transformViewModel.start,
                null,
                null,
                object : HttpClient.HttpCallback {

                    // 处理响应结果
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onSuccess(code: Int, body: String?, headers: Headers) {

                        if (code == 200) {
                            val gson = GsonBuilder().create()
                            val hait = gson.fromJson(body, Datalist::class.java).data

                            if (hait.isNotEmpty()) {
                                for (hot in hait) {
                                    transformViewModel.dataLists.add(hot)
                                }
                                activity?.runOnUiThread {
                                    // 通知 Adapter 更新数据
                                    recyclerView.adapter?.notifyDataSetChanged()
                                }
                                // 加载完成后将 isLoading 标志位设为 false
                                transformViewModel.isLoading = false
                            }

                        }
                    }
                })
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 判断滑动方向
                if (dy > 0 && com.yongle.aslua.MainActivity.fabds) {

                    // 向下滑动，并且 FAB 是可见的，则隐藏
                    com.yongle.aslua.MainActivity.fabds = false
                    fab.animate().translationY(fab.y + fab.height).setDuration(400).setInterpolator(
                        AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
                    ).start()

                } else if (dy < 0 && !com.yongle.aslua.MainActivity.fabds) {

                    // 向上滑动，并且 FAB 是隐藏的，则显示 FAB
                    com.yongle.aslua.MainActivity.fabds = true
                    fab.animate().translationY(0f).setDuration(400).setInterpolator(
                        AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
                    ).start()

                }

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition == transformViewModel.dataLists.size - 2) {
                    // 滑动到最后一个item，触发加载更多数据的操作
                    if (!transformViewModel.isLoading) {
                        transformViewModel.isLoading = true
                        loadMoreData()
                    }
                }
            }
        })


        return root
    }


    private fun intnrecyclerView(url: String) {

        if (!transformViewModel.isLoaSig) {

            transformViewModel.urls = url
            transformViewModel.start = 1
            transformViewModel.isLoading = false

            // 获取 RecyclerView 对象并进行相关操作
            HttpClient().okhttp(GetApi.SEARCH_HOT_DETAIL + url, null, null,
                object : HttpClient.HttpCallback {
                    // 处理响应结果
                    override fun onSuccess(code: Int, body: String?, headers: Headers) {

                        if (code == 200) {
                            val gson = GsonBuilder().create()
                            val hotDetail = gson.fromJson(body, Datalist::class.java).data

                            transformViewModel.dataLists.clear()

                            for (hot in hotDetail) {
                                transformViewModel.dataLists.add(hot)
                            }

                            activity?.runOnUiThread {
                                // 在这里执行 UI 操作
                                binding.recyclerviewTransform.adapter =
                                    MyAdapter(transformViewModel.dataLists)
                            }
                        }
                    }
                })
        } else {
            transformViewModel.isLoaSig = false
            binding.recyclerviewTransform.adapter =
                MyAdapter(transformViewModel.dataLists)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onDestroyView() {
        super.onDestroyView()

        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)!!
        // 向上滑动，并且 FAB 是隐藏的，则显示 FAB
        if (!com.yongle.aslua.MainActivity.fabds) {
            com.yongle.aslua.MainActivity.fabds = true
            fab.animate().translationY(0f).setDuration(400).setInterpolator(
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            ).start()
        }

        transformViewModel.isLoaSig = true
        // 解绑视图
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // 清除焦点
        binding.searchViewJk.clearFocus()
    }

    class MyAdapter(private val dataList: MutableList<ContentType>) :
        RecyclerView.Adapter<MyAdapter.TransformViewHolder>() {

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }

        @SuppressLint("ServiceCast")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
            val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context))

            // 为itemView设置点击事件监听器
            return TransformViewHolder(binding).apply {
                binding.materialCardView3.setOnClickListener {
                    // 获取当前点击的item的位置
                    val position = bindingAdapterPosition
                    // 判断当前位置是否有效
                    if (position != RecyclerView.NO_POSITION) {

                        // 隐藏输入法
                        val imm =
                            parent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(it.windowToken, 0)

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
                    // 声明 MMKV
                    val kv = MMKV.defaultMMKV()
                    val uid =
                        Gson().fromJson(kv.decodeString("user_login"), UserLogin::class.java)?.uid

                    val admin =
                        Gson().fromJson(kv.decodeString("user_login"), UserLogin::class.java)?.user_admin

                    // 判断是否为自己的帖子
                    val items = if (uid == dataList[bindingAdapterPosition].userId || admin == 1 ||admin == 2) {
                        arrayOf("复制名称", "修改帖子", "删除帖子")
                    } else {
                        arrayOf("复制名称")
                    }

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

        override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {

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

        inner class TransformViewHolder(
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

}