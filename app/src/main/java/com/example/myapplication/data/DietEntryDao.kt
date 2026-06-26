package com.example.myapplication.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface DietEntryDao {

    /** 查询指定日期的所有饮食记录，按时间排序 */
    @Query("SELECT * FROM diet_entries WHERE date = :date ORDER BY time ASC")
    suspend fun getByDate(date: String): List<DietEntry>

    /** 插入一条饮食记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DietEntry): Long

    /** 删除一条饮食记录 */
    @Query("DELETE FROM diet_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** 删除指定日期的所有饮食记录 */
    @Query("DELETE FROM diet_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
