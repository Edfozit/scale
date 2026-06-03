package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.WeightRecord
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.WeightCalendarView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var calendarView: WeightCalendarView
    private lateinit var tvMonthTitle: TextView
    private lateinit var tvSummaryMonth: TextView
    private lateinit var tvGainDays: TextView
    private lateinit var tvUnchangedDays: TextView
    private lateinit var tvLossDays: TextView
    private lateinit var tvTotalChange: TextView
    private lateinit var tvMorningWeight: TextView
    private lateinit var tvEveningWeight: TextView
    private lateinit var tvFoodRecord: TextView
    private lateinit var tvExerciseRecord: TextView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initViews()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        calendarView = findViewById(R.id.calendarView)
        tvMonthTitle = findViewById(R.id.tvMonthTitle)
        tvSummaryMonth = findViewById(R.id.tvSummaryMonth)
        tvGainDays = findViewById(R.id.tvGainDays)
        tvUnchangedDays = findViewById(R.id.tvUnchangedDays)
        tvLossDays = findViewById(R.id.tvLossDays)
        tvTotalChange = findViewById(R.id.tvTotalChange)
        tvMorningWeight = findViewById(R.id.tvMorningWeight)
        tvEveningWeight = findViewById(R.id.tvEveningWeight)
        tvFoodRecord = findViewById(R.id.tvFoodRecord)
        tvExerciseRecord = findViewById(R.id.tvExerciseRecord)
        fabAdd = findViewById(R.id.fabAdd)
    }

    private fun setupObservers() {
        viewModel.currentYear.observe(this) { year ->
            val month = viewModel.currentMonth.value ?: return@observe
            updateMonthTitle(year, month)
            calendarView.setYearMonth(year, month)
        }

        viewModel.currentMonth.observe(this) { month ->
            val year = viewModel.currentYear.value ?: return@observe
            updateMonthTitle(year, month)
            calendarView.setYearMonth(year, month)
        }

        viewModel.selectedDate.observe(this) { date ->
            calendarView.setSelectedDate(date)
        }

        viewModel.monthlyRecords.observe(this) { records ->
            calendarView.setRecords(records)
        }

        viewModel.selectedRecord.observe(this) { record ->
            updateRecordSection(record)
        }

        viewModel.monthlyStats.observe(this) { stats ->
            tvGainDays.text = "${stats.gainDays} 天"
            tvUnchangedDays.text = "${stats.unchangedDays} 天"
            tvLossDays.text = "${stats.lossDays} 天"

            val changeStr = if (stats.totalChange > 0) {
                String.format("+%.1f 斤", stats.totalChange)
            } else {
                String.format("%.1f 斤", stats.totalChange)
            }
            tvTotalChange.text = changeStr
        }
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnPrevMonth).setOnClickListener {
            viewModel.prevMonth()
        }

        findViewById<View>(R.id.btnNextMonth).setOnClickListener {
            viewModel.nextMonth()
        }

        calendarView.onDateSelected = { date ->
            viewModel.selectDate(date)
        }

        fabAdd.setOnClickListener {
            showAddRecordDialog()
        }
    }

    private fun updateMonthTitle(year: Int, month: Int) {
        val title = viewModel.formatMonthTitle(year, month)
        tvMonthTitle.text = title
        tvSummaryMonth.text = title
    }

    private fun updateRecordSection(record: WeightRecord?) {
        if (record != null) {
            // 早
            if (record.morningWeight != null) {
                tvMorningWeight.text = "早 ${String.format("%.1f", record.morningWeight)}"
                tvMorningWeight.setTextColor(getColor(R.color.text_primary))
            } else {
                tvMorningWeight.text = "早 --"
                tvMorningWeight.setTextColor(getColor(R.color.text_hint))
            }

            // 中午体重已移除

            // 晚
            if (record.eveningWeight != null) {
                tvEveningWeight.text = "晚 ${String.format("%.1f", record.eveningWeight)}"
                tvEveningWeight.setTextColor(getColor(R.color.text_primary))
            } else {
                tvEveningWeight.text = "晚 --"
                tvEveningWeight.setTextColor(getColor(R.color.text_hint))
            }

            // 饮食
            if (!record.foodNote.isNullOrBlank()) {
                tvFoodRecord.text = record.foodNote
                tvFoodRecord.setTextColor(getColor(R.color.text_primary))
            } else {
                tvFoodRecord.text = getString(R.string.no_food_record)
                tvFoodRecord.setTextColor(getColor(R.color.text_hint))
            }

            // 运动
            if (!record.exerciseNote.isNullOrBlank()) {
                tvExerciseRecord.text = record.exerciseNote
                tvExerciseRecord.setTextColor(getColor(R.color.text_primary))
            } else {
                tvExerciseRecord.text = getString(R.string.no_exercise_record)
                tvExerciseRecord.setTextColor(getColor(R.color.text_hint))
            }
        } else {
            tvMorningWeight.text = "早 --"
            tvMorningWeight.setTextColor(getColor(R.color.text_hint))
            tvEveningWeight.text = "晚 --"
            tvEveningWeight.setTextColor(getColor(R.color.text_hint))
            tvFoodRecord.text = getString(R.string.no_food_record)
            tvFoodRecord.setTextColor(getColor(R.color.text_hint))
            tvExerciseRecord.text = getString(R.string.no_exercise_record)
            tvExerciseRecord.setTextColor(getColor(R.color.text_hint))
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
