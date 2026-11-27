package com.example.mycycle.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Два начала месячных ближе [MIN_CYCLE_DAYS] дней друг к другу — это один и тот же цикл
 * (например, начало сначала отметили 1-го, а потом «Начало» на 30-м прошлого месяца).
 * Склеиваем их: раннее начало, самый поздний конец, id раннего цикла.
 */
const val MIN_CYCLE_DAYS = 15

fun mergeCloseCycles(cycles: List<Cycle>): List<Cycle> {
    val out = mutableListOf<Cycle>()
    for (c in cycles.sortedBy { it.startDate }) {
        val prev = out.lastOrNull()
        if (prev != null &&
            ChronoUnit.DAYS.between(LocalDate.parse(prev.startDate), LocalDate.parse(c.startDate)) < MIN_CYCLE_DAYS
        ) {
            val end = listOfNotNull(prev.endDate, c.endDate).maxOrNull()
            out[out.lastIndex] = prev.copy(endDate = end, updatedAt = maxOf(prev.updatedAt, c.updatedAt))
        } else {
            out.add(c)
        }
    }
    return out
}
