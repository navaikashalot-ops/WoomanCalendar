package com.example.mycycle.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mycycle.CycleApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Срабатывание напоминания и перезагрузка телефона.
 * После перезагрузки будильник ставится заново сам: запуск процесса создаёт CycleApp,
 * а Reminders.start() пересчитывает расписание.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as CycleApp
        if (intent.action != Reminders.ACTION_REMIND) return
        intent.getStringExtra(Reminders.EXTRA_START)?.let { app.reminders.show(LocalDate.parse(it)) }
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                app.reminders.rescheduleNow()
            } finally {
                pending.finish()
            }
        }
    }
}
