package com.aslua

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 *  LuaRecycler
 *  YONGLE-永乐 2023/6/19
**/

class LuaRecycler(private val adapterCreator: AdapterCreator) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return adapterCreator.getItemCount().toInt()
    }

    override fun getItemViewType(position: Int): Int {
        return adapterCreator.getItemViewType(position).toInt()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapterCreator.onBindViewHolder(holder, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return adapterCreator.onCreateViewHolder(parent, viewType)
    }

    interface AdapterCreator {
        fun getItemCount(): Long

        fun getItemViewType(position: Int): Long

        fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)

        fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    }

    class Holder(var view: View) : RecyclerView.ViewHolder(view)
}