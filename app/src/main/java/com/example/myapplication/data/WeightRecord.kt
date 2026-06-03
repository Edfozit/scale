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
    val createdAt: Long = System.currentTimeMillis()
)
