package com.example.mycycle.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mycycle.MainActivity
import com.example.mycycle.R
import com.example.mycycle.data.AppDao
import com.example.mycycle.data.Predictor
import com.example.mycycle.data.ReminderConfig
import com.example.mycycle.data.Settings
import com.example.mycycle.data.nextReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Напоминание о скором начале месячных: одно на цикл, за N дней в заданное время.
 * Настройки хранятся в SharedPreferences (это настройка телефона, в копию не попадает).
 * Будильник пересчитывается при любом изменении циклов, настроек цикла или напоминания.
 */
class Reminders(private val ctx: Context, private val dao: AppDao, private val scope: CoroutineScope) {
    private val prefs = ctx.getSharedPreferences("reminder", Context.MODE_PRIVATE)
    private val _config = MutableStateFlow(load())
    val config: StateFlow<ReminderConfig> = _config

    fun start() {
        createChannel()
        scope.launch {
            combine(dao.observeCycles(), dao.observeSettings(), _config) { c, s, cfg -> Triple(c, s, cfg) }
                .collect { (c, s, cfg) -> schedule(Predictor.compute(c, s ?: Settings()).predictedStarts, cfg) }
        }
    }

    fun update(cfg: ReminderConfig) {
        prefs.edit()
            .putBoolean("enabled", cfg.enabled)
            .putInt("days", cfg.daysBefore)
            .putInt("hour", cfg.time.hour)
            .putInt("minute", cfg.time.minute)
            .apply()
        _config.value = cfg
    }

    /** Вызывается после срабатывания: ставит напоминание на следующий цикл. */
    suspend fun rescheduleNow() {
        schedule(Predictor.compute(dao.getCycles(), dao.getSettings() ?: Settings()).predictedStarts, _config.value)
    }

    private fun load() = ReminderConfig(
        enabled = prefs.getBoolean("enabled", true),
        daysBefore = prefs.getInt("days", 5),
        time = LocalTime.of(prefs.getInt("hour", 9), prefs.getInt("minute", 0)),
    )

    private fun schedule(starts: List<LocalDate>, cfg: ReminderConfig) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val planned = nextReminder(starts, cfg, LocalDateTime.now())
        if (planned == null) {
            am.cancel(alarmIntent(null))
            return
        }
        val millis = planned.at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // Неточный будильник: точные требуют отдельного разрешения, а минутная точность тут не нужна.
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, alarmIntent(planned.periodStart))
    }

    private fun alarmIntent(start: LocalDate?): PendingIntent {
        val i = Intent(ctx, ReminderReceiver::class.java).setAction(ACTION_REMIND)
        if (start != null) i.putExtra(EXTRA_START, start.toString())
        return PendingIntent.getBroadcast(
            ctx, 1, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val ch = NotificationChannel(CHANNEL, ctx.getString(R.string.rem_channel), NotificationManager.IMPORTANCE_DEFAULT)
        ctx.getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
    }

    fun show(start: LocalDate) {
        val nm = NotificationManagerCompat.from(ctx)
        if (!nm.areNotificationsEnabled()) return
        val days = ChronoUnit.DAYS.between(LocalDate.now(), start).coerceAtLeast(0).toInt()
        val date = start.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(ctx.getString(R.string.rem_title))
            .setContentText(ctx.resources.getQuantityString(R.plurals.rem_text, days, days, date))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            nm.notify(NOTIFICATION_ID, n)
        } catch (e: SecurityException) {
            // разрешение на уведомления отозвано — молча пропускаем
        }
    }

    companion object {
        const val ACTION_REMIND = "com.example.mycycle.REMIND"
        const val EXTRA_START = "start"
        private const val CHANNEL = "period_reminder"
        private const val NOTIFICATION_ID = 1
    }
}
