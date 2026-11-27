package com.example.mycycle.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CycleMergeTest {
    private fun c(id: String, start: String, end: String? = null) = Cycle(id, start, end)

    @Test fun startMarkedEarlierInPreviousMonthJoinsCycle() {
        val merged = mergeCloseCycles(listOf(c("nov", "2026-11-01", "2026-11-04"), c("oct", "2026-10-30")))
        assertEquals(listOf(c("oct", "2026-10-30", "2026-11-04")), merged)
    }

    @Test fun realCyclesStaySeparate() {
        val list = listOf(c("a", "2026-10-02", "2026-10-06"), c("b", "2026-10-30"))
        assertEquals(list, mergeCloseCycles(list))
    }

    @Test fun mergedCycleCountsDaysAcrossMonthBoundary() {
        val merged = mergeCloseCycles(listOf(c("nov", "2026-11-01", "2026-11-03"), c("oct", "2026-10-30")))
        val p = Predictor.compute(merged, Settings(cycleLength = 28, periodLength = 5))
        // 30, 31 октября и 1–3 ноября = 5 дней месячных
        assertEquals(5, p.avgPeriod)
        assertEquals(LocalDate.parse("2026-11-27"), p.predictedStarts.first())
    }

    @Test fun laterEndWins() {
        val merged = mergeCloseCycles(listOf(c("a", "2026-10-30", "2026-11-02"), c("b", "2026-11-01", "2026-11-04")))
        assertEquals("2026-11-04", merged.single().endDate)
    }
}
