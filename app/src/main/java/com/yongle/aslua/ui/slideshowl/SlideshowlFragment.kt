package com.yongle.aslua.ui.slideshowl

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.yongle.aslua.R
import com.yongle.aslua.databinding.FragmentSlideshowlBinding

class SlideshowlFragment : Fragment() {

    private var _binding: FragmentSlideshowlBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowlViewModel = ViewModelProvider(this)[SlideshowlViewModel::class.java]

        _binding = FragmentSlideshowlBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 隐藏fab
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        if (fab.isVisible) {
            fab.hide()
        }



        // 获取 SearchView 对象并进行相关操作
        val searchView = binding.searchView
        searchView.onActionViewExpanded()
        searchView.clearFocus()
        //关闭搜索框

        searchView.setOnCloseListener { // 处理叉叉点击事件

            searchView.setQuery("", false)

            searchView.clearFocus()
            // 隐藏输入法
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchView.windowToken, 0)

            true
        }


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

                 }

                return true
            }
        })


        // 获取 tabTd 对象并添加选项卡
        val tabTd: TabLayout = binding.tabLayout
        tabTd.addTab(tabTd.newTab().setText("免费").setTag(0))
        tabTd.addTab(tabTd.newTab().setText("付费").setTag(1))


        // 添加分类选项卡的选择监听器
        tabTd.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 记录选中的tab
           }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 释放binding
        _binding = null
    }
}