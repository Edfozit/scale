package com.example.myapplication.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.myapplication.data.WeightRecord
import java.util.Calendar
import java.util.Locale

class WeightCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 数据
    private var records: Map<String, WeightRecord> = emptyMap()
    private var selectedDate: String = ""
    private var year: Int = 2026
    private var month: Int = 6

    // 日历计算
    private var firstDayOfWeek: Int = 0
    private var daysInMonth: Int = 30

    // 绘制参数
    private val cellWidth: Float
        get() = width / 7f

    // 统一格子高度（所有行一样高，日期数字对齐）
    private val cellHeight = 175f

    // Paint - 日期数字
    private val datePaintSelected = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 42f
        color = Color.parseColor("#1A1A1A")
    }

    private val datePaintNormal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 40f
        color = Color.parseColor("#1A1A1A")
    }

    // Paint - 早/中/晚 标签 + 值
    private val weightLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        color = Color.parseColor("#9E9E9E")
    }

    private val weightValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        color = Color.parseColor("#424242")
    }

    // Paint - 变化值
    private val changeGainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color = Color.parseColor("#F44336")
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val changeLossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color = Color.parseColor("#4CAF50")
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val changeUnchangedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color = Color.parseColor("#9E9E9E")
    }

    // Paint - 星期表头
    private val weekHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        color = Color.parseColor("#757575")
    }

    // Paint - 选中日期背景
    private val selectedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D8D8D8")
    }

    // Paint - 普通格子白色背景
    private val cellBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    // Paint - 今日标记
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 42f
        color = Color.parseColor("#2196F3")
    }

    private val textBounds = Rect()

    var onDateSelected: ((date: String) -> Unit)? = null

    init {
        val cal = Calendar.getInstance()
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH) + 1
        selectedDate = formatDate(year, month, cal.get(Calendar.DAY_OF_MONTH))
        recalculate()
        // 背景透明，让主题色透出来
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun setYearMonth(y: Int, m: Int) {
        year = y
        month = m
        recalculate()
        invalidate()
    }

    fun getYear(): Int = year
    fun getMonth(): Int = month

    fun setRecords(records: Map<String, WeightRecord>) {
        this.records = records
        requestLayout()
        invalidate()
    }

    fun setSelectedDate(date: String) {
        selectedDate = date
        invalidate()
    }

    fun getSelectedDate(): String = selectedDate

    private fun recalculate() {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getCellHeight(day: Int): Float = cellHeight

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)

        // 计算总高度：表头行 + 各行日期
        val headerHeight = cellHeight
        var totalHeight = headerHeight

        var dayCounter = 1
        val startOffset = firstDayOfWeek - 1
        var rowMaxHeight = 0f

        for (cellIndex in 0 until 42) {
            if (cellIndex < startOffset || dayCounter > daysInMonth) {
                rowMaxHeight = maxOf(rowMaxHeight, cellHeight)
            } else {
                rowMaxHeight = maxOf(rowMaxHeight, getCellHeight(dayCounter))
                dayCounter++
            }

            if ((cellIndex + 1) % 7 == 0) {
                totalHeight += rowMaxHeight
                rowMaxHeight = 0f
            }
        }
        if (dayCounter <= daysInMonth && rowMaxHeight > 0) {
            totalHeight += rowMaxHeight
        }

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(totalHeight.toInt(), heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cw = cellWidth
        val ch = cellHeight
        val headerHeight = cellHeight

        // 绘制星期表头
        val weekLabels = listOf("日", "一", "二", "三", "四", "五", "六")
        for (i in weekLabels.indices) {
            val label = weekLabels[i]
            weekHeaderPaint.getTextBounds(label, 0, label.length, textBounds)
            val x = cw * i + (cw - textBounds.width()) / 2f
            val y = headerHeight / 2f + textBounds.height() / 2f
            canvas.drawText(label, x, y, weekHeaderPaint)
        }

        // 绘制日期格子
        var dayCounter = 1
        val startOffset = firstDayOfWeek - 1
        var currentY = headerHeight
        var rowMaxHeight = 0f
        val rowHeights = mutableListOf<Float>()

        // 先算出每行的高度
        for (cellIndex in 0 until 42) {
            if (cellIndex < startOffset || dayCounter > daysInMonth) {
                rowMaxHeight = maxOf(rowMaxHeight, cellHeight)
            } else {
                rowMaxHeight = maxOf(rowMaxHeight, getCellHeight(dayCounter))
                dayCounter++
            }

            if ((cellIndex + 1) % 7 == 0) {
                rowHeights.add(rowMaxHeight)
                rowMaxHeight = 0f
            }
        }

        // 绘制每行
        dayCounter = 1
        currentY = headerHeight

        for (rowIndex in rowHeights.indices) {
            val rowHeight = rowHeights[rowIndex]

            for (col in 0 until 7) {
                val cellIndex = rowIndex * 7 + col
                if (cellIndex < startOffset || dayCounter > daysInMonth) continue

                val dateStr = formatDate(year, month, dayCounter)
                val record = records[dateStr]
                val prevRecord = findLatestPreviousRecord(dateStr)
                val isSelected = dateStr == selectedDate
                val isToday = dateStr == formatDate(
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1,
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                )

                val left = cw * col
                val centerX = left + cw / 2f

                // 格子间距
                val gap = 5f
                val cellLeft = left + gap
                val cellTop = currentY + gap
                val cellRight = left + cw - gap
                val cellBottom = currentY + rowHeight - gap
                val radius = 18f

                // 先画白色圆角卡片背景（所有日期格子）
                canvas.drawRoundRect(cellLeft, cellTop, cellRight, cellBottom, radius, radius, cellBgPaint)

                // 选中日期覆盖灰色背景
                if (isSelected) {
                    canvas.drawRoundRect(cellLeft, cellTop, cellRight, cellBottom, radius, radius, selectedBgPaint)
                }

                val hasData = record?.morningWeight != null || record?.eveningWeight != null

                // 统一：所有日期数字都在同一位置（格子顶部），保证对齐
                val dateTopY = currentY + 10f
                val dayText = dayCounter.toString()
                val datePaint = when {
                    isSelected -> datePaintSelected
                    isToday -> todayPaint
                    else -> datePaintNormal
                }
                datePaint.getTextBounds(dayText, 0, dayText.length, textBounds)
                canvas.drawText(dayText, centerX - textBounds.width() / 2f, dateTopY + textBounds.height(), datePaint)

                if (hasData) {
                    // 有数据：日期数字下方 → 早 → 晚 → 变化 （日历）
                    var yOffset = dateTopY + 56f

                    // 2. 早 XX.X
                    if (record?.morningWeight != null) {
                        val text = " ${String.format("%.1f", record.morningWeight)}"
                        weightValuePaint.getTextBounds(text, 0, text.length, textBounds)
                        canvas.drawText(text, centerX - textBounds.width() / 2f, yOffset + textBounds.height(), weightValuePaint)
                        yOffset += 30f
                    }

                    // 4. 晚 XX.X
                    if (record?.eveningWeight != null) {
                        val text = " ${String.format("%.1f", record.eveningWeight)}"
                        weightValuePaint.getTextBounds(text, 0, text.length, textBounds)
                        canvas.drawText(text, centerX - textBounds.width() / 2f, yOffset + textBounds.height(), weightValuePaint)
                        yOffset += 30f
                    }

                    // 5. 变化值（与前一日早重对比）
                    if (record?.morningWeight != null && prevRecord?.morningWeight != null) {
                        val change = record.morningWeight - prevRecord.morningWeight
                        val changeText: String
                        val changePaint: Paint

                        when {
                            change > 0.01f -> {
                                changeText = String.format("%.1f ↑", change)
                                changePaint = changeGainPaint
                            }
                            change < -0.01f -> {
                                changeText = String.format("%.1f ↓", -change)
                                changePaint = changeLossPaint
                            }
                            else -> {
                                changeText = "0.0 →"
                                changePaint = changeUnchangedPaint
                            }
                        }
                        changePaint.getTextBounds(changeText, 0, changeText.length, textBounds)
                        canvas.drawText(changeText, centerX - textBounds.width() / 2f, yOffset + textBounds.height(), changePaint)
                    }

                } // 无数据：日期数字已经在上面同一位置绘制，无需额外处理

                dayCounter++
            }
            currentY += rowHeight
            if (dayCounter > daysInMonth) break
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y

            val cw = cellWidth
            val headerHeight = cellHeight

            if (y < headerHeight) return true

            val col = (x / cw).toInt()
            if (col < 0 || col >= 7) return true

            // 找出点击了哪一行
            val rowHeights = calculateRowHeights()
            var currentY = headerHeight
            var clickedRow = -1

            for ((rowIndex, rowHeight) in rowHeights.withIndex()) {
                if (y >= currentY && y < currentY + rowHeight) {
                    clickedRow = rowIndex
                    break
                }
                currentY += rowHeight
            }

            if (clickedRow < 0) return true

            val startOffset = firstDayOfWeek - 1
            val cellIndex = clickedRow * 7 + col
            val day = cellIndex - startOffset + 1

            if (day in 1..daysInMonth) {
                selectedDate = formatDate(year, month, day)
                onDateSelected?.invoke(selectedDate)
                invalidate()
            }
        }
        return true
    }

    private fun calculateRowHeights(): List<Float> {
        val startOffset = firstDayOfWeek - 1
        var dayCounter = 1
        var rowMaxHeight = 0f
        val rowHeights = mutableListOf<Float>()

        for (cellIndex in 0 until 42) {
            if (cellIndex < startOffset || dayCounter > daysInMonth) {
                rowMaxHeight = maxOf(rowMaxHeight, cellHeight)
            } else {
                rowMaxHeight = maxOf(rowMaxHeight, getCellHeight(dayCounter))
                dayCounter++
            }

            if ((cellIndex + 1) % 7 == 0) {
                rowHeights.add(rowMaxHeight)
                rowMaxHeight = 0f
            }
        }

        return rowHeights
    }

    private fun formatDate(y: Int, m: Int, d: Int): String {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m, d)
    }

    private fun getPreviousDate(dateStr: String): String {
        val parts = dateStr.split("-")
        val cal = Calendar.getInstance()
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return String.format(
            Locale.getDefault(),
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** 查找最近一个有早体重数据的前置记录，跳过中间无数据的日期 */
    private fun findLatestPreviousRecord(dateStr: String): WeightRecord? {
        var currentDateStr = dateStr
        for (i in 0 until 30) {
            currentDateStr = getPreviousDate(currentDateStr)
            val record = records[currentDateStr]
            if (record?.morningWeight != null) {
                return record
            }
        }
        return null
    }
}
