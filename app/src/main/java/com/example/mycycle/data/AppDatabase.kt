package com.example.mycycle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * ВАЖНО: при любом изменении схемы — увеличить version и добавить явную Migration.
 * fallbackToDestructiveMigration() НЕ использовать: он стирает данные.
 */
@Database(
    entities = [Cycle::class, DayLog::class, Settings::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "cycle.db")
                // .addMigrations(MIGRATION_1_2, ...)
                .build()
    }
}
