package com.example.myapplication.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Database(entities = [WeightRecord::class, DietEntry::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun dietEntryDao(): DietEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scale_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
