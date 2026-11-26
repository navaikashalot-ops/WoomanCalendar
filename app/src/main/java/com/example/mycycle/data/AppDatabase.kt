package com.example.mycycle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Должен в точности совпадать со схемой, которую Room генерирует для Profile. */
const val MIGRATION_1_2_SQL =
    "CREATE TABLE IF NOT EXISTS profile (id INTEGER NOT NULL, name TEXT NOT NULL, " +
        "avatar TEXT NOT NULL, onboarded INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))"

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(MIGRATION_1_2_SQL)
    }
}

/**
 * ВАЖНО: при любом изменении схемы — увеличить version и добавить явную Migration.
 * fallbackToDestructiveMigration() НЕ использовать: он стирает данные.
 */
@Database(
    entities = [Cycle::class, DayLog::class, Settings::class, Profile::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "cycle.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
