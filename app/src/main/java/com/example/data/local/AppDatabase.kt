package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CalculationHistoryDao
import com.example.data.local.dao.RateAlertDao
import com.example.data.local.dao.RateSnapshotDao
import com.example.data.local.entity.CalculationHistoryEntity
import com.example.data.local.entity.RateAlertEntity
import com.example.data.local.entity.RateSnapshotEntity

@Database(
    entities = [
        CalculationHistoryEntity::class,
        RateSnapshotEntity::class,
        RateAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationHistoryDao(): CalculationHistoryDao
    abstract fun rateSnapshotDao(): RateSnapshotDao
    abstract fun rateAlertDao(): RateAlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tasacambio_ve.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
