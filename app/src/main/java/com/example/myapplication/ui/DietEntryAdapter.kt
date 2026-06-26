package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.DietEntry
import com.example.myapplication.databinding.ItemDietEntryBinding

class DietEntryAdapter : ListAdapter<DietEntry, DietEntryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDietEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemDietEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DietEntry) {
            // 餐次显示中文
            val mealTypeLabel = when (entry.mealType) {
                "breakfast" -> "早餐"
                "lunch" -> "午餐"
                "dinner" -> "晚餐"
                "snack" -> "加餐"
                else -> entry.mealType
            }
            binding.tvMealType.text = mealTypeLabel
            binding.tvTime.text = entry.time
            binding.tvFoodContent.text = entry.foodContent
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DietEntry>() {
            override fun areItemsTheSame(oldItem: DietEntry, newItem: DietEntry): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DietEntry, newItem: DietEntry): Boolean {
                return oldItem == newItem
            }
        }
    }
}
