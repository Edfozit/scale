package com.example.myapplication.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey
    val date: String,              // 格式: "2026-06-03"
    val morningWeight: Float?,     // 早晨体重（斤）
    val eveningWeight: Float?,     // 晚间体重（斤）
    val foodNote: String?,         // 饮食记录
    val exerciseNote: String?,     // 运动记录
    val waterIntake: String? = null,      // 饮水记录
    val diaryNote: String? = null,        // 日记
    val measurementNote: String? = null,  // 围度
    val dietPlan: String? = null,         // 饮食计划
    val bowelRecord: String? = null,      // 排便记录（"0"/"1" 七天逗号分隔）
    val habitNote: String? = null,        // 习惯
    val createdAt: Long = System.currentTimeMillis()
)
