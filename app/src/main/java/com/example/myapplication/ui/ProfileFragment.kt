package com.example.myapplication.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogThemePickerBinding
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.tools.ThemeHelper
import com.google.android.material.bottomsheet.BottomSheetDialog

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
        binding.root.setBackgroundColor(ThemeHelper.getThemeBgColor(requireContext()))
        binding.rowTheme.setOnClickListener {
            showThemePickerDialog()
        }
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

    private fun showThemePickerDialog(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding= DialogThemePickerBinding.inflate(layoutInflater)

        dialogBinding.root.background= ContextCompat.getDrawable(
            requireContext(),
            R.drawable.bg_profile_card
        )
        dialog.setContentView(dialogBinding.root)

        val gray=ContextCompat.getColor(requireContext(),R.color.theme_gray)
        val blue=ContextCompat.getColor(requireContext(),R.color.theme_blue)
        val yellow=ContextCompat.getColor(requireContext(),R.color.theme_yellow)
        val pink=ContextCompat.getColor(requireContext(),R.color.theme_pink)
        dialogBinding.colorGray.background=createCircleDrawable(gray,true)
        dialogBinding.colorBlue.background=createCircleDrawable(blue,false)
        dialogBinding.colorYellow.background=createCircleDrawable(yellow,false)
        dialogBinding.colorPink.background=createCircleDrawable(pink,false)
        dialogBinding.colorGray.setOnClickListener {
            dialogBinding.colorGray.background=createCircleDrawable(gray,false)
            dialogBinding.colorBlue.background=createCircleDrawable(blue,false)
            dialogBinding.colorYellow.background=createCircleDrawable(yellow,false)
            dialogBinding.colorPink.background=createCircleDrawable(pink,false)

            dialogBinding.colorGray.background=createCircleDrawable(gray,true)

            saveTheme("gray")

            binding.tvThemeValue.text="黑白灰"

            dialog.dismiss()
        }
        dialogBinding.colorBlue.setOnClickListener {
            dialogBinding.colorGray.background=createCircleDrawable(gray,false)
            dialogBinding.colorBlue.background=createCircleDrawable(blue,false)
            dialogBinding.colorYellow.background=createCircleDrawable(yellow,false)
            dialogBinding.colorPink.background=createCircleDrawable(pink,false)

            dialogBinding.colorBlue.background=createCircleDrawable(blue,true)

            saveTheme("blue")

            binding.tvThemeValue.text="蓝色"

            dialog.dismiss()
        }
        dialogBinding.colorYellow.setOnClickListener {
            dialogBinding.colorGray.background=createCircleDrawable(gray,false)
            dialogBinding.colorBlue.background=createCircleDrawable(blue,false)
            dialogBinding.colorYellow.background=createCircleDrawable(yellow,false)
            dialogBinding.colorPink.background=createCircleDrawable(pink,false)

            dialogBinding.colorYellow.background=createCircleDrawable(yellow,true)

            saveTheme("yellow")

            binding.tvThemeValue.text="黄色"

            dialog.dismiss()

        }
        dialogBinding.colorPink.setOnClickListener {
            dialogBinding.colorGray.background=createCircleDrawable(gray,false)
            dialogBinding.colorBlue.background=createCircleDrawable(blue,false)
            dialogBinding.colorYellow.background=createCircleDrawable(yellow,false)
            dialogBinding.colorPink.background=createCircleDrawable(pink,false)

            dialogBinding.colorPink.background=createCircleDrawable(pink,true)

            saveTheme("pink")

            binding.tvThemeValue.text="粉色"
            dialog.dismiss()

        }

        dialog.show()
    }

    private fun createCircleDrawable(color: Int, selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            if (selected) {
                setStroke(6, Color.BLACK)
            }
            setSize(108, 108)
        }
    }

    private fun saveTheme(theme: String) {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_color", theme).apply()
        (activity as? MainActivity)?.applyThemeColor()
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
