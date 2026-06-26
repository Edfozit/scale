package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.data.WeightRecord
import com.example.myapplication.databinding.FragmentRecordsBinding

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

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

        binding.fabAdd.setOnClickListener { showAddRecordDialog() }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
