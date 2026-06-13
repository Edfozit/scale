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

data class ChartStats(
    val maxWeight: Float,
    val minWeight: Float,
    val totalDelta: Float,
    val recordDays: Int,
    val avgWeight: Float
)

class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).weightRecordDao()

    private val _records = MutableLiveData<List<WeightRecord>>()
    val records: LiveData<List<WeightRecord>> = _records

    private val _stats = MutableLiveData<ChartStats?>()
    val stats: LiveData<ChartStats?> = _stats

    private val _rangeLabel = MutableLiveData("最近 30 天")
    val rangeLabel: LiveData<String> = _rangeLabel

    // 当前选择的天数范围
    var currentDays = 30
        private set

    init {
        loadData(30)
    }

    fun loadData(days: Int) {
        currentDays = days
        _rangeLabel.value = "最近 $days 天"
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val endDate = formatDate(cal)
            cal.add(Calendar.DAY_OF_YEAR, -(days - 1))
            val startDate = formatDate(cal)

            val data = dao.getByDateRange(startDate, endDate)
            _records.postValue(data)
            calculateStats(data)
        }
    }

    private fun calculateStats(data: List<WeightRecord>) {
        val weights = data.mapNotNull { it.morningWeight ?: it.eveningWeight }
        if (weights.isEmpty()) {
            _stats.postValue(null)
            return
        }
        val maxW = weights.max()
        val minW = weights.min()
        val avgW = weights.average().toFloat()
        val delta = (weights.last() - weights.first())
        _stats.postValue(ChartStats(maxW, minW, delta, weights.size, avgW))
    }

    private fun formatDate(cal: Calendar): String {
        return String.format(
            Locale.getDefault(),
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
