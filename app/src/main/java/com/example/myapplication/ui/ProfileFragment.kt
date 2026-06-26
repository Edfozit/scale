package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDecimalSegment()
        setupUnitSegment()
    }

    /** 默认小数点分段控件 */
    private fun setupDecimalSegment() {
        val tvDecimal0 = binding.tvDecimal0
        val tvDecimal00 = binding.tvDecimal00

        tvDecimal0.setOnClickListener {
            tvDecimal0.setBackgroundResource(R.drawable.bg_segment_selected)
            tvDecimal0.setTextColor(resources.getColor(R.color.profile_text_primary, null))
            tvDecimal00.background = null
            tvDecimal00.setTextColor(resources.getColor(R.color.profile_text_secondary, null))
        }

        tvDecimal00.setOnClickListener {
            tvDecimal00.setBackgroundResource(R.drawable.bg_segment_selected)
            tvDecimal00.setTextColor(resources.getColor(R.color.profile_text_primary, null))
            tvDecimal0.background = null
            tvDecimal0.setTextColor(resources.getColor(R.color.profile_text_secondary, null))
        }
    }

    /** 默认单位分段控件 */
    private fun setupUnitSegment() {
        val tvUnitKg = binding.tvUnitKg
        val tvUnitJin = binding.tvUnitJin

        tvUnitKg.setOnClickListener {
            tvUnitKg.setBackgroundResource(R.drawable.bg_segment_selected)
            tvUnitKg.setTextColor(resources.getColor(R.color.profile_text_primary, null))
            tvUnitJin.background = null
            tvUnitJin.setTextColor(resources.getColor(R.color.profile_text_secondary, null))
        }

        tvUnitJin.setOnClickListener {
            tvUnitJin.setBackgroundResource(R.drawable.bg_segment_selected)
            tvUnitJin.setTextColor(resources.getColor(R.color.profile_text_primary, null))
            tvUnitKg.background = null
            tvUnitKg.setTextColor(resources.getColor(R.color.profile_text_secondary, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
