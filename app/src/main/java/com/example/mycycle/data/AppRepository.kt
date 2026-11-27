package com.example.mycycle.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class AppRepository(
    private val dao: AppDao,
    val backup: BackupManager,
    private val scope: CoroutineScope,
) {
    val cycles = dao.observeCycles()
    val logs = dao.observeLogs()
    val settings = dao.observeSettings()
    val profile = dao.observeProfile()

    private val trigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @OptIn(FlowPreview::class)
    fun start() {
        scope.launch {
            backup.autoRestoreLocalIfEmpty()
            // Починка данных, где одно начало месячных отмечено дважды (см. mergeCloseCycles).
            if (dao.addCycleAndMerge(null)) requestBackup()
        }
        scope.launch { trigger.debounce(1500).collect { backup.writeAll() } }
    }

    fun requestBackup() {
        trigger.tryEmit(Unit)
    }

    /** Немедленная запись копии (вызывается при сворачивании приложения). */
    fun flush() {
        scope.launch { backup.writeAll() }
    }

    suspend fun startPeriod(date: LocalDate) {
        // Начало, отмеченное рядом с уже существующим (например, 30-го при отмеченном 1-м), сдвигает тот же цикл.
        dao.addCycleAndMerge(Cycle(UUID.randomUUID().toString(), date.toString(), null, System.currentTimeMillis()))
        requestBackup()
    }

    /** Возвращает false, если нет цикла, начавшегося не позже этой даты. */
    suspend fun endPeriod(date: LocalDate): Boolean {
        val target = dao.getCycles().lastOrNull { LocalDate.parse(it.startDate) <= date } ?: return false
        dao.upsertCycle(target.copy(endDate = date.toString(), updatedAt = System.currentTimeMillis()))
        requestBackup()
        return true
    }

    suspend fun deleteCycle(id: String) {
        dao.deleteCycle(id)
        requestBackup()
    }

    suspend fun saveLog(log: DayLog) {
        if (log.flow == 0 && log.symptoms.isBlank() && log.note.isBlank()) {
            dao.deleteLog(log.date)
        } else {
            dao.upsertLog(log.copy(updatedAt = System.currentTimeMillis()))
        }
        requestBackup()
    }

    suspend fun saveSettings(cycleLength: Int, periodLength: Int) {
        dao.upsertSettings(Settings(1, cycleLength, periodLength, System.currentTimeMillis()))
        requestBackup()
    }

    suspend fun saveProfile(name: String, avatar: String) {
        dao.upsertProfile(Profile(1, name, avatar, true, System.currentTimeMillis()))
        requestBackup()
    }

    suspend fun completeOnboarding(
        name: String,
        avatar: String,
        lastStart: LocalDate?,
        cycleLength: Int,
        periodLength: Int,
    ) {
        val now = System.currentTimeMillis()
        val cycle = lastStart?.let { Cycle(UUID.randomUUID().toString(), it.toString(), null, now) }
        val settings = if (lastStart != null) Settings(1, cycleLength, periodLength, now) else null
        dao.completeOnboarding(cycle, settings, Profile(1, name, avatar, true, now))
        requestBackup()
    }
}
