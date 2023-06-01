package com.yongle.aslua.ui.slideshow.liulan.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.yongle.aslua.R

// 定义一个数组，存储 Tab 的标题
private val TAB_TITLES = arrayOf(
    R.string.tab_text_4, // 第一个 Tab 的标题
    R.string.tab_text_5 // 第二个 Tab 的标题
)

// 定义一个 SectionsPagerAdapter 类，继承自 FragmentPagerAdapter
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    // 重写 getItem 方法，返回对应位置的 Fragment
    override fun getItem(position: Int): Fragment {

        return if (position == 0) PlaceholderFragment.newInstance()
        else PlaceholdersFragment.newInstance()
    }


    // 重写 getPageTitle 方法，返回对应位置的 Tab 标题
    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    // 重写 getCount 方法，返回 Tab 的数量
    override fun getCount(): Int {
        return 2
    }
}
