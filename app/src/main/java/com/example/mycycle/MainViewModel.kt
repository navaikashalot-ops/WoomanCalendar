package com.example.mycycle

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycycle.data.BackupInfo
import com.example.mycycle.data.Cycle
import com.example.mycycle.data.DayLog
import com.example.mycycle.data.Predictor
import com.example.mycycle.data.Prediction
import com.example.mycycle.data.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiState(
    val cycles: List<Cycle> = emptyList(),
    val logs: Map<String, DayLog> = emptyMap(),
    val settings: Settings = Settings(),
    val prediction: Prediction = Predictor.compute(emptyList(), Settings()),
    val loaded: Boolean = false,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as CycleApp).repo

    val state: StateFlow<UiState> = combine(repo.cycles, repo.logs, repo.settings) { c, l, s ->
        val st = s ?: Settings()
        UiState(c, l.associateBy { it.date }, st, Predictor.compute(c, st), true)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    val backupInfo: StateFlow<BackupInfo> = repo.backup.info

    private fun toast(textRes: Int) {
        toastText(getApplication<Application>().getString(textRes))
    }

    private fun toastText(text: String) {
        Toast.makeText(getApplication<Application>(), text, Toast.LENGTH_SHORT).show()
    }

    fun startPeriod(date: LocalDate) = viewModelScope.launch {
        repo.startPeriod(date)
        toast(R.string.toast_start_marked)
    }

    fun endPeriod(date: LocalDate) = viewModelScope.launch {
        if (repo.endPeriod(date)) toast(R.string.toast_end_marked) else toast(R.string.toast_need_start)
    }

    fun deleteCycleStartingAt(date: LocalDate) = viewModelScope.launch {
        val c = state.value.cycles.firstOrNull { it.startDate == date.toString() }
        if (c != null) {
            repo.deleteCycle(c.id)
            toast(R.string.toast_cycle_deleted)
        }
    }

    fun saveLog(log: DayLog) = viewModelScope.launch { repo.saveLog(log) }

    fun saveSettings(cycleLength: Int, periodLength: Int) = viewModelScope.launch {
        repo.saveSettings(cycleLength, periodLength)
    }

    /** Пользователь создал файл копии (в Google Drive) — запоминаем и сразу пишем. */
    fun createBackupFile(uri: Uri) = viewModelScope.launch {
        repo.backup.link(uri)
        repo.backup.writeAll(force = true)
        toast(R.string.toast_file_selected)
    }

    fun backupNow() = viewModelScope.launch {
        repo.backup.writeAll(force = true)
        toast(if (repo.backup.info.value.lastError == null) R.string.toast_backup_saved else R.string.toast_backup_error)
    }

    fun restore(uri: Uri) = viewModelScope.launch {
        val msg = repo.backup.restore(uri)
        repo.backup.link(uri)
        repo.requestBackup()
        toastText(msg)
    }

    fun flushBackup() = repo.flush()
}
