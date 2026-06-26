package com.example.myapplication.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * 饮食记录条目
 * 每条记录代表一次用餐（早餐/午餐/晚餐/加餐）
 */
@Entity(tableName = "diet_entries")
data class DietEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,          // 格式: "2026-05-27"
    val mealType: String,      // "breakfast" / "lunch" / "dinner" / "snack"
    val time: String,          // 格式: "08:58"
    val foodContent: String,   // 食物内容，如 "鸡蛋"
    val createdAt: Long = System.currentTimeMillis()
)
