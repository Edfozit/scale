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

    init {
        val today = formatDate(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH) + 1,
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        _selectedDate.value = today
        loadMonthData()
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
            updateSelectedRecord()
        }
    }

    private fun loadMonthData() {
        val y = _currentYear.value ?: return
        val m = _currentMonth.value ?: return

        val startDate = formatDate(y, m, 1)
        val cal = Calendar.getInstance()
        cal.set(y, m - 1, 1)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val endDate = formatDate(y, m, lastDay)

        viewModelScope.launch {
            val records = dao.getByDateRange(startDate, endDate)
            val map = records.associateBy { it.date }
            _monthlyRecords.postValue(map)
            calculateStats(records, lastDay)
            updateSelectedRecord()
        }
    }

    private fun updateSelectedRecord() {
        val date = _selectedDate.value ?: return
        viewModelScope.launch {
            val record = dao.getByDate(date)
            _selectedRecord.postValue(record)
        }
    }

    private fun calculateStats(records: List<WeightRecord>, lastDay: Int) {
        val sortedRecords = records.sortedBy { it.date }
        var gainDays = 0
        var unchangedDays = 0
        var lossDays = 0
        var totalChange = 0f

        var prevWeight: Float? = null

        for (record in sortedRecords) {
            val currentWeight = record.morningWeight ?: continue
            if (prevWeight != null) {
                val diff = currentWeight - prevWeight
                when {
                    diff > 0.01f -> gainDays++
                    diff < -0.01f -> lossDays++
                    else -> unchangedDays++
                }
                totalChange += diff
            }
            prevWeight = currentWeight
        }

        _monthlyStats.postValue(MonthlyStats(gainDays, unchangedDays, lossDays, totalChange))
    }

    fun formatDate(y: Int, m: Int, d: Int): String {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m, d)
    }

    fun formatMonthTitle(y: Int, m: Int): String {
        return String.format(Locale.getDefault(), "%d 年 %02d 月", y, m)
    }
}
