package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.data.WeightRecord
import com.example.myapplication.databinding.FragmentCalendarBinding
import com.example.myapplication.tools.ThemeHelper

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: WeightRecordAdapter

    private var isListTabActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupListeners()
        switchTab(false)
        binding.root.setBackgroundColor(ThemeHelper.getThemeBgColor(requireContext()))
    }

    private fun setupRecyclerView() {
        adapter = WeightRecordAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.currentYear.observe(viewLifecycleOwner) { year ->
            val month = viewModel.currentMonth.value ?: return@observe
            updateMonthTitle(year, month)
            binding.calendarView.setYearMonth(year, month)
        }

        viewModel.currentMonth.observe(viewLifecycleOwner) { month ->
            val year = viewModel.currentYear.value ?: return@observe
            updateMonthTitle(year, month)
            binding.calendarView.setYearMonth(year, month)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            binding.calendarView.setSelectedDate(date)
        }

        viewModel.monthlyRecords.observe(viewLifecycleOwner) { records ->
            binding.calendarView.setRecords(records)
        }

        viewModel.selectedRecord.observe(viewLifecycleOwner) { record ->
            updateRecordSection(record)
        }

        viewModel.monthlyStats.observe(viewLifecycleOwner) { stats ->
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

        viewModel.allRecords.observe(viewLifecycleOwner) { records ->
            adapter.submitAllRecords(records)
        }
    }

    private fun setupListeners() {
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }

        binding.calendarView.onDateSelected = { date ->
            viewModel.selectDate(date)
        }

        binding.fabAdd.setOnClickListener { showAddRecordDialog() }

        binding.tabList.setOnClickListener { switchTab(true) }
        binding.tabCalendar.setOnClickListener { switchTab(false) }
    }

    private fun switchTab(showList: Boolean) {
        isListTabActive = showList
        if (showList) {
            binding.tabList.setTextColor(requireContext().getColor(R.color.text_primary))
            binding.tabList.textSize = 16f
            binding.tabList.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.tabCalendar.setTextColor(requireContext().getColor(R.color.text_secondary))
            binding.tabCalendar.textSize = 16f
            binding.tabCalendar.setTypeface(null, android.graphics.Typeface.NORMAL)
            binding.recyclerView.visibility = View.VISIBLE
            binding.layoutCalendar.visibility = View.GONE
            viewModel.loadAllRecords()
        } else {
            binding.tabCalendar.setTextColor(requireContext().getColor(R.color.text_primary))
            binding.tabCalendar.textSize = 16f
            binding.tabCalendar.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.tabList.setTextColor(requireContext().getColor(R.color.text_secondary))
            binding.tabList.textSize = 16f
            binding.tabList.setTypeface(null, android.graphics.Typeface.NORMAL)
            binding.recyclerView.visibility = View.GONE
            binding.layoutCalendar.visibility = View.VISIBLE
        }
    }

    private fun updateMonthTitle(year: Int, month: Int) {
        val title = viewModel.formatMonthTitle(year, month)
        binding.tvMonthTitle.text = title
        binding.tvSummaryMonth.text = title
    }

    private fun updateRecordSection(record: WeightRecord?) {
        if (record != null) {
            if (record.morningWeight != null) {
                binding.tvMorningWeight.text = "早 ${String.format("%.1f", record.morningWeight)}"
                binding.tvMorningWeight.setTextColor(requireContext().getColor(R.color.text_primary))
            } else {
                binding.tvMorningWeight.text = "早 --"
                binding.tvMorningWeight.setTextColor(requireContext().getColor(R.color.text_hint))
            }
            if (record.eveningWeight != null) {
                binding.tvEveningWeight.text = "晚 ${String.format("%.1f", record.eveningWeight)}"
                binding.tvEveningWeight.setTextColor(requireContext().getColor(R.color.text_primary))
            } else {
                binding.tvEveningWeight.text = "晚 --"
                binding.tvEveningWeight.setTextColor(requireContext().getColor(R.color.text_hint))
            }
            if (!record.foodNote.isNullOrBlank()) {
                binding.tvFoodRecord.text = record.foodNote
                binding.tvFoodRecord.setTextColor(requireContext().getColor(R.color.text_primary))
            } else {
                binding.tvFoodRecord.text = getString(R.string.no_food_record)
                binding.tvFoodRecord.setTextColor(requireContext().getColor(R.color.text_hint))
            }
            if (!record.exerciseNote.isNullOrBlank()) {
                binding.tvExerciseRecord.text = record.exerciseNote
                binding.tvExerciseRecord.setTextColor(requireContext().getColor(R.color.text_primary))
            } else {
                binding.tvExerciseRecord.text = getString(R.string.no_exercise_record)
                binding.tvExerciseRecord.setTextColor(requireContext().getColor(R.color.text_hint))
            }
        } else {
            binding.tvMorningWeight.text = "早 --"
            binding.tvMorningWeight.setTextColor(requireContext().getColor(R.color.text_hint))
            binding.tvEveningWeight.text = "晚 --"
            binding.tvEveningWeight.setTextColor(requireContext().getColor(R.color.text_hint))
            binding.tvFoodRecord.text = getString(R.string.no_food_record)
            binding.tvFoodRecord.setTextColor(requireContext().getColor(R.color.text_hint))
            binding.tvExerciseRecord.text = getString(R.string.no_exercise_record)
            binding.tvExerciseRecord.setTextColor(requireContext().getColor(R.color.text_hint))
        }
    }

    private fun showAddRecordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_record, null)
        val etMorningWeight = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMorningWeight)
        val etEveningWeight = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEveningWeight)
        val etFoodNote = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etFoodNote)
        val etExerciseNote = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etExerciseNote)

        val currentRecord = viewModel.selectedRecord.value
        if (currentRecord != null) {
            currentRecord.morningWeight?.let { etMorningWeight.setText(String.format("%.1f", it)) }
            currentRecord.eveningWeight?.let { etEveningWeight.setText(String.format("%.1f", it)) }
            currentRecord.foodNote?.let { etFoodNote.setText(it) }
            currentRecord.exerciseNote?.let { etExerciseNote.setText(it) }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }

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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            binding.root.setBackgroundColor(ThemeHelper.getThemeBgColor(requireContext()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
