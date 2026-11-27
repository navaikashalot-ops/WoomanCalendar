package com.example.mycycle.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ReminderConfig(
    val enabled: Boolean = true,
    val daysBefore: Int = 5,
    val time: LocalTime = LocalTime.of(9, 0),
)

data class PlannedReminder(val periodStart: LocalDate, val at: LocalDateTime)

/**
 * Ближайшее напоминание: за [ReminderConfig.daysBefore] дней до прогнозного начала, в [ReminderConfig.time].
 * Если для ближайшего прогноза время уже прошло — берётся следующий прогноз (одно напоминание на цикл).
 */
fun nextReminder(predictedStarts: List<LocalDate>, cfg: ReminderConfig, now: LocalDateTime): PlannedReminder? {
    if (!cfg.enabled) return null
    return predictedStarts.sorted().firstNotNullOfOrNull { start ->
        val at = start.minusDays(cfg.daysBefore.toLong()).atTime(cfg.time)
        if (at.isAfter(now)) PlannedReminder(start, at) else null
    }
}
