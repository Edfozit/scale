package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.WeightRecord
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.currentYear.observe(this) { year ->
            val month = viewModel.currentMonth.value ?: return@observe
            updateMonthTitle(year, month)
            binding.calendarView.setYearMonth(year, month)
        }

        viewModel.currentMonth.observe(this) { month ->
            val year = viewModel.currentYear.value ?: return@observe
            updateMonthTitle(year, month)
            binding.calendarView.setYearMonth(year, month)
        }

        viewModel.selectedDate.observe(this) { date ->
            binding.calendarView.setSelectedDate(date)
        }

        viewModel.monthlyRecords.observe(this) { records ->
            binding.calendarView.setRecords(records)
        }

        viewModel.selectedRecord.observe(this) { record ->
            updateRecordSection(record)
        }

        viewModel.monthlyStats.observe(this) { stats ->
            binding.tvGainDays.text = "${stats.gainDays} 天"
            binding.tvUnchangedDays.text = "${stats.unchangedDays} 天"
            binding.tvLossDays.text = "${stats.lossDays} 天"

            val changeStr = if (stats.totalChange > 0) {
                String.format("+%.1f 斤", stats.totalChange)
            } else {
                String.format("%.1f 斤", stats.totalChange)
            }
            binding.tvTotalChange.text = changeStr
        }
    }

    private fun setupListeners() {
        binding.btnPrevMonth.setOnClickListener {
            viewModel.prevMonth()
        }

        binding.btnNextMonth.setOnClickListener {
            viewModel.nextMonth()
        }

        binding.calendarView.onDateSelected = { date ->
            viewModel.selectDate(date)
        }

        binding.fabAdd.setOnClickListener {
            showAddRecordDialog()
        }
    }

    private fun updateMonthTitle(year: Int, month: Int) {
        val title = viewModel.formatMonthTitle(year, month)
        binding.tvMonthTitle.text = title
        binding.tvSummaryMonth.text = title
    }

    private fun updateRecordSection(record: WeightRecord?) {
        if (record != null) {
            // 早
            if (record.morningWeight != null) {
                binding.tvMorningWeight.text = "早 ${String.format("%.1f", record.morningWeight)}"
                binding.tvMorningWeight.setTextColor(getColor(R.color.text_primary))
            } else {
                binding.tvMorningWeight.text = "早 --"
                binding.tvMorningWeight.setTextColor(getColor(R.color.text_hint))
            }

            // 中午体重已移除

            // 晚
            if (record.eveningWeight != null) {
                binding.tvEveningWeight.text = "晚 ${String.format("%.1f", record.eveningWeight)}"
                binding.tvEveningWeight.setTextColor(getColor(R.color.text_primary))
            } else {
                binding.tvEveningWeight.text = "晚 --"
                binding.tvEveningWeight.setTextColor(getColor(R.color.text_hint))
            }

            // 饮食
            if (!record.foodNote.isNullOrBlank()) {
                binding.tvFoodRecord.text = record.foodNote
                binding.tvFoodRecord.setTextColor(getColor(R.color.text_primary))
            } else {
                binding.tvFoodRecord.text = getString(R.string.no_food_record)
                binding.tvFoodRecord.setTextColor(getColor(R.color.text_hint))
            }

            // 运动
            if (!record.exerciseNote.isNullOrBlank()) {
                binding.tvExerciseRecord.text = record.exerciseNote
                binding.tvExerciseRecord.setTextColor(getColor(R.color.text_primary))
            } else {
                binding.tvExerciseRecord.text = getString(R.string.no_exercise_record)
                binding.tvExerciseRecord.setTextColor(getColor(R.color.text_hint))
            }
        } else {
            binding.tvMorningWeight.text = "早 --"
            binding.tvMorningWeight.setTextColor(getColor(R.color.text_hint))
            binding.tvEveningWeight.text = "晚 --"
            binding.tvEveningWeight.setTextColor(getColor(R.color.text_hint))
            binding.tvFoodRecord.text = getString(R.string.no_food_record)
            binding.tvFoodRecord.setTextColor(getColor(R.color.text_hint))
            binding.tvExerciseRecord.text = getString(R.string.no_exercise_record)
            binding.tvExerciseRecord.setTextColor(getColor(R.color.text_hint))
        }
    }

    private fun showAddRecordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null)
        val etMorningWeight = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMorningWeight)
        val etEveningWeight = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEveningWeight)
        val etFoodNote = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etFoodNote)
        val etExerciseNote = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etExerciseNote)

        // 如果已有记录，预填充
        val currentRecord = viewModel.selectedRecord.value
        if (currentRecord != null) {
            currentRecord.morningWeight?.let { etMorningWeight.setText(String.format("%.1f", it)) }
            currentRecord.eveningWeight?.let { etEveningWeight.setText(String.format("%.1f", it)) }
            currentRecord.foodNote?.let { etFoodNote.setText(it) }
            currentRecord.exerciseNote?.let { etExerciseNote.setText(it) }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val morningStr = etMorningWeight.text?.toString()
            val eveningStr = etEveningWeight.text?.toString()
            val foodStr = etFoodNote.text?.toString()
            val exerciseStr = etExerciseNote.text?.toString()

            val morningWeight = morningStr?.toFloatOrNull()
            val eveningWeight = eveningStr?.toFloatOrNull()

            if (morningWeight == null && eveningWeight == null) {
                etMorningWeight.error = "请至少输入一项体重"
                return@setOnClickListener
            }

            val date = viewModel.selectedDate.value ?: return@setOnClickListener
            val record = WeightRecord(
                date = date,
                morningWeight = morningWeight,
                eveningWeight = eveningWeight,
                foodNote = if (foodStr.isNullOrBlank()) null else foodStr,
                exerciseNote = if (exerciseStr.isNullOrBlank()) null else exerciseStr
            )
            viewModel.saveRecord(record)
            dialog.dismiss()
        }

        dialog.show()
    }
}
