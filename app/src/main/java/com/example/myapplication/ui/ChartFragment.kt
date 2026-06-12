package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentChartBinding

class ChartFragment : Fragment() {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChartViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChartViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.rangeLabel.observe(viewLifecycleOwner) { label ->
            binding.tvRangeLabel.text = label
        }

        viewModel.records.observe(viewLifecycleOwner) { records ->
            binding.lineChartView.setRecords(records)
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            if (stats != null) {
                binding.tvMaxWeight.text = String.format("%.1f 斤", stats.maxWeight)
                binding.tvMinWeight.text = String.format("%.1f 斤", stats.minWeight)
                binding.tvAvgWeight.text = String.format("%.1f 斤", stats.avgWeight)
                binding.tvRecordDays.text = "${stats.recordDays} 天"

                val delta = stats.totalDelta
                val deltaStr = if (delta > 0) {
                    String.format("+ %.1f 斤", delta)
                } else if (delta < 0) {
                    String.format("- %.1f 斤", -delta)
                } else {
                    String.format("%.1f 斤", 0f)
                }
                binding.tvTotalDelta.text = deltaStr

                val color = if (delta <= 0) {
                    requireContext().getColor(com.example.myapplication.R.color.weight_loss)
                } else {
                    requireContext().getColor(com.example.myapplication.R.color.weight_gain)
                }
                binding.tvTotalDelta.setTextColor(color)

                binding.tvGoalDistance.text = "暂无"
            } else {
                binding.tvMaxWeight.text = "-- 斤"
                binding.tvMinWeight.text = "-- 斤"
                binding.tvAvgWeight.text = "-- 斤"
                binding.tvRecordDays.text = "0 天"
                binding.tvTotalDelta.text = "-- 斤"
                binding.tvGoalDistance.text = "暂无"
            }
        }
    }

    private fun setupListeners() {
        binding.layoutRangeSelector.setOnClickListener {
            showRangeDialog()
        }
    }

    private fun showRangeDialog() {
        val options = arrayOf("最近 7 天", "最近 30 天", "最近 90 天", "最近 180 天")
        val days = intArrayOf(7, 30, 90, 180)

        AlertDialog.Builder(requireContext())
            .setTitle("选择时间范围")
            .setItems(options) { _, which ->
                viewModel.loadData(days[which])
            }
            .show()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.loadData(viewModel.currentDays)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
