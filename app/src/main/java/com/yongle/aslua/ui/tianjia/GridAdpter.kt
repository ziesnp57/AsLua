package com.yongle.aslua.ui.tianjia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yongle.aslua.R

class GridAdapter : RecyclerView.Adapter<GridAdapter.GridViewHolder>() {
    private val items: MutableList<GridItem> = mutableListOf()

    // 添加一个点击事件监听器
    private var onRadioButtonClickedListener: ((Int) -> Unit)? = null

    // 设置数据项

    fun setItems(newItems: List<GridItem>) {
        // 添加新数据项
        items.addAll(newItems)
    }


    // 设置点击事件监听器
    fun setOnRadioButtonClickedListener(listener: (Int) -> Unit) {
        onRadioButtonClickedListener = listener
    }

    // 创建视图持有者
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grid, parent, false)
        return GridViewHolder(view)
    }

    // 绑定数据到视图
    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
    }

    // 返回项数
    override fun getItemCount(): Int {
        return items.size
    }

    // 返回数据项
    fun getItems(): List<GridItem> {
        return items
    }

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val radioButton: RadioButton = itemView.findViewById(R.id.radio_button)
        private val textView: TextView = itemView.findViewById(R.id.radio_text)

        // 绑定数据到视图
        fun bind(item: GridItem, position: Int) {
            imageView.setImageResource(item.imageRes)
            radioButton.isChecked = item.isSelected
            textView.text = item.text



            imageView.setOnClickListener {
                onRadioButtonClickedListener?.invoke(position)
                items[position].isSelected = true  // 更新选中项的状态

                for (i in items.indices) {
                    items[i].isSelected = i == position
                }

                notifyDataSetChanged()  // 通知适配器数据已更新
            }

        }
    }
}

data class GridItem(val imageRes: Int, val text: String, var isSelected: Boolean = false)
