package com.yongle.aslua.ui.slideshowl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        val textView: TextView = binding.textSlideshowl
        slideshowlViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 释放binding
        _binding = null
    }
}