package com.example.myapplication.ui

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.DietEntry
import com.example.myapplication.databinding.ActivityDietBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class DietActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDietBinding
    private lateinit var adapter: DietEntryAdapter
    private lateinit var dao: com.example.myapplication.data.DietEntryDao

    private var selectedDate: String = ""  // 格式: "2026-05-27"
    private var selectedTime: String = ""  // 格式: "08:58"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置状态栏为白色
        window.statusBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        binding = ActivityDietBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传入的日期
        selectedDate = intent.getStringExtra(EXTRA_DATE) ?: run {
            val cal = Calendar.getInstance()
            String.format(
                "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        // 初始化 DAO
        dao = AppDatabase.getInstance(this).dietEntryDao()

        // 设置日期标题
        binding.tvDateHeader.text = selectedDate.replace("-", "/")

        // 设置默认时间（当前时间）
        val now = Calendar.getInstance()
        selectedTime = String.format(
            Locale.CHINA, "%02d:%02d",
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE)
        )

        // 初始化 RecyclerView
        adapter = DietEntryAdapter()
        binding.rvDietEntries.layoutManager = LinearLayoutManager(this)
        binding.rvDietEntries.adapter = adapter

        // 加载数据
        loadDietEntries()

        // 返回按钮
        binding.btnBack.setOnClickListener { finish() }

        // 日历按钮（暂时提示开发中）
        binding.btnCalendar.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_developing), Toast.LENGTH_SHORT).show()
        }

        // FAB 添加饮食
        binding.fabAddDiet.setOnClickListener { showAddDietDialog() }
    }

    private fun loadDietEntries() {
        lifecycleScope.launch {
            val entries = dao.getByDate(selectedDate)
            adapter.submitList(entries)

            // 空状态提示
            binding.tvEmpty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            binding.rvDietEntries.visibility = if (entries.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun showAddDietDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_diet_entry, null)
        val rgMealType = dialogView.findViewById<android.widget.RadioGroup>(R.id.rgMealType)
        val tvSelectedTime = dialogView.findViewById<android.widget.TextView>(R.id.tvSelectedTime)
        val btnPickTime = dialogView.findViewById<View>(R.id.btnPickTime)
        val etFoodContent = dialogView.findViewById<TextInputEditText>(R.id.etFoodContent)

        // 默认选中当前时段对应的餐次
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val defaultMealId = when {
            hour < 10 -> R.id.rbBreakfast
            hour < 14 -> R.id.rbLunch
            hour < 18 -> R.id.rbDinner
            else -> R.id.rbSnack
        }
        rgMealType.check(defaultMealId)

        // 显示默认时间
        tvSelectedTime.text = selectedTime

        // 时间选择
        btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedTime = String.format(Locale.CHINA, "%02d:%02d", hourOfDay, minute)
                    tvSelectedTime.text = selectedTime
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val mealType = when (rgMealType.checkedRadioButtonId) {
                R.id.rbBreakfast -> "breakfast"
                R.id.rbLunch -> "lunch"
                R.id.rbDinner -> "dinner"
                R.id.rbSnack -> "snack"
                else -> "breakfast"
            }

            val foodContent = etFoodContent.text?.toString()
            if (foodContent.isNullOrBlank()) {
                etFoodContent.error = "请输入食物内容"
                return@setOnClickListener
            }

            val entry = DietEntry(
                date = selectedDate,
                mealType = mealType,
                time = selectedTime,
                foodContent = foodContent
            )

            lifecycleScope.launch {
                dao.insert(entry)
                loadDietEntries()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        const val EXTRA_DATE = "extra_date"
    }
}
