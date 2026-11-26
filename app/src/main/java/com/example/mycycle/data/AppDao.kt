package com.example.mycycle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AppDao {
    @Query("SELECT * FROM cycles ORDER BY startDate")
    abstract fun observeCycles(): Flow<List<Cycle>>

    @Query("SELECT * FROM cycles ORDER BY startDate")
    abstract suspend fun getCycles(): List<Cycle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertCycle(c: Cycle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertCycles(list: List<Cycle>)

    @Query("DELETE FROM cycles WHERE id = :id")
    abstract suspend fun deleteCycle(id: String)

    @Query("DELETE FROM cycles")
    abstract suspend fun clearCycles()

    @Query("SELECT * FROM day_logs ORDER BY date")
    abstract fun observeLogs(): Flow<List<DayLog>>

    @Query("SELECT * FROM day_logs ORDER BY date")
    abstract suspend fun getLogs(): List<DayLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertLog(l: DayLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertLogs(list: List<DayLog>)

    @Query("DELETE FROM day_logs WHERE date = :date")
    abstract suspend fun deleteLog(date: String)

    @Query("DELETE FROM day_logs")
    abstract suspend fun clearLogs()

    @Query("SELECT * FROM settings WHERE id = 1")
    abstract fun observeSettings(): Flow<Settings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    abstract suspend fun getSettings(): Settings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertSettings(s: Settings)

    @Transaction
    open suspend fun replaceAll(cycles: List<Cycle>, logs: List<DayLog>, settings: Settings) {
        clearCycles()
        clearLogs()
        upsertCycles(cycles)
        upsertLogs(logs)
        upsertSettings(settings.copy(id = 1))
    }
}
