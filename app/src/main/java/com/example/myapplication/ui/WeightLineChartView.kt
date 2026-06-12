package com.example.myapplication.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.myapplication.data.WeightRecord
import kotlin.math.max
import kotlin.math.min

/**
 * 体重折线图 View
 * 支持灰色渐变填充 + 折线 + Y轴刻度 + X轴日期标签 + 最高/最低值标注
 */
class WeightLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var records: List<WeightRecord> = emptyList()

    // 折线画笔
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // 渐变填充画笔（运行时生成）
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Y轴文字画笔
    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        textSize = 28f
        textAlign = Paint.Align.RIGHT
    }

    // X轴文字画笔
    private val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    // 标注文字画笔（最高值红色，最低值绿色）
    private val maxLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F44336")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    private val minLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    // 网格线画笔
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22000000")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    fun setRecords(data: List<WeightRecord>) {
        records = data.sortedBy { it.date }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (records.isEmpty()) return

        val weights = records.mapNotNull { it.morningWeight ?: it.eveningWeight }
        if (weights.isEmpty()) return

        val paddingLeft = 70f
        val paddingRight = 20f
        val paddingTop = 36f
        val paddingBottom = 44f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // 计算Y轴范围（向外扩展2斤）
        val rawMax = weights.max()
        val rawMin = weights.min()
        val range = rawMax - rawMin
        val ext = if (range < 4f) 2f else range * 0.15f
        val yMax = Math.ceil((rawMax + ext).toDouble()).toFloat()
        val yMin = Math.floor((rawMin - ext).toDouble()).toFloat()
        val yRange = yMax - yMin

        // 计算X轴
        val n = records.size
        val xStep = if (n > 1) chartWidth / (n - 1).toFloat() else chartWidth

        // 收集点坐标
        val pointWeights = records.map { it.morningWeight ?: it.eveningWeight }
        val points = mutableListOf<PointF?>()
        for (i in records.indices) {
            val w = pointWeights[i]
            if (w != null) {
                val x = paddingLeft + i * xStep
                val y = paddingTop + chartHeight * (1f - (w - yMin) / yRange)
                points.add(PointF(x, y))
            } else {
                points.add(null)
            }
        }

        // 绘制Y轴刻度线
        val yTickCount = 4
        for (i in 0..yTickCount) {
            val yVal = yMin + yRange / yTickCount * i
            val y = paddingTop + chartHeight * (1f - (yVal - yMin) / yRange)
            // 网格线
            canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, gridPaint)
            // Y轴数值
            canvas.drawText(
                String.format("%.0f", yVal),
                paddingLeft - 8f,
                y + axisTextPaint.textSize / 3,
                axisTextPaint
            )
        }

        // 渐变填充路径
        val fillPath = Path()
        var started = false
        var firstX = 0f
        var lastX = 0f
        for (i in points.indices) {
            val p = points[i] ?: continue
            if (!started) {
                fillPath.moveTo(p.x, p.y)
                firstX = p.x
                started = true
            } else {
                fillPath.lineTo(p.x, p.y)
            }
            lastX = p.x
        }
        val bottomY = paddingTop + chartHeight
        fillPath.lineTo(lastX, bottomY)
        fillPath.lineTo(firstX, bottomY)
        fillPath.close()

        // 设置渐变
        fillPaint.shader = LinearGradient(
            0f, paddingTop,
            0f, paddingTop + chartHeight,
            Color.parseColor("#55AAAAAA"),
            Color.parseColor("#05AAAAAA"),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(fillPath, fillPaint)

        // 绘制折线
        val linePath = Path()
        var lineStarted = false
        for (p in points) {
            if (p == null) { lineStarted = false; continue }
            if (!lineStarted) {
                linePath.moveTo(p.x, p.y)
                lineStarted = true
            } else {
                linePath.lineTo(p.x, p.y)
            }
        }
        canvas.drawPath(linePath, linePaint)

        // 绘制X轴日期标签（每隔几个显示一次）
        val labelStep = max(1, n / 6)
        for (i in points.indices) {
            if (i % labelStep != 0 && i != n - 1) continue
            val p = points[i] ?: continue
            val date = records[i].date // "2026-06-01"
            val label = if (date.length >= 10) "${date.substring(5, 7)}/${date.substring(8, 10)}" else date
            canvas.drawText(label, p.x, height - 8f, xLabelPaint)
        }

        // 标注最高/最低值
        val maxW = weights.max()
        val minW = weights.min()
        val maxIdx = pointWeights.indexOfFirst { it == maxW }
        val minIdx = pointWeights.indexOfFirst { it == minW }

        if (maxIdx >= 0) {
            val p = points[maxIdx]
            if (p != null) {
                canvas.drawText(
                    String.format("%.1f", maxW),
                    p.x,
                    p.y - 10f,
                    maxLabelPaint
                )
            }
        }
        if (minIdx >= 0 && minIdx != maxIdx) {
            val p = points[minIdx]
            if (p != null) {
                canvas.drawText(
                    String.format("%.1f", minW),
                    p.x,
                    p.y + minLabelPaint.textSize + 2f,
                    minLabelPaint
                )
            }
        }
    }
}
