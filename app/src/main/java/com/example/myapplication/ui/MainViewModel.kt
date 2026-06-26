package com.example.myapplication.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.WeightRecord
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class MonthlyStats(
    val gainDays: Int,
    val unchangedDays: Int,
    val lossDays: Int,
    val totalChange: Float
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).weightRecordDao()

    private val _currentYear = MutableLiveData(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: LiveData<Int> = _currentYear

    private val _currentMonth = MutableLiveData(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val currentMonth: LiveData<Int> = _currentMonth

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _monthlyRecords = MutableLiveData<Map<String, WeightRecord>>()
    val monthlyRecords: LiveData<Map<String, WeightRecord>> = _monthlyRecords

    private val _selectedRecord = MutableLiveData<WeightRecord?>()
    val selectedRecord: LiveData<WeightRecord?> = _selectedRecord

    private val _monthlyStats = MutableLiveData<MonthlyStats>()
    val monthlyStats: LiveData<MonthlyStats> = _monthlyStats

    private val _allRecords = MutableLiveData<List<WeightRecord>>()
    val allRecords: LiveData<List<WeightRecord>> = _allRecords

    private val _recentRecords = MutableLiveData<List<WeightRecord>>()
    val recentRecords: LiveData<List<WeightRecord>> = _recentRecords

    init {
        val today = formatDate(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH) + 1,
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        _selectedDate.value = today
        loadMonthData()
        loadAllRecords()
        loadRecentRecords()
    }

    fun setYearMonth(year: Int, month: Int) {
        _currentYear.value = year
        _currentMonth.value = month
        loadMonthData()
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        updateSelectedRecord()
    }

    fun prevMonth() {
        val y = _currentYear.value ?: return
        val m = _currentMonth.value ?: return
        if (m == 1) {
            setYearMonth(y - 1, 12)
        } else {
            setYearMonth(y, m - 1)
        }
    }

    fun nextMonth() {
        val y = _currentYear.value ?: return
        val m = _currentMonth.value ?: return
        if (m == 12) {
            setYearMonth(y + 1, 1)
        } else {
            setYearMonth(y, m + 1)
        }
    }

    fun saveRecord(record: WeightRecord) {
        viewModelScope.launch {
            dao.insert(record)
            loadMonthData()
            loadAllRecords()
            loadRecentRecords()
            updateSelectedRecord()
        }
    }

    private fun loadMonthData() {
        // 取出当前年月，如果为空就直接返回
        val y = _currentYear.value ?: return
        val m = _currentMonth.value ?: return

        // 算出月初日期，如 "2026-06-01"
        val startDate = formatDate(y, m, 1)
        // 算出月末是几号，如6月有30天
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        // 算出月末日期，如 "2026-06-30"
        val endDate = formatDate(y, m, lastDay)

        viewModelScope.launch {
            // ②-1: 从数据库查出当月所有记录，如 [1号记录, 2号记录, 3号记录...]
            val records = dao.getByDateRange(startDate, endDate)

            // 把 List 转成 Map，以日期为 key，方便日历 View 快速查找
            // 转换前: [记录(date="06-01"), 记录(date="06-02"), ...]  → 要遍历才能找
            // 转换后: {"06-01": 记录1, "06-02": 记录2, ...}          → 直接用日期取
            val map = records.associateBy { it.date }

            // ②-2: 通知1 —— 当月记录变了，日历View会收到通知重绘
            _monthlyRecords.postValue(map)

            // ②-3: 算统计（增几天、减几天、总共变多少），算完会发通知2
            calculateStats(records, lastDay)

            // 更新选中日期的记录，会发通知3
            updateSelectedRecord()
        }
    }

    private fun updateSelectedRecord() {
        // 取出当前选中的日期，如 "2026-06-03"
        val date = _selectedDate.value ?: return
        viewModelScope.launch {
            // 从数据库查这一天的记录
            val record = dao.getByDate(date)
            // 通知3 —— 选中日期的记录变了，首页卡片会更新显示
            _selectedRecord.postValue(record)
        }
    }

    private fun calculateStats(records: List<WeightRecord>, lastDay: Int) {
        // 按日期排序，确保是 1号→2号→3号... 的顺序
        val sortedRecords = records.sortedBy { it.date }

        // 统计用的计数器
        var gainDays = 0       // 增重天数
        var unchangedDays = 0  // 持平天数
        var lossDays = 0       // 减重天数
        var totalChange = 0f   // 总变化量

        // 记住"前一天"的早晨体重，用来和"今天"对比
        var prevWeight: Float? = null

        for (record in sortedRecords) {
            // 取今天的早晨体重，如果没有就跳过这天
            val currentWeight = record.morningWeight ?: continue

            if (prevWeight != null) {
                // 算差值：今天 - 昨天
                val diff = currentWeight - prevWeight
                when {
                    // 差值 > 0.01 → 重了（0.01是阈值，避免浮点精度误判）
                    diff > 0.01f -> gainDays++
                    // 差值 < -0.01 → 轻了
                    diff < -0.01f -> lossDays++
                    // 差值在 -0.01 ~ 0.01 之间 → 没变
                    else -> unchangedDays++
                }
                // 累加总变化量
                totalChange += diff
            }

            // 今天变成"昨天"，为下一天对比做准备
            prevWeight = currentWeight
        }

        // 通知2 —— 统计数据变了，首页统计卡片会更新
        // 举例：MonthlyStats(gainDays=1, unchangedDays=0, lossDays=1, totalChange=-0.4f)
        // 显示为：增1天  持平0天  减1天  变化-0.4斤
        _monthlyStats.postValue(MonthlyStats(gainDays, unchangedDays, lossDays, totalChange))
    }

    fun formatDate(y: Int, m: Int, d: Int): String {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m, d)
    }

    fun formatMonthTitle(y: Int, m: Int): String {
        return String.format(Locale.getDefault(), "%d 年 %02d 月", y, m)
    }

    fun loadAllRecords() {
        viewModelScope.launch {
            val records = dao.getAllRecords()
            _allRecords.postValue(records)
        }
    }

    fun loadRecentRecords() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, -7)
            val startDate = formatDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val records = dao.getRecentRecords(startDate)
            _recentRecords.postValue(records)
        }
    }
}
