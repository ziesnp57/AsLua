package com.yongle.aslua.ui.reflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yongle.aslua.R
import com.yongle.aslua.databinding.FragmentReflowBinding


class ReflowFragment : Fragment() {

    // 该属性用于绑定视图，仅在 onCreateView 和 onDestroyView 方法之间有效
    private var _binding: FragmentReflowBinding? = null

    private val binding get() = _binding!! // 获取绑定视图


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


        return root // 返回根视图
    }

        override fun onDestroyView() {
            super.onDestroyView()

            _binding = null // 在视图销毁时解除视图绑定
        }
}