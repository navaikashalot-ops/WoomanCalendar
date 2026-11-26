package com.example.mycycle.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.example.mycycle.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

data class BackupInfo(
    val fileLabel: String? = null,
    val lastOk: Long? = null,
    val lastError: String? = null,
)

/**
 * Резервные копии:
 *  1) filesDir/backup/snapshot.json — локально; попадает в Android Auto Backup (Google Drive);
 *  2) файл, выбранный пользователем через системный выбор (Google Drive) — без OAuth и регистрации.
 * Пустая база НИКОГДА не затирает копии автоматически.
 */
class BackupManager(private val ctx: Context, private val dao: AppDao) {
    private val prefs = ctx.getSharedPreferences("backup", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }
    private val mutex = Mutex()
    private val localFile get() = File(ctx.filesDir, "backup/snapshot.json")

    val info = MutableStateFlow(readInfo())

    private fun readInfo(): BackupInfo {
        val u = prefs.getString("uri", null)
        return BackupInfo(
            fileLabel = u?.let { displayName(Uri.parse(it)) ?: ctx.getString(R.string.bk_file_generic) },
            lastOk = prefs.getLong("lastOk", 0L).takeIf { it > 0 },
            lastError = prefs.getString("lastError", null),
        )
    }

    private fun displayName(uri: Uri): String? = try {
        ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    } catch (e: Exception) {
        null
    }

    private fun linkedUri(): Uri? = prefs.getString("uri", null)?.let { Uri.parse(it) }

    /** Запоминаем файл копии и постоянное разрешение на него. */
    fun link(uri: Uri) {
        try {
            ctx.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        } catch (e: Exception) {
            // разрешение могло быть выдано уже
        }
        prefs.edit().putString("uri", uri.toString()).apply()
        info.value = readInfo()
    }

    private suspend fun snapshot() = Snapshot(
        exportedAt = System.currentTimeMillis(),
        settings = dao.getSettings() ?: Settings(),
        cycles = dao.getCycles(),
        dayLogs = dao.getLogs(),
    )

    /** Записывает снимок локально и в связанный файл. force=true — даже если данных нет (ручное «сохранить»). */
    suspend fun writeAll(force: Boolean = false) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val snap = snapshot()
                if (!force && snap.cycles.isEmpty() && snap.dayLogs.isEmpty()) return@withLock
                val bytes = json.encodeToString(snap).toByteArray()
                var err: String? = null
                try {
                    val f = localFile
                    f.parentFile?.mkdirs()
                    val tmp = File(f.parentFile, "snapshot.json.tmp")
                    tmp.writeBytes(bytes)
                    if (!tmp.renameTo(f)) {
                        f.writeBytes(bytes)
                        tmp.delete()
                    }
                } catch (e: Exception) {
                    err = ctx.getString(R.string.err_local, e.message)
                }
                val uri = linkedUri()
                if (uri != null) {
                    try {
                        val out = ctx.contentResolver.openOutputStream(uri, "wt")
                            ?: throw IllegalStateException(ctx.getString(R.string.err_file_unavailable))
                        out.use { it.write(bytes) }
                    } catch (e: Exception) {
                        err = ctx.getString(R.string.err_drive, e.message)
                    }
                }
                prefs.edit().apply {
                    if (err == null) putLong("lastOk", System.currentTimeMillis())
                    putString("lastError", err)
                }.apply()
                info.value = readInfo()
            }
        }
    }

    /** Восстановление из выбранного файла: полностью заменяет текущие данные. */
    suspend fun restore(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val text = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                ?: return@withContext ctx.getString(R.string.restore_open_fail)
            val snap = json.decodeFromString<Snapshot>(text)
            if (snap.schemaVersion > 1) return@withContext ctx.getString(R.string.restore_too_new)
            dao.replaceAll(snap.cycles, snap.dayLogs, snap.settings)
            ctx.getString(R.string.restore_ok, snap.cycles.size, snap.dayLogs.size)
        } catch (e: Exception) {
            ctx.getString(R.string.restore_fail, e.message)
        }
    }

    /**
     * Если база пуста (после переустановки), а Auto Backup вернул snapshot.json — подхватываем его сами.
     */
    suspend fun autoRestoreLocalIfEmpty() {
        withContext(Dispatchers.IO) {
            try {
                if (dao.getCycles().isNotEmpty() || dao.getLogs().isNotEmpty()) return@withContext
                val f = localFile
                if (!f.exists()) return@withContext
                val snap = json.decodeFromString<Snapshot>(f.readText())
                if (snap.schemaVersion > 1) return@withContext
                dao.replaceAll(snap.cycles, snap.dayLogs, snap.settings)
            } catch (e: Exception) {
                // повреждённый файл — игнорируем, ничего не затираем
            }
        }
    }
}
