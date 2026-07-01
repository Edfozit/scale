package com.example.myapplication.tools

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.myapplication.R

object ThemeHelper {

    fun getThemeBgColor(context: Context): Int {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val themeColor = prefs.getString("theme_color", "gray") ?: "gray"
        return when (themeColor) {
            "gray" -> ContextCompat.getColor(context, R.color.theme_gray_bg)
            "blue" -> ContextCompat.getColor(context, R.color.theme_blue_bg)
            "yellow" -> ContextCompat.getColor(context, R.color.theme_yellow_bg)
            "pink" -> ContextCompat.getColor(context, R.color.theme_pink_bg)
            else -> ContextCompat.getColor(context, R.color.theme_gray_bg)
        }
    }
}