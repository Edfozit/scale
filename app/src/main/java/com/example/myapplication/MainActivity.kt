package com.example.myapplication

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.CalendarFragment
import com.example.myapplication.ui.ChartFragment
import com.example.myapplication.ui.ProfileFragment
import com.example.myapplication.ui.RecordsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Fragment 实例（懒加载缓存，避免重建）
    private val calendarFragment by lazy { CalendarFragment() }
    private val chartFragment by lazy { ChartFragment() }
    private val recordsFragment by lazy { RecordsFragment() }
    private val profileFragment by lazy { ProfileFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 设置状态栏为白色
        window.statusBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyThemeColor()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // 默认显示 Fragment1（日历/记录页）
        if (savedInstanceState == null) {
            showFragment(calendarFragment)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_record -> { showFragment(calendarFragment); true }
                R.id.nav_chart -> { showFragment(chartFragment); true }
                R.id.nav_calendar -> { showFragment(recordsFragment); true }
                R.id.nav_profile -> { showFragment(profileFragment); true }
                else -> false
            }
        }

        // 默认选中第一个 tab
        binding.bottomNavigation.selectedItemId = R.id.nav_record
    }

    private fun showFragment(fragment: Fragment) {
        val tag = fragment::class.java.simpleName
        val ft = supportFragmentManager.beginTransaction()

        // 先隐藏所有已添加的 fragment
        supportFragmentManager.fragments.forEach { ft.hide(it) }

        if (!fragment.isAdded) {
            ft.add(R.id.fragmentContainer, fragment, tag)
        } else {
            ft.show(fragment)
        }
        ft.commitNow()
    }

    fun applyThemeColor() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val themeColor = prefs.getString("theme_color", "gray") ?: "gray"

        val bgColor = when (themeColor) {
            "gray" -> ContextCompat.getColor(this, R.color.theme_gray_bg)
            "blue" -> ContextCompat.getColor(this, R.color.theme_blue_bg)
            "yellow" -> ContextCompat.getColor(this, R.color.theme_yellow_bg)
            "pink" -> ContextCompat.getColor(this, R.color.theme_pink_bg)
            else -> ContextCompat.getColor(this, R.color.theme_gray_bg)
        }
        val color = when (themeColor) {
            "gray" -> ContextCompat.getColor(this, R.color.theme_gray)
            "blue" -> ContextCompat.getColor(this, R.color.theme_blue)
            "yellow" -> ContextCompat.getColor(this, R.color.theme_yellow)
            "pink" -> ContextCompat.getColor(this, R.color.theme_pink)
            else -> ContextCompat.getColor(this, R.color.theme_gray)
        }

        binding.bottomNavigation.itemIconTintList = ColorStateList.valueOf(color)
        binding.bottomNavigation.itemTextColor = ColorStateList.valueOf(color)
        // 改 MainActivity 根布局背景
        binding.main.setBackgroundColor(bgColor)
    }
}
