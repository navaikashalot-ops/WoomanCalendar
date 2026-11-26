package com.example.mycycle.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class Prediction(
    val avgCycle: Int,
    val avgPeriod: Int,
    /** Прогнозные даты начала следующих месячных (по порядку). */
    val predictedStarts: List<LocalDate>,
    val actualPeriodDays: Set<LocalDate>,
    val predictedPeriodDays: Set<LocalDate>,
    val ovulationDays: Set<LocalDate>,
    val fertileDays: Set<LocalDate>,
)

object Predictor {
    private const val HORIZON = 6      // на сколько циклов вперёд считаем
    private const val HISTORY = 6      // сколько последних циклов учитываем
    private const val LUTEAL = 14L     // дней от овуляции до следующих месячных

    fun compute(cycles: List<Cycle>, s: Settings): Prediction {
        val sorted = cycles.sortedBy { it.startDate }
        val starts = sorted.map { LocalDate.parse(it.startDate) }

        // Длины циклов: новые весят больше. Выбросы (<15 или >60 дней) отбрасываем.
        val lengths = starts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in 15..60 }
            .takeLast(HISTORY)
        val avgCycle = if (lengths.isEmpty()) {
            s.cycleLength
        } else {
            var sum = 0
            var wSum = 0
            lengths.forEachIndexed { i, l ->
                sum += l * (i + 1)
                wSum += i + 1
            }
            (sum.toDouble() / wSum).roundToInt()
        }

        val periodLens = sorted.mapNotNull { c ->
            c.endDate?.let { ChronoUnit.DAYS.between(LocalDate.parse(c.startDate), LocalDate.parse(it)).toInt() + 1 }
        }.filter { it in 1..14 }.takeLast(HISTORY)
        val avgPeriod = if (periodLens.isEmpty()) s.periodLength else periodLens.average().roundToInt()

        // Фактические дни месячных
        val actual = mutableSetOf<LocalDate>()
        sorted.forEachIndexed { i, c ->
            val start = starts[i]
            var end = c.endDate?.let { LocalDate.parse(it) } ?: start.plusDays((avgPeriod - 1).toLong())
            if (i + 1 < starts.size && !end.isBefore(starts[i + 1])) end = starts[i + 1].minusDays(1)
            if (end.isBefore(start)) end = start
            var d = start
            while (!d.isAfter(end)) {
                actual.add(d)
                d = d.plusDays(1)
            }
        }

        val predictedStarts = mutableListOf<LocalDate>()
        val predicted = mutableSetOf<LocalDate>()
        val ovulation = mutableSetOf<LocalDate>()
        val fertile = mutableSetOf<LocalDate>()
        if (starts.isNotEmpty()) {
            val last = starts.last()
            for (k in 1..HORIZON) {
                val ns = last.plusDays((k.toLong() * avgCycle))
                predictedStarts.add(ns)
                for (i in 0 until avgPeriod) predicted.add(ns.plusDays(i.toLong()))
                val ov = ns.minusDays(LUTEAL)
                ovulation.add(ov)
                for (i in -5..1) fertile.add(ov.plusDays(i.toLong()))
            }
        }
        return Prediction(avgCycle, avgPeriod, predictedStarts, actual, predicted, ovulation, fertile)
    }
}
