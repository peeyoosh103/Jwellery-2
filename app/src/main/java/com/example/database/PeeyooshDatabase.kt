package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CalculationHistoryEntity::class], version = 1, exportSchema = false)
abstract class PeeyooshDatabase : RoomDatabase() {

    abstract fun calculationHistoryDao(): CalculationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: PeeyooshDatabase? = null

        fun getDatabase(context: Context): PeeyooshDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PeeyooshDatabase::class.java,
                    "peeyoosh_creation_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
