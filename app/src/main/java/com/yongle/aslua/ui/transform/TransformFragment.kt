package com.yongle.aslua.ui.transform

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.yongle.aslua.R
import com.yongle.aslua.databinding.FragmentTransformBinding
import com.yongle.aslua.databinding.ItemTransformBinding
import com.yongle.aslua.room.ContentType
import com.yongle.aslua.ui.aeiun.MainActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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


        // 获取 SearchView 对象并进行相关操作
        val searchView = binding.searchViewJk
        searchView.onActionViewExpanded()
        searchView.clearFocus()

        // 获取 tabTd 对象并添加选项卡
        val tabTd:TabLayout = binding.tabTd
        tabTd.addTab(tabTd.newTab().setText("所有分类").setTag(0))



          // 异步IO线程 查询数据
              CoroutineScope(Dispatchers.Main).launch {
                  val result = withContext(Dispatchers.IO) {
                      transformViewModel.getdata()
                  }
                  addTabs(tabTd, result)
              }




        // 添加分类选项卡的选择监听器
        tabTd.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 记录选中的tab
                transformViewModel.selectedTab = tab.position

                // 选项卡被选中时的回调
                println(tab.text.toString())
                println(tab.tag)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        //设置tabTm
        val tabTm:TabLayout = binding.tabTm
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_1))
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_2))
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_3))
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_4))
        tabTm.addTab(tabTm.newTab().setText(R.string.tab_texto_5))

        // 设置选中状态
        tabTm.getTabAt(transformViewModel.selectedTabs)?.select()

        // 添加选项卡的选择监听器
        tabTm.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 记录选中的tab
                transformViewModel.selectedTabs = tab!!.position

                // 选项卡被选中时的回调
                println(tab.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })





        // 初始化RecyclerView和Adapter
        val recyclerView = binding.recyclerviewTransform

        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // 观察ViewModel中的数据变化，更新Adapter
        transformViewModel.texts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }



        val layoutManager = LinearLayoutManager(activity)


// 定义加载更多时的起始位置和每次加载的数据条数
        var start = 0
        val count = 10

// 定义是否正在加载的标志位
        var isLoading = false

        // 加载更多数据的方法
        fun loadMoreData() {
            // 发送网络请求，获取新的数据
            // ...

            // 更新数据列表

            print("加载更多数据")
            // ...

            // 更新起始位置
            start += count

            // 加载完成后将 isLoading 标志位设为 false
            isLoading = false
        }


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 判断滑动方向
                if (dy > 0 && com.yongle.aslua.MainActivity.fabds) {

                    // 向下滑动，并且 FAB 是可见的，则隐藏
                    com.yongle.aslua.MainActivity.fabds = false
                    fab.animate().translationY(fab.y + fab.height).setDuration(400).setInterpolator(
                        AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).start()

                } else if (dy < 0 && !com.yongle.aslua.MainActivity.fabds) {

                    // 向上滑动，并且 FAB 是隐藏的，则显示 FAB
                    com.yongle.aslua.MainActivity.fabds= true
                    fab.animate().translationY(0f).setDuration(400).setInterpolator(
                        AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).start()

                }


                // 判断是否滑动到了最后一个
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (lastVisibleItemPosition == totalItemCount - 1 && !isLoading) {
                    // 滑动到最后一个，并且没有正在加载数据，则加载更多
                    isLoading = true
                    loadMoreData()
                }
            }
        })






        return root
    }

    private fun addTabs(tabLayout: TabLayout, tabs: List<ContentType>) {
        for (tab in tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab.typeName).setTag(tab.typeId))
        }

        // 设置选中状态
        tabLayout.post {
            tabLayout.getTabAt(transformViewModel.selectedTab)?.select()
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
        // 解绑视图
        _binding = null
    }


    // Adapter类
    class TransformAdapter :
            ListAdapter<String, TransformViewHolder>(object : DiffUtil.ItemCallback<String>() {

            // 判断是否是同一个item
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            // 判断是否是同一个内容
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {


        // Avatar资源ID列表
        private val drawables = listOf(
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6,
            R.drawable.avatar_7,
            R.drawable.avatar_8,
            R.drawable.avatar_9,
            R.drawable.avatar_10,
            R.drawable.avatar_11,
            R.drawable.avatar_12,
            R.drawable.avatar_13,
            R.drawable.avatar_14,
            R.drawable.avatar_15,
            R.drawable.avatar_16,
        )

        // 创建ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
            val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context))

            // 为itemView设置点击事件监听器
            return TransformViewHolder(binding).apply {
                itemView.setOnClickListener {
                    // 获取当前点击的item的位置
                    val position = bindingAdapterPosition
                    // 判断当前位置是否有效
                    if (position != RecyclerView.NO_POSITION) {
                        // 获取当前点击的item的内容
                        val text = getItem(position)

                        // 跳转页面
                        val intent = Intent(parent.context, MainActivity::class.java)

                        // 传递数据
                        intent.putExtra("name", text)
                        parent.context.startActivity(intent)


                    }
                }
                // 为itemView设置长按事件监听器
                itemView.setOnLongClickListener {

                    true
                }

            }


        }




        // 绑定ViewHolder
        override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
            holder.textView.text = getItem(position)
            holder.imageView.setImageDrawable(
                ResourcesCompat.getDrawable(holder.imageView.resources, drawables[position], null)
            )
        }
    }

    // 定义一个名为 TransformViewHolder 的类，它继承自 RecyclerView.ViewHolder 类
    class TransformViewHolder(
        binding: ItemTransformBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        // 定义一个名为 imageView 的 ImageView 类型的变量，并将其初始化为 ItemTransformBinding 中的 imageViewItemTransform
        val imageView: CircleImageView = binding.imageViewItemTransform

        // 定义一个名为 textView 的 TextView 类型的变量，并将其初始化为 ItemTransformBinding 中的 textViewItemTransform
        val textView: TextView = binding.textView5

    }


}






