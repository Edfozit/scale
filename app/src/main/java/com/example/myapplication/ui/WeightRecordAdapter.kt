package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.WeightRecord
import java.util.Calendar
import java.util.Locale

class WeightRecordAdapter : ListAdapter<WeightRecord, WeightRecordAdapter.ViewHolder>(DIFF_CALLBACK) {

    // 记录前一条数据，用于计算变化量
    private var allRecords: List<WeightRecord> = emptyList()

    fun submitAllRecords(records: List<WeightRecord>) {
        allRecords = records
        submitList(records)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val layoutMorning: LinearLayout = itemView.findViewById(R.id.layoutMorning)
        val tvMorningWeight: TextView = itemView.findViewById(R.id.tvMorningWeight)
        val layoutEvening: LinearLayout = itemView.findViewById(R.id.layoutEvening)
        val tvEveningWeight: TextView = itemView.findViewById(R.id.tvEveningWeight)
        val tvChange: TextView = itemView.findViewById(R.id.tvChange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = getItem(position)
        val context = holder.itemView.context

        // 日期格式：2026年06月12日 五
        holder.tvDate.text = formatDateDisplay(record.date)

        // 早晨体重
        if (record.morningWeight != null) {
            holder.layoutMorning.visibility = View.VISIBLE
            holder.tvMorningWeight.text = formatWeight(record.morningWeight)
        } else {
            holder.layoutMorning.visibility = View.GONE
        }

        // 晚间体重
        if (record.eveningWeight != null) {
            holder.layoutEvening.visibility = View.VISIBLE
            holder.tvEveningWeight.text = formatWeight(record.eveningWeight)
        } else {
            holder.layoutEvening.visibility = View.GONE
        }

        // 计算与前一天的变化量（与上一条记录对比早晨体重）
        val change = calculateChange(position)
        if (change != null) {
            holder.tvChange.visibility = View.VISIBLE
            val absChange = Math.abs(change)
            when {
                change > 0.01f -> {
                    holder.tvChange.text = String.format(Locale.getDefault(), "+ %.1f斤", absChange)
                    holder.tvChange.setTextColor(context.getColor(R.color.weight_gain))
                }
                change < -0.01f -> {
                    holder.tvChange.text = String.format(Locale.getDefault(), "- %.1f斤", absChange)
                    holder.tvChange.setTextColor(context.getColor(R.color.weight_loss))
                }
                else -> {
                    holder.tvChange.text = String.format(Locale.getDefault(), "0.0斤")
                    holder.tvChange.setTextColor(context.getColor(R.color.text_primary))
                }
            }
        } else {
            holder.tvChange.visibility = View.INVISIBLE
        }
    }

    /**
     * 计算当前记录与下一条记录（即时间上的前一天）的变化量
     * 列表是按日期降序排列，position+1 就是时间更早的那条
     */
    private fun calculateChange(position: Int): Float? {
        val current = getItem(position)
        val currentWeight = current.morningWeight ?: current.eveningWeight ?: return null

        // 找下一条（时间更旧的）记录
        if (position + 1 >= allRecords.size) return null
        val prev = allRecords[position + 1]
        val prevWeight = prev.morningWeight ?: prev.eveningWeight ?: return null

        return currentWeight - prevWeight
    }

    private fun formatWeight(weight: Float): String {
        return String.format(Locale.getDefault(), "%.1f斤", weight)
    }

    private fun formatDateDisplay(date: String): String {
        // date 格式: "2026-06-12"
        return try {
            val parts = date.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val cal = Calendar.getInstance()
            cal.set(year, month - 1, day)
            val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "一"
                Calendar.TUESDAY -> "二"
                Calendar.WEDNESDAY -> "三"
                Calendar.THURSDAY -> "四"
                Calendar.FRIDAY -> "五"
                Calendar.SATURDAY -> "六"
                Calendar.SUNDAY -> "日"
                else -> ""
            }
            String.format(Locale.getDefault(), "%d年%02d月%02d日 %s", year, month, day, dayOfWeek)
        } catch (e: Exception) {
            date
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WeightRecord>() {
            override fun areItemsTheSame(oldItem: WeightRecord, newItem: WeightRecord): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: WeightRecord, newItem: WeightRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}
