package com.yongle.aslua.ui.slideshow.liulan.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.yongle.aslua.databinding.FragmentLiulansBinding


class PlaceholdersFragment : Fragment() {

    // 声明一个 PageViewModel 实例
    private lateinit var pageViewModel: PageViewModel

    // 声明一个 FragmentLiulanBinding 实例
    private var _binding: FragmentLiulansBinding? = null
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
        _binding = FragmentLiulansBinding.inflate(inflater, container, false)
        val root = binding.root

       /* val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // 在这里执行刷新操作
            // intnrecyclerView(wdteziViewModel.urls)
            swipeRefreshLayout.isRefreshing = false
        }

        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500
        )*/


        return root
    }


    companion object {
        // 创建一个静态方法，用于创建 Fragment 实例
        @JvmStatic
        fun newInstance(): PlaceholdersFragment {
            return PlaceholdersFragment().apply {
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