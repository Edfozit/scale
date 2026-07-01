package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.data.WeightRecord
import com.example.myapplication.databinding.FragmentRecordsBinding
import com.example.myapplication.tools.ThemeHelper
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import java.util.Locale

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    // 排便圆圈状态（Dialog 内临时使用）
    private val bowelStates = BooleanArray(7)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupCardClickListeners()
        observeRecentRecords()
        binding.root.setBackgroundColor(ThemeHelper.getThemeBgColor(requireContext()))
    }

    // ==================== 卡片点击监听 ====================

    private fun setupCardClickListeners() {
        // 设置按钮
        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.feature_developing), Toast.LENGTH_SHORT).show()
        }

        // FAB 添加体重
        binding.fabAdd.setOnClickListener { showAddRecordDialog() }

        // 饮食 → 跳转独立饮食记录页面
        binding.cardDiet.setOnClickListener {
            val date = viewModel.selectedDate.value ?: return@setOnClickListener
            val intent = Intent(requireContext(), DietActivity::class.java).apply {
                putExtra(DietActivity.EXTRA_DATE, date)
            }
            startActivity(intent)
        }

        // 饮水
        binding.cardWater.setOnClickListener { showWaterDialog() }

        // 日记
        binding.cardDiary.setOnClickListener { showDiaryDialog() }

        // 围度
        binding.cardMeasurement.setOnClickListener { showMeasurementDialog() }

        // 饮食计划
        binding.cardDietPlan.setOnClickListener { showDietPlanDialog() }

        // 形体照
        binding.cardBodyPhoto.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.feature_developing), Toast.LENGTH_SHORT).show()
        }

        // 排便
        binding.cardBowel.setOnClickListener { showBowelDialog() }

        // 运动
        binding.cardExercise.setOnClickListener { showExerciseDialog() }

        // 减肥小队
        binding.cardWeightTeam.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.feature_developing), Toast.LENGTH_SHORT).show()
        }

        // 习惯
        binding.cardHabit.setOnClickListener { showHabitDialog() }
    }

    // ==================== 动态状态文本绑定 ====================

    private fun observeRecentRecords() {
        viewModel.recentRecords.observe(viewLifecycleOwner) { records ->
            updateCardStatusText(records)
            updateBowelCircles(records)
        }
    }

    private fun updateCardStatusText(records: List<WeightRecord>) {
        // 饮食：统计有 foodNote 的记录数
        val dietCount = records.count { !it.foodNote.isNullOrBlank() }
        binding.cardDiet.getChildAt(1).let { spacer ->
            val parent = spacer.parent as ViewGroup
            val statusText = parent.getChildAt(2) as android.widget.TextView
            statusText.text = if (dietCount > 0)
                getString(R.string.recorded_count, dietCount)
            else
                getString(R.string.no_record_7d)
        }

        // 饮水
        val waterCount = records.count { !it.waterIntake.isNullOrBlank() }
        binding.cardWater.getChildAt(1).let { spacer ->
            val parent = spacer.parent as ViewGroup
            val statusText = parent.getChildAt(2) as android.widget.TextView
            statusText.text = if (waterCount > 0)
                getString(R.string.recorded_count, waterCount)
            else
                getString(R.string.no_record_7d)
        }

        // 日记
        val diaryCount = records.count { !it.diaryNote.isNullOrBlank() }
        binding.cardDiary.getChildAt(1).let { spacer ->
            val parent = spacer.parent as ViewGroup
            val statusText = parent.getChildAt(2) as android.widget.TextView
            statusText.text = if (diaryCount > 0)
                getString(R.string.recorded_count, diaryCount)
            else
                getString(R.string.no_record_7d)
        }

        // 围度
        val measurementCount = records.count { !it.measurementNote.isNullOrBlank() }
        binding.cardMeasurement.getChildAt(1).let { spacer ->
            val parent = spacer.parent as ViewGroup
            val statusText = parent.getChildAt(2) as android.widget.TextView
            statusText.text = if (measurementCount > 0)
                getString(R.string.recorded_count, measurementCount)
            else
                getString(R.string.no_record_7d)
        }

        // 饮食计划
        val dietPlanCount = records.count { !it.dietPlan.isNullOrBlank() }
        binding.cardDietPlan.getChildAt(1).let { spacer ->
            val parent = spacer.parent as ViewGroup
            val statusText = parent.getChildAt(2) as android.widget.TextView
            statusText.text = if (dietPlanCount > 0)
                getString(R.string.recorded_count, dietPlanCount)
            else
                getString(R.string.no_diet_plan)
        }

        // 运动
        val exerciseCount = records.count { !it.exerciseNote.isNullOrBlank() }
        binding.cardExercise.getChildAt(1).let { spacer ->
            val parent = spacer.parent as ViewGroup
            val statusText = parent.getChildAt(2) as android.widget.TextView
            statusText.text = if (exerciseCount > 0)
                getString(R.string.recorded_count, exerciseCount)
            else
                getString(R.string.no_record_7d)
        }

        // 习惯
        val habitCount = records.count { !it.habitNote.isNullOrBlank() }
        binding.cardHabit.getChildAt(1).let { spacer ->
            val parent = spacer.parent as ViewGroup
            // 习惯卡片右侧是 ImageView（+按钮），不是 TextView，跳过
        }
    }

    private fun updateBowelCircles(records: List<WeightRecord>) {
        // 排便卡片内的圆圈行是第3个子View（index=2）
        val bowelCard = binding.cardBowel
        val circleRow = bowelCard.getChildAt(2) as? ViewGroup ?: return

        // 获取最近7天的日期
        val cal = Calendar.getInstance()
        for (i in 6 downTo 0) {
            val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -i) }
            val dateStr = viewModel.formatDate(
                dayCal.get(Calendar.YEAR),
                dayCal.get(Calendar.MONTH) + 1,
                dayCal.get(Calendar.DAY_OF_MONTH)
            )
            val record = records.find { it.date == dateStr }
            val hasRecord = record != null && !record.bowelRecord.isNullOrBlank()

            val circle = circleRow.getChildAt(i)
            circle.setBackgroundResource(
                if (hasRecord) R.drawable.bg_bowel_circle_filled
                else R.drawable.bg_bowel_circle
            )
        }
    }

    // ==================== 各分类 Dialog ====================

    private fun showDietDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_diet, null)
        val etDiet = dialogView.findViewById<TextInputEditText>(R.id.etDiet)

        // 回填已有数据
        val currentRecord = viewModel.selectedRecord.value
        currentRecord?.foodNote?.let { etDiet.setText(it) }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val text = etDiet.text?.toString()
            saveFieldUpdateRecord { it.copy(foodNote = if (text.isNullOrBlank()) null else text) }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_water, null)
        val etWater = dialogView.findViewById<TextInputEditText>(R.id.etWater)

        val currentRecord = viewModel.selectedRecord.value
        currentRecord?.waterIntake?.let { etWater.setText(it) }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val text = etWater.text?.toString()
            saveFieldUpdateRecord { it.copy(waterIntake = if (text.isNullOrBlank()) null else text) }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDiaryDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_diary, null)
        val etDiary = dialogView.findViewById<TextInputEditText>(R.id.etDiary)

        val currentRecord = viewModel.selectedRecord.value
        currentRecord?.diaryNote?.let { etDiary.setText(it) }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val text = etDiary.text?.toString()
            saveFieldUpdateRecord { it.copy(diaryNote = if (text.isNullOrBlank()) null else text) }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showMeasurementDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_measurement, null)
        val etChest = dialogView.findViewById<TextInputEditText>(R.id.etChest)
        val etWaist = dialogView.findViewById<TextInputEditText>(R.id.etWaist)
        val etHip = dialogView.findViewById<TextInputEditText>(R.id.etHip)
        val etThigh = dialogView.findViewById<TextInputEditText>(R.id.etThigh)
        val etArm = dialogView.findViewById<TextInputEditText>(R.id.etArm)

        // 回填（围度存储在 measurementNote 中，格式: "胸围,腰围,臀围,大腿围,手臂围"）
        val currentRecord = viewModel.selectedRecord.value
        currentRecord?.measurementNote?.let { note ->
            val parts = note.split(",")
            if (parts.size >= 5) {
                if (parts[0].isNotBlank()) etChest.setText(parts[0])
                if (parts[1].isNotBlank()) etWaist.setText(parts[1])
                if (parts[2].isNotBlank()) etHip.setText(parts[2])
                if (parts[3].isNotBlank()) etThigh.setText(parts[3])
                if (parts[4].isNotBlank()) etArm.setText(parts[4])
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val chest = etChest.text?.toString() ?: ""
            val waist = etWaist.text?.toString() ?: ""
            val hip = etHip.text?.toString() ?: ""
            val thigh = etThigh.text?.toString() ?: ""
            val arm = etArm.text?.toString() ?: ""

            val combined = listOf(chest, waist, hip, thigh, arm)
                .joinToString(",")
            val note = if (combined.replace(",", "").isBlank()) null else combined

            saveFieldUpdateRecord { it.copy(measurementNote = note) }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDietPlanDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_diet_plan, null)
        val etDietPlan = dialogView.findViewById<TextInputEditText>(R.id.etDietPlan)

        val currentRecord = viewModel.selectedRecord.value
        currentRecord?.dietPlan?.let { etDietPlan.setText(it) }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val text = etDietPlan.text?.toString()
            saveFieldUpdateRecord { it.copy(dietPlan = if (text.isNullOrBlank()) null else text) }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showExerciseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exercise, null)
        val etExercise = dialogView.findViewById<TextInputEditText>(R.id.etExercise)

        val currentRecord = viewModel.selectedRecord.value
        currentRecord?.exerciseNote?.let { etExercise.setText(it) }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val text = etExercise.text?.toString()
            saveFieldUpdateRecord { it.copy(exerciseNote = if (text.isNullOrBlank()) null else text) }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBowelDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bowel, null)

        // 初始化圆圈状态
        val circleViews = listOf(
            dialogView.findViewById<View>(R.id.circleDay1),
            dialogView.findViewById<View>(R.id.circleDay2),
            dialogView.findViewById<View>(R.id.circleDay3),
            dialogView.findViewById<View>(R.id.circleDay4),
            dialogView.findViewById<View>(R.id.circleDay5),
            dialogView.findViewById<View>(R.id.circleDay6),
            dialogView.findViewById<View>(R.id.circleDay7)
        )

        // 回填已有数据
        val currentRecord = viewModel.selectedRecord.value
        val existingStates = currentRecord?.bowelRecord?.split(",") ?: emptyList()
        for (i in 0 until 7) {
            bowelStates[i] = existingStates.getOrNull(i) == "1"
            circleViews[i].setBackgroundResource(
                if (bowelStates[i]) R.drawable.bg_bowel_circle_filled
                else R.drawable.bg_bowel_circle
            )
        }

        // 点击切换状态
        for (i in 0 until 7) {
            circleViews[i].setOnClickListener {
                bowelStates[i] = !bowelStates[i]
                circleViews[i].setBackgroundResource(
                    if (bowelStates[i]) R.drawable.bg_bowel_circle_filled
                    else R.drawable.bg_bowel_circle
                )
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val record = bowelStates.joinToString(",") { if (it) "1" else "0" }
            saveFieldUpdateRecord { it.copy(bowelRecord = if (record.contains("1")) record else null) }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_habit, null)
        val etHabit = dialogView.findViewById<TextInputEditText>(R.id.etHabit)
        val tvHabitList = dialogView.findViewById<android.widget.TextView>(R.id.tvHabitList)

        // 显示已有习惯
        val currentRecord = viewModel.selectedRecord.value
        currentRecord?.habitNote?.let { note ->
            if (note.isNotBlank()) {
                tvHabitList.text = note
                tvHabitList.visibility = View.VISIBLE
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            val newHabit = etHabit.text?.toString()
            val existing = currentRecord?.habitNote ?: ""
            val combined = if (existing.isNotBlank() && !newHabit.isNullOrBlank()) {
                "$existing\n$newHabit"
            } else if (!newHabit.isNullOrBlank()) {
                newHabit
            } else {
                existing
            }
            saveFieldUpdateRecord { it.copy(habitNote = if (combined.isBlank()) null else combined) }
            dialog.dismiss()
        }
        dialog.show()
    }

    // ==================== 通用保存逻辑 ====================

    /**
     * 基于当前选中日期的记录，通过 transform 函数生成新记录并保存。
     * 如果当天无记录则创建新记录。
     */
    private fun saveFieldUpdateRecord(transform: (WeightRecord) -> WeightRecord) {
        val date = viewModel.selectedDate.value ?: return
        val existing = viewModel.selectedRecord.value
        val record = if (existing != null) {
            transform(existing)
        } else {
            transform(WeightRecord(
                date = date,
                morningWeight = null,
                eveningWeight = null,
                foodNote = null,
                exerciseNote = null
            ))
        }
        viewModel.saveRecord(record)
    }

    // ==================== FAB 添加体重 Dialog ====================

    private fun showAddRecordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_record, null)
        val etMorningWeight = dialogView.findViewById<TextInputEditText>(R.id.etMorningWeight)
        val etEveningWeight = dialogView.findViewById<TextInputEditText>(R.id.etEveningWeight)
        val etFoodNote = dialogView.findViewById<TextInputEditText>(R.id.etFoodNote)
        val etExerciseNote = dialogView.findViewById<TextInputEditText>(R.id.etExerciseNote)

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
