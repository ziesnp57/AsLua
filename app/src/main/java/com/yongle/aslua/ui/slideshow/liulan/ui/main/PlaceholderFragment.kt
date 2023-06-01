package com.yongle.aslua.ui.slideshow.liulan.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tencent.mmkv.MMKV
import com.yongle.aslua.R
import com.yongle.aslua.api.GetApi
import com.yongle.aslua.api.HttpClient
import com.yongle.aslua.data.ContentType
import com.yongle.aslua.data.Datalist
import com.yongle.aslua.databinding.FragmentLiulanBinding
import com.yongle.aslua.databinding.ItemTransformBinding
import com.yongle.aslua.login.UserLogin
import com.yongle.aslua.ui.aeiun.Code
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.Headers


class PlaceholderFragment : Fragment() {

    // 声明一个 PageViewModel 实例
    private lateinit var pageViewModel: PageViewModel

    // 声明一个 FragmentLiulanBinding 实例
    private var _binding: FragmentLiulanBinding? = null
    private val binding get() = _binding!!


    // 在 onCreate 方法中，初始化 pageViewModel 实例，并设置索引
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageViewModel = ViewModelProvider(this)[PageViewModel::class.java]
    }

    // 在 onCreateView 方法中，初始化 FragmentLiulanBinding 实例，并设置 sectionLabel 的文本
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 初始化 FragmentLiulanBinding 实例
        _binding = FragmentLiulanBinding.inflate(inflater, container, false)
        val root = binding.root


        // 初始化RecyclerView
        val recyclerView = binding.recyclerviewLiulan

         val kv = MMKV.defaultMMKV()

        // 加载数据
        intnrecyclerView(
            "&uid=" + Gson().fromJson(
                kv.decodeString("user_login"),
                UserLogin::class.java
            ).uid
        )

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // 在这里执行刷新操作
            intnrecyclerView(pageViewModel.urls)
            swipeRefreshLayout.isRefreshing = false
        }

        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500
        )


        fun loadMoreData() {
            // 发送网络请求，获取新的数据
            pageViewModel.start += 1

            HttpClient().okhttp(
                GetApi.SEARCH_HOT_DETAOU + "?uid=" + pageViewModel.urls + "&page=" + pageViewModel.start,
                null, null,
                object : HttpClient.HttpCallback {

                    // 处理响应结果
                    override fun onSuccess(code: Int, body: String?, headers: Headers) {

                        if (code == 200) {
                            val gson = GsonBuilder().create()
                            val hait = gson.fromJson(body, Datalist::class.java).data

                            if (hait.isNotEmpty()) {

                                activity?.runOnUiThread {

                                    val adapter = recyclerView.adapter as MyAdapter
                                    adapter.addAll(hait)

                                }
                                // 加载完成后将 isLoading 标志位设为 false
                                pageViewModel.isLoading = false
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
                    if (!pageViewModel.isLoading) {
                        pageViewModel.isLoading = true
                        loadMoreData()
                    }

                }
            }
        })

        return root
    }

    private fun intnrecyclerView(url: String) {

        pageViewModel.urls = url

        pageViewModel.start = 1

        pageViewModel.isLoading = false

        // 获取 RecyclerView 对象并进行相关操作

        HttpClient().okhttp(
            GetApi.SEARCH_HOT_DETAOU + "?uid=$url", null, null,
            object : HttpClient.HttpCallback {
                // 处理响应结果
                override fun onSuccess(code: Int, body: String?, headers: Headers) {

                    if (code == 200) {
                        val gson = GsonBuilder().create()
                        val hotDetail = gson.fromJson(body, Datalist::class.java).data

                        activity?.runOnUiThread {

                            // 在这里执行 UI 操作
                            binding.recyclerviewLiulan.adapter =
                                MyAdapter(hotDetail.toMutableList())
                        }

                    }
                }
            })
    }

    class MyAdapter(private val dataList: MutableList<ContentType>) :
        RecyclerView.Adapter<MyAdapter.LiulanViewHolder>() {

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiulanViewHolder {
            val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context))

            // 为itemView设置点击事件监听器
            return LiulanViewHolder(binding).apply {
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

        override fun onBindViewHolder(holder: LiulanViewHolder, position: Int) {

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

        inner class LiulanViewHolder(
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

    companion object {
        // 创建一个静态方法，用于创建 Fragment 实例
        @JvmStatic
        fun newInstance(): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle()
            }
        }
    }

    // 在 onDestroyView 方法中，清空 _binding 实例
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
